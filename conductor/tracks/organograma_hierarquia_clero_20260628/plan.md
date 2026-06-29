# Plano de Implementação: Hierarquia do Organograma Paroquial

## Phase 1: Backend Model, Service and Seed Setup (TDD)
- [x] Task: Atualizar o enum TipoOrganizacao e entidades backend (TDD) (b96f4ad)
    - [x] Adicionar valor `CONSELHO_ECONOMICO` no arquivo `TipoOrganizacao.java` (b96f4ad)
    - [x] Atualizar serializadores e conversores se necessário (b96f4ad)
- [x] Task: Implementar o seed de dados no script `seed-data.sql` (c691649)
    - [x] Adicionar insert do 'Conselho Econômico Paroquial' com tipo 'CONSELHO_ECONOMICO', associando lideranças 'Pedro Finanças' e 'Ana Administração', e membros ativos (c691649)
- [x] Task: Reestruturar a árvore no caso de uso `GetOrganogramaUseCase` (TDD) (a68abda)
    - [x] Alterar `GetOrganogramaUseCase.java` para montar a hierarquia multinível (Clero como raiz, Conselhos como filhos diretos do Clero, Pastorais como filhas do Conselho Pastoral) (a68abda)
    - [x] Escrever novos testes unitários em `GetOrganogramaUseCaseTest.java` validando a herança e estrutura multinível da árvore (a68abda)
- [x] Task: Executar testes automatizados do backend (a68abda)
    - [x] Rodar `./gradlew test` e confirmar sucesso (a68abda)
- [x] Task: Conductor - User Manual Verification 'Phase 1: Backend Model, Service and Seed Setup (TDD)' (Protocol in workflow.md) (a68abda)

## Phase 2: Frontend Layout and Component Adaptation (TDD)
- [ ] Task: Atualizar tipos e traduções no frontend
    - [ ] Adicionar rótulo "Conselho Econômico Paroquial" e cor de destaque verde escuro (#2e7d32) em `Organograma.tsx`
- [ ] Task: Adaptar o componente de Árvore multinível no frontend
    - [ ] Adaptar o JSX e estilos em `Organograma.tsx` para renderizar recursivamente a árvore (ou os níveis: Clero no topo -> Conselhos no meio -> Pastorais na base) vinda do backend
    - [ ] Ajustar o layout da visualização em Grade ("grid") para exibir os dois conselhos lado a lado
- [ ] Task: Escrever testes unitários do frontend e rodar testes/build (TDD)
    - [ ] Atualizar `src/pages/Organograma.test.tsx` para refletir as novas asserções de hierarquia (ex: Clero como raiz, presença do Conselho Econômico)
    - [ ] Validar a compilação geral rodando `npm run build` e `CI=true npm test` no frontend
- [ ] Task: Conductor - User Manual Verification 'Phase 2: Frontend Layout and Component Adaptation (TDD)' (Protocol in workflow.md)
