package com.zorykai.fishing.mixin;
import com.zorykai.fishing.*;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.network.packet.c2s.play.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(ServerPlayNetworkHandler.class)
public abstract class NetworkProtectionMixin {
 @Shadow public ServerPlayerEntity player;
 @Inject(method="onClickSlot",at=@At("HEAD"),cancellable=true)
 private void fishing$click(ClickSlotC2SPacket packet,CallbackInfo ci){var m=FishingMod.INSTANCE;if(m==null||m.sessions==null||!m.sessions.protectedPlayer(player))return;if(!m.sessions.active(player)||player.currentScreenHandler!=player.playerScreenHandler&&!FishingScreens.isFishing(player.currentScreenHandler)||player.currentScreenHandler==player.playerScreenHandler){player.closeHandledScreen();ci.cancel();}}
 @Inject(method="onPlayerAction",at=@At("HEAD"),cancellable=true)
 private void fishing$action(PlayerActionC2SPacket packet,CallbackInfo ci){var m=FishingMod.INSTANCE;if(m!=null&&m.sessions!=null&&m.sessions.protectedPlayer(player)){ci.cancel();m.engine.equipment.sync(player);}}
 @Inject(method="onPlayerMove",at=@At("HEAD"),cancellable=true)
 private void fishing$freeze(PlayerMoveC2SPacket packet,CallbackInfo ci){var m=FishingMod.INSTANCE;if(m!=null&&m.sessions!=null&&m.sessions.protectedPlayer(player)&&!m.sessions.active(player)){ci.cancel();if(player.getServer().getTicks()%5==0)player.networkHandler.requestTeleport(player.getX(),player.getY(),player.getZ(),player.getYaw(),player.getPitch());}}
 @Inject(method="onCreativeInventoryAction",at=@At("HEAD"),cancellable=true)
 private void fishing$creative(CreativeInventoryActionC2SPacket packet,CallbackInfo ci){var m=FishingMod.INSTANCE;if(m!=null&&m.sessions!=null&&m.sessions.protectedPlayer(player))ci.cancel();}
}
