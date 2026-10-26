package br.com.nsfatima.gestao.conselho.application.service;

import br.com.nsfatima.gestao.conselho.application.dto.AtorAutenticado;
import br.com.nsfatima.gestao.conselho.application.dto.ClerigoAtivo;
import br.com.nsfatima.gestao.conselho.application.port.out.PessoasPort;
import br.com.nsfatima.gestao.conselho.domain.exception.AtaInvalidaException;
import br.com.nsfatima.gestao.conselho.domain.model.ConteudoAta;
import br.com.nsfatima.gestao.conselho.domain.model.FuncaoPresenca;
import br.com.nsfatima.gestao.conselho.domain.model.ModoPresenca;
import br.com.nsfatima.gestao.conselho.domain.model.Presenca;
import br.com.nsfatima.gestao.conselho.domain.policy.RegrasTipoReuniao;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Confere a lista nominal de presença contra o cadastro, o que o agregado não pode fazer por conhecer
 * pessoas só por identificador: nos colegiados e organismos todos devem ser membros ativos do órgão (ou do
 * órgão associado); nas demais reuniões nominais, apenas devem ser fiéis ativos do cadastro.
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
        Set<UUID> clerigos = new HashSet<>();
        Set<UUID> ids = new HashSet<>();
        nominal.forEach(p -> (p.clerigo() ? clerigos : ids).add(p.fielId()));
        if (!clerigos.isEmpty() && !pessoas.clerigosAtivos().stream().map(ClerigoAtivo::id)
                .collect(Collectors.toSet()).containsAll(clerigos)) {
            throw new AtaInvalidaException("A presença cita clérigo não cadastrado ou inativo.");
        }
        if (ids.isEmpty()) {
            return;
        }
        if (regras.modoPresenca() == ModoPresenca.NOMINAL_MEMBROS) {
            if (!membros(convocanteId, associadaId).containsAll(ids)) {
                throw new AtaInvalidaException("A presença deve conter apenas membros ativos do órgão.");
            }
        } else if (!pessoas.fieisAtivos(ids).containsAll(ids)) {
            throw new AtaInvalidaException("A presença cita pessoa não cadastrada ou inativa.");
        }
    }

    /**
     * Exige que o redator conste na lista nominal como presente, com a função que o seu papel lhe dá
     * (Secretário(a) ou Coordenador(a)). Sem lista nominal, ou sem cadastro de fiel para o redator, nada a conferir.
     *
     * @throws AtaInvalidaException se o redator não consta, está ausente ou tem outra função
     */
    public void exigirRedatorPresente(AtorAutenticado ator, RegrasTipoReuniao regras, ConteudoAta conteudo,
                                      UUID convocanteId) {
        Optional<FuncaoPresenca> esperada = funcaoDoRedator(ator, regras, convocanteId);
        Optional<UUID> fiel = pessoas.fielDoUsuario(ator.usuarioId());
        if (conteudo.presenca().nominal().isEmpty() || esperada.isEmpty() || fiel.isEmpty()) {
            return;
        }
        boolean consta = conteudo.presenca().nominal().stream().anyMatch(p -> !p.clerigo()
                && p.fielId().equals(fiel.get()) && p.presente() && esperada.get().getRotulo().equals(p.funcao()));
        if (!consta) {
            throw new AtaInvalidaException("O redator da ata deve constar na presença como presente, com a função "
                    + esperada.get().getRotulo() + ".");
        }
    }

    /**
     * Presença do redator, para a interface já a incluir na lista.
     *
     * @return o fiel e a função do redator, ou vazio se ele não tem cadastro de fiel ou papel redator no órgão
     */
    public Optional<PresencaRedator> presencaDoRedator(AtorAutenticado ator, RegrasTipoReuniao regras,
                                                       UUID convocanteId) {
        return funcaoDoRedator(ator, regras, convocanteId).flatMap(funcao -> pessoas.fielDoUsuario(ator.usuarioId())
                .map(fiel -> new PresencaRedator(fiel, funcao.getRotulo())));
    }

    /** Fiel do redator e a função com que deve constar na presença. */
    public record PresencaRedator(UUID fielId, String funcao) {
    }

    private static Optional<FuncaoPresenca> funcaoDoRedator(AtorAutenticado ator, RegrasTipoReuniao regras,
                                                            UUID convocanteId) {
        return FuncaoPresenca.doRedator(ator.papeisNa(convocanteId), regras.papeisRedatores());
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
