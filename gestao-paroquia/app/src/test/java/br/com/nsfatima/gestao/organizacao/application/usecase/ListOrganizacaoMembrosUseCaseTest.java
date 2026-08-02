package br.com.nsfatima.gestao.organizacao.application.usecase;

import br.com.nsfatima.gestao.iam.infrastructure.persistence.entity.UsuarioEntity;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.repository.MembroJpaRepository;
import br.com.nsfatima.gestao.organizacao.api.v1.dto.OrganizacaoMembroResponse;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.MembroOrganizacaoEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.MembroOrganizacaoJpaRepository;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.OrganizacaoJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.hamcrest.Matchers.hasSize;

@ExtendWith(MockitoExtension.class)
class ListOrganizacaoMembrosUseCaseTest {

    @Mock
    private MembroOrganizacaoJpaRepository vinculoRepository;
    @Mock
    private UsuarioJpaRepository usuarioRepository;
    @Mock
    private MembroJpaRepository membroRepository;
    @Mock
    private OrganizacaoJpaRepository organizacaoRepository;

    private ListOrganizacaoMembrosUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ListOrganizacaoMembrosUseCase(
                vinculoRepository,
                usuarioRepository,
                membroRepository,
                organizacaoRepository
        );
    }

    @Test
    @DisplayName("Deve listar membros de uma organização resolvendo nomes de usuários e membros leigos")
    void deveListarOrganizacaoMembrosResolvingUsuarioEMembroNomes() {
        UUID orgId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        MembroOrganizacaoEntity memberEntity = new MembroOrganizacaoEntity();
        memberEntity.setId(UUID.randomUUID());
        memberEntity.setOrganizacaoId(orgId);
        memberEntity.setUsuarioId(userId);
        memberEntity.setTipoOrganizacao("PASTORAL");
        memberEntity.setPapel("COORDENADOR");
        memberEntity.setAtivo(true);

        UsuarioEntity userEntity = new UsuarioEntity();
        userEntity.setId(userId);
        userEntity.setNome("João da Silva");

        when(vinculoRepository.findByOrganizacaoIdAndAtivoTrue(orgId)).thenReturn(List.of(memberEntity));
        when(vinculoRepository.findByIgrejaVinculoIdAndAtivoTrue(orgId)).thenReturn(Collections.emptyList());
        when(usuarioRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(organizacaoRepository.findById(orgId)).thenReturn(Optional.empty());

        List<OrganizacaoMembroResponse> response = useCase.execute(orgId);

        assertThat(response).hasSize(1);
        assertThat(response.getFirst().usuarioNome()).isEqualTo("João da Silva");
        assertThat(response.getFirst().papel()).isEqualTo("COORDENADOR");
    }
}
