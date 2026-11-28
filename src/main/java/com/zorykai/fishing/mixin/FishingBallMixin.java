package com.zorykai.fishing.mixin;
import com.zorykai.fishing.*;
import com.cobblemon.mod.common.entity.pokeball.EmptyPokeBallEntity;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.hit.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(EmptyPokeBallEntity.class)
public abstract class FishingBallMixin {
 @Inject(method="onEntityHit",at=@At("HEAD"),cancellable=true)
 private void fishing$hit(EntityHitResult hit,CallbackInfo ci){var self=(EmptyPokeBallEntity)(Object)this;if(!self.getCommandTags().contains("zf_projectile"))return;ci.cancel();if(self.getCaptureState()!=EmptyPokeBallEntity.CaptureState.NOT)return;var m=FishingMod.INSTANCE;if(m==null||m.engine==null||!m.engine.captures.start(self,hit.getEntity())){if(m!=null&&m.engine!=null)m.engine.captures.abort(self);else self.discard();}}
 @Inject(method="onBlockHit",at=@At("HEAD"),cancellable=true)
 private void fishing$miss(BlockHitResult hit,CallbackInfo ci){var self=(EmptyPokeBallEntity)(Object)this;if(!self.getCommandTags().contains("zf_projectile")||self.getCaptureState()!=EmptyPokeBallEntity.CaptureState.NOT)return;ci.cancel();var m=FishingMod.INSTANCE;if(m!=null&&m.engine!=null){m.engine.captures.abort(self);if(self.getOwner() instanceof ServerPlayerEntity p)m.engine.equipment.sync(p);}else self.discard();}
 @Inject(method="beginCapture",at=@At("HEAD"),cancellable=true,remap=false)
 private void fishing$nativeShakes(CallbackInfo ci){var self=(EmptyPokeBallEntity)(Object)this;if(!self.getCommandTags().contains("zf_projectile"))return;ci.cancel();var m=FishingMod.INSTANCE;if(m!=null&&m.engine!=null)m.engine.captures.begin(self);else self.discard();}
 @Inject(method="breakFree",at=@At("HEAD"),cancellable=true,remap=false)
 private void fishing$release(CallbackInfo ci){var self=(EmptyPokeBallEntity)(Object)this;if(!self.getCommandTags().contains("zf_projectile"))return;ci.cancel();var m=FishingMod.INSTANCE;if(m!=null&&m.engine!=null)m.engine.captures.release(self,true);else self.discard();}
 @Inject(method="shakeBall$lambda$0",at=@At("HEAD"),cancellable=true,remap=false)
 private static void fishing$storeOnly(PokemonEntity pokemon,EmptyPokeBallEntity ball,ServerPlayerEntity player,CallbackInfoReturnable<kotlin.Unit> ci){if(!ball.getCommandTags().contains("zf_projectile"))return;ci.setReturnValue(kotlin.Unit.INSTANCE);var m=FishingMod.INSTANCE;if(m!=null&&m.engine!=null)m.engine.captures.success(ball);else ball.discard();}
}
