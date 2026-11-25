package com.zorykai.fishing;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
/** Local JSON text only: legacy & colors, decorations and &#RRGGBB. No command execution. */
public final class AreaText {
 public static Text parse(String input){var out=Text.empty();var part=new StringBuilder();Style style=Style.EMPTY;for(int i=0;i<input.length();i++){char c=input.charAt(i);if((c=='&'||c=='§')&&i+1<input.length()){
  if(input.charAt(i+1)=='#'&&i+7<input.length()){try{int rgb=Integer.parseInt(input.substring(i+2,i+8),16);if(!part.isEmpty()){out.append(Text.literal(part.toString()).setStyle(style));part.setLength(0);}style=Style.EMPTY.withColor(TextColor.fromRgb(rgb));i+=7;continue;}catch(NumberFormatException ignored){}}
  var format=Formatting.byCode(Character.toLowerCase(input.charAt(i+1)));if(format!=null){if(!part.isEmpty()){out.append(Text.literal(part.toString()).setStyle(style));part.setLength(0);}style=format==Formatting.RESET?Style.EMPTY:format.isColor()?Style.EMPTY.withColor(format):style.withFormatting(format);i++;continue;}
 }part.append(c);}if(!part.isEmpty())out.append(Text.literal(part.toString()).setStyle(style));return out;}
 private AreaText(){}
}
