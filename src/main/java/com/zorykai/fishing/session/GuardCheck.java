package com.zorykai.fishing.session;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.fabricmc.loader.api.FabricLoader;
/** Read-only audited Guard API. Never invokes its raid-specific authorization/bypass. */
public final class GuardCheck {
 public static boolean safeLogin(ServerPlayerEntity p,ServerWorld world,BlockPos pos){if(!FabricLoader.getInstance().isModLoaded("shirokai_guard"))return true;try{boolean authorized=(boolean)Class.forName("dev.shirokai.guard.EntryAccess").getMethod("safeLogin",ServerPlayerEntity.class,ServerWorld.class,BlockPos.class).invoke(null,p,world,pos);
  var flag=Class.forName("dev.shirokai.guard.core.Flag");Object entry=java.lang.Enum.valueOf((Class)flag,"ENTRY");return authorized&&(boolean)Class.forName("dev.shirokai.guard.ShiroGuard").getMethod("allowed",net.minecraft.world.World.class,BlockPos.class,flag,ServerPlayerEntity.class).invoke(null,world,pos,entry,p);}catch(Exception|LinkageError e){return false;}}
}
