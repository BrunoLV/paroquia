package br.com.nsfatima.gestao.governanca.application.usecase;

import br.com.nsfatima.gestao.clero.infrastructure.persistence.entity.ClerigoEntity;
import br.com.nsfatima.gestao.clero.infrastructure.persistence.repository.ClerigoJpaRepository;
import br.com.nsfatima.gestao.fiel.domain.service.ValidacaoDocumentoCivil;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.entity.FielEntity;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository.FielJpaRepository;
import br.com.nsfatima.gestao.governanca.api.v1.dto.ComprovanteConsentimentoResponse;
import br.com.nsfatima.gestao.governanca.api.v1.dto.HistoricoConsentimentoResponse;
import br.com.nsfatima.gestao.governanca.api.v1.dto.ManifestarConsentimentoRequest;
import br.com.nsfatima.gestao.governanca.api.v1.dto.SolicitarCodigoConsentimentoRequest;
import br.com.nsfatima.gestao.governanca.api.v1.dto.SolicitarCodigoConsentimentoResponse;
import br.com.nsfatima.gestao.governanca.api.v1.dto.ValidarCodigoConsentimentoRequest;
import br.com.nsfatima.gestao.governanca.api.v1.dto.ValidarCodigoConsentimentoResponse;
import br.com.nsfatima.gestao.governanca.application.service.ServicoEmailNotificacaoConsentimento;
import br.com.nsfatima.gestao.governanca.application.service.TokenConsentimentoService;
import br.com.nsfatima.gestao.governanca.domain.model.TipoEventoConsentimento;
import br.com.nsfatima.gestao.governanca.domain.model.TipoTitularConsentimento;
import br.com.nsfatima.gestao.governanca.domain.port.EnviadorEmailConsentimento;
import br.com.nsfatima.gestao.governanca.infrastructure.persistence.entity.DesafioConsentimentoEntity;
import br.com.nsfatima.gestao.governanca.infrastructure.persistence.entity.HistoricoConsentimentoEntity;
import br.com.nsfatima.gestao.governanca.infrastructure.persistence.repository.DesafioConsentimentoJpaRepository;
import br.com.nsfatima.gestao.governanca.infrastructure.persistence.repository.HistoricoConsentimentoJpaRepository;
import br.com.nsfatima.gestao.support.infrastructure.crypto.CpfBlindIndex;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneOffset;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Caso de uso principal para autoatendimento passwordless de consentimento sob a LGPD (Arts. 8º e 14).
 */
@Service
@Transactional
public class GerenciarConsentimentoPublicoUseCase {

    public static final String TERMO_VERSAO_PADRAO = "v1.0";
    private static final String CANAL_PORTAL = "PORTAL_AUTOATENDIMENTO";
    private static final String METODO_OTP = "OTP_EMAIL";
    public static final String TEXTO_TERMO_PADRAO = """
            TERMO DE PRIVACIDADE E CONSENTIMENTO PASTORAL - PARÓQUIA NOSSA SENHORA DE FÁTIMA
            Em conformidade com a Lei Geral de Proteção de Dados (Lei nº 13.709/2018 - LGPD)
            e com o Código de Direito Canônico (Cân. 535):
            1. FINALIDADE PASTORAL: Os dados coletados destinam-se exclusivamente às finalidades eclesiais,
            sacramentais, catequéticas e de comunicação comunitária da Paróquia.
            2. REGISTROS SACRAMENTAIS HISTÓRICOS: Conforme o Art. 16, I da LGPD e o Cân. 535, assentos de
            sacramentos (Batismo, Crisma, Matrimônio, Ordem) possuem fé pública e valor histórico perpétuo,
            permanecendo arquivados perenemente nos livros paroquiais.
            3. DIREITO DE REVOGAÇÃO: O titular pode revogar a qualquer momento o consentimento para fins de
            comunicação, divulgação comunitária de aniversário e veiculação de imagem/voz através deste portal.
            4. VEDAÇÃO COMERCIAL: É terminantemente proibida a comercialização, cessão onerosa ou uso dos dados
            para marketing econômico de terceiros (Art. 11, § 4º da LGPD).
            """;

