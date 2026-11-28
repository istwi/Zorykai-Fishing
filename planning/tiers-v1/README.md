# Proposta de tiers e tamanhos — revisão v1

**Proposta para revisão. Nenhuma mudança aplicada ao mod, JAR ou servidor.** O JSON desta pasta não é uma configuração instalável na versão atual.

[Baixar planilha Excel](../../downloads/Zorykai-Fishing-Tiers-e-Tamanhos.xlsx) · [Baixar pacote completo](../../downloads/Zorykai-Fishing-Proposta-Tiers-v1.zip)

## Como revisar

- **Catalogo:** lista legível das 115 espécies; filtre pela coluna Tier para revisar cada lago.
- **Especies:** cálculos detalhados, pesos editáveis e teto manual por espécie. Campos azuis são entradas editáveis; as fórmulas recalculam no Excel/LibreOffice.
- **Tamanhos:** perfis de chances de Pequeno até Recordista. No desenho futuro, perfil de tamanho e tier de espécies poderão ser escolhidos separadamente.
- **Parametros:** coeficientes e chances por grupo. Nos campos percentuais azuis, digite `25` para 25%, não `25%`.
- **Reserva eventos:** lendários, míticos, paradoxos e fósseis separados, sem chance nos lagos normais.
- **Leia-me / Fontes:** critérios, limitações e origem dos dados.

O CSV e o JSON são fotografias da proposta inicial: editar a planilha não os atualiza automaticamente. Não altere fórmulas ao ajustar pesos/tetos.

## Cálculo de tamanho

A base B vem de `height / 10` dos dados do Cobblemon 1.8.3+1.21.1. É uma **altura de referência**, usada pelo Fishing atual; não é uma medição do comprimento horizontal do peixe nem do modelo renderizado.

Mínimo = 0,70 × B. Máximo = B × fator, arredondado **para baixo** em passos de 0,10 m:

| Base | Fator máximo |
|---|---:|
| Até 0,50 m | 2,00 |
| Até 1,00 m | 1,75 |
| Até 2,00 m | 1,60 |
| Até 5,00 m | 1,40 |
| Acima de 5,00 m | 1,25 |

Esses tetos são sugestões de equilíbrio, não máximos oficiais. Espécies maiores recebem menor ampliação proporcional para evitar tamanhos exagerados. O teto final deve valer com qualquer tier, isca ou evento; buffs devem favorecer faixas maiores sem multiplicar além dele.

| Espécie | Mínimo | Máximo sugerido |
|---|---:|---:|
| Magikarp | 0,63 m | 1,50 m |
| Feebas | 0,42 m | 1,00 m |
| Gyarados | 4,55 m | 8,10 m |
| Milotic | 4,34 m | 7,70 m |
| Lapras | 1,75 m | 3,50 m |
| Wailord | 10,15 m | 18,10 m |

Wailord e Dondozo exigem áreas grandes. Habitats na tabela são sugestões temáticas manuais; não representam um filtro automático existente. A lista usa formas padrão: outras formas precisam de regras próprias.

## Chances e economia

Cada tier distribui 70% para Comum, 25% para Incomum, 4,5% para Raro e 0,5% para Especial. Dentro de cada grupo, pesos dividem a chance. São probabilidades propostas para a pesca, não spawnrates oficiais. Todos os tiers somam 100%.

O preço atual depende do quadrado do tamanho relativo. A planilha calcula a expectativa desse fator; isso não prevê dinheiro por hora e não inclui falhas, captura, shiny, tesouros ou preços individuais. A raridade atual inferida por chance ≤5% precisará ser substituída por grupo explícito na integração, para não classificar muitas espécies comuns como raras. Missões por espécie também deverão ser revisadas após definir as chances.

## Validação

115 espécies únicas presentes e implementadas no JAR consultado; quatro pools com 20/30/32/33 espécies. Recalculação independente no LibreOffice: 2.666 células com fórmulas, zero erros e zero divergências dos resultados armazenados. Todas as faixas de tamanho são estritamente crescentes. Resumo inspecionado visualmente. Isso valida a planilha, **não o comportamento em servidor**.

Para reproduzir os dados iniciais: Python 3 + `xlsxwriter`, executando `python gerar_planilha.py --jar /caminho/cobblemon.jar --out ./gerado`. A validação independente em `validacao.json` foi feita depois da geração.

## Lista por tier

### Tier 1 — Águas tranquilas

