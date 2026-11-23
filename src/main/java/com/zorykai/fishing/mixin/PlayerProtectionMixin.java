package com.zorykai.fishing.mixin;
import com.zorykai.fishing.FishingMod;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.world.TeleportTarget;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import java.util.Set;
@Mixin(ServerPlayerEntity.class)
public abstract class PlayerProtectionMixin {
 private ServerPlayerEntity fishing$self(){return (ServerPlayerEntity)(Object)this;}
 @Inject(method="teleport(Lnet/minecraft/server/world/ServerWorld;DDDLjava/util/Set;FF)Z",at=@At("HEAD"),cancellable=true)
 private void fishing$teleport(ServerWorld w,double x,double y,double z,Set<PositionFlag> flags,float yaw,float pitch,CallbackInfoReturnable<Boolean> ci){var m=FishingMod.INSTANCE;if(m!=null&&m.sessions!=null&&!m.sessions.permitMove(fishing$self(),w,x,y,z))ci.setReturnValue(false);}
 @Inject(method="teleportTo",at=@At("HEAD"),cancellable=true)
 private void fishing$portal(TeleportTarget target,CallbackInfoReturnable<Entity> ci){var m=FishingMod.INSTANCE;if(m!=null&&m.sessions!=null&&!m.sessions.permitMove(fishing$self(),target.world(),target.pos().x,target.pos().y,target.pos().z))ci.setReturnValue(null);}
 @Inject(method="requestTeleport(DDD)V",at=@At("HEAD"),cancellable=true)
 private void fishing$request(double x,double y,double z,CallbackInfo ci){var m=FishingMod.INSTANCE;if(m!=null&&m.sessions!=null&&!m.sessions.permitMove(fishing$self(),fishing$self().getServerWorld(),x,y,z))ci.cancel();}
 @Inject(method="dropItem(Lnet/minecraft/item/ItemStack;ZZ)Lnet/minecraft/entity/ItemEntity;",at=@At("HEAD"),cancellable=true)
 private void fishing$drop(ItemStack item,boolean spread,boolean retain,CallbackInfoReturnable<ItemEntity> ci){var m=FishingMod.INSTANCE;if(m!=null&&m.sessions!=null&&m.sessions.protectedPlayer(fishing$self()))ci.setReturnValue(null);}
}
