# PROMPT PARA CODEX — CRIAR O MOD ZORYKAI FISHING

## 1. OBJETIVO PRINCIPAL

Crie um mod completo chamado **Zorykai Fishing**, um sistema de pesca de Pokémon com progressão, áreas desbloqueáveis, tempo de permanência, equipamentos, missões, NPCs, economia e armazenamento exclusivo.

O mod será utilizado em um servidor Minecraft Cobblemon.

**Ambiente obrigatório:**

- Minecraft Java 1.21.1
- Fabric
- Java 21
- Cobblemon 1.8.3
- Fabric API compatível
- Mod ID: `zorykai_fishing`
- Pacote Java: `com.zorykai.fishing`
- Pasta: `config/zorykai-fishing/`
- Comando principal: `/pesca`
- Comando administrativo: `/pescaadmin`
- Prefixo: `✦ ZORYKAI`

Priorizar implementação server-side, sem exigir instalação adicional no cliente, desde que tecnicamente possível.

O mod deve ser desenvolvido de verdade, com código-fonte, JSONs, testes, documentação e JAR compilável.

Não entregar apenas pseudocódigo ou uma estrutura vazia.

---

# 2. INTEGRAÇÕES PERMITIDAS

Utilize **somente os quatro projetos abaixo como integrações com outros mods de gameplay**.

### 2.1. Shirokai Trainers

Repositório:

https://github.com/istwi/Shirokai-Trainers

Responsável pelos NPCs utilizados na pesca.

Quero utilizar NPCs para:

- Vender Pokémon pescados.
- Comprar varas.
- Comprar iscas.
- Comprar Pokébolas especiais.
- Melhorar equipamentos.
- Entregar e concluir missões.
- Participar de torneios.
- Consultar recompensas.

Investigue o código-fonte do Shirokai Trainers e descubra como vincular interações aos NPCs existentes.

Não presumir que ele já possui uma API de comércio ou missões.

Se o Trainers permitir apenas interações por comandos, utilize comandos do Zorykai Fishing para abrir os respectivos sistemas.

O Fishing continuará responsável pelas transações, missões e dados.

### 2.2. Zorykai Menus

Repositório:

https://github.com/istwi/Zorykai-menu

Utilizar a API pública de menus e lojas quando disponível.

Menus necessários:

- Menu principal.
- Perfil do pescador.
- Seleção de áreas.
- Varas.
- Melhorias.
- Iscas.
- Pokébolas.
- Armazém.
- Venda.
- Pokédex de Pesca.
- Missões.
- Recordes.
- Rankings.
- Torneios.
- Conquistas.
- Prestígio.
- Tempo restante.
- Loja de tempo.

Manter aparência limpa, organizada, com cores azul e ciano.

Não copiar o código do Zorykai Menus desnecessariamente.

Verificar a API real antes de criar os adaptadores.

### 2.3. Shirokai Guard

Repositório:

https://github.com/istwi/Shirokai-Guard

Utilizar para proteger o mapa e controlar ações nas regiões.

Regras:

- Impedir destruição e colocação de blocos.
- Impedir abertura indevida de baús.
- Bloquear drops e transferências de itens.
- Bloquear teleportes externos.
- Impedir fuga da sessão.
- Proteger os Pokémon pescados.
- Impedir interferência de outros jogadores.
- Controlar regiões administrativas.

**Importante:** o jogador poderá explorar livremente todas as regiões internas do mapa.

Não bloquear a entrada física em uma área somente porque o jogador ainda não desbloqueou a pesca nela.

A verificação de permissão para pescar será responsabilidade do Zorykai Fishing.

### 2.4. Multiworld

Utilizar o mod Multiworld instalado no servidor para trabalhar com um mundo exclusivo de pesca.

Antes de implementar:

1. Identificar o Multiworld exato utilizado.
2. Confirmar sua versão.
3. Verificar sua API ou comandos.
4. Validar compatibilidade com Fabric 1.21.1.
5. Documentar como integrar.

Não inventar APIs ou comandos do Multiworld.

O mundo de pesca já existe e contém várias regiões construídas.

