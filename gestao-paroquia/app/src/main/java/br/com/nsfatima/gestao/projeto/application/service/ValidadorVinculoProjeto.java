package br.com.nsfatima.gestao.projeto.application.service;

import br.com.nsfatima.gestao.calendario.domain.type.RegraRecorrencia;
import br.com.nsfatima.gestao.projeto.domain.policy.ProjetoVincularPolicy;
import br.com.nsfatima.gestao.projeto.domain.policy.ProjetoVinculo;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.entity.ProjetoEventoEntity;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.repository.ProjetoEventoJpaRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/** Carrega o projeto na aplicação antes de delegar a decisão à Policy pura de vínculo. */
@Service
public class ValidadorVinculoProjeto {

    private final ProjetoEventoJpaRepository projetoRepository;
    private final ProjetoVincularPolicy policy;

    public ValidadorVinculoProjeto(ProjetoEventoJpaRepository projetoRepository, ProjetoVincularPolicy policy) {
        this.projetoRepository = projetoRepository;
        this.policy = policy;
    }

    public void validarEvento(UUID projetoId, Instant inicio, Instant fim) {
        if (projetoId != null) {
            policy.validarEvento(carregar(projetoId), inicio, fim);
        }
    }

    public void validarRecorrencia(UUID projetoId, Instant inicio, Instant fim, RegraRecorrencia regra) {
        if (projetoId != null) {
            policy.validarRecorrencia(carregar(projetoId), inicio, fim, regra);
        }
    }

    private ProjetoVinculo carregar(UUID projetoId) {
        ProjetoEventoEntity projeto = projetoRepository.findById(projetoId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + projetoId));
        return new ProjetoVinculo(projeto.getNome(), projeto.getStatus(), projeto.getInicioUtc(), projeto.getFimUtc());
    }
}