    private static final int MAX_PEDIDOS_CODIGO = 3;
    private static final long JANELA_PEDIDOS_SEGUNDOS = 15L * 60;
    private static final int MAX_TENTATIVAS_CODIGO = 3;
    private static final int VALIDADE_CODIGO_SEGUNDOS = 600;
    private static final long RETENCAO_DESAFIOS_SEGUNDOS = 24L * 3600;
    private static final String MSG_CODIGO_INVALIDO = "Código de verificação inválido ou expirado.";
    private static final SolicitarCodigoConsentimentoResponse RESPOSTA_PEDIDO_CODIGO =
            new SolicitarCodigoConsentimentoResponse(
                    "***@***", VALIDADE_CODIGO_SEGUNDOS,
                    "Se este CPF estiver cadastrado em nossa paróquia e possuir e-mail registrado, um código de "
                            + "verificação foi enviado. Caso não o receba, contate a secretaria paroquial.");

    private final FielJpaRepository fielRepository;
    private final ClerigoJpaRepository clerigoRepository;
    private final DesafioConsentimentoJpaRepository desafioRepository;
    private final HistoricoConsentimentoJpaRepository historicoRepository;
    private final ServicoEmailNotificacaoConsentimento emailService;
    private final EnviadorEmailConsentimento enviadorEmail;
    private final TokenConsentimentoService tokenService;
    private final ValidacaoDocumentoCivil validacaoDocumento = new ValidacaoDocumentoCivil();
    private final String termoHashPadrao;

    public GerenciarConsentimentoPublicoUseCase(
            FielJpaRepository fielRepository,
            ClerigoJpaRepository clerigoRepository,
            DesafioConsentimentoJpaRepository desafioRepository,
            HistoricoConsentimentoJpaRepository historicoRepository,
            ServicoEmailNotificacaoConsentimento emailService,
            EnviadorEmailConsentimento enviadorEmail,
            TokenConsentimentoService tokenService) {
        this.fielRepository = fielRepository;
        this.clerigoRepository = clerigoRepository;
        this.desafioRepository = desafioRepository;
        this.historicoRepository = historicoRepository;
        this.emailService = emailService;
        this.enviadorEmail = enviadorEmail;
        this.tokenService = tokenService;
        this.termoHashPadrao = calcularHash(TEXTO_TERMO_PADRAO);
    }

    private record TitularEncontrado(
            TipoTitularConsentimento tipo,
            UUID id,
            String nome,
            String email,
            LocalDate dataNascimento,
            boolean termoAceito,
            boolean usoImagemAutorizado,
            boolean permitirDivulgacaoAniversario,
            String responsavelMenorNome,
            String responsavelMenorCpf,
            Instant dataHoraConsentimentoUtc
    ) {}

    private record Responsavel(String nome, String cpf) {
        static final Responsavel AUSENTE = new Responsavel(null, null);

        boolean presente() {
            return nome != null;
        }
    }

    /**
     * Pede um código OTP (RN-LGP-10). A resposta é sempre a mesma — CPF inexistente, titular sem e-mail,
     * limite de pedidos excedido ou código enviado —, para que o canal público não permita enumerar titulares.
     * No máximo {@value #MAX_PEDIDOS_CODIGO} códigos por titular a cada 15 minutos; pedidos excedentes são
     * descartados em silêncio. O e-mail só é disparado após o commit e a porta de envio é assíncrona, para
     * não criar diferença de latência entre CPF existente e inexistente.
     */
    public SolicitarCodigoConsentimentoResponse solicitarCodigo(SolicitarCodigoConsentimentoRequest request) {
        Instant agora = Instant.now();
        desafioRepository.deleteByCriadoEmUtcBefore(agora.minusSeconds(RETENCAO_DESAFIOS_SEGUNDOS));
        Optional<TitularEncontrado> titularOpt = buscarTitularPorCpf(limparCpf(request.cpf()))
                .filter(t -> t.email() != null && !t.email().isBlank());
        if (titularOpt.isEmpty()) {
            return RESPOSTA_PEDIDO_CODIGO;
        }
        TitularEncontrado titular = titularOpt.get();
        // ponytail: contagem sem trava; pedidos simultâneos podem exceder o limite em 1-2 unidades.
        long pedidosRecentes = desafioRepository.countByTitularTipoAndTitularIdAndCriadoEmUtcAfter(
                titular.tipo(), titular.id(), agora.minusSeconds(JANELA_PEDIDOS_SEGUNDOS));
        if (pedidosRecentes >= MAX_PEDIDOS_CODIGO) {
            return RESPOSTA_PEDIDO_CODIGO;
        }

        String codigo = emailService.gerarCodigoNumerico();
        DesafioConsentimentoEntity desafio = new DesafioConsentimentoEntity();
        desafio.setId(UUID.randomUUID());
        desafio.setTitularTipo(titular.tipo());
        desafio.setTitularId(titular.id());
        desafio.setCodigoHash(emailService.hashCodigo(codigo));
        desafio.setTentativas(0);
        desafio.setExpiraEmUtc(agora.plusSeconds(VALIDADE_CODIGO_SEGUNDOS));
        desafioRepository.save(desafio);

        executarAposCommit(() -> enviadorEmail.enviarCodigoOtp(titular.email(), codigo));
        return RESPOSTA_PEDIDO_CODIGO;
    }

