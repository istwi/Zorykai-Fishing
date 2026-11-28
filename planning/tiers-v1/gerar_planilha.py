from pathlib import Path
import json,zipfile,hashlib,math,collections
import xlsxwriter
from decimal import Decimal, ROUND_FLOOR
import argparse
parser=argparse.ArgumentParser(description='Gerar proposta de tiers para revisão')
parser.add_argument('--jar',type=Path,required=True)
parser.add_argument('--out',type=Path,default=Path(__file__).parent/'gerado')
args=parser.parse_args();jar=args.jar;out=args.out;out.mkdir(parents=True,exist_ok=True)
with zipfile.ZipFile(jar) as z:
 meta=json.loads(z.read('fabric.mod.json'));data={};paths={}
 for n in z.namelist():
  if '/species/' in n and n.endswith('.json'):
   key=n.rsplit('/',1)[-1][:-5];data[key]=json.loads(z.read(n));paths[key]=n
GROUPS={
1:{'Comum':'magikarp goldeen poliwag wooper barboach lotad surskit tympole buizel chewtle','Incomum':'psyduck slowpoke finneon remoraid marill','Raro':'azurill tadbulb dewpider','Especial':'feebas squirtle'},
2:{'Comum':'tentacool wingull shellder krabby corphish shellos chinchou horsea arrokuda basculin ducklett poliwhirl','Incomum':'seel spheal staryu luvdisc clamperl clauncher skrelp wimpod','Raro':'wishiwashi pyukumuku binacle qwilfish mantyke','Especial':'bruxish totodile mudkip piplup oshawott'},
3:{'Comum':'seaking quagsire whiscash lombre palpitoad floatzel golduck azumarill lumineon pelipper','Incomum':'slowbro lanturn seadra kingler crawdaunt octillery tentacruel dewgong sealeo gastrodon drednaw barraskewda','Raro':'jellicent alomomola basculegion araquanid cramorant vaporeon','Especial':'froakie popplio sobble quaxly'},
4:{'Comum':'poliwrath politoed ludicolo seismitoad slowking walrein mantine barbaracle','Incomum':'sharpedo huntail gorebyss clawitzer dragalge golisopod relicanth veluza','Raro':'gyarados milotic lapras kingdra tatsugiri palafin','Especial':'wailord dondozo greninja primarina feraligatr swampert empoleon blastoise quaquaval inteleon samurott'}
}
reserve='kyogre lugia suicune manaphy phione palkia keldeo tapufini walkingwake volcanion ironbundle omanyte kabuto tirtouga omastar kabutops carracosta'.split()
weights={'magikarp':4,'goldeen':2,'poliwag':2,'tentacool':2,'wingull':2,'seaking':2,'poliwrath':2,'mantine':2}
classes=['Comum','Incomum','Raro','Especial'];masses=[70,25,4.5,.5]
profiles={1:[25,60,12,2.8,.2],2:[15,60,20,4.5,.5],3:[10,55,27,7,1],4:[5,50,33,10,2]}
themes={1:'Águas tranquilas',2:'Exploração e variedade',3:'Encontros raros e evoluções',4:'Grandes descobertas'};levels={1:1,2:10,3:25,4:45}
fresh=set('magikarp goldeen poliwag wooper barboach lotad surskit tympole buizel chewtle psyduck slowpoke marill azurill tadbulb dewpider feebas squirtle poliwhirl corphish shellos totodile mudkip piplup oshawott seaking quagsire whiscash lombre palpitoad floatzel golduck azumarill slowbro crawdaunt gastrodon drednaw araquanid vaporeon froakie popplio sobble quaxly poliwrath politoed ludicolo seismitoad slowking gyarados milotic dondozo greninja feraligatr swampert blastoise inteleon samurott'.split())
coast=set('shellder krabby corphish shellos seel spheal staryu clamperl wimpod pyukumuku binacle dewpider kingler crawdaunt dewgong sealeo gastrodon walrein barbaracle golisopod'.split())
def habitat(s):return 'Lago / rio / margem' if s in fresh else 'Costa / praia / águas rasas' if s in coast else 'Mar / águas abertas'
def factor(b):return 2 if b<=.5 else 1.75 if b<=1 else 1.6 if b<=2 else 1.4 if b<=5 else 1.25
def limits(b):
 f=factor(b);cap=float((Decimal(str(b))*Decimal(str(f))*10).to_integral_value(rounding=ROUND_FLOOR)/10)
 return round(.7*b,3),cap,f
