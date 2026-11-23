package com.zorykai.fishing.core;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class AreaTest {
 MapDefinition map(){return new MapDefinition("mapa","minecraft:overworld",new Bounds(-100,0,-100,100,100,100),0,64,0);}
 Lake lake(Bounds bounds){return new Lake("lago","Lago","mapa","minecraft:overworld",bounds,1,null,Lake.starter());}
 @Test void cornersNormalizeAndNegativeFractionalPositionsAreCorrect(){var b=Bounds.between(2,4,6,-2,0,-6);assertTrue(b.contains(-1.9,0,-5.9));assertFalse(b.contains(-2.01,0,0));assertTrue(b.contains(2.999,4.9,6.9));assertFalse(b.contains(3,0,0));assertFalse(b.contains(Double.NaN,0,0));}
 @Test void lakesRequireGeneralPerimeterAndSameDimension(){lake(new Bounds(-10,1,-10,10,20,10)).validate(map(),List.of());assertThrows(IllegalArgumentException.class,()->lake(new Bounds(-101,0,0,0,10,10)).validate(map(),List.of()));var wrong=new Lake("lago","Lago","mapa","minecraft:the_nether",new Bounds(0,0,0,1,1,1),1,null,Lake.starter());assertThrows(IllegalArgumentException.class,()->wrong.validate(map(),List.of()));}
 @Test void lakeOverlapIsRejected(){var a=new Lake("outro","Outro","mapa","minecraft:overworld",new Bounds(0,0,0,10,10,10),1,null,Lake.starter());assertThrows(IllegalArgumentException.class,()->lake(new Bounds(5,0,5,15,10,15)).validate(map(),List.of(a)));}
 @Test void tableValidatesPercentAndDoesNotSelectZeroChance(){var rows=List.of(new Lake.Species("magikarp",100),new Lake.Species("feebas",0));var lake=new Lake("lago","Lago","mapa","minecraft:overworld",new Bounds(0,0,0,10,10,10),1,null,rows);lake.validate(map(),List.of());for(int i=0;i<1000;i++)assertEquals("magikarp",lake.draw(new Random(i)));var invalid=new Lake("lago","Lago","mapa","minecraft:overworld",lake.bounds(),1,null,List.of(new Lake.Species("magikarp",99)));assertThrows(IllegalArgumentException.class,()->invalid.validate(map(),List.of()));}
}
