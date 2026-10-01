package br.com.nsfatima.gestao.sacramento.infrastructure.persistence.mapper;

import br.com.nsfatima.gestao.sacramento.domain.model.AssentoSacramental;
import br.com.nsfatima.gestao.sacramento.domain.model.AssentoSacramentalId;
import br.com.nsfatima.gestao.sacramento.domain.model.Averbacao;
import br.com.nsfatima.gestao.sacramento.domain.model.NotificacaoCanonica;
import br.com.nsfatima.gestao.sacramento.domain.model.Participante;
import br.com.nsfatima.gestao.sacramento.domain.model.ReferenciaLivro;
import br.com.nsfatima.gestao.sacramento.domain.model.Ressalva;
import br.com.nsfatima.gestao.sacramento.domain.model.TeorAssento;
import br.com.nsfatima.gestao.sacramento.infrastructure.persistence.entity.AssentoNotificacaoEntity;
import br.com.nsfatima.gestao.sacramento.infrastructure.persistence.entity.AssentoParticipanteEntity;
import br.com.nsfatima.gestao.sacramento.infrastructure.persistence.entity.AssentoRessalvaEntity;
import br.com.nsfatima.gestao.sacramento.infrastructure.persistence.entity.AssentoSacramentalEntity;
import br.com.nsfatima.gestao.sacramento.infrastructure.persistence.entity.AverbacaoBatismoEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Tradutor entre o agregado {@link AssentoSacramental} e a árvore JPA. A gravação reconcilia por id
 * a entidade já gerenciada: participantes são sincronizados (inclusive remoção, só possível em
 * rascunho), averbações são atualizadas e ressalvas/notificações só são acrescentadas, jamais
 * removidas ou alteradas (append-only).
 */
@Component
public class AssentoSacramentalMapper {

    public AssentoSacramental toDomain(AssentoSacramentalEntity e) {
        TeorAssento teor = new TeorAssento(
                new ReferenciaLivro(e.getLivro(), e.getFolha(), e.getTermo()),
                e.getParoquiaCustodiante(), e.getDataCelebracao(), e.getLocalCelebracao(),
                e.getParticipantes().stream().map(AssentoSacramentalMapper::toParticipante).toList(),
                e.getDadosEspecificos(), e.isDadoSensivel());
        return new AssentoSacramental(new AssentoSacramentalId(e.getId()), e.getTipo(), e.getVisibilidade(),
                e.getDataLavratura(), e.getLavradoPor(), e.getCriadoEmUtc(), e.getStatus(), teor,
                e.getHomologadoPor(), e.getHomologadoEmUtc(), e.getMotivoCancelamento(), e.getCanceladoPor(),
                e.getCanceladoEmUtc(),
                e.getRessalvas().stream().map(AssentoSacramentalMapper::toRessalva).toList(),
                e.getAverbacoes().stream().map(AssentoSacramentalMapper::toAverbacao).toList(),
                e.getNotificacoes().stream().map(AssentoSacramentalMapper::toNotificacao).toList());
    }

    /** Aplica o estado do agregado sobre a entidade (nova ou já gerenciada). */
    public void aplicar(AssentoSacramentalEntity e, AssentoSacramental d) {
        TeorAssento teor = d.getTeor();
        e.setTipo(d.getTipo());
        e.setStatus(d.getStatus());
        e.setVisibilidade(d.getVisibilidade());
        e.setDataLavratura(d.getDataLavratura());
        e.setLavradoPor(d.getLavradoPor());
        e.setLivro(teor.referenciaLivro().livro());
        e.setFolha(teor.referenciaLivro().folha());
        e.setTermo(teor.referenciaLivro().termo());
        e.setParoquiaCustodiante(teor.paroquiaCustodiante());
        e.setDataCelebracao(teor.dataCelebracao());
        e.setLocalCelebracao(teor.localCelebracao());
        e.setDadoSensivel(teor.dadoSensivel());
        e.setDadosEspecificos(teor.dadosEspecificos());
        e.setHomologadoPor(d.getHomologadoPor());
        e.setHomologadoEmUtc(d.getHomologadoEmUtc());
        e.setMotivoCancelamento(d.getMotivoCancelamento());
        e.setCanceladoPor(d.getCanceladoPor());
        e.setCanceladoEmUtc(d.getCanceladoEmUtc());

        sincronizarParticipantes(e, teor.participantes());
        sincronizarAverbacoes(e, d.getAverbacoes());
        acrescentarRessalvas(e, d.getRessalvas());
        acrescentarNotificacoes(e, d.getNotificacoes());
    }

