# Fishing 0.2.2-beta.1 — disputa com o peixe

Troque somente o JAR com o servidor parado. Preserve banco, configurações, capturas e inventários reservados. O novo `config/zorykai-fishing/minigame.json` é criado automaticamente no primeiro início. Após editar esse arquivo, use `/pescaadmin reload`. Os recursos da 0.2.1 (bola reutilizável, recarga de 10s, loja de melhorias e bote) continuam disponíveis.

## Como jogar

Depois do som de fisgada, clique com a vara para iniciar o recolhimento. A action bar apresenta duas barras de dez letras `I`: **Tensão** e **Progresso**. O comprimento é fixo; as letras mudam de cor conforme os valores. A tensão fica verde abaixo de 60, amarela a partir de 60 e vermelha a partir de 80. O progresso preenche em azul. Ao completar o progresso, o encontro de pesca é criado normalmente; as regras de captura e armazenamento não mudam.

- **Calmo · Recolha:** cada clique rende progresso com a tensão normal. Os períodos calmos duram entre 4 e 7 segundos, sorteados a cada fase.
- **Puxando! Dê folga:** som e partículas privados na boia avisam a puxada, que dura entre 1,5 e 2,5 segundos. O progresso recua 6 pontos por segundo. Insistir em recolher dá pouco progresso e dobra a tensão por clique. Dar folga é a escolha segura durante a puxada.
- **Linha frouxa! Recolha:** durante uma fase calma, se a tensão estiver em 15 ou menos e você não recolher por mais de 3 segundos, o progresso começa a cair 4 pontos por segundo.

Ao terminar uma puxada, há **2 segundos de tolerância** antes de poder sofrer a perda por linha frouxa. Uma pausa curta para baixar a tensão não tira progresso imediatamente. A barra nunca fica negativa. Tensão em 100 ainda arrebenta a linha; o prazo de validade da linha e o prazo para reagir à fisgada continuam os existentes.

O progresso padrão por clique usa `capture.json.clickProgress` (20) multiplicado por 0,75 na fase calma (15 pontos) ou 0,25 durante a puxada (5 pontos), antes dos bônus de precisão da vara. Resistência continua reduzindo a tensão recebida. Não é preciso segurar botão nem instalar um mod novo no cliente.

A recarga da bola mantém prioridade na action bar enquanto estiver ativa. Quando termina, a interface volta à disputa/tempo. As mudanças de fase não geram mensagens no chat. Cliques muito rápidos continuam limitados; repetir atualizações dentro do mesmo tick não acelera a recuperação de tensão nem multiplica as perdas.

## Configuração padrão

`config/zorykai-fishing/minigame.json`:

```json
{
  "schemaVersion": 1,
  "calmMinSeconds": 4,
  "calmMaxSeconds": 7,
  "pullMinSeconds": 1.5,
  "pullMaxSeconds": 2.5,
  "idleGraceSeconds": 3,
  "recoveryGraceSeconds": 2,
  "pullLossPerSecond": 6,
  "slackLossPerSecond": 4,
  "slackTensionThreshold": 15,
  "calmProgressMultiplier": 0.75,
  "pullProgressMultiplier": 0.25,
  "pullTensionMultiplier": 2
}
```

Para facilitar, reduza as perdas ou aumente as tolerâncias. Para desativar somente uma perda, coloque a taxa correspondente em `0`. Valores são **pontos da barra por segundo**, não porcentagens multiplicadas pelo progresso atual. A redução de 6 retira 6 pontos tanto de 80 quanto de 20.

Limites validados: fase calma de 1–30s; puxada de 0,5–10s; tolerância ociosa de 1–30s; tolerância pós-puxada de 1–10s; perdas de 0–25 pontos/s; limite frouxo de 0–50; multiplicador calmo maior que 0 e até 2; multiplicador de progresso na puxada de 0–1; multiplicador de tensão na puxada de 1–5. Máximo de duração não pode ser menor que mínimo. Um reload inválido preserva a configuração anterior.

## Testes e instalação

Consulte `VALIDACAO_BETA.md` e os relatórios `022-*` em `docs/tests/`. Os testes automatizados usam modelos determinísticos e servidor Fabric isolado com entidades/callbacks reais. Aparência, áudio, latência e sensação de dificuldade ainda precisam ser conferidos no seu cliente real. Não houve alteração em produção.
