from pathlib import Path
import argparse
import csv
import hashlib
import json
import math
import zipfile

import xlsxwriter


parser = argparse.ArgumentParser(description="Gera a proposta de seis areas do Zorykai Fishing")
parser.add_argument("--source", type=Path, required=True, help="proposta-tiers.json da versao anterior")
parser.add_argument("--cobblemon", type=Path, required=True, help="JAR do Cobblemon 1.8.3")
parser.add_argument("--out", type=Path, required=True)
args = parser.parse_args()
args.out.mkdir(parents=True, exist_ok=True)

source = json.loads(args.source.read_text(encoding="utf-8"))
species_by_id = {row["id"]: row for row in source["species"]}

AREAS = {
    1: {
        "name": "Lago Florestal",
        "print": 2,
        "theme": "Primeiro lago; aprendizado e peixes pequenos",
        "ids": "magikarp goldeen poliwag barboach tympole buizel chewtle psyduck finneon remoraid krabby corphish seaking floatzel golduck lumineon kingler octillery drednaw".split(),
        "sizes": [30, 58, 10, 1.8, 0.2],
    },
    2: {
        "name": "Lago das Cerejeiras",
        "print": 4,
        "theme": "Segundo lago; variedade, beleza e primeiros raros",
        "ids": "wooper lotad surskit slowpoke marill azurill tadbulb dewpider feebas squirtle staryu luvdisc clamperl azumarill slowbro vaporeon blastoise milotic slowking".split(),
        "sizes": [24, 58, 14, 3.6, 0.4],
    },
    3: {
        "name": "Lago Tropical",
        "print": 1,
        "theme": "Terceiro lago; agua salgada, costeiros e nadadores rapidos",
        "ids": "tentacool wingull horsea arrokuda basculin clauncher skrelp wimpod wishiwashi pyukumuku binacle bruxish popplio pelipper seadra tentacruel barraskewda primarina sharpedo".split(),
        "sizes": [18, 57, 19, 5.4, 0.6],
    },
    4: {
        "name": "Pantano Antigo",
        "print": 3,
        "theme": "Quarto lago; agua turva, anfibios e especies resistentes",
        "ids": "shellos chinchou poliwhirl mudkip totodile froakie sobble quagsire whiscash lombre palpitoad lanturn crawdaunt gastrodon jellicent araquanid seismitoad ludicolo swampert".split(),
        "sizes": [14, 55, 23, 7, 1],
    },
    5: {
        "name": "Lago Glacial",
        "print": 5,
        "theme": "Quinto lago; especies frias, fortes e raras",
        "ids": "seel spheal piplup oshawott shellder mantyke qwilfish ducklett dewgong sealeo walrein lapras empoleon samurott inteleon alomomola basculegion cramorant relicanth".split(),
        "sizes": [9, 52, 29, 8.5, 1.5],
    },
    6: {
        "name": "Caverna Cristalina",
        "print": 6,
        "theme": "Area final; predadores, gigantes e especies especiais",
        "ids": "barbaracle clawitzer dondozo dragalge feraligatr golisopod gorebyss greninja gyarados huntail kingdra mantine palafin politoed poliwrath quaquaval quaxly tatsugiri veluza wailord".split(),
        "sizes": [5, 48, 33, 11, 3],
    },
}

AREA_BONUS = {1: 0, 2: 3, 3: 6, 4: 9, 5: 12, 6: 15}
RARITY_BONUS = {"Comum": 0, "Incomum": 4, "Raro": 8, "Especial": 12}
GROUP_MASS = {"Comum": 70, "Incomum": 24, "Raro": 5, "Especial": 1}
SIZE_LABELS = ["Pequeno", "Normal", "Grande", "Gigante", "Recordista"]


def jar_sha256(path):
    h = hashlib.sha256()
    with path.open("rb") as fh:
        for chunk in iter(lambda: fh.read(1024 * 1024), b""):
            h.update(chunk)
    return h.hexdigest()


with zipfile.ZipFile(args.cobblemon) as jar:
    metadata = json.loads(jar.read("fabric.mod.json"))
    raw_species = {}
    for name in jar.namelist():
        if "/species/" in name and name.endswith(".json"):
            sid = name.rsplit("/", 1)[-1][:-5]
            raw_species[sid] = json.loads(jar.read(name))


