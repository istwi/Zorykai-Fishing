package com.zorykai.fishing.mixin;
import com.zorykai.fishing.FishingMod;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
/** Scoped recovery holding state only. Fishing freezes interactions/movement; no entry authorization is issued. */
@Pseudo @Mixin(targets="dev.shirokai.guard.EntrySafety",remap=false)
public abstract class GuardRecoveryMixin {
 @Inject(method="enforce",at=@At("HEAD"),cancellable=true,remap=false)
 private static void fishing$recoveryBeforeSafety(ServerPlayerEntity p,CallbackInfoReturnable<Boolean> ci){var m=FishingMod.INSTANCE;if(m!=null&&m.sessions!=null&&m.sessions.protectedPlayer(p)&&!m.sessions.active(p))ci.setReturnValue(true);}
}
