package br.com.nsfatima.gestao.sacramento.application.usecase;

import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import br.com.nsfatima.gestao.sacramento.api.v1.dto.CertidaoResponse;
import br.com.nsfatima.gestao.sacramento.application.service.AssentoAcessoService;
import br.com.nsfatima.gestao.sacramento.application.service.SacramentoAuditoriaService;
import br.com.nsfatima.gestao.sacramento.domain.exception.AssentoImutavelException;
import br.com.nsfatima.gestao.sacramento.domain.model.AssentoSacramental;
import br.com.nsfatima.gestao.sacramento.domain.model.Averbacao;
import br.com.nsfatima.gestao.sacramento.domain.model.OperacaoSacramental;
import br.com.nsfatima.gestao.sacramento.domain.model.PapelParticipante;
import br.com.nsfatima.gestao.sacramento.domain.model.Participante;
import br.com.nsfatima.gestao.sacramento.domain.model.Ressalva;
import br.com.nsfatima.gestao.sacramento.domain.model.StatusAssento;
import br.com.nsfatima.gestao.sacramento.domain.model.StatusAverbacao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Emite certidão narrativa de assento homologado. Certidão simples nunca contém filiação de assento
 * marcado como sensível (adotiva), nem averbações reservadas ou pendentes de homologação (LGPD).
 *
 * <p>O teor selado nunca é sobrescrito, mas a certidão não pode repetir como certo um dado que o
 * Pároco já retificou: cada {@code Ressalva} atestada é anexada ao texto ("onde consta X, leia-se Y").
 * Em assento sensível, a ressalva que toca a filiação não é anexada, para não reabrir pela retificação
 * o dado que a certidão simples omite.
 */
@Service
public class EmitirCertidaoUseCase {

    private static final Set<PapelParticipante> FILIACAO =
            EnumSet.of(PapelParticipante.PAI, PapelParticipante.MAE, PapelParticipante.ADOTANTE);

    private static final Pattern FILIACAO_NO_CAMPO =
            Pattern.compile("filia|\\bpai\\b|\\bm[aã]e\\b|adot|genitor", Pattern.CASE_INSENSITIVE);

    private final EventoActorContextResolver actorResolver;
    private final AssentoAcessoService acesso;
    private final SacramentoAuditoriaService auditoria;

    public EmitirCertidaoUseCase(EventoActorContextResolver actorResolver, AssentoAcessoService acesso,
                                 SacramentoAuditoriaService auditoria) {
        this.actorResolver = actorResolver;
        this.acesso = acesso;
        this.auditoria = auditoria;
    }

    @Transactional
    public CertidaoResponse execute(UUID id) {
        EventoActorContext ator = actorResolver.resolveRequired();
        AssentoSacramental assento = acesso.carregar(id, ator, OperacaoSacramental.EMITIR_CERTIDAO);
        if (assento.getStatus() != StatusAssento.SEALED) {
            throw new AssentoImutavelException("Certidão só pode ser emitida de assento homologado.");
        }
        List<String> averbacoes = assento.getAverbacoes().stream()
                .filter(a -> a.status() == StatusAverbacao.HOMOLOGADA && !a.reservada())
                .map(Averbacao::teorNota).toList();
        String texto = narrativa(assento);
        auditoria.registrar(ator, "emitir-certidao", null, assento);
        return new CertidaoResponse(id, assento.getTipo().name(), assento.getTeor().referenciaLivro().livro(),
                assento.getTeor().referenciaLivro().folha(), assento.getTeor().referenciaLivro().termo(),
                assento.getTeor().paroquiaCustodiante(), assento.getTeor().dataCelebracao(),
                assento.getTeor().localCelebracao(), texto, averbacoes);
    }

    static String narrativa(AssentoSacramental a) {
        boolean ocultarFiliacao = a.getTeor().dadoSensivel();
        String pessoas = a.getTeor().participantes().stream()
                .filter(p -> !(ocultarFiliacao && FILIACAO.contains(p.papel())))
                .map(EmitirCertidaoUseCase::descrever)
                .collect(Collectors.joining("; "));
        return "Certifico que no livro %s, folha %s, termo %s, da %s, consta o assento de %s, celebrado em %s, em %s. "
                .formatted(a.getTeor().referenciaLivro().livro(), a.getTeor().referenciaLivro().folha(),
                        a.getTeor().referenciaLivro().termo(), a.getTeor().paroquiaCustodiante(), a.getTipo(),
                        a.getTeor().dataCelebracao(), a.getTeor().localCelebracao())
                + "Constam: " + pessoas + "." + ressalvas(a);
    }

    private static String ressalvas(AssentoSacramental a) {
        String itens = a.getRessalvas().stream()
                .filter(r -> !(a.getTeor().dadoSensivel() && FILIACAO_NO_CAMPO.matcher(r.campo()).find()))
                .map(EmitirCertidaoUseCase::descrever)
                .collect(Collectors.joining(" "));
        return itens.isEmpty() ? "" : " Ressalvas: " + itens;
    }

    private static String descrever(Ressalva r) {
        return r.valorOriginal() == null || r.valorOriginal().isBlank()
                ? "no campo \"%s\", leia-se \"%s\".".formatted(r.campo(), r.valorCorrigido())
                : "no campo \"%s\", onde consta \"%s\", leia-se \"%s\".".formatted(
                        r.campo(), r.valorOriginal(), r.valorCorrigido());
    }

    private static String descrever(Participante p) {
        return p.papel().name().toLowerCase(Locale.ROOT).replace('_', ' ') + " " + p.nomeHistorico();
    }
}
