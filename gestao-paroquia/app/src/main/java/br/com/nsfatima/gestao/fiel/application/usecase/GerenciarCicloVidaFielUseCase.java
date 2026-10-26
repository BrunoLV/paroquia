package br.com.nsfatima.gestao.fiel.application.usecase;

import br.com.nsfatima.gestao.fiel.infrastructure.persistence.entity.FielEntity;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository.FielDependenteJpaRepository;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository.FielJpaRepository;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository.RelacionamentoFamiliarJpaRepository;
import br.com.nsfatima.gestao.governanca.domain.model.TipoTitularConsentimento;
import br.com.nsfatima.gestao.governanca.infrastructure.persistence.repository.HistoricoConsentimentoJpaRepository;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

/**
 * Ciclo de vida LGPD: desativação reversível e anonimização automática após cinco anos. A anonimização é
 * completa: além dos dados do próprio cadastro ({@link FielEntity#anonimizar}), elimina dependentes e vínculos
 * familiares, anonimiza o IP e o responsável legal da trilha de consentimento (o evento permanece como prova)
 * e desativa/anonimiza a conta de acesso. Os assentos sacramentais são preservados (Art. 16, I).
 */
@Service
public class GerenciarCicloVidaFielUseCase {

    private static final String ATOR_SISTEMA = "lgpd-retencao-automatica";
    private final FielJpaRepository fielRepository;
    private final UsuarioJpaRepository usuarioRepository;
    private final FielDependenteJpaRepository dependenteRepository;
    private final RelacionamentoFamiliarJpaRepository relacionamentoRepository;
    private final HistoricoConsentimentoJpaRepository historicoRepository;
    private final Clock clock;

    @Autowired
    public GerenciarCicloVidaFielUseCase(
            FielJpaRepository fielRepository,
            UsuarioJpaRepository usuarioRepository,
            FielDependenteJpaRepository dependenteRepository,
            RelacionamentoFamiliarJpaRepository relacionamentoRepository,
            HistoricoConsentimentoJpaRepository historicoRepository) {
        this(fielRepository, usuarioRepository, dependenteRepository, relacionamentoRepository,
                historicoRepository, Clock.systemUTC());
    }

    GerenciarCicloVidaFielUseCase(
            FielJpaRepository fielRepository,
            UsuarioJpaRepository usuarioRepository,
            FielDependenteJpaRepository dependenteRepository,
            RelacionamentoFamiliarJpaRepository relacionamentoRepository,
            HistoricoConsentimentoJpaRepository historicoRepository,
            Clock clock) {
        this.fielRepository = fielRepository;
        this.usuarioRepository = usuarioRepository;
        this.dependenteRepository = dependenteRepository;
        this.relacionamentoRepository = relacionamentoRepository;
        this.historicoRepository = historicoRepository;
        this.clock = clock;
    }

    @Transactional
    public void desativar(UUID fielId, String actor) {
        FielEntity fiel = obter(fielId);
        fiel.desativar(Instant.now(clock), actor);
        usuarioRepository.findByFielId(fielId).ifPresent(usuario -> {
            usuario.setEnabled(false);
            usuarioRepository.save(usuario);
        });
        fielRepository.save(fiel);
    }

    @Transactional
    public void reativar(UUID fielId, String actor) {
        FielEntity fiel = obter(fielId);
        fiel.reativar(Instant.now(clock), actor);
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
            fiel.anonimizar(Instant.now(clock), ATOR_SISTEMA);
            eliminarRastros(fiel.getId());
            fielRepository.save(fiel);
        }
    }

    private void eliminarRastros(UUID fielId) {
        Instant agora = Instant.now(clock);
        dependenteRepository.excluirPorFiel(fielId);
        relacionamentoRepository.findVigentesDaPessoa(fielId).forEach(r -> r.setDeletedAt(agora));
        historicoRepository.anonimizarDadosPessoais(TipoTitularConsentimento.FIEL.name(), fielId);
        usuarioRepository.findByFielId(fielId).ifPresent(usuario -> {
            usuario.anonimizarConta();
            usuarioRepository.save(usuario);
        });
    }

    private FielEntity obter(UUID fielId) {
        return fielRepository.findById(fielId)
                .orElseThrow(() -> new EntityNotFoundException("Fiel não encontrado."));
    }
}
