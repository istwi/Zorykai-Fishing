package com.zorykai.fishing;
import com.zorykai.fishing.core.*;
import com.google.gson.*;
import java.util.*;
import java.math.*;
import java.sql.*;
import java.util.concurrent.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.fabricmc.loader.api.FabricLoader;
import static com.zorykai.fishing.GameConfig.*;

/** Persisted intent before money; uncertain results require explicit administrative attestation. */
public final class FishingFinance implements AutoCloseable {
 public record Transaction(String id,UUID player,String kind,String stage,BigDecimal amount,JsonObject payload){}
 final FishingMod mod;final ExecutorService executor=Executors.newSingleThreadExecutor(r->new Thread(r,"zorykai-fishing-economy"));
 public FishingFinance(FishingMod mod)throws Exception{this.mod=mod;mod.store.transaction(c->{try(var s=c.createStatement()){s.execute("CREATE UNIQUE INDEX IF NOT EXISTS fishing_finance_lock ON finances(player) WHERE stage NOT IN ('DONE','CANCELED')");s.execute("UPDATE finances SET stage=CASE WHEN CAST(amount AS NUMERIC)=0 THEN 'APPLY_PENDING' ELSE 'UNCERTAIN' END WHERE stage='INTENT'");}return null;});}
 public boolean available(){return FabricLoader.getInstance().isModLoaded("impactor");}
 public BigDecimal round(BigDecimal value){int precision=available()?Backend.decimals(str(mod.rules.get("economy"),"currency","impactor:dollars")):0;return value.setScale(precision,RoundingMode.valueOf(str(mod.rules.get("economy"),"roundingMode","HALF_UP")));}
 public boolean busy(UUID id)throws Exception{return pending(id)!=null;}
 public Transaction pending(UUID id)throws Exception{return mod.store.transaction(c->{try(var p=c.prepareStatement("SELECT id,kind,stage,amount,payload FROM finances WHERE player=? AND stage NOT IN ('DONE','CANCELED') ORDER BY created LIMIT 1")){p.setString(1,id.toString());try(var r=p.executeQuery()){return r.next()?new Transaction(r.getString(1),id,r.getString(2),r.getString(3),new BigDecimal(r.getString(4)),JsonParser.parseString(r.getString(5)).getAsJsonObject()):null;}}});}
 public void buy(ServerPlayerEntity p,String type,String id)throws Exception{
  var data=mod.data.profile(p.getUuid());String file=switch(type){case "rod"->"rods";case "bait"->"baits";case "ball"->"pokeballs";case "upgrade"->"upgrades";case "time"->"time-shop";case "capacity"->"storage";case "ball-upgrade"->"cooldown-shop";default->throw new IllegalArgumentException("Produto inválido");};
  var definition=(type.equals("capacity")||type.equals("ball-upgrade"))?mod.rules.get(file):mod.rules.definition(file,id);if(mod.rules.level(data.xp)<num(definition,"minimumLevel",1))throw new IllegalArgumentException("Nível insuficiente");
  var payload=new JsonObject();payload.addProperty("type",type);payload.addProperty("id",id);payload.add("definition",definition.deepCopy());BigDecimal price=BigDecimal.valueOf(decimal(definition,type.equals("capacity")?"expansionPrice":"price",0));
  if(type.equals("rod")&&data.rods.containsKey(id))throw new IllegalArgumentException("Você já possui essa vara; selecione-a no menu");
  if(type.equals("upgrade")){var rod=data.rods.get(data.selectedRod);int current=rod.upgrades.getOrDefault(id,0);if(current>=num(definition,"maximumLevel",5))throw new IllegalArgumentException("Melhoria no nível máximo");payload.addProperty("rod",data.selectedRod);payload.addProperty("target",current+1);price=price.multiply(BigDecimal.valueOf(current+1));}
  if(type.equals("ball-upgrade")){int current=data.ballCooldownLevel;if(!id.equals("recarga")||!bool(definition,"enabled",true))throw new IllegalArgumentException("Melhoria de recarga indisponível");if(current>=num(definition,"maximumLevel",5)||mod.engine.equipment.cooldownSeconds(current)<=mod.engine.equipment.cooldownSeconds(current+1))throw new IllegalArgumentException("Recarga no nível máximo");payload.addProperty("target",current+1);price=price.multiply(BigDecimal.valueOf(current+1));}
  if(type.equals("capacity")&&data.capacity>=num(definition,"maximumCapacity",300))throw new IllegalArgumentException("Armazém no tamanho máximo");
  if(type.equals("time")){data=mod.sessions.renew(p);if(mod.sessions.balance(data)+num(definition,"minutes",10)*60L>num(mod.rules.get("timers"),"maximumAccumulatedMinutes",360)*60L)throw new IllegalArgumentException("Esse pacote excede o limite de tempo acumulado");}
  begin(p.getUuid(),"BUY",price,false,payload);
 }
 public void sell(ServerPlayerEntity p,List<String> ids)throws Exception{
  var selected=mod.engine.fish(p.getUuid()).stream().filter(f->ids.contains(f.id())&&f.state().equals("AVAILABLE")&&!f.favourite()).toList();if(selected.isEmpty()||selected.size()!=new HashSet<>(ids).size()||selected.size()>num(mod.rules.get("storage"),"maximumBatchSale",100))throw new IllegalArgumentException("Seleção mudou, favorita ou indisponível");
  var payload=new JsonObject();var array=new JsonArray();selected.forEach(f->array.add(f.id()));payload.add("captures",array);BigDecimal sum=selected.stream().map(GameEngine.Fish::value).reduce(BigDecimal.ZERO,BigDecimal::add);begin(p.getUuid(),"SELL",sum,true,payload);
 }
 public void reward(UUID owner,String key,JsonObject reward,String source)throws Exception{var payload=new JsonObject();payload.addProperty("key",key);payload.addProperty("source",source);payload.add("reward",reward.deepCopy());begin(owner,"REWARD",BigDecimal.valueOf(decimal(reward,"money",0)),true,payload);}
 void begin(UUID owner,String kind,BigDecimal amount,boolean deposit,JsonObject payload)throws Exception{
  amount=round(amount);if(amount.signum()<0||amount.compareTo(new BigDecimal("1000000000"))>0)throw new IllegalArgumentException("Valor inválido");if(amount.signum()>0&&!available())throw new IllegalStateException("Economia Impactor indisponível");
  String id=UUID.randomUUID().toString();payload.addProperty("deposit",deposit);payload.addProperty("currency",str(mod.rules.get("economy"),"currency","impactor:dollars"));final var total=amount;
  mod.store.transaction(c->{if(kind.equals("REWARD")&&mod.data.profile(c,owner).counters.containsKey("reward:"+str(payload,"key","")))throw new IllegalStateException("Recompensa já entregue");if(pending(owner)!=null)throw new IllegalStateException("Uma transação sua ainda aguarda conclusão ou conferência");
   if(kind.equals("SELL"))for(var entry:payload.getAsJsonArray("captures")){String capture=entry.getAsString();try(var q=c.prepareStatement("INSERT INTO capture_details(id,payload,state,favourite) SELECT id,'{}','SALE_PENDING',0 FROM captures WHERE id=? AND owner=? ON CONFLICT(id) DO UPDATE SET state='SALE_PENDING' WHERE capture_details.state='AVAILABLE' AND capture_details.favourite=0")){q.setString(1,capture);q.setString(2,owner.toString());if(q.executeUpdate()!=1)throw new SQLException("Captura não reservada");}}
   try(var q=c.prepareStatement("INSERT INTO finances VALUES(?,?,?,'INTENT',?,?,?)")){q.setString(1,id);q.setString(2,owner.toString());q.setString(3,kind);q.setString(4,total.toPlainString());q.setString(5,payload.toString());q.setLong(6,System.currentTimeMillis());q.executeUpdate();}mod.data.audit(c,owner,"FINANCE_INTENT",Map.of("id",id,"kind",kind,"amount",total));return null;});
  var future=amount.signum()==0?CompletableFuture.completedFuture(true):CompletableFuture.supplyAsync(()->Backend.change(owner,str(payload,"currency",""),total,deposit),executor).thenCompose(x->x);
  future.whenComplete((ok,error)->mod.server.execute(()->{try{if(error!=null){stage(id,"UNCERTAIN");mod.log(error);message(owner,"Transação preservada sem confirmação. A equipe precisa conferir no Impactor.");}else if(Boolean.TRUE.equals(ok)){stage(id,"APPLY_PENDING");apply(new Transaction(id,owner,kind,"APPLY_PENDING",total,payload));message(owner,"Transação concluída: $"+total);}else{cancel(new Transaction(id,owner,kind,"INTENT",total,payload));message(owner,"Pagamento recusado; nenhuma compra/venda foi concluída.");}}catch(Exception e){mod.fail(e);message(owner,"Resultado preservado para recuperação; não repita pagamento.");}}));
 }
 void stage(String id,String stage)throws Exception{mod.store.transaction(c->{try(var q=c.prepareStatement("UPDATE finances SET stage=? WHERE id=? AND stage NOT IN ('DONE','CANCELED')")){q.setString(1,stage);q.setString(2,id);q.executeUpdate();}return null;});}
 void cancel(Transaction tx)throws Exception{mod.store.transaction(c->{if(tx.kind.equals("SELL"))for(var id:tx.payload.getAsJsonArray("captures"))try(var q=c.prepareStatement("UPDATE capture_details SET state='AVAILABLE' WHERE id=? AND state='SALE_PENDING'")){q.setString(1,id.getAsString());q.executeUpdate();}try(var q=c.prepareStatement("UPDATE finances SET stage='CANCELED' WHERE id=?")){q.setString(1,tx.id);q.executeUpdate();}mod.data.audit(c,tx.player,"FINANCE_CANCELED",Map.of("id",tx.id));return null;});}
 void apply(Transaction tx)throws Exception{mod.store.transaction(c->{try(var q=c.prepareStatement("SELECT stage FROM finances WHERE id=?")){q.setString(1,tx.id);try(var r=q.executeQuery()){if(!r.next()||!r.getString(1).equals("APPLY_PENDING"))return null;}}
  var d=mod.data.profile(c,tx.player);
  if(tx.kind.equals("BUY")){var payload=tx.payload;String type=str(payload,"type",""),id=str(payload,"id","");var definition=payload.getAsJsonObject("definition");switch(type){
   case "rod"->{d.rods.putIfAbsent(id,new PlayerData.Rod());d.selectedRod=id;}
   case "bait"->{d.baits.merge(id,num(definition,"amount",1),Math::addExact);d.selectedBait=id;}
   case "ball"->{d.balls.merge(id,num(definition,"amount",1),Math::addExact);d.selectedBall=id;}
   case "upgrade"->d.rods.get(str(payload,"rod","")).upgrades.merge(id,num(payload,"target",1),Math::max);
   case "ball-upgrade"->d.ballCooldownLevel=Math.max(d.ballCooldownLevel,num(payload,"target",1));
   case "capacity"->d.capacity=Math.min(num(definition,"maximumCapacity",300),d.capacity+num(definition,"expansionSlots",10));
   case "time"->mod.sessions.addTime(d,num(definition,"minutes",10)*60L,num(definition,"validHours",24));
   default->throw new IllegalArgumentException("Produto não reconhecido");
  }}else if(tx.kind.equals("SELL")){int sold=0;for(var id:tx.payload.getAsJsonArray("captures")){try(var q=c.prepareStatement("UPDATE capture_details SET state='SOLD' WHERE id=? AND state='SALE_PENDING'")){q.setString(1,id.getAsString());sold+=q.executeUpdate();}}d.sales+=sold;mod.progress.event(d,"SELL","","",sold);}
  else if(tx.kind.equals("REWARD")){String key=str(tx.payload,"key","");String source=str(tx.payload,"source","");if(d.counters.getOrDefault("reward:"+key,0L)==0){mod.progress.grant(d,tx.payload.getAsJsonObject("reward"));d.counters.put("reward:"+key,1L);if(source.equals("quest"))d.completedQuests.add(key.split("@")[0]);if(source.equals("achievement"))d.achievements.add(key);if(source.equals("tournament")&&bool(tx.payload.getAsJsonObject("reward"),"winner",false))d.tournamentWins++;}}
  mod.data.save(c,tx.player,d);try(var q=c.prepareStatement("UPDATE finances SET stage='DONE' WHERE id=?")){q.setString(1,tx.id);q.executeUpdate();}mod.data.audit(c,tx.player,"FINANCE_DONE",Map.of("id",tx.id,"kind",tx.kind,"amount",tx.amount));return null;
 });var p=mod.server.getPlayerManager().getPlayer(tx.player);if(p!=null&&mod.sessions.active(p))mod.engine.equip(p);}
 public void recover(UUID id)throws Exception{var tx=pending(id);if(tx!=null&&tx.stage.equals("APPLY_PENDING"))apply(tx);}
 public void resolve(UUID owner,String id,boolean applied)throws Exception{var tx=pending(owner);if(tx==null||!tx.id.equals(id)||!tx.stage.equals("UNCERTAIN"))throw new IllegalArgumentException("Transação incerta não encontrada");if(applied){stage(id,"APPLY_PENDING");apply(new Transaction(tx.id,owner,tx.kind,"APPLY_PENDING",tx.amount,tx.payload));}else cancel(tx);mod.audit(owner,"FINANCE_ATTESTATION",Map.of("id",id,"applied",applied));}
 void message(UUID owner,String text){var p=mod.server.getPlayerManager().getPlayer(owner);if(p!=null)FishingMod.tell(p,text);}
 public static class Backend {
  static net.impactdev.impactor.api.economy.currency.Currency currency(String id){return net.impactdev.impactor.api.economy.EconomyService.instance().currencies().currency(net.kyori.adventure.key.Key.key(id)).orElseThrow(()->new IllegalStateException("Moeda não registrada: "+id));}
  static int decimals(String id){return currency(id).decimals();}
  static CompletableFuture<Boolean> change(UUID owner,String id,BigDecimal amount,boolean deposit){var service=net.impactdev.impactor.api.economy.EconomyService.instance();return service.account(currency(id),owner).thenCompose(account->{if(!deposit&&account.balance().compareTo(amount)<0)return CompletableFuture.completedFuture(false);var result=deposit?account.deposit(amount):account.withdraw(amount);if(!result.successful())return CompletableFuture.completedFuture(false);return service.save(account).thenApply(v->true);}).toCompletableFuture();}
 }
 public void close(){executor.shutdown();}
}
