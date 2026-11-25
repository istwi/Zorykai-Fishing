# Zorykai Fishing 0.2.4-beta.1

**Downloads:** [JAR 0.2.4-beta.1](https://github.com/istwi/Zorykai-Fishing/raw/refs/heads/main/downloads/Zorykai-Fishing-0.2.4-beta.1.jar) · [Pacote completo](https://github.com/istwi/Zorykai-Fishing/raw/refs/heads/main/downloads/zorykai-fishing-0.2.4-beta.1-completo.zip) · [Configurações](https://github.com/istwi/Zorykai-Fishing/raw/refs/heads/main/downloads/zorykai-fishing-configuracoes.zip).

Preserve os seus JSONs e banco ao atualizar.

Mod **de servidor** Fabric para Minecraft 1.21.1, Java 21 e **Cobblemon 1.8.3**. A pesca usa encontros e captura próprios: Pokémon pescados ficam exclusivamente no SQLite do Fishing, sem entrar na Party, no PC ou na Pokédex oficial. O cliente usa as entidades, itens e telas já disponíveis no Minecraft/Cobblemon.

A dimensão do servidor ainda não foi criada. **O mod não cria, importa nem altera dimensões automaticamente.** A entrada fica indisponível até o administrador configurar uma dimensão carregada, o perímetro e uma entrada fisicamente segura. Nenhum outro repositório de mod foi alterado para esta implementação.

Veja [as mudanças e configuração da versão 0.2.1](docs/ATUALIZACAO_0.2.1.md): sincronização da bola, recarga inicial de 10s com barra de `I` na action bar, loja de melhorias permanentes, perímetro exclusivo, fisgada com som/partículas e bote temporário.

A versão 0.2.2 adiciona fases calmas/puxadas, perda de progresso com tolerância e barras de tensão/progresso. Veja [como jogar e configurar](docs/ATUALIZACAO_0.2.2.md).

A versão 0.2.3 adiciona nome do lago com sininho e marcações privadas apenas na borda da água. Veja [configuração e limites de partículas](docs/ATUALIZACAO_0.2.3.md).

A versão 0.2.4 adiciona `display` no JSON de cada lago, bordas mais visíveis, sinal azul/vermelho na boia e recolhimento/cancelamento livre da vara. Veja [configuração e controles](docs/ATUALIZACAO_0.2.4.md).

## Instalação

1. Pare o servidor e faça backup do mundo/configuração.
2. Coloque somente `Zorykai-Fishing-0.2.4-beta.1.jar` em `mods/`. Remova versões anteriores do Fishing. **Não instale os JARs `checks` ou `dev` no servidor.**
3. Mantenha Fabric API e as dependências habituais do Cobblemon. O JAR incorpora SQLite e Fabric Permissions API. Não precisa instalar Fishing no cliente.
4. Para dinheiro, use Impactor com a moeda existente `impactor:dollars`. Sem Impactor, pesca, armazenamento e recompensas sem dinheiro continuam disponíveis; operações pagas recusam antes de reservar/débitar.
5. Integrações opcionais auditadas: Shirokai Trainers, Zorykai Menus, Shirokai Guard e o Multiworld enviado pelo usuário. Os exemplos são instalados **manualmente**, sem sobrescrever a configuração desses mods.
6. Inicie uma vez para gerar `config/zorykai-fishing/`, configure a dimensão e os lagos e valide em uma cópia do servidor antes de disponibilizar aos jogadores.

Dados de jogadores, capturas, inventários reservados, recibos e histórico ficam em `<mundo-principal>/data/zorykai-fishing/fishing.sqlite`, com WAL. **Não apague o banco enquanto houver sessões, reservas ou pagamentos pendentes.** Para backup físico, pare o servidor e copie a pasta inteira; com servidor online use uma ferramenta de backup SQLite, nunca copie apenas o `.sqlite` ignorando um WAL ativo.

## Criar a construção e depois os lagos

Crie/carregue sua dimensão pelo Multiworld. Comandos reais auditados: `/mw list`, `/mw create <id> <ambiente>` e `/mw tp <id>`. Exemplo de criação manual: `/mw create pesca NORMAL`, seguido de `/mw tp pesca`. Consulte a ajuda do seu JAR para outros ambientes. O Multiworld adiciona `multiworld:` ao nome sem namespace. Não há necessidade de criar o mundo antes de instalar o Fishing, mas é necessário antes de cadastrar/entrar.

Na dimensão da construção, marque dois cantos opostos do volume **inteiro**, incluindo altura, e uma entrada com chão sólido, dois blocos livres, sem lava/fogo e permitida no Guard:

```
/pescaadmin mapa pos1
/pescaadmin mapa pos2
/pescaadmin mapa entrada
/pescaadmin mapa salvar
```

Depois marque cada volume de água dentro do perímetro:

```
/pescaadmin area criar lago_inicial
/pescaadmin area pos1 lago_inicial
/pescaadmin area pos2 lago_inicial
/pescaadmin area salvar lago_inicial
/pescaadmin area validar lago_inicial
/pescaadmin area listar
```

As posições são o bloco onde o administrador está. Inclua na seleção os blocos onde a **boia** ficará na água; não marque só a margem. São rejeitados lagos sobrepostos, fora do perímetro ou em outra dimensão. A seleção é individual por administrador. `/pescaadmin area entrada <id>` é um marcador auxiliar; o Fishing não teleporta entre lagos: os jogadores exploram livremente a construção.

Edite `areas/lago_inicial.json`, usando espécies implementadas no seu Cobblemon. As chances precisam somar **100**. Exemplo:

```json
{
  "id": "lago_inicial",
  "name": "Lago dos Primeiros Passos",
  "map": "mapa",
  "dimension": "multiworld:pesca",
  "bounds": {"minX": 10, "minY": 61, "minZ": 10, "maxX": 30, "maxY": 64, "maxZ": 30},
  "minimumLevel": 1,
  "permission": null,
  "unlockMode": "ALL",
  "quests": [],
  "rods": [],
  "baits": [],
  "sizeMultiplier": 1.0,
  "pokemon": [
    {"species": "magikarp", "chance": 45},
    {"species": "goldeen", "chance": 30},
    {"species": "poliwag", "chance": 15},
    {"species": "wooper", "chance": 8},
    {"species": "feebas", "chance": 2}
  ]
}
```

Coordenadas são ilustrativas: devem caber no seu `mapa.json`. `unlockMode` aceita `FREE`, `LEVEL`, `QUEST`, `PERMISSION`, `ANY` e `ALL`. `QUEST` requer todas as missões de `quests`; `ANY` aceita um requisito configurado atendido; `ALL` exige todos. Um desbloqueio permanente ignora esses requisitos, mas um bloqueio administrativo prevalece. Permissão temporária não grava desbloqueio permanente. Listas vazias de varas/iscas não restringem equipamentos. Os requisitos controlam **pescar**, não andar pelo lago.

Finalize com `/pescaadmin validar` e `/pescaadmin reload`. Uma configuração inválida não substitui o conjunto carregado. JSONs existentes não são sobrescritos; conjuntos válidos são arquivados em `backups/config-*.zip` (últimos 10). Remover um lago por comando arquiva seu JSON, preservando capturas históricas.

## Como jogar

`/pesca` abre o menu. `/pesca entrar` reserva duravelmente os pertences originais e entrega vara/bola temporárias. O mundo de pesca usa aventura e não permite mover equipamentos para outros inventários, largar itens ou acessar contêineres externos.

Use a vara para lançar uma boia. A água da boia determina o lago. Aguarde a mensagem de fisgada e clique espaçadamente: o progresso sobe, mas cliques seguidos elevam a tensão e podem arrebentar a linha. Após recolher, surge um Pokémon exclusivo ou um tesouro. Lance a bola temporária no Pokémon para tentar capturá-lo. Cada encontro tem dono e prazo; outro jogador não pode roubá-lo. Bolas premium e iscas têm estoque virtual persistente; a opção básica é gratuita e ilimitada.

`/pesca sair` encerra encontro/linha e restaura o inventário original, armadura, mão secundária, XP, alimentação, efeitos, habilidades e posição. Reconexão **não restaura uma oportunidade de captura**. Sair da construção encerra a sessão. Morte é interceptada para recuperar a reserva, sem derrubar equipamentos temporários. Desconexão/desligamento tentam a saída; uma queda deixa a reserva para recuperação no próximo login.

Cada vara mantém XP e melhorias próprios. Equipamentos premium têm requisitos e preços configuráveis. Tamanho é relativo à espécie; o valor usa espécie, raridade, tamanho, shiny e limite de preço. A escala visual usa a API do Cobblemon e tem limite separado.

Menus/comandos: `armazem`, `areas`, `perfil`, `varas`, `iscas`, `bolas`, `melhorias`, `missoes`, `tempo`, `conquistas`, `torneios`, `premios`, `eventos`, `ranking`, `recordes`, `dex`, `museu`, `prestigio`, `tesouros`, `pendencia` após `/pesca`.

- Armazém: visualizar, proteger/favoritar, vender uma captura ou lote da página com confirmação, soltar com confirmação, filtrar shiny/favoritos e ordenar por valor/tamanho. `/pesca buscar <especie>` pesquisa; favoritos são recusados na venda/liberação.
- Pokédex de pesca: paginação, quantidade encontrada/capturada, menor/maior e shiny; `/pesca dexbuscar <especie>` pesquisa e `shiny` filtra.
- Missões: diárias, semanais e de história, com objetivos de captura/espécie/tamanho/venda/nível/coleção/região; progresso e resgate separados. Os ciclos de calendário usam America/Sao_Paulo.
- Torneios: maior tamanho, quantidade, valor ou variedade; inscrição explícita, encerramento e recompensa única por edição. Prêmios antigos ficam em `premios` após começar nova edição.
- Rankings: capturas, maior tamanho, nível, espécies e torneios vencidos. Recordes por espécie têm histórico persistente. Museu é **virtual**, com favoritos e troféus; não constrói estruturas no mapa.
- Prestígio: opcional/desativado por padrão. Reinicia XP de pesca ao atingir o nível máximo e preserva varas, capturas e desbloqueios.
- Tesouros: iscas, bolas, fragmentos internos e itens. Itens ficam reservados para retirada **fora da sessão**. Itens externos recebidos no inventário temporário são preservados em reservas separadas; recibos com comparação antes/depois evitam entrega repetida.

## Tempo e VIP

`timers.json`: normal 60 minutos, ciclo móvel de 24 horas, limite de acumulação, avisos e actionbar. Tempo só é consumido durante sessão online dentro do perímetro. Créditos comprados/administrativos possuem validade e limite de acumulação.

`vip-time.json` usa a Fabric Permissions API, atendida pelo LuckPerms:

| Permissão | Minutos por ciclo |
|---|---:|
| `zorykai_fishing.vip.magma` | 72 |
| `zorykai_fishing.vip.aqua` | 86 |
| `zorykai_fishing.vip.plasma` | 103 |
| `zorykai_fishing.vip.galactic` | 124 |

Vale o maior bônus concedido; o ciclo existente mantém sua cota até renovação/reset. Exemplo: `/lp group magma permission set zorykai_fishing.vip.magma true`.

## Configuração separada

Regras ficam em `config/zorykai-fishing/`: `timers`, `vip-time`, `time-shop`, `levels`, `rods`, `upgrades`, `baits`, `pokeballs`, `capture`, `sizes`, `prices`, `storage`, `economy`, `commands`, `permissions`, `npc`, `records`, `rankings`, `achievements`, `prestige`, `worlds`, `messages`, `inventory`, `integrations`; mais pastas `menus`, `areas`, `missions`, `events`, `tournaments`, `treasures`, `pokemon`. Os arquivos de `pokemon/` são referências; a tabela efetiva está em cada lago.

Definições usam `values` com IDs estáveis. Não renomeie IDs já possuídos por jogadores sem uma migração planejada. `schemaVersion` desconhecida recusa carregamento. `prices.json.species` define preço-base por espécie. Eventos podem limitar `lakes`, multiplicar XP/tamanho/shiny/raros/tesouros e definir `species` como mapa de multiplicadores de encontro. Tesouros aceitam `lakes` e `minimumLevel`. Valores monetários são arredondados à precisão real da moeda Impactor. Pacotes de tempo que ultrapassariam o limite são recusados antes da cobrança. `storage.initialCapacity` vale para perfis novos; não reduz armazéns existentes. A proteção de inventário e a validação do Guard não podem ser desativadas por JSON. Alterar a integração de menus exige reiniciar para registrar/remover extensões com segurança.

Missões: `objective` `CAPTURE`, `SELL`, `LEVEL`, `COLLECTION` ou `REGION`; opcionais `species`, `size`, `lake`; `target`; `reward` com `xp`, `money`, `minutes`, `bait`, `ball`, `amount`, `rod`, `upgrade`, `upgradeLevel`, `unlock` ou `fragments`. Missões `REGION` usam contagem do `lake`. Datas/períodos: `DAILY`, `WEEKLY`, `STORY`.

Menus internos usam 54 slots. `menus/<pagina>.json` permite título e substituições visuais em `buttons`, indexadas pelo slot original. Exemplo: `"buttons":{"2":{"slot":11,"item":"minecraft:map","name":"&bMeus lagos","lore":["&7Explore Zorykai"],"enabled":true}}`. As ações continuam no servidor; JSON visual não transforma um botão em comando administrativo. Não sobreponha slots de navegação/confirmadores. Para layouts e ações adicionais, use a API pública do Zorykai Menus e os exemplos.

## NPCs e Zorykai Menus

Veja `examples/`. Os arquivos não são copiados automaticamente às pastas de outros mods.

NPC simples de venda:

```
/traineradmin npc create-command vendedor_pesca
/traineradmin npc command vendedor_pesca console pesca abrir armazem %player%
```

`pesca abrir <menu> <jogador>` aceita **somente console**, incluindo o executor de console do Trainers. Aliases de página: `venda`→armazém, `pokebolas`→bolas, `recompensas`→conquistas. Não concede `zmenu open` aos jogadores.

Para diálogo, instale um exemplo em `config/shirokai_trainers/dialogues/`, recarregue os diálogos e associe com `/traineradmin npc dialogue <npc> <dialogo>`. No Fishing, cadastre IDs de NPCs de diálogo confiáveis em `npc.json.allowedNpcIds` (array), ou seus UUIDs em `allowedEntityUuids`. NPC de comando que executa `pesca abrir ...` pelo console é reconhecido pelo registro real do Trainers. NPCs de batalha não são liberados durante a sessão.

O exemplo `fishing_main.json` vai manualmente em `config/zorykaimenus/menus/`. Conceda `zorykai_fishing.use` para ações externas. Ações: `zorykai_fishing:entrar`, `:sair` e os nomes de páginas. Placeholders da API: `zorykai_fishing:level`, `:xp`, `:time` (segundos), `:captures`, `:capacity`, `:prestige`; condição `zorykai_fishing:active`. Consulte a sintaxe de placeholders do seu Zorykai Menus. Dentro de sessões são usadas telas próprias bloqueadas, para preservar isolamento do inventário.

## Guard, autenticação e recuperação

O Fishing **não cria regiões Guard e não concede autorização permanente**. Configure sua região Guard para permitir entrada, saída, teleporte autorizado e interações necessárias com NPCs/vara/bolas. Mantenha exploração dos lagos liberada; Fishing decide quem pode pescar.

Antes de trocar inventário, o destino é validado quanto a dimensão carregada, limites/coordenadas, chão, espaço, fluidos/perigos e permissões de entrada do Guard. Um retorno inválido fica pendente. Pode configurar `worlds.json.recoverySpawn` com `{dimension,x,y,z}` explicitamente seguro/permitido; nenhum spawn arbitrário é escolhido.

Recuperação de login altera os dados carregados pelo PlayerManager **antes do JOIN**, sem depender da ordem dos callbacks. Enquanto a recuperação está pendente, a cooperação com EntrySafety impede expulsão prematura e o Fishing mantém movimento, interação e teleportes bloqueados. Isso não abre a arena nem autoriza entrada para terceiros. A reserva só é concluída após salvar/verificar o inventário restaurado. `/pesca sair` e `/pescaadmin recuperar <jogador>` permitem nova tentativa controlada; não há teleporte/reclamação repetida a cada tick.

`commands.json.allowed` libera somente comandos explicitamente permitidos, incluindo autenticação. Os padrões incluem `login`, `l`, `logar`, `register`, `reg`, `registrar`, `2fa`, `auth`, `pin`; **confira os aliases reais do seu autenticador**. Redirecionamentos Brigadier também são verificados. Não há integração que autentique o jogador automaticamente. LuckPerms `zorykai_fishing.bypass` permite comandos administrativos excepcionais com auditoria; `zorykai_fishing.teleport_bypass` encerra/restaura a sessão antes de um teleporte externo de staff, sem contornar o Guard.

## Administração e pendências financeiras

Administração: OP 4 ou `zorykai_fishing.admin`. Uso: `zorykai_fishing.use` (comandos liberados por padrão, respeitando negação explícita).

```
/pescaadmin status|validar|reload
/pescaadmin mapa pos1|pos2|entrada|salvar
/pescaadmin area criar|pos1|pos2|entrada|salvar|validar|remover <id>
/pescaadmin area listar
/pescaadmin area desbloquear|bloquear <jogador> <id>
/pescaadmin tempo ver|reset <jogador>
/pescaadmin tempo add|set <jogador> <minutos>
/pescaadmin xp add <jogador> <quantidade>
/pescaadmin vara dar <jogador> <id>
/pescaadmin missao completar <jogador> <id>
/pescaadmin sessao encerrar <jogador>
/pescaadmin recuperar <jogador>
/pescaadmin evento iniciar|encerrar <id>
/pescaadmin torneio iniciar|encerrar <id>
/pescaadmin transacao ver <jogador>
```

As mutações administrativas exigem jogador online e são auditadas. Completar missão libera o resgate, não repete entrega automaticamente.

Transações persistem intenção antes do Impactor; capturas à venda são reservadas e uma transação por jogador pode estar em andamento. Resultado confirmado retoma a aplicação interna sem repetir débito/crédito. Resultado desconhecido após erro/queda fica `UNCERTAIN`: a API do Impactor não oferece uma chave idempotente que permita adivinhar se o pagamento aconteceu. **Não apague o journal para destravar.** Confira o histórico/saldo do Impactor e então use uma atestação explícita:

```
/pescaadmin transacao confirmar-aplicada <jogador> <id>
/pescaadmin transacao confirmar-nao-aplicada <jogador> <id>
```

Esses comandos não repetem a operação monetária: apenas aplicam os efeitos internos ou cancelam/liberam a reserva conforme a conferência. Para retirada de item com inventário divergente, preserve o recibo e confira os estados antes/depois no banco; não há reentrega cega.

## Compilar e validar

Gradle 8.14.3, JDK 21; `./gradlew build remapIntegrationJar`. As dependências locais de compilação, não redistribuídas, vão em `libs/cobblemon.jar`, `libs/zorykaimenus.jar`, `libs/impactor.jar` (versões na auditoria). O classpath opcional não torna esses mods dependências obrigatórias.

Testes JUnit e ensaios Fabric estão em `src/test`, `src/integration` e `audit/run_native.py`/`run_restart.py`. Consulte `docs/VALIDACAO_BETA.md` para resultados, versões e limites. O JAR `checks` é exclusivamente um instrumento de testes que cria jogadores/dados, altera configuração do runtime isolado e encerra o servidor. **Nunca o instale em produção.**

A entrega é beta: os ensaios usam servidor Fabric real e jogadores simulados, não substituem um teste visual com seu cliente/modpack, rede, autenticação, carga e construção reais. Nenhuma alteração foi aplicada ao seu servidor ou publicada no GitHub.
