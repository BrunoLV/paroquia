package br.com.nsfatima.gestao.conselho.application.usecase;

import br.com.nsfatima.gestao.conselho.application.dto.AtorAutenticado;
import br.com.nsfatima.gestao.conselho.application.service.ConselhoAuthorizationService;
import br.com.nsfatima.gestao.conselho.application.service.ValidadorPresencaAta;
import br.com.nsfatima.gestao.conselho.application.service.ValidadorPresencaAta.PresencaRedator;
import br.com.nsfatima.gestao.conselho.domain.model.TipoReuniao;
import br.com.nsfatima.gestao.conselho.domain.policy.RegrasTipoReuniaoPolicy;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

/** Informa quem é o redator na lista de presença de uma ata nova, para a interface já incluí-lo com a função certa. */
@Service
public class ObterPresencaRedatorUseCase {

    private final ConselhoAuthorizationService autorizacao;
    private final ValidadorPresencaAta validadorPresenca;

    public ObterPresencaRedatorUseCase(ConselhoAuthorizationService autorizacao,
                                       ValidadorPresencaAta validadorPresenca) {
        this.autorizacao = autorizacao;
        this.validadorPresenca = validadorPresenca;
    }

    /**
     * Presença do usuário autenticado como redator do tipo no órgão.
     *
     * @param tipo tipo de reunião da ata
     * @param organizacaoId órgão convocante
     * @return o fiel e a função do redator, ou vazio se o usuário não redige o tipo nesse órgão
     */
    public Optional<PresencaRedator> execute(TipoReuniao tipo, UUID organizacaoId) {
        AtorAutenticado ator = autorizacao.atorAtual();
        if (!autorizacao.podeRedigir(ator, tipo, organizacaoId)) {
            return Optional.empty();
        }
        return validadorPresenca.presencaDoRedator(ator, RegrasTipoReuniaoPolicy.para(tipo), organizacaoId);
    }
}
