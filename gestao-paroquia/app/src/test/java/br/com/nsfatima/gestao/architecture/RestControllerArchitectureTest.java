package br.com.nsfatima.gestao.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Validação arquitetural automatizada de Controllers REST e Tratamento de Exceções.
 *
 * <p>Invariantes Mandatórias:
 * 1. Todos os controllers REST ({@code @RestController}) residem na camada {@code api}.
 * 2. Zero {@code @ExceptionHandler} inline em controllers; o tratamento é centralizado em {@code @RestControllerAdvice}.
 * 3. Controllers não devem depender diretamente de entidades JPA de persistência.
 */
@AnalyzeClasses(packages = "br.com.nsfatima.gestao", importOptions = {ImportOption.DoNotIncludeTests.class})
public class RestControllerArchitectureTest {

    private RestControllerArchitectureTest() {
    }

    @ArchTest
    public static final ArchRule rest_controllers_devem_residir_em_pacote_api =
            classes()
                    .that().areAnnotatedWith("org.springframework.web.bind.annotation.RestController")
                    .should().resideInAPackage("..api..");

    @ArchTest
    public static final ArchRule controllers_nao_devem_conter_exception_handler_inline =
            methods()
                    .that().areAnnotatedWith("org.springframework.web.bind.annotation.ExceptionHandler")
                    .should().beDeclaredInClassesThat()
                    .areAnnotatedWith("org.springframework.web.bind.annotation.RestControllerAdvice")
                    .orShould().beDeclaredInClassesThat()
                    .areAnnotatedWith("org.springframework.web.bind.annotation.ControllerAdvice");

    @ArchTest
    public static final ArchRule controllers_nao_devem_depender_de_entidades_jpa_diretamente =
            noClasses()
                    .that().areAnnotatedWith("org.springframework.web.bind.annotation.RestController")
                    .should().dependOnClassesThat()
                    .resideInAPackage("..infrastructure.persistence.entity..");
}