def expected_size(row, profile):
    b = row["bands"]
    means = [(b[i] + b[i + 1]) / 2 for i in range(5)]
    return sum(p / 100 * value for p, value in zip(profile, means))


def mass_score(kg):
    return min(22, 6 * math.log10(1 + kg))


def difficulty(row, area, rarity, metres):
    span = max(0.001, row["max"] - row["min"])
    size_score = max(0, min(18, (metres - row["min"]) / span * 18))
    return 10 + AREA_BONUS[area] + mass_score(row["weightKg"]) + size_score + RARITY_BONUS[rarity]


def metrics(d):
    return {
        "safeTension": max(62, 90 - 0.25 * d),
        "targetProgress": 100 + 0.9 * d,
        "clickProgress": max(10, 20 - 0.08 * d),
        "calmSeconds": max(3, 7 - 0.045 * d),
        "swimSeconds": 1.2 + 0.025 * d,
        "swimLoss": 2 + 0.09 * d,
        "slackDelay": max(1.5, 3.5 - 0.02 * d),
        "slackLoss": 1.5 + 0.06 * d,
    }


rows = []
seen = set()
for area, definition in AREAS.items():
    scored = []
    for sid in definition["ids"]:
        if sid not in species_by_id or sid not in raw_species:
            raise SystemExit(f"Especie ausente na fonte: {sid}")
        original = dict(species_by_id[sid])
        raw = raw_species[sid]
        original["weightKg"] = raw.get("weight", 0) / 10
        score = mass_score(original["weightKg"]) + original["base"] * 1.5
        scored.append((score, original))
        if sid in seen:
            raise SystemExit(f"Especie duplicada: {sid}")
        seen.add(sid)
    scored.sort(key=lambda item: (item[0], item[1]["id"]))
    count = len(scored)
    common = 10
    uncommon = 5 if count == 19 else 6
    rare = 3
    labels = ["Comum"] * common + ["Incomum"] * uncommon + ["Raro"] * rare + ["Especial"]
    if len(labels) != count:
        raise SystemExit(f"Distribuicao invalida na area {area}: {count}")
    group_counts = {g: labels.count(g) for g in GROUP_MASS}
    for (_, row), rarity in zip(scored, labels):
        row.update(
            {
                "area": area,
                "areaName": definition["name"],
                "print": definition["print"],
                "areaRarity": rarity,
                "areaChance": GROUP_MASS[rarity] / group_counts[rarity],
                "expectedSize": expected_size(row, definition["sizes"]),
            }
        )
        row["difficultyMin"] = difficulty(row, area, rarity, row["min"])
        row["difficultyMean"] = difficulty(row, area, rarity, row["expectedSize"])
        row["difficultyMax"] = difficulty(row, area, rarity, row["max"])
        rows.append(row)

all_source = set(species_by_id)
if seen != all_source:
    raise SystemExit(f"Cobertura incorreta. Faltando={sorted(all_source-seen)} extras={sorted(seen-all_source)}")


json_output = {
    "status": "PROPOSTA PARA REVISAO - AINDA NAO E CONFIGURACAO INSTALAVEL",
    "source": {
        "cobblemonVersion": metadata.get("version"),
        "cobblemonJarSha256": jar_sha256(args.cobblemon),
        "weightUnit": "weight/10 = kg",
        "sizeUnit": "height/10 = metros de referencia; nao e comprimento horizontal medido",
    },
    "worldRules": {
        "unconfiguredWater": "Tier 1",
        "naturalPokemonSpawnsInFishingDimension": False,
        "onlyFishedPokemonAppear": True,
        "boundaryParticles": False,
    },
    "areas": [
        {
            "order": area,
            "name": definition["name"],
            "sourcePrint": definition["print"],
            "theme": definition["theme"],
            "sizeProfilePercent": dict(zip(SIZE_LABELS, definition["sizes"])),
        }
        for area, definition in AREAS.items()
    ],
    "species": rows,
    "minigameModel": {
        "difficulty": "10 + areaBonus + min(22,6*log10(1+kg)) + normalizedSize*18 + rarityBonus",
        "lineBreak": "100 tension",
        "fishSwims": "during swim phase progress falls; if player stays idle after slack delay, progress also falls",
        "note": "Logarithmic weight prevents giant species from becoming mathematically impossible.",
    },
}

