# Fishing 0.2.3-beta.1 — identificação dos lagos e borda na água

Com o servidor parado, substitua o JAR anterior. Preserve banco e configurações. O novo `config/zorykai-fishing/area-feedback.json` é gerado automaticamente. Não precisa instalar Fishing no cliente.

## Nome da área e sininho

Durante uma sessão ativa de pesca, entrar num lago cadastrado mostra seu `name` como subtítulo discreto, sem título grande, durante aproximadamente dois segundos. Toca um sininho suave apenas para o próprio jogador. Não usa a action bar, que continua disponível para recarga, tensão e progresso.

O jogador precisa permanecer no lago por 10 ticks (0,5 segundo a 20 TPS) para confirmar a entrada. Permanecer dentro não repete o aviso. Sair e entrar novamente gera um novo aviso depois dessa estabilidade, sem mensagens no chat. O nome é configurado no JSON do lago em `areas/`. Se os requisitos não forem atendidos, aparece em vermelho com “pesca bloqueada”. O sistema considera até dois blocos acima do volume cadastrado para identificar quem estiver na margem; isso não amplia a área real em que a boia pode pescar.

## Partículas somente na água

O contorno é uma indicação privada, feita com pequenos pontos sobre a superfície da água descoberta **dentro dos limites de um lago cadastrado**. Não desenha o retângulo no chão, não cria blocos nem entidades. As amostras ficam junto à margem da água ou ao limite cadastrado quando a água continua além dele.

- Azul: requisitos de nível, permissão, missão, vara e isca selecionadas atendidos.
- Vermelho: lago cadastrado, mas requisitos não atendidos.
- Fora de lagos cadastrados, não há marcação, mesmo que exista água.

A indicação não modifica permissões nem amplia limites. A regra real continua sendo a posição da boia dentro do volume do lago. A marcação não substitui as verificações de estoque e demais condições de uma tentativa de pesca.

## Limites de custo

Padrões por jogador: raio de 12 blocos, no máximo 12 partículas por atualização, uma atualização a cada 20 ticks (um segundo a 20 TPS). A busca avalia no máximo 128 colunas por atualização, com faixa vertical limitada a quatro blocos acima/abaixo do jogador; a posição de amostragem gira para cobrir o trecho próximo. Os limites não crescem com o perímetro total. Só consulta chunks já carregados e só envia partículas ao próprio jogador.

Áreas grandes não recebem uma linha contínua de partículas: aparece uma indicação leve do trecho próximo, não o contorno inteiro permanente. Sem água descoberta/borda próxima pode não aparecer nenhum ponto. A seleção de partículas do cliente também influencia sua aparência. Não foi realizado benchmark com dezenas/centenas de jogadores; o limite de emissão foi verificado automaticamente.

## Configuração

`config/zorykai-fishing/area-feedback.json`:

```json
{
  "schemaVersion": 1,
  "entryEnabled": true,
  "entrySound": true,
  "entryStableTicks": 10,
  "entryHeightTolerance": 2,
  "markersEnabled": true,
  "markerRadius": 12,
  "markerIntervalTicks": 20,
  "maximumParticles": 12
}
```

Use `/pescaadmin reload` após editar. `entrySound: false` desliga apenas o sininho. `entryEnabled: false` desliga o aviso de entrada e seu som. `markersEnabled: false` desliga as partículas. É possível manter o nome e som com as partículas desligadas.

Para deixar ainda mais leve: `markerRadius: 8`, `markerIntervalTicks: 40`, `maximumParticles: 6`. Isso mostra no máximo seis partículas a cada dois segundos para cada jogador próximo.

Valores aceitos: estabilidade de 5–100 ticks, tolerância vertical de 0–4 blocos, raio de 4–24 blocos, intervalo de 20–200 ticks em múltiplos de 5 e limite de 1–24 partículas. Reload com valores inválidos preserva as regras anteriores.

## Verificação

Os relatórios `023-*` em `docs/tests/` registram a compilação e testes automatizados. O ensaio com servidor Fabric e pacotes reais verifica aviso/subtítulo e sino emitidos uma vez, partículas limitadas por jogador, origem exclusivamente sobre água dentro de lago e opção de desligar as partículas. Os ensaios anteriores de pesca, captura, inventário e reinício também são executados.

Ainda é necessário conferir no cliente real o tamanho/posição do subtítulo, volume do sino e aparência das partículas com seu modpack e configuração gráfica. Nenhuma alteração foi aplicada no servidor de produção.