O Zorykai Fishing não deve gerar um mundo novo automaticamente.

### Regra obrigatória

**Não integrar com nenhum outro mod personalizado do servidor.**

Não criar integrações com Safari, Daily, Pass, Dex, GTS, Raids, Essentials, Commands ou outros projetos Shirokai.

Cobblemon, Fabric API, LuckPerms quando necessário e a API de economia exigida são dependências técnicas permitidas, mas não devem ser usados para acrescentar integrações de gameplay fora do escopo.

Se uma das quatro integrações não possuir API adequada, documentar a limitação e implementar um adaptador ou solução interna, sem adicionar um quinto mod de compatibilidade.

---

# 3. ENTRADA NO MUNDO DE PESCA

O jogador utiliza:

`/pesca`

O comando abre o menu principal.

Ao selecionar **Entrar na pesca**:

1. Verificar se o jogador possui tempo disponível.
2. Verificar se não está em batalha.
3. Verificar se não possui outra sessão ativa.
4. Salvar inventário original.
5. Salvar armadura e mão secundária.
6. Salvar posição original.
7. Salvar estado necessário para restauração.
8. Criar inventário temporário.
9. Entregar sua vara de pesca salva.
10. Carregar suas iscas e Pokébolas especiais.
11. Teleportar para o mundo de pesca.
12. Iniciar o cronômetro.

O jogador começa com inventário temporário vazio, exceto pelos equipamentos autorizados de pesca.

Nunca apagar o inventário original sem confirmar que foi salvo.

---

# 4. SISTEMA DE TEMPO

Implementar tempo de permanência na pesca.

Configuração inicial:

- Jogador normal: 60 minutos.
- Ciclo de renovação: 24 horas.
- Tempo diminui somente enquanto estiver na pesca.
- Ao sair, o tempo pausa.
- Ao desconectar, o tempo pausa.
- Ao entrar novamente, continua com o saldo restante enquanto o ciclo estiver válido.
- Quando o tempo acabar, o jogador sai automaticamente.

Criar configurações para bônus VIP.

Exemplo:

| Cargo | Tempo |
|---|---|
| Normal | 60 minutos |
| Magma | 72 minutos |
| Aqua | 86 minutos |
| Plasma | 103 minutos |
| Galactic | 124 minutos |

Todos os valores devem ser configuráveis.

Permitir:
- Comprar tempo adicional.
- Receber tempo por missão.
- Receber tempo por comando administrativo.
- Definir tempo máximo acumulado.
- Definir validade do tempo comprado.
- Configurar renovação do ciclo.

Avisar quando faltarem 15 minutos, 5 minutos, 1 minuto, 30 segundos e 10 segundos.

Mostrar tempo restante no menu e, opcionalmente, na actionbar.

Quando acabar o tempo:

- Impedir novas pescarias.
- Finalizar encontros ativos com segurança.
- Salvar capturas confirmadas.
- Restaurar inventário original.
- Retornar à posição salva.
- Encerrar sessão.

O tempo nunca pode ser duplicado por desconexão ou troca de mundo.

---

# 5. SAÍDA E BLOQUEIO DE COMANDOS

Dentro da pesca, o jogador só poderá sair usando:

`/pesca sair`

Ou o botão **Sair** no menu.

Bloquear comandos externos, incluindo aliases.

Bloquear também teleportes externos, não apenas comandos digitados.

O jogador não pode utilizar:

- `/spawn`
- `/home`
- `/warp`
- `/tpa`
- `/back`
- `/rtp`
- Comandos de teleporte de outros mods.

Utilizar uma whitelist configurável de comandos permitidos durante a sessão.

Permitir bypass administrativo com auditoria.

Impedir que o jogador saia do perímetro geral do mundo de pesca por meios não autorizados.

Caso o jogador morra, desconecte ou o servidor reinicie, executar recuperação segura da sessão.

---

# 6. ÁREAS DE PESCA

O mapa possui várias regiões.

Quero cadastrar regiões por coordenadas.

Comandos:

