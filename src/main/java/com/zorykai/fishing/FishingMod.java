package com.zorykai.fishing;

import com.zorykai.fishing.core.*;
import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.Priority;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.*;
import net.fabricmc.fabric.api.event.player.*;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.*;
import net.minecraft.server.network.*;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.entity.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.text.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.hit.*;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.component.*;
import net.minecraft.component.type.*;
import net.minecraft.nbt.*;
import net.minecraft.util.WorldSavePath;
import java.nio.file.*;
import java.util.*;
import static net.minecraft.server.command.CommandManager.*;

/** Phase 0 runnable proof. No production inventory swaps, economic transactions or world creation. */
public final class FishingMod implements ModInitializer {
 public static FishingMod INSTANCE;public static final String TAG="zorykai_fishing_exclusive";
 public static final org.slf4j.Logger LOG=org.slf4j.LoggerFactory.getLogger("zorykai_fishing");
 public boolean menuIntegrationReady;
 public FishingConfiguration config;public FishingStore store;public MinecraftServer server;
 public com.zorykai.fishing.session.ItemDelivery delivery;public GameConfig rules;public GameData data;public GameEngine engine;public GameProgress progress;public FishingFinance finance;public com.zorykai.fishing.session.FishingSessions sessions;
 public boolean admin(ServerPlayerEntity p){return me.lucko.fabric.api.permissions.v0.Permissions.check(p,rules==null?"zorykai_fishing.admin":GameConfig.str(rules.get("permissions"),"admin","zorykai_fishing.admin"),4);}
 public void log(Throwable e){LOG.warn("Fishing: {}",e.toString());}
 public void fail(Exception e){LOG.error("Falha preservada no Fishing",e);}
 public void audit(UUID player,String event,Object details){try{store.transaction(c->{data.audit(c,player,event,details);return null;});}catch(Exception e){fail(e);}}
 public final Map<UUID,Encounter> encounters=new HashMap<>();private final Map<UUID,Draft> drafts=new HashMap<>();
 public record Encounter(UUID id,UUID owner,PokemonEntity entity,String lake,long expires){}
 static class Draft {String dimension,key;BlockPos one,two,entry;}
 public static net.minecraft.text.MutableText message(String text){var cfg=INSTANCE==null||INSTANCE.rules==null?new com.google.gson.JsonObject():INSTANCE.rules.get("messages");var primary=Optional.ofNullable(Formatting.byName(GameConfig.str(cfg,"primaryColor","aqua"))).orElse(Formatting.AQUA);var body=Optional.ofNullable(Formatting.byName(GameConfig.str(cfg,"textColor","gray"))).orElse(Formatting.GRAY);return Text.literal(GameConfig.str(cfg,"prefix","✦ ZORYKAI")+" ").formatted(primary).append(Text.literal(text==null?"Operação indisponível; consulte o log.":text).formatted(body));}
 public static void tell(ServerPlayerEntity p,String text){p.sendMessage(message(text),false);}

