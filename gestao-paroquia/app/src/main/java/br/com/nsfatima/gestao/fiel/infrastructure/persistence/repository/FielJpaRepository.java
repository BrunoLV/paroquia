package br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.fiel.infrastructure.persistence.entity.FielEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Repositório JPA de {@link FielEntity}.
 */
@Repository
public interface FielJpaRepository extends JpaRepository<FielEntity, UUID> {

    /**
     * Conta fiéis com cadastro ativo, base do indicador "total de fiéis ativos" do dashboard.
     *
     * @return quantidade de fiéis com cadastro ativo
     */
    long countByAtivoTrue();

    /**
     * Conta fiéis ativos cadastrados a partir do instante informado, usado pelo indicador de
     * novos registros no mês corrente. {@code criadoEmUtc} não é criptografado, então a contagem
     * é resolvida inteiramente no banco (nenhum dado pessoal trafega).
     *
     * @param inicio limite inferior (inclusive) do instante de cadastro
     * @return quantidade de fiéis ativos cadastrados desde {@code inicio}
     */
    long countByAtivoTrueAndCriadoEmUtcGreaterThanEqual(Instant inicio);

    /**
     * Busca todos os fiéis ativos para varreduras em memória que dependem de campos
     * criptografados (ex.: aniversariantes por dia/mês de {@code dataNascimento}).
     *
     * @return fiéis com cadastro ativo
     */
    List<FielEntity> findByAtivoTrue();
}
