package br.com.nsfatima.gestao.iam.domain.model;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Conjunto de papéis (authorities) globais de um {@code Usuario}, persistido como uma única string
 * separada por vírgulas (ex.: {@code "ROLE_ADMIN,ROLE_USER"}).
 *
 * <p>Encapsula, num único ponto, a manipulação antes espalhada como {@code roles.contains(...)} +
 * concatenação de string nos casos de uso (ver {@code ApproveUsuarioUseCase}).</p>
 */
public final class Papeis {

    private final Set<String> valores;

    private Papeis(Set<String> valores) {
        this.valores = valores;
    }

    public static Papeis vazio() {
        return new Papeis(new LinkedHashSet<>());
    }

    /** Interpreta uma string CSV persistida; tolera {@code null}, espaços e itens em branco. */
    public static Papeis deCsv(String csv) {
        Set<String> set = new LinkedHashSet<>();
        if (csv != null) {
            Arrays.stream(csv.split(","))
                    .map(String::trim)
                    .filter(p -> !p.isEmpty())
                    .forEach(set::add);
        }
        return new Papeis(set);
    }

    public boolean contem(String papel) {
        return valores.contains(papel);
    }

    public Papeis adicionar(String papel) {
        Objects.requireNonNull(papel, "Papel é obrigatório.");
        Set<String> novo = new LinkedHashSet<>(valores);
        novo.add(papel.trim());
        return new Papeis(novo);
    }

    public Papeis remover(String papel) {
        Set<String> novo = new LinkedHashSet<>(valores);
        novo.remove(papel);
        return new Papeis(novo);
    }

    /** Representação CSV para persistência; {@code null} quando não há nenhum papel. */
    public String paraCsv() {
        return valores.isEmpty() ? null : String.join(",", valores);
    }

    public Set<String> valores() {
        return Set.copyOf(valores);
    }
}
