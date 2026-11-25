# Validação da beta 0.2.4-beta.1

Os relatórios desta entrega são separados da prova de conceito 0.1.0. Os arquivos `024-*.json` em `docs/tests/` registram os ensaios do JAR entregue e seus hashes. O código de teste está em `src/integration` e não faz parte do JAR de produção.

## Ambiente

Java 21, Minecraft 1.21.1, Fabric Loader 0.17.2, Fabric API 0.116.6+1.21.1, Fabric Language Kotlin 1.13.6 e Cobblemon 1.8.3. Os ensaios opcionais carregam os JARs reais Shirokai Trainers 0.3.5, Zorykai Menus 1.3.6, Shirokai Guard 0.5.3-alpha.1 disponível no workspace, Impactor 5.3.5 e **o bundle Multiworld 1.13.1 gamerule-fix enviado pelo usuário**. A auditoria de branches anterior está em AUDITORIA.md; não confundir a versão local testada do Guard com uma publicação de versão nova no GitHub.

Cada runtime tem mundo, SQLite e configuração próprios, portas locais e modo offline apenas para testes. Nenhum mod de Raid/Safari/Daily/Pass/Dex/GTS/Essentials/Commands é uma integração do Fishing. Nenhum repositório ou configuração de produção desses mods foi alterado.

## Testes automatizados

JUnit verifica geometria, probabilidades, dono/duplicidade, expiração/capacidade, persistência, atomicidade entre captura e progresso, reserva de capacidade durante venda, estado permanente de varas/desbloqueios, geração/preservação/backup de configuração, rejeição de preço/schema inválidos e coordenadas negativas no fallback.

Os ensaios Fabric executam métodos e eventos reais do servidor com jogadores simulados e conexões de teste. Verificam:

- Entrada sem mapa não modifica inventário; perímetro antecede lagos e reload preserva a tabela.
- Entidade real Cobblemon com marca de captura nativa desativada e eventos de captura/visualização cancelados; captura própria não modifica Party/PC nem emite captura nativa. O bloqueio de batalha foi revisado no código; não houve um ensaio completo de batalha nativa.
- Boia real e escolha do lago pela água da boia; sequência determinística de fisgada/recolhimento/tensão; lançamento físico de EmptyPokeBallEntity com interceptação do impacto e estoque premium.
- Dono, repetição, armazém cheio e remoção da entidade somente após commit.
- Reserva durável do inventário, retorno, armadura, mão secundária, XP/habilidades, proteção contra teleporte/drop/comandos por duas rotas, morte e duas sessões simultâneas.
- Itens inesperados reservados separadamente e retirados uma vez por recibo. O Ender Chest não é trocado/restaurado pelo Fishing; recompensas externas nele permanecem.
- Cotas normal/VIP pelo caminho real da Fabric Permissions API (provedor simulado de permissões, não um servidor LuckPerms real), consumo limitado por segundo e recusa de pacote que excederia o limite antes da cobrança.
- Recuperação carregada do SQLite, PlayerManager.loadPlayerData antes do JOIN, ordem Guard/Fishing, região Guard com ENTRY deny respeitada e ausência de bypass geral, dimensão de retorno ausente e tentativa controlada.
- Proteção de favoritos, missão diária, resgate único, evento, torneio e abertura de telas dinâmicas.
- Registro real da API Zorykai Menus, comando de NPC real do Trainers abrindo armazém durante sessão e dimensão de teste criada pelo Multiworld seguida de entrada/retorno.
- Compra e venda usando conta/serviço Impactor reais no runtime isolado. Falha simulada entre intenção e confirmação não repete pagamento automaticamente; a atestação explícita de não aplicação libera o journal sem conceder estoque.