rows=[]
for tier,groups in GROUPS.items():
 for group,species in groups.items():
  ids=species.split();total=sum(weights.get(s,1) for s in ids)
  for s in ids:
   assert s in data,s
   d=data[s];assert d.get('implemented') is True,s
   assert not any(x in d.get('labels',[]) for x in ['legendary','mythical','ultra_beast','paradox']),s
   b=d['height']/10;lo,cap,f=limits(b);w=weights.get(s,1);chance=masses[classes.index(group)]*w/total
   bands=[lo,.9*b,b+.25*(cap-b),b+.65*(cap-b),b+.9*(cap-b),cap]
   mean=sum(p*(a+c)/2 for p,a,c in zip(profiles[tier],bands,bands[1:]))/100
   e2=sum(p*(a*a+a*c+c*c)/3 for p,a,c in zip(profiles[tier],bands,bands[1:]))/(100*b*b)
   note='Proposta de equilíbrio; não é spawnrate oficial.'
   if s in ['wailord','dondozo','gyarados','milotic']:note='Espécie grande: recomendar área ampla; sem reduzir sua base para caber em lago pequeno.'
   if s in ['wishiwashi','palafin','basculegion','wooper','slowpoke','slowbro','slowking','shellos','gastrodon','basculin','qwilfish','tatsugiri']:note+=' Somente forma padrão; variantes precisam de regra própria.'
   if s in ['squirtle','totodile','mudkip','piplup','oshawott','froakie','popplio','sobble','quaxly','greninja','primarina','feraligatr','swampert','empoleon','blastoise','quaquaval','inteleon','samurott']:note+=' Inicial/evolução: opção rara de coleção; não entra na Party.'
   rows.append(dict(tier=tier,id=s,name=d['name'],group=group,habitat=habitat(s),weight=w,chance=chance,base=b,min=lo,max=cap,factor=f,bands=bands,mean=mean,e2=e2,note=note,path=paths[s]))
