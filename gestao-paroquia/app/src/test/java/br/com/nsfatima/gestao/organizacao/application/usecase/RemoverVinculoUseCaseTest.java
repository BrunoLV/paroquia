package br.com.nsfatima.gestao.organizacao.application.usecase;

import br.com.nsfatima.gestao.membro.infrastructure.persistence.entity.MembroGrupoEntity;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.repository.MembroGrupoJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RemoverVinculoUseCaseTest {

    private MembroGrupoJpaRepository vinculoRepository;
    private RemoverVinculoUseCase useCase;

    @BeforeEach
    void setUp() {
        vinculoRepository = mock(MembroGrupoJpaRepository.class);
        useCase = new RemoverVinculoUseCase(vinculoRepository);
    }

    @Test
    void deveDesativarVinculoExistente() {
        UUID vinculoId = UUID.randomUUID();
        MembroGrupoEntity entity = new MembroGrupoEntity();
        entity.setId(vinculoId);
        entity.setAtivo(true);
        when(vinculoRepository.findById(vinculoId)).thenReturn(Optional.of(entity));

        useCase.execute(vinculoId);

        assertThat(entity.isAtivo()).isFalse();
        verify(vinculoRepository).save(entity);
    }

    @Test
    void deveLancarExcecaoQuandoVinculoNaoEncontrado() {
        UUID vinculoId = UUID.randomUUID();
        when(vinculoRepository.findById(vinculoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(vinculoId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Membro nao encontrado");
    }
}
