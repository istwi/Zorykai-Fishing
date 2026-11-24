package com.zorykai.fishing.core;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class BallRechargeTest {
 @Test void startsAtTenAndImprovesOneSecondPerPurchasedLevel(){for(int level=0;level<=5;level++)assertEquals(10-level,BallRecharge.seconds(10,2,1,5,level));}
 @Test void neverExceedsPurchasedTierCapOrFallsBelowMinimum(){assertEquals(5,BallRecharge.seconds(10,2,1,5,999));assertEquals(2,BallRecharge.seconds(10,2,4,5,5));assertEquals(10,BallRecharge.seconds(10,2,1,5,-1));}
}