assert len({r['id'] for r in rows})==len(rows)
for t in GROUPS:assert abs(sum(r['chance'] for r in rows if r['tier']==t)-100)<1e-9
for s in reserve:assert s in data,s
proposal={'status':'PROPOSTA PARA REVISAO - NAO INSTALAR COMO CONFIGURACAO ATUAL','source':{'mod':meta.get('name'), 'version':meta['version'],'jarSha256':hashlib.sha256(jar.read_bytes()).hexdigest(),'heightUnit':'decimetros; height/10 = metros de referencia, como Fishing 0.3.0'},'tiers':[{'tier':t,'name':themes[t],'suggestedMinimumLevel':levels[t],'categoryChancePercent':dict(zip(classes,masses)),'sizeProfilePercent':dict(zip(['Pequeno','Normal','Grande','Gigante','Recordista'],profiles[t]))} for t in GROUPS],'species':rows,'note':'Tetos, tiers, pesos e faixas sao proposta de design; os dados extraidos do JAR sao especie, implementacao e altura-base.'}
(out/'proposta-tiers.json').write_text(json.dumps(proposal,ensure_ascii=False,indent=2)+'\n')
# Workbook: all calculated cells carry cached results so they are readable before recalculation.
w=xlsxwriter.Workbook(out/'Zorykai-Fishing-Tiers-e-Tamanhos.xlsx');w.set_properties({'title':'Zorykai Fishing — proposta de tiers e limites','subject':'Revisão antes da implementação','author':'Zorykai','comments':'Dados-base do Cobblemon 1.8.3; parâmetros de equilíbrio propostos.'});w.set_calc_mode('auto')
navy='#172554';blue='#2563EB';aqua='#06B6D4';light='#EFF6FF';green='#15803D';orange='#B45309'
fmt={
 'title':w.add_format({'bold':True,'font_size':20,'font_color':'white','bg_color':navy,'valign':'vcenter'}),
 'sub':w.add_format({'font_color':'#475569','text_wrap':True,'valign':'top'}),
 'head':w.add_format({'bold':True,'font_color':'white','bg_color':blue,'text_wrap':True,'valign':'vcenter','border':1,'border_color':'#DBEAFE'}),
 'text':w.add_format({'font_color':'#334155','valign':'top','text_wrap':True}),
 'input':w.add_format({'font_color':blue,'bg_color':'#DBEAFE','num_format':'0.000','valign':'top'}),
 'inputint':w.add_format({'font_color':blue,'bg_color':'#DBEAFE','num_format':'0','valign':'top'}),
 'num':w.add_format({'num_format':'0.000','font_color':'#334155','valign':'top'}),
 'pct':w.add_format({'num_format':'0.000"%"','font_color':'#334155','valign':'top'}),
 'pctin':w.add_format({'num_format':'0.0"%"','font_color':blue,'bg_color':'#DBEAFE'}),
 'ok':w.add_format({'bg_color':'#DCFCE7','font_color':green}),
 'bad':w.add_format({'bg_color':'#FEE2E2','font_color':'#B91C1C'}),
 'warn':w.add_format({'bg_color':'#FEF3C7','font_color':orange,'text_wrap':True}),
 'note':w.add_format({'bg_color':light,'font_color':'#334155','text_wrap':True,'valign':'top'}),
 'count':w.add_format({'num_format':'0','font_color':'#334155','align':'center','valign':'top'}),
 'link':w.add_format({'font_color':blue,'underline':True}),
}
summary=w.add_worksheet('Resumo');cat=w.add_worksheet('Catalogo');sp=w.add_worksheet('Especies');sz=w.add_worksheet('Tamanhos');params=w.add_worksheet('Parametros');res=w.add_worksheet('Reserva eventos');read=w.add_worksheet('Leia-me');sources=w.add_worksheet('Fontes')
for sheet in [summary,cat,sp,sz,params,res,read,sources]:sheet.hide_gridlines(2);sheet.set_zoom(90);sheet.set_tab_color(blue);sheet.set_landscape();sheet.fit_to_pages(1,0);sheet.set_paper(9)
def header(sheet,title,subtitle,last=9):
 sheet.merge_range(0,0,1,last,title,fmt['title']);sheet.set_row(0,26);sheet.set_row(1,15);sheet.merge_range(2,0,3,last,subtitle,fmt['sub']);sheet.set_row(2,22);sheet.set_row(3,20)
header(params,'Parâmetros de equilíbrio','Células azuis são parâmetros propostos editáveis. Não alteram o servidor. As fórmulas da planilha usam estes valores.',6)
params.set_column('A:A',37);params.set_column('B:B',15);params.set_column('C:G',20)
settings=[('Multiplicador mínimo',.70,'Mínimo = base × este fator.'),('Pequeno termina em',.90,'Pequeno: mínimo até 90% da base.'),('Normal: parcela da folga',.25,'Normal termina em base + 25% × (máximo − base).'),('Grande: parcela da folga',.65,'Grande termina em base + 65% × (máximo − base).'),('Gigante: parcela da folga',.90,'Recordista começa acima deste limite até o teto.'),('Fator máximo: base até 0,5 m',2.0,'Pequenos podem variar mais proporcionalmente.'),('Fator máximo: até 1,0 m',1.75,'Ex.: Magikarp 0,90 m → teto calculado 1,50 m.'),('Fator máximo: até 2,0 m',1.6,'Variação moderada.'),('Fator máximo: até 5,0 m',1.4,'Evita crescimento exagerado de espécies médias/grandes.'),('Fator máximo: acima de 5,0 m',1.25,'Gigantes naturais continuam grandes, sem multiplicar por 5.')]
for i,(label,v,note) in enumerate(settings,5):params.write(i,0,label,fmt['text']);params.write_number(i,1,v,fmt['input']);params.merge_range(i,2,i,6,note,fmt['text']);params.set_row(i,31)
params.data_validation(5,1,14,1,{'validate':'decimal','criteria':'between','minimum':.1,'maximum':3})
# Category masses, rows 19..22 (Excel 20..23)
params.write_row(18,0,['Grupo','Chance total (%)','Significado'],fmt['head'])
for i,(c,m) in enumerate(zip(classes,masses),19):params.write(i,0,c,fmt['text']);params.write_number(i,1,m,fmt['pctin']);params.merge_range(i,2,i,6,'Dividida entre as espécies desse grupo dentro de cada tier.',fmt['text'])
params.write(23,0,'Soma dos grupos',fmt['head']);params.write_formula(23,1,'=SUM(B20:B23)',fmt['pct'],100);params.conditional_format('B24',{'type':'cell','criteria':'not between','minimum':99.999999,'maximum':100.000001,'format':fmt['bad']})
header(sz,'Perfis de tamanho','Perfis padrão de cada tier para esta simulação. Na implementação futura, a área poderá escolher outro perfil sem mudar as espécies nem seus tetos.',8);sz.set_column('A:A',10);sz.set_column('B:F',15);sz.set_column('G:G',16);sz.set_column('H:I',26)
sz.write_row(5,0,['Perfil padrão','Pequeno %','Normal %','Grande %','Gigante %','Recordista %','Soma %','Mín. nível sugerido','Identidade'],fmt['head']);sz.set_row(5,35)
for t in range(1,5):
 rr=5+t;sz.write_number(rr,0,t,fmt['count'])
 for c,v in enumerate(profiles[t],1):sz.write_number(rr,c,v,fmt['pctin'])
 sz.write_formula(rr,6,f'=SUM(B{rr+1}:F{rr+1})',fmt['pct'],100);sz.write_number(rr,7,levels[t],fmt['inputint']);sz.write(rr,8,themes[t],fmt['text'])