    private void sincronizarParticipantes(AssentoSacramentalEntity e, List<Participante> fonte) {
        Set<UUID> ids = fonte.stream().map(Participante::id).collect(Collectors.toSet());
        e.getParticipantes().removeIf(p -> !ids.contains(p.getId()));
        Map<UUID, AssentoParticipanteEntity> existentes =
                indexar(e.getParticipantes(), AssentoParticipanteEntity::getId);
        for (Participante p : fonte) {
            AssentoParticipanteEntity pe = existentes.get(p.id());
            if (pe == null) {
                pe = new AssentoParticipanteEntity(p.id(), e);
                e.getParticipantes().add(pe);
            }
            pe.setPapel(p.papel());
            pe.setNomeHistorico(p.nomeHistorico());
            pe.setDocumento(p.documento());
            pe.setLocalNascimento(p.localNascimento());
            pe.setCatolico(p.catolico());
            pe.setCrismado(p.crismado());
            pe.setIdadeNaCelebracao(p.idadeNaCelebracao());
            pe.setCondicaoCanonica(p.condicaoCanonica());
            pe.setFielId(p.fielId());
            pe.setClerigoId(p.clerigoId());
        }
    }

    private void sincronizarAverbacoes(AssentoSacramentalEntity e, List<Averbacao> fonte) {
        Map<UUID, AverbacaoBatismoEntity> existentes = indexar(e.getAverbacoes(), AverbacaoBatismoEntity::getId);
        for (Averbacao a : fonte) {
            AverbacaoBatismoEntity ae = existentes.get(a.id());
            if (ae == null) {
                ae = new AverbacaoBatismoEntity(a.id(), e);
                e.getAverbacoes().add(ae);
            }
            ae.setTipoEvento(a.tipoEvento());
            ae.setDataAverbacao(a.dataAverbacao());
            ae.setNumeroDocumento(a.numeroDocumento());
            ae.setTeorNota(a.teorNota());
            ae.setReservada(a.reservada());
            ae.setInseridaPor(a.inseridaPor());
            ae.setStatus(a.status());
            ae.setHomologadaPor(a.homologadaPor());
            ae.setHomologadaEmUtc(a.homologadaEmUtc());
        }
    }

    private void acrescentarRessalvas(AssentoSacramentalEntity e, List<Ressalva> fonte) {
        Set<UUID> existentes = e.getRessalvas().stream().map(AssentoRessalvaEntity::getId).collect(Collectors.toSet());
        fonte.stream().filter(r -> !existentes.contains(r.id())).forEach(r -> e.getRessalvas().add(
                new AssentoRessalvaEntity(r.id(), e, r.campo(), r.valorOriginal(), r.valorCorrigido(),
                        r.justificativa(), r.atestadaPor(), r.atestadaEmUtc())));
    }

    private void acrescentarNotificacoes(AssentoSacramentalEntity e, List<NotificacaoCanonica> fonte) {
        Set<UUID> existentes = e.getNotificacoes().stream()
                .map(AssentoNotificacaoEntity::getId).collect(Collectors.toSet());
        fonte.stream().filter(n -> !existentes.contains(n.id())).forEach(n -> e.getNotificacoes().add(
                new AssentoNotificacaoEntity(n.id(), e, n.destinatarioParoquia(), n.emitidaEm(), n.enviadaEm(),
                        n.registradaPor())));
    }

    private static <T> Map<UUID, T> indexar(List<T> itens, Function<T, UUID> id) {
        return itens.stream().collect(Collectors.toMap(id, Function.identity()));
    }

    private static Participante toParticipante(AssentoParticipanteEntity p) {
        return new Participante(p.getId(), p.getPapel(), p.getNomeHistorico(), p.getDocumento(),
                p.getLocalNascimento(), p.getCatolico(), p.getCrismado(), p.getIdadeNaCelebracao(),
                p.getCondicaoCanonica(), p.getFielId(), p.getClerigoId());
    }

    private static Averbacao toAverbacao(AverbacaoBatismoEntity a) {
        return new Averbacao(a.getId(), a.getTipoEvento(), a.getDataAverbacao(), a.getNumeroDocumento(),
                a.getTeorNota(), a.isReservada(), a.getInseridaPor(), a.getStatus(), a.getHomologadaPor(),
                a.getHomologadaEmUtc());
    }

    private static Ressalva toRessalva(AssentoRessalvaEntity r) {
        return new Ressalva(r.getId(), r.getCampo(), r.getValorOriginal(), r.getValorCorrigido(),
                r.getJustificativa(), r.getAtestadaPor(), r.getAtestadaEmUtc());
    }

    private static NotificacaoCanonica toNotificacao(AssentoNotificacaoEntity n) {
        return new NotificacaoCanonica(n.getId(), n.getDestinatarioParoquia(), n.getEmitidaEm(), n.getEnviadaEm(),
                n.getRegistradaPor());
    }
}
