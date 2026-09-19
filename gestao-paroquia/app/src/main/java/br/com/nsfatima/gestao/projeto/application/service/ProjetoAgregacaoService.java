package br.com.nsfatima.gestao.projeto.application.service;

import br.com.nsfatima.gestao.calendario.infrastructure.config.CacheConfig;
import br.com.nsfatima.gestao.projeto.api.v1.dto.MapaColaboracaoDTO;
import br.com.nsfatima.gestao.projeto.api.v1.dto.ProjetoResumoDTO;
import br.com.nsfatima.gestao.projeto.api.v1.dto.SaudeTemporalDTO;
import br.com.nsfatima.gestao.projeto.api.v1.dto.StatusExecucaoDTO;
import br.com.nsfatima.gestao.projeto.domain.exception.ProjetoNaoEncontradoException;
import br.com.nsfatima.gestao.projeto.domain.service.ProvedorEventoProjeto;
import br.com.nsfatima.gestao.projeto.domain.service.ProjetoAgregacaoDomainService;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.entity.ProjetoEventoEntity;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.repository.ProjetoEventoJpaRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Serviço de aplicação para agregação de dados de projetos.
 * Refactored to be decoupled from specific event implementations.
 */
@Service
public class ProjetoAgregacaoService {

    private final ProjetoEventoJpaRepository projetoRepository;
    private final ProvedorEventoProjeto provedorEventoProjeto;
    private final ProjetoAgregacaoDomainService domainService;

    public ProjetoAgregacaoService(
            ProjetoEventoJpaRepository projetoRepository,
            ProvedorEventoProjeto provedorEventoProjeto,
            ProjetoAgregacaoDomainService domainService) {
        this.projetoRepository = projetoRepository;
        this.provedorEventoProjeto = provedorEventoProjeto;
        this.domainService = domainService;
    }

    /**
     * Obtém o resumo agregado de um projeto.
     *
     * @param projetoId ID do projeto
     * @return DTO com o resumo agregado
     * @throws ProjetoNaoEncontradoException se o projeto não for encontrado
     */
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.PROJETO_RESUMO_CACHE, key = "#projetoId")
    public ProjetoResumoDTO obterResumo(UUID projetoId) {
        ProjetoEventoEntity projeto = projetoRepository.findById(projetoId)
                .orElseThrow(() -> new ProjetoNaoEncontradoException(projetoId));

        Instant agora = Instant.now();

        // Status de Execução
        int totalEventos = (int) provedorEventoProjeto.contarPorProjetoId(projetoId);
        int eventosRealizados = (int) provedorEventoProjeto.contarPorProjetoIdEFimUtcMenorQue(projetoId, agora);
        int eventosPendentes = (int) provedorEventoProjeto.contarPorProjetoIdEFimUtcMaiorOuIgualA(projetoId, agora);

        StatusExecucaoDTO statusExecucao = new StatusExecucaoDTO(totalEventos, eventosRealizados, eventosPendentes);

        // Mapa de Colaboração
        List<String> envolvidos = provedorEventoProjeto.buscarNomesOrganizacoesEnvolvidas(projetoId);
        MapaColaboracaoDTO mapaColaboracao = new MapaColaboracaoDTO(envolvidos);

        // Saúde Temporal
        BigDecimal percentual = domainService.calcularPercentualTempoDecorrido(
                projeto.getInicioUtc(), projeto.getFimUtc());
        boolean emRisco = domainService.verificarSeEstaEmRisco(projeto.getInicioUtc(),
                projeto.getFimUtc(), eventosPendentes);

        SaudeTemporalDTO saudeTemporal = new SaudeTemporalDTO(percentual, emRisco);

        return new ProjetoResumoDTO(statusExecucao, mapaColaboracao, saudeTemporal);
    }
}
