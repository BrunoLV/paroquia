package br.com.nsfatima.gestao.calendario.infrastructure.export;

import br.com.nsfatima.gestao.calendario.domain.type.CategoriaEvento;

import java.awt.Color;
import java.util.EnumMap;
import java.util.Map;

/**
 * Resolve a cor de destaque de cada {@link CategoriaEvento} para documentos exportados
 * (PDF, PowerPoint), espelhando a paleta usada pelo calendário no frontend
 * ({@code getCategoryColor} em {@code shared/utils/calendarHelpers.ts}).
 */
public final class CorCategoriaEventoResolver {

    private static final Color DEFAULT_COLOR = new Color(0x9E, 0x9E, 0x9E);

    private static final Map<CategoriaEvento, Color> CORES = new EnumMap<>(CategoriaEvento.class);

    static {
        CORES.put(CategoriaEvento.PASTORAL, new Color(0x2E, 0x7D, 0x32));
        CORES.put(CategoriaEvento.SOCIAL, new Color(0xED, 0x6C, 0x02));
        CORES.put(CategoriaEvento.LITURGICO, new Color(0xC5, 0x9B, 0x27));
        CORES.put(CategoriaEvento.ADMINISTRATIVO, new Color(0x61, 0x61, 0x61));
        CORES.put(CategoriaEvento.SACRAMENTAL, new Color(0x02, 0x88, 0xD1));
        CORES.put(CategoriaEvento.FORMATIVO, new Color(0x1A, 0x23, 0x7E));
        CORES.put(CategoriaEvento.ASSISTENCIAL, new Color(0x02, 0x88, 0xD1));
    }

    private CorCategoriaEventoResolver() {
    }

    /**
     * Resolve a cor de destaque associada à categoria informada.
     *
     * @param categoria categoria do evento, pode ser {@code null}
     * @return cor de destaque associada, ou uma cor cinza neutra para categorias sem
     *     mapeamento específico (ex.: {@code SOLENIDADE}, {@code MOVIMENTO})
     */
    public static Color resolve(CategoriaEvento categoria) {
        if (categoria == null) {
            return DEFAULT_COLOR;
        }
        return CORES.getOrDefault(categoria, DEFAULT_COLOR);
    }
}