 public void onInitialize(){
  INSTANCE=this;
  try{config=new FishingConfiguration(FabricLoader.getInstance().getConfigDir().resolve("zorykai-fishing"));rules=new GameConfig(config.root);}catch(Exception e){throw new IllegalStateException("Configuração do Fishing inválida",e);}
  if(FabricLoader.getInstance().isModLoaded("zorykaimenus")&&GameConfig.bool(rules.get("integrations"),"menus",true)){try{MenuBridge.register(this);menuIntegrationReady=true;}catch(LinkageError e){LOG.warn("API do Zorykai Menus incompatível; usando interface interna",e);}}
  CommandRegistrationCallback.EVENT.register((d,r,e)->{
   var player=literal("pesca").requires(s->s.getEntity()==null||!(s.getEntity() instanceof ServerPlayerEntity p)||me.lucko.fabric.api.permissions.v0.Permissions.check(p,GameConfig.str(rules.get("permissions"),"use","zorykai_fishing.use"),true)).executes(c->playerMenu(c.getSource(),"main"));
   for(String menu:List.of("armazem","areas","perfil","varas","iscas","bolas","missoes","tempo","conquistas","torneios","ranking","dex","museu","prestigio","tesouros","recordes","pendencia","melhorias","premios","eventos"))player.then(literal(menu).executes(c->playerMenu(c.getSource(),menu)));
   player.then(literal("entrar").executes(c->guarded(c.getSource(),()->{sessions.enter(c.getSource().getPlayerOrThrow());return 1;})));
   player.then(literal("sair").executes(c->guarded(c.getSource(),()->{sessions.exit(c.getSource().getPlayerOrThrow());return 1;})));
   player.then(literal("dexbuscar").then(argument("especie",StringArgumentType.word()).executes(c->playerMenu(c.getSource(),"dex:"+StringArgumentType.getString(c,"especie")))));
   player.then(literal("buscar").then(argument("especie",StringArgumentType.word()).executes(c->playerMenu(c.getSource(),"armazem:"+StringArgumentType.getString(c,"especie")))));
   player.then(literal("abrir").requires(s->s.getEntity()==null&&s.hasPermissionLevel(4)).then(argument("menu",StringArgumentType.word()).then(argument("jogador",net.minecraft.command.argument.EntityArgumentType.player()).executes(c->{var p=net.minecraft.command.argument.EntityArgumentType.getPlayer(c,"jogador");FishingScreens.open(this,p,StringArgumentType.getString(c,"menu"));return 1;}))));
   d.register(player);
   var admin=literal("pescaadmin").requires(s->s.getEntity() instanceof ServerPlayerEntity p?admin(p):s.hasPermissionLevel(4));
   AdminCommands.register(this,admin);
   for(String action:List.of("status","validar","reload"))admin.then(literal(action).executes(c->guarded(c.getSource(),()->{
    if(!action.equals("status")){var old=config;var next=new FishingConfiguration(old.root);validateSpecies(next);var nextRules=new GameConfig(old.root);config=next;rules=nextRules;if(data!=null)data.initialCapacity=GameConfig.num(rules.get("storage"),"initialCapacity",30);}
    reply(c.getSource(),"Fishing beta · mapa="+(config.map==null?"não configurado":config.map.id()+" ("+config.map.dimension()+")")+" · lagos="+config.lakes.size()+" · recuperações="+(sessions==null?0:sessions.pending.size()));return 1;
   })));
   var map=literal("mapa");for(String a:List.of("pos1","pos2","entrada","salvar"))map.then(literal(a).executes(c->guarded(c.getSource(),()->mark(c.getSource().getPlayerOrThrow(),"mapa",a))));admin.then(map);
   var area=literal("area");area.then(literal("listar").executes(c->{reply(c.getSource(),String.join(", ",new TreeSet<>(config.lakes.keySet())));return 1;}));
   for(String action:List.of("criar","pos1","pos2","entrada","salvar","validar","remover"))area.then(literal(action).then(argument("id",StringArgumentType.word()).executes(c->guarded(c.getSource(),()->area(c.getSource().getPlayerOrThrow(),StringArgumentType.getString(c,"id"),action)))));
   for(String mode:List.of("bloquear","desbloquear"))area.then(literal(mode).then(argument("jogador",net.minecraft.command.argument.EntityArgumentType.player()).then(argument("id",StringArgumentType.word()).executes(c->guarded(c.getSource(),()->{var p=net.minecraft.command.argument.EntityArgumentType.getPlayer(c,"jogador");String id=StringArgumentType.getString(c,"id");if(!config.lakes.containsKey(id))throw new IllegalArgumentException("Lago inexistente");data.update(p.getUuid(),v->{if(mode.equals("bloquear")){v.blockedAreas.add(id);v.unlocks.remove(id);}else{v.blockedAreas.remove(id);v.unlocks.add(id);}return null;});audit(p.getUuid(),"ADMIN_AREA_"+mode,Map.of("id",id,"by",c.getSource().getName()));return 1;})))));
   admin.then(area);
   admin.then(literal("debug").then(literal("encontro").executes(c->guarded(c.getSource(),()->{debugEncounter(c.getSource().getPlayerOrThrow());return 1;}))).then(literal("bola").executes(c->{var p=c.getSource().getPlayerOrThrow();if(!config.settings.debugCaptureEnabled){tell(p,"Ative debugCaptureEnabled para a prova de conceito.");return 0;}if(!p.getInventory().insertStack(ball()))return 0;return 1;})));
   admin.then(literal("recuperar").then(argument("jogador",net.minecraft.command.argument.EntityArgumentType.player()).executes(c->guarded(c.getSource(),()->{sessions.exit(net.minecraft.command.argument.EntityArgumentType.getPlayer(c,"jogador"));return 1;}))));
   for(String kind:List.of("evento","torneio")){var node=literal(kind);for(String action:List.of("iniciar","encerrar"))node.then(literal(action).then(argument("id",StringArgumentType.word()).executes(c->guarded(c.getSource(),()->{var id=StringArgumentType.getString(c,"id");if(kind.equals("torneio")){if(action.equals("iniciar"))progress.startTournament(id);else progress.endTournament(id);}else if(action.equals("iniciar"))progress.eventStart(id);else{var events=data.global("events");events.remove(id);data.global("events",events);}return 1;}))));admin.then(node);}
   d.register(admin);
  });
  ServerLifecycleEvents.SERVER_STARTED.register(s->{server=s;try{store=new FishingStore(s.getSavePath(WorldSavePath.ROOT).resolve("data/zorykai-fishing/fishing.sqlite"));store.expireAll();validateSpecies(config);data=new GameData(store);data.initialCapacity=GameConfig.num(rules.get("storage"),"initialCapacity",30);engine=new GameEngine(this);progress=new GameProgress(this);sessions=new com.zorykai.fishing.session.FishingSessions(this);finance=new FishingFinance(this);delivery=new com.zorykai.fishing.session.ItemDelivery(this);}catch(Exception e){throw new IllegalStateException("Falha ao abrir armazenamento do Fishing",e);}});
  ServerPlayConnectionEvents.JOIN.register((h,sender,s)->{if(sessions!=null){sessions.joined(h.player);try{finance.recover(h.player.getUuid());delivery.recover(h.player);}catch(Exception e){fail(e);}}});
  ServerLifecycleEvents.SERVER_STOPPING.register(s->{if(sessions!=null)sessions.stop();for(var e:List.copyOf(encounters.values()))expire(e);});
  ServerLifecycleEvents.SERVER_STOPPED.register(s->{try{if(store!=null)store.close();}catch(Exception e){LOG.error("Falha ao fechar Fishing",e);}if(finance!=null)finance.close();INSTANCE=null;});
  ServerPlayConnectionEvents.DISCONNECT.register((h,s)->{drafts.remove(h.player.getUuid());if(sessions!=null&&sessions.protectedPlayer(h.player))try{sessions.exit(h.player);}catch(Exception e){fail(e);}for(var e:List.copyOf(encounters.values()))if(e.owner.equals(h.player.getUuid()))expire(e);});
  net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents.ALLOW_DEATH.register((entity,source,amount)->{if(entity instanceof ServerPlayerEntity p&&sessions!=null&&sessions.protectedPlayer(p)){try{sessions.exit(p);}catch(Exception e){fail(e);}return false;}return true;});
  ServerEntityEvents.ENTITY_LOAD.register((entity,world)->{if(entity.getCommandTags().contains(TAG)&&!encounters.containsKey(entity.getUuid())||entity.getCommandTags().contains("zf_projectile")&&(engine==null||!engine.projectiles.contains(entity.getUuid())))entity.discard();});
  ServerTickEvents.END_SERVER_TICK.register(s->{if(sessions!=null){sessions.tick();engine.tick();progress.tick();}for(var e:List.copyOf(encounters.values()))if(System.currentTimeMillis()>=e.expires||!e.entity.isAlive())expire(e);});
  AttackEntityCallback.EVENT.register((p,w,h,e,hit)->e.getCommandTags().contains(TAG)||p instanceof ServerPlayerEntity sp&&sessions!=null&&sessions.protectedPlayer(sp)?ActionResult.FAIL:ActionResult.PASS);
  UseBlockCallback.EVENT.register((p,w,h,hit)->p instanceof ServerPlayerEntity sp&&sessions!=null&&sessions.protectedPlayer(sp)?ActionResult.FAIL:ActionResult.PASS);
  UseEntityCallback.EVENT.register((p,w,h,e,hit)->{if(p instanceof ServerPlayerEntity sp&&sessions!=null&&sessions.protectedPlayer(sp)&&(!sessions.active(sp)||!TrainerBridge.allowed(this,e)))return ActionResult.FAIL;if(!e.getCommandTags().contains(TAG))return ActionResult.PASS;if(p instanceof ServerPlayerEntity sp&&isBall(p.getStackInHand(h)))capture(sp,e.getUuid());return ActionResult.SUCCESS;});
  UseItemCallback.EVENT.register((p,w,h)->{var stack=p.getStackInHand(h);if(p instanceof ServerPlayerEntity sp&&sessions!=null&&sessions.protectedPlayer(sp)){if(sessions.active(sp))try{String type=engine.gearType(sp,stack);if(type.equals("rod"))engine.rod(sp);else if(type.equals("ball"))engine.throwBall(sp,h);}catch(Exception e){tell(sp,e.getMessage());log(e);}return TypedActionResult.success(p.getStackInHand(h));}if(!isBall(stack))return TypedActionResult.pass(stack);if(p instanceof ServerPlayerEntity sp){var e=encounters.values().stream().filter(x->x.owner.equals(sp.getUuid())&&x.entity.getWorld()==sp.getWorld()&&x.entity.squaredDistanceTo(sp)<=64).findFirst();if(e.isPresent())capture(sp,e.get().entity.getUuid());else tell(sp,"Nenhum encontro seu por perto.");}return TypedActionResult.success(stack);});
  CobblemonEvents.BATTLE_STARTED_PRE.subscribe(Priority.HIGHEST,event->{
   if(sessions!=null)for(var actor:event.getBattle().getActors())for(var uuid:actor.getPlayerUUIDs())if(sessions.pending.containsKey(uuid)){event.cancel();event.setReason(Text.literal("Saia da pesca antes de iniciar uma batalha."));}
   for(var actor:event.getBattle().getActors())for(var mon:actor.getPokemonList())if(mon.getEffectedPokemon().getPersistentData().getBoolean(TAG)){event.cancel();event.setReason(Text.literal("Este Pokémon pertence a um encontro exclusivo de pesca."));}
   return kotlin.Unit.INSTANCE;
  });
  CobblemonEvents.POKEMON_SEEN.subscribe(Priority.HIGHEST,event->{if(event.getPokemon().getPersistentData().getBoolean(TAG))event.cancel();return kotlin.Unit.INSTANCE;});
  CobblemonEvents.POKEDEX_DATA_CHANGED_PRE.subscribe(Priority.HIGHEST,event->{var data=event.getDataSource();if(data!=null&&data.getPokemon().getPersistentData().getBoolean(TAG))event.cancel();return kotlin.Unit.INSTANCE;});
  CobblemonEvents.THROWN_POKEBALL_HIT.subscribe(Priority.HIGHEST,event->{if(event.getPokemon().getCommandTags().contains(TAG))event.cancel();return kotlin.Unit.INSTANCE;});
 }
 int guarded(ServerCommandSource source,Checked task){try{return task.run();}catch(Exception e){reply(source,e.getMessage());LOG.warn("Ação de Fishing recusada: {}",e.toString());return 0;}}
 interface Checked{int run()throws Exception;}
 int playerMenu(ServerCommandSource s,String menu)throws com.mojang.brigadier.exceptions.CommandSyntaxException{FishingScreens.open(this,s.getPlayerOrThrow(),menu);return 1;}
 static void reply(ServerCommandSource source,String text){source.sendFeedback(()->message(text),false);}
 String dim(ServerPlayerEntity p){return p.getWorld().getRegistryKey().getValue().toString();}
 int mark(ServerPlayerEntity p,String id,String action)throws Exception{
  var draft=drafts.computeIfAbsent(p.getUuid(),x->new Draft());if(draft.key!=null)throw new IllegalArgumentException("Termine a seleção do lago antes de alterar o mapa");if(draft.dimension!=null&&!draft.dimension.equals(dim(p)))throw new IllegalArgumentException("Selecione os pontos na mesma dimensão");draft.dimension=dim(p);
  switch(action){case "pos1"->draft.one=p.getBlockPos();case "pos2"->draft.two=p.getBlockPos();case "entrada"->draft.entry=p.getBlockPos();case "salvar"->{
   if(draft.one==null||draft.two==null||draft.entry==null)throw new IllegalArgumentException("Marque pos1, pos2 e entrada primeiro");
   var b=Bounds.between(draft.one.getX(),draft.one.getY(),draft.one.getZ(),draft.two.getX(),draft.two.getY(),draft.two.getZ());
   var map=new MapDefinition(id,draft.dimension,b,draft.entry.getX()+.5,draft.entry.getY(),draft.entry.getZ()+.5);map.validate();
   for(var lake:config.lakes.values())lake.validate(map,config.lakes.values());
   FishingConfiguration.write(config.root.resolve("mapa.json"),map);config.reload();drafts.remove(p.getUuid());
  }}tell(p,"Mapa: "+action+" confirmado.");return 1;
 }
 int area(ServerPlayerEntity p,String id,String action)throws Exception{
  if(!id.matches("[a-z0-9_-]{1,48}"))throw new IllegalArgumentException("ID inválido");if(config.map==null)throw new IllegalArgumentException("Primeiro demarque e salve o mapa inteiro com /pescaadmin mapa");
  if(action.equals("listar"))return 1;
  if(action.equals("validar")){var lake=config.lakes.get(id);if(lake==null)throw new IllegalArgumentException("Lago inexistente");lake.validate(config.map,config.lakes.values());tell(p,"Lago válido.");return 1;}
  if(action.equals("remover")){for(var encounter:List.copyOf(encounters.values()))if(encounter.lake().equals(id))expire(encounter);var file=config.root.resolve("areas/"+id+".json");if(!Files.exists(file))throw new IllegalArgumentException("Lago inexistente");Files.createDirectories(config.root.resolve("backups/areas"));Files.move(file,config.root.resolve("backups/areas/"+id+"-"+System.currentTimeMillis()+".json"));config.reload();audit(p.getUuid(),"ADMIN_AREA_REMOVE",Map.of("id",id));return 1;}
  var d=drafts.computeIfAbsent(p.getUuid(),x->new Draft());if(action.equals("criar")){var next=new Draft();next.key=id;drafts.put(p.getUuid(),next);tell(p,"Lago "+id+": marque pos1 e pos2; depois salvar.");return 1;}
  if(!id.equals(d.key))throw new IllegalArgumentException("Use /pescaadmin area criar "+id+" antes de selecionar este lago");
  if(!config.map.dimension().equals(dim(p)))throw new IllegalArgumentException("Use a dimensão do mapa");if(!config.map.bounds().contains(p.getX(),p.getY(),p.getZ()))throw new IllegalArgumentException("Marque os pontos dentro do perímetro geral");
  if(d.dimension!=null&&!d.dimension.equals(dim(p)))throw new IllegalArgumentException("Seleção em outra dimensão");d.dimension=dim(p);
  if(action.equals("pos1"))d.one=p.getBlockPos();else if(action.equals("pos2"))d.two=p.getBlockPos();else if(action.equals("entrada"))d.entry=p.getBlockPos();else if(action.equals("salvar")){
   if(d.one==null||d.two==null)throw new IllegalArgumentException("Marque os dois cantos do lago");if(config.lakes.containsKey(id))throw new IllegalArgumentException("ID existente: edite o JSON para preservar sua tabela");
   var b=Bounds.between(d.one.getX(),d.one.getY(),d.one.getZ(),d.two.getX(),d.two.getY(),d.two.getZ());var lake=new Lake(id,id,config.map.id(),dim(p),b,1,null,Lake.starter());lake.validate(config.map,config.lakes.values());validateSpecies(lake);
   FishingConfiguration.write(config.root.resolve("areas/"+id+".json"),lake);config.reload();drafts.remove(p.getUuid());
  }tell(p,"Lago "+id+": "+action+" confirmado.");return 1;
 }
 void validateSpecies(FishingConfiguration cfg){for(var lake:cfg.lakes.values())validateSpecies(lake);}
 void validateSpecies(Lake lake){for(var entry:lake.pokemon()){var speciesId=Identifier.of(entry.species().contains(":")?entry.species():"cobblemon:"+entry.species());var p=com.cobblemon.mod.common.api.pokemon.PokemonSpecies.getByIdentifier(speciesId);if(p==null||!p.getImplemented())throw new IllegalArgumentException("Espécie não implementada: "+entry.species());}}
 public Encounter spawnProof(ServerPlayerEntity p,ServerWorld world,BlockPos water,Lake lake)throws Exception{
  if(!world.getFluidState(water).isIn(FluidTags.WATER))throw new IllegalArgumentException("A boia deve estar na água");if(!lake.bounds().contains(water.getX()+.5,water.getY()+.5,water.getZ()+.5)||!lake.dimension().equals(world.getRegistryKey().getValue().toString()))throw new IllegalArgumentException("Água fora do lago");
  if(encounters.values().stream().anyMatch(e->e.owner.equals(p.getUuid())))throw new IllegalArgumentException("Já existe um encontro seu ativo");
  return spawnConfigured(p,world,water,lake,lake.draw(new Random()),1,false);
 }
 public Encounter spawnConfigured(ServerPlayerEntity p,ServerWorld world,BlockPos water,Lake lake,String species,double relative,boolean shiny)throws Exception{
  if(!world.getFluidState(water).isIn(FluidTags.WATER)||!lake.bounds().contains(water.getX()+.5,water.getY()+.5,water.getZ()+.5))throw new IllegalArgumentException("Água fora do lago");
  var entity=PokemonProperties.Companion.parse(species+" uncatchable=true level=5"," ","=").createEntity(world);var pokemon=entity.getPokemon();pokemon.getPersistentData().putBoolean(TAG,true);pokemon.getPersistentData().putDouble("zf_relative",relative);pokemon.setShiny(shiny);pokemon.setScaleModifier((float)Math.min(relative,GameConfig.decimal(rules.get("sizes"),"maximumVisualScale",3)));if(!pokemon.isUncatchable())throw new IllegalStateException("Cobblemon não confirmou bloqueio de captura nativa");
  entity.setAiDisabled(true);entity.setInvulnerable(true);entity.addCommandTag(TAG);entity.setPosition(water.getX()+.5,water.getY()+.7,water.getZ()+.5);
  var id=UUID.randomUUID();long expires=System.currentTimeMillis()+config.settings.encounterSeconds*1000L;store.encounter(id,p.getUuid(),entity.getUuid(),pokemon.getSpecies().getResourceIdentifier().toString(),lake.id(),pokemon.getSpecies().getStandardForm().getHeight()/10d*relative,pokemon.getShiny(),expires);
  var encounter=new Encounter(id,p.getUuid(),entity,lake.id(),expires);encounters.put(entity.getUuid(),encounter);
  if(!world.spawnEntity(entity)){encounters.remove(entity.getUuid());store.expire(id);throw new IllegalStateException("Spawn recusado; nenhum prêmio concedido");}try{data.update(p.getUuid(),d->{var dex=d.dex.computeIfAbsent(pokemon.getSpecies().getResourceIdentifier().toString(),k->new PlayerData.Dex());dex.seen++;return null;});}catch(Exception e){fail(e);}return encounter;
 }
 void debugEncounter(ServerPlayerEntity p)throws Exception{
  if(!config.settings.debugCaptureEnabled)throw new IllegalArgumentException("Prova de captura desativada: ative debugCaptureEnabled em config.json e recarregue");
  var hit=p.raycast(8,0,true);if(!(hit instanceof BlockHitResult block)||hit.getType()!=HitResult.Type.BLOCK)throw new IllegalArgumentException("Mire na água de um lago cadastrado");
  var pos=block.getBlockPos();var lake=config.at(dim(p),pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5);if(lake==null)throw new IllegalArgumentException("Nenhum lago cadastrado nesta água");if(lake.minimumLevel()>1||lake.permission()!=null)throw new IllegalArgumentException("Requisitos avançados serão habilitados nas próximas fases; use lago nível 1 sem permissão para a prova");spawnProof(p,p.getServerWorld(),pos,lake);tell(p,"Encontro de teste criado. Use a bola de prova para registrar no armazém.");
 }
 public boolean capture(ServerPlayerEntity p,UUID entityId){var e=encounters.get(entityId);if(e==null||!e.owner.equals(p.getUuid())||e.entity.getWorld()!=p.getWorld()||e.entity.squaredDistanceTo(p)>64){tell(p,"Este encontro não pertence a você ou está distante.");return false;}
  try{if(!engine.complete(p,e)){tell(p,"Captura não registrada: armazém cheio, encontro expirado ou já concluído.");return false;}encounters.remove(entityId);e.entity.discard();tell(p,"Captura registrada exclusivamente no armazém de pesca!");return true;}catch(Exception failure){LOG.error("Captura preservada após falha de armazenamento",failure);tell(p,"Falha no armazenamento. O encontro foi preservado; avise a administração.");return false;}}
 void expire(Encounter e){try{store.expire(e.id);encounters.remove(e.entity.getUuid());e.entity.discard();}catch(Exception x){LOG.error("Falha ao encerrar encontro; registro preservado",x);}}
 public static ItemStack ball(){var item=new ItemStack(Items.SNOWBALL);var n=new NbtCompound();n.putBoolean("zorykai_fishing_proof_ball",true);item.set(DataComponentTypes.CUSTOM_DATA,NbtComponent.of(n));item.set(DataComponentTypes.CUSTOM_NAME,Text.literal("Bola de pesca — prova de conceito").formatted(Formatting.AQUA));return item;}
 static boolean isBall(ItemStack s){return s.isOf(Items.SNOWBALL)&&s.getOrDefault(DataComponentTypes.CUSTOM_DATA,NbtComponent.DEFAULT).copyNbt().getBoolean("zorykai_fishing_proof_ball");}
}
