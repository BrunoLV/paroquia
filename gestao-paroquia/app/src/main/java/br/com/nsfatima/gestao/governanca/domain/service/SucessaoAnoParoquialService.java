package br.com.nsfatima.gestao.governanca.domain.service;

import br.com.nsfatima.gestao.governanca.domain.repository.AnoParoquialRepository;

/**
 * Serviço de domínio (stateless) que guarda a regra de sucessão entre anos paroquiais:
 * um ano só pode ser fechado ou concluído se todos os anos anteriores já estiverem
 * {@code CONCLUIDO}. Depende do repositório, portanto não cabe no agregado.
 */
public class SucessaoAnoParoquialService {

    private final AnoParoquialRepository repository;

    public SucessaoAnoParoquialService(AnoParoquialRepository repository) {
        this.repository = repository;
    }

    public void assegurarAnosAnterioresConcluidos(int ano) {
        if (repository.existeAnoAnteriorNaoConcluido(ano)) {
            throw new IllegalArgumentException(
                    "O ano anterior precisa estar concluído antes que este ano possa ser fechado ou concluído.");
        }
    }
}
