package com.zorykai.fishing.core;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class StoreTest {
 @TempDir Path root;
 @Test void exclusiveOwnerAndIdempotencySurviveReopening()throws Exception{var owner=UUID.randomUUID();var id=UUID.randomUUID();var file=root.resolve("fishing.sqlite");try(var s=new FishingStore(file)){s.encounter(id,owner,UUID.randomUUID(),"magikarp","lago",2,false,10000);assertFalse(s.capture(id,UUID.randomUUID(),30,100));assertTrue(s.capture(id,owner,30,100));assertFalse(s.capture(id,owner,30,101));assertEquals(1,s.captures(owner).size());}try(var s=new FishingStore(file)){assertFalse(s.capture(id,owner,30,102));assertEquals(1,s.captures(owner).size());}}
 @Test void fullStorageAndExpiredEncountersDoNotCreateCaptures()throws Exception{try(var s=new FishingStore(root.resolve("f.sqlite"))){var owner=UUID.randomUUID();var a=UUID.randomUUID();s.encounter(a,owner,UUID.randomUUID(),"magikarp","lago",1,false,1000);assertTrue(s.capture(a,owner,1,100));var b=UUID.randomUUID();s.encounter(b,owner,UUID.randomUUID(),"magikarp","lago",1,false,1000);assertFalse(s.capture(b,owner,1,101));assertFalse(s.capture(b,owner,30,1000));assertEquals(1,s.captures(owner).size());}}
 @Test void startupExpiresUnfinishedEncountersButKeepsCompletedOnes()throws Exception{try(var s=new FishingStore(root.resolve("f.sqlite"))){var owner=UUID.randomUUID();var id=UUID.randomUUID();s.encounter(id,owner,UUID.randomUUID(),"magikarp","lago",1,false,1000);assertEquals(1,s.expireAll());assertFalse(s.capture(id,owner,30,100));}}
}
