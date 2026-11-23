# Resultados da fase 0

Build e 7 testes JUnit aprovados. Servidor dedicado Fabric/Minecraft 1.21.1 com Cobblemon 1.8.3: 23 verificações sem os quatro mods opcionais e 26 com o bundle Multiworld enviado, Trainers 0.3.5, Zorykai Menus 1.3.6 e Guard 0.5.3-alpha.1 carregados.

Cobertura: configuração inicialmente sem dimensão, ausência de troca de inventário, mapa antes dos lagos, preservação de JSON, entidade real incapturável na água, cancelamento dos eventos nativos de bola/seen, não interferência em Pokémon comuns, propriedade exclusiva, gravação e idempotência, Party/PC preexistentes preservados, nenhum PokemonCapturedEvent, menu de armazém bloqueado contra movimentação, armazém cheio, expiração e leitura por outra conexão SQLite. No ensaio integrado também foram confirmados o Multiworld 1.13.1 e `/mw list`, registro de ações pela API real e validação do menu externo na inicialização.

Os jogadores são simulados no servidor, sem cliente gráfico. Não houve lançamento físico de bola nativa, pesca completa de vara/boia, sessão temporária, morte/recuperação, pagamento, NPC clicado ou regras de regiões Guard exercitadas. Isso não comprova compatibilidade completa do modpack. A fixture usa jogadores que não passam pelo login completo do Cobblemon; por isso os logs podem conter avisos de arquivos auxiliares `.old` desses jogadores artificiais na limpeza. Nenhum jogador real foi utilizado e nenhum dado de produção foi alterado.

Veja JSONs para as verificações individuais. O JAR checks é separado e não está contido no produto. Não instale o fixture em produção.
