package com.zorykai.fishing;

import com.zorykai.fishing.session.GuardCheck;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import java.util.*;
import static com.zorykai.fishing.GameConfig.*;

/** Session equipment is a reusable tool; consumable stock lives in the durable profile. */
public final class FishingEquipment {
 public static final String BOAT="zf_boat";
 private final FishingMod mod;
 private record Recharge(int until,int duration){}
 private record Vessel(String session,BoatEntity entity){}
 private final Map<UUID,Recharge> recharge=new HashMap<>();
 private final Map<UUID,Integer> resync=new HashMap<>();
 private final Map<UUID,Integer> lastUse=new HashMap<>();
 private final Map<UUID,Vessel> boats=new HashMap<>();
 public FishingEquipment(FishingMod mod){this.mod=mod;}
 public void sync(ServerPlayerEntity p){syncNow(p);resync.put(p.getUuid(),mod.server.getTicks()+1);}
 private void syncNow(ServerPlayerEntity p){
  p.getInventory().markDirty();p.playerScreenHandler.syncState();
  // -2 addresses PlayerInventory directly, including selected slot and offhand.
  p.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(-2,0,p.getInventory().selectedSlot,p.getMainHandStack().copy()));
  p.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(-2,0,40,p.getOffHandStack().copy()));
 }
 /** A single click on water can send both block-use and item-use packets. */
 public boolean acceptUse(ServerPlayerEntity p){int tick=mod.server.getTicks();if(tick-lastUse.getOrDefault(p.getUuid(),-100)<4)return false;lastUse.put(p.getUuid(),tick);return true;}
 public boolean cooling(ServerPlayerEntity p){var r=recharge.get(p.getUuid());return r!=null&&r.until>mod.server.getTicks();}
 public double cooldownSeconds(ServerPlayerEntity p)throws Exception{return cooldownSeconds(mod.data.profile(p.getUuid()).ballCooldownLevel);}
 public double cooldownSeconds(int level){var shop=mod.rules.get("cooldown-shop");return com.zorykai.fishing.core.BallRecharge.seconds(decimal(mod.rules.get("capture"),"ballCooldownSeconds",10),decimal(shop,"minimumCooldownSeconds",2),decimal(shop,"secondsPerLevel",1),num(shop,"maximumLevel",5),level);}
 public void beginCooldown(ServerPlayerEntity p)throws Exception{
  int ticks=(int)Math.ceil(cooldownSeconds(p)*20);
  recharge.put(p.getUuid(),new Recharge(mod.server.getTicks()+ticks,ticks));updateCooldown(p);showRecharge(p,ticks,ticks);
 }
 private void showRecharge(ServerPlayerEntity p,int remaining,int duration){
  int filled=Math.max(0,Math.min(20,(int)Math.floor(20d*(duration-remaining)/duration)));
  var text=Text.literal("Recarga da bola · ").formatted(net.minecraft.util.Formatting.GRAY)
   .append(Text.literal("I".repeat(filled)).formatted(net.minecraft.util.Formatting.AQUA))
   .append(Text.literal("I".repeat(20-filled)).formatted(net.minecraft.util.Formatting.DARK_GRAY))
   .append(Text.literal(remaining==0?" · Pronta!":String.format(Locale.forLanguageTag("pt-BR")," · %.1fs",remaining/20d)).formatted(net.minecraft.util.Formatting.GRAY));
  p.sendMessage(text,true);
 }
 public void updateCooldown(ServerPlayerEntity p){var r=recharge.get(p.getUuid());if(r==null)return;int remaining=Math.max(0,r.until-mod.server.getTicks());for(var def:rows(mod.rules.get("pokeballs"))){var item=net.minecraft.registry.Registries.ITEM.get(net.minecraft.util.Identifier.of(str(def,"item","cobblemon:poke_ball")));if(remaining>0)p.getItemCooldownManager().set(item,remaining);else p.getItemCooldownManager().remove(item);}}
 public void bite(ServerPlayerEntity p,FishingBobberEntity bobber){signal(p,bobber,false);
  p.networkHandler.sendPacket(new net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket(net.minecraft.registry.Registries.SOUND_EVENT.getEntry(SoundEvents.ENTITY_FISHING_BOBBER_SPLASH),SoundCategory.PLAYERS,bobber.getX(),bobber.getY(),bobber.getZ(),.8f,1.25f,p.getRandom().nextLong()));
  p.getServerWorld().spawnParticles(p,ParticleTypes.SPLASH,true,bobber.getX(),bobber.getY()+.15,bobber.getZ(),16,.25,.15,.25,.1);
  p.getServerWorld().spawnParticles(p,ParticleTypes.BUBBLE,true,bobber.getX(),bobber.getY(),bobber.getZ(),10,.2,.1,.2,.05);
 }
 public void pull(ServerPlayerEntity p,FishingBobberEntity bobber){signal(p,bobber,true);p.networkHandler.sendPacket(new net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket(net.minecraft.registry.Registries.SOUND_EVENT.getEntry(SoundEvents.ENTITY_FISHING_BOBBER_SPLASH),SoundCategory.PLAYERS,bobber.getX(),bobber.getY(),bobber.getZ(),.6f,.65f,p.getRandom().nextLong()));p.getServerWorld().spawnParticles(p,ParticleTypes.SPLASH,true,bobber.getX(),bobber.getY()+.15,bobber.getZ(),6,.2,.1,.2,.08);}
 public void signal(ServerPlayerEntity p,FishingBobberEntity bobber,boolean pulling){if(!bool(mod.rules.get("capture"),"bobberSignalEnabled",true))return;var pos=bobber.getBlockPos();var world=p.getServerWorld();if(!world.isChunkLoaded(pos)||!world.getFluidState(pos).isIn(FluidTags.WATER))return;double y=pos.getY()+world.getFluidState(pos).getHeight(world,pos)+.12;world.spawnParticles(p,new net.minecraft.particle.DustParticleEffect(pulling?new org.joml.Vector3f(1f,.15f,.1f):new org.joml.Vector3f(.1f,.45f,1f),(float)decimal(mod.rules.get("capture"),"bobberSignalScale",1.4)),true,pos.getX()+.5,y,pos.getZ()+.5,4,.15,.05,.15,0);}
 public boolean ownsLine(FishingBobberEntity bobber){var owner=bobber.getPlayerOwner();var line=owner==null?null:mod.engine.lines.get(owner.getUuid());return line!=null&&line.bobber==bobber;}
 public boolean mayRide(Entity rider,Entity target){
  if(!target.getCommandTags().contains(BOAT))return true;
  var v=boats.get(rider.getUuid());return rider instanceof ServerPlayerEntity p&&mod.sessions.active(p)&&v!=null&&v.entity==target&&mod.sessions.pending.get(p.getUuid()).id().equals(v.session);
 }
 public boolean knownBoat(Entity entity){return boats.values().stream().anyMatch(v->v.entity==entity);}
 public void useBoat(ServerPlayerEntity p)throws Exception{
  if(!mod.sessions.active(p)||!bool(mod.rules.get("navigation"),"boatEnabled",true))return;
  var current=boats.get(p.getUuid());if(current!=null){removeBoat(p.getUuid());sync(p);return;}
  var hit=p.raycast(6,1,true);if(!(hit instanceof BlockHitResult block)||hit.getType()!=HitResult.Type.BLOCK)throw new IllegalArgumentException("Mire na água próxima para colocar o bote.");
  var pos=block.getBlockPos();var world=p.getServerWorld();if(!world.getFluidState(pos).isIn(FluidTags.WATER))throw new IllegalArgumentException("Coloque o bote na água.");
  var boat=new BoatEntity(world,pos.getX()+.5,pos.getY()+.8,pos.getZ()+.5);boat.setVariant(BoatEntity.Type.OAK);boat.setYaw(p.getYaw());boat.setInvulnerable(true);boat.addCommandTag(BOAT);boat.addCommandTag("zf_owner:"+p.getUuid());boat.addCommandTag("zf_session:"+mod.sessions.pending.get(p.getUuid()).id());
  if(!inside(world,boat.getPos())||!inside(world,new Vec3d(boat.getBoundingBox().minX,boat.getY(),boat.getBoundingBox().minZ))||!inside(world,new Vec3d(boat.getBoundingBox().maxX,boat.getY(),boat.getBoundingBox().maxZ))||!world.getWorldBorder().contains(boat.getBlockPos())||!world.isSpaceEmpty(boat,boat.getBoundingBox())||!GuardCheck.safeLogin(p,world,boat.getBlockPos()))throw new IllegalArgumentException("Sem espaço ou permissão para colocar o bote neste local.");
  boats.put(p.getUuid(),new Vessel(mod.sessions.pending.get(p.getUuid()).id(),boat));if(!world.spawnEntity(boat)){boats.remove(p.getUuid());throw new IllegalStateException("Não foi possível colocar o bote.");}sync(p);
 }
 public boolean interactBoat(ServerPlayerEntity p,Entity target){
  if(!target.getCommandTags().contains(BOAT))return false;
  if(!mayRide(p,target))return true;
  if(p.isSneaking())removeBoat(p.getUuid());else p.startRiding(target);
  sync(p);return true;
 }
 private boolean inside(ServerWorld world,Vec3d pos){var map=mod.config.map;return map!=null&&map.dimension().equals(world.getRegistryKey().getValue().toString())&&map.bounds().contains(pos.x,pos.y,pos.z);}
 public void removeBoat(UUID owner){var vessel=boats.remove(owner);if(vessel!=null){vessel.entity.removeAllPassengers();vessel.entity.discard();}}
 public void close(UUID owner){var r=recharge.remove(owner);if(r!=null){var p=mod.server.getPlayerManager().getPlayer(owner);if(p!=null)for(var def:rows(mod.rules.get("pokeballs")))p.getItemCooldownManager().remove(net.minecraft.registry.Registries.ITEM.get(net.minecraft.util.Identifier.of(str(def,"item","cobblemon:poke_ball"))));}removeBoat(owner);resync.remove(owner);lastUse.remove(owner);}
 public void tick(){
  int tick=mod.server.getTicks();for(var e:List.copyOf(resync.entrySet()))if(e.getValue()<=tick){resync.remove(e.getKey());var p=mod.server.getPlayerManager().getPlayer(e.getKey());if(p!=null&&mod.sessions.protectedPlayer(p))syncNow(p);}
  for(var e:List.copyOf(recharge.entrySet())){var p=mod.server.getPlayerManager().getPlayer(e.getKey());var r=e.getValue();int remaining=r.until-tick;if(p==null||!mod.sessions.active(p)||remaining<=0){if(p!=null){updateCooldown(p);if(mod.sessions.active(p))showRecharge(p,0,r.duration);}recharge.remove(e.getKey());continue;}showRecharge(p,remaining,r.duration);}
  if(tick%20==0){for(var e:List.copyOf(boats.entrySet())){var p=mod.server.getPlayerManager().getPlayer(e.getKey());var b=e.getValue().entity;if(p==null||!mod.sessions.active(p)||!b.isAlive()||!inside((ServerWorld)b.getWorld(),b.getPos())||!GuardCheck.safeLogin(p,(ServerWorld)b.getWorld(),b.getBlockPos()))removeBoat(e.getKey());}FishingHabitat.sweep(mod);}
 }
}
