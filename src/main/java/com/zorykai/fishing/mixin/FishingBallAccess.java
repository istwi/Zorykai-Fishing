package com.zorykai.fishing.mixin;
import com.cobblemon.mod.common.entity.pokeball.EmptyPokeBallEntity;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.api.scheduling.ScheduledTask;
import com.cobblemon.mod.common.api.pokeball.catching.CaptureContext;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.*;
@Mixin(EmptyPokeBallEntity.class)
public interface FishingBallAccess {
 @Invoker(value="attemptCatch",remap=false) void fishing$attempt(PokemonEntity pokemon);
 @Invoker(value="shakeBall",remap=false) void fishing$shake(ScheduledTask task,int remaining,CaptureContext context);
 @Accessor(value="HIT_TARGET_POSITION",remap=false) static TrackedData<Vec3d> fishing$target(){throw new AssertionError();}
 @Accessor(value="HIT_VELOCITY",remap=false) static TrackedData<Vec3d> fishing$velocity(){throw new AssertionError();}
}
