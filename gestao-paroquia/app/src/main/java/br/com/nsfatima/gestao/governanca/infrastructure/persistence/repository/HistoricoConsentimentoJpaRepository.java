package br.com.nsfatima.gestao.governanca.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.governanca.domain.model.TipoTitularConsentimento;
import br.com.nsfatima.gestao.governanca.infrastructure.persistence.entity.HistoricoConsentimentoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/** Repositório JPA para trilha imutável de auditoria de consentimento LGPD. */
@Repository
public interface HistoricoConsentimentoJpaRepository extends JpaRepository<HistoricoConsentimentoEntity, UUID> {

    List<HistoricoConsentimentoEntity> findByTitularTipoAndTitularIdOrderByCriadoEmUtcDesc(
            TipoTitularConsentimento titularTipo, UUID titularId);

    /**
     * Anonimiza os dados pessoais da trilha de um titular eliminado (IP, user-agent e dados do responsável
     * legal), preservando o evento de consentimento como prova. É UPDATE nativo porque a trilha é
     * append-only no banco (V042) e o JPA não deve re-salvar o registro; o gatilho aceita somente esta
     * forma de alteração.
     *
     * @param titularTipo nome de {@link TipoTitularConsentimento}
     * @param titularId identificador do titular
     * @return quantidade de eventos anonimizados
     */
    @Modifying
    @Query(value = "UPDATE calendario.historico_consentimentos SET ip_origem = NULL, user_agent = NULL, "
            + "responsavel_menor_nome = NULL, responsavel_menor_cpf = NULL "
            + "WHERE titular_tipo = :titularTipo AND titular_id = :titularId", nativeQuery = true)
    int anonimizarDadosPessoais(@Param("titularTipo") String titularTipo, @Param("titularId") UUID titularId);
}
