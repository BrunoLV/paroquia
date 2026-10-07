package br.com.nsfatima.gestao.membro.domain.policy;

import br.com.nsfatima.gestao.membro.domain.model.IdoneidadeAgente;
import br.com.nsfatima.gestao.organizacao.domain.model.SetorPastoral;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Discernimento pastoral sobre a idoneidade do agente para a função que assume. Stateless e sem I/O: recebe os
 * fatos e devolve as pendências, sem alterar o vínculo.
 *
 * <p>Regras: quem coordena precisa de formação doutrinária básica, vivência sacramental atestada, termo de
 * proteção de menores vigente e envio (missão) conferido pelo Pároco; membros de organismos do múnus de ensinar
 * (catequese e formação) precisam de vivência sacramental atestada e termo de proteção de menores; membros do
 * múnus de santificar (liturgia) precisam de vivência sacramental atestada.</p>
 */
public final class PoliticaIdoneidadeFuncao {

    private PoliticaIdoneidadeFuncao() {
    }

    /**
     * Lista as pendências de idoneidade do agente para a função.
     *
     * @param coordenacao {@code true} se a função é de coordenação (coordenador ou vice)
     * @param setorPastoral múnus a que a organização serve, ou {@code null}
     * @param idoneidade registro de idoneidade do agente
     * @param hoje data de referência para a vigência do termo
     * @return pendências; vazia quando o agente é apto
     */
    public static List<String> pendencias(boolean coordenacao, SetorPastoral setorPastoral,
                                          IdoneidadeAgente idoneidade, LocalDate hoje) {
        List<String> pendencias = new ArrayList<>();
        boolean ensina = setorPastoral == SetorPastoral.FORMACAO_ENSINAR;
        boolean santifica = setorPastoral == SetorPastoral.SANTIFICACAO_LITURGIA;

        if (coordenacao && idoneidade.formacaoConcluidaEm() == null) {
            pendencias.add("Formação doutrinária básica não registrada.");
        }
        if ((coordenacao || ensina || santifica) && idoneidade.vivenciaSacramentalAtestadaEm() == null) {
            pendencias.add("Vivência sacramental não atestada pelo Pároco.");
        }
        if ((coordenacao || ensina) && !idoneidade.termoVigenteEm(hoje)) {
            pendencias.add("Termo de proteção de menores e vulneráveis ausente ou vencido.");
        }
        if (coordenacao && idoneidade.enviadoEm() == null) {
            pendencias.add("Envio (missão) não conferido pelo Pároco.");
        }
        return pendencias;
    }
}
