package com.zorykai.fishing.mixin;
import com.zorykai.fishing.FishingMod;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(FishingBobberEntity.class)
public abstract class FishingBobberMixin {
 @Inject(method="tickFishingLogic",at=@At("HEAD"),cancellable=true)
 private void fishing$customBite(BlockPos pos,CallbackInfo ci){var m=FishingMod.INSTANCE;if(m!=null&&m.engine!=null&&m.engine.equipment.ownsLine((FishingBobberEntity)(Object)this))ci.cancel();}
}
