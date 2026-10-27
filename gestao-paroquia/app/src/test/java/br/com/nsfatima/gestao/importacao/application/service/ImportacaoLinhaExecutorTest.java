package br.com.nsfatima.gestao.importacao.application.service;

import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.CriarEventoRequest;
import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.ResultadoOperacaoEvento;
import br.com.nsfatima.gestao.calendario.application.usecase.evento.CriarEventoUseCase;
import br.com.nsfatima.gestao.calendario.domain.type.CategoriaEvento;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.entity.UsuarioEntity;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import br.com.nsfatima.gestao.importacao.domain.model.DesfechoLinha;
import br.com.nsfatima.gestao.importacao.domain.model.TipoEntidadeImportacao;
import br.com.nsfatima.gestao.organizacao.application.service.OrganizacaoService;
import br.com.nsfatima.gestao.organizacao.application.service.ValidadorHierarquiaOrganizacional;
import br.com.nsfatima.gestao.organizacao.application.usecase.AdicionarVinculoUseCase;
import br.com.nsfatima.gestao.organizacao.domain.exception.OrganizacaoNegocioException;
import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import br.com.nsfatima.gestao.organizacao.domain.model.SetorPastoral;
import br.com.nsfatima.gestao.organizacao.domain.model.SubtipoConselho;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoUnidadeTerritorial;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.OrganizacaoEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.UnidadeTerritorialEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.OrganizacaoJpaRepository;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.UnidadeTerritorialJpaRepository;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImportacaoLinhaExecutorTest {

    @Mock
    private UsuarioJpaRepository usuarioJpaRepository;
    @Mock
    private OrganizacaoJpaRepository organizacaoJpaRepository;
    @Mock
    private UnidadeTerritorialJpaRepository unidadeTerritorialJpaRepository;
    @Mock
    private AdicionarVinculoUseCase adicionarVinculoUseCase;
    @Mock
    private OrganizacaoService organizacaoService;
    @Mock
    private ValidadorHierarquiaOrganizacional hierarquiaOrganizacional;
    @Mock
    private CriarEventoUseCase criarEventoUseCase;

    private ImportacaoLinhaExecutor executor;

    @BeforeEach
    void setUp() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        executor = new ImportacaoLinhaExecutor(usuarioJpaRepository, organizacaoJpaRepository,
                unidadeTerritorialJpaRepository, adicionarVinculoUseCase, organizacaoService,
                hierarquiaOrganizacional, criarEventoUseCase, validator);
    }

    @Nested
    @DisplayName("membro")
    class Membro {

        private final UUID usuarioId = UUID.randomUUID();
        private final UUID orgId = UUID.randomUUID();
        private final UUID igrejaId = UUID.randomUUID();

        private Map<String, String> linha(String papel, String igreja) {
            Map<String, String> dados = new HashMap<>();
            dados.put("usuarioUsername", "joao");
            dados.put("organizacaoNome", "Pastoral da Música");
            dados.put("papel", papel);
            dados.put("igrejaId", igreja);
            return dados;
        }

        private void stubUsuarioEOrganizacao(TipoOrganizacao tipo) {
            UsuarioEntity usuario = new UsuarioEntity();
            usuario.setId(usuarioId);
            usuario.setFielId(UUID.randomUUID());
            OrganizacaoEntity org = new OrganizacaoEntity();
            org.setId(orgId);
            org.setTipo(tipo);
            when(usuarioJpaRepository.findByUsernameIgnoreCase("joao")).thenReturn(Optional.of(usuario));
            when(organizacaoJpaRepository.findByNomeIgnoreCase("Pastoral da Música")).thenReturn(Optional.of(org));
        }

        private void stubIgreja(TipoUnidadeTerritorial tipo) {
            UnidadeTerritorialEntity igreja = new UnidadeTerritorialEntity();
            igreja.setId(igrejaId);
            igreja.setTipo(tipo);
            when(unidadeTerritorialJpaRepository.findById(igrejaId)).thenReturn(Optional.of(igreja));
        }

        @Test
        @DisplayName("exige usuarioUsername, inclusive quando a coluna vem nula")
        void exigeUsername() {
            Map<String, String> dados = new HashMap<>();
            dados.put("usuarioUsername", null);
            assertThatThrownBy(() -> executor.validar(TipoEntidadeImportacao.MEMBRO, dados))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("O campo 'usuarioUsername' é obrigatório.");
        }

        @Test
        @DisplayName("rejeita usuário inexistente")
        void usuarioInexistente() {
            when(usuarioJpaRepository.findByUsernameIgnoreCase("joao")).thenReturn(Optional.empty());
            assertThatThrownBy(() -> executor.validar(TipoEntidadeImportacao.MEMBRO, linha("MEMBRO_ATIVO", "")))
                    .hasMessage("Usuário não encontrado: joao");
        }

        @Test
        @DisplayName("rejeita organização inexistente")
        void organizacaoInexistente() {
            UsuarioEntity usuario = new UsuarioEntity();
            usuario.setFielId(UUID.randomUUID());
            when(usuarioJpaRepository.findByUsernameIgnoreCase("joao")).thenReturn(Optional.of(usuario));
            when(organizacaoJpaRepository.findByNomeIgnoreCase("Pastoral da Música")).thenReturn(Optional.empty());
            assertThatThrownBy(() -> executor.validar(TipoEntidadeImportacao.MEMBRO, linha("MEMBRO_ATIVO", "")))
                    .hasMessage("Organização não encontrada: Pastoral da Música");
        }

        @Test
        @DisplayName("rejeita papel desconhecido sem assumir papel padrão")
        void papelDesconhecido() {
            stubUsuarioEOrganizacao(TipoOrganizacao.PASTORAL);
            Map<String, String> dados = linha("MEMBRO", igrejaId.toString());
            assertThatThrownBy(() -> executor.validar(TipoEntidadeImportacao.MEMBRO, dados))
                    .hasMessageContaining("Valor inválido para 'papel'");
        }

        @Test
        @DisplayName("nunca atribui coordenação nem papéis de conselho ou de vice-coordenação")
        void rejeitaPapeisDeLideranca() {
            stubUsuarioEOrganizacao(TipoOrganizacao.PASTORAL);
            for (String papel : new String[]{"COORDENADOR", "SECRETARIO_CONSELHO", "MEMBRO_CONSELHO_PASTORAL",
                    "MEMBRO_CONSELHO_ECONOMICO", "SECRETARIO_GRUPO"}) {
                Map<String, String> dados = linha(papel, igrejaId.toString());
                assertThatThrownBy(() -> executor.validar(TipoEntidadeImportacao.MEMBRO, dados))
                        .as(papel).hasMessageContaining("MEMBRO_ATIVO e VOLUNTARIO");
                assertThatThrownBy(() -> executor.processar(TipoEntidadeImportacao.MEMBRO, dados))
                        .as(papel).hasMessageContaining("MEMBRO_ATIVO e VOLUNTARIO");
            }
            verifyNoInteractions(adicionarVinculoUseCase);
        }

        @Test
        @DisplayName("pastoral e movimento exigem igreja de vínculo")
        void exigeIgrejaParaPastoral() {
            stubUsuarioEOrganizacao(TipoOrganizacao.PASTORAL);
            assertThatThrownBy(() -> executor.validar(TipoEntidadeImportacao.MEMBRO, linha("MEMBRO_ATIVO", "")))
                    .hasMessageContaining("igrejaId");
            assertThatThrownBy(() -> executor.validar(TipoEntidadeImportacao.MEMBRO, linha("MEMBRO_ATIVO", "xyz")))
                    .hasMessageContaining("igrejaId");
        }

        @Test
        @DisplayName("rejeita igreja de vínculo inexistente")
        void igrejaInexistente() {
            stubUsuarioEOrganizacao(TipoOrganizacao.PASTORAL);
            when(unidadeTerritorialJpaRepository.findById(igrejaId)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> executor.validar(TipoEntidadeImportacao.MEMBRO,
                    linha("MEMBRO_ATIVO", igrejaId.toString())))
                    .hasMessageContaining("Igreja de vínculo não encontrada");
        }

        @Test
        @DisplayName("rejeita papel que o tipo da organização não admite")
        void papelNaoAdmitidoNoTipo() {
            stubUsuarioEOrganizacao(TipoOrganizacao.CONSELHO);
            assertThatThrownBy(() -> executor.validar(TipoEntidadeImportacao.MEMBRO, linha("MEMBRO_ATIVO", "")))
                    .hasMessageContaining("não existe em organização do tipo CONSELHO");
        }

        @Test
        @DisplayName("validar não persiste e processar delega ao vínculo de organização")
        void validarNaoPersisteProcessarDelega() {
            stubUsuarioEOrganizacao(TipoOrganizacao.PASTORAL);
            stubIgreja(TipoUnidadeTerritorial.MATRIZ);
            Map<String, String> dados = linha("voluntario", igrejaId.toString());

            executor.validar(TipoEntidadeImportacao.MEMBRO, dados);
            verifyNoInteractions(adicionarVinculoUseCase);

            DesfechoLinha desfecho = executor.processar(TipoEntidadeImportacao.MEMBRO, dados);

            assertThat(desfecho).isEqualTo(DesfechoLinha.IMPORTADA);
            verify(adicionarVinculoUseCase).execute(usuarioId, orgId, TipoOrganizacao.PASTORAL,
                    PapelOrganizacional.VOLUNTARIO, igrejaId);
        }
    }

    @Nested
    @DisplayName("organização")
    class Organizacao {

        private Map<String, String> linha() {
            Map<String, String> dados = new HashMap<>();
            dados.put("nome", "Pastoral da Música");
            dados.put("tipo", "PASTORAL");
            dados.put("setorPastoral", "SANTIFICACAO_LITURGIA");
            return dados;
        }

        @Test
        @DisplayName("tipo inválido ou ausente é rejeitado em vez de virar PASTORAL")
        void tipoInvalido() {
            Map<String, String> invalido = linha();
            invalido.put("tipo", "QUALQUER");
            assertThatThrownBy(() -> executor.processar(TipoEntidadeImportacao.ORGANIZACAO, invalido))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("'tipo'")
                    .hasMessageContaining("PASTORAL");
            Map<String, String> ausente = linha();
            ausente.remove("tipo");
            assertThatThrownBy(() -> executor.validar(TipoEntidadeImportacao.ORGANIZACAO, ausente))
                    .hasMessage("O campo 'tipo' é obrigatório.");
            verifyNoInteractions(organizacaoService);
        }

        @Test
        @DisplayName("parentId malformado é rejeitado em vez de buscar uma organização qualquer")
        void parentIdMalformado() {
            Map<String, String> dados = linha();
            dados.put("parentId", "nao-e-uuid");
            assertThatThrownBy(() -> executor.validar(TipoEntidadeImportacao.ORGANIZACAO, dados))
                    .hasMessageContaining("'parentId'");
            verifyNoInteractions(organizacaoService, hierarquiaOrganizacional);
        }

        @Test
        @DisplayName("pastoralOficialId malformado, setor e subtipo inválidos são rejeitados")
        void camposDeClassificacaoInvalidos() {
            Map<String, String> oficial = linha();
            oficial.put("pastoralOficialId", "x");
            assertThatThrownBy(() -> executor.validar(TipoEntidadeImportacao.ORGANIZACAO, oficial))
                    .hasMessageContaining("'pastoralOficialId'");
            Map<String, String> setor = linha();
            setor.put("setorPastoral", "NADA");
            assertThatThrownBy(() -> executor.validar(TipoEntidadeImportacao.ORGANIZACAO, setor))
                    .hasMessageContaining("'setorPastoral'");
            Map<String, String> subtipo = linha();
            subtipo.put("subtipoConselho", "XYZ");
            assertThatThrownBy(() -> executor.validar(TipoEntidadeImportacao.ORGANIZACAO, subtipo))
                    .hasMessageContaining("'subtipoConselho'");
        }

        @Test
        @DisplayName("validar aplica a política de hierarquia sem criar; processar cria com os mesmos argumentos")
        void validarENaoCriaProcessarCria() {
            UUID parent = UUID.randomUUID();
            UUID oficial = UUID.randomUUID();
            Map<String, String> dados = linha();
            dados.put("parentId", parent.toString());
            dados.put("pastoralOficialId", oficial.toString());
            dados.put("contato", "musica@paroquia.org");

            executor.validar(TipoEntidadeImportacao.ORGANIZACAO, dados);

            verify(hierarquiaOrganizacional).validar(null, TipoOrganizacao.PASTORAL, parent, oficial,
                    SetorPastoral.SANTIFICACAO_LITURGIA, null);
            verify(organizacaoService, never()).criar(anyString(), any(), any(), any(), any(), any(), any());

            DesfechoLinha desfecho = executor.processar(TipoEntidadeImportacao.ORGANIZACAO, dados);

            assertThat(desfecho).isEqualTo(DesfechoLinha.IMPORTADA);
            verify(organizacaoService).criar("Pastoral da Música", TipoOrganizacao.PASTORAL, "musica@paroquia.org",
                    parent, oficial, SetorPastoral.SANTIFICACAO_LITURGIA, null);
        }

        @Test
        @DisplayName("violação de hierarquia da política barra a linha já na validação")
        void violacaoDeHierarquia() {
            doThrow(new OrganizacaoNegocioException("Pastoral deve ter organização-mãe."))
                    .when(hierarquiaOrganizacional).validar(any(), any(), any(), any(), any(), any());

            assertThatThrownBy(() -> executor.validar(TipoEntidadeImportacao.ORGANIZACAO, linha()))
                    .hasMessage("Pastoral deve ter organização-mãe.");
            verifyNoInteractions(organizacaoService);
        }

        @Test
        @DisplayName("conselho aceita subtipo")
        void conselhoComSubtipo() {
            Map<String, String> dados = new HashMap<>();
            dados.put("nome", "Conselho Pastoral");
            dados.put("tipo", "conselho");
            dados.put("subtipoConselho", "cpp");

            executor.processar(TipoEntidadeImportacao.ORGANIZACAO, dados);

            verify(organizacaoService).criar("Conselho Pastoral", TipoOrganizacao.CONSELHO, null, null, null,
                    null, SubtipoConselho.CPP);
        }
    }

    @Nested
    @DisplayName("evento")
    class Evento {

        private final UUID orgId = UUID.randomUUID();

        private Map<String, String> linha() {
            Map<String, String> dados = new HashMap<>();
            dados.put("nome", "Missa Dominical");
            dados.put("descricao", "Missa solene");
            dados.put("dataInicio", "2026-08-02 09:00");
            dados.put("dataFim", "2026-08-02 10:30");
            dados.put("categoria", "LITURGICO");
            dados.put("organizacaoId", orgId.toString());
            return dados;
        }

        private void stubOrganizacaoExiste() {
            when(organizacaoJpaRepository.existsById(orgId)).thenReturn(true);
        }

        @Test
        @DisplayName("datas ausentes ou inválidas são rejeitadas em vez de virar agora")
        void datasInvalidas() {
            Map<String, String> semInicio = linha();
            semInicio.remove("dataInicio");
            assertThatThrownBy(() -> executor.validar(TipoEntidadeImportacao.EVENTO, semInicio))
                    .hasMessage("O campo 'dataInicio' é obrigatório.");
            Map<String, String> inicioRuim = linha();
            inicioRuim.put("dataInicio", "amanhã");
            assertThatThrownBy(() -> executor.validar(TipoEntidadeImportacao.EVENTO, inicioRuim))
                    .hasMessageContaining("'dataInicio'");
            Map<String, String> semFim = linha();
            semFim.put("dataFim", "");
            assertThatThrownBy(() -> executor.validar(TipoEntidadeImportacao.EVENTO, semFim))
                    .hasMessage("O campo 'dataFim' é obrigatório.");
            verifyNoInteractions(criarEventoUseCase);
        }

        @Test
        @DisplayName("organização responsável deve existir e categoria é obrigatória")
        void organizacaoECategoria() {
            Map<String, String> semOrg = linha();
            semOrg.remove("organizacaoId");
            assertThatThrownBy(() -> executor.validar(TipoEntidadeImportacao.EVENTO, semOrg))
                    .hasMessage("O campo 'organizacaoId' é obrigatório.");
            when(organizacaoJpaRepository.existsById(orgId)).thenReturn(false);
            assertThatThrownBy(() -> executor.validar(TipoEntidadeImportacao.EVENTO, linha()))
                    .hasMessageContaining("Organização responsável não encontrada");
            stubOrganizacaoExiste();
            Map<String, String> semCategoria = linha();
            semCategoria.remove("categoria");
            assertThatThrownBy(() -> executor.validar(TipoEntidadeImportacao.EVENTO, semCategoria))
                    .hasMessage("O campo 'categoria' é obrigatório.");
        }

        @Test
        @DisplayName("restrições da tela (título, intervalo) valem na importação")
        void restricoesDaTela() {
            stubOrganizacaoExiste();
            Map<String, String> fimAntes = linha();
            fimAntes.put("dataFim", "2026-08-02 08:00");
            assertThatThrownBy(() -> executor.validar(TipoEntidadeImportacao.EVENTO, fimAntes))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("posterior");
            Map<String, String> tituloLongo = linha();
            tituloLongo.put("nome", "x".repeat(161));
            assertThatThrownBy(() -> executor.validar(TipoEntidadeImportacao.EVENTO, tituloLongo))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("validar apenas confere pelo caso de uso do calendário; processar cria sem aceitar status do arquivo")
        void validarEProcessar() {
            stubOrganizacaoExiste();
            Map<String, String> dados = linha();
            dados.put("status", "APROVADO");
            dados.put("justificativa", "Carga inicial da agenda");
            when(criarEventoUseCase.execute(anyString(), any())).thenReturn(
                    new ResultadoOperacaoEvento.Sucesso(new Object(), HttpStatus.CREATED));

            executor.validar(TipoEntidadeImportacao.EVENTO, dados);
            verify(criarEventoUseCase).validarCriacao(any(CriarEventoRequest.class));
            verify(criarEventoUseCase, never()).execute(anyString(), any());

            DesfechoLinha desfecho = executor.processar(TipoEntidadeImportacao.EVENTO, dados);

            assertThat(desfecho).isEqualTo(DesfechoLinha.IMPORTADA);
            ArgumentCaptor<CriarEventoRequest> captor = ArgumentCaptor.forClass(CriarEventoRequest.class);
            ArgumentCaptor<String> chave = ArgumentCaptor.forClass(String.class);
            verify(criarEventoUseCase).execute(chave.capture(), captor.capture());
            CriarEventoRequest request = captor.getValue();
            assertThat(request.status()).isNull();
            assertThat(request.titulo()).isEqualTo("Missa Dominical");
            assertThat(request.categoria()).isEqualTo(CategoriaEvento.LITURGICO);
            assertThat(request.organizacaoResponsavelId()).isEqualTo(orgId);
            assertThat(request.adicionadoExtraJustificativa()).isEqualTo("Carga inicial da agenda");
            assertThat(request.inicio()).isEqualTo(Instant.parse("2026-08-02T12:00:00Z"));
            assertThat(request.fim()).isEqualTo(Instant.parse("2026-08-02T13:30:00Z"));
            assertThat(chave.getValue()).startsWith("importacao-");
        }

        @Test
        @DisplayName("evento que cai na esteira de aprovação é contado como aguardando aprovação")
        void aguardandoAprovacao() {
            stubOrganizacaoExiste();
            when(criarEventoUseCase.execute(anyString(), any())).thenReturn(
                    new ResultadoOperacaoEvento.Pendente(new Object(), HttpStatus.ACCEPTED));

            assertThat(executor.processar(TipoEntidadeImportacao.EVENTO, linha()))
                    .isEqualTo(DesfechoLinha.AGUARDANDO_APROVACAO);
        }

        @Test
        @DisplayName("a mesma linha gera sempre a mesma chave de idempotência")
        void chaveDeterministica() {
            stubOrganizacaoExiste();
            when(criarEventoUseCase.execute(anyString(), any())).thenReturn(
                    new ResultadoOperacaoEvento.Sucesso(new Object(), HttpStatus.CREATED));

            executor.processar(TipoEntidadeImportacao.EVENTO, linha());
            executor.processar(TipoEntidadeImportacao.EVENTO, linha());

            ArgumentCaptor<String> chaves = ArgumentCaptor.forClass(String.class);
            verify(criarEventoUseCase, times(2)).execute(chaves.capture(), any());
            assertThat(chaves.getAllValues().get(0)).isEqualTo(chaves.getAllValues().get(1));
        }
    }
}
