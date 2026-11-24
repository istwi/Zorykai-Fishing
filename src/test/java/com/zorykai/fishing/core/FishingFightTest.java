package com.zorykai.fishing.core;
import java.util.Random;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class FishingFightTest {
 final FishingFight.Rules r=new FishingFight.Rules(80,80,40,40,60,40,6,4,15,.75,.25,2);
 FishingFight.State state(){var s=new FishingFight.State();FishingFight.begin(s,0,r,new Random(1));return s;}
 @Test void restingBrieflyLowersTensionWithoutTakingProgress(){var s=state();FishingFight.reel(s,0,r,22,20);FishingFight.advance(s,40,r,new Random(2),12);assertEquals(15,s.progress,.001);assertEquals(18,s.tension,.001);}
 @Test void prolongedSlackLosesProgressAndNeverGoesNegative(){var s=state();s.progress=30;FishingFight.advance(s,70,r,new Random(1),12);assertEquals(28,s.progress,.001);assertTrue(s.slack);s.progress=.01;FishingFight.advance(s,71,r,new Random(1),12);assertEquals(0,s.progress);}
 @Test void pullingTakesGroundAndBadTimingCostsMoreTension(){var s=state();s.progress=60;assertTrue(FishingFight.advance(s,80,r,new Random(1),0));double before=s.progress;FishingFight.advance(s,100,r,new Random(1),0);assertEquals(before-6,s.progress,.001);double tension=s.tension,progress=s.progress;FishingFight.reel(s,100,r,22,20);assertEquals(tension+44,s.tension,.001);assertEquals(progress+5,s.progress,.001);}
 @Test void pullEndGivesTimeToRecoverWithoutSlackPenalty(){var s=state();s.progress=80;FishingFight.advance(s,120,r,new Random(1),12);assertFalse(s.pulling);double before=s.progress;FishingFight.advance(s,160,r,new Random(1),12);assertEquals(before,s.progress,.001);FishingFight.advance(s,161,r,new Random(1),12);assertTrue(s.progress<before);}
 @Test void repeatedTickCallsCannotSpeedUpDecayOrRepeatPullWarning(){var s=state();s.progress=70;assertTrue(FishingFight.advance(s,80,r,new Random(1),12));double progress=s.progress,tension=s.tension;for(int i=0;i<100;i++)assertFalse(FishingFight.advance(s,80,r,new Random(1),12));assertEquals(progress,s.progress);assertEquals(tension,s.tension);}
 @Test void successfulCalmReelingCanFinishWithoutCrossingBreakLimit(){var s=state();for(int t=0;t<=320&&s.progress<100;t++){FishingFight.advance(s,t,r,new Random(t),12);if(!s.pulling&&s.tension<=65)FishingFight.reel(s,t,r,22,20);assertTrue(s.tension<100);}assertEquals(100,s.progress);}
 @Test void randomizedDurationsStayWithinTheirConfiguredBounds(){var rules=new FishingFight.Rules(80,140,30,50,60,40,6,4,15,.75,.25,2);var random=new Random(123);var s=new FishingFight.State();FishingFight.begin(s,0,rules,random);for(int i=0;i<100;i++){int now=s.phaseEnd;FishingFight.advance(s,now,rules,random,0);int length=s.phaseEnd-now;assertTrue(length>=(s.pulling?30:80)&&length<=(s.pulling?50:140));}}
}
