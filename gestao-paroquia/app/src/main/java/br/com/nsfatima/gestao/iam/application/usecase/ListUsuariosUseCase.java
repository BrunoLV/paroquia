package br.com.nsfatima.gestao.iam.application.usecase;

import java.util.List;
import br.com.nsfatima.gestao.iam.api.v1.dto.UsuarioResponse;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.repository.MembroJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.stream.Collectors;

@Service
public class ListUsuariosUseCase {

    private final UsuarioJpaRepository usuarioRepository;
    private final MembroJpaRepository membroRepository;

    public ListUsuariosUseCase(UsuarioJpaRepository usuarioRepository, MembroJpaRepository membroRepository) {
        this.usuarioRepository = usuarioRepository;
        this.membroRepository = membroRepository;
    }

    @Transactional(readOnly = true)
    /**
     * Recupera a listagem completa de usuários cadastrados para visualização e gestão em painéis administrativos.
     * 
     * Exemplo: useCase.execute()
     */
    public List<UsuarioResponse> execute() {
        return usuarioRepository.findAll().stream()
                .map(u -> {
                    String nome = u.getNome();
                    String telefone = u.getTelefone();
                    java.time.LocalDate dataNascimento = u.getDataNascimento();

                    if (nome == null || nome.trim().isEmpty()) {
                        var membroOpt = membroRepository.findByUsuarioId(u.getId());
                        if (membroOpt.isPresent()) {
                            nome = membroOpt.get().getNomeCompleto();
                            telefone = membroOpt.get().getTelefone();
                            dataNascimento = membroOpt.get().getDataNascimento();
                        }
                    }

                    return new UsuarioResponse(
                            u.getId(),
                            u.getUsername(),
                            u.isEnabled(),
                            u.getRoles() == null ? List.of()
                                    : Arrays.stream(u.getRoles().split(",")).map(String::trim).collect(Collectors.toList()),
                            List.of(),
                            nome,
                            telefone,
                            dataNascimento);
                })
                .toList();
    }
}
