package com.zorykai.fishing.mixin;
import com.zorykai.fishing.FishingEquipment;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(BoatEntity.class)
public abstract class FishingBoatMixin {
 @Inject(method="fall",at=@At("HEAD"),cancellable=true)
 private void fishing$noDrops(double height,boolean ground,BlockState state,BlockPos pos,CallbackInfo ci){if(((BoatEntity)(Object)this).getCommandTags().contains(FishingEquipment.BOAT))ci.cancel();}
}
