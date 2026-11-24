package com.zorykai.fishing.mixin;
import com.zorykai.fishing.FishingEquipment;
import net.minecraft.entity.vehicle.VehicleEntity;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(VehicleEntity.class)
public abstract class FishingVehicleDropsMixin {
 @Inject(method="killAndDropItem",at=@At("HEAD"),cancellable=true)
 private void fishing$noTemporaryBoatDrops(Item item,CallbackInfo ci){var self=(VehicleEntity)(Object)this;if(self.getCommandTags().contains(FishingEquipment.BOAT)){self.discard();ci.cancel();}}
}
