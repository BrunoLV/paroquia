package br.com.nsfatima.gestao.patrimonio.application.usecase.observacao;

import br.com.nsfatima.gestao.patrimonio.domain.model.TipoNotaAtivoPatrimonial;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.entity.ObservacaoAtivoPatrimonialEntity;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.repository.ObservacaoAtivoPatrimonialJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Registra uma nota de observação no histórico de um ativo patrimonial (ex.: justificativa de
 * alocação, motivo de devolução), servindo como trilha obrigatória de uso do ativo — não é uma
 * anotação livre, mas parte do rastro exigido por outros casos de uso (alocação, desalocação).
 */
@Service
public class CriarNotaAtivoPatrimonialUseCase {

    private final ObservacaoAtivoPatrimonialJpaRepository repository;

    public CriarNotaAtivoPatrimonialUseCase(ObservacaoAtivoPatrimonialJpaRepository repository) {
        this.repository = repository;
    }

    /**
     * Cria e persiste a nota de observação vinculada ao ativo.
     *
     * @param ativoPatrimonialId ativo referenciado pela nota
     * @param usuarioId          usuário responsável pelo registro
     * @param tipo               tipo da nota (ex.: alocação, devolução)
     * @param conteudo           texto/justificativa da nota
     * @return a nota persistida
     */
    @Transactional
    public ObservacaoAtivoPatrimonialEntity execute(UUID ativoPatrimonialId, UUID usuarioId,
                                                      TipoNotaAtivoPatrimonial tipo, String conteudo) {
        ObservacaoAtivoPatrimonialEntity entity = new ObservacaoAtivoPatrimonialEntity();
        entity.setId(UUID.randomUUID());
        entity.setAtivoPatrimonialId(ativoPatrimonialId);
        entity.setUsuarioId(usuarioId);
        entity.setTipo(tipo.name());
        entity.setConteudo(conteudo);
        entity.setCriadoEmUtc(Instant.now());
        return repository.save(entity);
    }
}
