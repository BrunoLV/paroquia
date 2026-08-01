package br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoAlocacaoStatus;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.entity.AtivoAlocacaoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AtivoAlocacaoJpaRepository extends JpaRepository<AtivoAlocacaoEntity, UUID> {

    List<AtivoAlocacaoEntity> findByAtivoIdAndStatusIn(UUID ativoId, List<AtivoAlocacaoStatus> status);

    List<AtivoAlocacaoEntity> findByAtivoIdOrderByDataInicioDesc(UUID ativoId);

    List<AtivoAlocacaoEntity> findByEventoIdOrderByDataInicioDesc(UUID eventoId);

    Optional<AtivoAlocacaoEntity> findByAprovacaoId(UUID aprovacaoId);

    List<AtivoAlocacaoEntity> findByStatusAndDataInicioLessThanEqual(AtivoAlocacaoStatus status, LocalDate data);

    List<AtivoAlocacaoEntity> findByStatusAndDataFimLessThanEqualAndAlertaAtrasoEmitidoEmIsNull(
            AtivoAlocacaoStatus status, LocalDate data);
}
