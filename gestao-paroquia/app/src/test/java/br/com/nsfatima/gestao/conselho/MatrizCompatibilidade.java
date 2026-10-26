package br.com.nsfatima.gestao.conselho;

import br.com.nsfatima.gestao.conselho.domain.model.TipoReuniao;
import br.com.nsfatima.gestao.organizacao.domain.model.SubtipoConselho;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Matriz esperada de quem pode convocar cada tipo de reunião, escrita à mão e independente do código de produção.
 * Serve de referência única para o teste do domínio e para o do gatilho do banco, que precisam concordar.
 */
public final class MatrizCompatibilidade {

    /** Órgão possível: tipo e, se for conselho, o subtipo. */
    public record Orgao(TipoOrganizacao tipo, SubtipoConselho subtipo) {
        @Override
        public String toString() {
            return subtipo == null ? tipo.name() : tipo + "/" + subtipo;
        }
    }

    private static final Orgao CPP = new Orgao(TipoOrganizacao.CONSELHO, SubtipoConselho.CPP);
    private static final Orgao CPC = new Orgao(TipoOrganizacao.CONSELHO, SubtipoConselho.CPC);
    private static final Orgao CAEP = new Orgao(TipoOrganizacao.CONSELHO, SubtipoConselho.CAEP);
    private static final Orgao PASTORAL = new Orgao(TipoOrganizacao.PASTORAL, null);
    private static final Orgao MOVIMENTO = new Orgao(TipoOrganizacao.MOVIMENTO, null);
    private static final Orgao SERVICO = new Orgao(TipoOrganizacao.SERVICO_PAROQUIAL, null);

    /** Todos os órgãos que existem no sistema. */
    public static final List<Orgao> TODOS = List.of(CPP, CPC, CAEP, PASTORAL, MOVIMENTO, SERVICO);

    private static final Map<TipoReuniao, Set<Orgao>> CONVOCANTES = Map.ofEntries(
            Map.entry(TipoReuniao.REUNIAO_CPP, Set.of(CPP)),
            Map.entry(TipoReuniao.REUNIAO_CPC, Set.of(CPC)),
            Map.entry(TipoReuniao.REUNIAO_CAEP, Set.of(CAEP)),
            Map.entry(TipoReuniao.REUNIAO_CONJUNTA_CPP_CAEP, Set.of(CPP)),
            Map.entry(TipoReuniao.REUNIAO_PASTORAL, Set.of(CPP, CPC)),
            Map.entry(TipoReuniao.REUNIAO_COORDENADORES, Set.of(CPP, CPC)),
            Map.entry(TipoReuniao.REUNIAO_ANIMADORES, Set.of(CPP, CPC)),
            Map.entry(TipoReuniao.ASSEMBLEIA_PAROQUIAL, Set.of(CPP)),
            Map.entry(TipoReuniao.ASSEMBLEIA_COMUNITARIA, Set.of(CPC)),
            Map.entry(TipoReuniao.PLANEJAMENTO_PASTORAL, Set.of(CPP)),
            Map.entry(TipoReuniao.REUNIAO_ORGANISMO, Set.of(PASTORAL, MOVIMENTO, SERVICO)),
            Map.entry(TipoReuniao.COMISSAO_ESPECIAL, Set.copyOf(TODOS)));

    private MatrizCompatibilidade() {
    }

    /** Indica se o órgão pode convocar o tipo de reunião. */
    public static boolean pode(TipoReuniao tipo, Orgao orgao) {
        return CONVOCANTES.get(tipo).contains(orgao);
    }
}
