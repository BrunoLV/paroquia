package br.com.nsfatima.gestao.conselho.application.usecase;

import br.com.nsfatima.gestao.conselho.application.dto.AtaVisao;
import br.com.nsfatima.gestao.conselho.application.dto.AtorAutenticado;
import br.com.nsfatima.gestao.conselho.application.service.AtaAuditoriaService;
import br.com.nsfatima.gestao.conselho.application.service.AtaVisaoMontador;
import br.com.nsfatima.gestao.conselho.application.service.CompatibilidadeOrgaoAta;
import br.com.nsfatima.gestao.conselho.application.service.ConselhoAuthorizationService;
import br.com.nsfatima.gestao.conselho.application.service.ValidadorPresencaAta;
import br.com.nsfatima.gestao.conselho.domain.model.AtaReuniao;
import br.com.nsfatima.gestao.conselho.domain.policy.AcaoAta;
import br.com.nsfatima.gestao.conselho.domain.repository.AtaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Conclui a redação. Congela o total de membros convocados (base do quórum) e leva a ata à homologação do
 * Pároco, ou a registra e sela de imediato quando o tipo de reunião dispensa homologação.
 */
@Service
public class SubmeterAtaUseCase {

    private static final String ACAO = "SUBMETER";

    private final AtaRepository repository;
    private final ConselhoAuthorizationService autorizacao;
    private final ValidadorPresencaAta validadorPresenca;
    private final CompatibilidadeOrgaoAta compatibilidade;
    private final AtaAuditoriaService auditoria;
    private final AtaVisaoMontador montador;

    public SubmeterAtaUseCase(AtaRepository repository, ConselhoAuthorizationService autorizacao,
                              ValidadorPresencaAta validadorPresenca, CompatibilidadeOrgaoAta compatibilidade,
                              AtaAuditoriaService auditoria, AtaVisaoMontador montador) {
        this.repository = repository;
        this.autorizacao = autorizacao;
        this.validadorPresenca = validadorPresenca;
        this.compatibilidade = compatibilidade;
        this.auditoria = auditoria;
        this.montador = montador;
    }

    /**
     * Submete a ata.
     *
     * @param id ata a submeter
     * @return a ata, pendente de homologação ou já registrada
     */
    @Transactional
    public AtaVisao execute(UUID id) {
        AtorAutenticado ator = autorizacao.atorAtual();
        AtaReuniao ata = autorizacao.carregar(id, ator, AcaoAta.SUBMETER);
        compatibilidade.exigir(ata);
        int membros = ata.regras().aplicaQuorum()
                ? validadorPresenca.membros(ata.getOrganizacaoConvocanteId(), ata.getOrganizacaoAssociadaId()).size()
                : 0;

        String antes = auditoria.capturar(ata);
        ata.submeter(membros, repository, Instant.now());
        AtaReuniao salva = repository.salvar(ata);
        auditoria.registrar(ator, ACAO, antes, salva);
        return montador.montar(ator, salva);
    }
}
