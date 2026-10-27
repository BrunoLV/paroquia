package br.com.nsfatima.gestao.membro.application.usecase;

import br.com.nsfatima.gestao.fiel.infrastructure.persistence.entity.FielEntity;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository.FielJpaRepository;
import br.com.nsfatima.gestao.iam.application.usecase.CriarUsuarioUseCase;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.entity.UsuarioEntity;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import br.com.nsfatima.gestao.iam.infrastructure.security.UsuarioDetails;
import br.com.nsfatima.gestao.membro.api.v1.dto.PromoverACoordenadorRequest;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.entity.MembroGrupoEntity;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.mapper.MembroGrupoMapper;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.repository.MembroGrupoJpaRepository;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.OrganizacaoEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.OrganizacaoJpaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PromoverACoordenadorUseCaseTest {

    private MembroGrupoJpaRepository membroRepository;
    private FielJpaRepository fielRepository;
    private OrganizacaoJpaRepository organizacaoRepository;
    private UsuarioJpaRepository usuarioRepository;
    private CriarUsuarioUseCase createUsuarioUseCase;
    private PromoverACoordenadorUseCase useCase;

    private UUID fielId;
    private UUID organizacaoId;
    private UUID actorId;

    @BeforeEach
    void setUp() {
        membroRepository = mock(MembroGrupoJpaRepository.class);
        fielRepository = mock(FielJpaRepository.class);
        organizacaoRepository = mock(OrganizacaoJpaRepository.class);
        usuarioRepository = mock(UsuarioJpaRepository.class);
        createUsuarioUseCase = mock(CriarUsuarioUseCase.class);
        useCase = new PromoverACoordenadorUseCase(membroRepository, fielRepository, organizacaoRepository,
                usuarioRepository, createUsuarioUseCase, new MembroGrupoMapper());

        fielId = UUID.randomUUID();
        organizacaoId = UUID.randomUUID();
        actorId = UUID.randomUUID();

        FielEntity fiel = new FielEntity();
        fiel.setId(fielId);
        fiel.setNome("Maria Coordenadora");
        when(fielRepository.findById(fielId)).thenReturn(Optional.of(fiel));

        OrganizacaoEntity org = new OrganizacaoEntity();
        org.setId(organizacaoId);
        org.setTipo(TipoOrganizacao.PASTORAL);
        when(organizacaoRepository.findById(organizacaoId)).thenReturn(Optional.of(org));

        MembroGrupoEntity membro = new MembroGrupoEntity();
        membro.setId(UUID.randomUUID());
        membro.setFielId(fielId);
        membro.setOrganizacaoId(organizacaoId);
        membro.setTipoOrganizacao(TipoOrganizacao.PASTORAL);
        membro.setPapel("VOLUNTARIO");
        membro.setAtivo(true);
        membro.setFormacaoConcluidaEm(LocalDate.of(2025, 1, 1));
        membro.setVivenciaSacramentalAtestadaEm(LocalDate.of(2025, 1, 1));
        membro.setTermoProtecaoMenoresEm(LocalDate.of(2025, 1, 1));
        membro.setEnviadoEm(LocalDate.of(2025, 1, 1));
        when(membroRepository.findByFielIdAndOrganizacaoIdAndAtivoTrue(fielId, organizacaoId))
                .thenReturn(Optional.of(membro));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateWith(String authority) {
        var principal = new UsuarioDetails(actorId, "actor", "pass", true, "", List.of());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null,
                        List.of(new SimpleGrantedAuthority(authority))));
    }

    private void authenticateAsParoco() {
        var principal = new UsuarioDetails(actorId, "actor", "pass", true, "", List.of());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null,
                        List.of(new SimpleGrantedAuthority("ROLE_CLERO_PAROCO"))));
    }

    private PromoverACoordenadorRequest request() {
        return new PromoverACoordenadorRequest(fielId, organizacaoId, "COORDENADOR", "maria.coord",
                "senhaForte123");
    }

    @Test
    void deveCriarUsuarioQuandoFielNaoTemUsuarioPrevio() {
        authenticateAsParoco();
        when(usuarioRepository.findByFielId(fielId)).thenReturn(Optional.empty());
        UUID novoUsuarioId = UUID.randomUUID();
        when(createUsuarioUseCase.execute("maria.coord", "senhaForte123", fielId)).thenReturn(novoUsuarioId);

        var response = useCase.execute(request());

        assertThat(response.usuarioId()).isEqualTo(novoUsuarioId);
        assertThat(response.usuarioCriado()).isTrue();
    }

    @Test
    void devePermitirQuandoAtorEhParoco() {
        authenticateAsParoco();
        var usuarioExistente = new UsuarioEntity();
        when(usuarioRepository.findByFielId(fielId)).thenReturn(Optional.of(usuarioExistente));

        var response = useCase.execute(request());

        assertThat(response.usuarioCriado()).isFalse();
    }

    @Test
    void deveReutilizarUsuarioExistenteSemDuplicar() {
        authenticateAsParoco();
        UUID usuarioExistenteId = UUID.randomUUID();
        var usuarioExistente = new UsuarioEntity();
        usuarioExistente.setId(usuarioExistenteId);
        usuarioExistente.setFielId(fielId);
        when(usuarioRepository.findByFielId(fielId)).thenReturn(Optional.of(usuarioExistente));

        var response = useCase.execute(request());

        assertThat(response.usuarioId()).isEqualTo(usuarioExistenteId);
        assertThat(response.usuarioCriado()).isFalse();
        verify(createUsuarioUseCase, never()).execute(any(), any(), any());
    }

    @Test
    void deveAtualizarPapelDoVinculoSemCopiarALiderancaNaOrganizacao() {
        authenticateAsParoco();
        var usuarioExistente = new UsuarioEntity();
        when(usuarioRepository.findByFielId(fielId)).thenReturn(Optional.of(usuarioExistente));

        useCase.execute(request());

        var captor = ArgumentCaptor.forClass(MembroGrupoEntity.class);
        verify(membroRepository).save(captor.capture());
        assertThat(captor.getValue().getPapel()).isEqualTo("coordenador");
        verify(organizacaoRepository, never()).save(any(OrganizacaoEntity.class));
    }

    @Test
    void deveLancarExcecaoQuandoFielNaoEhMembroAtivo() {
        authenticateAsParoco();
        when(membroRepository.findByFielIdAndOrganizacaoIdAndAtivoTrue(fielId, organizacaoId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(request()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("não é membro ativo");
    }

    @Test
    void deveLancarExcecaoQuandoFielNaoEncontrado() {
        authenticateAsParoco();
        when(fielRepository.findById(fielId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(request()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Fiel não encontrado");
    }

    @Test
    void deveLancarExcecaoQuandoOrganizacaoNaoEncontrada() {
        authenticateAsParoco();
        when(organizacaoRepository.findById(organizacaoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(request()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Organização não encontrada");
    }

    @Test
    void deveLancarExcecaoQuandoPapelInvalido() {
        authenticateAsParoco();
        var request = new PromoverACoordenadorRequest(fielId, organizacaoId, "PAPEL_INEXISTENTE", "x", "y");

        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Papel inválido");
    }

    @Test
    void deveRejeitarPapelGlobalViaMembroGrupo() {
        authenticateAsParoco();
        var request = new PromoverACoordenadorRequest(fielId, organizacaoId, "PAROCO", "x", "y");

        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Papel inválido");
    }

    @Test
    void deveNegarQuandoAtorSemAlcadaDeParoco() {
        authenticateWith("ROLE_COORDENADOR");

        assertThatThrownBy(() -> useCase.execute(request()))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void deveNegarQuandoAtorEhAdminMesmoComPapelGlobal() {
        var principal = new UsuarioDetails(actorId, "admin", "pass", true, "", List.of());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null,
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"),
                                new SimpleGrantedAuthority("ROLE_GLOBAL_ADMIN"))));

        assertThatThrownBy(() -> useCase.execute(request()))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Apenas o Pároco possui provisão canônica");
    }

    @Test
    void deveNegarQuandoAtorEhSecretariaParoquialMesmoComPapelGlobal() {
        authenticateWith("ROLE_SECRETARIA_PAROQUIAL");

        assertThatThrownBy(() -> useCase.execute(request()))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Apenas o Pároco possui provisão canônica");
        verify(membroRepository, never()).save(any(MembroGrupoEntity.class));
    }

    @Test
    void deveRecusarViceCoordenadorSemIdoneidade() {
        authenticateAsParoco();
        MembroGrupoEntity semIdoneidade = new MembroGrupoEntity();
        semIdoneidade.setId(UUID.randomUUID());
        semIdoneidade.setFielId(fielId);
        semIdoneidade.setOrganizacaoId(organizacaoId);
        semIdoneidade.setTipoOrganizacao(TipoOrganizacao.PASTORAL);
        semIdoneidade.setPapel("VOLUNTARIO");
        semIdoneidade.setAtivo(true);
        when(membroRepository.findByFielIdAndOrganizacaoIdAndAtivoTrue(fielId, organizacaoId))
                .thenReturn(Optional.of(semIdoneidade));
        var comoVice = new PromoverACoordenadorRequest(fielId, organizacaoId, "SECRETARIO_GRUPO", "x", "y");

        assertThatThrownBy(() -> useCase.execute(comoVice))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("idoneidade");
    }

    @Test
    void deveRecusarSegundoCoordenadorAtivoNaMesmaOrganizacao() {
        authenticateAsParoco();
        MembroGrupoEntity outro = new MembroGrupoEntity();
        outro.setId(UUID.randomUUID());
        outro.setFielId(UUID.randomUUID());
        outro.setOrganizacaoId(organizacaoId);
        outro.setPapel("coordenador");
        outro.setAtivo(true);
        when(membroRepository.findByOrganizacaoIdAndAtivoTrue(organizacaoId)).thenReturn(List.of(outro));

        assertThatThrownBy(() -> useCase.execute(request()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("coordenador ativo");
        verify(membroRepository, never()).save(any(MembroGrupoEntity.class));
    }

    @Test
    void deveNegarQuandoNaoAutenticado() {
        SecurityContextHolder.clearContext();

        assertThatThrownBy(() -> useCase.execute(request()))
                .isInstanceOf(AccessDeniedException.class);
    }
}
