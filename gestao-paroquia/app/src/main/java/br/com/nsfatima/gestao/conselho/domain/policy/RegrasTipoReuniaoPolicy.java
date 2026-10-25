package br.com.nsfatima.gestao.conselho.domain.policy;

import br.com.nsfatima.gestao.conselho.domain.model.ModoPresenca;
import br.com.nsfatima.gestao.conselho.domain.model.TipoReuniao;
import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import br.com.nsfatima.gestao.organizacao.domain.model.SubtipoConselho;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;

import java.util.Set;

/**
 * Tabela de regras por tipo de reunião (Strategy sem hierarquia de classes: a variação é só de dados).
 * Stateless e zero-I/O. Concentra a variação por tipo para que nenhum caso de uso decida por
 * {@code if/else} do tipo. Os colegiados (CPP, CPC, CAEP) exigem homologação, aplicam quórum e votam em
 * caráter consultivo (Cân. 536 §2 e 537); os demais tipos refletem a prática pastoral.
 */
public final class RegrasTipoReuniaoPolicy {

    private static final Set<TipoOrganizacao> SO_CONSELHO = Set.of(TipoOrganizacao.CONSELHO);
    private static final Set<SubtipoConselho> QUALQUER_SUBTIPO = Set.of();
    private static final Set<SubtipoConselho> PASTORAIS = Set.of(SubtipoConselho.CPP, SubtipoConselho.CPC);
    private static final Set<PapelOrganizacional> SECRETARIO = Set.of(PapelOrganizacional.SECRETARIO_CONSELHO);
    private static final Set<PapelOrganizacional> SECRETARIO_OU_COORDENACAO = Set.of(
            PapelOrganizacional.SECRETARIO_GRUPO, PapelOrganizacional.COORDENADOR);

    private RegrasTipoReuniaoPolicy() {
    }

    /**
     * Regras do tipo informado.
     *
     * @param tipo tipo de reunião
     * @return as exigências do tipo
     */
    public static RegrasTipoReuniao para(TipoReuniao tipo) {
        return switch (tipo) {
            case REUNIAO_CPP -> colegiado(Set.of(SubtipoConselho.CPP));
            case REUNIAO_CPC -> colegiado(Set.of(SubtipoConselho.CPC));
            case REUNIAO_CAEP -> colegiado(Set.of(SubtipoConselho.CAEP));
            case REUNIAO_CONJUNTA_CPP_CAEP -> new RegrasTipoReuniao(SO_CONSELHO, Set.of(SubtipoConselho.CPP),
                    true, true, true, ModoPresenca.NOMINAL_MEMBROS, true, SECRETARIO);
            case REUNIAO_PASTORAL -> new RegrasTipoReuniao(SO_CONSELHO, PASTORAIS,
                    false, true, false, ModoPresenca.NOMINAL_LIVRE, true, SECRETARIO);
            case REUNIAO_COORDENADORES, REUNIAO_ANIMADORES -> new RegrasTipoReuniao(SO_CONSELHO, PASTORAIS,
                    false, false, false, ModoPresenca.NOMINAL_LIVRE, false, SECRETARIO);
            case ASSEMBLEIA_PAROQUIAL -> new RegrasTipoReuniao(SO_CONSELHO, Set.of(SubtipoConselho.CPP),
                    false, true, false, ModoPresenca.QUANTITATIVA, false, SECRETARIO);
            case ASSEMBLEIA_COMUNITARIA -> new RegrasTipoReuniao(SO_CONSELHO, Set.of(SubtipoConselho.CPC),
                    false, false, false, ModoPresenca.QUANTITATIVA, false, SECRETARIO);
            case REUNIAO_ORGANISMO -> new RegrasTipoReuniao(
                    Set.of(TipoOrganizacao.PASTORAL, TipoOrganizacao.MOVIMENTO, TipoOrganizacao.SERVICO_PAROQUIAL),
                    QUALQUER_SUBTIPO, false, false, false, ModoPresenca.NOMINAL_MEMBROS, false,
                    SECRETARIO_OU_COORDENACAO);
            case PLANEJAMENTO_PASTORAL -> new RegrasTipoReuniao(SO_CONSELHO, Set.of(SubtipoConselho.CPP),
                    false, true, false, ModoPresenca.NOMINAL_LIVRE, false, SECRETARIO);
            case COMISSAO_ESPECIAL -> new RegrasTipoReuniao(Set.of(TipoOrganizacao.values()), QUALQUER_SUBTIPO,
                    false, false, false, ModoPresenca.NOMINAL_LIVRE, false, redatoresDeComissao());
        };
    }

    private static RegrasTipoReuniao colegiado(Set<SubtipoConselho> subtipos) {
        return new RegrasTipoReuniao(SO_CONSELHO, subtipos, false, true, true,
                ModoPresenca.NOMINAL_MEMBROS, true, SECRETARIO);
    }

    private static Set<PapelOrganizacional> redatoresDeComissao() {
        return Set.of(PapelOrganizacional.SECRETARIO_GRUPO, PapelOrganizacional.COORDENADOR,
                PapelOrganizacional.SECRETARIO_CONSELHO);
    }
}