(args.out / "proposta-seis-areas.json").write_text(json.dumps(json_output, ensure_ascii=False, indent=2), encoding="utf-8")

with (args.out / "pools-seis-areas.csv").open("w", encoding="utf-8-sig", newline="") as fh:
    writer = csv.writer(fh)
    writer.writerow(["Ordem", "Area", "Print", "Especie", "Grupo", "Chance %", "Peso kg", "Min m", "Medio m", "Max m", "Dificuldade media"])
    for row in sorted(rows, key=lambda x: (x["area"], -x["areaChance"], x["name"])):
        writer.writerow([row["area"], row["areaName"], row["print"], row["name"], row["areaRarity"], row["areaChance"], row["weightKg"], row["min"], row["expectedSize"], row["max"], row["difficultyMean"]])


book_path = args.out / "Zorykai-Fishing-6-Areas-e-Dificuldade.xlsx"
wb = xlsxwriter.Workbook(book_path)
wb.set_properties({"title": "Zorykai Fishing - seis areas e dificuldade", "comments": "Proposta para revisao; ainda nao implementada"})

navy = "#182856"
blue = "#2D68E8"
light = "#EAF2FD"
gold = "#F5C542"
gray = "#667085"
red = "#C62828"
green = "#18864B"
white = "#FFFFFF"

fmt_title = wb.add_format({"bold": True, "font_size": 20, "font_color": white, "bg_color": navy, "align": "left", "valign": "vcenter"})
fmt_subtitle = wb.add_format({"font_color": "#344054", "font_size": 10})
fmt_head = wb.add_format({"bold": True, "font_color": white, "bg_color": blue, "border": 1, "align": "center", "valign": "vcenter", "text_wrap": True})
fmt_text = wb.add_format({"border": 1, "border_color": "#D0D5DD", "valign": "top"})
fmt_center = wb.add_format({"border": 1, "border_color": "#D0D5DD", "align": "center", "valign": "vcenter"})
fmt_num = wb.add_format({"border": 1, "border_color": "#D0D5DD", "num_format": "0.00", "align": "center"})
fmt_pct = wb.add_format({"border": 1, "border_color": "#D0D5DD", "num_format": '0.0000"%"', "align": "center"})
fmt_note = wb.add_format({"bg_color": "#FFF4CE", "font_color": "#8A4B00", "text_wrap": True, "valign": "vcenter"})
fmt_good = wb.add_format({"bg_color": "#E8F5E9", "font_color": green, "text_wrap": True, "valign": "vcenter"})
fmt_link = wb.add_format({"font_color": "#0563C1", "underline": True})
fmt_input = wb.add_format({"border": 1, "border_color": "#D0D5DD", "font_color": "#0057D9", "bg_color": "#F4F8FF", "num_format": "0.00", "align": "center"})


def title(ws, text, subtitle):
    ws.set_row(0, 34)
    ws.merge_range(0, 0, 0, 12, text, fmt_title)
    ws.merge_range(1, 0, 1, 12, subtitle, fmt_subtitle)
    ws.freeze_panes(3, 0)


summary = wb.add_worksheet("Resumo")
summary.hide_gridlines(2)
summary.set_landscape()
summary.set_paper(9)
summary.fit_to_pages(1, 1)
summary.set_margins(0.25, 0.25, 0.35, 0.35)
summary.print_area(0, 0, 19, 12)
title(summary, "Zorykai Fishing — seis areas", "PROPOSTA PARA REVISAO • 115 especies • peso + tamanho afetam o minigame • nenhuma mudanca aplicada ao mod")
summary.write_url(3, 0, "internal:'Pools por area'!A1", fmt_link, "→ Ver especies e chances")
summary.write_url(3, 4, "internal:'Dificuldade'!A1", fmt_link, "→ Ver calculos do minigame")
summary.write_url(3, 8, "internal:'Mecanica'!A1", fmt_link, "→ Entender tensao e progresso")
headers = ["Ordem", "Area", "Print", "Identidade", "Especies", "Chance", "Tam. medio", "Dif. media", "Dif. maxima"]
for col, header in enumerate(headers):
    summary.write(5, col, header, fmt_head)