Na versão 0.2.1, os ensaios também verificam bloqueio de spawn dentro/fora do perímetro, rejeição de tag forjada, encontro próprio preservado, ícone reutilizável e pacotes explícitos de sincronização, estoque consumido uma vez com 100 usos rápidos, recarga inicial de 10s, barra de `I` preenchendo na action bar sem boss bar, som espacial/partículas privados e emitidos uma vez, cancelamento efetivo da lógica vanilla só na boia do mod, bote real com dono, montar/desmontar, recolher/recolocar, saída montada com inventário restaurado e deduplicação de um clique que envie block-use e item-use. A loja de recarga é testada com Impactor real e persistência em conexão SQLite independente; sem Impactor, a compra paga recusa sem conceder melhoria.

O teste de queda executa **duas JVMs distintas**. A primeira salva inventário temporário com sessão ACTIVE e é encerrada por SIGKILL, sem SERVER_STOPPING. A segunda carrega a reserva do SQLite e restaura o original no caminho nativo de login, verificando inventário/armadura/mão secundária/XP/posição e repetição sem duplicar. A queda ocorre com bote temporário montado e melhorias de recarga salvas: confere também a ausência de bote órfão e a persistência dos níveis.

A versão 0.2.2 acrescenta testes unitários da tolerância para dar folga, perda por linha frouxa, perda durante puxadas, redução do ganho e aumento da tensão por clique durante puxadas, descanso após puxada, limites das barras, duração aleatória das fases e possibilidade de vencer sem arrebentar. O runtime verifica a redução efetiva de progresso pelo motor do Fishing, aviso privado único por puxada e barras de dez letras. Chamadas repetidas no mesmo tick não podem alterar as taxas.

Os testes da 0.2.3 verificam aviso discreto de entrada, sininho privado único, limite de emissão de partículas e posições exclusivamente sobre água dentro de lago registrado, além de desativação da marcação. Não são testes de FPS ou benchmarks de escala.

A versão 0.2.4 testa compatibilidade de JSONs de área sem display/blocks parciais, cores legacy/hex, duração inválida e envio de subtítulo personalizado com nick e tempo. O runtime verifica brilho branco junto aos pontos coloridos, preservação do limite total, sinal adicional de fisgada e recolhimento antes da fisgada/cancelamento durante a disputa.

## Limites da evidência

Não houve cliente gráfico humano nem ensaio no servidor do usuário. Os testes não demonstram latência de rede real, qualidade visual/animação em todos os clientes, escala de centenas de jogadores, interferência de todo o modpack ou todos os provedores de autenticação. Os aliases precisam corresponder ao autenticador efetivamente instalado. A confirmação de permissões foi feita via API Fabric, não por uma instância real de LuckPerms com grupos do usuário.

O minijogo é dirigido deterministicamente no ensaio: a boia e os eventos são reais, mas o relógio de fisgada e a sequência de cliques são controlados pela fixture. A captura própria mostra o lançamento e usa resultado próprio; não promete a sequência completa de captura nativa do Cobblemon.

Não foi provocado um crash no exato ciclo de CPU de um débito externo. O teste de intenção interrompida persiste esse estado e reinicializa o serviço financeiro. O Impactor não fornece chave idempotente por transação: um resultado monetário desconhecido requer conferência administrativa, não uma suposição automática.

Os avisos de autenticação pública indisponível, gerador flat e metadados `.old` para perfis artificiais recém-criados pertencem ao runtime isolado e constam nos logs. Falhas de assertiva são registradas como `passed:false`, nunca omitidas como sucesso. Consulte somente os relatórios 024 finais vinculados ao hash da entrega para o resultado final; execuções intermediárias são mantidas separadamente no workspace.





## Resultado final

Compilação concluída com sucesso. JUnit: **25 testes, zero falhas**.

| Runtime Fabric | Verificações | Falhas |
|---|---:|---:|
| standalone | 121 | 0 |
| integrated | 142 | 0 |
| crash | 6 | 0 |

As contagens de runtimes incluem cenários repetidos entre ambientes. Não são 269 funcionalidades independentes. Todos os runtimes usaram exatamente o JAR com SHA-256 `7c3d03c88e21ea8da748c35c55c4a64678613e4c687bcde29656739467a0f8c4`.
