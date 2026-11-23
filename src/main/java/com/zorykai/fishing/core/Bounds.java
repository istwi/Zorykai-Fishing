package com.zorykai.fishing.core;
public record Bounds(int minX,int minY,int minZ,int maxX,int maxY,int maxZ){
 public Bounds {if(minX>maxX||minY>maxY||minZ>maxZ||Math.abs((long)minX)>29999984||Math.abs((long)maxX)>29999984||Math.abs((long)minZ)>29999984||Math.abs((long)maxZ)>29999984||minY< -2048||maxY>2048)throw new IllegalArgumentException("Coordenadas inválidas");}
 public static Bounds between(int x,int y,int z,int a,int b,int c){return new Bounds(Math.min(x,a),Math.min(y,b),Math.min(z,c),Math.max(x,a),Math.max(y,b),Math.max(z,c));}
 public boolean contains(double x,double y,double z){return Double.isFinite(x)&&Double.isFinite(y)&&Double.isFinite(z)&&x>=minX&&x<maxX+1d&&y>=minY&&y<maxY+1d&&z>=minZ&&z<maxZ+1d;}
 public boolean encloses(Bounds b){return minX<=b.minX&&minY<=b.minY&&minZ<=b.minZ&&maxX>=b.maxX&&maxY>=b.maxY&&maxZ>=b.maxZ;}
 public boolean overlaps(Bounds b){return minX<=b.maxX&&maxX>=b.minX&&minY<=b.maxY&&maxY>=b.minY&&minZ<=b.maxZ&&maxZ>=b.minZ;}
}