for index, (area, definition) in enumerate(AREAS.items(), 6):
    selected = [r for r in rows if r["area"] == area]
    mean_size = sum(r["expectedSize"] * r["areaChance"] / 100 for r in selected)
    mean_diff = sum(r["difficultyMean"] * r["areaChance"] / 100 for r in selected)
    summary.write(index, 0, area, fmt_center)
    summary.write(index, 1, definition["name"], fmt_text)
    summary.write(index, 2, definition["print"], fmt_center)
    summary.write(index, 3, definition["theme"], fmt_text)
    summary.write(index, 4, len(selected), fmt_center)
    summary.write_number(index, 5, sum(r["areaChance"] for r in selected), fmt_pct)
    summary.write_number(index, 6, mean_size, fmt_num)
    summary.write_number(index, 7, mean_diff, fmt_num)
    summary.write_number(index, 8, max(r["difficultyMax"] for r in selected), fmt_num)
summary.merge_range(14, 0, 15, 8, "Fora de uma area especial: pesca Tier 1. Na dimensao de pesca, spawns naturais de Pokemon ficam bloqueados; somente encontros gerados pela pesca aparecem. Particulas de demarcacao ficam desligadas.", fmt_good)
summary.merge_range(17, 0, 19, 8, "A dificuldade usa peso em escala logaritmica e tamanho sorteado dentro do limite individual. Isso faz um exemplar recordista exigir mais controle sem tornar Wailord/Dondozo impossiveis. Peixes fortes nadam por mais tempo e retiram progresso; ficar parado com a linha frouxa tambem retira progresso.", fmt_note)
summary.set_column("A:A", 9)
summary.set_column("B:B", 23)
summary.set_column("C:C", 8)
summary.set_column("D:D", 50)
summary.set_column("E:I", 14)

areas_ws = wb.add_worksheet("Areas")
areas_ws.hide_gridlines(2)
title(areas_ws, "Areas e perfis de tamanho", "A ordem segue os prints indicados pelo usuario. Os percentuais azuis sao propostas editaveis e somam 100 por area.")
area_headers = ["Ordem", "Nome", "Print", "Tema", "Pequeno %", "Normal %", "Grande %", "Gigante %", "Recordista %", "Soma %", "Bonus dificuldade"]
for col, header in enumerate(area_headers):
    areas_ws.write(3, col, header, fmt_head)
for row_index, (area, definition) in enumerate(AREAS.items(), 4):
    areas_ws.write(row_index, 0, area, fmt_center)
    areas_ws.write(row_index, 1, definition["name"], fmt_text)
    areas_ws.write(row_index, 2, definition["print"], fmt_center)
    areas_ws.write(row_index, 3, definition["theme"], fmt_text)
    for offset, value in enumerate(definition["sizes"], 4):
        areas_ws.write_number(row_index, offset, value, fmt_input)
    areas_ws.write_formula(row_index, 9, f"=SUM(E{row_index+1}:I{row_index+1})", fmt_pct, sum(definition["sizes"]))
    areas_ws.write_number(row_index, 10, AREA_BONUS[area], fmt_input)
areas_ws.conditional_format(4, 9, 9, 9, {"type": "cell", "criteria": "!=", "value": 100, "format": wb.add_format({"bg_color": "#FFD9D9", "font_color": red})})
areas_ws.set_column("A:A", 9)
areas_ws.set_column("B:B", 24)
areas_ws.set_column("C:C", 8)
areas_ws.set_column("D:D", 55)
areas_ws.set_column("E:K", 15)

pool = wb.add_worksheet("Pools por area")
pool.hide_gridlines(2)
title(pool, "Especies por area", "Cada especie aparece uma vez nesta proposta. Chance soma 100% dentro de cada area; grupo e chance sao proprios da pesca.")
pool_headers = ["Ordem", "Area", "Print", "ID", "Especie", "Grupo", "Chance %", "Peso kg", "Base ref. m", "Min m", "Medio m", "Max m", "Somente profundo?"]
for col, header in enumerate(pool_headers):
    pool.write(3, col, header, fmt_head)
