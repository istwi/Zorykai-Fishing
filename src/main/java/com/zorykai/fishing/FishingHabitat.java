package com.zorykai.fishing;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
/** Applies to the whole selected 3D construction, not just its lake volumes. */
public final class FishingHabitat {
 public static boolean blocked(ServerWorld world,Entity entity){var m=FishingMod.INSTANCE;if(m==null||m.config==null||!(entity instanceof PokemonEntity))return false;var map=m.config.map;if(map==null||!map.dimension().equals(world.getRegistryKey().getValue().toString())||!map.bounds().contains(entity.getX(),entity.getY(),entity.getZ()))return false;var encounter=m.encounters.get(entity.getUuid());return encounter==null||encounter.entity()!=entity||!entity.getCommandTags().contains(FishingMod.TAG);}
 public static void loaded(FishingMod m,Entity e,ServerWorld world){if(blocked(world,e)&&e instanceof PokemonEntity mon){if(mon.getPokemon().getOwnerUUID()!=null)mon.getPokemon().recall();else e.discard();}}
 public static void sweep(FishingMod m){if(m.config.map==null)return;for(var world:m.server.getWorlds())if(world.getRegistryKey().getValue().toString().equals(m.config.map.dimension())){var b=m.config.map.bounds();for(var e:world.getEntitiesByClass(PokemonEntity.class,new net.minecraft.util.math.Box(b.minX(),b.minY(),b.minZ(),b.maxX()+1d,b.maxY()+1d,b.maxZ()+1d),mon->true))loaded(m,e,world);}}
}
