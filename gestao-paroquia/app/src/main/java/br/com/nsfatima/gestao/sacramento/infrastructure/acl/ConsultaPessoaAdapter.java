package br.com.nsfatima.gestao.sacramento.infrastructure.acl;

import br.com.nsfatima.gestao.clero.infrastructure.persistence.repository.ClerigoJpaRepository;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository.FielJpaRepository;
import br.com.nsfatima.gestao.sacramento.application.port.out.ConsultaPessoaPort;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/** Anti-Corruption Layer sobre os repositórios de fiel e clero, expondo apenas o nome civil. */
@Component
public class ConsultaPessoaAdapter implements ConsultaPessoaPort {

    private final FielJpaRepository fielRepository;
    private final ClerigoJpaRepository clerigoRepository;

    public ConsultaPessoaAdapter(FielJpaRepository fielRepository, ClerigoJpaRepository clerigoRepository) {
        this.fielRepository = fielRepository;
        this.clerigoRepository = clerigoRepository;
    }

    @Override
    public Optional<String> nomeDoFiel(UUID fielId) {
        return fielRepository.findById(fielId).map(f -> f.getNome());
    }

    @Override
    public Optional<String> nomeDoClerigo(UUID clerigoId) {
        return clerigoRepository.findById(clerigoId).map(c -> c.getNomeCivil());
    }
}
