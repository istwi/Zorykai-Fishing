# Instalação rápida

1. Coloque **somente** o JAR principal em `mods/` de uma cópia do servidor (MC1.21.1, Java21, Fabric e Cobblemon1.8.3).
2. Inicie para gerar a pasta `config/zorykai-fishing/`.
3. Crie/carregue a dimensão de pesca no Multiworld, manualmente. Exemplo: `/mw create pesca NORMAL`, depois `/mw tp pesca`.
4. Construa o local, selecione o volume inteiro com `/pescaadmin mapa pos1` e `pos2`, marque `mapa entrada` em chão seguro e use `mapa salvar`.
5. Para cada lago: `area criar <id>`, `area pos1 <id>`, `area pos2 <id>`, `area salvar <id>`.
6. Edite a tabela de Pokémon de cada arquivo em `areas/`, mantenha a soma 100, e use `/pescaadmin validar` e `/pescaadmin reload`.
7. Configure permissões VIP e confira as permissões/interações/entrada/saída no Guard. O Fishing não abre regiões protegidas.
8. Use `/pesca entrar`, pesque e teste `/pesca sair` antes de liberar aos jogadores.

O ZIP de configurações não contém mapa ativo nem lago instalado. Não substitua JSONs personalizados sem comparar. Exemplos de NPC/menu são optativos e precisam ser instalados nas pastas correspondentes; veja README.md.

Para o servidor real, ainda valide com seu cliente, autenticador, dimensão construída e modpack. Os JARs de teste `checks` criam dados artificiais e encerram o runtime: não devem ir para produção.

Atualizando da beta anterior: troque somente o JAR, preserve banco e JSONs; veja `ATUALIZACAO_0.2.1.md`. A recarga começa em 10s e as melhorias ficam em `/pesca recarga`.
