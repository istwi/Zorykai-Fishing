package com.zorykai.fishing;

import com.zorykai.fishing.core.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.sound.*;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.*;
import java.util.*;
import static com.zorykai.fishing.GameConfig.*;

/** Private region hints. No entities/blocks created, chunks loaded, or fishing permissions changed. */
public final class AreaFeedback {
 private final FishingMod mod;
 private static final class Visit {String current="",candidate="";int since,cursor;}
 private final Map<UUID,Visit> visits=new HashMap<>();private final Map<Integer,List<BlockPos>> grids=new HashMap<>();private int lastTick=-1;
 public AreaFeedback(FishingMod mod){this.mod=mod;}
 public void clear(UUID owner){visits.remove(owner);}
 public void tick(){int tick=mod.server.getTicks();if(tick==lastTick||tick%5!=0)return;lastTick=tick;for(var p:mod.server.getPlayerManager().getPlayerList()){if(!mod.sessions.active(p)){clear(p.getUuid());continue;}try{updatePlayer(p,tick);if(tick%num(mod.rules.get("area-feedback"),"markerIntervalTicks",20)==0)mark(p);}catch(Exception e){mod.fail(e);}}}
 public void updatePlayer(ServerPlayerEntity p,int tick)throws Exception{
  if(!mod.sessions.active(p))return;var cfg=mod.rules.get("area-feedback");var visit=visits.computeIfAbsent(p.getUuid(),k->new Visit());Lake lake=null;for(int down=0;down<=num(cfg,"entryHeightTolerance",2)&&lake==null;down++)lake=mod.config.at(p.getWorld().getRegistryKey().getValue().toString(),p.getX(),p.getY()-down,p.getZ());String id=lake==null?"":lake.id();
  if(!id.equals(visit.candidate)){visit.candidate=id;visit.since=tick;}
  if(tick-visit.since<num(cfg,"entryStableTicks",10)||visit.current.equals(id))return;
  visit.current=id;if(lake==null||!bool(cfg,"entryEnabled",true))return;
  boolean allowed=canFish(p,lake,mod.data.profile(p.getUuid()));
  if(bool(cfg,"entrySound",true))p.networkHandler.sendPacket(new PlaySoundS2CPacket(Registries.SOUND_EVENT.getEntry(SoundEvents.BLOCK_NOTE_BLOCK_BELL.value()),SoundCategory.PLAYERS,p.getX(),p.getY(),p.getZ(),.35f,1.35f,p.getRandom().nextLong()));
  p.networkHandler.sendPacket(new TitleFadeS2CPacket(5,30,10));
  p.networkHandler.sendPacket(new SubtitleS2CPacket(Text.literal(lake.name()+(!allowed?" · pesca bloqueada":"")).formatted(allowed?Formatting.AQUA:Formatting.RED)));
  p.networkHandler.sendPacket(new TitleS2CPacket(Text.empty()));
 }
 private boolean canFish(ServerPlayerEntity p,Lake lake,PlayerData d){return mod.engine.allowed(p,lake,d)&&(lake.rods().isEmpty()||lake.rods().contains(d.selectedRod))&&(lake.baits().isEmpty()||lake.baits().contains(d.selectedBait));}
 public void mark(ServerPlayerEntity p)throws Exception{
  var cfg=mod.rules.get("area-feedback");if(!mod.sessions.active(p)||!bool(cfg,"markersEnabled",true))return;
  int radius=num(cfg,"markerRadius",12),cap=num(cfg,"maximumParticles",12),budget=128,used=0;var world=p.getServerWorld();var profile=mod.data.profile(p.getUuid());var visit=visits.computeIfAbsent(p.getUuid(),k->new Visit());var grid=grids.computeIfAbsent(radius,r->{var points=new ArrayList<BlockPos>();for(int x=-r;x<=r;x++)for(int z=-r;z<=r;z++)if(x*x+z*z<=r*r)points.add(new BlockPos(x,0,z));points.sort(Comparator.comparingInt((BlockPos pos)->pos.getX()*pos.getX()+pos.getZ()*pos.getZ()).thenComparingInt(BlockPos::getX).thenComparingInt(BlockPos::getZ));return List.copyOf(points);});int total=grid.size();
  var lakes=mod.config.lakes.values().stream().filter(l->l.dimension().equals(world.getRegistryKey().getValue().toString())&&l.bounds().minX()<=p.getX()+radius&&l.bounds().maxX()+1>=p.getX()-radius&&l.bounds().minZ()<=p.getZ()+radius&&l.bounds().maxZ()+1>=p.getZ()-radius).sorted(Comparator.comparing(Lake::id)).toList();
  for(int n=0;n<Math.min(budget,total)&&used<cap;n++){
   var offset=grid.get(Math.floorMod(visit.cursor++,total));int x=p.getBlockX()+offset.getX(),z=p.getBlockZ()+offset.getZ();
   if(Vec3d.ofCenter(new BlockPos(x,p.getBlockY(),z)).squaredDistanceTo(p.getPos())>radius*radius)continue;
   for(var lake:lakes){var b=lake.bounds();if(x<b.minX()||x>b.maxX()||z<b.minZ()||z>b.maxZ())continue;
    for(int y=Math.min(b.maxY(),p.getBlockY()+4);y>=Math.max(b.minY(),p.getBlockY()-4);y--){var pos=new BlockPos(x,y,z);if(!world.isChunkLoaded(pos)||!world.isChunkLoaded(pos.north())||!world.isChunkLoaded(pos.south())||!world.isChunkLoaded(pos.east())||!world.isChunkLoaded(pos.west()))continue;
     if(!world.getFluidState(pos).isIn(FluidTags.WATER)||!world.getFluidState(pos.up()).isEmpty()||!world.getBlockState(pos.up()).getCollisionShape(world,pos.up()).isEmpty())continue;
     boolean edge=x==b.minX()||x==b.maxX()||z==b.minZ()||z==b.maxZ()||!world.getFluidState(pos.north()).isIn(FluidTags.WATER)||!world.getFluidState(pos.south()).isIn(FluidTags.WATER)||!world.getFluidState(pos.east()).isIn(FluidTags.WATER)||!world.getFluidState(pos.west()).isIn(FluidTags.WATER);
     if(edge){boolean allowed=canFish(p,lake,profile);var color=allowed?new org.joml.Vector3f(.2f,.8f,1f):new org.joml.Vector3f(1f,.25f,.2f);double surface=y+world.getFluidState(pos).getHeight(world,pos)+.03;world.spawnParticles(p,new DustParticleEffect(color,.65f),true,x+.5,surface,z+.5,1,0,0,0,0);used++;}break;
    }break;
   }
  }
 }
}
