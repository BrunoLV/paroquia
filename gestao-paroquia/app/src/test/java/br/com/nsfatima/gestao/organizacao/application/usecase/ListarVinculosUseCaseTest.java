package br.com.nsfatima.gestao.organizacao.application.usecase;

import br.com.nsfatima.gestao.iam.infrastructure.persistence.entity.UsuarioEntity;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.entity.MembroGrupoEntity;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.repository.MembroGrupoJpaRepository;
import br.com.nsfatima.gestao.organizacao.api.v1.dto.VinculoResponse;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoGrupo;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.GrupoParoquialEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.GrupoParoquialJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ListarVinculosUseCaseTest {

    private MembroGrupoJpaRepository vinculoRepository;
    private UsuarioJpaRepository usuarioRepository;
    private GrupoParoquialJpaRepository grupoParoquialRepository;
    private ListarVinculosUseCase useCase;

    @BeforeEach
    void setUp() {
        vinculoRepository = mock(MembroGrupoJpaRepository.class);
        usuarioRepository = mock(UsuarioJpaRepository.class);
        grupoParoquialRepository = mock(GrupoParoquialJpaRepository.class);
        useCase = new ListarVinculosUseCase(vinculoRepository, usuarioRepository, grupoParoquialRepository);
    }

    @Test
    void deveListarVinculosAtivosDoFielDoUsuario() {
        UUID usuarioId = UUID.randomUUID();
        UUID fielId = UUID.randomUUID();
        UUID grupoParoquialId = UUID.randomUUID();
        UUID igrejaId = UUID.randomUUID();

        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setId(usuarioId);
        usuario.setFielId(fielId);
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));

        MembroGrupoEntity vinculo = new MembroGrupoEntity();
        vinculo.setId(UUID.randomUUID());
        vinculo.setFielId(fielId);
        vinculo.setGrupoParoquialId(grupoParoquialId);
        vinculo.setTipoGrupo(TipoGrupo.PASTORAL);
        vinculo.setPapel("COORD_PASTORAL_LOCAL");
        vinculo.setIgrejaVinculoId(igrejaId);
        vinculo.setAtivo(true);
        when(vinculoRepository.findByFielIdAndAtivoTrue(fielId)).thenReturn(List.of(vinculo));

        GrupoParoquialEntity org = new GrupoParoquialEntity();
        org.setId(grupoParoquialId);
        org.setNome("Pastoral X");
        when(grupoParoquialRepository.findById(grupoParoquialId)).thenReturn(Optional.of(org));

        GrupoParoquialEntity igreja = new GrupoParoquialEntity();
        igreja.setId(igrejaId);
        igreja.setNome("Matriz");
        when(grupoParoquialRepository.findById(igrejaId)).thenReturn(Optional.of(igreja));

        List<VinculoResponse> result = useCase.execute(usuarioId);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().grupoParoquialNome()).isEqualTo("Pastoral X");
        assertThat(result.getFirst().igrejaVinculoNome()).isEqualTo("Matriz");
        assertThat(result.getFirst().papel()).isEqualTo("COORD_PASTORAL_LOCAL");
    }

    @Test
    void deveLancarExcecaoQuandoUsuarioNaoEncontrado() {
        UUID usuarioId = UUID.randomUUID();
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(usuarioId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Usuario nao encontrado");
    }
}
