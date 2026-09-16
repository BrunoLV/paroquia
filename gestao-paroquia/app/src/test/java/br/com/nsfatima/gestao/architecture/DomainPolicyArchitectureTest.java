package br.com.nsfatima.gestao.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Validação arquitetural automatizada das Políticas de Domínio (Domain Policies - DDD Tático).
 *
 * <p>Invariantes Mandatórias:
 * 1. Toda Policy é estritamente stateless e zero-I/O: não pode injetar repositórios, APIs, banco de dados ou cache.
 * 2. Nenhuma classe de Policy no domínio pode carregar anotações do Spring Framework (ex: {@code @Service}, {@code @Component}).
 * 3. Classes que implementam ou representam regras de política devem residir nos pacotes {@code domain.policy}.
 */
@AnalyzeClasses(packages = "br.com.nsfatima.gestao", importOptions = {ImportOption.DoNotIncludeTests.class})
public class DomainPolicyArchitectureTest {

    private DomainPolicyArchitectureTest() {
    }

    @ArchTest
    public static final ArchRule policies_do_dominio_devem_residir_no_pacote_policy =
            classes()
                    .that().resideInAPackage("..domain..")
                    .and().haveSimpleNameEndingWith("Policy")
                    .should().resideInAPackage("..domain..policy..");

    @ArchTest
    public static final ArchRule policies_do_dominio_nao_devem_ter_anotacoes_de_framework =
            noClasses()
                    .that().resideInAPackage("..domain..policy..")
                    .should().beAnnotatedWith("org.springframework.stereotype.Service")
                    .orShould().beAnnotatedWith("org.springframework.stereotype.Component")
                    .orShould().beAnnotatedWith("org.springframework.stereotype.Repository")
                    .orShould().beAnnotatedWith("org.springframework.transaction.annotation.Transactional");

    @ArchTest
    public static final ArchRule policies_do_dominio_nao_devem_depender_de_infraestrutura =
            noClasses()
                    .that().resideInAPackage("..domain..policy..")
                    .should().dependOnClassesThat()
                    .resideInAPackage("..infrastructure..");
}