```text
/pescaadmin area criar <id>
/pescaadmin area pos1 <id>
/pescaadmin area pos2 <id>
/pescaadmin area entrada <id>
/pescaadmin area salvar <id>
/pescaadmin area validar <id>
/pescaadmin area listar
/pescaadmin area remover <id>
```

Cada área terá:

- ID.
- Nome.
- Dimensão.
- Coordenadas.
- Prioridade.
- Nível mínimo.
- Missão necessária.
- Permissão.
- Espécies disponíveis.
- Porcentagens de encontro.
- Iscas permitidas.
- Varas permitidas.
- Multiplicadores de tamanho.
- Eventos.
- Recompensas especiais.

### Exploração livre

O jogador pode entrar andando em qualquer região interna.

Entretanto, só pode pescar se tiver os requisitos.

Exemplo:

**Oceano Profundo**
- Nível mínimo: 30.
- Missão: Explorador do Oceano.
- Permissão opcional.

Se não tiver os requisitos, pode explorar normalmente, mas não pescar.

### Verificação

Validar a posição da boia na água, não apenas a posição do jogador.

Impedir que jogadores pesquem espécies exclusivas de uma área bloqueada ficando na borda de outra região.

---

# 7. DESBLOQUEIO DE ÁREAS

Implementar:

- Desbloqueio por nível.
- Desbloqueio por missão.
- Desbloqueio por permissão.
- Desbloqueio por combinação de requisitos.

Modos:

- `FREE`
- `LEVEL`
- `QUEST`
- `PERMISSION`
- `ANY`
- `ALL`

Missões podem conceder desbloqueios permanentes.

Salvar desbloqueios por UUID.

Permissões temporárias não devem ser tratadas automaticamente como desbloqueios permanentes.

---

# 8. POKÉMON POR REGIÃO

Cada região terá JSON próprio contendo os Pokémon disponíveis e suas chances.

Exemplo:

```json
{
  "id": "lago_inicial",
  "pokemon": [
    {"species": "magikarp", "chance": 45.0},
    {"species": "goldeen", "chance": 30.0},
    {"species": "poliwag", "chance": 15.0},
    {"species": "wooper", "chance": 8.0},
    {"species": "feebas", "chance": 2.0}
  ]
}
```

Validar:
- Espécies existentes.
- Porcentagens.
- Soma das chances.
- Condições de encontro.
- Modificadores de isca.
- Requisitos especiais.

Permitir configurações específicas para shiny e lendários.

---

# 9. MECÂNICA PRINCIPAL DE PESCA

O jogador lança a vara na água.

Fluxo:

1. Identificar região da boia.
2. Validar requisitos.
3. Verificar equipamento.
4. Consumir isca quando aplicável.
5. Aguardar fisgada.
6. Sortear Pokémon.
7. Sortear tamanho.
8. Iniciar mecânica de tensão da linha.
9. Fazer Pokémon aparecer na água.
10. Permitir captura com Pokébolas especiais.
11. Registrar captura no armazém.
12. Exibir resultado no chat.

O Pokémon pode escapar.

Cada encontro terá proprietário exclusivo.

Outros jogadores não poderão capturar o Pokémon fisgado.

---

# 10. CAPTURA COM POKÉBOLAS ESPECIAIS

Quero que o Pokémon apareça na água e o jogador tente capturá-lo com Pokébolas especiais de pesca.

**Regra obrigatória:**

O Pokémon pescado não pode entrar na Party nem no PC.

Ele deve ir exclusivamente para o armazenamento do Zorykai Fishing.

Investigar a API real do Cobblemon 1.8.3.

Se não for possível interceptar com segurança uma captura nativa, desenvolver uma mecânica própria de captura, com aparência semelhante, sem registrar o Pokémon como captura normal.

Não capturar normalmente e depois apagar o Pokémon da Party como solução improvisada.

Evitar duplicação e inconsistências.

---

# 11. VARAS DE PESCA

O jogador começa com uma vara inicial.

Cada vara terá:
- Nível.
- XP.
- Raridade.
- Velocidade.
- Sorte.
- Resistência.
- Precisão.
- Melhorias.
- Requisitos.

A vara evolui pescando.

O progresso deve ser permanente.

Exemplo:

O jogador entra com Vara Inicial nível 1.

