# Especificação: Hierarquia do Organograma Paroquial (Clero no Topo)

## 1. Visão Geral (Overview)
Reestruturar a hierarquia organizacional do sistema para refletir corretamente a governança paroquial. O Clero Paroquial (liderado pelo Pároco) assume a posição de nó raiz da árvore. Abaixo dele respondem o Conselho Pastoral Paroquial e o Conselho Econômico Paroquial. Abaixo do Conselho Pastoral, ramificam-se todas as Pastorais e Movimentos.

## 2. Requisitos Funcionais

### Backend & Banco de Dados
* Adicionar o tipo `CONSELHO_ECONOMICO` ao enum `TipoOrganizacao`.
* Atualizar o caso de uso `GetOrganogramaUseCase` para montar a árvore na seguinte ordem de herança/parentesco:
  - **Nó Raiz:** Clero Paroquial (`TipoOrganizacao.CLERO`).
  - **Nós Filhos do Clero:** Conselho Pastoral (`TipoOrganizacao.CONSELHO`) e Conselho Econômico (`TipoOrganizacao.CONSELHO_ECONOMICO`).
  - **Nós Filhos do Conselho Pastoral:** Todas as pastorais e movimentos (`TipoOrganizacao.PASTORAL` e `TipoOrganizacao.MOVIMENTO`).
* Atualizar o script de dados inicial `seed-data.sql` para cadastrar o Conselho Econômico com tipo `CONSELHO_ECONOMICO`, com coordenador "Pedro Finanças" e vice "Ana Administração", vinculando membros fictícios.

### Frontend (Interface)
* Na tela de Organograma (`Organograma.tsx`):
  - Mapear a tradução e exibir "Conselho Econômico Paroquial" para o tipo `CONSELHO_ECONOMICO`.
  - Associar a cor verde escuro (#2e7d32) para a liderança e membros do conselho econômico.
  - **Modo Árvore:** Renderizar a árvore multinível de forma automática através da resposta estruturada do backend. O fluxo exibirá o card do Clero no topo, ramificando para os dois conselhos (Pastoral e Econômico) no nível médio, e o Conselho Pastoral expandindo em conexões para as Pastorais no nível inferior.
  - **Modo Grade:** Apresentar a grade organizada, com o Clero na primeira seção e os dois conselhos lado a lado (Clero como administrador geral no topo e os conselhos logo abaixo).

## 3. Critérios de Aceite
* O endpoint do organograma retorna a árvore reestruturada com Clero no topo.
* A visualização em Árvore exibe corretamente as conexões multiníveis (Clero -> Conselhos -> Pastorais).
* Todos os testes automatizados do backend e frontend passam com sucesso.
