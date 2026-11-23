package com.zorykai.fishing.core;
public record MapDefinition(String id,String dimension,Bounds bounds,double entryX,double entryY,double entryZ){
 public void validate(){if(id==null||!id.matches("[a-z0-9_-]{1,48}")||dimension==null||!dimension.matches("[a-z0-9_.-]+:[a-z0-9_./-]+")||bounds==null||!bounds.contains(entryX,entryY,entryZ))throw new IllegalArgumentException("Mapa inválido: ID, dimensão, perímetro e entrada devem estar definidos");}
}