sz.data_validation('B7:F10',{'validate':'decimal','criteria':'between','minimum':0,'maximum':100});sz.conditional_format('G7:G10',{'type':'cell','criteria':'not between','minimum':99.999999,'maximum':100.000001,'format':fmt['bad']});sz.freeze_panes(6,1)
sz.merge_range('A13:I15','ATENÇÃO: no mod atual, gigante começa em 1,6× e recordista em 2,1× para todos. Esta proposta substitui isso por faixas relativas ao teto individual. Assim, um Magikarp pode ser recordista com 1,45–1,50 m, sem precisar passar de 1,89 m.',fmt['warn'])
# Main species data table, rows begin Excel 7.
header(sp,'Espécies — fórmulas e limites','Edite pesos e tetos manuais nas células azuis. Alturas-base vêm do JAR. Tiers/habitats e raridades são decisões propostas, não spawnrates oficiais.',23)
cols=['Tier','Pokémon','ID Cobblemon','Grupo','Ambiente sugerido','Peso no grupo','Chance %','Base (m)','Fator máx.','Mínimo (m)','Máx. calculado (m)','Teto manual (m)','Máx. final (m)','Pequeno até (m)','Normal até (m)','Grande até (m)','Gigante até (m)','Recordista até (m)','Média prevista (m)','Fator preço² médio','Máximo / base','Observações','Implementado no JAR','Fonte no JAR']
sp.set_column('A:A',7);sp.set_column('B:B',16);sp.set_column('C:C',24);sp.set_column('D:D',12);sp.set_column('E:E',27);sp.set_column('F:U',15);sp.set_column('V:V',62);sp.set_column('W:W',17);sp.set_column('X:X',62);sp.set_row(5,44)
start=6;end=start+len(rows)-1;last=end+1
sp.add_table(5,0,end,23,{'name':'CatalogoEspecies','style':'Table Style Medium 2','columns':[{'header':c} for c in cols]});sp.freeze_panes(6,3)
for i,d in enumerate(rows,start):
 er=i+1;t=d['tier'];prof=6+t;groupidx=classes.index(d['group']);catparam=20+groupidx
 vals=[t,d['name'],'cobblemon:'+d['id'],d['group'],d['habitat'],d['weight']]
 for col,v in enumerate(vals):sp.write(i,col,v,fmt['inputint'] if col in [0,5] else fmt['text'])
 formulas={
 6:(f'=IFERROR(INDEX(Parametros!$B$20:$B$23,MATCH(D{er},Parametros!$A$20:$A$23,0))*F{er}/SUMIFS($F$7:$F${last},$A$7:$A${last},A{er},$D$7:$D${last},D{er}),0)',d['chance']),
 8:(f'=IF(H{er}<=0.5,Parametros!$B$11,IF(H{er}<=1,Parametros!$B$12,IF(H{er}<=2,Parametros!$B$13,IF(H{er}<=5,Parametros!$B$14,Parametros!$B$15))))',d['factor']),
 9:(f'=ROUND(H{er}*Parametros!$B$6,3)',d['min']),
 10:(f'=ROUNDDOWN(H{er}*I{er},1)',d['max']),
 12:(f'=IF(L{er}="",K{er},L{er})',d['max']),
 13:(f'=H{er}*Parametros!$B$7',d['bands'][1]),
 14:(f'=H{er}+(M{er}-H{er})*Parametros!$B$8',d['bands'][2]),
 15:(f'=H{er}+(M{er}-H{er})*Parametros!$B$9',d['bands'][3]),
 16:(f'=H{er}+(M{er}-H{er})*Parametros!$B$10',d['bands'][4]),
 17:(f'=M{er}',d['max']),
 20:(f'=M{er}/H{er}',d['max']/d['base'])}
 sp.write_number(i,7,d['base'],fmt['num']);sp.write_blank(i,11,None,fmt['input'])
 for c,(formula,result) in formulas.items():sp.write_formula(i,c,formula,fmt['pct'] if c==6 else fmt['num'],result)
 bounds=['J','N','O','P','Q','R'];terms=[];squares=[]
 for k,(left,right) in enumerate(zip(bounds,bounds[1:]),1):
  prob=f'INDEX(Tamanhos!$B$7:$F$10,MATCH(A{er},Tamanhos!$A$7:$A$10,0),{k})/100'
  terms.append(f'({left}{er}+{right}{er})/2*{prob}')
  squares.append(f'({left}{er}^2+{left}{er}*{right}{er}+{right}{er}^2)/3*{prob}')
 sp.write_formula(i,18,'='+ '+'.join(terms),fmt['num'],d['mean']);sp.write_formula(i,19,'=('+'+'.join(squares)+f')/H{er}^2',fmt['num'],d['e2']);sp.write(i,21,d['note'],fmt['text']);sp.write(i,22,'Sim',fmt['ok']);sp.write(i,23,d['path'],fmt['text']);sp.set_row(i,36)
