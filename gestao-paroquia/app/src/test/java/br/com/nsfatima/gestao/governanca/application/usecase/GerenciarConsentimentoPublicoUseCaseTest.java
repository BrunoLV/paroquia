package br.com.nsfatima.gestao.governanca.application.usecase;

import br.com.nsfatima.gestao.clero.infrastructure.persistence.entity.ClerigoEntity;
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
import br.com.nsfatima.gestao.governanca.domain.port.EnviadorEmailConsentimento;
import br.com.nsfatima.gestao.governanca.infrastructure.persistence.entity.DesafioConsentimentoEntity;
import br.com.nsfatima.gestao.governanca.infrastructure.persistence.entity.HistoricoConsentimentoEntity;
import br.com.nsfatima.gestao.governanca.infrastructure.persistence.repository.DesafioConsentimentoJpaRepository;
import br.com.nsfatima.gestao.governanca.infrastructure.persistence.repository.HistoricoConsentimentoJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GerenciarConsentimentoPublicoUseCaseTest {

    private static final String CPF_RESPONSAVEL_VALIDO = "529.982.247-25";

    @Mock
    private FielJpaRepository fielRepository;

    @Mock
    private ClerigoJpaRepository clerigoRepository;

    @Mock
    private DesafioConsentimentoJpaRepository desafioRepository;

    @Mock
    private HistoricoConsentimentoJpaRepository historicoRepository;

    @Mock
    private EnviadorEmailConsentimento enviadorEmail;

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
                enviadorEmail,
                tokenService
        );
    }

    private FielEntity fielComEmail(UUID id) {
        FielEntity fiel = new FielEntity();
        fiel.setId(id);
        fiel.setNome("Maria Silva");
        fiel.setEmail("maria.silva@exemplo.com");
        return fiel;
    }

    private void tokenConsumivel(UUID jti) {
        when(desafioRepository.consumir(eq(jti), any())).thenReturn(1);
    }

    @Test
    @DisplayName("Resposta ao pedido de codigo e identica para CPF inexistente, sem e-mail, limite excedido e sucesso")
    void respostaDoPedidoDeCodigoNaoPermiteEnumerar() {
        UUID semEmailId = UUID.randomUUID();
        FielEntity semEmail = new FielEntity();
        semEmail.setId(semEmailId);
        FielEntity comEmail = fielComEmail(UUID.randomUUID());
        when(fielRepository.findByCpfHashAndAtivoTrue(any()))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(semEmail))
                .thenReturn(Optional.of(comEmail))
                .thenReturn(Optional.of(comEmail));
        when(clerigoRepository.findByCpfHashAndAtivoTrue(any())).thenReturn(Optional.empty());
        when(desafioRepository.countByTitularTipoAndTitularIdAndCriadoEmUtcAfter(
                eq(TipoTitularConsentimento.FIEL), eq(comEmail.getId()), any())).thenReturn(3L, 0L);

        SolicitarCodigoConsentimentoRequest req = new SolicitarCodigoConsentimentoRequest("123.456.789-00");
        SolicitarCodigoConsentimentoResponse inexistente = useCase.solicitarCodigo(req);
        SolicitarCodigoConsentimentoResponse semEmailResp = useCase.solicitarCodigo(req);
        SolicitarCodigoConsentimentoResponse limiteExcedido = useCase.solicitarCodigo(req);
        SolicitarCodigoConsentimentoResponse enviado = useCase.solicitarCodigo(req);

        assertThat(inexistente.emailMascarado()).isEqualTo("***@***");
        assertThat(inexistente.mensagem()).contains("Se este CPF estiver cadastrado");
        assertThat(semEmailResp).isEqualTo(inexistente);
        assertThat(limiteExcedido).isEqualTo(inexistente);
        assertThat(enviado).isEqualTo(inexistente);
        verify(desafioRepository).save(any(DesafioConsentimentoEntity.class));
    }

    @Test
    @DisplayName("Deve persistir o desafio e enviar o codigo ao e-mail do titular pela porta de envio")
    void deveEnviarCodigoPelaPorta() {
        FielEntity fiel = fielComEmail(UUID.randomUUID());
        when(fielRepository.findByCpfHashAndAtivoTrue(any())).thenReturn(Optional.of(fiel));
        when(desafioRepository.countByTitularTipoAndTitularIdAndCriadoEmUtcAfter(any(), any(), any()))
                .thenReturn(2L);

        useCase.solicitarCodigo(new SolicitarCodigoConsentimentoRequest("123.456.789-00"));

        ArgumentCaptor<DesafioConsentimentoEntity> desafio = ArgumentCaptor.forClass(DesafioConsentimentoEntity.class);
        ArgumentCaptor<String> codigo = ArgumentCaptor.forClass(String.class);
        verify(desafioRepository).save(desafio.capture());
        verify(enviadorEmail).enviarCodigoOtp(eq("maria.silva@exemplo.com"), codigo.capture());
        assertThat(codigo.getValue()).matches("\\d{6}");
        assertThat(desafio.getValue().getCodigoHash()).isEqualTo(emailService.hashCodigo(codigo.getValue()));
        assertThat(desafio.getValue().getCodigoHash()).doesNotContain(codigo.getValue());
    }

    @Test
    @DisplayName("Limite de pedidos excedido nao cria desafio nem envia e-mail")
    void limiteDePedidosNaoCriaDesafio() {
        FielEntity fiel = fielComEmail(UUID.randomUUID());
        when(fielRepository.findByCpfHashAndAtivoTrue(any())).thenReturn(Optional.of(fiel));
        when(desafioRepository.countByTitularTipoAndTitularIdAndCriadoEmUtcAfter(any(), any(), any()))
                .thenReturn(3L);

        useCase.solicitarCodigo(new SolicitarCodigoConsentimentoRequest("12345678900"));

        verify(desafioRepository, never()).save(any());
        verify(enviadorEmail, never()).enviarCodigoOtp(any(), any());
    }

    @Test
    @DisplayName("Deve validar codigo OTP, marcar o desafio como validado e emitir token vinculado a ele")
    void deveValidarCodigoOtpComSucesso() {
        UUID fielId = UUID.randomUUID();
        FielEntity fiel = new FielEntity();
        fiel.setId(fielId);
        fiel.setNome("Pedro Santos");
        fiel.setEmail("pedro@exemplo.com");
        fiel.setDataNascimento(LocalDate.of(1990, 1, 1));
        fiel.setTermoConsentimentoAceito(true);

        DesafioConsentimentoEntity desafio = desafioDoTitular(fielId, "123456");

        when(fielRepository.findByCpfHashAndAtivoTrue(any())).thenReturn(Optional.of(fiel));
        when(desafioRepository
                .findTopByTitularTipoAndTitularIdAndValidadoEmUtcIsNullAndExpiraEmUtcAfterOrderByCriadoEmUtcDesc(
                        eq(TipoTitularConsentimento.FIEL), eq(fielId), any())).thenReturn(Optional.of(desafio));

        ValidarCodigoConsentimentoResponse resp = useCase.validarCodigo(
                new ValidarCodigoConsentimentoRequest("12345678900", "123456"));

        assertThat(resp.nome()).isEqualTo("Pedro Santos");
        assertThat(resp.isMenor()).isFalse();
        assertThat(resp.termoAceito()).isTrue();
        assertThat(desafio.getValidadoEmUtc()).isNotNull();
        assertThat(tokenService.validarToken(resp.token()).jti()).isEqualTo(desafio.getId());
    }

    @Test
    @DisplayName("Codigo errado devolve mensagem uniforme e persiste a tentativa")
    void codigoErradoIncrementaTentativas() {
        UUID fielId = UUID.randomUUID();
        FielEntity fiel = fielComEmail(fielId);
        DesafioConsentimentoEntity desafio = desafioDoTitular(fielId, "123456");
        when(fielRepository.findByCpfHashAndAtivoTrue(any())).thenReturn(Optional.of(fiel));
        when(desafioRepository
                .findTopByTitularTipoAndTitularIdAndValidadoEmUtcIsNullAndExpiraEmUtcAfterOrderByCriadoEmUtcDesc(
                        any(), any(), any())).thenReturn(Optional.of(desafio));

        assertThatThrownBy(() -> useCase.validarCodigo(new ValidarCodigoConsentimentoRequest("12345678900", "000000")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Código de verificação inválido ou expirado.");

        assertThat(desafio.getTentativas()).isEqualTo(1);
        verify(desafioRepository).save(desafio);
        assertThat(desafio.getValidadoEmUtc()).isNull();
    }

    @Test
    @DisplayName("Tentativas esgotadas rejeitam ate o codigo correto, com a mesma mensagem")
    void tentativasEsgotadasRejeitamCodigoCorreto() {
        UUID fielId = UUID.randomUUID();
        DesafioConsentimentoEntity desafio = desafioDoTitular(fielId, "123456");
        desafio.setTentativas(3);
        when(fielRepository.findByCpfHashAndAtivoTrue(any())).thenReturn(Optional.of(fielComEmail(fielId)));
        when(desafioRepository
                .findTopByTitularTipoAndTitularIdAndValidadoEmUtcIsNullAndExpiraEmUtcAfterOrderByCriadoEmUtcDesc(
                        any(), any(), any())).thenReturn(Optional.of(desafio));

        assertThatThrownBy(() -> useCase.validarCodigo(new ValidarCodigoConsentimentoRequest("12345678900", "123456")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Código de verificação inválido ou expirado.");
        assertThat(desafio.getValidadoEmUtc()).isNull();
    }

    @Test
    @DisplayName("Validar codigo de CPF inexistente devolve a mesma mensagem de codigo incorreto")
    void validarCpfInexistenteUsaMensagemUniforme() {
        when(fielRepository.findByCpfHashAndAtivoTrue(any())).thenReturn(Optional.empty());
        when(clerigoRepository.findByCpfHashAndAtivoTrue(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.validarCodigo(new ValidarCodigoConsentimentoRequest("12345678900", "123456")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Código de verificação inválido ou expirado.");
    }

    @Test
    @DisplayName("Token ja utilizado ou nunca validado e rejeitado sem alterar o titular")
    void tokenDeUsoUnico() {
        UUID fielId = UUID.randomUUID();
        UUID jti = UUID.randomUUID();
        when(desafioRepository.consumir(eq(jti), any())).thenReturn(0);
        String token = tokenService.gerarToken(TipoTitularConsentimento.FIEL, fielId, jti);
        ManifestarConsentimentoRequest req = new ManifestarConsentimentoRequest(true, true, false, null, null);

        assertThatThrownBy(() -> useCase.manifestar(token, req, "127.0.0.1", "Agent"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("já utilizado");
        verify(fielRepository, never()).save(any());
        verify(historicoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve registrar concessao de consentimento gravando historico e enviando comprovante")
    void deveRegistrarConcessaoGravandoHistorico() {
        UUID fielId = UUID.randomUUID();
        UUID jti = UUID.randomUUID();
        FielEntity fiel = fielComEmail(fielId);
        fiel.setDataNascimento(LocalDate.of(2000, 5, 20));
        when(fielRepository.findById(fielId)).thenReturn(Optional.of(fiel));
        when(historicoRepository.save(any(HistoricoConsentimentoEntity.class))).thenAnswer(i -> i.getArgument(0));
        tokenConsumivel(jti);

        String token = tokenService.gerarToken(TipoTitularConsentimento.FIEL, fielId, jti);
        ComprovanteConsentimentoResponse comp = useCase.manifestar(
                token, new ManifestarConsentimentoRequest(true, true, false, null, null),
                "192.168.1.100", "Mozilla/5.0 Test");

        assertThat(comp.tipoEvento()).isEqualTo("CONCESSAO");
        assertThat(fiel.isTermoConsentimentoAceito()).isTrue();
        assertThat(fiel.isUsoImagemAutorizado()).isTrue();
        assertThat(fiel.isPermitirDivulgacaoAniversario()).isFalse();
        assertThat(fiel.getIpConsentimento()).isEqualTo("192.168.1.100");
        verify(historicoRepository).save(any());
        verify(enviadorEmail).enviarComprovante(eq("maria.silva@exemplo.com"), eq("CONCESSAO"), any());
    }

    @Test
    @DisplayName("Com termoAceito=false as autorizacoes de imagem e aniversario sao forcadas a false")
    void semTermoAceitoAutorizacoesViramFalse() {
        UUID fielId = UUID.randomUUID();
        UUID jti = UUID.randomUUID();
        FielEntity fiel = fielComEmail(fielId);
        fiel.setUsoImagemAutorizado(true);
        fiel.setPermitirDivulgacaoAniversario(true);
        when(fielRepository.findById(fielId)).thenReturn(Optional.of(fiel));
        when(historicoRepository.save(any(HistoricoConsentimentoEntity.class))).thenAnswer(i -> i.getArgument(0));
        tokenConsumivel(jti);

        String token = tokenService.gerarToken(TipoTitularConsentimento.FIEL, fielId, jti);
        useCase.manifestar(token, new ManifestarConsentimentoRequest(false, true, true, null, null), "1.1.1.1", "UA");

        assertThat(fiel.isTermoConsentimentoAceito()).isFalse();
        assertThat(fiel.isUsoImagemAutorizado()).isFalse();
        assertThat(fiel.isPermitirDivulgacaoAniversario()).isFalse();
        ArgumentCaptor<HistoricoConsentimentoEntity> historico =
                ArgumentCaptor.forClass(HistoricoConsentimentoEntity.class);
        verify(historicoRepository).save(historico.capture());
        assertThat(historico.getValue().isUsoImagemAutorizado()).isFalse();
        assertThat(historico.getValue().isPermitirDivulgacaoAniversario()).isFalse();
    }

    @Test
    @DisplayName("Deve exigir dados do responsavel legal quando fiel for menor de 18 anos")
    void deveExigirResponsavelQuandoMenor() {
        UUID fielId = UUID.randomUUID();
        UUID jti = UUID.randomUUID();
        FielEntity fiel = new FielEntity();
        fiel.setId(fielId);
        fiel.setNome("Jovem Menor");
        fiel.setDataNascimento(LocalDate.now().minusYears(15));
        when(fielRepository.findById(fielId)).thenReturn(Optional.of(fiel));
        tokenConsumivel(jti);

        String token = tokenService.gerarToken(TipoTitularConsentimento.FIEL, fielId, jti);
        ManifestarConsentimentoRequest reqSemResp = new ManifestarConsentimentoRequest(true, false, false, null, null);

        assertThatThrownBy(() -> useCase.manifestar(token, reqSemResp, "127.0.0.1", "Agent"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("responsável legal é obrigatório");
    }

    @Test
    @DisplayName("CPF invalido do responsavel legal do menor e rejeitado")
    void cpfInvalidoDoResponsavelERejeitado() {
        UUID fielId = UUID.randomUUID();
        UUID jti = UUID.randomUUID();
        FielEntity fiel = new FielEntity();
        fiel.setId(fielId);
        fiel.setDataNascimento(LocalDate.now().minusYears(15));
        when(fielRepository.findById(fielId)).thenReturn(Optional.of(fiel));
        tokenConsumivel(jti);

        String token = tokenService.gerarToken(TipoTitularConsentimento.FIEL, fielId, jti);
        ManifestarConsentimentoRequest req = new ManifestarConsentimentoRequest(
                true, false, false, "Mãe da Criança", "111.111.111-11");

        assertThatThrownBy(() -> useCase.manifestar(token, req, "127.0.0.1", "Agent"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CPF do responsável legal informado é inválido");
        verify(fielRepository, never()).save(any());
    }

    @Test
    @DisplayName("Menor com responsavel legal valido grava o responsavel normalizado no cadastro e na trilha")
    void menorComResponsavelValido() {
        UUID fielId = UUID.randomUUID();
        UUID jti = UUID.randomUUID();
        FielEntity fiel = new FielEntity();
        fiel.setId(fielId);
        fiel.setDataNascimento(LocalDate.now().minusYears(15));
        when(fielRepository.findById(fielId)).thenReturn(Optional.of(fiel));
        when(historicoRepository.save(any(HistoricoConsentimentoEntity.class))).thenAnswer(i -> i.getArgument(0));
        tokenConsumivel(jti);

        String token = tokenService.gerarToken(TipoTitularConsentimento.FIEL, fielId, jti);
        useCase.manifestar(token, new ManifestarConsentimentoRequest(
                true, false, false, "Mãe da Criança", CPF_RESPONSAVEL_VALIDO), "10.0.0.1", "UA");

        assertThat(fiel.getResponsavelMenorCpf()).isEqualTo("52998224725");
        ArgumentCaptor<HistoricoConsentimentoEntity> historico =
                ArgumentCaptor.forClass(HistoricoConsentimentoEntity.class);
        verify(historicoRepository).save(historico.capture());
        assertThat(historico.getValue().getResponsavelMenorCpf()).isEqualTo("52998224725");
    }

    @Test
    @DisplayName("Titular clerigo e localizado por blind index, sem varrer e decifrar todos os clerigos")
    void clerigoELocalizadoPorBlindIndex() {
        ClerigoEntity clerigo = new ClerigoEntity();
        clerigo.setId(UUID.randomUUID());
        clerigo.setNomeEclesiastico("Pe. João");
        clerigo.setEmail("pe.joao@exemplo.com");
        when(fielRepository.findByCpfHashAndAtivoTrue(any())).thenReturn(Optional.empty());
        when(clerigoRepository.findByCpfHashAndAtivoTrue(any())).thenReturn(Optional.of(clerigo));
        when(desafioRepository.countByTitularTipoAndTitularIdAndCriadoEmUtcAfter(
                eq(TipoTitularConsentimento.CLERIGO), eq(clerigo.getId()), any())).thenReturn(0L);

        useCase.solicitarCodigo(new SolicitarCodigoConsentimentoRequest("529.982.247-25"));

        verify(enviadorEmail).enviarCodigoOtp(eq("pe.joao@exemplo.com"), any());
        verify(clerigoRepository, never()).findByAtivoTrueOrderByNomeEclesiasticoAsc();
    }

    @Test
    @DisplayName("Deve revogar consentimento zerando switches e gravando evento de revogacao")
    void deveRevogarConsentimentoComSucesso() {
        UUID fielId = UUID.randomUUID();
        UUID jti = UUID.randomUUID();
        FielEntity fiel = new FielEntity();
        fiel.setId(fielId);
        fiel.setNome("Ana Paula");
        fiel.setTermoConsentimentoAceito(true);
        fiel.setUsoImagemAutorizado(true);
        when(fielRepository.findById(fielId)).thenReturn(Optional.of(fiel));
        when(historicoRepository.save(any(HistoricoConsentimentoEntity.class))).thenAnswer(i -> i.getArgument(0));
        tokenConsumivel(jti);

        String token = tokenService.gerarToken(TipoTitularConsentimento.FIEL, fielId, jti);
        ComprovanteConsentimentoResponse comp = useCase.revogar(token, "200.10.20.30", "Chrome/Android");

        assertThat(comp.tipoEvento()).isEqualTo("REVOGACAO");
        assertThat(fiel.isTermoConsentimentoAceito()).isFalse();
        assertThat(fiel.isUsoImagemAutorizado()).isFalse();
        verify(historicoRepository).save(any());
    }

    private DesafioConsentimentoEntity desafioDoTitular(UUID titularId, String codigo) {
        DesafioConsentimentoEntity desafio = new DesafioConsentimentoEntity();
        desafio.setId(UUID.randomUUID());
        desafio.setTitularTipo(TipoTitularConsentimento.FIEL);
        desafio.setTitularId(titularId);
        desafio.setCodigoHash(emailService.hashCodigo(codigo));
        desafio.setExpiraEmUtc(Instant.now().plusSeconds(300));
        return desafio;
    }
}
