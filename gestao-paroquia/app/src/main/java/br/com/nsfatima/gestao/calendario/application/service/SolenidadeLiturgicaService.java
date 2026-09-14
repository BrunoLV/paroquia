package br.com.nsfatima.gestao.calendario.application.service;

import br.com.nsfatima.gestao.calendario.api.v1.dto.SolenidadeLiturgicaRequestDTO;
import br.com.nsfatima.gestao.calendario.api.v1.dto.SolenidadeLiturgicaResponseDTO;
import br.com.nsfatima.gestao.calendario.domain.exception.ForbiddenOperationException;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.SolenidadeLiturgicaEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.SolenidadeLiturgicaJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Serviço de aplicação/domínio que gerencia o cadastro de solenidades litúrgicas —
 * os marcos-base (fixos ou móveis, ver {@code TipoSolenidade}) usados por
 * {@code MotorLiturgico} para calcular o ano litúrgico. A gestão do cadastro
 * (criar, atualizar, desativar) é restrita a Pároco ou Vigário; a exclusão é lógica
 * (desativação), preservando o histórico de solenidades já usadas em cálculos
 * anteriores.
 */
@Service
public class SolenidadeLiturgicaService {

    private final SolenidadeLiturgicaJpaRepository repository;

    public SolenidadeLiturgicaService(SolenidadeLiturgicaJpaRepository repository) {
        this.repository = repository;
    }

    /**
     * Lista todas as solenidades litúrgicas cadastradas, ativas ou não.
     *
     * @return lista de todas as solenidades cadastradas
     */
    @Transactional(readOnly = true)
    public List<SolenidadeLiturgicaResponseDTO> findAll() {
        return repository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Lista apenas as solenidades litúrgicas atualmente ativas, isto é, elegíveis
     * para uso no cálculo do ano litúrgico.
     *
     * @return lista de solenidades ativas
     */
    @Transactional(readOnly = true)
    public List<SolenidadeLiturgicaResponseDTO> findAtivas() {
        return repository.findByAtivoTrue().stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Cria uma nova solenidade litúrgica no cadastro, aplicando valores padrão para
     * campos opcionais não informados (cor branca, fator de demanda 1.0, nível de
     * prioridade 2, ativa).
     *
     * @param req dados da solenidade a ser criada
     * @param userRole papel do usuário solicitante, usado para validar autorização
     * @return solenidade criada
     * @throws ForbiddenOperationException se o usuário não for Pároco nem Vigário
     */
    @Transactional
    public SolenidadeLiturgicaResponseDTO create(SolenidadeLiturgicaRequestDTO req, String userRole) {
        validarPapel(userRole);

        SolenidadeLiturgicaEntity entity = new SolenidadeLiturgicaEntity();
        entity.setId(UUID.randomUUID());
        entity.setChave(req.chave());
        entity.setNome(req.nome());
        entity.setTipoSolenidade(req.tipoSolenidade());
        entity.setMesFixo(req.mesFixo());
        entity.setDiaFixo(req.diaFixo());
        entity.setOffsetDiasPascoa(req.offsetDiasPascoa());
        entity.setCorLiturgica(req.corLiturgica() != null ? req.corLiturgica().toUpperCase() : "BRANCO");
        entity.setFatorDemanda(req.fatorDemanda() != null ? req.fatorDemanda() : 1.0);
        entity.setNivelPrioridade(req.nivelPrioridade() != null ? req.nivelPrioridade() : 2);
        entity.setDescricao(req.descricao());
        entity.setAtivo(req.ativo() != null ? req.ativo() : true);

        SolenidadeLiturgicaEntity saved = repository.save(entity);
        return toResponse(saved);
    }

    /**
     * Atualiza os campos informados (não nulos) de uma solenidade litúrgica
     * existente — campos ausentes na requisição preservam o valor atual.
     *
     * @param id identificador da solenidade a ser atualizada
     * @param req dados a serem atualizados; campos {@code null} são ignorados
     * @param userRole papel do usuário solicitante, usado para validar autorização
     * @return solenidade atualizada
     * @throws ForbiddenOperationException se o usuário não for Pároco nem Vigário
     * @throws IllegalArgumentException se nenhuma solenidade existir com o identificador informado
     */
    @Transactional
    public SolenidadeLiturgicaResponseDTO update(UUID id, SolenidadeLiturgicaRequestDTO req, String userRole) {
        validarPapel(userRole);

        SolenidadeLiturgicaEntity entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Solenidade litúrgica não encontrada: " + id));

        if (req.chave() != null) {
            entity.setChave(req.chave());
        }
        if (req.nome() != null) {
            entity.setNome(req.nome());
        }
        if (req.tipoSolenidade() != null) {
            entity.setTipoSolenidade(req.tipoSolenidade());
        }
        if (req.mesFixo() != null) {
            entity.setMesFixo(req.mesFixo());
        }
        if (req.diaFixo() != null) {
            entity.setDiaFixo(req.diaFixo());
        }
        if (req.offsetDiasPascoa() != null) {
            entity.setOffsetDiasPascoa(req.offsetDiasPascoa());
        }
        if (req.corLiturgica() != null) {
            entity.setCorLiturgica(req.corLiturgica().toUpperCase());
        }
        if (req.fatorDemanda() != null) {
            entity.setFatorDemanda(req.fatorDemanda());
        }
        if (req.nivelPrioridade() != null) {
            entity.setNivelPrioridade(req.nivelPrioridade());
        }
        if (req.descricao() != null) {
            entity.setDescricao(req.descricao());
        }
        if (req.ativo() != null) {
            entity.setAtivo(req.ativo());
        }

        SolenidadeLiturgicaEntity saved = repository.save(entity);
        return toResponse(saved);
    }

    /**
     * Desativa (exclusão lógica) uma solenidade litúrgica, removendo-a das
     * solenidades ativas sem apagar o registro histórico.
     *
     * @param id identificador da solenidade a ser desativada
     * @param userRole papel do usuário solicitante, usado para validar autorização
     * @throws ForbiddenOperationException se o usuário não for Pároco nem Vigário
     * @throws IllegalArgumentException se nenhuma solenidade existir com o identificador informado
     */
    @Transactional
    public void delete(UUID id, String userRole) {
        validarPapel(userRole);

        SolenidadeLiturgicaEntity entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Solenidade litúrgica não encontrada: " + id));
        entity.setAtivo(false);
        repository.save(entity);
    }

    private void validarPapel(String userRole) {
        String roleUpper = userRole != null ? userRole.toUpperCase().replace("ROLE_", "") : "";
        if (!"PAROCO".equals(roleUpper) && !"VIGARIO".equals(roleUpper)) {
            throw new ForbiddenOperationException(
                    "Apenas Pároco ou Vigário podem gerenciar o cadastro de solenidades litúrgicas.");
        }
    }

    private SolenidadeLiturgicaResponseDTO toResponse(SolenidadeLiturgicaEntity e) {
        return new SolenidadeLiturgicaResponseDTO(
                e.getId(),
                e.getChave(),
                e.getNome(),
                e.getTipoSolenidade(),
                e.getMesFixo(),
                e.getDiaFixo(),
                e.getOffsetDiasPascoa(),
                e.getCorLiturgica(),
                e.getFatorDemanda(),
                e.getNivelPrioridade(),
                e.getDescricao(),
                e.isAtivo()
        );
    }
}
