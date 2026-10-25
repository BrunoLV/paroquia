package br.com.nsfatima.gestao.conselho.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A interface oferece as funções de presença numa lista própria ({@code FUNCOES_PRESENCA}, em {@code ata.ts}).
 * O servidor é quem valida, então a lista da interface precisa ser exatamente o catálogo do domínio, na mesma
 * ordem e com a mesma marcação de função única.
 */
@DisplayName("Catálogo de funções de presença na interface")
class FuncaoPresencaCatalogoDaInterfaceTest {

    private static final Path ATA_TS = Path.of("../../gestao-paroquia-ui/src/contextos/conselho/domain/ata.ts");
    private static final Pattern FUNCAO = Pattern.compile("\\{ rotulo: '([^']+)', unica: (true|false) }");

    @Test
    @DisplayName("a lista da interface é igual ao catálogo do domínio")
    void listaDaInterfaceIgualAoDominio() throws IOException {
        List<String> daInterface = new ArrayList<>();
        Matcher m = FUNCAO.matcher(Files.readString(ATA_TS));
        while (m.find()) {
            daInterface.add(m.group(1) + " / unica=" + m.group(2));
        }

        List<String> doDominio = Arrays.stream(FuncaoPresenca.values())
                .map(f -> f.getRotulo() + " / unica=" + f.ehUnica()).toList();

        assertThat(daInterface).containsExactlyElementsOf(doDominio);
    }
}
