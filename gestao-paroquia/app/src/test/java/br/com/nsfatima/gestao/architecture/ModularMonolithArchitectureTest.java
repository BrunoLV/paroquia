package br.com.nsfatima.gestao.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Validação arquitetural automatizada do Monólito Modular e Encapsulamento Inter-Domínios.
 *
 * <p>Invariantes Mandatórias:
 * 1. A camada de domínio de qualquer bounded context não pode acessar infraestruturas de outros bounded contexts.
 * 2. As entidades JPA de persistência de um bounded context são privadas a ele, não podendo ser consumidas
 *    diretamente pelo domínio de outro contexto delimitado.
 */
@AnalyzeClasses(packages = "br.com.nsfatima.gestao", importOptions = {ImportOption.DoNotIncludeTests.class})
public class ModularMonolithArchitectureTest {

    private ModularMonolithArchitectureTest() {
    }

    @ArchTest
    public static final ArchRule dominio_nao_deve_acessar_persistencia_de_outros_contextos =
            noClasses()
                    .that().resideInAPackage("br.com.nsfatima.gestao.(*).domain..")
                    .should().dependOnClassesThat()
                    .resideInAPackage("br.com.nsfatima.gestao.(*).infrastructure.persistence..");

    @ArchTest
    public static final ArchRule aplicacao_nao_deve_acessar_entidades_jpa_de_outros_contextos =
            noClasses()
                    .that().resideInAPackage("br.com.nsfatima.gestao.(*).application..")
                    .should().dependOnClassesThat()
                    .resideInAPackage("br.com.nsfatima.gestao.(*).infrastructure.persistence.entity..");
}