sp.data_validation(start,0,end,0,{'validate':'integer','criteria':'between','minimum':1,'maximum':4});sp.data_validation(start,3,end,3,{'validate':'list','source':classes});sp.data_validation(start,5,end,5,{'validate':'decimal','criteria':'between','minimum':.01,'maximum':100});sp.data_validation(start,11,end,11,{'validate':'decimal','criteria':'between','minimum':.01,'maximum':50,'ignore_blank':True})
sp.conditional_format(start,12,end,12,{'type':'formula','criteria':'=OR(M7<=H7,J7>=N7,N7>=O7,O7>=P7,P7>=Q7,Q7>=R7)','format':fmt['bad']});sp.conditional_format(start,6,end,6,{'type':'data_bar','bar_color':aqua});sp.conditional_format(start,18,end,18,{'type':'data_bar','bar_color':'#93C5FD'})
header(cat,'Catálogo de revisão','Lista pronta para conferir. As fórmulas detalhadas ficam em “Especies”; use os filtros para ver um tier. Tamanhos em metros de referência, não comprimento medido do modelo.',9)
cat.set_column('A:A',7);cat.set_column('B:B',17);cat.set_column('C:C',12);cat.set_column('D:H',14);cat.set_column('I:I',27);cat.set_column('J:J',67);cat.set_row(5,40)
catcols=['Tier','Pokémon','Grupo','Chance %','Mínimo (m)','Base (m)','Média prevista (m)','Máximo rígido (m)','Ambiente','Observação']
cat.add_table(5,0,5+len(rows),9,{'name':'RevisaoTiers','style':'Table Style Medium 2','columns':[{'header':c} for c in catcols]});cat.freeze_panes(6,2)
for idx,d in enumerate(rows,6):
 er=idx+1;mapping=[('A',d['tier']),('B',d['name']),('D',d['group']),('G',d['chance']),('J',d['min']),('H',d['base']),('S',d['mean']),('M',d['max']),('E',d['habitat']),('V',d['note'])]
 for col,(letter,v) in enumerate(mapping):cat.write_formula(idx,col,f'=Especies!{letter}{er}',fmt['pct'] if col==3 else fmt['num'] if 4<=col<=7 else fmt['text'],v)
 cat.set_row(idx,36)
