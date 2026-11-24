package com.zorykai.fishing.mixin;
import com.zorykai.fishing.FishingMod;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(Entity.class)
public abstract class FishingPassengerMixin {
 @Inject(method="startRiding(Lnet/minecraft/entity/Entity;Z)Z",at=@At("HEAD"),cancellable=true)
 private void fishing$ownerOnly(Entity vehicle,boolean force,CallbackInfoReturnable<Boolean> ci){var m=FishingMod.INSTANCE;if(m!=null&&m.engine!=null&&!m.engine.equipment.mayRide((Entity)(Object)this,vehicle))ci.setReturnValue(false);}
}
