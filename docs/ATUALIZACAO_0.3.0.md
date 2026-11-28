# Fishing 0.3.0-beta.1 — captura animada, arsenal e coleção

Atualização de servidor para Fabric 1.21.1, Java 21 e Cobblemon **1.8.3**. Pare o servidor, faça backup e substitua somente o JAR anterior. Preserve `config/zorykai-fishing/` e o banco de dados do mundo. Não precisa instalar Fishing no cliente; os modelos/itens/animações vêm do Cobblemon já instalado.

## Captura com a animação do Cobblemon

As bolas de pesca passam pela sequência nativa de impacto, raio de recolhimento, queda, balanços e confirmação de captura. O peixe só é registrado ao final bem-sucedido da sequência. A chance continua sendo a configurada no Fishing e considera o tamanho do encontro. Não é uma captura garantida por haver animação.

O resultado continua exclusivo da coleção do Fishing. Não envia o Pokémon para Party, PC ou Pokédex oficial; não dispara o evento nativo de Pokémon capturado nem aplica recompensas de captura do jogo normal. Pokémon comuns fora da sessão não recebem esse comportamento.

Você pode caminhar dentro do perímetro da pesca durante uma tentativa já aceita. Isso não libera capturas remotas: o impacto precisa ser válido, o encontro precisa ser seu e a mesma sessão deve continuar ativa. Sair do perímetro/dimensão cancela a tentativa.

Um encontro aceita uma tentativa de animação por vez. Enquanto a bola estiver capturando, novos lançamentos do jogador não debitam estoque. O sistema não permite encurtar a animação usando o comando/caminho de captura de prova. A bola física continua sendo um ícone reutilizável; o consumo de estoque finito por lançamento aceito e a recarga continuam os anteriores.

Se a bola falhar, o Pokémon volta a ficar visível e a trava da tentativa é liberada. A chance de escapar configurada em `capture.json` continua válida. Cancelamento da sessão, desconexão, expiração do encontro ou encerramento limpam as tentativas. Uma tentativa interrompida por reinício não é transformada em captura bem-sucedida: só um registro confirmado no banco vale.

A integração usa a API/estados internos do Cobblemon 1.8.3. Não use este JAR como compatível com outra versão do Cobblemon sem validação.

## Varas com modelos reais e upgrades

As três varas já existentes ganham os modelos nativos:

| ID existente | Modelo padrão |
|---|---|
| `inicial` | `cobblemon:poke_rod` |
| `boa` | `cobblemon:great_rod` |
| `super` | `cobblemon:ultra_rod` |

Seus níveis, experiência, propriedade e upgrades não são resetados. Os limites, preços e efeitos não foram aumentados automaticamente. A vara equipada continua usando o minigame do Fishing; não ativa a pesca comum do Cobblemon ou do Minecraft em paralelo.

Em `rods.json`, cada definição pode ter `"item": "cobblemon:poke_rod"` para definir sua aparência. Nos arquivos antigos que não possuem `item`, os três IDs acima usam os modelos correspondentes automaticamente. Outros IDs usam `poke_rod` como padrão. Só aceita itens reais derivados de vara de pesca; um item inválido é recusado antes de cobrar a compra/antes de reservar o inventário na entrada.

Uma descrição própria da vara pode ser adicionada com `"description": "Uma companheira de muitas marés..."`. Isso só altera o texto da loja.

Na página **Arsenal de Varas**, equipe a vara desejada e abra **Aprimorar vara equipada**. A **Oficina do Pescador** mostra:

- **Carretilha:** velocidade até a fisgada.
- **Anzol:** sorte para os encontros raros.
- **Linha reforçada:** resistência, reduzindo a tensão recebida ao recolher.
- **Empunhadura:** precisão, aumentando o progresso por movimento.

Cada upgrade pertence à vara selecionada. A tela apresenta nível atual/máximo, bônus e preço do próximo nível. O preço segue `preço base × próximo nível`, igual ao sistema existente. O nível máximo não pode ser comprado novamente. Compra/recuperação usa o journal financeiro existente; não concede duas melhorias pelo mesmo pagamento.

