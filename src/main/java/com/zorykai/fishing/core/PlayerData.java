package com.zorykai.fishing.core;
import java.util.*;
public final class PlayerData {
 public String name="";public double largest;public long xp;public int prestige;public int ballCooldownLevel;public String selectedRod="inicial",selectedBait="comum",selectedBall="basica";
 public Map<String,Rod> rods=new LinkedHashMap<>();public Map<String,Integer> baits=new HashMap<>(),balls=new HashMap<>();
 public long cycleStart,remainingSeconds;public List<Credit> credits=new ArrayList<>();public Set<String> unlocks=new HashSet<>(),blockedAreas=new HashSet<>(),completedQuests=new HashSet<>(),achievements=new HashSet<>();
 public Map<String,Quest> quests=new HashMap<>();public Map<String,Dex> dex=new HashMap<>();public long captures,sales,speciesFound,shinyCaptures,tournamentWins;public int capacity=30;public Map<String,Long> counters=new HashMap<>();
 public static class Rod {public long xp;public Map<String,Integer> upgrades=new HashMap<>();}
 public static class Credit {public long seconds,expires;public Credit(){}public Credit(long s,long e){seconds=s;expires=e;}}
 public static class Quest {public long progress;public String cycle="";public boolean claimed;}
 public static class Dex {public long caught,seen,shiny;public double smallest=Double.MAX_VALUE,largest;}
 public PlayerData(){rods.put("inicial",new Rod());}
}
