package com.zorykai.fishing.mixin;
import com.zorykai.fishing.*;
import net.minecraft.server.command.*;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.*;
@Mixin(CommandManager.class)
public abstract class CommandProtectionMixin {
 @Inject(method="executeWithPrefix",at=@At("HEAD"),cancellable=true)
 private void fishing$command(ServerCommandSource source,String command,CallbackInfo ci){check(source,command,ci);}
 @Inject(method="execute",at=@At("HEAD"),cancellable=true)
 private void fishing$parsed(com.mojang.brigadier.ParseResults<ServerCommandSource> parsed,String command,CallbackInfo ci){check(parsed.getContext().getSource(),command,ci);}
 private void check(ServerCommandSource source,String command,CallbackInfo ci){var m=FishingMod.INSTANCE;if(m==null||m.sessions==null||!(source.getEntity() instanceof ServerPlayerEntity p)||!m.sessions.protectedPlayer(p))return;
  String root=command.replaceFirst("^/","").strip().split("\\s+",2)[0].toLowerCase(Locale.ROOT);var allowed=m.rules.get("commands").getAsJsonArray("allowed").asList().stream().map(x->x.getAsString()).toList();
  if(allowed.contains(root)||root.equals("pescaadmin")&&m.admin(p))return;
  var node=m.server.getCommandManager().getDispatcher().getRoot().getChild(root);for(int i=0;node!=null&&i<16;i++,node=node.getRedirect())if(allowed.contains(node.getName()))return;
  if(me.lucko.fabric.api.permissions.v0.Permissions.check(p,GameConfig.str(m.rules.get("commands"),"adminBypassPermission","zorykai_fishing.bypass"),false)){m.audit(p.getUuid(),"COMMAND_BYPASS",Map.of("command",command));return;}
  m.sessions.notice(p,"Comando bloqueado durante a pesca. Use /pesca sair.");ci.cancel();
 }
}
