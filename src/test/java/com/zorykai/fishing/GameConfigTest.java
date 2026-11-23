package com.zorykai.fishing;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import static org.junit.jupiter.api.Assertions.*;
class GameConfigTest {
 @TempDir Path root;
 @Test void editableRulesAreGeneratedAndPreservedWithBackup()throws Exception{var cfg=new GameConfig(root);assertTrue(cfg.files.size()>40);var p=root.resolve("prices.json");var json=cfg.get("prices").deepCopy();json.addProperty("base",123);FishingConfiguration.write(p,json);var reload=new GameConfig(root);assertEquals(123,GameConfig.num(reload.get("prices"),"base",0));try(var list=Files.list(root.resolve("backups"))){assertTrue(list.anyMatch(x->x.toString().endsWith(".zip")));}}
 @Test void invalidPricesOrNewerSchemaAreRejected()throws Exception{var cfg=new GameConfig(root);var j=cfg.get("prices").deepCopy();j.addProperty("base",-1);FishingConfiguration.write(root.resolve("prices.json"),j);assertThrows(IllegalArgumentException.class,()->new GameConfig(root));j.addProperty("base",123);j.addProperty("schemaVersion",999);FishingConfiguration.write(root.resolve("prices.json"),j);assertThrows(IllegalArgumentException.class,()->new GameConfig(root));}
 @Test void explicitSafeFallbackCanUseNegativeCoordinates()throws Exception{var cfg=new GameConfig(root);var j=cfg.get("worlds").deepCopy();j.add("recoverySpawn",com.google.gson.JsonParser.parseString("{\"dimension\":\"minecraft:overworld\",\"x\":-500,\"y\":64,\"z\":-300}"));FishingConfiguration.write(root.resolve("worlds.json"),j);assertDoesNotThrow(()->new GameConfig(root));}
}
