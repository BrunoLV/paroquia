package br.com.nsfatima.gestao.clero.application.usecase;

import br.com.nsfatima.gestao.clero.infrastructure.persistence.entity.ClerigoEntity;
import br.com.nsfatima.gestao.clero.infrastructure.persistence.repository.ClerigoJpaRepository;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository.RelacionamentoFamiliarJpaRepository;
import br.com.nsfatima.gestao.governanca.domain.model.TipoTitularConsentimento;
import br.com.nsfatima.gestao.governanca.infrastructure.persistence.repository.HistoricoConsentimentoJpaRepository;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

/**
 * Ciclo de vida LGPD do clérigo, em paridade com o do fiel (Regra de Paridade Irrevogável): cinco anos após a
 * inativação o cadastro é anonimizado. Elimina dados civis e o blind index do CPF, vínculos familiares e o
 * rastro pessoal da trilha de consentimento, e desativa/anonimiza a conta de acesso. Permanecem os dados
 * institucionais e canônicos — nome eclesiástico, grau da ordem, ordenações e encargos — que têm fé pública
 * perpétua (Art. 16, I da LGPD; Cân. 535).
 */
@Service
public class GerenciarCicloVidaClerigoUseCase {

    private static final int ANOS_RETENCAO = 5;

    private final ClerigoJpaRepository clerigoRepository;
    private final UsuarioJpaRepository usuarioRepository;
    private final RelacionamentoFamiliarJpaRepository relacionamentoRepository;
    private final HistoricoConsentimentoJpaRepository historicoRepository;
    private final Clock clock;

    @Autowired
    public GerenciarCicloVidaClerigoUseCase(
            ClerigoJpaRepository clerigoRepository,
            UsuarioJpaRepository usuarioRepository,
            RelacionamentoFamiliarJpaRepository relacionamentoRepository,
            HistoricoConsentimentoJpaRepository historicoRepository) {
        this(clerigoRepository, usuarioRepository, relacionamentoRepository, historicoRepository,
                Clock.systemUTC());
    }

    GerenciarCicloVidaClerigoUseCase(
            ClerigoJpaRepository clerigoRepository,
            UsuarioJpaRepository usuarioRepository,
            RelacionamentoFamiliarJpaRepository relacionamentoRepository,
            HistoricoConsentimentoJpaRepository historicoRepository,
            Clock clock) {
        this.clerigoRepository = clerigoRepository;
        this.usuarioRepository = usuarioRepository;
        this.relacionamentoRepository = relacionamentoRepository;
        this.historicoRepository = historicoRepository;
        this.clock = clock;
    }

    /** Anonimiza os clérigos inativos há mais de cinco anos que ainda não foram anonimizados. */
    @Transactional
    public void anonimizarCadastrosVencidos() {
        Instant limite = Instant.now(clock).atZone(ZoneOffset.UTC).minusYears(ANOS_RETENCAO).toInstant();
        for (ClerigoEntity clerigo : clerigoRepository
                .findByAtivoFalseAndDadosCadastraisEliminadosFalseAndDeletedAtBefore(limite)) {
            anonimizar(clerigo);
        }
    }

    private void anonimizar(ClerigoEntity clerigo) {
        UUID id = clerigo.getId();
        Instant agora = Instant.now(clock);
        clerigo.anonimizar();
        relacionamentoRepository.findVigentesDaPessoa(id).forEach(r -> r.setDeletedAt(agora));
        historicoRepository.anonimizarDadosPessoais(TipoTitularConsentimento.CLERIGO.name(), id);
        usuarioRepository.findByClerigoId(id).ifPresent(usuario -> {
            usuario.anonimizarConta();
            usuarioRepository.save(usuario);
        });
        clerigoRepository.save(clerigo);
    }
}
