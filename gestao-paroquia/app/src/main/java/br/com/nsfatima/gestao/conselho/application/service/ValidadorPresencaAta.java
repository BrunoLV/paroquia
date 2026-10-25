package br.com.nsfatima.gestao.conselho.application.service;

import br.com.nsfatima.gestao.conselho.application.port.out.PessoasPort;
import br.com.nsfatima.gestao.conselho.domain.exception.AtaInvalidaException;
import br.com.nsfatima.gestao.conselho.domain.model.ConteudoAta;
import br.com.nsfatima.gestao.conselho.domain.model.ModoPresenca;
import br.com.nsfatima.gestao.conselho.domain.model.Presenca;
import br.com.nsfatima.gestao.conselho.domain.policy.RegrasTipoReuniao;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Confere a lista nominal de presença contra o cadastro, o que o agregado não pode fazer por conhecer
 * pessoas só por identificador: nos colegiados e organismos todos devem ser membros ativos do órgão (ou do
 * órgão associado); nas demais reuniões nominais, apenas devem existir no cadastro de fiéis.
 */
@Service
public class ValidadorPresencaAta {

    private final PessoasPort pessoas;

    public ValidadorPresencaAta(PessoasPort pessoas) {
        this.pessoas = pessoas;
    }

    /**
     * Valida a presença.
     *
     * @throws AtaInvalidaException se alguém na lista não é membro, ou não existe no cadastro
     */
    public void validar(RegrasTipoReuniao regras, ConteudoAta conteudo, UUID convocanteId, UUID associadaId) {
        List<Presenca> nominal = conteudo.presenca().nominal();
        if (nominal.isEmpty()) {
            return;
        }
        Set<UUID> ids = new HashSet<>();
        nominal.forEach(p -> ids.add(p.fielId()));
        if (regras.modoPresenca() == ModoPresenca.NOMINAL_MEMBROS) {
            if (!membros(convocanteId, associadaId).containsAll(ids)) {
                throw new AtaInvalidaException("A presença deve conter apenas membros ativos do órgão.");
            }
        } else if (!pessoas.nomes(ids).keySet().containsAll(ids)) {
            throw new AtaInvalidaException("A presença cita pessoa não cadastrada.");
        }
    }

    /** Membros ativos do órgão convocante e, se houver, do associado; base do quórum. */
    public Set<UUID> membros(UUID convocanteId, UUID associadaId) {
        Set<UUID> membros = new HashSet<>(pessoas.membrosAtivos(convocanteId));
        if (associadaId != null) {
            membros.addAll(pessoas.membrosAtivos(associadaId));
        }
        return membros;
    }
}
