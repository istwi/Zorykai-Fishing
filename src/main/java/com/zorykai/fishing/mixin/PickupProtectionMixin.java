package com.zorykai.fishing.mixin;
import com.zorykai.fishing.FishingMod;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(ItemEntity.class)
public abstract class PickupProtectionMixin {
 @Inject(method="onPlayerCollision",at=@At("HEAD"),cancellable=true)
 private void fishing$pickup(PlayerEntity p,CallbackInfo ci){var m=FishingMod.INSTANCE;if(p instanceof ServerPlayerEntity sp&&m!=null&&m.sessions!=null&&m.sessions.protectedPlayer(sp))ci.cancel();}
}
