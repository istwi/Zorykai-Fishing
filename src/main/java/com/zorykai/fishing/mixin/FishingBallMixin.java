package com.zorykai.fishing.mixin;
import com.zorykai.fishing.FishingMod;
import com.cobblemon.mod.common.entity.pokeball.EmptyPokeBallEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(EmptyPokeBallEntity.class)
public abstract class FishingBallMixin {
 @Inject(method="onEntityHit",at=@At("HEAD"),cancellable=true)
 private void fishing$hit(EntityHitResult hit,CallbackInfo ci){var self=(EmptyPokeBallEntity)(Object)this;if(!self.getCommandTags().contains("zf_projectile"))return;ci.cancel();var m=FishingMod.INSTANCE;if(m!=null&&m.engine!=null){m.engine.hit(self,hit.getEntity());m.engine.projectiles.remove(self.getUuid());}self.discard();}
 @Inject(method="onBlockHit",at=@At("HEAD"),cancellable=true)
 private void fishing$miss(BlockHitResult hit,CallbackInfo ci){var self=(EmptyPokeBallEntity)(Object)this;if(!self.getCommandTags().contains("zf_projectile"))return;ci.cancel();var m=FishingMod.INSTANCE;if(m!=null&&m.engine!=null)m.engine.projectiles.remove(self.getUuid());self.discard();}
}
