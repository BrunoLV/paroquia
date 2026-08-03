package br.com.nsfatima.gestao.patrimonio.application.usecase.observacao;

import br.com.nsfatima.gestao.patrimonio.domain.model.TipoNotaAtivoPatrimonial;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.entity.ObservacaoAtivoPatrimonialEntity;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.repository.ObservacaoAtivoPatrimonialJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class CreateNotaAtivoPatrimonialUseCase {

    private final ObservacaoAtivoPatrimonialJpaRepository repository;

    public CreateNotaAtivoPatrimonialUseCase(ObservacaoAtivoPatrimonialJpaRepository repository) {
        this.repository = repository;
    }

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
