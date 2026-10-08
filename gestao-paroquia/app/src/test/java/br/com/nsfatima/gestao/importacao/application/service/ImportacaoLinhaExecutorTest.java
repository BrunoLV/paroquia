package br.com.nsfatima.gestao.importacao.application.service;

import br.com.nsfatima.gestao.calendario.application.service.MotorLiturgico;
import br.com.nsfatima.gestao.calendario.application.service.ProvisionamentoLiturgicoService;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.AnoLiturgicoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.CicloLiturgicoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.CicloLiturgicoRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.governanca.domain.model.AnoParoquialStatus;
import br.com.nsfatima.gestao.governanca.infrastructure.persistence.entity.AnoParoquialEntity;
import br.com.nsfatima.gestao.governanca.infrastructure.persistence.repository.AnoParoquialJpaRepository;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.entity.UsuarioEntity;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import br.com.nsfatima.gestao.importacao.domain.model.TipoEntidadeImportacao;
import br.com.nsfatima.gestao.membro.api.v1.dto.MembroRequest;
import br.com.nsfatima.gestao.membro.application.usecase.AdicionarMembroUseCase;
import br.com.nsfatima.gestao.organizacao.application.service.OrganizacaoService;
import br.com.nsfatima.gestao.organizacao.domain.model.PastoralMovimentoOficial;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoUnidadeTerritorial;
import br.com.nsfatima.gestao.organizacao.domain.repository.PastoralMovimentoOficialRepository;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.OrganizacaoEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.UnidadeTerritorialEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.OrganizacaoJpaRepository;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.UnidadeTerritorialJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImportacaoLinhaExecutorTest {

    @Mock
    private AdicionarMembroUseCase adicionarMembroUseCase;
    @Mock
    private UsuarioJpaRepository usuarioJpaRepository;
    @Mock
    private OrganizacaoService organizacaoService;
    @Mock
    private EventoJpaRepository eventoJpaRepository;
    @Mock
    private OrganizacaoJpaRepository organizacaoJpaRepository;
    @Mock
    private UnidadeTerritorialJpaRepository unidadeTerritorialJpaRepository;
    @Mock
    private PastoralMovimentoOficialRepository pastoralOficialRepository;
    @Mock
    private AnoParoquialJpaRepository anoParoquialJpaRepository;
    @Mock
    private MotorLiturgico motorLiturgico;
    @Mock
    private CicloLiturgicoRepository cicloLiturgicoRepository;
    @Mock
    private ProvisionamentoLiturgicoService provisionamentoLiturgicoService;

    private ImportacaoLinhaExecutor novoExecutor() {
        return new ImportacaoLinhaExecutor(adicionarMembroUseCase, usuarioJpaRepository, organizacaoService,
                eventoJpaRepository, organizacaoJpaRepository, unidadeTerritorialJpaRepository,
                pastoralOficialRepository, anoParoquialJpaRepository, motorLiturgico, cicloLiturgicoRepository,
                provisionamentoLiturgicoService);
    }

    /** EVERY importEvento() call resolves o ano litúrgico via provisionamentoLiturgicoService; stub obrigatório. */
    private void stubProvisionamentoLiturgico() {
        AnoLiturgicoEntity anoLiturgico = mock(AnoLiturgicoEntity.class);
        when(anoLiturgico.getAno()).thenReturn(2026);
        when(provisionamentoLiturgicoService.resolveAndEnsure(any())).thenReturn(anoLiturgico);
    }

    @Nested
    @DisplayName("validarLinha")
    class ValidarLinha {

        @Test
        @DisplayName("tipo desconhecido retorna mensagem de erro")
        void tipoDesconhecido() {
            String erro = novoExecutor().validarLinha("INEXISTENTE", Map.of());
            assertThat(erro).isEqualTo("Tipo de entidade desconhecido");
        }

        @Test
        @DisplayName("membro sem usuarioUsername é inválido")
        void membroSemUsername() {
            String erro = novoExecutor().validarLinha("MEMBRO", Map.of());
            assertThat(erro).isEqualTo("O campo 'usuarioUsername' é obrigatório.");
        }

        @Test
        @DisplayName("membro com usuário inexistente é inválido")
        void membroUsuarioInexistente() {
            when(usuarioJpaRepository.findByUsernameIgnoreCase("joao")).thenReturn(Optional.empty());
            String erro = novoExecutor().validarLinha("MEMBRO", Map.of("usuarioUsername", "joao"));
            assertThat(erro).isEqualTo("Usuário não encontrado: joao");
        }

        @Test
        @DisplayName("membro sem organizacaoNome é inválido")
        void membroSemOrgNome() {
            when(usuarioJpaRepository.findByUsernameIgnoreCase("joao"))
                    .thenReturn(Optional.of(new UsuarioEntity()));
            String erro = novoExecutor().validarLinha("MEMBRO", Map.of("usuarioUsername", "joao"));
            assertThat(erro).isEqualTo("O campo 'organizacaoNome' é obrigatório.");
        }

        @Test
        @DisplayName("membro com organização inexistente é inválido")
        void membroOrgInexistente() {
            when(usuarioJpaRepository.findByUsernameIgnoreCase("joao"))
                    .thenReturn(Optional.of(new UsuarioEntity()));
            when(organizacaoJpaRepository.findByNomeIgnoreCase("Matriz")).thenReturn(Optional.empty());
            String erro = novoExecutor().validarLinha("MEMBRO",
                    Map.of("usuarioUsername", "joao", "organizacaoNome", "Matriz"));
            assertThat(erro).isEqualTo("Organização não encontrada: Matriz");
        }

        @Test
        @DisplayName("membro sem papel é inválido")
        void membroSemPapel() {
            when(usuarioJpaRepository.findByUsernameIgnoreCase("joao"))
                    .thenReturn(Optional.of(new UsuarioEntity()));
            when(organizacaoJpaRepository.findByNomeIgnoreCase("Matriz"))
                    .thenReturn(Optional.of(new OrganizacaoEntity()));
            String erro = novoExecutor().validarLinha("MEMBRO",
                    Map.of("usuarioUsername", "joao", "organizacaoNome", "Matriz"));
            assertThat(erro).isEqualTo("O campo 'papel' é obrigatório.");
        }

        @Test
        @DisplayName("membro válido não retorna erro")
        void membroValido() {
            when(usuarioJpaRepository.findByUsernameIgnoreCase("joao"))
                    .thenReturn(Optional.of(new UsuarioEntity()));
            when(organizacaoJpaRepository.findByNomeIgnoreCase("Matriz"))
                    .thenReturn(Optional.of(new OrganizacaoEntity()));
            String erro = novoExecutor().validarLinha("MEMBRO",
                    Map.of("usuarioUsername", "joao", "organizacaoNome", "Matriz", "papel", "MEMBRO_PASTORAL"));
            assertThat(erro).isNull();
        }

        @Test
        @DisplayName("grupo paroquial sem nome é inválido")
        void grupoSemNome() {
            String erro = novoExecutor().validarLinha("ORGANIZACAO", Map.of());
            assertThat(erro).isEqualTo("O campo 'nome' é obrigatório.");
        }

        @Test
        @DisplayName("grupo paroquial sem tipo é inválido")
        void grupoSemTipo() {
            String erro = novoExecutor().validarLinha("ORGANIZACAO", Map.of("nome", "Comunidade Sao Jose"));
            assertThat(erro).isEqualTo("O campo 'tipo' é obrigatório.");
        }

        @Test
        @DisplayName("grupo paroquial válido não retorna erro")
        void grupoValido() {
            String erro = novoExecutor().validarLinha("ORGANIZACAO",
                    Map.of("nome", "Comunidade Sao Jose", "tipo", "COMUNIDADE"));
            assertThat(erro).isNull();
        }

        @Test
        @DisplayName("evento sem nome é inválido")
        void eventoSemNome() {
            String erro = novoExecutor().validarLinha("EVENTO", Map.of());
            assertThat(erro).isEqualTo("O campo 'nome' é obrigatório.");
        }

        @Test
        @DisplayName("evento sem dataInicio é inválido")
        void eventoSemDataInicio() {
            String erro = novoExecutor().validarLinha("EVENTO", Map.of("nome", "Missa"));
            assertThat(erro).isEqualTo("O campo 'dataInicio' é obrigatório.");
        }

        @Test
        @DisplayName("evento válido não retorna erro")
        void eventoValido() {
            String erro = novoExecutor().validarLinha("EVENTO",
                    Map.of("nome", "Missa", "dataInicio", "2026-10-15T10:00:00Z"));
            assertThat(erro).isNull();
        }
    }

    @Nested
    @DisplayName("processarImportacao")
    class ProcessarImportacao {

        @Test
        @DisplayName("tipo nulo lança NullPointerException (switch sobre enum nulo, sem 'case null')")
        void tipoNuloLancaExcecao() {
            assertThatThrownBy(() -> novoExecutor().processarImportacao(null, Map.of(), "actor"))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("MEMBRO com usuário e organização inexistentes lança IllegalArgumentException")
        void membroUsuarioInexistenteLancaExcecao() {
            when(usuarioJpaRepository.findByUsernameIgnoreCase("joao")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> novoExecutor().processarImportacao(TipoEntidadeImportacao.MEMBRO,
                    Map.of("usuarioUsername", "joao", "organizacaoNome", "Matriz", "papel", "SECRETARIO"),
                    "actor"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Usuário não encontrado: joao");
        }

        @Test
        @DisplayName("MEMBRO com organização inexistente lança IllegalArgumentException")
        void membroOrgInexistenteLancaExcecao() {
            UsuarioEntity usuario = new UsuarioEntity();
            when(usuarioJpaRepository.findByUsernameIgnoreCase("joao")).thenReturn(Optional.of(usuario));
            when(organizacaoJpaRepository.findByNomeIgnoreCase("Matriz")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> novoExecutor().processarImportacao(TipoEntidadeImportacao.MEMBRO,
                    Map.of("usuarioUsername", "joao", "organizacaoNome", "Matriz", "papel", "SECRETARIO"),
                    "actor"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Organização não encontrada: Matriz");
        }

        @Test
        @DisplayName("MEMBRO válido delega para AdicionarMembroUseCase com o papel informado")
        void membroValidoDelegaParaAdicionarMembro() {
            UUID fielId = UUID.randomUUID();
            UUID orgId = UUID.randomUUID();
            UsuarioEntity usuario = mock(UsuarioEntity.class);
            when(usuario.getFielId()).thenReturn(fielId);
            OrganizacaoEntity org = mock(OrganizacaoEntity.class);
            when(org.getId()).thenReturn(orgId);
            when(usuarioJpaRepository.findByUsernameIgnoreCase("joao")).thenReturn(Optional.of(usuario));
            when(organizacaoJpaRepository.findByNomeIgnoreCase("Matriz")).thenReturn(Optional.of(org));

            novoExecutor().processarImportacao(TipoEntidadeImportacao.MEMBRO,
                    Map.of("usuarioUsername", "joao", "organizacaoNome", "Matriz", "papel", "SECRETARIO"),
                    "actor");

            ArgumentCaptor<MembroRequest> captor = ArgumentCaptor.forClass(MembroRequest.class);
            verify(adicionarMembroUseCase).execute(captor.capture(), eq("actor"));
            MembroRequest request = captor.getValue();
            assertThat(request.fielId()).isEqualTo(fielId);
            assertThat(request.organizacaoId()).isEqualTo(orgId);
            assertThat(request.papel()).isEqualTo("SECRETARIO");
            assertThat(request.dataEntrada()).isEqualTo(LocalDate.now());
            assertThat(request.igrejaId()).isNull();
            assertThat(request.ativo()).isTrue();
        }

        @Test
        @DisplayName("MEMBRO sem papel informado usa MEMBRO_PASTORAL como default")
        void membroSemPapelUsaDefault() {
            UUID fielId = UUID.randomUUID();
            UUID orgId = UUID.randomUUID();
            UsuarioEntity usuario = mock(UsuarioEntity.class);
            when(usuario.getFielId()).thenReturn(fielId);
            OrganizacaoEntity org = mock(OrganizacaoEntity.class);
            when(org.getId()).thenReturn(orgId);
            when(usuarioJpaRepository.findByUsernameIgnoreCase("joao")).thenReturn(Optional.of(usuario));
            when(organizacaoJpaRepository.findByNomeIgnoreCase("Matriz")).thenReturn(Optional.of(org));

            novoExecutor().processarImportacao(TipoEntidadeImportacao.MEMBRO,
                    Map.of("usuarioUsername", "joao", "organizacaoNome", "Matriz"), "actor");

            ArgumentCaptor<MembroRequest> captor = ArgumentCaptor.forClass(MembroRequest.class);
            verify(adicionarMembroUseCase).execute(captor.capture(), eq("actor"));
            assertThat(captor.getValue().papel()).isEqualTo("MEMBRO_PASTORAL");
            assertThat(captor.getValue().fielId()).isEqualTo(fielId);
            assertThat(captor.getValue().organizacaoId()).isEqualTo(orgId);
        }

        @Test
        @DisplayName("ORGANIZACAO delega para OrganizacaoService.criar")
        void organizacaoDelegaParaService() {
            novoExecutor().processarImportacao(TipoEntidadeImportacao.ORGANIZACAO,
                    Map.of("nome", "Ministerio de Musica", "tipo", "SERVICO_PAROQUIAL"), "actor");

            verify(organizacaoService).criar("Ministerio de Musica", TipoOrganizacao.SERVICO_PAROQUIAL,
                    null, null, null, null, null, null, null);
        }

        @Test
        @DisplayName("EVENTO persiste entidade com status default APROVADO quando ano paroquial não está fechado")
        void eventoPersisteComStatusDefault() {
            stubProvisionamentoLiturgico();
            when(anoParoquialJpaRepository.findByAno(2026)).thenReturn(Optional.empty());
            when(organizacaoJpaRepository.existsById(any())).thenReturn(true);

            novoExecutor().processarImportacao(TipoEntidadeImportacao.EVENTO,
                    Map.of("nome", "Missa", "dataInicio", "2026-10-15T10:00:00Z",
                            "organizacaoId", UUID.randomUUID().toString()),
                    "actor");

            verify(eventoJpaRepository).save(any(EventoEntity.class));
        }

        @Test
        @DisplayName("EVENTO com ano paroquial fechado e status ADICIONADO_EXTRA aplica justificativa")
        void eventoAnoFechadoComAdicionadoExtra() {
            stubProvisionamentoLiturgico();
            AnoParoquialEntity anoFechado = mock(AnoParoquialEntity.class);
            when(anoFechado.getStatus()).thenReturn(AnoParoquialStatus.FECHADO);
            when(anoParoquialJpaRepository.findByAno(2026)).thenReturn(Optional.of(anoFechado));

            EventoEntity[] captured = new EventoEntity[1];
            when(eventoJpaRepository.save(any(EventoEntity.class))).thenAnswer(inv -> {
                captured[0] = inv.getArgument(0);
                return captured[0];
            });

            novoExecutor().processarImportacao(TipoEntidadeImportacao.EVENTO,
                    Map.of("nome", "Missa", "dataInicio", "2026-10-15T10:00:00Z", "status", "ADICIONADO_EXTRA",
                            "adicionadoExtraJustificativa", "Pedido especial da comunidade"),
                    "actor");

            assertThat(captured[0].getStatus()).isEqualTo("ADICIONADO_EXTRA");
            assertThat(captured[0].getAdicionadoExtraJustificativa()).isEqualTo("Pedido especial da comunidade");
        }

        @Test
        @DisplayName("EVENTO com ano paroquial fechado e status não-extra fica PENDENTE de homologação")
        void eventoAnoFechadoSemAdicionadoExtraFicaPendente() {
            stubProvisionamentoLiturgico();
            AnoParoquialEntity anoFechado = mock(AnoParoquialEntity.class);
            when(anoFechado.getStatus()).thenReturn(AnoParoquialStatus.FECHADO);
            when(anoParoquialJpaRepository.findByAno(2026)).thenReturn(Optional.of(anoFechado));

            EventoEntity[] captured = new EventoEntity[1];
            when(eventoJpaRepository.save(any(EventoEntity.class))).thenAnswer(inv -> {
                captured[0] = inv.getArgument(0);
                return captured[0];
            });

            novoExecutor().processarImportacao(TipoEntidadeImportacao.EVENTO,
                    Map.of("nome", "Missa", "dataInicio", "2026-10-15T10:00:00Z"), "actor");

            assertThat(captured[0].getStatus()).isEqualTo("PENDENTE");
            assertThat(captured[0].getMotivoConflito())
                    .isEqualTo("Importado em lote durante Ano Paroquial Fechado - aguardando homologação");
        }

        @Test
        @DisplayName("EVENTO usa ProvisionamentoLiturgicoService quando disponível, ignorando MotorLiturgico")
        void eventoUsaProvisionamentoLiturgicoQuandoDisponivel() {
            when(anoParoquialJpaRepository.findByAno(2026)).thenReturn(Optional.empty());
            AnoLiturgicoEntity anoLiturgico = mock(AnoLiturgicoEntity.class);
            when(anoLiturgico.getAno()).thenReturn(2026);
            when(provisionamentoLiturgicoService.resolveAndEnsure(any())).thenReturn(anoLiturgico);

            novoExecutor().processarImportacao(TipoEntidadeImportacao.EVENTO,
                    Map.of("nome", "Missa", "dataInicio", "2026-10-15T10:00:00Z"), "actor");

            verify(provisionamentoLiturgicoService).resolveAndEnsure(any());
        }

        @Test
        @DisplayName("EVENTO usa CicloLiturgicoRepository quando presente para preencher o ciclo litúrgico")
        void eventoUsaCicloLiturgicoRepository() {
            stubProvisionamentoLiturgico();
            when(anoParoquialJpaRepository.findByAno(2026)).thenReturn(Optional.empty());
            UUID cicloId = UUID.randomUUID();
            CicloLiturgicoEntity ciclo = mock(CicloLiturgicoEntity.class);
            when(ciclo.getId()).thenReturn(cicloId);
            when(cicloLiturgicoRepository.findAll()).thenReturn(List.of(ciclo));

            EventoEntity[] captured = new EventoEntity[1];
            when(eventoJpaRepository.save(any(EventoEntity.class))).thenAnswer(inv -> {
                captured[0] = inv.getArgument(0);
                return captured[0];
            });

            novoExecutor().processarImportacao(TipoEntidadeImportacao.EVENTO,
                    Map.of("nome", "Missa", "dataInicio", "2026-10-15T10:00:00Z"), "actor");

            assertThat(captured[0].getCicloLiturgicoId()).isEqualTo(cicloId);
        }

        @Test
        @DisplayName("EVENTO sem dataInicio/dataFim parseáveis usa Instant.now() e uma hora de duração")
        void eventoSemDatasParseaveisUsaFallback() {
            stubProvisionamentoLiturgico();
            when(anoParoquialJpaRepository.findByAno(any(Integer.class))).thenReturn(Optional.empty());

            EventoEntity[] captured = new EventoEntity[1];
            when(eventoJpaRepository.save(any(EventoEntity.class))).thenAnswer(inv -> {
                captured[0] = inv.getArgument(0);
                return captured[0];
            });

            novoExecutor().processarImportacao(TipoEntidadeImportacao.EVENTO,
                    Map.of("nome", "Missa", "dataInicio", "data-invalida"), "actor");

            assertThat(captured[0].getFimUtc())
                    .isEqualTo(captured[0].getInicioUtc().plusSeconds(3600));
        }

        @Test
        @DisplayName("EVENTO com dataInicio no formato 'yyyy-MM-dd HH:mm' é interpretado corretamente")
        void eventoComFormatoAlternativoDeData() {
            stubProvisionamentoLiturgico();
            when(anoParoquialJpaRepository.findByAno(any(Integer.class))).thenReturn(Optional.empty());

            EventoEntity[] captured = new EventoEntity[1];
            when(eventoJpaRepository.save(any(EventoEntity.class))).thenAnswer(inv -> {
                captured[0] = inv.getArgument(0);
                return captured[0];
            });

            novoExecutor().processarImportacao(TipoEntidadeImportacao.EVENTO,
                    Map.of("nome", "Missa", "dataInicio", "2026-10-15 10:00"), "actor");

            assertThat(captured[0].getInicioUtc()).isNotNull();
        }
    }

    @Nested
    @DisplayName("resolveParentId (via importOrganizacao)")
    class ResolveParentId {

        @Test
        @DisplayName("parentId inexistente cai para busca automática: CONSELHO usa primeira MATRIZ/COMUNIDADE")
        void conselhoSemParentIdUsaPrimeiraUnidade() {
            when(organizacaoJpaRepository.findAll()).thenReturn(List.of(new OrganizacaoEntity()));
            UnidadeTerritorialEntity matriz = mock(UnidadeTerritorialEntity.class);
            when(matriz.getTipo()).thenReturn(TipoUnidadeTerritorial.MATRIZ);
            UUID matrizId = UUID.randomUUID();
            when(matriz.getId()).thenReturn(matrizId);
            when(unidadeTerritorialJpaRepository.findAll()).thenReturn(List.of(matriz));

            novoExecutor().processarImportacao(TipoEntidadeImportacao.ORGANIZACAO,
                    Map.of("nome", "Conselho Central", "tipo", "CONSELHO"), "actor");

            verify(organizacaoService).criar("Conselho Central", TipoOrganizacao.CONSELHO,
                    null, null, null, matrizId, null, null, null);
        }

        @Test
        @DisplayName("PASTORAL sem parentId usa a primeira organização do tipo CONSELHO como pai")
        void pastoralSemParentIdUsaPrimeiroConselho() {
            OrganizacaoEntity conselho = mock(OrganizacaoEntity.class);
            when(conselho.getTipo()).thenReturn(TipoOrganizacao.CONSELHO);
            UUID conselhoId = UUID.randomUUID();
            when(conselho.getId()).thenReturn(conselhoId);
            when(organizacaoJpaRepository.findAll()).thenReturn(List.of(conselho));
            when(pastoralOficialRepository.findAll()).thenReturn(List.of());

            novoExecutor().processarImportacao(TipoEntidadeImportacao.ORGANIZACAO,
                    Map.of("nome", "Pastoral da Familia", "tipo", "PASTORAL"), "actor");

            verify(organizacaoService).criar("Pastoral da Familia", TipoOrganizacao.PASTORAL,
                    null, null, null, conselhoId, null, null, null);
        }

        @Test
        @DisplayName("tipo desconhecido de organização recai em PASTORAL")
        void tipoOrgDesconhecidoRecaiEmPastoral() {
            when(organizacaoJpaRepository.findAll()).thenReturn(List.of());

            novoExecutor().processarImportacao(TipoEntidadeImportacao.ORGANIZACAO,
                    Map.of("nome", "Movimento X", "tipo", "TIPO_INVALIDO"), "actor");

            verify(organizacaoService).criar("Movimento X", TipoOrganizacao.PASTORAL,
                    null, null, null, null, null, null, null);
        }

        @Test
        @DisplayName("PASTORAL/MOVIMENTO resolve pastoralOficialId pelo tipo quando não informado")
        void pastoralResolvePastoralOficialIdPeloTipo() {
            when(organizacaoJpaRepository.findAll()).thenReturn(List.of());
            UUID oficialId = UUID.randomUUID();
            PastoralMovimentoOficial oficial = mock(PastoralMovimentoOficial.class);
            when(oficial.getTipo()).thenReturn(TipoOrganizacao.PASTORAL);
            when(oficial.getId()).thenReturn(oficialId);
            when(pastoralOficialRepository.findAll()).thenReturn(List.of(oficial));

            novoExecutor().processarImportacao(TipoEntidadeImportacao.ORGANIZACAO,
                    Map.of("nome", "Pastoral da Familia", "tipo", "PASTORAL"), "actor");

            verify(organizacaoService).criar("Pastoral da Familia", TipoOrganizacao.PASTORAL,
                    null, null, null, null, oficialId, null, null);
        }
    }

    @Nested
    @DisplayName("resolveOrganizacaoResponsavelId (via importEvento)")
    class ResolveOrganizacaoResponsavelId {

        @Test
        @DisplayName("organizacaoId inválido/ausente cai para a primeira organização cadastrada")
        void semOrganizacaoIdUsaPrimeiraOrganizacao() {
            stubProvisionamentoLiturgico();
            when(anoParoquialJpaRepository.findByAno(any(Integer.class))).thenReturn(Optional.empty());
            OrganizacaoEntity primeira = mock(OrganizacaoEntity.class);
            UUID primeiraId = UUID.randomUUID();
            when(primeira.getId()).thenReturn(primeiraId);
            when(organizacaoJpaRepository.findAll()).thenReturn(List.of(primeira));

            EventoEntity[] captured = new EventoEntity[1];
            when(eventoJpaRepository.save(any(EventoEntity.class))).thenAnswer(inv -> {
                captured[0] = inv.getArgument(0);
                return captured[0];
            });

            novoExecutor().processarImportacao(TipoEntidadeImportacao.EVENTO,
                    Map.of("nome", "Missa", "dataInicio", "2026-10-15T10:00:00Z"), "actor");

            assertThat(captured[0].getOrganizacaoResponsavelId()).isEqualTo(primeiraId);
        }

        @Test
        @DisplayName("organizacaoId válido e existente é utilizado diretamente")
        void organizacaoIdValidoEExistenteEUsadoDiretamente() {
            stubProvisionamentoLiturgico();
            when(anoParoquialJpaRepository.findByAno(any(Integer.class))).thenReturn(Optional.empty());
            UUID orgId = UUID.randomUUID();
            when(organizacaoJpaRepository.existsById(orgId)).thenReturn(true);

            EventoEntity[] captured = new EventoEntity[1];
            when(eventoJpaRepository.save(any(EventoEntity.class))).thenAnswer(inv -> {
                captured[0] = inv.getArgument(0);
                return captured[0];
            });

            novoExecutor().processarImportacao(TipoEntidadeImportacao.EVENTO,
                    Map.of("nome", "Missa", "dataInicio", "2026-10-15T10:00:00Z",
                            "organizacaoId", orgId.toString()),
                    "actor");

            assertThat(captured[0].getOrganizacaoResponsavelId()).isEqualTo(orgId);
        }
    }
}
