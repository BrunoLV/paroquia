# Alinhamento visual do autocomplete

Evidência do ajuste em `createAppTheme`: o MUI Autocomplete usa a mesma densidade dos campos `TextField outlined small`, sem bordas de botão em seta/limpeza.

- [Modal de cadastro de fiel — claro](autocomplete-aligned-light.png)
- [Modal de cadastro de fiel — escuro](autocomplete-aligned-dark.png)
- [Seleção múltipla, erro, desabilitado e paginação — viewport reduzido](autocomplete-aligned-mobile.png)

Capturas em Chrome headless com os componentes reais de produção, `ThemeProvider`, `CssBaseline` e fonte IBM Plex Sans. O modal é montado isoladamente, com campos vazios e sem consultas à API; não representa uma sessão autenticada. Viewports: 1440×900 (modais) e 390×1100 (campos).

A verificação de geometria no navegador compara seis campos nos dois temas: altura de 54,125 px no zoom 100% do tema atual, raio de 10 px, fonte de 14 px e a mesma superfície. Seta/limpeza têm borda de 0 px. Essa medida descreve o tema existente, não define uma altura fixa: chips múltiplos podem ocupar novas linhas.

Com três chips e contêiner de 240 px no tema escuro, a seleção múltipla cresce para 80,125 px, enquanto os campos simples permanecem em 54,125 px.

`autocompleteTheme.test.tsx` protege a configuração de padding, superfície, tipografia, arredondamento e separação entre adornos e botões de ação externos. Testes de seleção e paginação continuam verificando pesquisa e contratos de valores.