header(summary,'Zorykai Fishing — tiers e tamanhos','PROPOSTA PARA APROVAÇÃO • '+str(len(rows))+' espécies em quatro tiers • Dados-base: Cobblemon '+meta['version']+' • Nada aplicado ao mod ou ao servidor.',9)
summary.set_column('A:A',8);summary.set_column('B:B',30);summary.set_column('C:D',13);summary.set_column('E:G',20);summary.set_column('H:J',17)
summary.write_url('A5',"internal:'Catalogo'!A1",fmt['link'],'→ Abrir lista de espécies');summary.write_url('D5',"internal:'Tamanhos'!A1",fmt['link'],'→ Perfis de tamanho');summary.write_url('G5',"internal:'Leia-me'!A1",fmt['link'],'→ Critérios e limitações')
summary.write_row(6,0,['Tier','Identidade','Nível sugerido','Espécies','Soma chance %','Média tamanho (m)','Fator preço² médio'],fmt['head']);summary.set_row(6,40)
for t in range(1,5):
 rr=6+t;er=rr+1;subset=[d for d in rows if d['tier']==t];mean=sum(d['chance']*d['mean'] for d in subset)/100;e2=sum(d['chance']*d['e2'] for d in subset)/100
 summary.write_number(rr,0,t,fmt['count']);summary.write(rr,1,themes[t],fmt['text']);summary.write_number(rr,2,levels[t],fmt['text']);summary.write_formula(rr,3,f'=COUNTIF(Especies!$A$7:$A${last},A{er})',fmt['count'],len(subset));summary.write_formula(rr,4,f'=SUMIF(Especies!$A$7:$A${last},A{er},Especies!$G$7:$G${last})',fmt['pct'],100);summary.write_formula(rr,5,f'=SUMPRODUCT((Especies!$A$7:$A${last}=A{er})*Especies!$G$7:$G${last}*Especies!$S$7:$S${last})/100',fmt['num'],mean);summary.write_formula(rr,6,f'=SUMPRODUCT((Especies!$A$7:$A${last}=A{er})*Especies!$G$7:$G${last}*Especies!$T$7:$T${last})/100',fmt['num'],e2);summary.set_row(rr,30)
summary.conditional_format('E8:E11',{'type':'cell','criteria':'not between','minimum':99.999999,'maximum':100.000001,'format':fmt['bad']})
summary.merge_range('A14:J16','Magikarp: base de referência 0,90 m → mínimo 0,63 m → máximo rígido proposto 1,50 m. O teto vale mesmo em tier alto, isca de tamanho ou evento. Recordista proposto: acima de 1,44 m, até 1,50 m.',fmt['note'])
summary.merge_range('A18:J20','ANTES DE IMPLEMENTAR: hoje o Fishing não possui tier por comando nem teto individual. A raridade econômica atual depende de chance ≤ 5%; isso não deve ser reutilizado sem adaptação ao criar pools grandes. A coluna “Grupo” desta proposta deve virar raridade explícita na integração.',fmt['warn'])
chart=w.add_chart({'type':'column'});chart.add_series({'name':'Espécies por tier','categories':'=Resumo!$A$8:$A$11','values':'=Resumo!$D$8:$D$11','fill':{'color':blue},'border':{'none':True},'data_labels':{'value':True}});chart.set_title({'name':'Variedade por tier'});chart.set_legend({'none':True});chart.set_size({'width':480,'height':285});summary.insert_chart('A23',chart)
chart=w.add_chart({'type':'column','subtype':'stacked'})
for i,label in enumerate(['Pequeno','Normal','Grande','Gigante','Recordista']):chart.add_series({'name':label,'categories':'=Tamanhos!$A$7:$A$10','values':['Tamanhos',6,i+1,9,i+1]})
chart.set_title({'name':'Chance de cada faixa de tamanho (%)'});chart.set_y_axis({'min':0,'max':100});chart.set_size({'width':510,'height':285});summary.insert_chart('F23',chart)
summary.set_print_scale(70)
header(res,'Reserva para eventos — fora dos tiers regulares','Sugestões opcionais. Chance regular proposta: ZERO. Lendários/míticos/paradoxo e fósseis ficam separados para não banalizar os encontros.',8)
rescols=['Pokémon','ID','Motivo','Implementado?','Base (m)','Máximo sugerido (m)','Chance regular %','Fonte','Observação'];res.set_column('A:A',16);res.set_column('B:B',24);res.set_column('C:C',24);res.set_column('D:G',16);res.set_column('H:H',55);res.set_column('I:I',55);res.write_row(5,0,rescols,fmt['head'])
for i,s in enumerate(reserve,6):
 d=data[s];b=d['height']/10;reason='Fóssil / descoberta especial' if 'fossil' in d.get('labels',[]) else 'Lendário / mítico / paradoxo';cap=limits(b)[1]
 res.write_row(i,0,[d['name'],'cobblemon:'+s,reason,'Sim' if d.get('implemented') else 'Não',b,cap,0,paths[s],'Não incluído no sorteio regular. Somente forma padrão; revisar ambiente e apresentação.'],fmt['text']);res.set_row(i,33)
