# Fishing 0.2.1-beta.1

Atualização para servidor Fabric 1.21.1, Java 21, Cobblemon 1.8.3. Não precisa de Fishing no cliente.

## Instalar

Pare o servidor, faça backup do mundo e de `config/zorykai-fishing/`, remova o JAR anterior e instale somente `Zorykai-Fishing-0.2.1-beta.1.jar`. Preserve o SQLite e seus arquivos associados. Reinicie; não use reload para trocar JARs. Os novos JSONs ausentes são gerados automaticamente. Nenhum mundo/dimensão é criado automaticamente.

Não substitua suas configurações inteiras pelos exemplos. As tabelas de peixes, capturas, inventários reservados, permissões e dados anteriores continuam válidos. Não instale JARs `checks` ou `dev`.

## Bola e recarga

A bola física é um ícone reutilizável de uma unidade; bolas pagas continuam consumindo **estoque virtual** por lançamento aceito. O estoque pode ser consultado no menu. Com estoque zero, o ícone permanece na mão, mas o lançamento é recusado. A bola básica continua infinita.

A atualização força o envio do inventário/mãos, inclusive após uso e colisão do projétil, e repete a sincronização no tick seguinte. Isso corrige a previsão de consumo do cliente sem consumir o ícone nem depender de abrir o inventário. Cliques durante a recarga não consomem estoque.

Padrão inicial: **10 segundos**. Em `config/zorykai-fishing/capture.json`, acrescente/edite apenas:

```json
"ballCooldownSeconds": 10
```

Lembre de usar vírgula entre as propriedades do JSON. Se o campo não existir, o padrão também é 10. Depois, `/pescaadmin reload`.

A action bar (mesmo lugar da tensão) exibe 20 letras `I`, inicialmente cinza escuro, ficando azuis da esquerda para a direita. A recarga tem prioridade sobre tensão/tempo. Ao concluir aparece a barra cheia e depois retorna a informação normal. **Sem boss bar de recarga.** A recarga é por jogador, compartilhada entre todas as bolas, não pode ser reiniciada trocando de bola e uma compra durante uma recarga não encurta o lançamento já em andamento.

## Loja de melhorias

Acesse `/pesca recarga`, o botão de recarga no menu principal interno ou **Melhorias → Melhorar recarga das bolas**. Para NPC de comando via console: `pesca abrir recarga <jogador>`. No Zorykai Menus use a ação `zorykai_fishing:recarga`; o exemplo atualizado inclui esse botão, mas não altera seu menu externo automaticamente.

`config/zorykai-fishing/cooldown-shop.json`:

```json
{
  "schemaVersion": 1,
  "enabled": true,
  "name": "Recarga Acelerada",
  "price": 2000,
  "maximumLevel": 5,
  "secondsPerLevel": 1,
  "minimumCooldownSeconds": 2
}
```

Por padrão: nível 0 = 10s, 1 = 9s, 2 = 8s, 3 = 7s, 4 = 6s e 5 = 5s. Os preços são $2.000, $4.000, $6.000, $8.000 e $10.000 (preço base × próximo nível). Tudo configurável. O mínimo de 2s é uma proteção para ajustes futuros; o padrão de cinco níveis chega a 5s.

A melhoria é permanente por jogador, salva no banco, e vale para todas as bolas do Fishing. Não ocupa um item ou mexe nas bolas comuns fora da pesca. `enabled: false` desativa novas compras e preserva melhorias já compradas. Impactor é necessário para compras pagas; usa a moeda configurada no Fishing. A entrega usa o journal existente, com bloqueio de compras concorrentes e preservação de resultados incertos.

A recarga base aceita 0,5–120 segundos; o mínimo deve ser >=0,5 e <=tempo base. O máximo de níveis é 1–100. Um próximo nível que não reduzir mais o tempo não pode ser comprado.

## Fisgada e perímetro

O aviso de fisgada passa a ser som de água e partículas de splash/bolhas **na posição da boia**, enviados apenas ao pescador. O texto “Fisgou!” não aparece no chat. A lógica de fisgada/peixes/bolhas do Minecraft é cancelada nas boias gerenciadas pela sessão; a linha e boia continuam visíveis. Varas comuns fora desse sistema mantêm o comportamento vanilla.

Em todo o **volume 3D** salvo em `mapa.json`, não apenas nos lagos, novos Pokémon são recusados, exceto encontros registrados pelo Fishing. Isso inclui o envio de Pokémon da Party nessa área, cancelado antes da criação da entidade. Fora do volume/dimensão, o spawn não muda. Uma verificação por segundo limpa Pokémon selvagens já carregados que entrem no volume; Pokémon com dono são recolhidos, preservando seus dados. NPCs não são Pokémon e continuam funcionando. Marque a altura do perímetro de acordo com a sua construção.

## Bote temporário

O terceiro slot recebe o **Bote de Pesca**. Mire na água próxima (até 6 blocos) e use o item para colocar; clique no bote para embarcar e use Shift para desembarcar. Use o item novamente para recolher o bote; Shift + clique no próprio bote também recolhe. O ícone permanece no inventário para colocar novamente.

Existe somente um bote por jogador/sessão, apenas o dono pode embarcar. Não quebra em itens nem gera barcos permanentes. É removido ao sair, morrer, desconectar ou encerrar a sessão, e barcos órfãos são descartados quando carregados após reinício. Caso saia do perímetro ou perca permissão de entrada, é recolhido. O Guard continua sendo consultado e não recebe bypass genérico. Jogadores e inventários originais são restaurados pelo mecanismo já existente.

`config/zorykai-fishing/navigation.json`:

```json
{"schemaVersion":1,"boatEnabled":true}
```

Configure `false` para desativar novos botes; o equipamento é atualizado no próximo equip/entrada da sessão.

## Validação

Consulte os resultados em `docs/tests/` e `VALIDACAO_BETA.md`. Testes usam servidor Fabric dedicado isolado, entidades reais, callbacks e pacotes de saída com jogadores automatizados. Não substituem conferir renderização, som, movimento do barco e interação com o modpack no seu cliente real. Nenhuma alteração foi aplicada no servidor de produção.
