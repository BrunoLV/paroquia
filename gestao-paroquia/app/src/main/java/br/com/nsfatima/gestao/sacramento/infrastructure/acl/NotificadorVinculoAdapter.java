package br.com.nsfatima.gestao.sacramento.infrastructure.acl;

import br.com.nsfatima.gestao.fiel.application.usecase.GerenciarNotificacaoUseCase;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioPerfilJpaRepository;
import br.com.nsfatima.gestao.sacramento.application.port.out.NotificadorVinculoPort;
import org.springframework.stereotype.Component;

/**
 * Anti-Corruption Layer sobre as notificações persistentes: entrega o aviso a todo usuário ativo com
 * papel de Pároco. Administradores também aprovam acessos, mas não atuam sobre atos sacramentais.
 */
@Component
public class NotificadorVinculoAdapter implements NotificadorVinculoPort {

    static final String TIPO_NOTIFICACAO = "SACRAMENTO_VINCULO_PENDENTE";

    private final GerenciarNotificacaoUseCase notificacoes;
    private final UsuarioPerfilJpaRepository usuarios;

    public NotificadorVinculoAdapter(GerenciarNotificacaoUseCase notificacoes, UsuarioPerfilJpaRepository usuarios) {
        this.notificacoes = notificacoes;
        this.usuarios = usuarios;
    }

    @Override
    public void notificarParocos(String autor, String sacramento, int quantidade) {
        String mensagem = ("%d vínculo(s) de fiel/clérigo em assento de %s proposto(s) por '%s' "
                + "aguardam aprovação do Pároco.").formatted(quantidade, sacramento, autor);
        usuarios.findAprovadoresAtivos().stream()
                .filter(u -> u.getRoles() != null && u.getRoles().toUpperCase().contains("PAROCO"))
                .forEach(u -> notificacoes.publicar(u.getId(), TIPO_NOTIFICACAO, mensagem));
    }
}
