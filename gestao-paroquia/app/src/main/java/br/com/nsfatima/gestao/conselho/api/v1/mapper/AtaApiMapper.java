package br.com.nsfatima.gestao.conselho.api.v1.mapper;

import br.com.nsfatima.gestao.conselho.api.v1.dto.AtaAnteriorRequest;
import br.com.nsfatima.gestao.conselho.api.v1.dto.AtaResponse;
import br.com.nsfatima.gestao.conselho.api.v1.dto.AtaResumoResponse;
import br.com.nsfatima.gestao.conselho.api.v1.dto.ConflitoInteresseRequest;
import br.com.nsfatima.gestao.conselho.api.v1.dto.ConflitoInteresseResponse;
import br.com.nsfatima.gestao.conselho.api.v1.dto.ConteudoAtaRequest;
import br.com.nsfatima.gestao.conselho.api.v1.dto.ConteudoAtaResponse;
import br.com.nsfatima.gestao.conselho.api.v1.dto.DeliberacaoRequest;
import br.com.nsfatima.gestao.conselho.api.v1.dto.DeliberacaoResponse;
import br.com.nsfatima.gestao.conselho.api.v1.dto.ItemPautaRequest;
import br.com.nsfatima.gestao.conselho.api.v1.dto.ItemPautaResponse;
import br.com.nsfatima.gestao.conselho.api.v1.dto.PaginaAtasResponse;
import br.com.nsfatima.gestao.conselho.api.v1.dto.PresencaItemRequest;
import br.com.nsfatima.gestao.conselho.api.v1.dto.PresencaRequest;
import br.com.nsfatima.gestao.conselho.api.v1.dto.PresencaResponse;
import br.com.nsfatima.gestao.conselho.api.v1.dto.QuorumResponse;
import br.com.nsfatima.gestao.conselho.api.v1.dto.RegistroAuditoriaResponse;
import br.com.nsfatima.gestao.conselho.api.v1.dto.TipoReuniaoResponse;
import br.com.nsfatima.gestao.conselho.api.v1.dto.VotacaoRequest;
import br.com.nsfatima.gestao.conselho.application.dto.AtaVisao;
import br.com.nsfatima.gestao.conselho.application.dto.PaginaVisoes;
import br.com.nsfatima.gestao.conselho.application.dto.TipoReuniaoInfo;
import br.com.nsfatima.gestao.conselho.domain.model.AtaAnterior;
import br.com.nsfatima.gestao.conselho.domain.model.AtaReuniao;
import br.com.nsfatima.gestao.conselho.domain.model.ConflitoInteresse;
import br.com.nsfatima.gestao.conselho.domain.model.ConteudoAta;
import br.com.nsfatima.gestao.conselho.domain.model.Deliberacao;
import br.com.nsfatima.gestao.conselho.domain.model.ItemPauta;
import br.com.nsfatima.gestao.conselho.domain.model.Presenca;
import br.com.nsfatima.gestao.conselho.domain.model.PresencaReuniao;
import br.com.nsfatima.gestao.conselho.domain.model.RegistroAuditoriaAta;
import br.com.nsfatima.gestao.conselho.domain.model.Votacao;
import br.com.nsfatima.gestao.conselho.domain.policy.RegrasTipoReuniao;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.stream.IntStream;

/**
 * Converte entre os DTOs da API e o modelo do conselho. A numeração de itens e deliberações segue a posição na
 * lista, de modo que o cliente não consegue enviar sequência inconsistente. As exceções de domínio lançadas
 * aqui (conteúdo inválido) chegam ao {@code ConselhoExceptionHandler}.
 */
@Component
public class AtaApiMapper {

    private static final String PESSOA_DESCONHECIDA = "Pessoa não identificada";

    /** Converte o conteúdo recebido para o modelo de domínio, validando suas invariantes. */
    public ConteudoAta paraConteudo(ConteudoAtaRequest r) {
        return new ConteudoAta(r.titulo(), r.dataReuniao(), r.local(), r.carater(), r.modalidade(),
                presenca(r.presenca()), ataAnterior(r.ataAnterior()), itens(r.itens()));
    }

    private static PresencaReuniao presenca(PresencaRequest r) {
        if (r == null) {
            return PresencaReuniao.vazia();
        }
        List<Presenca> nominal = r.nominal() == null ? List.of() : r.nominal().stream()
                .map((PresencaItemRequest p) -> new Presenca(p.fielId(), p.funcao(), p.situacao())).toList();
        return new PresencaReuniao(nominal, r.total());
    }

    private static AtaAnterior ataAnterior(AtaAnteriorRequest r) {
        return r == null ? AtaAnterior.nenhuma() : new AtaAnterior(r.ataId(), r.aprovacao(), r.ressalvas());
    }

    private static List<ItemPauta> itens(List<ItemPautaRequest> itens) {
        return numerar(itens, (ordem, i) -> new ItemPauta(ordem, i.titulo(), i.relato(),
                numerar(i.deliberacoes(), AtaApiMapper::deliberacao)));
    }

