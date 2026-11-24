package com.zorykai.fishing;
import com.zorykai.fishing.core.*;
import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.pokeball.ThrownPokeballHitEvent;
import com.cobblemon.mod.common.entity.pokeball.EmptyPokeBallEntity;
import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.mojang.authlib.GameProfile;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.PlayerManager;
import net.minecraft.network.packet.c2s.common.SyncedClientOptions;
import net.minecraft.item.*;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import java.util.*;
import java.nio.file.*;
import java.lang.reflect.*;

public final class FishingProofChecks implements ModInitializer {
 int ticks,nativeCaptures;boolean done;List<Map<String,Object>> checks=new ArrayList<>();
 void check(String name,boolean ok){checks.add(Map.of("name",name,"passed",ok));if(!ok)throw new AssertionError(name);}
 ServerPlayerEntity player(MinecraftServer s,String name)throws Exception{return player(s,name,UUID.randomUUID());}
 ServerPlayerEntity player(MinecraftServer s,String name,UUID id)throws Exception{var p=new ServerPlayerEntity(s,s.getOverworld(),new GameProfile(id,name),SyncedClientOptions.createDefault());p.networkHandler=FakePlayer.get(s.getOverworld(),new GameProfile(UUID.randomUUID(),"FishingConnection")).networkHandler;p.networkHandler.player=p;for(var f:PlayerManager.class.getDeclaredFields())if(f.getGenericType() instanceof ParameterizedType t&&t.getRawType()==Map.class&&Arrays.equals(t.getActualTypeArguments(),new Type[]{UUID.class,ServerPlayerEntity.class})){f.setAccessible(true);((Map<UUID,ServerPlayerEntity>)f.get(s.getPlayerManager())).put(p.getUuid(),p);break;}s.getPlayerManager().getPlayerList().add(p);return p;}
 Set<UUID> ids(Iterable<com.cobblemon.mod.common.pokemon.Pokemon> store){var ids=new HashSet<UUID>();for(var p:store)ids.add(p.getUuid());return ids;}
 FishingBetaChecks beta;FishingInteractionChecks interactions;
 public void onInitialize(){String restart=System.getProperty("zorykai.fishing.testRestart");if(restart!=null){FishingRestartChecks.register(this,restart);return;}CobblemonEvents.POKEMON_CAPTURED.subscribe(e->nativeCaptures++);ServerTickEvents.END_SERVER_TICK.register(s->{if(done)return;try{if(++ticks==30){run(s);beta=new FishingBetaChecks(this);beta.start(s);}else if(interactions!=null){if(interactions.tick(s))finish(s);}else if(beta!=null&&beta.tick(s)){interactions=new FishingInteractionChecks(this);interactions.start(s);}}catch(Throwable e){e.printStackTrace();checks.add(Map.of("name","fatal","passed",false,"error",e.toString()));finish(s);}});}
 void finish(MinecraftServer s){done=true;try{Files.writeString(Path.of("fishing-proof-results.json"),FishingConfiguration.JSON.toJson(checks));}catch(Exception e){throw new RuntimeException(e);}s.stop(false);}

