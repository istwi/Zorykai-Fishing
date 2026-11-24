package com.zorykai.fishing;
import com.zorykai.menus.api.ZorykaiMenusAPI;
import net.minecraft.server.network.ServerPlayerEntity;
/** Calls only the audited public API; never edits another mod's configuration. */
public final class MenuBridge {
 public static void register(FishingMod mod){
  for(String key:java.util.List.of("level","xp","time","captures","capacity","prestige"))ZorykaiMenusAPI.registerPlaceholder("zorykai_fishing:"+key,(player,argument)->{try{var d=mod.data.profile(player.getUuid());return net.minecraft.text.Text.literal(switch(key){case "level"->Integer.toString(mod.rules.level(d.xp));case "xp"->Long.toString(d.xp);case "time"->Long.toString(mod.sessions.balance(d));case "captures"->Long.toString(d.captures);case "capacity"->Integer.toString(d.capacity);default->Integer.toString(d.prestige);});}catch(Exception e){mod.fail(e);return net.minecraft.text.Text.literal("indisponível");}});
  ZorykaiMenusAPI.registerCondition("zorykai_fishing:active",(player,json)->mod.sessions!=null&&mod.sessions.active(player));
  ZorykaiMenusAPI.registerAction("zorykai_fishing:entrar","zorykai_fishing.use",context->{try{mod.sessions.enter(context.player());}catch(Exception e){FishingMod.tell(context.player(),e.getMessage());}});
  ZorykaiMenusAPI.registerAction("zorykai_fishing:sair","zorykai_fishing.use",context->{try{mod.sessions.exit(context.player());}catch(Exception e){mod.fail(e);}});
  for(String page:java.util.List.of("armazem","areas","perfil","varas","iscas","bolas","melhorias","missoes","tempo","conquistas","torneios","ranking","dex","museu","prestigio","tesouros","pendencia","recordes","premios","eventos","recarga"))ZorykaiMenusAPI.registerAction("zorykai_fishing:"+page,"zorykai_fishing.use",context->FishingScreens.open(mod,context.player(),page));
 }
 public static boolean openConfigured(FishingMod mod,ServerPlayerEntity p){
  if(!ZorykaiMenusAPI.ready()||!ZorykaiMenusAPI.menuExists(mod.config.settings.menuId))return false;
  // A configured menu's access denial must not open an alternative UI.
  if(!ZorykaiMenusAPI.openMenu(p,mod.config.settings.menuId))FishingMod.tell(p,"Menu externo solicitado; se não abrir, confira suas permissões.");
  return true;
 }
}