Evolui até nível 15.

Sai da pesca.

Ao entrar novamente, recebe sua Vara Inicial nível 15 com todas as melhorias.

Salvar por UUID.

Permitir várias varas, cada uma com progressão própria.

---

# 12. ISCAS E POKÉBOLAS

Criar iscas:
- Comum.
- Rápida.
- Rara.
- Gigante.
- Especializada.
- Shiny.
- Evento.

Criar Pokébolas especiais de pesca com diferentes chances de captura.

Tudo configurável:
- Preço.
- Efeito.
- Raridade.
- Chance.
- Requisito.
- Descrição.

Manter opção básica gratuita para evitar que jogadores fiquem impossibilitados de pescar.

---

# 13. TAMANHO DOS POKÉMON

Cada captura terá tamanho individual.

Exemplo:

**Magikarp**
- Tamanho: 4,00 metros.
- Categoria: Recordista.
- Valor: $12.500.

Classificações:
- Pequeno.
- Normal.
- Grande.
- Gigante.
- Recordista.

O tamanho deve ser calculado em relação à espécie.

Permitir multiplicadores por região, isca e evento.

Investigar se é possível representar visualmente o tamanho no mundo usando a API do Cobblemon.

Se não for seguro ou compatível, manter a medida como informação registrada.

---

# 14. ARMAZÉM EXCLUSIVO

Criar armazém de Pokémon pescados.

Capacidade inicial sugerida: 30 espaços.

Permitir expansão.

Cada registro terá:
- Espécie.
- Tamanho.
- Raridade.
- Shiny.
- Região.
- Data.
- Valor.
- Proprietário.
- ID único.

Ações:
- Visualizar.
- Favoritar.
- Vender.
- Vender em lote.
- Devolver ao mar.
- Pesquisar.
- Filtrar.
- Ordenar.

Pokémon favoritos devem ser protegidos contra venda acidental.

Não permitir transferências para Party, PC ou outros sistemas.

---

# 15. ECONOMIA

Utilizar a economia existente do servidor por meio da API apropriada.

Moeda inicial: `impactor:dollars`.

Implementar:
- Venda individual.
- Venda em lote.
- Compra de varas.
- Compra de iscas.
- Compra de Pokébolas.
- Compra de melhorias.
- Compra de tempo.

Valor de venda baseado em:
- Espécie.
- Tamanho.
- Raridade.
- Bônus especiais.

Permitir limites de preço.

Todas as transações precisam ser seguras e auditáveis.

Não permitir pagamento duplicado.

---

# 16. NPCS COM SHIROKAI TRAINERS

Quero usar NPCs existentes ou criados pelo Shirokai Trainers.

Funções:
- Pescador vendedor.
- Comerciante de equipamentos.
- Mestre das varas.
- Instrutor de pesca.
- Entregador de missões.
- Organizador de torneios.

Cada NPC poderá abrir um menu do Zorykai Fishing por meio de integração validada.

As operações continuarão sendo executadas pelo Fishing.

Não presumir suporte a funcionalidades que o Trainers ainda não implementou.

---

# 17. MISSÕES

Criar missões:
- Diárias.
- Semanais.
- De história.
- De região.
- De captura.
- De tamanho.
- De venda.
- De coleção.

Exemplos:
- Capturar 20 Magikarp.
- Vender 10 Pokémon.
- Encontrar um Feebas.
- Capturar 3 Pokémon gigantes.
- Alcançar nível 25 de pesca.

Recompensas:
- XP.
- Dinheiro.
- Iscas.
- Pokébolas.
- Varas.
- Melhorias.
- Tempo adicional.
- Desbloqueios permanentes.

Criar JSON separado para cada missão ou conjunto de missões.

---

# 18. POKÉDEX DE PESCA

Criar Pokédex própria.

Mostrar:
- Espécies encontradas.
- Espécies capturadas.
- Quantidade pescada.
- Maior tamanho.
- Menor tamanho.
- Recorde pessoal.
- Recorde do servidor.
- Capturas shiny.
- Progresso por região.

Adicionar pesquisa, filtros e paginação.

