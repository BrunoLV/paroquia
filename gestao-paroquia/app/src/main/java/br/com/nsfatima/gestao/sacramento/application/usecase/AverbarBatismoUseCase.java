package br.com.nsfatima.gestao.sacramento.application.usecase;

import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import br.com.nsfatima.gestao.sacramento.api.v1.dto.AssentoResponse;
import br.com.nsfatima.gestao.sacramento.api.v1.dto.AverbacaoRequest;
import br.com.nsfatima.gestao.sacramento.application.service.AssentoAcessoService;
import br.com.nsfatima.gestao.sacramento.application.service.SacramentoAuditoriaService;
import br.com.nsfatima.gestao.sacramento.domain.model.AssentoSacramental;
import br.com.nsfatima.gestao.sacramento.domain.model.Averbacao;
import br.com.nsfatima.gestao.sacramento.domain.model.OperacaoSacramental;
import br.com.nsfatima.gestao.sacramento.domain.repository.AssentoSacramentalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Averba à margem do assento de Batismo: a secretaria digita (pendente) e somente o Pároco
 * homologa. O registro é append-only; não há edição nem remoção de averbações.
 */
@Service
public class AverbarBatismoUseCase {

    private final EventoActorContextResolver actorResolver;
    private final AssentoAcessoService acesso;
    private final AssentoSacramentalRepository repository;
    private final SacramentoAuditoriaService auditoria;

    public AverbarBatismoUseCase(EventoActorContextResolver actorResolver, AssentoAcessoService acesso,
                                 AssentoSacramentalRepository repository, SacramentoAuditoriaService auditoria) {
        this.actorResolver = actorResolver;
        this.acesso = acesso;
        this.repository = repository;
        this.auditoria = auditoria;
    }

    @Transactional
    public AssentoResponse inserir(UUID assentoId, AverbacaoRequest request) {
        EventoActorContext ator = actorResolver.resolveRequired();
        AssentoSacramental assento = acesso.carregar(assentoId, ator, OperacaoSacramental.AVERBAR_DIGITAR);
        String antes = auditoria.capturar(assento);
        assento.inserirAverbacao(Averbacao.digitar(request.tipoEvento(), request.dataAverbacao(),
                request.numeroDocumento(), request.teorNota(), Boolean.TRUE.equals(request.reservada()), ator.actor()));
        AssentoSacramental salvo = repository.save(assento);
        auditoria.registrar(ator, "averbar", antes, salvo);
        return AssentoResponse.de(salvo);
    }

    @Transactional
    public AssentoResponse homologar(UUID assentoId, UUID averbacaoId) {
        EventoActorContext ator = actorResolver.resolveRequired();
        AssentoSacramental assento = acesso.carregar(assentoId, ator, OperacaoSacramental.HOMOLOGAR_AVERBACAO);
        String antes = auditoria.capturar(assento);
        assento.homologarAverbacao(averbacaoId, ator.actor(), Instant.now());
        AssentoSacramental salvo = repository.save(assento);
        auditoria.registrar(ator, "homologar-averbacao", antes, salvo);
        return AssentoResponse.de(salvo);
    }
}
