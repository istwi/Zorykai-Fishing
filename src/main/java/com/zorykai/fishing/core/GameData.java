package com.zorykai.fishing.core;
import com.google.gson.*;
import java.sql.*;
import java.util.*;
public final class GameData {
 public static final Gson JSON=new GsonBuilder().setPrettyPrinting().create();public final FishingStore store;public int initialCapacity=30;
 public GameData(FishingStore store){this.store=store;}
 public PlayerData profile(Connection c,UUID player)throws SQLException{
  try(var p=c.prepareStatement("SELECT payload FROM profiles WHERE player=?")){p.setString(1,player.toString());try(var r=p.executeQuery()){if(r.next()){var d=JSON.fromJson(r.getString(1),PlayerData.class);if(d==null)throw new SQLException("Perfil inválido");return d;}}}var fresh=new PlayerData();fresh.capacity=initialCapacity;return fresh;
 }
 public PlayerData profile(UUID id)throws Exception{return store.transaction(c->profile(c,id));}
 public void save(Connection c,UUID id,PlayerData data)throws SQLException{try(var p=c.prepareStatement("INSERT INTO profiles VALUES(?,?) ON CONFLICT(player) DO UPDATE SET payload=excluded.payload")){p.setString(1,id.toString());p.setString(2,JSON.toJson(data));p.executeUpdate();}}
 public <T>T update(UUID id,java.util.function.Function<PlayerData,T> mutate)throws Exception{return store.transaction(c->{var data=profile(c,id);var result=mutate.apply(data);save(c,id,data);return result;});}
 public void audit(Connection c,UUID id,String event,Object details)throws SQLException{try(var p=c.prepareStatement("INSERT INTO audit(at,player,event,payload) VALUES(?,?,?,?)")){p.setLong(1,System.currentTimeMillis());p.setString(2,id==null?null:id.toString());p.setString(3,event);p.setString(4,JSON.toJson(details));p.executeUpdate();}}
 public List<Map.Entry<UUID,PlayerData>> profiles()throws Exception{return store.transaction(c->{var all=new ArrayList<Map.Entry<UUID,PlayerData>>();try(var s=c.createStatement();var r=s.executeQuery("SELECT player,payload FROM profiles")){while(r.next())all.add(Map.entry(UUID.fromString(r.getString(1)),JSON.fromJson(r.getString(2),PlayerData.class)));}return all;});}
 public JsonObject global(String id)throws Exception{return store.transaction(c->{try(var p=c.prepareStatement("SELECT payload FROM global_state WHERE id=?")){p.setString(1,id);try(var r=p.executeQuery()){return r.next()?JsonParser.parseString(r.getString(1)).getAsJsonObject():new JsonObject();}}});}
 public void global(String id,JsonObject data)throws Exception{store.transaction(c->{try(var p=c.prepareStatement("INSERT INTO global_state VALUES(?,?) ON CONFLICT(id) DO UPDATE SET payload=excluded.payload")){p.setString(1,id);p.setString(2,data.toString());p.executeUpdate();}return null;});}
}
