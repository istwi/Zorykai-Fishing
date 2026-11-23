from pathlib import Path
import sys,subprocess,time,json
source=Path('/workspace/Zorykai-Fishing/audit/run_native.py').read_text()
exec(compile(source.split("with (r/'server.log').open('w') as log:")[0], 'native-setup','exec'))
props=r/'server.properties';props.write_text(props.read_text().replace('25570','25572').replace('25571','25572'))
def launch(phase):
 log=(r/(phase+'.log')).open('w')
 p=subprocess.Popen(['/workspace/shared/jdk/bin/java',*jvm,'-Dzorykai.fishing.testRestart='+phase,'-Xmx3G','-Xverify:all','-jar','fabric-server-launch.jar','nogui'],cwd=r,stdin=subprocess.PIPE,stdout=log,stderr=subprocess.STDOUT)
 return p,log
p,log=launch('prepare')
try:
 deadline=time.monotonic()+200
 while not (r/'restart-ready').exists():
  if p.poll() is not None:raise RuntimeError('Prepare exited: '+str(r/'prepare.log'))
  if time.monotonic()>deadline:raise TimeoutError('Prepare timeout')
  time.sleep(.25)
 p.kill();p.wait(timeout=20);log.close()
 assert 'Stopping server' not in (r/'prepare.log').read_text(),'Test was not a hard kill'
 p,log=launch('recover');code=p.wait(timeout=200);log.close()
 a=json.loads((r/'fishing-proof-results.json').read_text());print(json.dumps(a,indent=2));assert code==0 and all(x.get('passed') for x in a)
finally:
 if p.poll() is None:p.kill();p.wait(timeout=20)
