package com.zorykai.fishing;
import com.google.gson.*;
import java.nio.file.*;
import java.util.*;
import java.math.*;
/** Independently editable, versioned rules. Reload builds/validates a complete replacement first. */
public final class GameConfig {
 public final Map<String,JsonObject> files;public final Path root;
 public GameConfig(Path root)throws Exception{this.root=root;var next=new LinkedHashMap<String,JsonObject>();for(var e:defaults().entrySet()){var p=root.resolve(e.getKey()+".json");if(!Files.exists(p))FishingConfiguration.write(p,versioned(e.getValue()));var j=JsonParser.parseString(Files.readString(p)).getAsJsonObject();if(num(j,"schemaVersion",1)!=1)throw new IllegalArgumentException("schemaVersion não suportada: "+e.getKey());next.put(e.getKey(),j);}files=Map.copyOf(next);validate();backup();}
 static JsonObject versioned(String text){var j=JsonParser.parseString(text).getAsJsonObject();j.addProperty("schemaVersion",1);return j;}
 void backup()throws Exception{var dir=root.resolve("backups");Files.createDirectories(dir);var target=dir.resolve("config-"+System.currentTimeMillis()+".zip");try(var zip=new java.util.zip.ZipOutputStream(Files.newOutputStream(target))){for(var e:files.entrySet()){zip.putNextEntry(new java.util.zip.ZipEntry(e.getKey()+".json"));zip.write(e.getValue().toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));zip.closeEntry();}for(var p:java.util.List.of(root.resolve("config.json"),root.resolve("mapa.json"))){if(Files.exists(p)){zip.putNextEntry(new java.util.zip.ZipEntry(p.getFileName().toString()));Files.copy(p,zip);zip.closeEntry();}}if(Files.isDirectory(root.resolve("areas")))try(var areas=Files.list(root.resolve("areas"))){for(var p:areas.filter(x->x.toString().endsWith(".json")).toList()){zip.putNextEntry(new java.util.zip.ZipEntry("areas/"+p.getFileName()));Files.copy(p,zip);zip.closeEntry();}}}try(var list=Files.list(dir)){var old=list.filter(x->x.getFileName().toString().startsWith("config-")).sorted(java.util.Comparator.reverseOrder()).toList();for(int i=10;i<old.size();i++)Files.deleteIfExists(old.get(i));}}
 public JsonObject get(String name){return Objects.requireNonNull(files.get(name),name);}
 public static String str(JsonObject j,String key,String fallback){return j.has(key)&&!j.get(key).isJsonNull()?j.get(key).getAsString():fallback;}
 public static int num(JsonObject j,String key,int fallback){return j.has(key)?j.get(key).getAsInt():fallback;}
 public static double decimal(JsonObject j,String key,double fallback){return j.has(key)?j.get(key).getAsDouble():fallback;}
 public static boolean bool(JsonObject j,String key,boolean fallback){return j.has(key)?j.get(key).getAsBoolean():fallback;}
 public static List<JsonObject> rows(JsonObject j){var a=j.getAsJsonArray("values");return a==null?List.of():a.asList().stream().map(JsonElement::getAsJsonObject).toList();}
 public JsonObject definition(String file,String id){return rows(get(file)).stream().filter(x->str(x,"id","").equals(id)).findFirst().orElseThrow(()->new IllegalArgumentException("Definição inexistente: "+file+"/"+id));}
 public int level(long xp){int max=num(get("levels"),"maximumLevel",100);double base=decimal(get("levels"),"baseXp",100),growth=decimal(get("levels"),"growth",1.13);double threshold=0;for(int level=1;level<max;level++){threshold+=Math.ceil(base*Math.pow(growth,level-1));if(xp<threshold)return level;}return max;}
 void validate(){
  if(!bool(get("inventory"),"blockExternalTransfers",true)||!bool(get("inventory"),"quarantineUnexpectedItems",true)||!bool(get("integrations"),"guard",true))throw new IllegalArgumentException("Proteções de inventário e validação Guard são obrigatórias");
  for(var f:List.of("rods","baits","pokeballs","upgrades","time-shop","missions/daily","missions/weekly","missions/story","achievements","events/example","tournaments/example","treasures/default")){
   var seen=new HashSet<String>();for(var row:rows(get(f))){String id=str(row,"id","");if(!id.matches("[a-z0-9_-]{1,48}")||!seen.add(id))throw new IllegalArgumentException("ID inválido/repetido em "+f);for(String key:List.of("price","captureChance","weight","xpMultiplier","sizeMultiplier"))if(row.has(key)){double v=row.get(key).getAsDouble();if(!Double.isFinite(v)||v<0||v>1e9)throw new IllegalArgumentException("Valor inválido em "+f+"/"+id+"/"+key);}if(row.has("captureChance")&&decimal(row,"captureChance",0)>1)throw new IllegalArgumentException("captureChance deve ser 0..1");}
  }
  definition("rods","inicial");definition("baits","comum");definition("pokeballs","basica");
  if(num(get("timers"),"normalMinutes",60)<1||num(get("timers"),"cycleHours",24)<1||num(get("timers"),"maximumAccumulatedMinutes",360)<1)throw new IllegalArgumentException("Tempos inválidos");
  if(num(get("levels"),"maximumLevel",100)<2||num(get("levels"),"maximumLevel",100)>1000||decimal(get("levels"),"growth",1.13)<1||decimal(get("levels"),"growth",1.13)>2)throw new IllegalArgumentException("Curva de níveis inválida");
  double shiny=decimal(get("capture"),"shinyChance",1d/4096);if(!Double.isFinite(shiny)||shiny<0||shiny>1)throw new IllegalArgumentException("Shiny deve ser 0..1");
  for(var file:files.entrySet())checkNumbers(file.getValue(),file.getKey());
  if(decimal(get("sizes"),"minimumMultiplier",.6)<=0||decimal(get("sizes"),"maximumMultiplier",2.5)<decimal(get("sizes"),"minimumMultiplier",.6))throw new IllegalArgumentException("Intervalo de tamanhos inválido");
  if(num(get("storage"),"initialCapacity",30)<1||num(get("storage"),"maximumCapacity",300)<num(get("storage"),"initialCapacity",30)||num(get("storage"),"expansionSlots",10)<1)throw new IllegalArgumentException("Capacidade inválida");
  RoundingMode.valueOf(str(get("economy"),"roundingMode","HALF_UP"));
  var quests=new HashSet<String>();for(String file:List.of("missions/daily","missions/weekly","missions/story"))for(var q:rows(get(file)))if(!quests.add(str(q,"id","")))throw new IllegalArgumentException("ID de missão duplicado entre arquivos");
  for(var r:rows(get("vip-time")))if(num(r,"minutes",0)<1||str(r,"permission","").isBlank())throw new IllegalArgumentException("VIP inválido");
 }
 static void checkNumbers(JsonElement j,String path){if(j.isJsonObject())for(var e:j.getAsJsonObject().entrySet())checkNumbers(e.getValue(),path+"."+e.getKey());else if(j.isJsonArray())for(var e:j.getAsJsonArray())checkNumbers(e,path+"[]");else if(j.isJsonPrimitive()&&j.getAsJsonPrimitive().isNumber()){double n=j.getAsDouble();if(!Double.isFinite(n)||n<0&&!path.matches(".*\\.(x|y|z)$")||n>1e12)throw new IllegalArgumentException("Número inválido: "+path);}}
 static Map<String,String> defaults(){var d=new LinkedHashMap<String,String>();
  d.put("messages","{\"prefix\":\"✦ ZORYKAI\",\"primaryColor\":\"aqua\",\"textColor\":\"gray\"}");
  d.put("commands","{\"allowed\":[\"pesca\",\"login\",\"l\",\"logar\",\"register\",\"reg\",\"registrar\",\"2fa\",\"auth\",\"pin\"],\"adminBypassPermission\":\"zorykai_fishing.bypass\"}");
  d.put("timers","{\"normalMinutes\":60,\"cycleHours\":24,\"maximumAccumulatedMinutes\":360,\"actionbar\":true,\"warnings\":[900,300,60,30,10]}");
  d.put("vip-time","{\"values\":[{\"id\":\"magma\",\"permission\":\"zorykai_fishing.vip.magma\",\"minutes\":72},{\"id\":\"aqua\",\"permission\":\"zorykai_fishing.vip.aqua\",\"minutes\":86},{\"id\":\"plasma\",\"permission\":\"zorykai_fishing.vip.plasma\",\"minutes\":103},{\"id\":\"galactic\",\"permission\":\"zorykai_fishing.vip.galactic\",\"minutes\":124}]}");
  d.put("time-shop","{\"values\":[{\"id\":\"dez\",\"name\":\"10 minutos\",\"minutes\":10,\"validHours\":24,\"price\":2000},{\"id\":\"trinta\",\"name\":\"30 minutos\",\"minutes\":30,\"validHours\":24,\"price\":5000},{\"id\":\"sessenta\",\"name\":\"60 minutos\",\"minutes\":60,\"validHours\":24,\"price\":9000}]}");
  d.put("inventory","{\"blockExternalTransfers\":true,\"quarantineUnexpectedItems\":true}");
  d.put("permissions","{\"admin\":\"zorykai_fishing.admin\",\"use\":\"zorykai_fishing.use\"}");
  d.put("levels","{\"maximumLevel\":100,\"baseXp\":100,\"growth\":1.13,\"captureXp\":25,\"rodCaptureXp\":20}");
  d.put("rods","{\"values\":[{\"id\":\"inicial\",\"name\":\"Vara Inicial\",\"rarity\":\"common\",\"minimumLevel\":1,\"price\":0,\"waitSeconds\":15,\"luck\":1,\"resistance\":1,\"precision\":1},{\"id\":\"boa\",\"name\":\"Vara de Carvalho\",\"rarity\":\"uncommon\",\"minimumLevel\":10,\"price\":15000,\"waitSeconds\":12,\"luck\":1.15,\"resistance\":1.2,\"precision\":1.1},{\"id\":\"super\",\"name\":\"Vara das Marés\",\"rarity\":\"rare\",\"minimumLevel\":25,\"price\":50000,\"waitSeconds\":9,\"luck\":1.4,\"resistance\":1.5,\"precision\":1.3}]}");
  d.put("upgrades","{\"values\":[{\"id\":\"speed\",\"name\":\"Carretilha\",\"price\":3000,\"maximumLevel\":5},{\"id\":\"luck\",\"name\":\"Anzol\",\"price\":4000,\"maximumLevel\":5},{\"id\":\"resistance\",\"name\":\"Linha reforçada\",\"price\":2500,\"maximumLevel\":5},{\"id\":\"precision\",\"name\":\"Empunhadura\",\"price\":2500,\"maximumLevel\":5}]}");
  d.put("baits","{\"values\":[{\"id\":\"comum\",\"name\":\"Isca Comum\",\"price\":0,\"amount\":1,\"infinite\":true},{\"id\":\"rapida\",\"name\":\"Isca Rápida\",\"price\":100,\"amount\":10,\"speedMultiplier\":0.7},{\"id\":\"rara\",\"name\":\"Isca Rara\",\"price\":500,\"amount\":10,\"rareMultiplier\":2},{\"id\":\"gigante\",\"name\":\"Isca Gigante\",\"price\":400,\"amount\":10,\"sizeMultiplier\":1.3},{\"id\":\"especializada\",\"name\":\"Isca Especializada\",\"price\":400,\"amount\":10,\"species\":[\"feebas\"],\"speciesMultiplier\":3},{\"id\":\"shiny\",\"name\":\"Isca Brilhante\",\"price\":1500,\"amount\":5,\"shinyMultiplier\":3},{\"id\":\"evento\",\"name\":\"Isca de Evento\",\"price\":1000,\"amount\":5,\"xpMultiplier\":1.5}]}");
  d.put("pokeballs","{\"values\":[{\"id\":\"basica\",\"name\":\"Bola de Pesca\",\"item\":\"cobblemon:poke_ball\",\"price\":0,\"amount\":1,\"infinite\":true,\"captureChance\":0.55},{\"id\":\"boa\",\"name\":\"Bola das Marés\",\"item\":\"cobblemon:great_ball\",\"price\":500,\"amount\":10,\"captureChance\":0.75},{\"id\":\"super\",\"name\":\"Bola Abissal\",\"item\":\"cobblemon:ultra_ball\",\"price\":1500,\"amount\":10,\"captureChance\":0.9}]}");
  d.put("capture","{\"shinyChance\":0.000244140625,\"escapeChance\":0.08,\"clickProgress\":20,\"clickTension\":22,\"tensionDecayPerSecond\":12,\"minimumReelTicks\":5,\"lineExpirySeconds\":120,\"treasureChance\":0.05}");
  d.put("sizes","{\"minimumMultiplier\":0.6,\"maximumMultiplier\":2.5,\"large\":1.2,\"giant\":1.6,\"record\":2.1,\"maximumVisualScale\":3}");
  d.put("prices","{\"base\":250,\"rare\":1500,\"legendary\":10000,\"maximum\":100000,\"shinyMultiplier\":2,\"sizeExponent\":2,\"species\":{\"magikarp\":250,\"goldeen\":350,\"poliwag\":400,\"wooper\":500,\"feebas\":1500}}");
  d.put("storage","{\"initialCapacity\":30,\"maximumCapacity\":300,\"expansionSlots\":10,\"expansionPrice\":10000,\"maximumBatchSale\":100}");
  d.put("economy","{\"currency\":\"impactor:dollars\",\"roundingMode\":\"HALF_UP\"}");
  d.put("npc","{\"consoleCommand\":\"pesca abrir <menu> <jogador>\",\"menus\":[\"venda\",\"varas\",\"iscas\",\"pokebolas\",\"melhorias\",\"missoes\",\"torneios\",\"recompensas\"]}");
  d.put("records","{\"announce\":true,\"historyLimit\":100}");d.put("rankings","{\"displayLimit\":20}");
  d.put("prestige","{\"enabled\":false,\"maximumPrestige\":100,\"xpBonusPerPrestige\":0.02}");
  d.put("integrations","{\"trainers\":true,\"menus\":true,\"guard\":true,\"multiworld\":true}");
  d.put("worlds","{\"allowVanillaDimensions\":false,\"recoverySpawn\":null}");
  d.put("missions/daily","{\"values\":[{\"id\":\"magikarp_diario\",\"name\":\"Cardume do dia\",\"period\":\"DAILY\",\"objective\":\"CAPTURE\",\"species\":\"magikarp\",\"target\":20,\"reward\":{\"xp\":250,\"minutes\":10,\"bait\":\"rapida\",\"amount\":10}}]}");
  d.put("missions/weekly","{\"values\":[{\"id\":\"pescador_semanal\",\"name\":\"Uma semana no lago\",\"period\":\"WEEKLY\",\"objective\":\"CAPTURE\",\"target\":50,\"reward\":{\"money\":5000,\"xp\":500}}]}");
  d.put("missions/story","{\"values\":[{\"id\":\"primeira_venda\",\"name\":\"Pescador comerciante\",\"period\":\"STORY\",\"objective\":\"SELL\",\"target\":10,\"reward\":{\"money\":1000,\"xp\":200}},{\"id\":\"explorador_oceano\",\"name\":\"Explorador do Oceano\",\"period\":\"STORY\",\"objective\":\"LEVEL\",\"target\":25,\"reward\":{\"unlock\":\"oceano_profundo\",\"xp\":500}}]}");
  d.put("achievements","{\"values\":[{\"id\":\"primeiros_passos\",\"name\":\"Primeiros passos\",\"stat\":\"captures\",\"target\":10,\"reward\":{\"xp\":100}},{\"id\":\"colecionador\",\"name\":\"Colecionador\",\"stat\":\"species\",\"target\":5,\"reward\":{\"minutes\":10}},{\"id\":\"brilhante\",\"name\":\"Um brilho no lago\",\"stat\":\"shiny\",\"target\":1,\"reward\":{\"money\":5000}}]}");
  d.put("events/example","{\"values\":[{\"id\":\"mares_favoraveis\",\"name\":\"Marés favoráveis\",\"durationSeconds\":900,\"automaticIntervalSeconds\":0,\"sizeMultiplier\":1.2,\"xpMultiplier\":1.5,\"shinyMultiplier\":2,\"rareMultiplier\":1.3,\"lakes\":[]}]}");
  d.put("tournaments/example","{\"values\":[{\"id\":\"maior_peixe\",\"name\":\"O maior do lago\",\"metric\":\"LARGEST\",\"durationSeconds\":600,\"automaticIntervalSeconds\":21600,\"minimumPlayers\":1,\"rewards\":[5000,2500,1000]},{\"id\":\"cardume\",\"name\":\"Festival do cardume\",\"metric\":\"QUANTITY\",\"durationSeconds\":600,\"automaticIntervalSeconds\":0,\"minimumPlayers\":1,\"rewards\":[5000,2500,1000]}]}");
  d.put("treasures/default","{\"values\":[{\"id\":\"iscas\",\"weight\":50,\"type\":\"bait\",\"reference\":\"rapida\",\"amount\":5},{\"id\":\"bolas\",\"weight\":35,\"type\":\"ball\",\"reference\":\"boa\",\"amount\":3},{\"id\":\"fragmento\",\"weight\":10,\"type\":\"fragment\",\"amount\":1},{\"id\":\"diamante\",\"weight\":5,\"type\":\"item\",\"reference\":\"minecraft:diamond\",\"amount\":1}]}");
  for(String menu:List.of("main","profile","storage","shop","areas","rods","upgrades","baits","pokeballs","sale","dex","missions","records","rankings","tournaments","achievements","prestige","time","time-shop","museum","treasures"))d.put("menus/"+menu,"{\"title\":\"✦ ZORYKAI · "+menu+"\",\"primaryColor\":\"aqua\",\"textColor\":\"gray\",\"rows\":6}");
  d.put("pokemon/common","{\"description\":\"Modelo de referência; o sorteio usa a tabela pokemon de cada lago.\",\"values\":[]}");
  d.put("pokemon/uncommon","{\"description\":\"Modelo de referência; o sorteio usa a tabela pokemon de cada lago.\",\"values\":[]}");
  d.put("pokemon/rare","{\"description\":\"Modelo de referência; o sorteio usa a tabela pokemon de cada lago.\",\"values\":[]}");
  d.put("pokemon/legendary","{\"description\":\"Modelo de referência; o sorteio usa a tabela pokemon de cada lago.\",\"values\":[]}");
  return d;
 }
}
