package com.zorykai.fishing.core;
import java.util.random.RandomGenerator;
/** Tick-driven fight. Repeated callbacks within one tick never speed up decay or losses. */
public final class FishingFight {
 public record Rules(int calmMin,int calmMax,int pullMin,int pullMax,int idleGrace,int recoveryGrace,double pullLoss,double slackLoss,double slackThreshold,double calmGain,double pullGain,double pullTension){}
 public static class State {public double tension=20,progress;public boolean fighting,pulling,slack;public int updatedTick,phaseEnd,lastReelTick,graceUntil;}
 private static int duration(RandomGenerator random,int min,int max){return random.nextInt(min,max+1);}
 public static void begin(State s,int tick,Rules r,RandomGenerator random){if(s.fighting)return;s.fighting=true;s.updatedTick=tick;s.lastReelTick=tick;s.phaseEnd=tick+duration(random,r.calmMin,r.calmMax);}
 /** Returns true only on a transition into a pull, for one private warning. */
 public static boolean advance(State s,int tick,Rules r,RandomGenerator random,double decay){
  if(!s.fighting||tick<=s.updatedTick)return false;boolean pullStarted=false;
  for(int t=s.updatedTick+1;t<=tick;t++){
   if(t>=s.phaseEnd){s.pulling=!s.pulling;if(s.pulling)pullStarted=true;else s.graceUntil=t+r.recoveryGrace;s.phaseEnd=t+duration(random,s.pulling?r.pullMin:r.calmMin,s.pulling?r.pullMax:r.calmMax);}
   s.tension=Math.max(0,s.tension-decay/20d);
   s.slack=!s.pulling&&s.tension<=r.slackThreshold&&t-s.lastReelTick>r.idleGrace&&t>s.graceUntil;
   double loss=s.pulling?r.pullLoss:s.slack?r.slackLoss:0;s.progress=Math.max(0,s.progress-loss/20d);
  }
  s.updatedTick=tick;return pullStarted;
 }
 public static void reel(State s,int tick,Rules r,double tension,double gain){s.lastReelTick=tick;s.slack=false;s.tension=Math.min(100,s.tension+tension*(s.pulling?r.pullTension:1));s.progress=Math.min(100,s.progress+gain*(s.pulling?r.pullGain:r.calmGain));}
 private FishingFight(){}
}
