package br.com.nsfatima.gestao.projeto.application.usecase;

import br.com.nsfatima.gestao.projeto.api.v1.dto.MapaColaboracaoDTO;
import br.com.nsfatima.gestao.projeto.api.v1.dto.ProjetoResumoDTO;
import br.com.nsfatima.gestao.projeto.api.v1.dto.SaudeTemporalDTO;
import br.com.nsfatima.gestao.projeto.api.v1.dto.StatusExecucaoDTO;
import br.com.nsfatima.gestao.projeto.domain.exception.ProjetoNotFoundException;
import br.com.nsfatima.gestao.projeto.domain.service.ProjetoAgregacaoDomainService;
import br.com.nsfatima.gestao.projeto.domain.service.ProjectEventProvider;
import br.com.nsfatima.gestao.calendario.infrastructure.config.CacheConfig;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.entity.ProjetoEventoEntity;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.repository.ProjetoEventoJpaRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Serviço de aplicação para agregação de dados de projetos.
 * Refactored to be decoupled from specific event implementations.
 */
@Service
public class ProjetoAgregacaoService {

    private final ProjetoEventoJpaRepository projetoRepository;
    private final ProjectEventProvider eventProvider;
    private final ProjetoAgregacaoDomainService domainService;

    public ProjetoAgregacaoService(
            ProjetoEventoJpaRepository projetoRepository,
            ProjectEventProvider eventProvider,
            ProjetoAgregacaoDomainService domainService) {
        this.projetoRepository = projetoRepository;
        this.eventProvider = eventProvider;
        this.domainService = domainService;
    }

    /**
     * Obtém o resumo agregado de um projeto.
     *
     * @param projetoId ID do projeto
     * @return DTO com o resumo agregado
     * @throws ProjetoNotFoundException se o projeto não for encontrado
     */
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.PROJECT_RESUMO_CACHE, key = "#projetoId")
    public ProjetoResumoDTO obterResumo(UUID projetoId) {
        ProjetoEventoEntity projeto = projetoRepository.findById(projetoId)
                .orElseThrow(() -> new ProjetoNotFoundException(projetoId));

        Instant agora = Instant.now();

        // Status de Execução
        int totalEventos = (int) eventProvider.countByProjetoId(projetoId);
        int eventosRealizados = (int) eventProvider.countByProjetoIdAndFimUtcLessThan(projetoId, agora);
        int eventosPendentes = (int) eventProvider.countByProjetoIdAndFimUtcGreaterThanEqual(projetoId, agora);

        StatusExecucaoDTO statusExecucao = new StatusExecucaoDTO(totalEventos, eventosRealizados, eventosPendentes);

        // Mapa de Colaboração
        List<String> envolvidos = eventProvider.findInvolvedOrganizationNames(projetoId);
        MapaColaboracaoDTO mapaColaboracao = new MapaColaboracaoDTO(envolvidos);

        // Saúde Temporal
        double percentual = domainService.calcularPercentualTempoDecorrido(projeto.getInicioUtc(), projeto.getFimUtc());
        boolean emRisco = domainService.verificarSeEstaEmRisco(projeto.getInicioUtc(), projeto.getFimUtc(), eventosPendentes);
        SaudeTemporalDTO saudeTemporal = new SaudeTemporalDTO(percentual, emRisco);

        return new ProjetoResumoDTO(statusExecucao, mapaColaboracao, saudeTemporal);
    }
}
