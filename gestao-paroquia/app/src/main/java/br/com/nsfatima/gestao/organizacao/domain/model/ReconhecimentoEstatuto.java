package br.com.nsfatima.gestao.organizacao.domain.model;

import java.time.LocalDate;

/**
 * Reconhecimento formal dos estatutos de um movimento apostólico ou associação de fiéis pela autoridade
 * eclesiástica competente (Cân. 299 e 304). Sem ele, o movimento não pode atuar como organização da paróquia.
 *
 * @param autoridade autoridade que reconheceu os estatutos (ex.: Bispo diocesano, Santa Sé)
 * @param data data do reconhecimento; não pode ser futura
 * @param referencia número do decreto, documento ou protocolo que formaliza o reconhecimento
 */
public record ReconhecimentoEstatuto(String autoridade, LocalDate data, String referencia) {

    public ReconhecimentoEstatuto {
        if (autoridade == null || autoridade.isBlank()) {
            throw new IllegalArgumentException("Autoridade que reconheceu o estatuto é obrigatória.");
        }
        if (data == null || data.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "Data do reconhecimento do estatuto é obrigatória e não pode ser futura.");
        }
        autoridade = autoridade.trim();
        referencia = referencia == null || referencia.isBlank() ? null : referencia.trim();
    }
}
