package com.zorykai.fishing.core;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class ProgressPersistenceTest {
 @TempDir Path root;
 @Test void nestedCaptureAndProgressCommitOrRollbackTogether()throws Exception{try(var store=new FishingStore(root.resolve("db.sqlite"))){var data=new GameData(store);var owner=UUID.randomUUID();var e=UUID.randomUUID();store.encounter(e,owner,UUID.randomUUID(),"magikarp","lake",1,false,10000);assertThrows(Exception.class,()->store.transaction(c->{assertTrue(store.capture(e,owner,30,1));var d=data.profile(c,owner);d.captures++;data.save(c,owner,d);throw new Exception("simulated failure before commit");}));assertTrue(store.captures(owner).isEmpty());assertEquals(0,data.profile(owner).captures);assertTrue(store.capture(e,owner,30,2));}}
 @Test void saleReservationOccupiesCapacityButCompletedSaleFreesIt()throws Exception{try(var store=new FishingStore(root.resolve("db.sqlite"))){var owner=UUID.randomUUID();var a=UUID.randomUUID();store.encounter(a,owner,UUID.randomUUID(),"magikarp","lake",1,false,10000);assertTrue(store.capture(a,owner,1,1));store.transaction(c->{try(var p=c.prepareStatement("INSERT INTO capture_details VALUES(?,'{}','SALE_PENDING',0)")){p.setString(1,a.toString());p.executeUpdate();}return null;});var b=UUID.randomUUID();store.encounter(b,owner,UUID.randomUUID(),"magikarp","lake",1,false,10000);assertFalse(store.capture(b,owner,1,2));store.transaction(c->{try(var q=c.createStatement()){q.execute("UPDATE capture_details SET state='SOLD'");}return null;});assertTrue(store.capture(b,owner,1,3));}}
 @Test void perRodProgressAndPermanentUnlocksSurviveReopen()throws Exception{var owner=UUID.randomUUID();var file=root.resolve("db.sqlite");try(var store=new FishingStore(file)){var data=new GameData(store);data.update(owner,d->{d.rods.get("inicial").xp=500;d.unlocks.add("ocean");d.counters.put("reward:quest",1L);return null;});}try(var store=new FishingStore(file)){var d=new GameData(store).profile(owner);assertEquals(500,d.rods.get("inicial").xp);assertTrue(d.unlocks.contains("ocean"));assertEquals(1L,d.counters.get("reward:quest"));}}
}
