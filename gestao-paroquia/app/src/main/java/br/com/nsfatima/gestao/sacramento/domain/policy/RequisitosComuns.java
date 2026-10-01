package br.com.nsfatima.gestao.sacramento.domain.policy;

import br.com.nsfatima.gestao.sacramento.domain.model.PapelParticipante;
import br.com.nsfatima.gestao.sacramento.domain.model.Participante;
import br.com.nsfatima.gestao.sacramento.domain.model.TeorAssento;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Verificações compartilhadas pelas policies por sacramento: metadados universais (spec §2),
 * contagem de papéis e elegibilidade de padrinhos (Cân. 874).
 */
final class RequisitosComuns {

    static final int IDADE_MINIMA_PADRINHO = 16;
    private static final Set<String> MINISTROS = Set.of("BISPO", "PRESBITERO", "DIACONO");

    private RequisitosComuns() {
    }

    static List<String> metadadosUniversais(TeorAssento teor) {
        List<String> pendencias = new ArrayList<>();
        if (!teor.referenciaLivro().completa()) {
            pendencias.add("Livro, folha e termo são obrigatórios");
        }
        exigir(pendencias, teor.paroquiaCustodiante(), "Paróquia custodiante é obrigatória");
        exigir(pendencias, teor.localCelebracao(), "Local da celebração é obrigatório");
        if (teor.dataCelebracao() == null) {
            pendencias.add("Data e hora da celebração são obrigatórias");
        }
        return pendencias;
    }

    static void exigir(List<String> pendencias, String valor, String mensagem) {
        if (valor == null || valor.isBlank()) {
            pendencias.add(mensagem);
        }
    }

    static long contar(TeorAssento teor, PapelParticipante... papeis) {
        Set<PapelParticipante> alvo = EnumSet.copyOf(List.of(papeis));
        return teor.participantes().stream().filter(p -> alvo.contains(p.papel())).count();
    }

    static void exigirMinimo(List<String> pendencias, TeorAssento teor, long minimo, String descricao,
                             PapelParticipante... papeis) {
        if (contar(teor, papeis) < minimo) {
            pendencias.add(descricao);
        }
    }

    static void exigirMinistro(List<String> pendencias, TeorAssento teor, PapelParticipante papel, String descricao) {
        List<Participante> ministros = teor.participantes().stream().filter(p -> p.papel() == papel).toList();
        if (ministros.isEmpty()) {
            pendencias.add(descricao);
        }
        ministros.stream()
                .filter(p -> p.condicaoCanonica() == null || !MINISTROS.contains(p.condicaoCanonica()))
                .forEach(p -> pendencias.add("Condição canônica do ministro deve ser BISPO, PRESBITERO ou DIACONO"));
    }

    static void exigirDado(List<String> pendencias, TeorAssento teor, String chave, String descricao) {
        exigir(pendencias, teor.dadosEspecificos().get(chave), descricao);
    }

    /** Padrinho canônico: católico, crismado e com ao menos 16 anos; não-católico só como testemunha. */
    static void elegibilidadePadrinhos(List<String> pendencias, TeorAssento teor) {
        for (Participante p : teor.participantes()) {
            if (p.papel() != PapelParticipante.PADRINHO && p.papel() != PapelParticipante.MADRINHA) {
                continue;
            }
            if (!Boolean.TRUE.equals(p.catolico())) {
                pendencias.add("Padrinho não católico deve constar como testemunha (Cân. 874 §2): "
                        + p.nomeHistorico());
            } else if (!Boolean.TRUE.equals(p.crismado())) {
                pendencias.add("Padrinho deve ser crismado (Cân. 874 §1): " + p.nomeHistorico());
            }
            if (p.idadeNaCelebracao() == null || p.idadeNaCelebracao() < IDADE_MINIMA_PADRINHO) {
                pendencias.add("Padrinho deve ter ao menos 16 anos (Cân. 874 §1): " + p.nomeHistorico());
            }
        }
    }
}
