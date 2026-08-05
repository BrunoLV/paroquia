package br.com.nsfatima.gestao.iam.application.usecase;

import br.com.nsfatima.gestao.iam.api.v1.dto.RegistrarUsuarioRequest;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.entity.UsuarioEntity;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.OrganizacaoJpaRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Autocadastro público de um novo fiel/usuário (endpoint sem autenticação, {@code POST /api/v1/usuarios/registrar}).
 * O usuário nasce sempre com o papel {@code ROLE_USER}; seu estado de habilitação depende de quem está
 * autenticado no momento do cadastro — se for {@code ROLE_ADMIN} ou {@code ROLE_CLERO_PAROCO} cadastrando em
 * nome de terceiro, o usuário já nasce habilitado, caso contrário nasce desabilitado e pendente de aprovação
 * via {@code ApproveUsuarioUseCase}.
 */
@Service
public class RegistrarUsuarioUseCase {

    private final UsuarioJpaRepository usuarioRepository;
    private final OrganizacaoJpaRepository organizacaoRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistrarUsuarioUseCase(
            UsuarioJpaRepository usuarioRepository,
            OrganizacaoJpaRepository organizacaoRepository,
            PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.organizacaoRepository = organizacaoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Valida e persiste o cadastro de um novo usuário: rejeita username/e-mail já cadastrado (case-insensitive),
     * exige uma igreja/comunidade de vínculo do tipo MATRIZ ou COMUNIDADE (outros tipos — movimentos, clero —
     * não são aceitos como igreja de cadastro), e decide o estado inicial de habilitação com base em quem
     * está autenticado no momento da chamada (admin/pároco cadastrando por terceiro habilita direto).
     *
     * @param request dados do cadastro (nome, credenciais, telefone, data de nascimento, igreja e flags
     *                sacramentais)
     * @return o id gerado para o novo usuário
     * @throws IllegalArgumentException se o username/e-mail já estiver em uso, se {@code igrejaId} for nulo,
     *                                  não existir, ou não for do tipo MATRIZ/COMUNIDADE
     */
    @Transactional
    public UUID execute(RegistrarUsuarioRequest request) {
        if (usuarioRepository.findByUsernameIgnoreCase(request.username()).isPresent()) {
            throw new IllegalArgumentException("Username/E-mail ja cadastrado: " + request.username());
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdminOrParoco = auth != null && auth.isAuthenticated() &&
                auth.getAuthorities().stream().anyMatch(a ->
                        "ROLE_ADMIN".equals(a.getAuthority()) || "ROLE_CLERO_PAROCO".equals(a.getAuthority()));

        if (request.igrejaId() == null) {
            throw new IllegalArgumentException("A igreja/comunidade e obrigatoria para o cadastro do usuario");
        }
        var igrejaTargetOpt = organizacaoRepository.findById(request.igrejaId());
        if (igrejaTargetOpt.isEmpty()) {
            throw new IllegalArgumentException("Igreja nao encontrada: " + request.igrejaId());
        }
        var igrejaTarget = igrejaTargetOpt.get();
        if (igrejaTarget.getTipo() != TipoOrganizacao.MATRIZ
                && igrejaTarget.getTipo() != TipoOrganizacao.COMUNIDADE) {
            throw new IllegalArgumentException("A igreja selecionada deve ser do tipo MATRIZ ou COMUNIDADE");
        }

        UsuarioEntity usuario = new UsuarioEntity();
        UUID usuarioId = UUID.randomUUID();
        usuario.setId(usuarioId);
        usuario.setNome(request.nome());
        usuario.setUsername(request.username());
        usuario.setPasswordHash(passwordEncoder.encode(request.password()));
        usuario.setTelefone(request.telefone());
        usuario.setDataNascimento(request.dataNascimento());
        usuario.setIgrejaId(request.igrejaId());
        usuario.setBatizado(Boolean.TRUE.equals(request.batizado()));
        usuario.setPrimeiraComunhao(Boolean.TRUE.equals(request.primeiraComunhao()));
        usuario.setCrismado(Boolean.TRUE.equals(request.crismado()));
        usuario.setCasadoIgrejaCatolica(Boolean.TRUE.equals(request.casadoIgrejaCatolica()));
        usuario.setEnabled(isAdminOrParoco); // Ativo diretamente se cadastrado por admin

        usuario.setRoles("ROLE_USER"); // Papel padrão não administrativo

        // Autocadastro público não tem actor autenticado no caso comum (a própria pessoa se registrando);
        // quando um admin/pároco cadastra em nome de terceiro, usamos o actor autenticado como auditoria.
        String actor = (isAdminOrParoco && auth != null) ? auth.getName() : request.username();
        usuario.setCriadoPor(actor);
        usuario.setAtualizadoPor(actor);

        usuarioRepository.save(usuario);

        return usuarioId;
    }
}
