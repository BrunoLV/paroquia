package br.com.nsfatima.gestao.organizacao.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.nsfatima.gestao.fiel.infrastructure.persistence.entity.FielEntity;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository.FielJpaRepository;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.entity.MembroGrupoEntity;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.mapper.MembroGrupoMapper;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.repository.MembroGrupoJpaRepository;
import br.com.nsfatima.gestao.organizacao.domain.exception.OrganizacaoNegocioException;
import br.com.nsfatima.gestao.organizacao.domain.model.FuncaoCoordenacao;
import br.com.nsfatima.gestao.organizacao.domain.model.MandatoCoordenacao;
import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import br.com.nsfatima.gestao.organizacao.domain.model.StatusMandato;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import br.com.nsfatima.gestao.organizacao.domain.repository.MandatoCoordenacaoRepository;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.OrganizacaoEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.OrganizacaoJpaRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MandatoCoordenacaoServiceTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 6, 1);

    private final UUID organizacaoId = UUID.randomUUID();
    private final UUID fielId = UUID.randomUUID();

    private MandatoCoordenacaoRepository mandatos;
    private OrganizacaoJpaRepository organizacoes;
    private FielJpaRepository fieis;
    private MembroGrupoJpaRepository membros;
    private MandatoCoordenacaoService service;
    private OrganizacaoEntity organizacao;
    private MembroGrupoEntity membro;

    @BeforeEach
    void setUp() {
        mandatos = mock(MandatoCoordenacaoRepository.class);
        organizacoes = mock(OrganizacaoJpaRepository.class);
        fieis = mock(FielJpaRepository.class);
        membros = mock(MembroGrupoJpaRepository.class);
        service = new MandatoCoordenacaoService(mandatos, organizacoes, fieis, membros, new MembroGrupoMapper(),
                Clock.fixed(Instant.parse("2026-06-01T12:00:00Z"), ZoneOffset.UTC));

        organizacao = new OrganizacaoEntity();
        organizacao.setId(organizacaoId);
        organizacao.setAtivo(true);
        when(organizacoes.findById(organizacaoId)).thenReturn(Optional.of(organizacao));
        when(fieis.existsById(fielId)).thenReturn(true);
        FielEntity fiel = new FielEntity();
        fiel.setNome("Maria da Silva");
        when(fieis.findById(fielId)).thenReturn(Optional.of(fiel));

        membro = new MembroGrupoEntity();
        membro.setId(UUID.randomUUID());
        membro.setFielId(fielId);
        membro.setOrganizacaoId(organizacaoId);
        membro.setTipoOrganizacao(TipoOrganizacao.PASTORAL);
        membro.setPapel(PapelOrganizacional.MEMBRO_ATIVO.storedValue());
        membro.setAtivo(true);
        when(membros.findByFielIdAndOrganizacaoIdAndAtivoTrue(fielId, organizacaoId))
                .thenReturn(Optional.of(membro));
        when(mandatos.findByOrganizacaoId(organizacaoId)).thenReturn(List.of());
    }

    @Test
    void proporRegistraMandatoPendenteDeHomologacao() {
        MandatoCoordenacao m = service.propor(organizacaoId, fielId, FuncaoCoordenacao.COORDENADOR,
                HOJE, HOJE.plusYears(2));

        assertThat(m.getStatus()).isEqualTo(StatusMandato.PROPOSTO);
        verify(mandatos).save(m);
        assertThat(organizacao.getCoordenador()).isNull();
    }

    @Test
    void proporExigeFielMembroAtivoDaOrganizacao() {
        when(membros.findByFielIdAndOrganizacaoIdAndAtivoTrue(fielId, organizacaoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.propor(organizacaoId, fielId, FuncaoCoordenacao.COORDENADOR,
                HOJE, HOJE.plusYears(1))).isInstanceOf(OrganizacaoNegocioException.class)
                .hasMessageContaining("membro ativo");
        verify(mandatos, never()).save(any());
    }

    @Test
    void proporRecusaOrganizacaoInativaEMandatoVitalicio() {
        assertThatThrownBy(() -> service.propor(organizacaoId, fielId, FuncaoCoordenacao.COORDENADOR,
                HOJE, HOJE.plusYears(4))).isInstanceOf(OrganizacaoNegocioException.class)
                .hasMessageContaining("3 anos");

        organizacao.setAtivo(false);
        assertThatThrownBy(() -> service.propor(organizacaoId, fielId, FuncaoCoordenacao.COORDENADOR,
                HOJE, HOJE.plusYears(1))).isInstanceOf(OrganizacaoNegocioException.class);
    }

    @Test
    void proporRecusaPeriodoConcomitanteParaAMesmaFuncao() {
        MandatoCoordenacao existente = MandatoCoordenacao.propor(organizacaoId, UUID.randomUUID(),
                FuncaoCoordenacao.COORDENADOR, HOJE, HOJE.plusYears(1));
        when(mandatos.findByOrganizacaoId(organizacaoId)).thenReturn(List.of(existente));

        assertThatThrownBy(() -> service.propor(organizacaoId, fielId, FuncaoCoordenacao.COORDENADOR,
                HOJE.plusMonths(6), HOJE.plusYears(2))).isInstanceOf(OrganizacaoNegocioException.class)
                .hasMessageContaining("Já existe mandato");

        assertThat(service.propor(organizacaoId, fielId, FuncaoCoordenacao.VICE_COORDENADOR,
                HOJE.plusMonths(6), HOJE.plusYears(2))).isNotNull();
    }

    @Test
    void homologarInvesteOFielNoPapelEEspelhaONome() {
        MandatoCoordenacao m = MandatoCoordenacao.propor(organizacaoId, fielId, FuncaoCoordenacao.COORDENADOR,
                HOJE, HOJE.plusYears(2));
        when(mandatos.findById(m.getId())).thenReturn(Optional.of(m));

        service.homologar(m.getId());

        assertThat(m.getStatus()).isEqualTo(StatusMandato.HOMOLOGADO);
        assertThat(membro.getPapel()).isEqualTo(PapelOrganizacional.COORDENADOR.storedValue());
        assertThat(organizacao.getCoordenador()).isEqualTo("Maria da Silva");
    }

    @Test
    void encerrarDevolveOFielAMembroAtivoELimpaOEspelho() {
        MandatoCoordenacao m = MandatoCoordenacao.propor(organizacaoId, fielId, FuncaoCoordenacao.VICE_COORDENADOR,
                HOJE, HOJE.plusYears(2));
        when(mandatos.findById(m.getId())).thenReturn(Optional.of(m));
        service.homologar(m.getId());
        assertThat(organizacao.getViceCoordenador()).isEqualTo("Maria da Silva");

        service.encerrar(m.getId());

        assertThat(m.getStatus()).isEqualTo(StatusMandato.ENCERRADO);
        assertThat(membro.getPapel()).isEqualTo(PapelOrganizacional.MEMBRO_ATIVO.storedValue());
        assertThat(organizacao.getViceCoordenador()).isNull();
    }

    @Test
    void vetarNaoAlteraOVinculoDoFiel() {
        MandatoCoordenacao m = MandatoCoordenacao.propor(organizacaoId, fielId, FuncaoCoordenacao.COORDENADOR,
                HOJE, HOJE.plusYears(2));
        when(mandatos.findById(m.getId())).thenReturn(Optional.of(m));

        service.vetar(m.getId(), "Falta de formação");

        assertThat(m.getStatus()).isEqualTo(StatusMandato.VETADO);
        assertThat(membro.getPapel()).isEqualTo(PapelOrganizacional.MEMBRO_ATIVO.storedValue());
        assertThatThrownBy(() -> service.vetar(m.getId(), "outra vez"))
                .isInstanceOf(OrganizacaoNegocioException.class);
    }

    @Test
    void encerrarVencidosEncerraSomenteOsHomologadosComPrazoTerminado() {
        MandatoCoordenacao m = new MandatoCoordenacao(UUID.randomUUID(), organizacaoId, fielId,
                FuncaoCoordenacao.COORDENADOR, HOJE.minusYears(2), HOJE.minusDays(1), StatusMandato.HOMOLOGADO,
                "paroco", null, null);
        when(mandatos.findHomologadosVencidosAntesDe(HOJE)).thenReturn(List.of(m));
        when(mandatos.findById(m.getId())).thenReturn(Optional.of(m));

        assertThat(service.encerrarVencidos()).isEqualTo(1);
        assertThat(m.getStatus()).isEqualTo(StatusMandato.ENCERRADO);
    }
}