sorted_rows = sorted(rows, key=lambda x: (x["area"], ["Comum", "Incomum", "Raro", "Especial"].index(x["areaRarity"]), x["name"]))
for index, row in enumerate(sorted_rows, 4):
    values = [row["area"], row["areaName"], row["print"], row["id"], row["name"], row["areaRarity"]]
    for col, value in enumerate(values):
        pool.write(index, col, value, fmt_center if col in (0, 2) else fmt_text)
    pool.write_number(index, 6, row["areaChance"], fmt_pct)
    pool.write_number(index, 7, row["weightKg"], fmt_num)
    pool.write_number(index, 8, row["base"], fmt_num)
    pool.write_number(index, 9, row["min"], fmt_num)
    pool.write_number(index, 10, row["expectedSize"], fmt_num)
    pool.write_number(index, 11, row["max"], fmt_num)
    deep = "Sim" if row["id"] in {"wailord", "dondozo", "gyarados", "milotic", "lapras"} else "Nao"
    pool.write(index, 12, deep, fmt_center)
pool.autofilter(3, 0, 3 + len(sorted_rows), len(pool_headers) - 1)
pool.freeze_panes(4, 0)
pool.set_column("A:A", 8)
pool.set_column("B:B", 24)
pool.set_column("C:C", 8)
pool.set_column("D:E", 20)
pool.set_column("F:F", 13)
pool.set_column("G:L", 13)
pool.set_column("M:M", 18)

difficulty_ws = wb.add_worksheet("Dificuldade")
difficulty_ws.hide_gridlines(2)
title(difficulty_ws, "Dificuldade por especie e tamanho", "Tres cenarios por especie: minimo, medio esperado e maximo. A escala serve para comparar e alimentar as regras futuras.")
diff_headers = ["Area", "Especie", "Cenario", "Tamanho m", "Peso kg", "Grupo", "Dificuldade", "Tensao segura ate", "Progresso necessario", "Progresso/clique", "Cliques ideais", "Calmo s", "Nadando s", "Perda nadando/s", "Espera frouxa s", "Perda frouxa/s"]
for col, header in enumerate(diff_headers):
    difficulty_ws.write(3, col, header, fmt_head)
diff_data = []
for row in sorted_rows:
    for scenario, metres, d in [("Minimo", row["min"], row["difficultyMin"]), ("Medio", row["expectedSize"], row["difficultyMean"]), ("Maximo", row["max"], row["difficultyMax"])]:
        diff_data.append((row, scenario, metres, d, metrics(d)))
for index, (row, scenario, metres, d, rule) in enumerate(diff_data, 4):
    difficulty_ws.write(index, 0, row["area"], fmt_center)
    difficulty_ws.write(index, 1, row["name"], fmt_text)
    difficulty_ws.write(index, 2, scenario, fmt_center)
    difficulty_ws.write_number(index, 3, metres, fmt_num)
    difficulty_ws.write_number(index, 4, row["weightKg"], fmt_num)
    difficulty_ws.write(index, 5, row["areaRarity"], fmt_center)
    difficulty_ws.write_number(index, 6, d, fmt_num)
    difficulty_ws.write_number(index, 7, rule["safeTension"], fmt_num)
    difficulty_ws.write_number(index, 8, rule["targetProgress"], fmt_num)
    difficulty_ws.write_number(index, 9, rule["clickProgress"], fmt_num)
    difficulty_ws.write_number(index, 10, rule["targetProgress"] / rule["clickProgress"], fmt_num)
    difficulty_ws.write_number(index, 11, rule["calmSeconds"], fmt_num)
    difficulty_ws.write_number(index, 12, rule["swimSeconds"], fmt_num)
    difficulty_ws.write_number(index, 13, rule["swimLoss"], fmt_num)
    difficulty_ws.write_number(index, 14, rule["slackDelay"], fmt_num)
    difficulty_ws.write_number(index, 15, rule["slackLoss"], fmt_num)
