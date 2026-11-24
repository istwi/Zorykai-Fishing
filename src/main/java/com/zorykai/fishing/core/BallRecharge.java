package com.zorykai.fishing.core;
/** Seconds, clamped both to configured purchase tiers and the safe minimum. */
public final class BallRecharge {
 private BallRecharge(){}
 public static double seconds(double base,double minimum,double perLevel,int maximumLevel,int level){return Math.max(minimum,base-Math.max(0,Math.min(maximumLevel,level))*perLevel);}
}
