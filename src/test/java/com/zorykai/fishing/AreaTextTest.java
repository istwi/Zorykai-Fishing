package com.zorykai.fishing;
import com.zorykai.fishing.core.*;
import com.google.gson.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class AreaTextTest {
 @Test void legacyAndHexColorsAreStylesRatherThanVisibleCodes(){var text=AreaText.parse("&9Lago &#33AAFFAzul&r!");assertEquals("Lago Azul!",text.getString());assertEquals(0x5555ff,text.getSiblings().get(0).getStyle().getColor().getRgb());assertEquals(0x33aaff,text.getSiblings().get(1).getStyle().getColor().getRgb());assertNull(text.getSiblings().get(2).getStyle().getColor());}
 @Test void oldAreaFilesAndPartialDisplayBlocksKeepDefaults(){var lake=new Lake("lago","Lago","mapa","minecraft:overworld",new Bounds(0,60,0,5,70,5),1,null,Lake.starter());var json=FishingConfiguration.JSON.toJsonTree(lake).getAsJsonObject();json.remove("display");var old=FishingConfiguration.JSON.fromJson(json,Lake.class);assertEquals("&b{area}{status}",old.display().subtitle);json.add("display",JsonParser.parseString("{\"subtitle\":\"&9Meu lago\",\"stayTicks\":60}"));var partial=FishingConfiguration.JSON.fromJson(json,Lake.class);assertEquals("",partial.display().title);assertEquals(60,partial.display().stayTicks);assertTrue(partial.display().sound);assertEquals(lake.pokemon(),partial.pokemon());}
 @Test void invalidDisplayTimingIsRejected(){var display=new Lake.Display();display.stayTicks=-1;assertThrows(IllegalArgumentException.class,display::validate);}
}