difficulty_ws.autofilter(3, 0, 3 + len(diff_data), len(diff_headers) - 1)
difficulty_ws.freeze_panes(4, 0)
difficulty_ws.set_column("A:A", 8)
difficulty_ws.set_column("B:B", 19)
difficulty_ws.set_column("C:C", 11)
difficulty_ws.set_column("D:P", 15)

mechanic = wb.add_worksheet("Mecanica")
mechanic.hide_gridlines(2)
title(mechanic, "Modelo do minigame", "O peixe alterna entre CALMO e NADANDO. Acoes erradas ou demora fazem o progresso recuar.")
mechanic_headers = ["Estado", "O que acontece", "Acao do jogador", "Tensao", "Progresso"]
for col, header in enumerate(mechanic_headers):
    mechanic.write(3, col, header, fmt_head)
states = [
    ("Fisgada", "Comeca com 20 de tensao e 0 de progresso.", "Comecar a recolher sem clicar em excesso.", "Estavel", "0"),
    ("Calmo", "Janela curta para avancar.", "Recolher em ritmo controlado.", "Sobe por clique e cai com o tempo.", "Aumenta"),
    ("Nadando", "O peixe puxa; especies dificeis ficam mais tempo neste estado.", "Dar folga e esperar o aviso terminar.", "Clicar multiplica a tensao.", "Diminui por segundo"),
    ("Linha frouxa", "O jogador demorou alem da tolerancia sem recolher.", "Voltar a recolher.", "Baixa", "Diminui por segundo"),
    ("Linha arrebentou", "Tensao chegou a 100.", "A pesca termina.", "100", "Perde o encontro"),
    ("Concluido", "O progresso necessario foi alcancado.", "Usar a bola de pesca no encontro.", "—", "Pokemon aparece"),
]
for row_index, state in enumerate(states, 4):
    for col, value in enumerate(state):
        mechanic.write(row_index, col, value, fmt_text)
mechanic.merge_range(12, 0, 14, 4, "Formula proposta: 10 + bonus da area + peso logaritmico + tamanho normalizado + raridade. Peso usa log10 para que Wailord seja mais forte sem ficar impossivel. O tamanho usa a posicao entre o minimo e o teto daquela especie: um Magikarp recordista recebe o mesmo bonus de tamanho que outro recordista, mas seu peso-base continua muito menor.", fmt_note)
mechanic.merge_range(16, 0, 18, 4, "Balanceamento de varas: Resistencia reduz tensao recebida; Precisao aumenta progresso por clique; upgrades de tempo podem aumentar a janela CALMO. Nenhum upgrade remove a fase NADANDO nem permite passar do teto de tamanho da especie.", fmt_good)
mechanic.set_column("A:A", 20)
mechanic.set_column("B:C", 48)
mechanic.set_column("D:E", 28)

params = wb.add_worksheet("Parametros")
params.hide_gridlines(2)
title(params, "Parametros propostos", "Campos azuis sao os valores usados nesta simulacao. Alterar esta aba ainda nao muda as outras tabelas nem o mod.")
param_rows = [
    ("Dificuldade inicial", 10, "Base para qualquer peixe"),
    ("Peso maximo na dificuldade", 22, "Limita impacto de especies gigantes"),
    ("Peso: multiplicador log10", 6, "Usado em 6 × log10(1 + kg)"),
    ("Tamanho: bonus maximo", 18, "Do menor ao teto individual"),
    ("Tensao inicial", 20, "Inicio da fisgada"),
    ("Tensao de rompimento", 100, "Ao atingir, perde o encontro"),
    ("Tensao segura base", 90, "Cai 0,25 por ponto de dificuldade"),
    ("Progresso base necessario", 100, "Sobe 0,90 por ponto de dificuldade"),
    ("Progresso base por clique", 20, "Cai 0,08 por ponto de dificuldade; minimo 10"),
    ("Duracao calma base s", 7, "Cai 0,045 por dificuldade; minimo 3"),
    ("Duracao nadando base s", 1.2, "Sobe 0,025 por dificuldade"),
    ("Perda nadando base/s", 2, "Sobe 0,09 por dificuldade"),
    ("Espera linha frouxa base s", 3.5, "Cai 0,02 por dificuldade; minimo 1,5"),
    ("Perda linha frouxa base/s", 1.5, "Sobe 0,06 por dificuldade"),
]
for col, header in enumerate(["Parametro", "Valor", "Uso"]):
    params.write(3, col, header, fmt_head)
