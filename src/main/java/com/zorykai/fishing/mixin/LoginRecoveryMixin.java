package com.zorykai.fishing.mixin;
import com.zorykai.fishing.FishingMod;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.Optional;
@Mixin(PlayerManager.class)
public abstract class LoginRecoveryMixin {
 @Inject(method="loadPlayerData",at=@At("RETURN"),cancellable=true)
 private void fishing$restoreBeforeJoin(ServerPlayerEntity player,CallbackInfoReturnable<Optional<NbtCompound>> cir){var m=FishingMod.INSTANCE;if(m!=null&&m.sessions!=null)cir.setReturnValue(m.sessions.beforeLogin(player,cir.getReturnValue()));}
}
