package com.zorykai.fishing;
import com.zorykai.fishing.core.*;
import com.zorykai.fishing.mixin.PlayerSaveInvoker;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.nbt.*;
import net.minecraft.item.*;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import java.util.*;
import java.nio.file.*;
/** Two distinct JVMs with SIGKILL between them; no graceful shutdown recovery. */
final class FishingRestartChecks {
 static final UUID OWNER=UUID.fromString("60dc73d1-bf49-4d2a-973d-640fb2b52538");
 static void register(FishingProofChecks proof,String phase){int[] ticks={0};ServerTickEvents.END_SERVER_TICK.register(s->{if(++ticks[0]!=30)return;try{var m=FishingMod.INSTANCE;var p=proof.player(s,"FishingCrash",OWNER);if(phase.equals("prepare")){
   var map=new MapDefinition("mapa","minecraft:overworld",new Bounds(-10,60,-10,10,80,10),.5,64,.5);FishingConfiguration.write(m.config.root.resolve("mapa.json"),map);m.config.reload();m.rules.get("worlds").addProperty("allowVanillaDimensions",true);
   for(int x=-2;x<=34;x++)for(int z=-2;z<=2;z++){s.getOverworld().setBlockState(new BlockPos(x,63,z),Blocks.STONE.getDefaultState());s.getOverworld().setBlockState(new BlockPos(x,64,z),Blocks.AIR.getDefaultState());s.getOverworld().setBlockState(new BlockPos(x,65,z),Blocks.AIR.getDefaultState());}
   p.setPosition(32.5,64,.5);p.getInventory().setStack(0,new ItemStack(Items.DIAMOND,42));p.getInventory().armor.set(0,new ItemStack(Items.DIAMOND_BOOTS));p.getInventory().offHand.set(0,new ItemStack(Items.TOTEM_OF_UNDYING));p.setExperienceLevel(18);var snapshot=p.writeNbt(new NbtCompound());NbtIo.writeCompressed(snapshot,Path.of("expected-original.nbt"));m.sessions.enter(p);((PlayerSaveInvoker)s.getPlayerManager()).fishing$save(p);s.save(false,true,true);Files.writeString(Path.of("restart-ready"),"Session ACTIVE with temporary inventory saved to playerdata; terminate process without stop");
  }else{
   proof.check("New JVM reads pending Fishing recovery from durable SQLite",m.sessions.protectedPlayer(p)&&!m.sessions.active(p));p.setPosition(.5,64,.5);var expected=NbtIo.readCompressed(Path.of("expected-original.nbt"),NbtSizeTracker.ofUnlimitedBytes());var loaded=s.getPlayerManager().loadPlayerData(p);proof.check("After actual process kill native login restores original inventory before JOIN",loaded.isPresent()&&p.getInventory().writeNbt(new NbtList()).equals(expected.getList("Inventory",10))&&p.getInventory().count(Items.DIAMOND)==42&&Math.abs(p.getX()-32.5)<.01);m.sessions.joined(p);proof.check("Post-crash recovery preserves armor offhand XP and clears only verified reservation",!m.sessions.protectedPlayer(p)&&p.experienceLevel==18&&p.getInventory().armor.getFirst().isOf(Items.DIAMOND_BOOTS)&&p.getInventory().offHand.getFirst().isOf(Items.TOTEM_OF_UNDYING));s.getPlayerManager().loadPlayerData(p);m.sessions.joined(p);proof.check("Repeated login after crash does not duplicate inventory",p.getInventory().count(Items.DIAMOND)==42&&!m.sessions.protectedPlayer(p));proof.finish(s);
  }}catch(Throwable e){e.printStackTrace();proof.checks.add(Map.of("name","restart-fatal","passed",false,"error",e.toString()));proof.finish(s);}});}
}
