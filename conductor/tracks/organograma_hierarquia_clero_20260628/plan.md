# Plano de Implementação: Hierarquia do Organograma Paroquial

## Phase 1: Backend Model, Service and Seed Setup (TDD)
- [ ] Task: Atualizar o enum TipoOrganizacao e entidades backend (TDD)
    - [ ] Adicionar valor `CONSELHO_ECONOMICO` no arquivo `TipoOrganizacao.java`
    - [ ] Atualizar serializadores e conversores se necessário
- [ ] Task: Implementar o seed de dados no script `seed-data.sql`
    - [ ] Adicionar insert do 'Conselho Econômico Paroquial' com tipo 'CONSELHO_ECONOMICO', associando lideranças 'Pedro Finanças' e 'Ana Administração', e membros ativos
- [ ] Task: Reestruturar a árvore no caso de uso `GetOrganogramaUseCase` (TDD)
    - [ ] Alterar `GetOrganogramaUseCase.java` para montar a hierarquia multinível (Clero como raiz, Conselhos como filhos diretos do Clero, Pastorais como filhas do Conselho Pastoral)
    - [ ] Escrever novos testes unitários em `GetOrganogramaUseCaseTest.java` validando a herança e estrutura multinível da árvore
- [ ] Task: Executar testes automatizados do backend
    - [ ] Rodar `./gradlew test` e confirmar sucesso
- [ ] Task: Conductor - User Manual Verification 'Phase 1: Backend Model, Service and Seed Setup (TDD)' (Protocol in workflow.md)

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
