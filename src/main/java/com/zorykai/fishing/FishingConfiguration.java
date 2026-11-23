package com.zorykai.fishing;
import com.zorykai.fishing.core.*;
import com.google.gson.*;
import java.nio.file.*;
import java.util.*;
import java.io.*;

public final class FishingConfiguration {
 public static final Gson JSON=new GsonBuilder().setPrettyPrinting().create();
 public final Path root;public MapDefinition map;public Map<String,Lake> lakes=Map.of();
 public Settings settings=new Settings();
 public static class Settings { public int schemaVersion=1;public boolean debugCaptureEnabled=false;public int storageCapacity=30;public int encounterSeconds=120;public String menuId="fishing_main"; }
 public FishingConfiguration(Path root)throws Exception{this.root=root;Files.createDirectories(root.resolve("areas"));if(!Files.exists(root.resolve("config.json")))write(root.resolve("config.json"),settings);reload();}
 public void reload()throws Exception{
  var next=JSON.fromJson(Files.readString(root.resolve("config.json")),Settings.class);
  if(next==null||next.schemaVersion!=1||next.storageCapacity<1||next.storageCapacity>10000||next.encounterSeconds<15||next.encounterSeconds>600)throw new IllegalArgumentException("config.json inválido");
  MapDefinition parent=null;
  if(Files.exists(root.resolve("mapa.json"))){parent=JSON.fromJson(Files.readString(root.resolve("mapa.json")),MapDefinition.class);if(parent==null)throw new IllegalArgumentException("mapa.json vazio");parent.validate();}
  var all=new LinkedHashMap<String,Lake>();try(var paths=Files.list(root.resolve("areas"))){for(var p:paths.filter(x->x.getFileName().toString().endsWith(".json")).sorted().toList()){
   if(parent==null)throw new IllegalArgumentException("Cadastre o perímetro geral antes dos lagos");var lake=JSON.fromJson(Files.readString(p),Lake.class);if(lake==null||!p.getFileName().toString().equals(lake.id()+".json"))throw new IllegalArgumentException("Nome do arquivo deve coincidir com ID do lago");lake.validate(parent,all.values());if(all.putIfAbsent(lake.id(),lake)!=null)throw new IllegalArgumentException("ID repetido");
  }}
  settings=next;map=parent;lakes=Map.copyOf(all);
 }
 public static void write(Path path,Object value)throws IOException{
  Files.createDirectories(path.toAbsolutePath().getParent());var bytes=JSON.toJson(value).getBytes(java.nio.charset.StandardCharsets.UTF_8);var temp=Files.createTempFile(path.getParent(),".fishing-",".tmp");
  try{try(var c=java.nio.channels.FileChannel.open(temp,StandardOpenOption.WRITE)){var buffer=java.nio.ByteBuffer.wrap(bytes);while(buffer.hasRemaining())c.write(buffer);c.force(true);}Files.move(temp,path,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);}finally{Files.deleteIfExists(temp);}
 }
 public Lake at(String dimension,double x,double y,double z){return lakes.values().stream().filter(l->l.dimension().equals(dimension)&&l.bounds().contains(x,y,z)).findFirst().orElse(null);}
}