    private static Deliberacao deliberacao(int ordem, DeliberacaoRequest d) {
        VotacaoRequest v = d.votacao();
        List<ConflitoInteresse> conflitos = d.conflitos() == null ? List.of() : d.conflitos().stream()
                .map((ConflitoInteresseRequest c) -> new ConflitoInteresse(c.declaranteFielId(), c.descricao()))
                .toList();
        return new Deliberacao(ordem, d.texto(), d.resultado(),
                v == null ? null : new Votacao(v.favor(), v.contra(), v.abstencoes()), conflitos);
    }

    private static <T, R> List<R> numerar(List<T> origem, BiFunction<Integer, T, R> conversor) {
        if (origem == null) {
            return List.of();
        }
        return IntStream.range(0, origem.size()).mapToObj(i -> conversor.apply(i + 1, origem.get(i))).toList();
    }

    /** Ata completa, com nomes resolvidos e as ações permitidas ao ator. */
    public AtaResponse paraResposta(AtaVisao visao) {
        AtaReuniao ata = visao.ata();
        return new AtaResponse(ata.getId().value(), ata.getTipo(), ata.getTipo().getRotulo(),
                ata.getOrganizacaoConvocanteId(), visao.nomeOrganizacaoConvocante(),
                ata.getOrganizacaoAssociadaId(), visao.nomeOrganizacaoAssociada(), ata.getStatus(),
                ata.identificacao().orElse(null), conteudo(ata.getConteudo(), visao.nomesFieis()),
                ata.quorum().map(q -> new QuorumResponse(q.membrosConvocados(), q.minimoNecessario(),
                        q.presentes(), q.atingido())).orElse(null),
                ata.getRedatorId(), ata.getHomologadorId(), ata.getMotivoVeto(), ata.getDecididoEmUtc(),
                ata.getAtaRetificadaId() == null ? null : ata.getAtaRetificadaId().value(),
                ata.getJustificativaRetificacao(), ata.getCriadoEmUtc(), visao.acoes());
    }

    private static ConteudoAtaResponse conteudo(ConteudoAta c, Map<UUID, String> nomes) {
        List<PresencaResponse> presencas = c.presenca().nominal().stream()
                .map(p -> new PresencaResponse(p.fielId(), nome(nomes, p.fielId()), p.funcao(), p.situacao()))
                .toList();
        return new ConteudoAtaResponse(c.titulo(), c.dataReuniao(), c.local(), c.carater(), c.modalidade(),
                presencas, c.presenca().total(), c.ataAnterior().ataId(), c.ataAnterior().aprovacao(),
                c.ataAnterior().ressalvas(), c.itens().stream().map(i -> item(i, nomes)).toList());
    }

    private static ItemPautaResponse item(ItemPauta i, Map<UUID, String> nomes) {
        return new ItemPautaResponse(i.ordem(), i.titulo(), i.relato(),
                i.deliberacoes().stream().map(d -> deliberacao(d, nomes)).toList());
    }

    private static DeliberacaoResponse deliberacao(Deliberacao d, Map<UUID, String> nomes) {
        Votacao v = d.votacao();
        List<ConflitoInteresseResponse> conflitos = d.conflitos().stream()
                .map(c -> new ConflitoInteresseResponse(c.declaranteFielId(), nome(nomes, c.declaranteFielId()),
                        c.descricao())).toList();
        return new DeliberacaoResponse(d.ordem(), d.texto(), d.resultado(),
                v == null ? null : new VotacaoRequest(v.favor(), v.contra(), v.abstencoes()), conflitos);
    }

    private static String nome(Map<UUID, String> nomes, UUID fielId) {
        return nomes.getOrDefault(fielId, PESSOA_DESCONHECIDA);
    }

    /** Linha de listagem. */
    public AtaResumoResponse paraResumo(AtaVisao visao) {
        AtaReuniao ata = visao.ata();
        return new AtaResumoResponse(ata.getId().value(), ata.getTipo(), ata.getTipo().getRotulo(),
                ata.getConteudo().titulo(), ata.getConteudo().dataReuniao(), ata.getOrganizacaoConvocanteId(),
                visao.nomeOrganizacaoConvocante(), ata.getStatus(), ata.identificacao().orElse(null),
                visao.acoes());
    }

    /** Página de listagem. */
    public PaginaAtasResponse paraPagina(PaginaVisoes pagina, int numero, int tamanho) {
        return new PaginaAtasResponse(pagina.itens().stream().map(this::paraResumo).toList(), pagina.total(),
                numero, tamanho);
    }

    /** Tipo de reunião com suas regras. */
    public TipoReuniaoResponse paraTipo(TipoReuniaoInfo info) {
        RegrasTipoReuniao r = info.regras();
        return new TipoReuniaoResponse(info.tipo(), info.tipo().getRotulo(), r.exigeHomologacao(), r.aplicaQuorum(),
                r.modoPresenca(), r.permiteVotacao(), r.exigeOrganizacaoAssociada(), r.tiposConvocantes(),
                r.subtiposConselho(), r.papeisRedatores());
    }

    /** Entrada da trilha, sem IP nem estados serializados. */
    public RegistroAuditoriaResponse paraTrilha(RegistroAuditoriaAta registro) {
        return new RegistroAuditoriaResponse(registro.id(), registro.acao(), registro.ator(), registro.perfil(),
                registro.ocorridoEmUtc());
    }
}
