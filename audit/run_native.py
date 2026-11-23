from pathlib import Path
import os,sys,shutil,subprocess,json,urllib.parse
repo=Path('/workspace/Zorykai-Fishing');r=Path(sys.argv[1]);integrated=len(sys.argv)>2;r.mkdir(exist_ok=False);base=Path('/workspace/shared/raids-test-server')
for n in ['server.jar','libraries','fabric-server-launch.jar']:(r/n).symlink_to(base/n,target_is_directory=(base/n).is_dir())
(r/'mods').mkdir()
for p in (base/'mods').glob('*.jar'):
 if 'Raids' not in p.name:shutil.copy2(p,r/'mods'/p.name)
for name in ['Zorykai-Fishing-0.2.0-beta.1.jar','Zorykai-Fishing-0.2.0-beta.1-checks.jar']:shutil.copy2(repo/'build/libs'/name,r/'mods'/name)
if integrated:
 for p in [Path('/workspace/attachments/d21e2e9d-8b64-4b1f-b135-3c9ae646ed0e/Multiworld-Fabric-bundle-1.13.1-gamerule-fix.jar'),Path('/workspace/Zorykai-menu/build/libs/zorykaimenus-1.3.6.jar'),Path('/workspace/Zorykai-menu/.cache/deps/impactor.jar'),Path('/workspace/Shirokai-Trainers/downloads/shirokai-trainers-0.3.5.jar'),Path('/workspace/Shirokai-Guard/build/libs/shirokai-guard-0.5.3-alpha.1.jar')]:shutil.copy2(p,r/'mods'/p.name)
if integrated:
 menu=r/'config/zorykaimenus/menus';menu.mkdir(parents=True);shutil.copy2(repo/'examples/zorykai-menus/fishing_main.json',menu/'fishing_main.json')
(r/'eula.txt').write_text('eula=true\n');(r/'server.properties').write_text('server-ip=127.0.0.1\nserver-port='+os.environ.get('FISHING_TEST_PORT', '25571' if integrated else '25570')+'\nonline-mode=false\nenforce-secure-profile=false\nview-distance=2\nsimulation-distance=2\ngenerate-structures=false\nlevel-type=minecraft:flat\n')
jvm=[]
for scheme in ['http','https']:
 u=urllib.parse.urlsplit(os.environ.get(scheme.upper()+'_PROXY',''))
 if u.hostname:jvm+=['-D'+scheme+'.proxyHost='+u.hostname,'-D'+scheme+'.proxyPort='+str(u.port or 80)]
jvm+=['-Djavax.net.ssl.trustStore=/etc/ssl/certs/java/cacerts','-Dhttp.nonProxyHosts=localhost|127.*|[::1]']
with (r/'server.log').open('w') as log:
 p=subprocess.Popen(['/workspace/shared/jdk/bin/java',*jvm,'-Xmx3G','-Xverify:all','-jar','fabric-server-launch.jar','nogui'],cwd=r,stdin=subprocess.PIPE,stdout=log,stderr=subprocess.STDOUT)
 try:code=p.wait(timeout=240)
 finally:
  if p.poll() is None:p.terminate();p.wait(timeout=30)
print('exit',code)
report=r/'fishing-proof-results.json'
if not report.exists():raise RuntimeError('No result: '+str(r/'server.log'))
d=json.loads(report.read_text());print(json.dumps(d,indent=2));assert all(x.get('passed') for x in d)