res.autofilter(5,0,5+len(reserve),8);res.freeze_panes(6,2)
header(read,'Como revisar a proposta','Planilha de planejamento. Não é arquivo de configuração compatível com a versão atual e não cria comandos ou altera o servidor.',7);read.set_column('A:H',18)
notes=[
('1. O que é dado e o que é proposta','Dado extraído: nome, ID, implemented=true e altura-base do JAR Cobblemon 1.8.3. Tiers, habitats, chances, limites, níveis e perfis são sugestões de equilíbrio para sua revisão. Não foram extraídos dos spawnrates naturais.'),
('2. Como o mod mede tamanho','Fishing 0.3.0 usa altura do Pokémon / 10 × multiplicador, e não comprimento horizontal medido do modelo. A planilha mantém essa referência para não misturar unidades. Se quiser comprimento de peixe, é necessário outro estudo/modelo.'),
('3. Fórmula do teto','Máximo = arredondar PARA BAIXO a 0,1 m (base × fator por porte). Fatores: até 0,5 m → 2,0; até 1 m → 1,75; até 2 m → 1,6; até 5 m → 1,4; acima disso → 1,25. Um teto manual pode substituir o calculado; manter acima da base para não degenerar as faixas.'),
('4. Faixas por espécie','Pequeno vai de 70% a 90% da base. Normal termina em base + 25% da folga até o teto; Grande em 65%; Gigante em 90%; Recordista vai daí até o teto. Sorteio uniforme dentro de cada faixa, depois de escolher a faixa.'),
('5. Chance das espécies','Cada tier recebe 70% Comum, 25% Incomum, 4,5% Raro e 0,5% Especial. O peso de cada espécie divide a chance do grupo. Os valores exibidos são arredondados; as fórmulas mantêm precisão para fechar 100%.'),
('6. Onde cada Pokémon vive','A coluna de ambiente é orientação temática. Os tiers compartilhados misturam habitats; um filtro opcional por tema seria uma evolução futura. Não significa que o mod atual já filtra automaticamente rios, mares ou praias.'),
('7. Tier não é tamanho absoluto','Um tier alto pode favorecer exemplares grandes dentro dos limites de cada espécie. O teto de Magikarp continua 1,50 m, enquanto o de Wailord é 18,10 m por ter base de 14,50 m. Espécies enormes precisam de áreas amplas. Os perfis são padrões de simulação: uma área futura poderá combinar espécies do tier 1 com o perfil 4, mantendo os mesmos tetos.'),
('8. Buffs não podem furar o teto','Na implementação futura, iscas/eventos devem favorecer a faixa de tamanho ou redistribuir probabilidade antes do sorteio final. Nunca multiplicar o tamanho já sorteado sem reaplicar o limite. Evitar truncar muitos resultados no teto: isso criaria pilhas de recordes idênticos.'),
('9. Economia e captura precisam de adaptação','Atualmente a chance ≤5% define raridade econômica e o preço cresce com multiplicador². Uma lista com muitas espécies pode fazer todas parecerem raras por esse critério. Usar grupo explícito por espécie na futura integração e revisar a dificuldade de captura quando depender de tamanho relativo. Missões de capturar espécies específicas também devem ser revistas se a chance mudar; não alterei essas configurações.'),
('10. Fator preço²','É E[(tamanho/base)²], calculado pela mistura das faixas propostas; não é preço final nem ganho por hora. Não inclui shiny, espécies com preço próprio, chance de captura, tesouro, falhas, tempo ou cap monetário. Hoje, uniforme 0,6–2,5 gera fator médio 2,7033 antes de buffs.'),
('11. Formas e espécies extras','Somente formas padrão do JAR inspecionado. Wishiwashi escola, Palafin herói, regionais, megas e outras variantes não têm limites próprios nesta proposta. Addons/datapacks do seu servidor não foram fornecidos nem validados.'),
('12. O que editar','Para revisar: use Catalogo e filtre Tier. Para calcular: edite pesos e tetos manuais em Especies; perfis em Tamanhos; fatores em Parametros. Campos azuis são entradas. Confira que cada tier e cada perfil somam 100%. O JSON acompanhante é proposta, não instalar em config/.'),
('13. Estado da implementação','Nada aplicado ao código de runtime, ao banco ou à produção. /pescaadmin area tier e /pescaadmin area tamanho são ideias para uma próxima implementação após sua revisão. O limite visual e o tamanho registrado também precisarão ser unificados na integração para não contradizer a lore.'),
]
for idx,(title,body) in enumerate(notes):
 rr=5+idx*4;read.merge_range(rr,0,rr,7,title,fmt['head']);read.merge_range(rr+1,0,rr+3,7,body,fmt['note']);read.set_row(rr+1,24);read.set_row(rr+2,23);read.set_row(rr+3,23)
