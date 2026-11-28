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
 @Inject(method="removeIfInvalid",at=@At("HEAD"),cancellable=true)
 private void fishing$rodSkin(net.minecraft.entity.player.PlayerEntity owner,org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<Boolean> ci){var m=FishingMod.INSTANCE;var self=(FishingBobberEntity)(Object)this;if(m==null||m.engine==null||!m.engine.equipment.ownsLine(self)||!(owner instanceof net.minecraft.server.network.ServerPlayerEntity p))return;boolean held=m.engine.gearType(p,p.getMainHandStack()).equals("rod")||m.engine.gearType(p,p.getOffHandStack()).equals("rod");boolean invalid=!m.sessions.active(p)||owner.isRemoved()||!owner.isAlive()||!held||self.squaredDistanceTo(owner)>1024;if(invalid)self.discard();ci.setReturnValue(invalid);}

}
