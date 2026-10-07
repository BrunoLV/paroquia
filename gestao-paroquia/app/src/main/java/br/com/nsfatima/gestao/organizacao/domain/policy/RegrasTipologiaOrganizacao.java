package br.com.nsfatima.gestao.organizacao.domain.policy;

import br.com.nsfatima.gestao.organizacao.domain.model.Organizacao;
import br.com.nsfatima.gestao.organizacao.domain.model.PastoralMovimentoOficial;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Registro das {@link RegraTipologiaOrganizacao} por tipo, com as estratégias concretas da tipologia eclesial:
 * pastoral orgânica, movimento apostólico (estatuto reconhecido, Cân. 299/304), serviço paroquial e conselho.
 * Pastorais, movimentos e serviços articulam-se obrigatoriamente a um conselho pastoral (CPP/CPC), expressão
 * da comunhão pastoral e da corresponsabilidade (Cân. 536).
 */
public final class RegrasTipologiaOrganizacao {

    private static final Map<TipoOrganizacao, RegraTipologiaOrganizacao> REGRAS = registrar();

    private RegrasTipologiaOrganizacao() {
    }

    /**
     * Avalia o contexto contra a regra do tipo informado.
     *
     * @param tipo tipo da organização
     * @param contexto dados já carregados
     * @return mensagens de violação; vazia quando válido
     */
    public static List<String> avaliar(TipoOrganizacao tipo, ContextoTipologia contexto) {
        return REGRAS.get(tipo).violacoes(contexto);
    }

    private static Map<TipoOrganizacao, RegraTipologiaOrganizacao> registrar() {
        Map<TipoOrganizacao, RegraTipologiaOrganizacao> mapa = new EnumMap<>(TipoOrganizacao.class);
        mapa.put(TipoOrganizacao.PASTORAL, new PastoralOrganica());
        mapa.put(TipoOrganizacao.MOVIMENTO, new MovimentoApostolico());
        mapa.put(TipoOrganizacao.SERVICO_PAROQUIAL, new ServicoParoquial());
        mapa.put(TipoOrganizacao.CONSELHO, new Conselho());
        return mapa;
    }

    private static void exigirConselhoPastoralComoMae(TipoOrganizacao tipo, ContextoTipologia ctx,
                                                      List<String> violacoes) {
        if (!ctx.possuiMae()) {
            violacoes.add(tipo.name() + " deve ter uma organização-mãe.");
            return;
        }
        Organizacao mae = ctx.organizacaoMae();
        if (mae == null) {
            violacoes.add(tipo.name() + " deve ter uma organização-mãe do tipo CONSELHO.");
        } else if (!mae.isAtivo()) {
            violacoes.add("Organização-mãe não está ativa.");
        } else if (mae.getTipo() != TipoOrganizacao.CONSELHO) {
            violacoes.add(tipo.name() + " deve ter uma organização-mãe do tipo CONSELHO.");
        } else if (mae.getSubtipoConselho() != null && !mae.getSubtipoConselho().isPastoral()) {
            violacoes.add(tipo.name() + " deve ser articulado a um conselho pastoral (CPP ou CPC), não ao CAEP.");
        }
    }

    private static void exigirOficialDoTipo(TipoOrganizacao tipo, ContextoTipologia ctx, List<String> violacoes) {
        PastoralMovimentoOficial oficial = ctx.oficial();
        if (oficial == null) {
            violacoes.add("Vínculo com Pastoral ou Movimento Oficial é obrigatório.");
        } else if (oficial.getTipo() != tipo) {
            violacoes.add("O tipo da Pastoral/Movimento Oficial (" + oficial.getTipo()
                    + ") não coincide com o tipo da organização (" + tipo + ").");
        }
    }

    private static void exigirSetor(ContextoTipologia ctx, List<String> violacoes) {
        if (ctx.setorPastoral() == null) {
            violacoes.add("Setor pastoral (ensinar, santificar ou governar) é obrigatório.");
        }
    }

    private static final class PastoralOrganica implements RegraTipologiaOrganizacao {
        @Override
        public TipoOrganizacao tipo() {
            return TipoOrganizacao.PASTORAL;
        }

        @Override
        public List<String> violacoes(ContextoTipologia ctx) {
            List<String> v = new ArrayList<>();
            exigirConselhoPastoralComoMae(tipo(), ctx, v);
            exigirOficialDoTipo(tipo(), ctx, v);
            exigirSetor(ctx, v);
            return v;
        }
    }

    private static final class MovimentoApostolico implements RegraTipologiaOrganizacao {
        @Override
        public TipoOrganizacao tipo() {
            return TipoOrganizacao.MOVIMENTO;
        }

        @Override
        public List<String> violacoes(ContextoTipologia ctx) {
            List<String> v = new ArrayList<>();
            exigirConselhoPastoralComoMae(tipo(), ctx, v);
            exigirOficialDoTipo(tipo(), ctx, v);
            if (ctx.oficial() != null && ctx.oficial().getTipo() == tipo()
                    && !ctx.oficial().possuiEstatutoReconhecido()) {
                v.add("Movimento apostólico exige estatuto reconhecido pela autoridade eclesiástica "
                        + "(Cân. 299 e 304).");
            }
            return v;
        }
    }

    private static final class ServicoParoquial implements RegraTipologiaOrganizacao {
        @Override
        public TipoOrganizacao tipo() {
            return TipoOrganizacao.SERVICO_PAROQUIAL;
        }

        @Override
        public List<String> violacoes(ContextoTipologia ctx) {
            List<String> v = new ArrayList<>();
            exigirConselhoPastoralComoMae(tipo(), ctx, v);
            exigirSetor(ctx, v);
            return v;
        }
    }

    private static final class Conselho implements RegraTipologiaOrganizacao {
        @Override
        public TipoOrganizacao tipo() {
            return TipoOrganizacao.CONSELHO;
        }

        @Override
        public List<String> violacoes(ContextoTipologia ctx) {
            List<String> v = new ArrayList<>();
            if (!ctx.possuiMae()) {
                v.add("CONSELHO deve ter uma organização-mãe.");
            }
            if (ctx.subtipoConselho() == null) {
                v.add("Subtipo do conselho (CPP, CPC ou CAEP) é obrigatório.");
            }
            return v;
        }
    }
}
