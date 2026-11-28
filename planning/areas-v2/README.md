# Proposta das seis áreas e dificuldade de pesca

**Material para revisão. Ainda não foi aplicado ao mod, ao JAR ou ao servidor.**

[Baixar a planilha](../../downloads/Zorykai-Fishing-6-Areas-e-Dificuldade.xlsx)

## Ordem das áreas

| Ordem | Região | Print | Espécies |
|---:|---|---:|---:|
| 1 | Lago Florestal | 2 | 19 |
| 2 | Lago das Cerejeiras | 4 | 19 |
| 3 | Lago Tropical | 1 | 19 |
| 4 | Pântano Antigo | 3 | 19 |
| 5 | Lago Glacial | 5 | 19 |
| 6 | Caverna Cristalina | 6 | 20 |

As 115 espécies da proposta anterior foram distribuídas uma única vez. Cada área soma 100% de chance. O grupo de raridade e a chance são próprios daquela área de pesca.

## Regras consideradas

- Água sem uma área especial usa o Tier 1.
- Na dimensão de pesca, Pokémon naturais ficam bloqueados.
- Apenas encontros gerados pelo sistema de pesca aparecem.
- As partículas de demarcação ficam desligadas.
- Wailord, Dondozo, Gyarados, Milotic e Lapras exigem ponto de água profunda.

## Dificuldade do minigame

A dificuldade combina área, raridade, peso da espécie e tamanho sorteado. O peso usa escala logarítmica para que Pokémon gigantes sejam mais difíceis sem ficarem impossíveis. O tamanho é medido dentro da faixa individual de cada espécie, do mínimo ao recordista.

Durante o estado **Calmo**, recolher aumenta o progresso e a tensão. Durante **Nadando**, o peixe puxa: o jogador deve dar folga, e o progresso diminui por segundo. Se o jogador ficar parado por tempo demais durante a calmaria, a linha fica frouxa e o progresso também diminui. Tensão 100 rompe a linha.

Peixes mais difíceis:

- têm limite seguro de tensão menor;
- precisam de mais progresso;
- dão menos progresso por ação;
- entram mais cedo e permanecem mais tempo no estado Nadando;
- retiram mais progresso ao nadar ou quando a linha fica frouxa.

Resistência da vara reduz tensão recebida; Precisão aumenta progresso por ação. Upgrades não removem a fase Nadando e não ultrapassam o teto de tamanho da espécie.

## Arquivos

- `pools-seis-areas.csv`: tabela simples para abrir no navegador ou em qualquer editor de planilhas.
- `proposta-seis-areas.json`: fotografia dos pools e parâmetros para a futura implementação.
- `validacao-seis-areas.json`: contagens, somas e origem dos dados.
- `gerar_planilha.py`: gerador reproduzível da planilha.
- `resumo.png`: prévia visual da primeira aba.

Validação atual: 115 espécies únicas; áreas com 19/19/19/19/19/20 espécies; todas as chances e perfis de tamanho somam 100%; seis fórmulas recalculadas sem erros no LibreOffice. Isso valida a proposta e a planilha, não o comportamento dentro de um servidor real.
