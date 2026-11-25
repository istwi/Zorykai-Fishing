# Fishing 0.2.4-beta.1 — nome da área, sinal azul e recolhimento livre

Pare o servidor e troque o JAR, preservando banco, áreas e demais configurações. Não precisa de novo mod no cliente. As áreas antigas continuam válidas e mantêm seus IDs, tabelas de Pokémon, requisitos e posições. Não é necessário recriá-las.

## Configurar o aviso de cada área

No arquivo existente `config/zorykai-fishing/areas/<id>.json`, adicione esta propriedade **dentro do objeto principal**, mantendo os outros campos do arquivo:

```json
"display": {
  "enabled": true,
  "title": "",
  "subtitle": "&9Lago Azul &7· {player}{status}",
  "blockedSuffix": "&c · pesca bloqueada",
  "sound": true,
  "fadeInTicks": 5,
  "stayTicks": 60,
  "fadeOutTicks": 10
}
```

Atenção às vírgulas entre propriedades do JSON. Esse bloco é uma propriedade a acrescentar ao seu arquivo, não um JSON de área completo para substituir todos os dados.

- `title`: linha grande do Minecraft; deixe vazia para manter um aviso pequeno.
- `subtitle`: linha menor, onde aparece o nome no padrão atual.
- `enabled`: liga/desliga o aviso deste lago, respeitando também a opção global.
- `sound`: liga/desliga o sininho deste lago, respeitando também a opção global.
- `fadeInTicks`, `stayTicks`, `fadeOutTicks`: entrada, permanência e saída do aviso. A 20 TPS, 20 ticks equivalem a 1 segundo; `stayTicks: 60` mantém a mensagem por 3 segundos, além das transições.
- `blockedSuffix`: texto usado no placeholder `{status}` se os requisitos de pesca não forem atendidos.

Placeholders locais: `{area}` usa o campo `name` do lago; `{player}` usa o nick; `{status}` fica vazio quando liberado ou mostra `blockedSuffix` quando bloqueado. Para preservar o aviso de bloqueio com um nome fixo, inclua `{status}` em `subtitle`.

Cores: `&9` azul, `&b` aqua, `&7` cinza, `&c` vermelho, `&l` negrito e `&r` para resetar. Também aceita hexadecimal como `&#33AAFF`. Não são executados comandos nem placeholders de outros mods.

Exemplo de duas linhas: `title: "&9Lago Azul"` e `subtitle: "&7Prepare sua vara, {player}!{status}"`. O tamanho das linhas é o padrão do Minecraft; não existe escala de fonte arbitrária neste pacote de servidor.

Depois, use `/pescaadmin reload` e saia/entre na área para conferir. Áreas novas criadas pelo mod já incluem o bloco `display`. Em arquivos antigos, a ausência do bloco usa os padrões; um bloco parcial mantém os padrões dos campos omitidos. Um nome em `display` não renomeia o ID nem altera registros de capturas. A duração aceita até 600 ticks de permanência e até 200 ticks por transição; textos têm limites para rejeitar configuração inválida no reload.

## Borda da água mais visível

O padrão agora combina pontos coloridos maiores com brilhos brancos, ligeiramente acima da água. A indicação reaproveita posições válidas próximas e as revalida a cada atualização, evitando sumir só porque a amostragem passou para outra parte do lago. Continua exclusivamente sobre água descoberta dentro da área cadastrada, apenas para o jogador e sem criar blocos/entidades.

Em `area-feedback.json`, os campos novos são opcionais em configurações antigas:

```json
"markerStyle": "bright",
"markerScale": 1.5,
"markerHeight": 0.18
```

`bright` combina ponto colorido e brilho branco; `dust` usa só o ponto colorido. A escala aceita 0,5–3; altura acima da superfície aceita 0,05–0,5 bloco. O brilho branco tem tamanho próprio do Minecraft; a escala altera o ponto colorido.

O teto padrão continua sendo **12 partículas por atualização por jogador**, uma vez por segundo, incluindo os dois tipos. No estilo `bright`, isso normalmente representa seis locais com duas partículas cada. Se o cliente estiver configurado para reduzir partículas, a aparência pode continuar mais discreta; confira também as opções gráficas do seu modpack.

## Sinal na boia e quando recolher

Ao fisgar, aparece um grupo de partículas azuis grandes no bloco de água em que está a boia. O sinal azul se repete enquanto for hora de recolher. Durante a puxada forte do peixe ou com tensão a partir de 80, muda para vermelho: dê folga. Isso acompanha as fases do minigame; não substitui os limites de tensão e progresso.

Em `capture.json`, opções novas (ausentes usam os padrões):

```json
"bobberSignalEnabled": true,
"bobberSignalIntervalTicks": 10,
"bobberSignalScale": 1.4
```

O sinal é privado, com quatro partículas por emissão, no máximo a cada 10 ticks na repetição periódica. Transições de fase/fisgada também emitem o aviso imediatamente. Intervalos aceitos de 10–40 ticks e escala de 0,5–3. O limite das partículas da borda é separado deste sinal na boia. Não há novas mensagens repetidas no chat.

## Recolher sem ficar preso

- Antes da fisgada: outro clique direito com a vara recolhe e remove a boia. Pode lançar de novo.
- Depois da fisgada: clique direito recolhe pelo minigame; **Shift + clique direito** cancela a tentativa em qualquer fase e remove a boia.
- Shift sem uma linha ativa não lança outra boia acidentalmente.

Cancelar não cria encontro, captura, dinheiro, progresso ou reembolso automático de uma isca já consumida. Um encontro que já saiu da água continua precisando ser capturado/resolvido antes de iniciar outra pescaria. O controle evita tratar os dois pacotes de um mesmo clique como lançar e recolher imediatamente.

## Evidência

Relatórios `024-*` em `docs/tests/` registram os testes automatizados, incluindo compatibilidade de áreas antigas, bloco parcial de display, cores/hex, tempo do aviso, tipos e limite das partículas, sinal na fisgada e cancelamento antes/durante a disputa. Os cenários anteriores de inventário, captura, loja, bote e queda/reconexão também são executados.

Não houve teste gráfico humano nem benchmark de FPS. É necessário conferir a visibilidade, os sinais e os controles no seu cliente real. Nenhuma alteração foi aplicada em produção.