    /**
     * Valida o OTP e emite o token de uso único. Toda falha — titular inexistente, sem desafio vigente, código
     * errado ou tentativas esgotadas — produz a mesma mensagem. O contador de tentativas é persistido mesmo
     * quando a exceção é lançada ({@code noRollbackFor}); sem isso o limite nunca valeria.
     */
    @Transactional(noRollbackFor = IllegalArgumentException.class)
    public ValidarCodigoConsentimentoResponse validarCodigo(ValidarCodigoConsentimentoRequest request) {
        TitularEncontrado titular = buscarTitularPorCpf(limparCpf(request.cpf()))
                .orElseThrow(() -> new IllegalArgumentException(MSG_CODIGO_INVALIDO));
        DesafioConsentimentoEntity desafio = desafioRepository
                .findTopByTitularTipoAndTitularIdAndValidadoEmUtcIsNullAndExpiraEmUtcAfterOrderByCriadoEmUtcDesc(
                        titular.tipo(), titular.id(), Instant.now())
                .orElseThrow(() -> new IllegalArgumentException(MSG_CODIGO_INVALIDO));

        if (desafio.getTentativas() >= MAX_TENTATIVAS_CODIGO || !codigoConfere(desafio, request.codigo())) {
            desafio.setTentativas(desafio.getTentativas() + 1);
            desafioRepository.save(desafio);
            throw new IllegalArgumentException(MSG_CODIGO_INVALIDO);
        }

        desafio.setValidadoEmUtc(Instant.now());
        desafioRepository.save(desafio);

        String token = tokenService.gerarToken(titular.tipo(), titular.id(), desafio.getId());
        return new ValidarCodigoConsentimentoResponse(
                token,
                titular.tipo().name(),
                titular.id(),
                titular.nome(),
                ehMenor(titular.dataNascimento()),
                titular.termoAceito(),
                TERMO_VERSAO_PADRAO,
                termoHashPadrao,
                TEXTO_TERMO_PADRAO,
                titular.usoImagemAutorizado(),
                titular.permitirDivulgacaoAniversario(),
                titular.responsavelMenorNome(),
                titular.responsavelMenorCpf(),
                titular.dataHoraConsentimentoUtc()
        );
    }