| Espécie | Grupo | Chance | Mínimo (m) | Máximo (m) |
|---|---|---:|---:|---:|
| Magikarp | Comum | 18.6667% | 0.63 | 1.50 |
| Goldeen | Comum | 9.3333% | 0.42 | 1.00 |
| Poliwag | Comum | 9.3333% | 0.42 | 1.00 |
| Wooper | Comum | 4.6667% | 0.28 | 0.80 |
| Barboach | Comum | 4.6667% | 0.28 | 0.80 |
| Lotad | Comum | 4.6667% | 0.35 | 1.00 |
| Surskit | Comum | 4.6667% | 0.35 | 1.00 |
| Tympole | Comum | 4.6667% | 0.35 | 1.00 |
| Buizel | Comum | 4.6667% | 0.49 | 1.20 |
| Chewtle | Comum | 4.6667% | 0.21 | 0.60 |
| Psyduck | Incomum | 5.0000% | 0.56 | 1.40 |
| Slowpoke | Incomum | 5.0000% | 0.84 | 1.90 |
| Finneon | Incomum | 5.0000% | 0.28 | 0.80 |
| Remoraid | Incomum | 5.0000% | 0.42 | 1.00 |
| Marill | Incomum | 5.0000% | 0.28 | 0.80 |
| Azurill | Raro | 1.5000% | 0.14 | 0.40 |
| Tadbulb | Raro | 1.5000% | 0.21 | 0.60 |
| Dewpider | Raro | 1.5000% | 0.21 | 0.60 |
| Feebas | Especial | 0.2500% | 0.42 | 1.00 |
| Squirtle | Especial | 0.2500% | 0.35 | 1.00 |

### Tier 2 — Exploração e variedade

| Espécie | Grupo | Chance | Mínimo (m) | Máximo (m) |
|---|---|---:|---:|---:|
| Tentacool | Comum | 10.0000% | 0.63 | 1.50 |
| Wingull | Comum | 10.0000% | 0.42 | 1.00 |
| Shellder | Comum | 5.0000% | 0.21 | 0.60 |
| Krabby | Comum | 5.0000% | 0.28 | 0.80 |
| Corphish | Comum | 5.0000% | 0.42 | 1.00 |
| Shellos | Comum | 5.0000% | 0.21 | 0.60 |
| Chinchou | Comum | 5.0000% | 0.35 | 1.00 |
| Horsea | Comum | 5.0000% | 0.28 | 0.80 |
| Arrokuda | Comum | 5.0000% | 0.35 | 1.00 |
| Basculin | Comum | 5.0000% | 0.70 | 1.70 |
| Ducklett | Comum | 5.0000% | 0.35 | 1.00 |
| Poliwhirl | Comum | 5.0000% | 0.70 | 1.70 |
| Seel | Incomum | 3.1250% | 0.77 | 1.70 |
| Spheal | Incomum | 3.1250% | 0.56 | 1.40 |
| Staryu | Incomum | 3.1250% | 0.56 | 1.40 |
| Luvdisc | Incomum | 3.1250% | 0.42 | 1.00 |
| Clamperl | Incomum | 3.1250% | 0.28 | 0.80 |
| Clauncher | Incomum | 3.1250% | 0.35 | 1.00 |
| Skrelp | Incomum | 3.1250% | 0.35 | 1.00 |
| Wimpod | Incomum | 3.1250% | 0.35 | 1.00 |
| Wishiwashi | Raro | 0.9000% | 0.14 | 0.40 |
| Pyukumuku | Raro | 0.9000% | 0.21 | 0.60 |
| Binacle | Raro | 0.9000% | 0.35 | 1.00 |
| Qwilfish | Raro | 0.9000% | 0.35 | 1.00 |
| Mantyke | Raro | 0.9000% | 0.70 | 1.70 |
| Bruxish | Especial | 0.1000% | 0.63 | 1.50 |
| Totodile | Especial | 0.1000% | 0.42 | 1.00 |
| Mudkip | Especial | 0.1000% | 0.28 | 0.80 |
| Piplup | Especial | 0.1000% | 0.28 | 0.80 |
| Oshawott | Especial | 0.1000% | 0.35 | 1.00 |

### Tier 3 — Encontros raros e evoluções

