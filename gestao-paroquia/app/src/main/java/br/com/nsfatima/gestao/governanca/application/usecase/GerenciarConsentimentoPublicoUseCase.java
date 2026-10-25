package br.com.nsfatima.gestao.governanca.application.usecase;

import br.com.nsfatima.gestao.clero.infrastructure.persistence.entity.ClerigoEntity;
import br.com.nsfatima.gestao.clero.infrastructure.persistence.repository.ClerigoJpaRepository;
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
import br.com.nsfatima.gestao.governanca.infrastructure.persistence.entity.DesafioConsentimentoEntity;
import br.com.nsfatima.gestao.governanca.infrastructure.persistence.entity.HistoricoConsentimentoEntity;
import br.com.nsfatima.gestao.governanca.infrastructure.persistence.repository.DesafioConsentimentoJpaRepository;
import br.com.nsfatima.gestao.governanca.infrastructure.persistence.repository.HistoricoConsentimentoJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    private final FielJpaRepository fielRepository;
    private final ClerigoJpaRepository clerigoRepository;
    private final DesafioConsentimentoJpaRepository desafioRepository;
    private final HistoricoConsentimentoJpaRepository historicoRepository;
    private final ServicoEmailNotificacaoConsentimento emailService;
    private final TokenConsentimentoService tokenService;
    private final String termoHashPadrao;

    public GerenciarConsentimentoPublicoUseCase(
            FielJpaRepository fielRepository,
            ClerigoJpaRepository clerigoRepository,
            DesafioConsentimentoJpaRepository desafioRepository,
            HistoricoConsentimentoJpaRepository historicoRepository,
            ServicoEmailNotificacaoConsentimento emailService,
            TokenConsentimentoService tokenService) {
        this.fielRepository = fielRepository;
        this.clerigoRepository = clerigoRepository;
        this.desafioRepository = desafioRepository;
        this.historicoRepository = historicoRepository;
        this.emailService = emailService;
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

    public SolicitarCodigoConsentimentoResponse solicitarCodigo(SolicitarCodigoConsentimentoRequest request) {
        String cpfLimpo = limparCpf(request.cpf());
        Optional<TitularEncontrado> titularOpt = buscarTitularPorCpf(cpfLimpo);

        if (titularOpt.isEmpty()) {
            return new SolicitarCodigoConsentimentoResponse(
                    "***@***", 600,
                    "Se este CPF estiver cadastrado em nossa paróquia, um código de verificação foi enviado "
                            + "para o e-mail registrado.");
        }

        TitularEncontrado titular = titularOpt.get();
        if (titular.email() == null || titular.email().isBlank()) {
            throw new IllegalArgumentException(
                    "Cadastro paroquial localizado, mas não há um e-mail registrado. "
                            + "Por favor, contate a secretaria paroquial para registrar seu e-mail.");
        }

        desafioRepository.deleteAll(desafioRepository.findByTitularTipoAndTitularId(titular.tipo(), titular.id()));

        String codigo = emailService.gerarCodigoNumerico();
        String codigoHash = emailService.hashCodigo(codigo);

        DesafioConsentimentoEntity desafio = new DesafioConsentimentoEntity();
        desafio.setId(UUID.randomUUID());
        desafio.setTitularTipo(titular.tipo());
        desafio.setTitularId(titular.id());
        desafio.setCodigoHash(codigoHash);
        desafio.setTentativas(0);
        desafio.setExpiraEmUtc(Instant.now().plusSeconds(600));
        desafioRepository.save(desafio);

        emailService.enviarCodigoOtp(titular.email(), codigo);

        return new SolicitarCodigoConsentimentoResponse(
                emailService.mascararEmail(titular.email()),
                600,
                "Código de verificação de 6 dígitos enviado para o e-mail cadastrado.");
    }

    public ValidarCodigoConsentimentoResponse validarCodigo(ValidarCodigoConsentimentoRequest request) {
        String cpfLimpo = limparCpf(request.cpf());
        TitularEncontrado titular = buscarTitularPorCpf(cpfLimpo)
                .orElseThrow(() -> new IllegalArgumentException("Código de verificação inválido ou expirado."));

        DesafioConsentimentoEntity desafio = desafioRepository
                .findTopByTitularTipoAndTitularIdAndExpiraEmUtcAfterOrderByCriadoEmUtcDesc(
                        titular.tipo(), titular.id(), Instant.now())
                .orElseThrow(() -> new IllegalArgumentException("Código de verificação inválido ou expirado."));

        if (desafio.getTentativas() >= 3) {
            desafioRepository.delete(desafio);
            throw new IllegalArgumentException(
                    "Número máximo de tentativas incorretas excedido. Solicite um novo código.");
        }

        String codigoHashRecebido = emailService.hashCodigo(request.codigo());
        if (!desafio.getCodigoHash().equals(codigoHashRecebido)) {
            desafio.setTentativas(desafio.getTentativas() + 1);
            desafioRepository.save(desafio);
            throw new IllegalArgumentException(
                    "Código de verificação incorreto. Tentativa " + desafio.getTentativas() + " de 3.");
        }

        desafioRepository.delete(desafio);

        String token = tokenService.gerarToken(titular.tipo(), titular.id());
        boolean isMenor = titular.dataNascimento() != null
                && Period.between(titular.dataNascimento(), LocalDate.now(ZoneOffset.UTC)).getYears() < 18;

        return new ValidarCodigoConsentimentoResponse(
                token,
                titular.tipo().name(),
                titular.id(),
                titular.nome(),
                isMenor,
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

    public ComprovanteConsentimentoResponse manifestar(
            String token, ManifestarConsentimentoRequest request, String ipOrigem, String userAgent) {
        TokenConsentimentoService.TokenPayload payload = tokenService.validarToken(token);
        String nomeTitular;
        String emailTitular;

        if (payload.titularTipo() == TipoTitularConsentimento.FIEL) {
            FielEntity fiel = fielRepository.findById(payload.titularId())
                    .orElseThrow(() -> new IllegalArgumentException("Fiel não encontrado."));
            nomeTitular = fiel.getNome();
            emailTitular = fiel.getEmail();

            boolean isMenor = fiel.getDataNascimento() != null
                    && Period.between(fiel.getDataNascimento(), LocalDate.now(ZoneOffset.UTC)).getYears() < 18;

            if (isMenor && request.termoAceito()) {
                if (request.responsavelMenorNome() == null || request.responsavelMenorNome().isBlank()) {
                    throw new IllegalArgumentException(
                            "O nome do responsável legal é obrigatório para titulares menores de idade.");
                }
                if (request.responsavelMenorCpf() == null || request.responsavelMenorCpf().isBlank()) {
                    throw new IllegalArgumentException(
                            "O CPF do responsável legal é obrigatório para titulares menores de idade.");
                }
                fiel.setResponsavelMenorNome(request.responsavelMenorNome());
                fiel.setResponsavelMenorCpf(request.responsavelMenorCpf());
            }

            fiel.setTermoConsentimentoAceito(request.termoAceito());
            fiel.setUsoImagemAutorizado(request.usoImagemAutorizado());
            fiel.setPermitirDivulgacaoAniversario(request.permitirDivulgacaoAniversario());
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

            clerigo.setTermoConsentimentoAceito(request.termoAceito());
            clerigo.setUsoImagemAutorizado(request.usoImagemAutorizado());
            clerigo.setPermitirDivulgacaoAniversario(request.permitirDivulgacaoAniversario());
            clerigo.setDataHoraConsentimentoUtc(Instant.now());
            clerigo.setIpConsentimento(ipOrigem);
            clerigo.setVersaoTermoConsentimento(TERMO_VERSAO_PADRAO);
            clerigo.setHashTermoConsentimento(termoHashPadrao);
            clerigo.setCanalConsentimento(CANAL_PORTAL);
            clerigoRepository.save(clerigo);
        }

        TipoEventoConsentimento evento = request.termoAceito()
                ? TipoEventoConsentimento.CONCESSAO
                : TipoEventoConsentimento.REVOGACAO;

        HistoricoConsentimentoEntity historico = new HistoricoConsentimentoEntity();
        historico.setId(UUID.randomUUID());
        historico.setTitularTipo(payload.titularTipo());
        historico.setTitularId(payload.titularId());
        historico.setTipoEvento(evento);
        historico.setCanal(CANAL_PORTAL);
        historico.setMetodoAutenticacao(METODO_OTP);
        historico.setTermoVersao(TERMO_VERSAO_PADRAO);
        historico.setTermoHash(termoHashPadrao);
        historico.setTermoAceito(request.termoAceito());
        historico.setUsoImagemAutorizado(request.usoImagemAutorizado());
        historico.setPermitirDivulgacaoAniversario(request.permitirDivulgacaoAniversario());
        historico.setIpOrigem(ipOrigem);
        historico.setUserAgent(userAgent);
        historico.setResponsavelMenorNome(request.responsavelMenorNome());
        historico.setResponsavelMenorCpf(request.responsavelMenorCpf());
        historicoRepository.save(historico);

        if (emailTitular != null) {
            emailService.enviarComprovante(emailTitular, evento.name(), historico.getId().toString());
        }

        return new ComprovanteConsentimentoResponse(
                historico.getId(),
                nomeTitular,
                evento.name(),
                Instant.now(),
                TERMO_VERSAO_PADRAO,
                termoHashPadrao,
                request.termoAceito()
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

    private Optional<TitularEncontrado> buscarTitularPorCpf(String cpfLimpo) {
        String cpfHash = calcularHash(cpfLimpo);
        Optional<FielEntity> fielOpt = fielRepository.findByCpfHashAndAtivoTrue(cpfHash);
        if (fielOpt.isPresent()) {
            FielEntity f = fielOpt.get();
            return Optional.of(new TitularEncontrado(
                    TipoTitularConsentimento.FIEL,
                    f.getId(),
                    f.getNome(),
                    f.getEmail(),
                    f.getDataNascimento(),
                    f.isTermoConsentimentoAceito(),
                    f.isUsoImagemAutorizado(),
                    f.isPermitirDivulgacaoAniversario(),
                    f.getResponsavelMenorNome(),
                    f.getResponsavelMenorCpf(),
                    f.getDataHoraConsentimentoUtc()));
        }

        List<ClerigoEntity> clerigos = clerigoRepository.findByAtivoTrueOrderByNomeEclesiasticoAsc();
        for (ClerigoEntity c : clerigos) {
            if (c.getCpf() != null && limparCpf(c.getCpf()).equals(cpfLimpo)) {
                return Optional.of(new TitularEncontrado(
                        TipoTitularConsentimento.CLERIGO,
                        c.getId(),
                        c.getNomeEclesiastico(),
                        c.getEmail(),
                        c.getDataNascimento(),
                        c.isTermoConsentimentoAceito(),
                        c.isUsoImagemAutorizado(),
                        c.isPermitirDivulgacaoAniversario(),
                        null,
                        null,
                        c.getDataHoraConsentimentoUtc()));
            }
        }

        return Optional.empty();
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
