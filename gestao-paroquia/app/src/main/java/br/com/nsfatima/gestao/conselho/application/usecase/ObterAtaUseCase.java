package br.com.nsfatima.gestao.conselho.application.usecase;

import br.com.nsfatima.gestao.conselho.application.dto.AtaVisao;
import br.com.nsfatima.gestao.conselho.application.dto.AtorAutenticado;
import br.com.nsfatima.gestao.conselho.application.service.AtaVisaoMontador;
import br.com.nsfatima.gestao.conselho.application.service.ConselhoAuthorizationService;
import br.com.nsfatima.gestao.conselho.domain.policy.AcaoAta;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/** Consulta uma ata. Quem não pode lê-la recebe "não encontrada". */
@Service
public class ObterAtaUseCase {

    private final ConselhoAuthorizationService autorizacao;
    private final AtaVisaoMontador montador;

    public ObterAtaUseCase(ConselhoAuthorizationService autorizacao, AtaVisaoMontador montador) {
        this.autorizacao = autorizacao;
        this.montador = montador;
    }

    /**
     * Obtém a ata.
     *
     * @param id ata consultada
     * @return a ata, com as ações permitidas ao ator
     */
    @Transactional(readOnly = true)
    public AtaVisao execute(UUID id) {
        AtorAutenticado ator = autorizacao.atorAtual();
        return montador.montar(ator, autorizacao.carregar(id, ator, AcaoAta.LER));
    }
}