 void run(MinecraftServer s)throws Exception{
  var mod=FishingMod.INSTANCE;check("Fresh server remains unconfigured and creates no fishing dimension",mod.config.map==null&&s.getWorldRegistryKeys().stream().noneMatch(k->k.getValue().toString().contains("fishing")));
  var p=player(s,"FishingOwner");var other=player(s,"FishingOther");p.setPosition(.5,64,.5);other.setPosition(.5,64,.5);
  p.getInventory().setStack(0,new ItemStack(Items.DIAMOND,7));var original=p.getInventory().writeNbt(new net.minecraft.nbt.NbtList()).copy();
  s.getCommandManager().executeWithPrefix(p.getCommandSource(),"pesca entrar");check("Entering an unconfigured map does not modify inventory",original.equals(p.getInventory().writeNbt(new net.minecraft.nbt.NbtList())));
  var parent=new MapDefinition("mapa","minecraft:overworld",new Bounds(-10,50,-10,10,80,10),.5,64,.5);FishingConfiguration.write(mod.config.root.resolve("mapa.json"),parent);
  var lake=new Lake("lago_inicial","Lago inicial","mapa","minecraft:overworld",new Bounds(-5,60,-5,5,70,5),1,null,List.of(new Lake.Species("magikarp",100)));
  FishingConfiguration.write(mod.config.root.resolve("areas/lago_inicial.json"),lake);mod.config.reload();mod.validateSpecies(mod.config);
  check("General perimeter and internal lake reload in order",mod.config.map.bounds().encloses(mod.config.lakes.get(lake.id()).bounds()));
  var beforeFile=Files.readString(mod.config.root.resolve("areas/lago_inicial.json"));mod.config.reload();check("Reload preserves customized lake definition",beforeFile.equals(Files.readString(mod.config.root.resolve("areas/lago_inicial.json"))));
  var party=Cobblemon.INSTANCE.getStorage().getParty(p);var pc=Cobblemon.INSTANCE.getStorage().getPC(p);
  party.add(PokemonProperties.Companion.parse("pikachu"," ","=").create());pc.add(PokemonProperties.Companion.parse("eevee"," ","=").create());var partyBefore=ids(party);var pcBefore=ids(pc);
  var water=new BlockPos(2,64,2);s.getOverworld().setBlockState(water,Blocks.WATER.getDefaultState());var encounter=mod.spawnProof(p,s.getOverworld(),water,lake);
  check("Real Cobblemon entity appears in registered water",encounter.entity().isAlive()&&s.getOverworld().getEntity(encounter.entity().getUuid())==encounter.entity());
  check("Native capture is disabled before the entity enters the world",encounter.entity().isUncatchable());
  var hit=new ThrownPokeballHitEvent(new EmptyPokeBallEntity(s.getOverworld()),encounter.entity());CobblemonEvents.THROWN_POKEBALL_HIT.emit(hit);check("Native thrown-ball hit event is canceled for protected fishing entity",hit.isCanceled());
  var seen=new com.cobblemon.mod.common.api.events.pokemon.PokemonSeenEvent(p.getUuid(),encounter.entity().getPokemon());CobblemonEvents.POKEMON_SEEN.emit(seen);check("Fishing Pokemon seen event does not advance official dex",seen.isCanceled());
  var ordinarySeen=new com.cobblemon.mod.common.api.events.pokemon.PokemonSeenEvent(p.getUuid(),PokemonProperties.Companion.parse("pikachu"," ","=").create());CobblemonEvents.POKEMON_SEEN.emit(ordinarySeen);check("Ordinary Pokemon seen event is unaffected",!ordinarySeen.isCanceled());
  check("Another player cannot capture the encounter",!mod.capture(other,encounter.entity().getUuid()));check("Foreign capture writes nothing",mod.store.captures(other.getUuid()).isEmpty());
  check("Owner capture commits exclusive storage",mod.capture(p,encounter.entity().getUuid()));check("Repeated capture is idempotent",!mod.capture(p,encounter.entity().getUuid())&&mod.store.captures(p.getUuid()).size()==1);
  check("Captured display entity is removed after successful storage",encounter.entity().isRemoved());
  check("Party including pre-existing Pokemon remains unchanged",partyBefore.equals(ids(party)));check("PC including pre-existing Pokemon remains unchanged",pcBefore.equals(ids(pc)));check("No native PokemonCapturedEvent was emitted",nativeCaptures==0);
  check("Player items are preserved",original.equals(p.getInventory().writeNbt(new net.minecraft.nbt.NbtList())));
  FishingScreens.open(mod,p,"armazem");check("Storage menu opens with actual capture data",p.currentScreenHandler!=p.playerScreenHandler);var menu=p.currentScreenHandler;menu.onSlotClick(0,0,net.minecraft.screen.slot.SlotActionType.QUICK_MOVE,p);check("Storage UI cannot move Pokemon records as items",original.equals(p.getInventory().writeNbt(new net.minecraft.nbt.NbtList())));p.closeHandledScreen();
  mod.config.settings.storageCapacity=1;var second=mod.spawnProof(p,s.getOverworld(),water,lake);check("Full storage refuses capture without deleting encounter",!mod.capture(p,second.entity().getUuid())&&second.entity().isAlive());mod.expire(second);check("Explicit encounter expiry removes entity without extra capture",second.entity().isRemoved()&&mod.store.captures(p.getUuid()).size()==1);
  var file=s.getSavePath(net.minecraft.util.WorldSavePath.ROOT).resolve("data/zorykai-fishing/fishing.sqlite");try(var secondConnection=new FishingStore(file)){check("Capture persists through an independent SQLite connection",secondConnection.captures(p.getUuid()).size()==1);}
  if(net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("multiworld")){check("Exact supplied Multiworld 1.13.1 loaded",net.fabricmc.loader.api.FabricLoader.getInstance().getModContainer("multiworld").orElseThrow().getMetadata().getVersion().getFriendlyString().equals("1.13.1"));s.getCommandManager().executeWithPrefix(s.getCommandSource(),"mw list");}
  if(net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("zorykaimenus")){
   check("Real Zorykai menu API contains registered Fishing storage action",com.zorykai.menus.api.ZorykaiMenusAPI.actionRegistered("zorykai_fishing:armazem"));
   check("External Fishing menu validates at startup independent of SERVER_STARTED order",com.zorykai.menus.api.ZorykaiMenusAPI.menuExists("fishing_main"));
  }
 }
}
