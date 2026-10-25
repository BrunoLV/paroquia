package br.com.nsfatima.gestao.conselho.application.service;

import br.com.nsfatima.gestao.calendario.domain.exception.OperacaoProibidaException;
import br.com.nsfatima.gestao.conselho.application.dto.AtorAutenticado;
import br.com.nsfatima.gestao.conselho.application.port.out.AtorAutenticadoPort;
import br.com.nsfatima.gestao.conselho.domain.exception.AtaImutavelException;
import br.com.nsfatima.gestao.conselho.domain.exception.AtaNaoEncontradaException;
import br.com.nsfatima.gestao.conselho.domain.exception.TransicaoAtaInvalidaException;
import br.com.nsfatima.gestao.conselho.domain.model.AtaReuniao;
import br.com.nsfatima.gestao.conselho.domain.model.AtaReuniaoId;
import br.com.nsfatima.gestao.conselho.domain.model.StatusAta;
import br.com.nsfatima.gestao.conselho.domain.model.TipoReuniao;
import br.com.nsfatima.gestao.conselho.domain.policy.AcaoAta;
import br.com.nsfatima.gestao.conselho.domain.policy.AtorOrganizacao;
import br.com.nsfatima.gestao.conselho.domain.policy.AutorizacaoAtaPolicy;
import br.com.nsfatima.gestao.conselho.domain.policy.RegrasTipoReuniaoPolicy;
import br.com.nsfatima.gestao.conselho.domain.repository.AtaRepository;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Set;
import java.util.UUID;

/**
 * Ponto único de autorização das atas: monta o retrato do ator para a organização da ata e consulta a
 * {@link AutorizacaoAtaPolicy}. Quem não pode nem ler recebe "não encontrada", para que a existência de atas
 * de outros órgãos não vaze; quem lê mas não pode agir recebe "proibido".
 */
@Service
public class ConselhoAuthorizationService {

    private final AtaRepository repository;
    private final AtorAutenticadoPort atorPort;

    public ConselhoAuthorizationService(AtaRepository repository, AtorAutenticadoPort atorPort) {
        this.repository = repository;
        this.atorPort = atorPort;
    }

    /** Usuário autenticado da requisição. */
    public AtorAutenticado atorAtual() {
        return atorPort.atual();
    }

    /** Ações permitidas ao ator sobre a ata, no estado atual dela. */
    public Set<AcaoAta> acoes(AtorAutenticado ator, AtaReuniao ata) {
        return AutorizacaoAtaPolicy.acoesPermitidas(
                atorDa(ator, ata.getOrganizacaoConvocanteId(), ata.getOrganizacaoAssociadaId()),
                ata.regras(), ata.getStatus());
    }

    /**
     * Carrega a ata exigindo uma ação.
     *
     * @throws AtaNaoEncontradaException se a ata não existe ou o ator não pode lê-la
     * @throws OperacaoProibidaException se o ator lê a ata mas não pode executar a ação
     */
    public AtaReuniao carregar(UUID id, AtorAutenticado ator, AcaoAta exigida) {
        AtaReuniao ata = repository.buscarPorId(new AtaReuniaoId(id))
                .orElseThrow(() -> new AtaNaoEncontradaException(id));
        Set<AcaoAta> acoes = acoes(ator, ata);
        if (!acoes.contains(AcaoAta.LER)) {
            throw new AtaNaoEncontradaException(id);
        }
        if (!acoes.contains(exigida)) {
            throw recusa(ator, ata, exigida);
        }
        return ata;
    }

    /**
     * Distingue "o seu papel não permite" (403) de "o seu papel permitiria, mas a ata não está em estado de
     * aceitar" (409, ou imutável se já selada), para que a resposta oriente o usuário.
     */
    private RuntimeException recusa(AtorAutenticado ator, AtaReuniao ata, AcaoAta exigida) {
        AtorOrganizacao retrato = atorDa(ator, ata.getOrganizacaoConvocanteId(), ata.getOrganizacaoAssociadaId());
        boolean permitidaEmOutroEstado = Arrays.stream(StatusAta.values()).anyMatch(
                status -> AutorizacaoAtaPolicy.acoesPermitidas(retrato, ata.regras(), status).contains(exigida));
        if (!permitidaEmOutroEstado) {
            return new OperacaoProibidaException("O seu perfil não permite esta operação sobre a ata.");
        }
        return ata.getStatus().selada()
                ? new AtaImutavelException("Ata selada não pode ser alterada; use a ata retificadora.")
                : new TransicaoAtaInvalidaException("A ata não está em situação de aceitar esta operação. "
                        + "Situação atual: " + ata.getStatus() + ".");
    }

    /**
     * Exige que o ator possa redigir uma ata nova do tipo, na organização convocante.
     *
     * @throws OperacaoProibidaException se o ator não é redator do tipo nessa organização
     */
    public void exigirRedacao(AtorAutenticado ator, TipoReuniao tipo, UUID convocanteId, UUID associadaId) {
        Set<AcaoAta> acoes = AutorizacaoAtaPolicy.acoesPermitidas(atorDa(ator, convocanteId, associadaId),
                RegrasTipoReuniaoPolicy.para(tipo), StatusAta.RASCUNHO);
        if (!acoes.contains(AcaoAta.REDIGIR)) {
            throw new OperacaoProibidaException("O seu perfil não permite redigir ata deste tipo nesta organização.");
        }
    }

    private static AtorOrganizacao atorDa(AtorAutenticado ator, UUID convocanteId, UUID associadaId) {
        return new AtorOrganizacao(ator.papeisGlobais(), ator.papeisNa(convocanteId), ator.papeisNa(associadaId));
    }
}
