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
import br.com.nsfatima.gestao.sacramento.domain.model.StatusAssento;
import br.com.nsfatima.gestao.sacramento.domain.model.StatusAverbacao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Emite certidão narrativa de assento homologado. Certidão simples nunca contém filiação de assento
 * marcado como sensível (adotiva), nem averbações reservadas ou pendentes de homologação (LGPD).
 */
@Service
public class EmitirCertidaoUseCase {

    private static final Set<PapelParticipante> FILIACAO =
            EnumSet.of(PapelParticipante.PAI, PapelParticipante.MAE, PapelParticipante.ADOTANTE);

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

    private static String narrativa(AssentoSacramental a) {
        boolean ocultarFiliacao = a.getTeor().dadoSensivel();
        String pessoas = a.getTeor().participantes().stream()
                .filter(p -> !(ocultarFiliacao && FILIACAO.contains(p.papel())))
                .map(EmitirCertidaoUseCase::descrever)
                .collect(Collectors.joining("; "));
        return "Certifico que no livro %s, folha %s, termo %s, da %s, consta o assento de %s, celebrado em %s, em %s. "
                .formatted(a.getTeor().referenciaLivro().livro(), a.getTeor().referenciaLivro().folha(),
                        a.getTeor().referenciaLivro().termo(), a.getTeor().paroquiaCustodiante(), a.getTipo(),
                        a.getTeor().dataCelebracao(), a.getTeor().localCelebracao())
                + "Constam: " + pessoas + ".";
    }

    private static String descrever(Participante p) {
        return p.papel().name().toLowerCase(Locale.ROOT).replace('_', ' ') + " " + p.nomeHistorico();
    }
}
