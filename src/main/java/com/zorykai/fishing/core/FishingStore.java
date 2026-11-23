package com.zorykai.fishing.core;
import java.nio.file.*;
import java.sql.*;
import java.util.*;

/** SQLite is private Fishing storage. This class never calls Cobblemon party/PC APIs. */
public final class FishingStore implements AutoCloseable {
 private final Connection db;
 public record Capture(String id,String owner,String species,String lake,double metres,boolean shiny,long captured){}
 public FishingStore(Path file)throws Exception{
  Files.createDirectories(file.toAbsolutePath().getParent());db=DriverManager.getConnection("jdbc:sqlite:"+file);
  try(var s=db.createStatement()){
   s.execute("PRAGMA busy_timeout=5000");s.execute("PRAGMA journal_mode=WAL");s.execute("PRAGMA synchronous=FULL");
   int version;try(var r=s.executeQuery("PRAGMA user_version")){r.next();version=r.getInt(1);}if(version>2)throw new IllegalStateException("Banco de versão mais recente");
   s.execute("CREATE TABLE IF NOT EXISTS encounters(id TEXT PRIMARY KEY,owner TEXT NOT NULL,entity TEXT UNIQUE NOT NULL,species TEXT NOT NULL,lake TEXT NOT NULL,metres REAL NOT NULL,shiny INTEGER NOT NULL,expires INTEGER NOT NULL,state TEXT NOT NULL CHECK(state IN ('ACTIVE','CAPTURED','EXPIRED')))");
   s.execute("CREATE TABLE IF NOT EXISTS captures(id TEXT PRIMARY KEY REFERENCES encounters(id),owner TEXT NOT NULL,species TEXT NOT NULL,lake TEXT NOT NULL,metres REAL NOT NULL,shiny INTEGER NOT NULL,captured INTEGER NOT NULL,favourite INTEGER NOT NULL DEFAULT 0)");
   s.execute("CREATE INDEX IF NOT EXISTS captures_owner ON captures(owner)");s.execute("CREATE TABLE IF NOT EXISTS profiles(player TEXT PRIMARY KEY,payload TEXT NOT NULL)");
   s.execute("CREATE TABLE IF NOT EXISTS sessions(player TEXT PRIMARY KEY,transaction_id TEXT UNIQUE NOT NULL,phase TEXT NOT NULL,snapshot BLOB NOT NULL,origin TEXT NOT NULL,started INTEGER NOT NULL)");
   s.execute("CREATE TABLE IF NOT EXISTS finances(id TEXT PRIMARY KEY,player TEXT NOT NULL,kind TEXT NOT NULL,stage TEXT NOT NULL,amount TEXT NOT NULL,payload TEXT NOT NULL,created INTEGER NOT NULL)");
   s.execute("CREATE INDEX IF NOT EXISTS finances_player ON finances(player,stage)");
   s.execute("CREATE TABLE IF NOT EXISTS capture_details(id TEXT PRIMARY KEY REFERENCES captures(id),payload TEXT NOT NULL,state TEXT NOT NULL DEFAULT 'AVAILABLE',favourite INTEGER NOT NULL DEFAULT 0)");
   s.execute("CREATE TABLE IF NOT EXISTS global_state(id TEXT PRIMARY KEY,payload TEXT NOT NULL)");
   s.execute("CREATE TABLE IF NOT EXISTS audit(id INTEGER PRIMARY KEY AUTOINCREMENT,at INTEGER NOT NULL,player TEXT,event TEXT NOT NULL,payload TEXT NOT NULL)");
   s.execute("PRAGMA foreign_keys=ON");s.execute("PRAGMA user_version=2");
  }
 }
 public interface Work<T>{T run(Connection c)throws Exception;}
 public synchronized <T>T transaction(Work<T> work)throws Exception{
  boolean owner=db.getAutoCommit();if(owner)db.setAutoCommit(false);
  try{T result=work.run(db);if(owner)db.commit();return result;}catch(Exception e){if(owner)db.rollback();throw e;}finally{if(owner)db.setAutoCommit(true);}
 }
 public synchronized void encounter(UUID id,UUID owner,UUID entity,String species,String lake,double metres,boolean shiny,long expires)throws SQLException{
  if(!Double.isFinite(metres)||metres<=0||expires<=0)throw new IllegalArgumentException("Medida/expiração inválida");
  try(var p=db.prepareStatement("INSERT INTO encounters VALUES(?,?,?,?,?,?,?,?, 'ACTIVE')")){p.setString(1,id.toString());p.setString(2,owner.toString());p.setString(3,entity.toString());p.setString(4,species);p.setString(5,lake);p.setDouble(6,metres);p.setInt(7,shiny?1:0);p.setLong(8,expires);p.executeUpdate();}
 }
 public synchronized boolean capture(UUID encounter,UUID owner,int capacity,long now)throws SQLException{
  if(capacity<1)throw new IllegalArgumentException("Capacidade inválida");
  try{return transaction(c->{
   try(var q=c.prepareStatement("SELECT count(*) FROM captures LEFT JOIN capture_details USING(id) WHERE captures.owner=? AND (capture_details.state IS NULL OR capture_details.state IN ('AVAILABLE','SALE_PENDING'))")){q.setString(1,owner.toString());try(var rs=q.executeQuery()){if(rs.next()&&rs.getInt(1)>=capacity)return false;}}
   try(var p=c.prepareStatement("INSERT OR IGNORE INTO captures(id,owner,species,lake,metres,shiny,captured) SELECT id,owner,species,lake,metres,shiny,? FROM encounters WHERE id=? AND owner=? AND state='ACTIVE' AND expires>?")){p.setLong(1,now);p.setString(2,encounter.toString());p.setString(3,owner.toString());p.setLong(4,now);if(p.executeUpdate()!=1)return false;}
   try(var p=c.prepareStatement("UPDATE encounters SET state='CAPTURED' WHERE id=? AND state='ACTIVE'")){p.setString(1,encounter.toString());if(p.executeUpdate()!=1)throw new SQLException("Estado de captura divergente");}return true;
  });}catch(Exception e){if(e instanceof SQLException q)throw q;throw new SQLException(e);}
 }
 public synchronized void expire(UUID id)throws SQLException{try(var p=db.prepareStatement("UPDATE encounters SET state='EXPIRED' WHERE id=? AND state='ACTIVE'")){p.setString(1,id.toString());p.executeUpdate();}}
 public synchronized int expireAll()throws SQLException{try(var s=db.createStatement()){return s.executeUpdate("UPDATE encounters SET state='EXPIRED' WHERE state='ACTIVE'");}}
 public synchronized List<Capture> captures(UUID owner)throws SQLException{
  var rows=new ArrayList<Capture>();try(var p=db.prepareStatement("SELECT id,owner,species,lake,metres,shiny,captured FROM captures WHERE owner=? ORDER BY captured DESC,id")){p.setString(1,owner.toString());try(var r=p.executeQuery()){while(r.next())rows.add(new Capture(r.getString(1),r.getString(2),r.getString(3),r.getString(4),r.getDouble(5),r.getInt(6)!=0,r.getLong(7)));}}return List.copyOf(rows);
 }
 @Override public synchronized void close()throws SQLException{db.close();}
}
