package com.zorykai.fishing;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.Entity;
import com.cobblemon.mod.common.entity.npc.NPCEntity;
import static com.zorykai.fishing.GameConfig.*;
/** Read-only inspection of the audited Trainers registry; only trusted fishing NPCs interact. */
final class TrainerBridge {
 static boolean allowed(FishingMod mod,Entity entity){if(!bool(mod.rules.get("integrations"),"trainers",true)||!(entity instanceof NPCEntity)||!FabricLoader.getInstance().isModLoaded("shirokai_trainers"))return false;try{
  var explicit=mod.rules.get("npc").getAsJsonArray("allowedEntityUuids");if(explicit!=null&&explicit.asList().stream().anyMatch(x->x.getAsString().equals(entity.getUuidAsString())))return true;
  for(var container:FabricLoader.getInstance().getEntrypointContainers("main",net.fabricmc.api.ModInitializer.class))if(container.getProvider().getMetadata().getId().equals("shirokai_trainers")){
   var entry=container.getEntrypoint();var npcs=entry.getClass().getMethod("getNpcs").invoke(entry);var records=(java.util.Map<?,?>)npcs.getClass().getMethod("getRecords").invoke(npcs);
   for(var record:records.values()){var type=record.getClass();if(!entity.getUuid().equals(type.getMethod("getEntity").invoke(record))||Boolean.TRUE.equals(type.getMethod("getRemoved").invoke(record))||type.getMethod("getTrainer").invoke(record)!=null)continue;String id=(String)type.getMethod("getId").invoke(record);var ids=mod.rules.get("npc").getAsJsonArray("allowedNpcIds");if(ids!=null&&ids.asList().stream().anyMatch(x->x.getAsString().equals(id)))return true;var command=type.getMethod("getClickCommand").invoke(record);if(command!=null&&"console".equals(command.getClass().getMethod("getExecutor").invoke(command))&&((String)command.getClass().getMethod("getCommand").invoke(command)).startsWith("pesca abrir "))return true;
   }
  }
 }catch(Exception|LinkageError e){FishingMod.LOG.warn("NPC Fishing recusado: {}",e.toString());}return false;}
}