    /**
     * Registra a manifestação. O token é de uso único: é consumido atomicamente antes de qualquer alteração e,
     * se uma validação de negócio falhar, a transação reverte e o token continua utilizável para correção.
     * Com {@code termoAceito=false} (revogação) as autorizações secundárias são sempre zeradas, ignorando o
     * que o cliente enviou, e os dados do responsável legal só são exigidos e gravados para menores que aceitam.
     */
    public ComprovanteConsentimentoResponse manifestar(
            String token, ManifestarConsentimentoRequest request, String ipOrigem, String userAgent) {
        TokenConsentimentoService.TokenPayload payload = tokenService.validarToken(token);
        if (desafioRepository.consumir(payload.jti(), Instant.now()) == 0) {
            throw new IllegalArgumentException("Token de sessão de consentimento já utilizado ou inválido.");
        }
        boolean aceito = request.termoAceito();
        boolean usoImagem = aceito && request.usoImagemAutorizado();
        boolean divulgacao = aceito && request.permitirDivulgacaoAniversario();
        Responsavel responsavel;
        String nomeTitular;
        String emailTitular;

        if (payload.titularTipo() == TipoTitularConsentimento.FIEL) {
            FielEntity fiel = fielRepository.findById(payload.titularId())
                    .orElseThrow(() -> new IllegalArgumentException("Fiel não encontrado."));
            nomeTitular = fiel.getNome();
            emailTitular = fiel.getEmail();
            responsavel = exigirResponsavelSeMenor(fiel.getDataNascimento(), request);
            if (responsavel.presente()) {
                fiel.setResponsavelMenorNome(responsavel.nome());
                fiel.setResponsavelMenorCpf(responsavel.cpf());
            }
            fiel.setTermoConsentimentoAceito(aceito);
            fiel.setUsoImagemAutorizado(usoImagem);
            fiel.setPermitirDivulgacaoAniversario(divulgacao);
            fiel.setDataHoraConsentimentoUtc(Instant.now());
            fiel.setIpConsentimento(ipOrigem);
            fiel.setVersaoTermoConsentimento(TERMO_VERSAO_PADRAO);
            fiel.setHashTermoConsentimento(termoHashPadrao);
            fiel.setCanalConsentimento(CANAL_PORTAL);
            fielRepository.save(fiel);
        } else {
            ClerigoEntity clerigo = clerigoRepository.findById(payload.titularId())
                    .orElseThrow(() -> new IllegalArgumentException("Clérigo não encontrado."));
            nomeTitular = clerigo.getNomeEclesiastico();
            emailTitular = clerigo.getEmail();
            responsavel = Responsavel.AUSENTE;
            clerigo.setTermoConsentimentoAceito(aceito);
            clerigo.setUsoImagemAutorizado(usoImagem);
            clerigo.setPermitirDivulgacaoAniversario(divulgacao);
            clerigo.setDataHoraConsentimentoUtc(Instant.now());
            clerigo.setIpConsentimento(ipOrigem);
            clerigo.setVersaoTermoConsentimento(TERMO_VERSAO_PADRAO);
            clerigo.setHashTermoConsentimento(termoHashPadrao);
            clerigo.setCanalConsentimento(CANAL_PORTAL);
            clerigoRepository.save(clerigo);
        }

        TipoEventoConsentimento evento = aceito ? TipoEventoConsentimento.CONCESSAO : TipoEventoConsentimento.REVOGACAO;
        HistoricoConsentimentoEntity historico = registrarHistorico(
                payload, evento, ipOrigem, userAgent, usoImagem, divulgacao, responsavel);

        if (emailTitular != null) {
            String protocolo = historico.getId().toString();
            String destinatario = emailTitular;
            executarAposCommit(() -> enviadorEmail.enviarComprovante(destinatario, evento.name(), protocolo));
        }

        return new ComprovanteConsentimentoResponse(
                historico.getId(),
                nomeTitular,
                evento.name(),
                Instant.now(),
                TERMO_VERSAO_PADRAO,
                termoHashPadrao,
                aceito
                        ? "Consentimento e preferências registradas com sucesso."
                        : "Consentimento revogado com sucesso.");
    }

    public ComprovanteConsentimentoResponse revogar(String token, String ipOrigem, String userAgent) {
        ManifestarConsentimentoRequest requestRevogacao = new ManifestarConsentimentoRequest(
                false, false, false, null, null);
        return manifestar(token, requestRevogacao, ipOrigem, userAgent);
    }

    @Transactional(readOnly = true)
    public List<HistoricoConsentimentoResponse> listarHistorico(TipoTitularConsentimento titularTipo, UUID titularId) {
        return historicoRepository.findByTitularTipoAndTitularIdOrderByCriadoEmUtcDesc(titularTipo, titularId)
                .stream()
                .map(this::mapHistorico)
                .toList();
    }

    private Responsavel exigirResponsavelSeMenor(LocalDate nascimento, ManifestarConsentimentoRequest request) {
        if (!ehMenor(nascimento) || !request.termoAceito()) {
            return Responsavel.AUSENTE;
        }
        if (request.responsavelMenorNome() == null || request.responsavelMenorNome().isBlank()) {
            throw new IllegalArgumentException(
                    "O nome do responsável legal é obrigatório para titulares menores de idade.");
        }
        if (request.responsavelMenorCpf() == null || request.responsavelMenorCpf().isBlank()) {
            throw new IllegalArgumentException(
                    "O CPF do responsável legal é obrigatório para titulares menores de idade.");
        }
        if (!validacaoDocumento.cpfEstritamenteValido(request.responsavelMenorCpf())) {
            throw new IllegalArgumentException("O CPF do responsável legal informado é inválido.");
        }
        return new Responsavel(request.responsavelMenorNome().trim(), limparCpf(request.responsavelMenorCpf()));
    }

