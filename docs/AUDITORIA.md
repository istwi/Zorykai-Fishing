> Histórico da fase 0. A implementação beta posterior está descrita no README e em VALIDACAO_BETA.md; os limites desta auditoria inicial não descrevem todas as funcionalidades da versão atual.

# Fase 0 — auditoria das integrações

Somente Trainers, Zorykai Menus, Guard e Multiworld são integrações de gameplay. Nenhuma integração com Raids, Pass, Safari, Daily, Dex, GTS, Essentials ou Commands foi criada. Os repositórios existentes não foram modificados.

## Shirokai Trainers

Branches remotas examinadas: main (`fb8f571`), feat/independent-command-npcs (`66d1b48`), native-npcs (`7203971`), maps/shirokai-safari (`5ab4132`), zorykai (`88b3502`). Distribuição mais recente de gameplay encontrada: 0.3.5 em zorykai; a branch de mapas contém construções, não uma nova versão do mod.

O clique de NPC possui comando player/console e aceita `%player%` como argumento completo. Diálogos JSON possuem botões `command`, `page`, `battle` e `close`. Não foi encontrada uma API de missões ou transações; o Fishing não presume que exista.

Integração proposta e implementada pelo Fishing: comando de console `pesca abrir <main|perfil|areas|armazem> <jogador>`. Ele abre uma tela pertencente ao jogador informado sem conceder OP. Exemplo de vínculo, a ser executado pelo administrador:

```text
/traineradmin npc create-command pescadora
/traineradmin npc command pescadora console pesca abrir armazem %player%
```

Opcionalmente copiar o exemplo de diálogo manualmente e vincular pelo comando do Trainers. O Fishing não cria NPCs e não altera os arquivos do Trainers. Não existe compra, venda, missão ou torneio ativo na fase 0.

## Zorykai Menus

Branches examinadas: main/feature/1.1.0 (`b8d6b99`, 1.1.0), feature/dynamic-market (`103e268`, 1.2.0), feature/pokemon-sales (`aa9deb1`, 1.3.0), feature/pokemon-sales-limits (`b09d6cf`, 1.3.4), feature/menu-entry-access (`276cecc`, 1.3.5), feature/optional-main-priority-aliases (`e63120b`, 1.3.6) e research/pokemon-sale-pricing (pesquisa).

API real verificada: `com.zorykai.menus.api.ZorykaiMenusAPI`, com `ready`, `menuExists`, `openMenu`, `openShop`, `registerAction`, `registerPlaceholder`, `registerCondition` e observadores de transações. A API não oferece cadastro arbitrário de definições de menu em memória; não inventamos esse método.

O Fishing registra ações próprias antes dos eventos SERVER_STARTED, permitindo que os JSONs externos sejam validados na inicialização independentemente da ordem desses eventos. Ações disponíveis nesta fase: `zorykai_fishing:armazem`, `zorykai_fishing:areas`, `zorykai_fishing:perfil`. A API exige uma permissão por ação: conceder `zorykai_fishing.use` via LuckPerms para os grupos que usarão essas ações. Sem ação externa configurada, o Fishing usa telas vanilla próprias. Um menu externo configurado que negue acesso não é contornado pelo fallback.

Exemplo opcional em `examples/zorykai-menus/fishing_main.json`: copiar manualmente para o diretório de menus do Zorykai Menus. O Fishing não grava no diretório de outro mod. Compras e vendas de capturas exclusivas serão transações do Fishing, não vendas nativas de Party/PC do Zorykai Menus.

## Shirokai Guard

Branches examinadas: main (`6cc7610`, fonte 0.5.1-alpha.1) e zorykai (`9faa75e`, fonte 0.5.2-alpha.1). No ambiente havia o JAR previamente compilado 0.5.3-alpha.1; ele foi usado no ensaio de coexistência e identificado pelo manifesto.

Flags reais verificadas: block-break, block-place, chest-access, item-drop, item-pickup, entry, entry-mode, exit, teleport, pokemon-capture, pokemon-battle, pokemon-interact, entity-interact e comandos permitidos/bloqueados. `Protection.transition` e EntryAccess controlam fronteiras/teleporte, e a fase de proteção roda antes dos callbacks padrão de interação. Isso é relevante para a fase de sessões e para permitir varas, bolas e NPCs dentro de regiões protegidas.

Nesta entrega não existe gerador de regiões Guard nem bypass de proteção. O perímetro e os lagos do Fishing são sua geometria própria. A área inteira precisa ter exploração liberada no Guard; o Fishing controlará o direito de pescar nos lagos. O teste atual comprova inicialização conjunta, não valida regras de região ou recuperação de sessões com Guard. Estes itens pertencem às fases 1/3/6.

## Multiworld exato enviado

Arquivo: `Multiworld-Fabric-bundle-1.13.1-gamerule-fix.jar`. O bundle ID é `multiworld-bundle`; dentro dele a implementação escolhida para Minecraft 1.21.1 tem ID `multiworld`, versão 1.13.1, entrypoint `me.isaiah.multiworld.fabric.MultiworldModFabric`, com Fantasy 0.6.3+1.21 aninhado. Metadados e hash em MULTIWORLD.json. O nome gamerule-fix identifica um artefato fornecido pelo usuário; não presumimos que seja idêntico à distribuição upstream.

Classes/comandos reais auditados no bytecode: MultiworldMod, CreateCommand e TpCommand. Prefixo real `/mw`; comandos incluem `list`, `create`, `tp`, `setspawn` e `gamerule`. CreateCommand recebe nome e ambiente, adiciona `multiworld:` se o nome não tem namespace e suporta argumentos opcionais próprios. Não é necessário chamar a fábrica de mundos a partir do Fishing.

O usuário informou que ainda não criou a dimensão. O Fishing não cria nem importa mundos. Até salvar mapa.json, a configuração fica sem dimensão e a entrada indisponível. Futuramente o administrador poderá criar manualmente um mundo com os comandos reais do mod e selecioná-lo; o Fishing resolverá a dimensão existente pelo registro nativo do servidor, sem usar APIs inventadas. O teste carregou o bundle exato e executou `/mw list`; não criou um novo mundo Multiworld nem validou teleportes de sessões nessa dimensão.

## Cobblemon 1.8.3 — captura exclusiva

APIs reais verificadas: PokemonProperties.createEntity, propriedade `uncatchable`, PokemonEntity.isUncatchable, Pokemon.persistentData, THROWN_POKEBALL_HIT, BATTLE_STARTED_PRE, POKEMON_SEEN, POKEDEX_DATA_CHANGED_PRE, POKEMON_CAPTURED e consultas ao armazenamento Party/PC.

POKEMON_CAPTURED é um evento pós-captura e não é usado para remover Pokémon da Party depois. A prova usa captura própria, com interação de item: a entidade é criada com `uncatchable=true` antes de aparecer; seus dados recebem marcador exclusivo. Capturas nativas, batalhas e eventos de progresso da Pokédex referentes a esses Pokémon são cancelados. A operação própria confirma dono, distância, validade e capacidade e grava uma linha SQLite única por encontro; só depois remove a entidade. Party/PC não participam da operação.

A bola de prova é uma snowball vanilla identificada por componente, gratuita e reutilizável. Não equivale ainda à animação completa de uma Poké Bola nem a uma mecânica de tensão da linha. O ensaio emitiu eventos nativos e usou jogadores simulados, sem cliente gráfico ou lançamento físico de Poké Bola. Não há declaração de compatibilidade completa de todas as fases.