O bônus de 10% por nível aumenta o atributo; isso não significa dez pontos percentuais de chance ou redução absoluta de dez segundos. Os efeitos reais preservam as fórmulas já utilizadas pelo mod. A loja de recarga das bolas é separada e continua acessível pelo menu.

## Ícones 3D dos Pokémon

Coleção, detalhes da captura, favoritos/museu e caderno de espécies usam o `PokemonItem` nativo do Cobblemon para representar a espécie em 3D. Capturas shiny usam o aspecto shiny. Criar esses ícones não cria Pokémon no mundo nem adiciona Pokémon aos jogadores. O tamanho real do exemplar fica na descrição; o ícone usa a apresentação de inventário do Cobblemon.

Os modelos são construídos diretamente com os dados de espécie/aspecto, sem criar um Pokémon temporário a cada ícone.

As telas continuam controladas pelo servidor: os ícones não podem ser retirados para o inventário, arrastados ou usados como itens reais. Se uma espécie antiga não existir mais no modpack, o registro continua preservado e usa um ícone alternativo; a ausência do modelo não apaga a captura.

Se você já configurou `buttons.<slot>.item` num JSON de menu, esse override continua sendo respeitado e pode substituir o modelo. Remova o override se quiser usar o ícone nativo definido pelo mod.

## Menu e descrições

O menu principal passa a ter grupos de navegação, ícones correspondentes e moldura discreta. As páginas usam nomes como **Mapa dos Lagos**, **Coleção de Capturas**, **Arsenal de Varas**, **Oficina do Pescador**, **Pedidos da Vila** e **Galeria de Memórias**. Os comandos existentes, incluindo `/pesca`, não foram renomeados.

As descrições ganham pequenas histórias, números organizados e instruções de uso. A coleção mostra o nome do Pokémon sem namespace técnico, lago de origem, tamanho, raridade, shiny, preço e proteção contra venda. Os IDs internos das capturas não ocupam a descrição comum.

As lojas de varas, bolas, iscas, melhorias e tempo passam a ter paginação, ações de seleção e reposição separadas e confirmação antes de compras. A tela de confirmação continua exigindo uma ação explícita; fechar o menu não autoriza compras.

## Seus JSONs personalizados

Nenhum arquivo de menu existente é apagado ou reescrito. Títulos que ainda tenham exatamente o padrão antigo gerado pelo mod ganham o nome mais amigável em execução. Um título personalizado permanece respeitado. `buttons.<slot>.name`, `item`, `lore`, `slot` e `enabled` continuam disponíveis.

Você pode adicionar `"story": "Uma pequena história para esta página..."` no JSON da página para trocar seu texto introdutório padrão. Cores `&9`, `&7`, `&f` e hexadecimal `&#RRGGBB` são processadas em nomes e lore. Um `lore` explicitamente personalizado no botão substitui a descrição automática.

Se o seu `/pesca` principal é aberto pelo **Zorykai-Menu**, o mod continua respeitando esse menu externo. O exemplo renovado está em `examples/zorykai-menus/fishing_main.json`; para usar esse visual, compare/instale esse arquivo em `config/zorykaimenus/menus/fishing_main.json` e recarregue o Zorykai-Menu. O Fishing não sobrescreve um menu externo personalizado automaticamente. As páginas internas de coleção/lojas/oficina são atualizadas pelo JAR.

Para configurar menus, varas e preços use os mesmos arquivos e `/pescaadmin reload`. Para trocar o JAR, reinicie o servidor. Recomenda-se comparar os exemplos novos, sem sobrescrever seu pacote inteiro de configurações.

## Validação

Consulte `VALIDACAO_BETA.md` e `docs/tests/030-*`. Os testes automatizados verificam os estados reais da Poké Bola com ticks do mundo, registro somente no final, falha, cancelamento, ausência de alterações na Party/PC e proteção dos ícones de menu. Também testam o item real de cada vara, o efeito de uma melhoria no recolhimento e uma queda forçada com captura em andamento, sem fabricar registro após reconectar.

Não houve ensaio gráfico humano no cliente do usuário nem teste com todo o seu modpack. É necessário conferir visualmente animações, modelos, contraste da lore e eventuais configurações de renderização do cliente. Os testes não constituem garantia de ausência de bugs em todas as combinações de mods. Nenhuma alteração foi aplicada em produção.