| Espécie | Grupo | Chance | Mínimo (m) | Máximo (m) |
|---|---|---:|---:|---:|
| Seaking | Comum | 12.7273% | 0.91 | 2.00 |
| Quagsire | Comum | 6.3636% | 0.98 | 2.20 |
| Whiscash | Comum | 6.3636% | 0.63 | 1.50 |
| Lombre | Comum | 6.3636% | 0.84 | 1.90 |
| Palpitoad | Comum | 6.3636% | 0.56 | 1.40 |
| Floatzel | Comum | 6.3636% | 0.77 | 1.70 |
| Golduck | Comum | 6.3636% | 1.19 | 2.70 |
| Azumarill | Comum | 6.3636% | 0.56 | 1.40 |
| Lumineon | Comum | 6.3636% | 0.84 | 1.90 |
| Pelipper | Comum | 6.3636% | 0.84 | 1.90 |
| Slowbro | Incomum | 2.0833% | 1.12 | 2.50 |
| Lanturn | Incomum | 2.0833% | 0.84 | 1.90 |
| Seadra | Incomum | 2.0833% | 0.84 | 1.90 |
| Kingler | Incomum | 2.0833% | 0.91 | 2.00 |
| Crawdaunt | Incomum | 2.0833% | 0.77 | 1.70 |
| Octillery | Incomum | 2.0833% | 0.63 | 1.50 |
| Tentacruel | Incomum | 2.0833% | 1.12 | 2.50 |
| Dewgong | Incomum | 2.0833% | 1.19 | 2.70 |
| Sealeo | Incomum | 2.0833% | 0.77 | 1.70 |
| Gastrodon | Incomum | 2.0833% | 0.63 | 1.50 |
| Drednaw | Incomum | 2.0833% | 0.70 | 1.70 |
| Barraskewda | Incomum | 2.0833% | 0.91 | 2.00 |
| Jellicent | Raro | 0.7500% | 1.54 | 3.00 |
| Alomomola | Raro | 0.7500% | 0.84 | 1.90 |
| Basculegion | Raro | 0.7500% | 2.10 | 4.20 |
| Araquanid | Raro | 0.7500% | 1.26 | 2.80 |
| Cramorant | Raro | 0.7500% | 0.56 | 1.40 |
| Vaporeon | Raro | 0.7500% | 0.70 | 1.70 |
| Froakie | Especial | 0.1250% | 0.21 | 0.60 |
| Popplio | Especial | 0.1250% | 0.28 | 0.80 |
| Sobble | Especial | 0.1250% | 0.21 | 0.60 |
| Quaxly | Especial | 0.1250% | 0.35 | 1.00 |

### Tier 4 — Grandes descobertas

| Espécie | Grupo | Chance | Mínimo (m) | Máximo (m) |
|---|---|---:|---:|---:|
| Poliwrath | Comum | 14.0000% | 0.91 | 2.00 |
| Politoed | Comum | 7.0000% | 0.77 | 1.70 |
| Ludicolo | Comum | 7.0000% | 1.05 | 2.40 |
| Seismitoad | Comum | 7.0000% | 1.05 | 2.40 |
| Slowking | Comum | 7.0000% | 1.40 | 3.20 |
| Walrein | Comum | 7.0000% | 0.98 | 2.20 |
| Mantine | Comum | 14.0000% | 1.47 | 2.90 |
| Barbaracle | Comum | 7.0000% | 0.91 | 2.00 |
| Sharpedo | Incomum | 3.1250% | 1.26 | 2.80 |
| Huntail | Incomum | 3.1250% | 1.19 | 2.70 |
| Gorebyss | Incomum | 3.1250% | 1.26 | 2.80 |
| Clawitzer | Incomum | 3.1250% | 0.91 | 2.00 |
| Dragalge | Incomum | 3.1250% | 1.26 | 2.80 |
| Golisopod | Incomum | 3.1250% | 1.40 | 3.20 |
| Relicanth | Incomum | 3.1250% | 0.70 | 1.70 |
| Veluza | Incomum | 3.1250% | 1.75 | 3.50 |
| Gyarados | Raro | 0.7500% | 4.55 | 8.10 |
| Milotic | Raro | 0.7500% | 4.34 | 7.70 |
| Lapras | Raro | 0.7500% | 1.75 | 3.50 |
| Kingdra | Raro | 0.7500% | 1.26 | 2.80 |
| Tatsugiri | Raro | 0.7500% | 0.21 | 0.60 |
| Palafin | Raro | 0.7500% | 0.91 | 2.00 |
| Wailord | Especial | 0.0455% | 10.15 | 18.10 |
| Dondozo | Especial | 0.0455% | 8.40 | 15.00 |
| Greninja | Especial | 0.0455% | 1.05 | 2.40 |
| Primarina | Especial | 0.0455% | 1.26 | 2.80 |
| Feraligatr | Especial | 0.0455% | 1.61 | 3.20 |
| Swampert | Especial | 0.0455% | 1.05 | 2.40 |
| Empoleon | Especial | 0.0455% | 1.19 | 2.70 |
| Blastoise | Especial | 0.0455% | 1.12 | 2.50 |
| Quaquaval | Especial | 0.0455% | 1.26 | 2.80 |
| Inteleon | Especial | 0.0455% | 1.33 | 3.00 |
| Samurott | Especial | 0.0455% | 1.05 | 2.40 |