Não alterar a Pokédex oficial do Cobblemon.

---

# 19. RANKINGS E RECORDES

Implementar:
- Maior captura por espécie.
- Maior captura geral.
- Maior nível de pesca.
- Mais capturas.
- Mais espécies diferentes.
- Mais torneios vencidos.

Anunciar recordes no chat.

Criar histórico de recordes e estatísticas.

---

# 20. TORNEIOS E EVENTOS

Criar torneios:
- Maior Pokémon.
- Maior valor.
- Maior quantidade.
- Maior variedade.

Permitir torneios automáticos e administrativos.

Criar eventos especiais por região.

Exemplos:
- Aumento temporário de uma espécie.
- Encontro raro.
- Bônus de XP.
- Bônus de tamanho.
- Tesouros especiais.

Tudo configurável em JSON.

---

# 21. CONQUISTAS, MUSEU E PRESTÍGIO

Criar conquistas por:
- Capturas.
- Espécies.
- Tamanhos.
- Regiões.
- Missões.
- Recordes.
- Torneios.

Criar museu virtual para exibir capturas favoritas e troféus.

Criar prestígio opcional após atingir o nível máximo.

Prestígio não deve apagar varas, capturas, recordes ou desbloqueios permanentes.

---

# 22. TESOUROS DE PESCA

Permitir encontrar tesouros em vez de Pokémon.

Exemplos:
- Iscas.
- Pokébolas.
- Itens.
- Fragmentos.
- Recompensas especiais.

Configurar chances e requisitos por região.

---

# 23. CONFIGURAÇÕES JSON

Quero arquivos separados e fáceis de editar.

Estrutura sugerida:

```text
config/zorykai-fishing/
├── config.json
├── messages.json
├── commands.json
├── timers.json
├── vip-time.json
├── time-shop.json
├── inventory.json
├── permissions.json
├── levels.json
├── rods.json
├── upgrades.json
├── baits.json
├── pokeballs.json
├── capture.json
├── sizes.json
├── prices.json
├── storage.json
├── economy.json
├── npc.json
├── records.json
├── rankings.json
├── achievements.json
├── prestige.json
├── integrations.json
├── worlds.json
├── menus/
│   ├── main.json
│   ├── profile.json
│   ├── storage.json
│   ├── shop.json
│   └── areas.json
├── areas/
│   ├── lago_inicial.json
│   ├── rio_tropical.json
│   └── oceano_profundo.json
├── pokemon/
│   ├── common.json
│   ├── uncommon.json
│   ├── rare.json
│   └── legendary.json
├── missions/
│   ├── daily.json
│   ├── weekly.json
│   └── story.json
├── tournaments/
│   └── example.json
├── events/
│   └── example.json
└── treasures/
    └── default.json
```

Todos os arquivos devem:
- Ser gerados automaticamente.
- Ter exemplos.
- Ser validados.
- Permitir reload.
- Preservar alterações.
- Possuir mensagens de erro claras.
- Suportar migrações e backups.

Separar configurações dos dados persistentes dos jogadores.

---

# 24. COMANDOS ADMINISTRATIVOS

Implementar:

```text
/pescaadmin reload
/pescaadmin validar
/pescaadmin status
/pescaadmin area criar <id>
/pescaadmin area pos1 <id>
/pescaadmin area pos2 <id>
/pescaadmin area entrada <id>
/pescaadmin area salvar <id>
/pescaadmin area listar
/pescaadmin area remover <id>
/pescaadmin tempo ver <jogador>
/pescaadmin tempo add <jogador> <minutos>
/pescaadmin tempo set <jogador> <minutos>
/pescaadmin tempo reset <jogador>
/pescaadmin xp add <jogador> <quantidade>
/pescaadmin vara dar <jogador> <id>
/pescaadmin area desbloquear <jogador> <id>
/pescaadmin area bloquear <jogador> <id>
/pescaadmin missao completar <jogador> <id>
/pescaadmin sessao encerrar <jogador>
/pescaadmin recuperar <jogador>
/pescaadmin debug
```

Implementar permissões administrativas e logs.

---

# 25. SEGURANÇA E PERSISTÊNCIA

