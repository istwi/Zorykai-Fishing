"""Package only the artifact that passed all isolated release runs. No production or GitHub writes."""
from pathlib import Path
import hashlib,json,shutil,zipfile,xml.etree.ElementTree as ET
repo=Path('/workspace/Zorykai-Fishing');out=Path('/workspace/deliverables/zorykai-fishing-0.2.2-beta.1');out.mkdir(parents=True,exist_ok=True)
name='Zorykai-Fishing-0.2.2-beta.1.jar';artifact=repo/'build/libs'/name
sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
assert artifact.stat().st_size>1000000
runs={};reports=repo/'docs/tests';reports.mkdir(exist_ok=True)
for kind in ('standalone','integrated','crash'):
 runtime=Path('/workspace/shared/fishing-022-verified-'+kind)
 assert sha(runtime/'mods'/name)==sha(artifact),f'Artifact differs from tested {kind}'
 a=json.loads((runtime/'fishing-proof-results.json').read_text())
 assert a and all(x.get('passed') for x in a),(kind,a)
 helper=Path('/workspace/fishing-022-verified-'+kind+'.log').read_text()
 assert 'Traceback' not in helper and (kind=='crash' or helper.startswith('exit 0'))
 shutil.copy2(runtime/'fishing-proof-results.json',reports/f'022-{kind}-results.json')
 for log in (('prepare.log','recover.log') if kind=='crash' else ('server.log',)):
  shutil.copy2(runtime/log,reports/f'022-{kind}-{log}')
 runs[kind]={'checks':len(a),'failed':0,'artifactSha256':sha(artifact)}
unit=[]
for p in (repo/'build/test-results/test').glob('TEST-*.xml'):
 x=ET.parse(p).getroot();row={k:int(x.attrib[k]) for k in ('tests','failures','errors')};row['suite']=x.attrib['name'];assert row['failures']==row['errors']==0;unit.append(row)
(reports/'022-unit-results.json').write_text(json.dumps(unit,indent=2)+'\n')
summary={'version':'0.2.2-beta.1','jar':name,'sha256':sha(artifact),'unitTests':sum(x['tests'] for x in unit),'fabricRuns':runs,'humanClientTested':False,'productionModified':False,'githubPublished':False}
(reports/'022-verification.json').write_text(json.dumps(summary,ensure_ascii=False,indent=2)+'\n')
shutil.copy2('/workspace/fishing-022-build.log',reports/'022-build.log')
validation=repo/'docs/VALIDACAO_BETA.md';text=validation.read_text();marker='## Resultado final\n';text=text.split(marker)[0]
text+='\n'+marker+'\nCompilação concluída com sucesso. JUnit: **'+str(summary['unitTests'])+' testes, zero falhas**.\n\n| Runtime Fabric | Verificações | Falhas |\n|---|---:|---:|\n'
for kind,d in runs.items():text+=f'| {kind} | {d["checks"]} | 0 |\n'
text+='\nAs contagens de runtimes incluem cenários repetidos entre ambientes. Não são '+str(sum(x['checks'] for x in runs.values()))+' funcionalidades independentes. Todos os runtimes usaram exatamente o JAR com SHA-256 `'+sha(artifact)+'`.\n'
validation.write_text(text)
shutil.copy2(artifact,out/name);(out/name).chmod(0o644)
allowed=[]
for p in repo.rglob('*'):
 if not p.is_file():continue
 rel=p.relative_to(repo);parts=rel.parts
 if parts[0] in ('src','docs','examples','gradle'):allowed.append((rel,p))
 elif str(rel) in ('build.gradle','settings.gradle','gradle.properties','gradlew','gradlew.bat','.gitignore','README.md','libs/LEIA-ME.md','audit/run_native.py','audit/run_restart.py','audit/package_release.py'):allowed.append((rel,p))
def writezip(path,files):
 with zipfile.ZipFile(path,'w',zipfile.ZIP_DEFLATED,compresslevel=9) as z:
  for rel,p in sorted(files,key=lambda x:str(x[0])):
   i=zipfile.ZipInfo(str(rel).replace('\\','/'),(2026,11,24,0,0,0));i.compress_type=zipfile.ZIP_DEFLATED;i.external_attr=((0o755 if p.name=='gradlew' else 0o644)|0o100000)<<16;z.writestr(i,p.read_bytes())
 path.chmod(0o644)
source=out/'zorykai-fishing-codigo.zip';writezip(source,[(Path('Zorykai-Fishing')/r,p) for r,p in allowed])
configroot=Path('/workspace/shared/fishing-022-verified-standalone/config/zorykai-fishing')
configfiles=[]
for p in configroot.rglob('*.json'):
 rel=p.relative_to(configroot)
 if rel.parts[0] in ('backups','areas') or rel.name=='mapa.json':continue
 configfiles.append((Path('config/zorykai-fishing')/rel,p))
assert len(configfiles)>40 and not any(r.name=='mapa.json' for r,p in configfiles)
configs=out/'zorykai-fishing-configuracoes.zip';writezip(configs,configfiles+[(Path('examples')/p.relative_to(repo/'examples'),p) for p in (repo/'examples').rglob('*') if p.is_file()]+[(Path('INSTALACAO_RAPIDA.md'),repo/'docs/INSTALACAO_RAPIDA.md'),(Path('ATUALIZACAO_0.2.2.md'),repo/'docs/ATUALIZACAO_0.2.2.md')])
full=out/'zorykai-fishing-0.2.2-beta.1-completo.zip'
fullfiles=[(Path('mods')/name,artifact),(Path('README.md'),repo/'README.md'),(Path('INSTALACAO_RAPIDA.md'),repo/'docs/INSTALACAO_RAPIDA.md')]+configfiles
fullfiles += [(Path('codigo/Zorykai-Fishing')/r,p) for r,p in allowed]
fullfiles += [(Path('examples')/p.relative_to(repo/'examples'),p) for p in (repo/'examples').rglob('*') if p.is_file()]
fullfiles += [(Path('docs')/p.relative_to(repo/'docs'),p) for p in (repo/'docs').rglob('*') if p.is_file()]
writezip(full,fullfiles)
for path in (source,configs,full):
 with zipfile.ZipFile(path) as z:assert z.testzip() is None and not any('checks.jar' in n or 'multiworld-code' in n or '/libs/' in n and not n.endswith('LEIA-ME.md') for n in z.namelist())
(out/'SHA256SUMS').write_text(''.join(f'{sha(p)}  {p.name}\n' for p in (out/name,source,configs,full)))
shutil.copy2(repo/'docs/INSTALACAO_RAPIDA.md',out/'INSTALACAO_RAPIDA.md');shutil.copy2(validation,out/'VALIDACAO.md')
print(json.dumps(summary,ensure_ascii=False,indent=2));print('Packaged:',[p.name for p in out.iterdir()])
