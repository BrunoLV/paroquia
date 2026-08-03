package br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.SolenidadeLiturgicaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositório de persistência de {@link SolenidadeLiturgicaEntity}, responsável pelo catálogo de
 * solenidades litúrgicas (datas fixas ou móveis do calendário litúrgico, ex.: Natal, Páscoa)
 * usadas para enriquecer/validar o calendário de eventos da paróquia.
 */
@Repository
public interface SolenidadeLiturgicaJpaRepository extends JpaRepository<SolenidadeLiturgicaEntity, UUID> {

    /**
     * Lista todas as solenidades litúrgicas atualmente ativas, ou seja, aquelas que devem ser
     * consideradas no calendário vigente (excluindo solenidades desativadas/descontinuadas).
     *
     * @return solenidades litúrgicas com o indicador de ativo marcado como verdadeiro
     */
    List<SolenidadeLiturgicaEntity> findByAtivoTrue();

    /**
     * Busca uma solenidade litúrgica pela sua chave única de identificação de negócio (não o id
     * técnico), usada para resolver referências estáveis a solenidades específicas em regras de
     * cálculo do calendário.
     *
     * @param chave chave de identificação da solenidade litúrgica
     * @return a solenidade litúrgica correspondente, ou {@link Optional#empty()} caso não exista
     *     nenhuma solenidade cadastrada com a chave informada
     */
    Optional<SolenidadeLiturgicaEntity> findByChave(String chave);
}