    private HistoricoConsentimentoEntity registrarHistorico(
            TokenConsentimentoService.TokenPayload payload, TipoEventoConsentimento evento, String ipOrigem,
            String userAgent, boolean usoImagem, boolean divulgacao, Responsavel responsavel) {
        HistoricoConsentimentoEntity historico = new HistoricoConsentimentoEntity();
        historico.setId(UUID.randomUUID());
        historico.setTitularTipo(payload.titularTipo());
        historico.setTitularId(payload.titularId());
        historico.setTipoEvento(evento);
        historico.setCanal(CANAL_PORTAL);
        historico.setMetodoAutenticacao(METODO_OTP);
        historico.setTermoVersao(TERMO_VERSAO_PADRAO);
        historico.setTermoHash(termoHashPadrao);
        historico.setTermoAceito(evento == TipoEventoConsentimento.CONCESSAO);
        historico.setUsoImagemAutorizado(usoImagem);
        historico.setPermitirDivulgacaoAniversario(divulgacao);
        historico.setIpOrigem(ipOrigem);
        historico.setUserAgent(userAgent);
        historico.setResponsavelMenorNome(responsavel.nome());
        historico.setResponsavelMenorCpf(responsavel.cpf());
        historicoRepository.save(historico);
        return historico;
    }

    private HistoricoConsentimentoResponse mapHistorico(HistoricoConsentimentoEntity h) {
        return new HistoricoConsentimentoResponse(
                h.getId(),
                h.getTipoEvento().name(),
                h.getCanal(),
                h.getMetodoAutenticacao(),
                h.getTermoVersao(),
                h.getTermoHash(),
                h.isTermoAceito(),
                h.isUsoImagemAutorizado(),
                h.isPermitirDivulgacaoAniversario(),
                h.getResponsavelMenorNome(),
                h.getResponsavelMenorCpf(),
                h.getIpOrigem(),
                h.getUserAgent(),
                h.getCriadoEmUtc());
    }

    /**
     * Localiza o titular pelo blind index (HMAC) do CPF, tanto de fiéis quanto de clérigos: nenhuma varredura
     * com decifragem em lote é feita no canal público.
     */
    private Optional<TitularEncontrado> buscarTitularPorCpf(String cpfLimpo) {
        String cpfHash = CpfBlindIndex.hash(cpfLimpo);
        Optional<TitularEncontrado> fiel = fielRepository.findByCpfHashAndAtivoTrue(cpfHash)
                .map(f -> new TitularEncontrado(
                        TipoTitularConsentimento.FIEL, f.getId(), f.getNome(), f.getEmail(),
                        f.getDataNascimento(), f.isTermoConsentimentoAceito(), f.isUsoImagemAutorizado(),
                        f.isPermitirDivulgacaoAniversario(), f.getResponsavelMenorNome(),
                        f.getResponsavelMenorCpf(), f.getDataHoraConsentimentoUtc()));
        if (fiel.isPresent()) {
            return fiel;
        }
        return clerigoRepository.findByCpfHashAndAtivoTrue(cpfHash)
                .map(c -> new TitularEncontrado(
                        TipoTitularConsentimento.CLERIGO, c.getId(), c.getNomeEclesiastico(), c.getEmail(),
                        c.getDataNascimento(), c.isTermoConsentimentoAceito(), c.isUsoImagemAutorizado(),
                        c.isPermitirDivulgacaoAniversario(), null, null, c.getDataHoraConsentimentoUtc()));
    }

    private boolean codigoConfere(DesafioConsentimentoEntity desafio, String codigoInformado) {
        byte[] esperado = desafio.getCodigoHash().getBytes(StandardCharsets.UTF_8);
        byte[] recebido = emailService.hashCodigo(codigoInformado).getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(esperado, recebido);
    }

    private boolean ehMenor(LocalDate nascimento) {
        return nascimento != null && Period.between(nascimento, LocalDate.now(ZoneOffset.UTC)).getYears() < 18;
    }

    private void executarAposCommit(Runnable acao) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    acao.run();
                }
            });
        } else {
            acao.run();
        }
    }

    private String limparCpf(String cpf) {
        if (cpf == null) {
            return "";
        }
        return cpf.replaceAll("\\D", "");
    }

    private String calcularHash(String valor) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(valor.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponível", e);
        }
    }
}