Salvar por UUID:
- Inventário original.
- Posição de retorno.
- Estado da sessão.
- Tempo.
- Ciclo.
- Varas.
- XP.
- Melhorias.
- Iscas.
- Pokébolas.
- Capturas.
- Missões.
- Desbloqueios.
- Conquistas.
- Recordes.
- Prestígio.
- Histórico financeiro.

Preferir SQLite com migrações versionadas.

Implementar operações idempotentes.

Proteger contra:
- Duplicação de itens.
- Duplicação de Pokémon.
- Pagamentos duplicados.
- Exploits de reconexão.
- Teleportes externos.
- Captura por outros jogadores.
- Saída durante transações.
- Falhas de inventário.
- Crashes e reinicializações.

---

# 26. TESTES

Criar testes para:
- Entrada e saída.
- Restauração do inventário.
- Desconexão.
- Reinicialização.
- Tempo e renovação.
- Bônus VIP.
- Compra de tempo.
- Áreas bloqueadas.
- Permissões.
- Missões.
- Porcentagens de encontro.
- Varas e XP.
- Captura exclusiva.
- Armazém.
- Venda.
- Rankings.
- NPCs.
- Menus.
- Guard.
- Multiworld.
- Múltiplos jogadores simultâneos.

Testar em servidor dedicado Fabric com Cobblemon 1.8.3.

Não declarar compatibilidade apenas porque o código compilou.

---

# 27. ORDEM DE DESENVOLVIMENTO

## Fase 0 — Auditoria

Analisar os quatro mods de compatibilidade:

1. Shirokai Trainers.
2. Zorykai Menus.
3. Shirokai Guard.
4. Multiworld.

Confirmar APIs, versões e limitações.

Validar também a integração com Cobblemon 1.8.3.

**Não avançar com uma integração inventada.**

## Fase 1 — Base

Implementar:
- `/pesca`.
- Mundo de pesca.
- Entrada.
- Saída.
- Tempo.
- Inventário temporário.
- Persistência.
- Recuperação.

## Fase 2 — Pesca

Implementar:
- Varas.
- XP.
- Iscas.
- Pokémon.
- Pokébolas especiais.
- Captura exclusiva.
- Armazém.

## Fase 3 — Regiões

Implementar:
- Áreas.
- Permissões.
- Missões.
- Tabelas de Pokémon.
- Desbloqueios.

## Fase 4 — Economia e NPCs

Implementar:
- Shirokai Trainers.
- Lojas.
- Venda.
- Melhorias.
- Preços.
- Tesouros.

## Fase 5 — Sistemas avançados

Implementar:
- Pokédex de Pesca.
- Rankings.
- Recordes.
- Torneios.
- Eventos.
- Conquistas.
- Museu.
- Prestígio.

## Fase 6 — Integrações e validação

Testar:
- Shirokai Trainers.
- Zorykai Menus.
- Shirokai Guard.
- Multiworld.
- Cobblemon 1.8.3.

Revisar performance, segurança, recuperação e compatibilidade.

---

# 28. INSTRUÇÕES FINAIS

Quero que você desenvolva o **Zorykai Fishing** como um mod independente, organizado e funcional.

**Utilize exclusivamente Shirokai Trainers, Zorykai Menus, Shirokai Guard e Multiworld como integrações de gameplay.**

Não adicionar outros mods personalizados como dependências.

Não modificar os repositórios existentes sem minha autorização.

Comece pela auditoria das integrações e pela prova de conceito da captura exclusiva do Cobblemon.

Se a implementação exata do Multiworld não estiver identificada, solicite seu nome ou link antes de desenvolver a integração específica.

Desenvolva por fases, entregando ao final de cada uma:

- Código-fonte.
- Testes.
- Resultado da compilação.
- JSONs.
- Documentação.
- Limitações reais.
- Próximos passos.

**Objetivo final:** criar um sistema completo de pesca Pokémon, com mapa próprio, tempo de permanência, áreas desbloqueáveis, varas evolutivas, captura exclusiva, economia, missões, NPCs, menus e progressão permanente, sem conflitos com os quatro mods escolhidos.