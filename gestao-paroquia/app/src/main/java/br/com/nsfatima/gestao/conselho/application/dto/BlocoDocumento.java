package br.com.nsfatima.gestao.conselho.application.dto;

import java.util.List;

/**
 * Elemento neutro de um documento. A composição da ata gera uma lista destes blocos uma única vez, e cada
 * exportador (PDF, Word) apenas os desenha, de modo que o conteúdo não diverge entre formatos.
 */
public sealed interface BlocoDocumento {

    /** Título de seção; {@code nivel} 1 é o título do documento. */
    record Titulo(String texto, int nivel) implements BlocoDocumento {
    }

    /** Parágrafo de texto corrido. */
    record Paragrafo(String texto) implements BlocoDocumento {
    }

    /** Aviso em destaque, como a marca de minuta. */
    record Destaque(String texto) implements BlocoDocumento {
    }

    /** Tabela; sem cabeçalho quando {@code cabecalho} é vazio (quadro de campos). */
    record Tabela(List<String> cabecalho, List<List<String>> linhas) implements BlocoDocumento {

        public Tabela {
            cabecalho = List.copyOf(cabecalho);
            linhas = linhas.stream().map(List::copyOf).toList();
        }
    }

    /** Linhas de assinatura, uma por rótulo (por exemplo, "Redator" e "Pároco"). */
    record Assinaturas(List<String> rotulos) implements BlocoDocumento {

        public Assinaturas {
            rotulos = List.copyOf(rotulos);
        }
    }

    /** Quebra de página, usada entre atas de uma compilação. */
    record QuebraDePagina() implements BlocoDocumento {
    }
}
