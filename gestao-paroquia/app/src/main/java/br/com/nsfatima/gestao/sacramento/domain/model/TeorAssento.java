package br.com.nsfatima.gestao.sacramento.domain.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Conteúdo documental do assento: tudo o que se torna somente-leitura ao selar (spec §5.2).
 *
 * @param dadosEspecificos campos próprios do sacramento (ex.: processo de habilitação, grau da ordem,
 *     referência canônica ao batismo na crisma), validados pela policy do tipo
 * @param dadoSensivel marca filiação adotiva/notas reservadas: omitido de certidões simples (LGPD)
 */
public record TeorAssento(
        ReferenciaLivro referenciaLivro,
        String paroquiaCustodiante,
        LocalDateTime dataCelebracao,
        String localCelebracao,
        List<Participante> participantes,
        Map<String, String> dadosEspecificos,
        boolean dadoSensivel) {

    public TeorAssento {
        referenciaLivro = referenciaLivro == null ? ReferenciaLivro.vazia() : referenciaLivro;
        participantes = List.copyOf(Objects.requireNonNullElse(participantes, List.of()));
        dadosEspecificos = Map.copyOf(Objects.requireNonNullElse(dadosEspecificos, Map.of()));
    }
}