for row_index, item in enumerate(param_rows, 4):
    params.write(row_index, 0, item[0], fmt_text)
    params.write_number(row_index, 1, item[1], fmt_input)
    params.write(row_index, 2, item[2], fmt_text)
params.set_column("A:A", 35)
params.set_column("B:B", 18)
params.set_column("C:C", 70)

readme = wb.add_worksheet("Leia-me")
readme.hide_gridlines(2)
title(readme, "Como revisar esta proposta", "Planilha de planejamento. Nao substituir arquivos do servidor por este XLSX ou pelo JSON.")
notes = [
    "1. Confira em 'Pools por area' se cada especie combina com o mapa e com a ordem dos seis prints.",
    "2. Chance e grupo sao proprios de cada area. Os 115 Pokemon aparecem uma vez e cada pool soma 100%.",
    "3. Base de tamanho e peso vieram do Cobblemon 1.8.3. height/10 e referencia em metros; weight/10 e kg.",
    "4. Minimo e maximo sao tetos de equilibrio, nao medidas oficiais de comprimento do modelo 3D.",
    "5. 'Dificuldade' compara exemplar minimo, medio e maximo. Valores sao uma proposta para futura implementacao.",
    "6. Agua sem area especial usa Tier 1. Na dimensao de pesca, nenhum Pokemon natural nasce: apenas encontros pescados.",
    "7. Areas gigantes precisam de ponto profundo para Wailord, Dondozo, Gyarados, Milotic e Lapras. A planilha marca isso.",
    "8. O modelo nao foi testado em servidor real. Antes de publicar, simular jogadores com vara inicial e upgrades.",
]
for index, note in enumerate(notes, 3):
    readme.merge_range(index, 0, index, 10, note, fmt_text)
    readme.set_row(index, 28)
readme.merge_range(13, 0, 15, 10, "Decisao de seguranca do balanceamento: a dificuldade por peso e logaritmica. Usar peso linear faria Wailord ter centenas de vezes a forca de Magikarp e quebraria o minigame. O tamanho e normalizado pelo limite de cada especie para preservar recordistas sem misturar escala visual com dificuldade de forma exagerada.", fmt_note)
readme.set_column("A:K", 15)

sources = wb.add_worksheet("Fontes")
sources.hide_gridlines(2)
title(sources, "Fontes e auditoria", "Dados usados para montar a proposta.")
source_rows = [
    ("Cobblemon", metadata.get("version")),
    ("SHA-256 do JAR", jar_sha256(args.cobblemon)),
    ("Especies", len(rows)),
    ("Areas", len(AREAS)),
    ("Cobertura unica", "Sim — todas as 115 especies, sem duplicatas"),
    ("Mudanca no mod/JAR", "Nao"),
    ("Status", "Proposta para revisao"),
]
for row_index, item in enumerate(source_rows, 3):
    sources.write(row_index, 0, item[0], fmt_head)
    sources.merge_range(row_index, 1, row_index, 8, item[1], fmt_text)
sources.set_column("A:A", 24)
sources.set_column("B:I", 16)

wb.close()

validation = {
    "species": len(rows),
    "uniqueSpecies": len(seen),
    "allOriginalSpeciesCovered": seen == all_source,
    "areas": {str(area): {"count": len([r for r in rows if r["area"] == area]), "chanceSum": round(sum(r["areaChance"] for r in rows if r["area"] == area), 10)} for area in AREAS},
    "sizeProfilesSum100": all(abs(sum(a["sizes"]) - 100) < 1e-9 for a in AREAS.values()),
    "naturalSpawnsDisabledInFishingDimension": True,
    "boundaryParticles": False,
    "runtimeChanged": False,
    "proposalOnly": True,
    "cobblemonVersion": metadata.get("version"),
    "cobblemonJarSha256": jar_sha256(args.cobblemon),
}
(args.out / "validacao-seis-areas.json").write_text(json.dumps(validation, ensure_ascii=False, indent=2), encoding="utf-8")
print(json.dumps(validation, ensure_ascii=False, indent=2))
