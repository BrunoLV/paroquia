package br.com.nsfatima.gestao.conselho.application.usecase;

import br.com.nsfatima.gestao.conselho.application.dto.AtorAutenticado;
import br.com.nsfatima.gestao.conselho.application.service.ConselhoAuthorizationService;
import br.com.nsfatima.gestao.conselho.domain.model.AtaReuniao;
import br.com.nsfatima.gestao.conselho.domain.model.RegistroAuditoriaAta;
import br.com.nsfatima.gestao.conselho.domain.policy.AcaoAta;
import br.com.nsfatima.gestao.conselho.domain.repository.AtaHistoricoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/** Consulta a trilha de auditoria da ata; restrita a Pároco, Bispo diocesano e secretaria paroquial. */
@Service
public class ListarTrilhaAtaUseCase {

    private final ConselhoAuthorizationService autorizacao;
    private final AtaHistoricoRepository historico;

    public ListarTrilhaAtaUseCase(ConselhoAuthorizationService autorizacao, AtaHistoricoRepository historico) {
        this.autorizacao = autorizacao;
        this.historico = historico;
    }

    /**
     * Lista a trilha em ordem cronológica.
     *
     * @param id ata consultada
     * @return as entradas da trilha
     */
    @Transactional(readOnly = true)
    public List<RegistroAuditoriaAta> execute(UUID id) {
        AtorAutenticado ator = autorizacao.atorAtual();
        AtaReuniao ata = autorizacao.carregar(id, ator, AcaoAta.VER_TRILHA);
        return historico.listarPorAta(ata.getId());
    }
}
