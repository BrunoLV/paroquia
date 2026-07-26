# Spec: Exportar Organograma para Imagem

## Visão Geral

Implementar na página de Organograma da aplicação `gestao-paroquia-ui` a funcionalidade de exportar/baixar o organograma como imagem. O usuário poderá escolher entre os formatos **PNG** e **SVG**, e a imagem gerada deverá capturar o **organograma completo** (não apenas a área visível na tela), preservando toda a hierarquia e nós do organograma.

## Requisitos Funcionais

### RF-01: Botão de Exportação
- Um botão primário destacado deve ser adicionado **acima do gráfico do Organograma**.
- O botão deve abrir um menu de seleção de formato (PNG ou SVG) ao ser clicado.

### RF-02: Seleção de Formato
- Após clicar no botão, o usuário deve poder escolher entre:
  - **PNG** - Formato raster, ideal para compartilhamento e visualização.
  - **SVG** - Formato vetorial, redimensionável sem perda de qualidade.

### RF-03: Captura do Organograma Completo
- A imagem gerada deve capturar **todo o organograma**, incluindo nós e conexões que não estejam visíveis na viewport atual (fora do scroll/zoom).
- A renderização deve ser fiel ao design do organograma exibido na tela (cores, tipografia, hierarquia).

### RF-04: Download Automático
- Após a seleção do formato, o arquivo deve ser baixado automaticamente pelo navegador.
- O nome do arquivo deve seguir o padrão: `organograma_<nome-da-organizacao>_<YYYY-MM-DD>.<ext>`.

### RF-05: Controle de Acesso
- A funcionalidade deve estar disponível para **todos os usuários autenticados** que já possuem acesso à página do Organograma.
- Não há nova camada de permissionamento; respeita as regras já existentes de acesso à página.

## Requisitos Não-Funcionais

- **Biblioteca de Captura:** Utilizar uma biblioteca client-side consolidada (ex: `html-to-image` ou `dom-to-image`) para realizar a captura do DOM sem necessitar de chamadas à API.
- **Performance:** A geração da imagem deve ser processada inteiramente no cliente (browser) para não sobrecarregar o backend.
- **UX durante exportação:** Exibir indicador de carregamento (loading) enquanto a imagem é gerada, pois organogramas grandes podem demorar alguns segundos.
- **Compatibilidade:** Funcionar nos navegadores modernos (Chrome, Firefox, Edge).

## Critérios de Aceitação

- [ ] Botão de download é exibido acima do gráfico do Organograma.
- [ ] Ao clicar no botão, o menu de seleção de formato (PNG/SVG) é exibido.
- [ ] Selecionar PNG inicia o download de um arquivo `.png` com o organograma completo.
- [ ] Selecionar SVG inicia o download de um arquivo `.svg` com o organograma completo.
- [ ] O nome do arquivo segue o padrão definido.
- [ ] Um indicador de carregamento é exibido durante a geração da imagem.
- [ ] A funcionalidade é acessível apenas para usuários autenticados com acesso ao organograma.
- [ ] Testes unitários cobrem o hook/utilitário de exportação com cobertura > 80%.

## Fora do Escopo

- Exportação para PDF.
- Geração de imagem no backend/servidor.
- Customização de cores ou layout antes do download.
- Exportação de apenas uma parte/nó específico do organograma.
