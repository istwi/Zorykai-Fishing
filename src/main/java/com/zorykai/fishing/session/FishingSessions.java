package com.zorykai.fishing.session;
import com.zorykai.fishing.*;
import com.zorykai.fishing.core.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.nbt.*;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;
import net.minecraft.registry.*;
import net.minecraft.world.World;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;
import net.minecraft.item.*;
import java.util.*;
import java.io.*;
import java.nio.file.*;
import java.sql.*;
import me.lucko.fabric.api.permissions.v0.Permissions;
import static com.zorykai.fishing.GameConfig.*;

public final class FishingSessions {
 public record Session(UUID player,String id,String phase,NbtCompound snapshot,String dimension,double x,double y,double z,float yaw,float pitch){}
 private final FishingMod mod;public final Map<UUID,Session> pending=new HashMap<>();private final Map<UUID,Long> lastSecond=new HashMap<>(),lastNotice=new HashMap<>();private final ThreadLocal<UUID> permitted=new ThreadLocal<>();
 public FishingSessions(FishingMod mod)throws Exception{this.mod=mod;mod.store.transaction(c->{try(var s=c.createStatement();var r=s.executeQuery("SELECT player,transaction_id,phase,snapshot,origin FROM sessions WHERE phase!='DONE'")){while(r.next()){var pos=com.google.gson.JsonParser.parseString(r.getString(5)).getAsJsonObject();var id=UUID.fromString(r.getString(1));var nbt=NbtIo.readCompressed(new ByteArrayInputStream(r.getBytes(4)),NbtSizeTracker.ofUnlimitedBytes());pending.put(id,new Session(id,r.getString(2),"RESTORING",nbt,str(pos,"dimension",""),pos.get("x").getAsDouble(),pos.get("y").getAsDouble(),pos.get("z").getAsDouble(),pos.get("yaw").getAsFloat(),pos.get("pitch").getAsFloat()));}}return null;});}
 public boolean active(ServerPlayerEntity p){var s=pending.get(p.getUuid());return s!=null&&s.phase.equals("ACTIVE");}
 public boolean protectedPlayer(ServerPlayerEntity p){return pending.containsKey(p.getUuid());}
 public boolean authorized(ServerPlayerEntity p){return p.getUuid().equals(permitted.get());}
 public boolean permitMove(ServerPlayerEntity p,ServerWorld target,double x,double y,double z){if(!protectedPlayer(p)||authorized(p))return true;if(Permissions.check(p,str(mod.rules.get("commands"),"teleportBypassPermission","zorykai_fishing.teleport_bypass"),false)){try{exit(p);if(!protectedPlayer(p)){mod.audit(p.getUuid(),"TELEPORT_BYPASS",Map.of("dimension",target.getRegistryKey().getValue().toString(),"x",x,"y",y,"z",z));return true;}}catch(Exception e){mod.fail(e);}}notice(p,"Use /pesca sair para deixar a pesca.");return false;}
 public void notice(ServerPlayerEntity p,String message){long now=System.currentTimeMillis();if(now-lastNotice.getOrDefault(p.getUuid(),0L)>3000){FishingMod.tell(p,message);lastNotice.put(p.getUuid(),now);}}
 public void enter(ServerPlayerEntity p)throws Exception{
  if(mod.delivery.pending(p.getUuid()))throw new IllegalStateException("Conclua a retirada de itens pendente antes de entrar");if(protectedPlayer(p))throw new IllegalStateException("Sessão ou recuperação já ativa");if(mod.config.map==null)throw new IllegalStateException("Cadastre primeiro a dimensão e o perímetro do mapa");
  if(mod.config.map.dimension().startsWith("multiworld:")&&!bool(mod.rules.get("integrations"),"multiworld",true))throw new IllegalStateException("Integração com Multiworld desativada");
  if(mod.config.map.dimension().startsWith("minecraft:")&&!bool(mod.rules.get("worlds"),"allowVanillaDimensions",false))throw new IllegalStateException("Configure uma dimensão exclusiva de pesca");
  if(!p.isAlive()||p.isSpectator()||p.isCreative()||com.cobblemon.mod.common.Cobblemon.INSTANCE.getBattleRegistry().getBattleByParticipatingPlayer(p)!=null)throw new IllegalStateException("Saia da batalha e use modo sobrevivência/aventura");
  if(!p.currentScreenHandler.getCursorStack().isEmpty())throw new IllegalStateException("Guarde o item do cursor antes de entrar");
  var world=world(mod.config.map.dimension());if(world==null||!safe(p,world,mod.config.map.entryX(),mod.config.map.entryY(),mod.config.map.entryZ()))throw new IllegalStateException("Entrada indisponível, insegura ou protegida pelo Guard");
  var profile=renew(p);if(balance(profile)<=0)throw new IllegalStateException("Seu tempo de pesca acabou");
  p.closeHandledScreen();var snapshot=p.writeNbt(new NbtCompound()).copy();var id=UUID.randomUUID().toString();var s=new Session(p.getUuid(),id,"PREPARED",snapshot,p.getWorld().getRegistryKey().getValue().toString(),p.getX(),p.getY(),p.getZ(),p.getYaw(),p.getPitch());
  var output=new ByteArrayOutputStream();NbtIo.writeCompressed(snapshot,output);var pos=new com.google.gson.JsonObject();pos.addProperty("dimension",s.dimension);pos.addProperty("x",s.x);pos.addProperty("y",s.y);pos.addProperty("z",s.z);pos.addProperty("yaw",s.yaw);pos.addProperty("pitch",s.pitch);
  mod.store.transaction(c->{try(var q=c.prepareStatement("INSERT INTO sessions VALUES(?,?,?,?,?,?) ON CONFLICT(player) DO UPDATE SET transaction_id=excluded.transaction_id,phase=excluded.phase,snapshot=excluded.snapshot,origin=excluded.origin,started=excluded.started")){q.setString(1,s.player.toString());q.setString(2,id);q.setString(3,"PREPARED");q.setBytes(4,output.toByteArray());q.setString(5,pos.toString());q.setLong(6,System.currentTimeMillis());/* column count handled below */q.executeUpdate();}return null;});
  pending.put(p.getUuid(),s);
  try{p.getInventory().clear();p.clearStatusEffects();p.changeGameMode(GameMode.ADVENTURE);p.getAbilities().invulnerable=true;p.sendAbilitiesUpdate();mod.engine.equip(p);
   if(!move(p,world,mod.config.map.entryX(),mod.config.map.entryY(),mod.config.map.entryZ(),0,0))throw new IllegalStateException("Teleporte de entrada recusado");
   phase(s,"ACTIVE");lastSecond.put(p.getUuid(),0L);mod.audit(p.getUuid(),"SESSION_ENTER",Map.of("transaction",id));
  }catch(Exception e){exit(p);throw e;}
 }
 public PlayerData renew(ServerPlayerEntity p)throws Exception{return mod.data.update(p.getUuid(),d->{d.name=p.getGameProfile().getName();long now=System.currentTimeMillis();long hours=num(mod.rules.get("timers"),"cycleHours",24);if(d.cycleStart==0||now-d.cycleStart>=hours*3600000L){int minutes=num(mod.rules.get("timers"),"normalMinutes",60);for(var row:rows(mod.rules.get("vip-time")))if(Permissions.check(p,str(row,"permission",""),false))minutes=Math.max(minutes,num(row,"minutes",60));long credits=d.credits.stream().filter(x->x.expires>now&&x.seconds>0).mapToLong(x->x.seconds).sum();d.remainingSeconds=Math.min(minutes*60L,Math.max(0,num(mod.rules.get("timers"),"maximumAccumulatedMinutes",360)*60L-credits));d.cycleStart=now;}d.credits.removeIf(c->c.expires<=now||c.seconds<=0);return d;});}
 public long balance(PlayerData p){long now=System.currentTimeMillis();return p.remainingSeconds+p.credits.stream().filter(x->x.expires>now).mapToLong(x->x.seconds).sum();}
 public void addTime(PlayerData d,long seconds,long validityHours){long maximum=num(mod.rules.get("timers"),"maximumAccumulatedMinutes",360)*60L;long add=Math.min(Math.max(0,seconds),Math.max(0,maximum-balance(d)));if(add>0)d.credits.add(new PlayerData.Credit(add,System.currentTimeMillis()+Math.max(1,validityHours)*3600000L));}
 public void tick(){for(var entry:List.copyOf(pending.entrySet())){var p=mod.server.getPlayerManager().getPlayer(entry.getKey());if(p==null)continue;if(!entry.getValue().phase.equals("ACTIVE")){continue;}try{
   if(mod.config.map==null||!mod.config.map.dimension().equals(p.getWorld().getRegistryKey().getValue().toString())||!mod.config.map.bounds().contains(p.getX(),p.getY(),p.getZ())){exit(p);continue;}
   long second=System.nanoTime()/1000000000L;if(second==lastSecond.getOrDefault(p.getUuid(),-1L))continue;lastSecond.put(p.getUuid(),second);
   var data=renew(p);long remaining=balance(data);if(remaining<=0){exit(p);continue;}
   mod.data.update(p.getUuid(),d->{if(d.remainingSeconds>0)d.remainingSeconds--;else for(var credit:d.credits)if(credit.expires>System.currentTimeMillis()&&credit.seconds>0){credit.seconds--;break;}return null;});
   if(bool(mod.rules.get("timers"),"actionbar",true))p.sendMessage(Text.literal("✦ ZORYKAI · Tempo: "+(remaining/60)+"m "+remaining%60+"s"),true);
   for(var warn:mod.rules.get("timers").getAsJsonArray("warnings"))if(remaining==warn.getAsLong())p.sendMessage(Text.literal("✦ ZORYKAI · Restam "+remaining+" segundos de pesca."),false);
  }catch(Exception e){mod.fail(e);}}}
 void phase(Session old,String phase)throws Exception{mod.store.transaction(c->{try(var p=c.prepareStatement("UPDATE sessions SET phase=? WHERE player=? AND transaction_id=?")){p.setString(1,phase);p.setString(2,old.player.toString());p.setString(3,old.id);if(p.executeUpdate()!=1)throw new SQLException("Sessão divergente");}return null;});pending.put(old.player,new Session(old.player,old.id,phase,old.snapshot,old.dimension,old.x,old.y,old.z,old.yaw,old.pitch));}
 public void exit(ServerPlayerEntity p)throws Exception{
  var s=pending.get(p.getUuid());if(s==null)return;mod.engine.closeEncounter(p.getUuid());if(!s.phase.equals("RESTORING"))mod.delivery.quarantine(p,s);phase(s,"RESTORING");
  var world=world(s.dimension);double x=s.x,y=s.y,z=s.z;
  if(world==null||!safe(p,world,x,y,z)){var fallback=mod.rules.get("worlds").get("recoverySpawn");if(fallback==null||fallback.isJsonNull()){notice(p,"Retorno indisponível: dados preservados. Avise a administração.");return;}var f=fallback.getAsJsonObject();world=world(str(f,"dimension",""));x=decimal(f,"x",Double.NaN);y=decimal(f,"y",Double.NaN);z=decimal(f,"z",Double.NaN);if(world==null||!safe(p,world,x,y,z)){notice(p,"Destino alternativo inválido; recuperação preservada.");return;}}
  if(!move(p,world,x,y,z,s.yaw,s.pitch)){notice(p,"Teleporte recusado; recuperação preservada.");return;}
  restore(p,s);saveVerified(p,s.snapshot);complete(p,s);mod.audit(p.getUuid(),"SESSION_EXIT",Map.of("transaction",s.id));
 }
 static final List<String> RESTORE=List.of("Inventory","SelectedItemSlot","XpLevel","XpP","XpTotal","foodLevel","foodSaturationLevel","foodExhaustionLevel","Health","AbsorptionAmount","ActiveEffects","active_effects","abilities","playerGameType","previousPlayerGameType");
 public void restore(ServerPlayerEntity p,Session s){p.closeHandledScreen();var data=p.writeNbt(new NbtCompound());for(String key:RESTORE){if(s.snapshot.contains(key))data.put(key,s.snapshot.get(key).copy());else data.remove(key);}p.clearStatusEffects();p.readCustomDataFromNbt(data);p.addCommandTag("zf_restored:"+s.id);p.sendAbilitiesUpdate();p.getInventory().markDirty();p.playerScreenHandler.sendContentUpdates();}
 public Optional<NbtCompound> beforeLogin(ServerPlayerEntity p,Optional<NbtCompound> original){var s=pending.get(p.getUuid());if(s==null)return original;try{
   var loaded=original.orElseGet(()->p.writeNbt(new NbtCompound())).copy();if(loaded.getList("Tags",8).asString().contains("zf_restored:"+s.id))return original;mod.delivery.quarantineLoaded(p,s,loaded);
   var world=world(s.dimension);if(world==null||!safe(p,world,s.x,s.y,s.z))return original;
   for(String key:RESTORE){if(s.snapshot.contains(key))loaded.put(key,s.snapshot.get(key).copy());else loaded.remove(key);}
   loaded.putString("Dimension",s.dimension);var pos=new NbtList();pos.add(NbtDouble.of(s.x));pos.add(NbtDouble.of(s.y));pos.add(NbtDouble.of(s.z));loaded.put("Pos",pos);var rot=new NbtList();rot.add(NbtFloat.of(s.yaw));rot.add(NbtFloat.of(s.pitch));loaded.put("Rotation",rot);var tags=loaded.getList("Tags",8);tags.add(NbtString.of("zf_restored:"+s.id));loaded.put("Tags",tags);p.readNbt(loaded);return Optional.of(loaded);
  }catch(Exception e){mod.fail(e);return original;}}
 public void joined(ServerPlayerEntity p){var s=pending.get(p.getUuid());if(s==null)return;try{if(p.getCommandTags().contains("zf_restored:"+s.id)){saveVerified(p,s.snapshot);complete(p,s);}else exit(p);}catch(Exception e){mod.fail(e);}}
 void complete(ServerPlayerEntity p,Session s)throws Exception{phase(s,"DONE");pending.remove(p.getUuid());lastSecond.remove(p.getUuid());p.removeCommandTag("zf_restored:"+s.id);}
 public void saveVerified(ServerPlayerEntity p,NbtCompound snapshot)throws Exception{((com.zorykai.fishing.mixin.PlayerSaveInvoker)mod.server.getPlayerManager()).fishing$save(p);var file=mod.server.getSavePath(WorldSavePath.PLAYERDATA).resolve(p.getUuid()+".dat");var saved=NbtIo.readCompressed(file,NbtSizeTracker.ofUnlimitedBytes());if(!sameInventory(snapshot.getList("Inventory",10),saved.getList("Inventory",10)))throw new IOException("Inventário restaurado diverge; reserva preservada");try(var channel=java.nio.channels.FileChannel.open(file,StandardOpenOption.WRITE)){channel.force(true);}}
 boolean sameInventory(NbtList a,NbtList b){var left=new HashMap<Integer,ItemStack>();var right=new HashMap<Integer,ItemStack>();for(var pair:List.of(Map.entry(a,left),Map.entry(b,right)))for(int i=0;i<pair.getKey().size();i++){var n=pair.getKey().getCompound(i);pair.getValue().put(n.getByte("Slot")&255,ItemStack.fromNbt(mod.server.getRegistryManager(),n).orElse(ItemStack.EMPTY));}if(!left.keySet().equals(right.keySet()))return false;for(int slot:left.keySet())if(!ItemStack.areEqual(left.get(slot),right.get(slot)))return false;return true;}
 ServerWorld world(String dimension){try{return mod.server.getWorld(RegistryKey.of(RegistryKeys.WORLD,Identifier.of(dimension)));}catch(Exception e){return null;}}
 boolean safe(ServerPlayerEntity p,ServerWorld world,double x,double y,double z){if(!Double.isFinite(x)||!Double.isFinite(y)||!Double.isFinite(z)||Math.abs(x)>29999984||Math.abs(z)>29999984)return false;var pos=BlockPos.ofFloored(x,y,z);if(y<=world.getBottomY()||y+2>=world.getTopY()||!world.getWorldBorder().contains(pos))return false;world.getChunk(pos);if(!world.getFluidState(pos).isEmpty()||!world.getFluidState(pos.up()).isEmpty()||Set.of(net.minecraft.block.Blocks.MAGMA_BLOCK,net.minecraft.block.Blocks.CACTUS,net.minecraft.block.Blocks.CAMPFIRE,net.minecraft.block.Blocks.SOUL_CAMPFIRE).contains(world.getBlockState(pos.down()).getBlock())||world.getBlockState(pos).isOf(net.minecraft.block.Blocks.FIRE)||world.getBlockState(pos).isOf(net.minecraft.block.Blocks.SOUL_FIRE))return false;if(!world.getBlockState(pos.down()).isSideSolidFullSquare(world,pos.down(),Direction.UP)||!world.isSpaceEmpty(p,p.getBoundingBox().offset(x-p.getX(),y-p.getY(),z-p.getZ())))return false;return GuardCheck.safeLogin(p,world,pos);}
 boolean move(ServerPlayerEntity p,ServerWorld world,double x,double y,double z,float yaw,float pitch){permitted.set(p.getUuid());try{return p.teleport(world,x,y,z,Set.of(),yaw,pitch)&&p.getServerWorld()==world&&p.getPos().squaredDistanceTo(x,y,z)<.01;}finally{permitted.remove();}}
 public void stop(){for(var id:List.copyOf(pending.keySet())){var p=mod.server.getPlayerManager().getPlayer(id);if(p!=null)try{exit(p);}catch(Exception e){mod.fail(e);}}}
}
