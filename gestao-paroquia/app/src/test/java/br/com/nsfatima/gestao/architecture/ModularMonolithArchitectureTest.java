package br.com.nsfatima.gestao.architecture;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import com.tngtech.archunit.library.freeze.FreezingArchRule;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Validação arquitetural automatizada do Monólito Modular e Encapsulamento Inter-Domínios.
 *
 * <p>Invariantes Mandatórias:
 * 1. A camada de domínio de qualquer bounded context não acessa a persistência de nenhum contexto.
 * 2. Um contexto não acessa as camadas {@code application} e {@code infrastructure} de outro contexto;
 *    a colaboração ocorre por portas, eventos ou camada anticorrupção (as entidades JPA ficam em
 *    {@code infrastructure.persistence} e, portanto, também são privadas ao contexto dono). O pacote
 *    {@code support} é o núcleo compartilhado (shared kernel) e é a única exceção.
 * 3. Os contextos não dependem mutuamente uns dos outros (ciclos de dois nós).
 *
 * <p>As regras 2 e 3 têm legado conhecido e são congeladas com {@link FreezingArchRule}: as violações
 * existentes ficam registradas em {@code src/test/resources/archunit_store} e qualquer violação nova
 * falha a suíte. Ao corrigir um acoplamento legado, o registro encolhe automaticamente.
 */
@AnalyzeClasses(packages = "br.com.nsfatima.gestao", importOptions = {ImportOption.DoNotIncludeTests.class})
public class ModularMonolithArchitectureTest {

    private static final String RAIZ = "br.com.nsfatima.gestao.";
    private static final String NUCLEO_COMPARTILHADO = "support";
    private static final Set<String> CAMADAS_PRIVADAS = Set.of("application", "infrastructure");

    private ModularMonolithArchitectureTest() {
    }

    @ArchTest
    public static final ArchRule dominio_nao_deve_acessar_persistencia_de_outros_contextos =
            noClasses()
                    .that().resideInAPackage("br.com.nsfatima.gestao.(*).domain..")
                    .should().dependOnClassesThat()
                    .resideInAPackage("br.com.nsfatima.gestao.(*).infrastructure.persistence..");

    @ArchTest
    public static final ArchRule contexto_nao_deve_acessar_application_ou_infrastructure_de_outro =
            FreezingArchRule.freeze(
                    classes()
                            .that(new ContextoPredicado())
                            .should(naoAcessarCamadasPrivadasDeOutroContexto())
                            .as("contexto nao deve acessar application ou infrastructure de outro contexto"));

    @ArchTest
    public static final ArchRule contextos_nao_devem_depender_mutuamente =
            FreezingArchRule.freeze(
                    classes()
                            .that(new ContextoPredicado())
                            .should(naoDependerMutuamenteDeOutroContexto())
                            .as("contextos nao devem depender mutuamente uns dos outros"));

    /**
     * Detecta dependência mútua (ciclo de dois nós) entre contextos, com um achado por par. O detector
     * genérico de ciclos do ArchUnit trunca o relatório (o grafo atual já é uma única componente
     * fortemente conexa), o que tornaria o congelamento instável; o par mútuo é determinístico e impede
     * que novos acoplamentos bidirecionais entrem.
     */
    private static ArchCondition<JavaClass> naoDependerMutuamenteDeOutroContexto() {
        return new ArchCondition<>("nao depender mutuamente de outro contexto") {
            private final Set<String> arestas = new HashSet<>();

            @Override
            public void init(Collection<JavaClass> classes) {
                arestas.clear();
            }

            @Override
            public void check(JavaClass origem, ConditionEvents eventos) {
                String contextoOrigem = contexto(origem).orElseThrow();
                for (Dependency dependencia : origem.getDirectDependenciesFromSelf()) {
                    contexto(dependencia.getTargetClass())
                            .filter(alvo -> !alvo.equals(contextoOrigem) && !NUCLEO_COMPARTILHADO.equals(alvo))
                            .ifPresent(alvo -> arestas.add(contextoOrigem + ">" + alvo));
                }
            }

            @Override
            public void finish(ConditionEvents eventos) {
                Set<String> pares = new TreeSet<>();
                for (String aresta : arestas) {
                    String[] extremos = aresta.split(">");
                    if (arestas.contains(extremos[1] + ">" + extremos[0]) && extremos[0].compareTo(extremos[1]) < 0) {
                        pares.add(extremos[0] + " <-> " + extremos[1]);
                    }
                }
                pares.forEach(par -> eventos.add(
                        SimpleConditionEvent.violated(par, "Contextos em dependência mútua: " + par)));
            }
        };
    }

    private static ArchCondition<JavaClass> naoAcessarCamadasPrivadasDeOutroContexto() {
        return new ArchCondition<>("nao acessar application nem infrastructure de outro contexto") {
            @Override
            public void check(JavaClass origem, ConditionEvents eventos) {
                verificarDependencias(origem, eventos, ModularMonolithArchitectureTest::ehCamadaPrivada);
            }
        };
    }

    private static void verificarDependencias(
            JavaClass origem, ConditionEvents eventos, Predicate<JavaClass> alvoProibido) {
        String contextoOrigem = contexto(origem).orElseThrow();
        for (Dependency dependencia : origem.getDirectDependenciesFromSelf()) {
            JavaClass alvo = dependencia.getTargetClass();
            Optional<String> contextoAlvo = contexto(alvo);
            boolean outroContexto = contextoAlvo.isPresent()
                    && !contextoAlvo.get().equals(contextoOrigem)
                    && !NUCLEO_COMPARTILHADO.equals(contextoAlvo.get());
            if (outroContexto && alvoProibido.test(alvo)) {
                eventos.add(SimpleConditionEvent.violated(dependencia, dependencia.getDescription()));
            }
        }
    }

    private static boolean ehCamadaPrivada(JavaClass alvo) {
        return camada(alvo).filter(CAMADAS_PRIVADAS::contains).isPresent();
    }

    private static Optional<String> contexto(JavaClass classe) {
        return segmentos(classe, 0);
    }

    private static Optional<String> camada(JavaClass classe) {
        return segmentos(classe, 1);
    }

    private static Optional<String> segmentos(JavaClass classe, int indice) {
        String pacote = classe.getPackageName();
        if (!pacote.startsWith(RAIZ)) {
            return Optional.empty();
        }
        String[] partes = pacote.substring(RAIZ.length()).split("\\.");
        return partes.length > indice ? Optional.of(partes[indice]) : Optional.empty();
    }

    /** Seleciona as classes que pertencem a algum bounded context (pacote {@code gestao.<contexto>.<camada>}). */
    private static final class ContextoPredicado extends DescribedPredicate<JavaClass> {

        private ContextoPredicado() {
            super("pertencem a um bounded context");
        }

        @Override
        public boolean test(JavaClass classe) {
            return contexto(classe).isPresent() && camada(classe).isPresent();
        }
    }
}
