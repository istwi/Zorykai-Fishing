package com.zorykai.fishing.mixin;
import com.zorykai.fishing.FishingHabitat;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(ServerWorld.class)
public abstract class FishingSpawnMixin {
 @Inject(method="addEntity",at=@At("HEAD"),cancellable=true)
 private void fishing$onlyEncounters(Entity entity,CallbackInfoReturnable<Boolean> ci){if(FishingHabitat.blocked((ServerWorld)(Object)this,entity))ci.setReturnValue(false);}
}
