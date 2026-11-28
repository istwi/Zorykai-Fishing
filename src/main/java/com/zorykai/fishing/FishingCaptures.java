package com.zorykai.fishing;
import com.cobblemon.mod.common.entity.pokeball.EmptyPokeBallEntity;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.api.pokeball.catching.CaptureContext;
import com.zorykai.fishing.mixin.FishingBallAccess;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import static com.zorykai.fishing.GameConfig.*;
/** Native visual sequence, private durable Fishing result. Never enters native Party/PC capture code. */
public final class FishingCaptures {
 private final FishingMod mod;private final Map<UUID,Attempt> attempts=new HashMap<>();private final Set<UUID> locked=new HashSet<>();private UUID committing;
 private static final class Attempt {EmptyPokeBallEntity ball;FishingMod.Encounter encounter;UUID owner;String session;CaptureContext result;double escape;int started,releaseAt=-1;boolean shaking;}
 public FishingCaptures(FishingMod mod){this.mod=mod;}
 public boolean busyOwner(UUID owner){return attempts.values().stream().anyMatch(a->a.owner.equals(owner));}
 public boolean active(UUID ball){return attempts.containsKey(ball);}
 public boolean locked(UUID entity){return locked.contains(entity);}
 public boolean committing(UUID entity){return entity.equals(committing);}
 private boolean valid(Attempt a){var p=mod.server.getPlayerManager().getPlayer(a.owner);return p!=null&&mod.config.map!=null&&mod.config.map.dimension().equals(p.getWorld().getRegistryKey().getValue().toString())&&mod.config.map.bounds().contains(p.getX(),p.getY(),p.getZ())&&mod.sessions.active(p)&&mod.sessions.pending.get(a.owner).id().equals(a.session)&&p.getServerWorld()==a.ball.getWorld()&&a.ball.getOwner()==p&&a.encounter.entity().isAlive()&&mod.encounters.get(a.encounter.entity().getUuid())==a.encounter&&System.currentTimeMillis()<a.encounter.expires();}
 public boolean start(EmptyPokeBallEntity ball,Entity target){
  if(!mod.engine.projectiles.contains(ball.getUuid())||ball.getCaptureState()!=EmptyPokeBallEntity.CaptureState.NOT||!(ball.getOwner() instanceof ServerPlayerEntity p)||!mod.sessions.active(p)||!(target instanceof PokemonEntity mon))return false;
  var e=mod.encounters.get(target.getUuid());if(e==null||!e.owner().equals(p.getUuid())||p.getWorld()!=target.getWorld()||p.squaredDistanceTo(target)>64||locked(target.getUuid())||mon.isBusy())return false;
  try{
   var data=mod.data.profile(p.getUuid());long occupied=mod.engine.fish(p.getUuid()).stream().filter(f->f.state().equals("AVAILABLE")||f.state().equals("SALE_PENDING")).count();if(occupied>=data.capacity){FishingMod.tell(p,"Sua coleção está cheia. Libere espaço antes de capturar.");return false;}
   String id=ball.getCommandTags().stream().filter(x->x.startsWith("zf_ball:")).map(x->x.substring(8)).findFirst().orElse("basica");var definition=mod.rules.definition("pokeballs",id);double relative=mon.getPokemon().getPersistentData().getDouble("zf_relative"),chance=Math.min(1,decimal(definition,"captureChance",.55)/Math.sqrt(Math.max(1,relative)));boolean success=ThreadLocalRandom.current().nextDouble()<chance;
   var a=new Attempt();a.ball=ball;a.encounter=e;a.owner=p.getUuid();a.session=mod.sessions.pending.get(p.getUuid()).id();a.result=new CaptureContext(success?3:ThreadLocalRandom.current().nextInt(1,4),success,false);a.escape=decimal(mod.rules.get("capture"),"escapeChance",.08);a.started=mod.server.getTicks();attempts.put(ball.getUuid(),a);locked.add(target.getUuid());ball.setCapturingPokemon(mon);ball.getDataTracker().set(FishingBallAccess.fishing$target(),mon.getPos());ball.getDataTracker().set(FishingBallAccess.fishing$velocity(),ball.getVelocity().normalize());((FishingBallAccess)(Object)ball).fishing$attempt(mon);mod.engine.equipment.sync(p);return true;
  }catch(Exception ex){abort(ball);mod.fail(ex);return false;}
 }
 public void begin(EmptyPokeBallEntity ball){var a=attempts.get(ball.getUuid());if(a==null||!valid(a)){abort(ball);return;}if(a.shaking||a.releaseAt>=0)return;a.shaking=true;ball.setCaptureState(EmptyPokeBallEntity.CaptureState.SHAKE);int[] remaining={a.result.getNumberOfShakes()};ball.taskBuilder().iterations(remaining[0]+1).delay(1f).interval(1.25f).execute(task->{if(active(ball.getUuid()))((FishingBallAccess)(Object)ball).fishing$shake(task,remaining[0]--,a.result);else task.expire();return kotlin.Unit.INSTANCE;}).build();}
 public void success(EmptyPokeBallEntity ball){var a=attempts.get(ball.getUuid());if(a==null||!valid(a)||!a.shaking||!a.result.isSuccessfulCapture()||ball.getCaptureState()!=EmptyPokeBallEntity.CaptureState.CAPTURED||ball.getCaptureFuture().isDone()){abort(ball);return;}var p=mod.server.getPlayerManager().getPlayer(a.owner);boolean saved=false;committing=a.encounter.entity().getUuid();try{saved=mod.capture(p,committing);}finally{committing=null;}if(saved){remove(a);ball.getCaptureFuture().complete(true);ball.discard();mod.engine.equipment.sync(p);}else release(ball,false);}
 public void release(EmptyPokeBallEntity ball,boolean allowEscape){var a=attempts.get(ball.getUuid());if(a==null){abort(ball);return;}if(a.releaseAt>=0)return;restore(a);ball.setCaptureState(EmptyPokeBallEntity.CaptureState.BROKEN_FREE);a.releaseAt=mod.server.getTicks()+24;ball.getCaptureFuture().complete(false);var p=mod.server.getPlayerManager().getPlayer(a.owner);if(p!=null&&mod.sessions.active(p))FishingMod.tell(p,"O Pokémon saiu da bola. Prepare a próxima tentativa!");if(allowEscape&&ThreadLocalRandom.current().nextDouble()<a.escape){remove(a);ball.discard();mod.expire(a.encounter);} }
 private void restore(Attempt a){var mon=a.encounter.entity();mon.setInvisible(false);mon.setSilent(false);mon.setBeamMode(0);mon.setPhasingTargetId(-1);mon.getBusyLocks().remove(a.ball);}
 private void remove(Attempt a){restore(a);attempts.remove(a.ball.getUuid());locked.remove(a.encounter.entity().getUuid());mod.engine.projectiles.remove(a.ball.getUuid());}
 public void abort(EmptyPokeBallEntity ball){var a=attempts.get(ball.getUuid());if(a!=null)remove(a);mod.engine.projectiles.remove(ball.getUuid());ball.getCaptureFuture().complete(false);ball.discard();}
 public void cancelOwner(UUID owner){for(var a:List.copyOf(attempts.values()))if(a.owner.equals(owner))abort(a.ball);}
 public void cancelEncounter(UUID entity){for(var a:List.copyOf(attempts.values()))if(a.encounter.entity().getUuid().equals(entity))abort(a.ball);}
 public void tick(){for(var a:List.copyOf(attempts.values())){if(!a.ball.isAlive()||!valid(a)||mod.server.getTicks()-a.started>400)abort(a.ball);else if(a.releaseAt>=0&&mod.server.getTicks()>=a.releaseAt){remove(a);a.ball.discard();}}}
}
