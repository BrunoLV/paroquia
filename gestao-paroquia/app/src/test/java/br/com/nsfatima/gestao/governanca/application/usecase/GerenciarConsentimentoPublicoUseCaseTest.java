package br.com.nsfatima.gestao.governanca.application.usecase;

import br.com.nsfatima.gestao.clero.infrastructure.persistence.repository.ClerigoJpaRepository;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.entity.FielEntity;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository.FielJpaRepository;
import br.com.nsfatima.gestao.governanca.api.v1.dto.ComprovanteConsentimentoResponse;
import br.com.nsfatima.gestao.governanca.api.v1.dto.ManifestarConsentimentoRequest;
import br.com.nsfatima.gestao.governanca.api.v1.dto.SolicitarCodigoConsentimentoRequest;
import br.com.nsfatima.gestao.governanca.api.v1.dto.SolicitarCodigoConsentimentoResponse;
import br.com.nsfatima.gestao.governanca.api.v1.dto.ValidarCodigoConsentimentoRequest;
import br.com.nsfatima.gestao.governanca.api.v1.dto.ValidarCodigoConsentimentoResponse;
import br.com.nsfatima.gestao.governanca.application.service.ServicoEmailNotificacaoConsentimento;
import br.com.nsfatima.gestao.governanca.application.service.TokenConsentimentoService;
import br.com.nsfatima.gestao.governanca.domain.model.TipoTitularConsentimento;
import br.com.nsfatima.gestao.governanca.infrastructure.persistence.entity.DesafioConsentimentoEntity;
import br.com.nsfatima.gestao.governanca.infrastructure.persistence.repository.DesafioConsentimentoJpaRepository;
import br.com.nsfatima.gestao.governanca.infrastructure.persistence.repository.HistoricoConsentimentoJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GerenciarConsentimentoPublicoUseCaseTest {

    @Mock
    private FielJpaRepository fielRepository;

    @Mock
    private ClerigoJpaRepository clerigoRepository;

    @Mock
    private DesafioConsentimentoJpaRepository desafioRepository;

    @Mock
    private HistoricoConsentimentoJpaRepository historicoRepository;

    private ServicoEmailNotificacaoConsentimento emailService;
    private TokenConsentimentoService tokenService;
    private GerenciarConsentimentoPublicoUseCase useCase;

    @BeforeEach
    void setUp() {
        emailService = new ServicoEmailNotificacaoConsentimento();
        tokenService = new TokenConsentimentoService("chave-secreta-de-teste-para-token-lgpd-2026-junit");
        useCase = new GerenciarConsentimentoPublicoUseCase(
                fielRepository,
                clerigoRepository,
                desafioRepository,
                historicoRepository,
                emailService,
                tokenService
        );
    }

    @Test
    @DisplayName("Deve retornar mensagem generica anti-enumeracao quando CPF nao for localizado")
    void deveRetornarMensagemAntiEnumeracaoQuandoCpfNaoExistir() {
        when(fielRepository.findByCpfHashAndAtivoTrue(any())).thenReturn(Optional.empty());
        when(clerigoRepository.findByAtivoTrueOrderByNomeEclesiasticoAsc()).thenReturn(Collections.emptyList());

        SolicitarCodigoConsentimentoResponse resp = useCase.solicitarCodigo(
                new SolicitarCodigoConsentimentoRequest("123.456.789-00"));

        assertThat(resp.emailMascarado()).isEqualTo("***@***");
        assertThat(resp.mensagem()).contains("Se este CPF estiver cadastrado");
    }

    @Test
    @DisplayName("Deve enviar codigo OTP e mascarar email quando titular for localizado com email")
    void deveEnviarCodigoOtpQuandoTitularLocalizadoComEmail() {
        FielEntity fiel = new FielEntity();
        fiel.setId(UUID.randomUUID());
        fiel.setNome("Maria Silva");
        fiel.setEmail("maria.silva@exemplo.com");

        when(fielRepository.findByCpfHashAndAtivoTrue(any())).thenReturn(Optional.of(fiel));

        SolicitarCodigoConsentimentoResponse resp = useCase.solicitarCodigo(
                new SolicitarCodigoConsentimentoRequest("123.456.789-00"));

        assertThat(resp.emailMascarado()).isEqualTo("m***a@exemplo.com");
        assertThat(resp.expiraEmSegundos()).isEqualTo(600);
        verify(desafioRepository).save(any(DesafioConsentimentoEntity.class));
    }

    @Test
    @DisplayName("Deve validar codigo OTP com sucesso e devolver token efemero e status")
    void deveValidarCodigoOtpComSucesso() {
        UUID fielId = UUID.randomUUID();
        FielEntity fiel = new FielEntity();
        fiel.setId(fielId);
        fiel.setNome("Pedro Santos");
        fiel.setEmail("pedro@exemplo.com");
        fiel.setDataNascimento(LocalDate.of(1990, 1, 1));
        fiel.setTermoConsentimentoAceito(true);

        String codigoValido = "123456";
        String codigoHash = emailService.hashCodigo(codigoValido);

        DesafioConsentimentoEntity desafio = new DesafioConsentimentoEntity();
        desafio.setId(UUID.randomUUID());
        desafio.setTitularTipo(TipoTitularConsentimento.FIEL);
        desafio.setTitularId(fielId);
        desafio.setCodigoHash(codigoHash);
        desafio.setExpiraEmUtc(Instant.now().plusSeconds(300));

        when(fielRepository.findByCpfHashAndAtivoTrue(any())).thenReturn(Optional.of(fiel));
        when(desafioRepository.findTopByTitularTipoAndTitularIdAndExpiraEmUtcAfterOrderByCriadoEmUtcDesc(
                eq(TipoTitularConsentimento.FIEL), eq(fielId), any())).thenReturn(Optional.of(desafio));

        ValidarCodigoConsentimentoResponse resp = useCase.validarCodigo(
                new ValidarCodigoConsentimentoRequest("12345678900", codigoValido));

        assertThat(resp.token()).isNotBlank();
        assertThat(resp.nome()).isEqualTo("Pedro Santos");
        assertThat(resp.isMenor()).isFalse();
        assertThat(resp.termoAceito()).isTrue();
        verify(desafioRepository).delete(desafio);
    }

    @Test
    @DisplayName("Deve registrar concessao de consentimento gravando historico imutavel")
    void deveRegistrarConcessaoGravandoHistorico() {
        UUID fielId = UUID.randomUUID();
        FielEntity fiel = new FielEntity();
        fiel.setId(fielId);
        fiel.setNome("Carlos Silva");
        fiel.setEmail("carlos@exemplo.com");
        fiel.setDataNascimento(LocalDate.of(2000, 5, 20));

        when(fielRepository.findById(fielId)).thenReturn(Optional.of(fiel));

        String token = tokenService.gerarToken(TipoTitularConsentimento.FIEL, fielId);
        ManifestarConsentimentoRequest req = new ManifestarConsentimentoRequest(
                true, true, false, null, null);

        ComprovanteConsentimentoResponse comp = useCase.manifestar(
                token, req, "192.168.1.100", "Mozilla/5.0 Test");

        assertThat(comp.protocolo()).isNotNull();
        assertThat(comp.tipoEvento()).isEqualTo("CONCESSAO");
        assertThat(fiel.isTermoConsentimentoAceito()).isTrue();
        assertThat(fiel.isUsoImagemAutorizado()).isTrue();
        assertThat(fiel.isPermitirDivulgacaoAniversario()).isFalse();
        assertThat(fiel.getIpConsentimento()).isEqualTo("192.168.1.100");
        verify(historicoRepository).save(any());
    }

    @Test
    @DisplayName("Deve exigir dados do responsavel legal quando fiel for menor de 18 anos")
    void deveExigirResponsavelQuandoMenor() {
        UUID fielId = UUID.randomUUID();
        FielEntity fiel = new FielEntity();
        fiel.setId(fielId);
        fiel.setNome("Jovem Menor");
        fiel.setDataNascimento(LocalDate.now().minusYears(15)); // 15 anos

        when(fielRepository.findById(fielId)).thenReturn(Optional.of(fiel));

        String token = tokenService.gerarToken(TipoTitularConsentimento.FIEL, fielId);
        ManifestarConsentimentoRequest reqSemResp = new ManifestarConsentimentoRequest(
                true, false, false, null, null);

        assertThatThrownBy(() -> useCase.manifestar(token, reqSemResp, "127.0.0.1", "Agent"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("responsável legal é obrigatório");
    }

    @Test
    @DisplayName("Deve revogar consentimento zerando switches e gravando evento de revogacao")
    void deveRevogarConsentimentoComSucesso() {
        UUID fielId = UUID.randomUUID();
        FielEntity fiel = new FielEntity();
        fiel.setId(fielId);
        fiel.setNome("Ana Paula");
        fiel.setTermoConsentimentoAceito(true);
        fiel.setUsoImagemAutorizado(true);

        when(fielRepository.findById(fielId)).thenReturn(Optional.of(fiel));

        String token = tokenService.gerarToken(TipoTitularConsentimento.FIEL, fielId);

        ComprovanteConsentimentoResponse comp = useCase.revogar(token, "200.10.20.30", "Chrome/Android");

        assertThat(comp.tipoEvento()).isEqualTo("REVOGACAO");
        assertThat(fiel.isTermoConsentimentoAceito()).isFalse();
        assertThat(fiel.isUsoImagemAutorizado()).isFalse();
        verify(historicoRepository).save(any());
    }
}
