package br.com.nsfatima.gestao.fiel.application.usecase;

import br.com.nsfatima.gestao.fiel.infrastructure.persistence.entity.FielEntity;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository.FielJpaRepository;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

/** Ciclo de vida LGPD: desativação reversível e anonimização automática após cinco anos. */
@Service
public class GerenciarCicloVidaFielUseCase {

    private static final String ATOR_SISTEMA = "lgpd-retencao-automatica";
    private final FielJpaRepository fielRepository;
    private final UsuarioJpaRepository usuarioRepository;
    private final Clock clock;

    @Autowired
    public GerenciarCicloVidaFielUseCase(FielJpaRepository fielRepository, UsuarioJpaRepository usuarioRepository) {
        this(fielRepository, usuarioRepository, Clock.systemUTC());
    }

    GerenciarCicloVidaFielUseCase(FielJpaRepository fielRepository, UsuarioJpaRepository usuarioRepository, Clock clock) {
        this.fielRepository = fielRepository;
        this.usuarioRepository = usuarioRepository;
        this.clock = clock;
    }

    @Transactional
    public void desativar(UUID fielId, String actor) {
        FielEntity fiel = obter(fielId);
        Instant agora = Instant.now(clock);
        fiel.setAtivo(false);
        fiel.setDesativadoEmUtc(agora);
        fiel.setAtualizadoEmUtc(agora);
        fiel.setAtualizadoPor(actor);
        usuarioRepository.findByFielId(fielId).ifPresent(usuario -> {
            usuario.setEnabled(false);
            usuarioRepository.save(usuario);
        });
        fielRepository.save(fiel);
    }

    @Transactional
    public void reativar(UUID fielId, String actor) {
        FielEntity fiel = obter(fielId);
        if (fiel.isDadosCadastraisEliminados()) {
            throw new IllegalStateException("Cadastro anonimizado não pode ser reativado.");
        }
        Instant agora = Instant.now(clock);
        fiel.setAtivo(true);
        fiel.setDesativadoEmUtc(null);
        fiel.setAtualizadoEmUtc(agora);
        fiel.setAtualizadoPor(actor);
        usuarioRepository.findByFielId(fielId).ifPresent(usuario -> {
            usuario.setEnabled(true);
            usuarioRepository.save(usuario);
        });
        fielRepository.save(fiel);
    }

    @Transactional
    public void anonimizarCadastrosVencidos() {
        Instant limite = Instant.now(clock).atZone(ZoneOffset.UTC).minusYears(5).toInstant();
        for (FielEntity fiel : fielRepository
                .findByAtivoFalseAndDadosCadastraisEliminadosFalseAndDesativadoEmUtcBefore(limite)) {
            anonimizar(fiel);
            fiel.setAtualizadoEmUtc(Instant.now(clock));
            fiel.setAtualizadoPor(ATOR_SISTEMA);
            fielRepository.save(fiel);
        }
    }

    private FielEntity obter(UUID fielId) {
        return fielRepository.findById(fielId)
                .orElseThrow(() -> new EntityNotFoundException("Fiel não encontrado."));
    }

    private void anonimizar(FielEntity fiel) {
        fiel.setNome(null); fiel.setCpf(null); fiel.setRg(null); fiel.setDocumentoEstrangeiroNumero(null);
        fiel.setEmail(null); fiel.setCelular(null); fiel.setTelefoneFixo(null); fiel.setEnderecoLogradouro(null);
        fiel.setEnderecoNumero(null); fiel.setEnderecoComplemento(null); fiel.setEnderecoBairro(null);
        fiel.setEnderecoCep(null); fiel.setEnderecoCidade(null); fiel.setEnderecoEstado(null);
        fiel.setDadosCadastraisEliminados(true);
    }
}