header(sources,'Fontes e auditoria','Referência técnica dos dados usados. Não é uma garantia de desempenho, captura ou preço no servidor real.',6);sources.set_column('A:A',27);sources.set_column('B:G',20)
audit=[('Data da proposta','28/11/2026'),('Versão do Cobblemon',meta['version']),('SHA-256 do JAR',hashlib.sha256(jar.read_bytes()).hexdigest()),('Espécies do JAR',len(data)),('Espécies da proposta',len(rows)),('Espécies implementadas',sum(data[d['id']].get('implemented') is True for d in rows)),('Duplicidades na proposta',len(rows)-len({d['id'] for d in rows})),('Lendários nos tiers regulares',0),('Escopo','Cobblemon base; sem espécies adicionais de datapacks.'),('Altura-base','data/cobblemon/species/**.json → height / 10'),('Fórmula atual do mod','GameEngine.java spawn()/complete(); FishingMod.java spawnConfigured().'),('Repositório','https://github.com/istwi/Zorykai-Fishing'),('Versão de referência Fishing','0.3.0-beta.1'),('Natureza dos limites','Sugestão de design; não são máximos oficiais do Cobblemon.')]
for idx,(key,value) in enumerate(audit,5):sources.write(idx,0,key,fmt['head']);sources.merge_range(idx,1,idx,6,value,fmt['text']);sources.set_row(idx,34)
summary.activate();w.close()
# Standalone review CSV and concise guide.
import csv
with (out/'catalogo-tiers.csv').open('w',encoding='utf-8-sig',newline='') as f:
 wr=csv.writer(f,delimiter=';');wr.writerow(['Tier','Pokemon','ID','Grupo','Chance %','Minimo m','Base m','Media m','Maximo rigido m','Ambiente'])
 for d in rows:wr.writerow([d['tier'],d['name'],'cobblemon:'+d['id'],d['group'],f"{d['chance']:.6f}".replace('.',','),str(d['min']).replace('.',','),str(d['base']).replace('.',','),f"{d['mean']:.4f}".replace('.',','),str(d['max']).replace('.',','),d['habitat']])
report={'species':len(rows),'tiers':{t:{'count':sum(d['tier']==t for d in rows),'chanceSum':sum(d['chance'] for d in rows if d['tier']==t)} for t in GROUPS},'sourceVersion':meta['version'],'sourceSha256':hashlib.sha256(jar.read_bytes()).hexdigest(),'allImplemented':True,'runtimeChanged':False,'proposalOnly':True}
(out/'validacao.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n')
print(json.dumps(report,indent=2));print('Workbook',out/'Zorykai-Fishing-Tiers-e-Tamanhos.xlsx')
for s in ['magikarp','feebas','gyarados','milotic','lapras','wailord','dondozo']:
 d=next(d for d in rows if d['id']==s);print(s,d['tier'],d['base'],d['min'],d['max'],d['mean'])
