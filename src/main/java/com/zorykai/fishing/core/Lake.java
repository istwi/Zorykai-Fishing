package com.zorykai.fishing.core;
import java.util.*;
import java.util.random.RandomGenerator;
public record Lake(String id,String name,String map,String dimension,Bounds bounds,int minimumLevel,String permission,List<Species> pokemon,String unlockMode,List<String> quests,List<String> rods,List<String> baits,Double sizeMultiplier,Display display){
 public static final class Display {public boolean enabled=true,sound=true;public String title="",subtitle="&b{area}{status}",blockedSuffix="&c · pesca bloqueada";public int fadeInTicks=5,stayTicks=40,fadeOutTicks=10;public void validate(){if(title==null||subtitle==null||blockedSuffix==null||title.length()>256||subtitle.length()>256||blockedSuffix.length()>128||fadeInTicks<0||fadeInTicks>200||stayTicks<1||stayTicks>600||fadeOutTicks<0||fadeOutTicks>200)throw new IllegalArgumentException("display inválido no JSON do lago");}}
 public Lake(String id,String name,String map,String dimension,Bounds bounds,int minimumLevel,String permission,List<Species> pokemon,String unlockMode,List<String> quests,List<String> rods,List<String> baits,Double sizeMultiplier){this(id,name,map,dimension,bounds,minimumLevel,permission,pokemon,unlockMode,quests,rods,baits,sizeMultiplier,new Display());}
 public Lake(String id,String name,String map,String dimension,Bounds bounds,int minimumLevel,String permission,List<Species> pokemon,String unlockMode,List<String> quests,List<String> rods,List<String> baits){this(id,name,map,dimension,bounds,minimumLevel,permission,pokemon,unlockMode,quests,rods,baits,1d);}
 public Lake(String id,String name,String map,String dimension,Bounds bounds,int minimumLevel,String permission,List<Species> pokemon){this(id,name,map,dimension,bounds,minimumLevel,permission,pokemon,"ALL",List.of(),List.of(),List.of());}
 public Lake{display=display==null?new Display():display;display.validate();sizeMultiplier=sizeMultiplier==null?1d:sizeMultiplier;unlockMode=unlockMode==null?"ALL":unlockMode;quests=quests==null?List.of():List.copyOf(quests);rods=rods==null?List.of():List.copyOf(rods);baits=baits==null?List.of():List.copyOf(baits);}
 public record Species(String species,double chance){}
 public void validate(MapDefinition parent,Collection<Lake> others){
  parent.validate();if(!Double.isFinite(sizeMultiplier)||sizeMultiplier<=0||sizeMultiplier>5)throw new IllegalArgumentException("Multiplicador de tamanho inválido");
  if(!Set.of("FREE","LEVEL","QUEST","PERMISSION","ANY","ALL").contains(unlockMode)||unlockMode.equals("QUEST")&&quests.isEmpty()||unlockMode.equals("PERMISSION")&&permission==null)throw new IllegalArgumentException("Requisitos de desbloqueio inválidos");
  if(id==null||!id.matches("[a-z0-9_-]{1,48}")||name==null||name.isBlank()||!parent.id().equals(map)||!parent.dimension().equals(dimension)||bounds==null||!parent.bounds().encloses(bounds)||minimumLevel<1)throw new IllegalArgumentException("O lago deve estar inteiro dentro do mapa e na mesma dimensão");
  if(permission!=null&&!permission.matches("[a-z0-9_.:-]{1,100}"))throw new IllegalArgumentException("Permissão inválida");
  for(var other:others)if(!other.id.equals(id)&&bounds.overlaps(other.bounds))throw new IllegalArgumentException("Lago sobrepõe "+other.id);
  if(pokemon==null||pokemon.isEmpty()||pokemon.size()>256)throw new IllegalArgumentException("Tabela de Pokémon vazia ou grande demais");
  var ids=new HashSet<String>();double sum=0;
  for(var p:pokemon){if(p==null||p.species==null||!p.species.matches("(?:[a-z0-9_.-]+:)?[a-z0-9_]+")||!ids.add(p.species)||!Double.isFinite(p.chance)||p.chance<0||p.chance>100)throw new IllegalArgumentException("Espécie/chance inválida ou duplicada");sum+=p.chance;}
  if(Math.abs(sum-100)>1e-6)throw new IllegalArgumentException("Chances do lago devem somar 100");
 }
 public String draw(RandomGenerator random){double roll=random.nextDouble()*100;String last=null;for(var p:pokemon)if(p.chance>0){last=p.species;if(roll<p.chance)return p.species;roll-=p.chance;}return Objects.requireNonNull(last);}
 public static List<Species> starter(){return List.of(new Species("magikarp",45),new Species("goldeen",30),new Species("poliwag",15),new Species("wooper",8),new Species("feebas",2));}
}
