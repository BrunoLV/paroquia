package br.com.nsfatima.gestao.fiel.application.usecase;
import br.com.nsfatima.gestao.fiel.api.v1.dto.NotificacaoResponse;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.entity.NotificacaoEntity;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository.NotificacaoJpaRepository;
import java.time.Instant;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service public class GerenciarNotificacaoUseCase {
 private final NotificacaoJpaRepository repository; public GerenciarNotificacaoUseCase(NotificacaoJpaRepository r){repository=r;}
 @Transactional public void publicar(UUID usuarioId,String mensagem){var n=new NotificacaoEntity();n.setId(UUID.randomUUID());n.setUsuarioId(usuarioId);n.setTipo("AJUSTE_CADASTRAL_DECIDIDO");n.setMensagem(mensagem);n.setCriadoEmUtc(Instant.now());repository.save(n);}
 @Transactional(readOnly=true) public List<NotificacaoResponse> listar(UUID usuarioId){return repository.findByUsuarioIdOrderByCriadoEmUtcDesc(usuarioId).stream().map(n->new NotificacaoResponse(n.getId(),n.getTipo(),n.getMensagem(),n.getCriadoEmUtc(),n.getLidaEmUtc())).toList();}
 @Transactional public void marcarLida(UUID id,UUID usuarioId){var n=repository.findByIdAndUsuarioId(id,usuarioId).orElseThrow(()->new IllegalArgumentException("Notificação não encontrada."));if(n.getLidaEmUtc()==null){n.setLidaEmUtc(Instant.now());repository.save(n);}}
}
