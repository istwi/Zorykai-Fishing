package com.zorykai.fishing;

import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.entity.pokeball.EmptyPokeBallEntity;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import net.fabricmc.fabric.api.event.player.*;
import net.minecraft.block.Blocks;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.*;
import net.minecraft.network.*;
import net.minecraft.network.packet.*;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.*;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.hit.*;
import net.minecraft.util.math.*;
import java.util.*;

/** Real server entities/callbacks and outbound packets; no graphical client is simulated. */
public final class FishingInteractionChecks {
 final FishingProofChecks proof;FishingMod m;ServerPlayerEntity p,other;Recording net;int ready,stage;BoatEntity boat;java.util.concurrent.CompletableFuture<Boolean> funding;
 FishingInteractionChecks(FishingProofChecks proof){this.proof=proof;}
 void check(String name,boolean ok){proof.check(name,ok);}
 static final class Recording extends ServerPlayNetworkHandler {
  final List<Packet<?>> packets=new ArrayList<>();
  Recording(MinecraftServer s,ServerPlayerEntity p){super(s,new ClientConnection(NetworkSide.SERVERBOUND),p,ConnectedClientData.createDefault(p.getGameProfile(),false));}
  @Override public void sendPacket(Packet<?> packet){if(packets!=null)packets.add(packet);}
  @Override public void send(Packet<?> packet,PacketCallbacks callbacks){if(packets!=null)packets.add(packet);}
 }
 public void start(MinecraftServer s)throws Exception{
  m=FishingMod.INSTANCE;p=proof.player(s,"FishingEquipment");other=proof.player(s,"FishingBoatOther");net=new Recording(s,p);p.networkHandler=net;
  m.rules.get("worlds").addProperty("allowVanillaDimensions",true);
  for(int x=-10;x<=25;x++)for(int z=-10;z<=10;z++)s.getOverworld().setBlockState(new BlockPos(x,63,z),Blocks.STONE.getDefaultState());
  for(int x=1;x<=8;x++)for(int z=1;z<=8;z++)s.getOverworld().setBlockState(new BlockPos(x,64,z),Blocks.WATER.getDefaultState());
  // Contain the fixture pool: flowing water must not flood the reserved map entrance.
  for(int n=1;n<=9;n++){s.getOverworld().setBlockState(new BlockPos(0,64,n),Blocks.STONE.getDefaultState());s.getOverworld().setBlockState(new BlockPos(9,64,n),Blocks.STONE.getDefaultState());s.getOverworld().setBlockState(new BlockPos(n,64,0),Blocks.STONE.getDefaultState());s.getOverworld().setBlockState(new BlockPos(n,64,9),Blocks.STONE.getDefaultState());}
  p.setPosition(20.5,64,.5);p.getInventory().setStack(4,new ItemStack(Items.DIAMOND,11));m.sessions.enter(p);
  check("Default capture cooldown starts at ten seconds",m.engine.equipment.cooldownSeconds(p)==10);
  check("Session equips one reusable temporary boat",m.engine.gearType(p,p.getInventory().getStack(2)).equals("boat")&&p.getInventory().getStack(2).getCount()==1);
  // Spawn suppression also covers commands/direct entity insertion, not only Cobblemon's natural spawner.
  var denied=PokemonProperties.Companion.parse("magikarp"," ","=").createEntity(s.getOverworld());denied.setPosition(7,65,7);check("Wild Pokemon spawn blocked in construction outside lake",!s.getOverworld().spawnEntity(denied));
  var outside=PokemonProperties.Companion.parse("magikarp"," ","=").createEntity(s.getOverworld());outside.setPosition(24,65,0);check("Pokemon outside construction can still spawn",s.getOverworld().spawnEntity(outside));outside.discard();
  var forged=PokemonProperties.Companion.parse("magikarp"," ","=").createEntity(s.getOverworld());forged.addCommandTag(FishingMod.TAG);forged.setPosition(2,65,2);check("Tag alone cannot bypass fishing encounter spawn authorization",!s.getOverworld().spawnEntity(forged));
  var encounter=m.spawnProof(p,s.getOverworld(),new BlockPos(2,64,2),m.config.lakes.values().iterator().next());check("Authorized fished Pokemon still spawns inside construction",encounter.entity().isAlive());
  m.data.update(p.getUuid(),d->{d.selectedBall="boa";d.balls.put("boa",2);return null;});m.rules.definition("pokeballs","boa").addProperty("captureChance",1);m.engine.equip(p);p.getInventory().selectedSlot=1;p.setPosition(2.5,65,-.5);net.packets.clear();
  UseItemCallback.EVENT.invoker().interact(p,p.getWorld(),Hand.MAIN_HAND);
  check("Throw sends authoritative hand slot even when server stack is unchanged",net.packets.stream().anyMatch(packet->packet instanceof ScreenHandlerSlotUpdateS2CPacket slot&&slot.getSyncId()==-2&&slot.getSlot()==1&&slot.getStack().getCount()==1));
  check("Capture ball remains in selected hand after throw",p.getMainHandStack().getCount()==1&&m.engine.gearType(p,p.getMainHandStack()).equals("ball"));
  for(int i=0;i<100;i++)UseItemCallback.EVENT.invoker().interact(p,p.getWorld(),Hand.MAIN_HAND);
  check("100 rapid uses debit finite ball stock only once during cooldown",m.data.profile(p.getUuid()).balls.get("boa")==1);
  check("Cooldown sends filling action bar and native overlay, without boss bar",net.packets.stream().anyMatch(packet->packet instanceof CooldownUpdateS2CPacket)&&net.packets.stream().anyMatch(packet->packet instanceof GameMessageS2CPacket message&&message.overlay()&&message.content().getString().contains("IIIIIIIIIIIIIIIIIIII"))&&net.packets.stream().noneMatch(packet->packet instanceof BossBarS2CPacket));
  var projectile=(EmptyPokeBallEntity)java.util.stream.StreamSupport.stream(p.getServerWorld().iterateEntities().spliterator(),false).filter(e->e instanceof EmptyPokeBallEntity ball&&ball.getOwner()==p).findFirst().orElseThrow();
  for(var method:projectile.getClass().getDeclaredMethods())if(Arrays.equals(method.getParameterTypes(),new Class[]{EntityHitResult.class})){method.setAccessible(true);method.invoke(projectile,new EntityHitResult(encounter.entity()));break;}
  check("Projectile hit records exactly one capture and retains reusable icon",m.store.captures(p.getUuid()).size()==1&&p.getMainHandStack().getCount()==1&&encounter.entity().isRemoved());
  // A second owner cannot mount this session's boat, including through startRiding(force).
  p.getInventory().selectedSlot=2;p.setPitch(30);p.setYaw(0);m.engine.equipment.useBoat(p);
  boat=(BoatEntity)java.util.stream.StreamSupport.stream(p.getServerWorld().iterateEntities().spliterator(),false).filter(e->e.getCommandTags().contains(FishingEquipment.BOAT)).findFirst().orElseThrow();
  check("Reusable boat places a real invulnerable owned entity in water",boat.isAlive()&&boat.isInvulnerable()&&p.getMainHandStack().getCount()==1);
  check("Foreign rider cannot mount temporary boat even with force",!other.startRiding(boat,true));
  check("Owner can mount temporary boat",p.startRiding(boat));p.stopRiding();check("Owner can dismount without leaving fishing session",!p.hasVehicle()&&m.sessions.active(p));
  // Move into lake to test the custom bite event at the actual bobber coordinates.
  p.getInventory().selectedSlot=0;m.engine.rod(p);var line=m.engine.lines.get(p.getUuid());line.bobber.setPosition(2.5,64.2,2.5);m.engine.tick();line.biteAt=System.currentTimeMillis()-1;net.packets.clear();m.engine.tick();
  check("Bite sends spatial sound at bobber",net.packets.stream().anyMatch(packet->packet instanceof PlaySoundS2CPacket sound&&Math.abs(sound.getX()-2.5)<.2&&Math.abs(sound.getZ()-2.5)<.2));
  check("Bite sends splash and bubble packets at bobber",net.packets.stream().filter(packet->packet instanceof ParticleS2CPacket particle&&Math.abs(particle.getX()-2.5)<.2).count()==2);
  check("Bite no longer sends Fisgou chat message",net.packets.stream().noneMatch(packet->packet instanceof GameMessageS2CPacket message&&message.content().getString().contains("Fisgou")));
  net.packets.clear();m.engine.tick();check("Bite feedback is emitted once per hooked fish",net.packets.stream().noneMatch(packet->packet instanceof PlaySoundS2CPacket||packet instanceof ParticleS2CPacket));m.engine.closeLine(p.getUuid());
  // Exercise the actual engine path and action bar, with a deterministic pull boundary.
  m.engine.rod(p);var fight=m.engine.lines.get(p.getUuid());fight.bobber.setPosition(2.5,64.2,2.5);m.engine.tick();fight.biteAt=System.currentTimeMillis()-1;m.engine.tick();fight.lastClick=s.getTicks()-5;m.engine.rod(p);fight.progress=60;fight.phaseEnd=s.getTicks();fight.updatedTick=s.getTicks()-1;net.packets.clear();m.engine.tick();check("Actual fishing engine pulls progress backwards and warns once",fight.pulling&&fight.progress<60&&net.packets.stream().anyMatch(packet->packet instanceof PlaySoundS2CPacket));double retained=fight.progress;net.packets.clear();m.engine.tick();check("Repeated same-tick engine calls cannot multiply fish losses or warning sounds",fight.progress==retained&&net.packets.stream().noneMatch(packet->packet instanceof PlaySoundS2CPacket));check("Fight HUD has ten bars for tension and ten for progress",FishingEngineHud(fight).replace("I","").length()==FishingEngineHud(fight).length()-20);m.engine.closeLine(p.getUuid());
  // Verify vanilla tickFishingLogic really is intercepted, while unrelated vanilla rods remain normal.
  m.engine.rod(p);line=m.engine.lines.get(p.getUuid());var logic=Arrays.stream(net.minecraft.entity.projectile.FishingBobberEntity.class.getDeclaredMethods()).filter(method->Arrays.equals(method.getParameterTypes(),new Class[]{BlockPos.class})&&method.getReturnType()==void.class).toList();
  check("Fishing bobber is identified by entity identity and owner",m.engine.equipment.ownsLine(line.bobber));
  var method=logic.getFirst();method.setAccessible(true);var fields=Arrays.stream(net.minecraft.entity.projectile.FishingBobberEntity.class.getDeclaredFields()).filter(f->f.getType()==int.class&&!java.lang.reflect.Modifier.isStatic(f.getModifiers())).toList();var before=new ArrayList<Integer>();for(var field:fields){field.setAccessible(true);before.add(field.getInt(line.bobber));}method.invoke(line.bobber,new BlockPos(2,64,2));boolean unchanged=true;for(int i=0;i<fields.size();i++)unchanged&=fields.get(i).getInt(line.bobber)==before.get(i);check("Actual vanilla fishing logic is canceled for mod bobber",unchanged);
  var ordinary=new net.minecraft.entity.projectile.FishingBobberEntity(other,s.getOverworld(),0,0);before.clear();for(var field:fields)before.add(field.getInt(ordinary));method.invoke(ordinary,new BlockPos(2,64,2));boolean changed=false;for(int i=0;i<fields.size();i++)changed|=fields.get(i).getInt(ordinary)!=before.get(i);check("Ordinary Minecraft rod still runs vanilla fishing logic",changed);ordinary.discard();m.engine.closeLine(p.getUuid());
  ready=s.getTicks()+205;
 }
 static String FishingEngineHud(GameEngine.Line line){return GameEngine.fightText(line).getString();}
 public boolean tick(MinecraftServer s)throws Exception{
  if(stage==1){if(!funding.isDone())return false;check("Real Impactor funds cooldown shop test account",funding.join());m.finance.buy(p,"ball-upgrade","recarga");boolean locked=false;try{m.finance.buy(p,"ball-upgrade","recarga");}catch(IllegalStateException expected){locked=true;}check("Rapid duplicate cooldown purchases respect durable finance lock",locked);stage=2;return false;}
  if(stage==2){if(m.finance.busy(p.getUuid()))return false;check("Paid cooldown improvement persists and changes all fishing balls to nine seconds",m.data.profile(p.getUuid()).ballCooldownLevel==1&&m.engine.equipment.cooldownSeconds(p)==9);try(var database=new com.zorykai.fishing.core.FishingStore(s.getSavePath(WorldSavePath.ROOT).resolve("data/zorykai-fishing/fishing.sqlite"))){check("Cooldown buff survives independent database reload",new com.zorykai.fishing.core.GameData(database).profile(p.getUuid()).ballCooldownLevel==1);}return true;}
  if(s.getTicks()<ready)return false;
  var fill=net.packets.stream().filter(packet->packet instanceof GameMessageS2CPacket msg&&msg.overlay()&&msg.content().getString().startsWith("Recarga da bola")).map(packet->((GameMessageS2CPacket)packet).content().getSiblings().getFirst().getString().length()).toList();
  check("Action bar fills progressively from gray to twenty blue bars",fill.stream().anyMatch(n->n>0&&n<20)&&fill.contains(20));
  check("Authoritative hand slot is resent on later server ticks",net.packets.stream().anyMatch(packet->packet instanceof ScreenHandlerSlotUpdateS2CPacket slot&&slot.getSyncId()==-2&&slot.getSlot()==0));
  check("Cooldown expires automatically without switching hand or reopening inventory",!m.engine.equipment.cooling(p));
  p.getInventory().selectedSlot=1;UseItemCallback.EVENT.invoker().interact(p,p.getWorld(),Hand.MAIN_HAND);
  check("Throw works again after cooldown, exhausting virtual stock while icon stays",m.data.profile(p.getUuid()).balls.get("boa")==0&&p.getMainHandStack().getCount()==1);
  p.getInventory().selectedSlot=2;m.engine.equipment.useBoat(p);check("Reusing boat tool retrieves existing entity without item duplication",boat.isRemoved()&&p.getMainHandStack().getCount()==1);
  p.setPosition(2.5,65,-.5);p.setPitch(30);p.setYaw(0);m.engine.equipment.useBoat(p);boat=(BoatEntity)java.util.stream.StreamSupport.stream(p.getServerWorld().iterateEntities().spliterator(),false).filter(e->e.getCommandTags().contains(FishingEquipment.BOAT)).findFirst().orElseThrow();p.startRiding(boat);
  m.sessions.exit(p);
  check("Leaving while mounted removes temporary boat and restores original inventory",boat.isRemoved()&&!p.hasVehicle()&&!m.sessions.protectedPlayer(p)&&p.getInventory().getStack(4).isOf(Items.DIAMOND)&&p.getInventory().getStack(4).getCount()==11&&p.getInventory().getStack(2).isEmpty());
  check("Session exit clears capture recharge UI",!m.engine.equipment.cooling(p));
  m.sessions.enter(p);p.getInventory().selectedSlot=2;p.setPosition(2.5,65,-.5);p.setPitch(30);p.setYaw(0);var waterHit=(BlockHitResult)p.raycast(6,1,true);UseBlockCallback.EVENT.invoker().interact(p,p.getWorld(),Hand.MAIN_HAND,waterHit);UseItemCallback.EVENT.invoker().interact(p,p.getWorld(),Hand.MAIN_HAND);check("One water click carrying block-use and item-use packets places exactly one boat without recalling it",java.util.stream.StreamSupport.stream(p.getServerWorld().iterateEntities().spliterator(),false).filter(e->e.isAlive()&&e.getCommandTags().contains(FishingEquipment.BOAT)).count()==1);m.sessions.exit(p);
  FishingScreens.open(m,p,"recarga");check("Cooldown shop opens with upgrade and current/next timing",p.currentScreenHandler.getSlot(22).getStack().isOf(Items.CLOCK));p.closeHandledScreen();
  if(m.finance.available()){funding=FishingFinance.Backend.change(p.getUuid(),"impactor:dollars",new java.math.BigDecimal("10000"),true);stage=1;return false;}boolean refused=false;try{m.finance.buy(p,"ball-upgrade","recarga");}catch(IllegalStateException expected){refused=true;}check("Missing Impactor refuses paid cooldown buffs without granting upgrades",refused&&m.data.profile(p.getUuid()).ballCooldownLevel==0);return true;
 }
}
