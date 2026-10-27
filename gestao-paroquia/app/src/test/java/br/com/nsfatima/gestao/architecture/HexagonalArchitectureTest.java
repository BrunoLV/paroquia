package br.com.nsfatima.gestao.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.freeze.FreezingArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Validação arquitetural automatizada da Arquitetura Hexagonal (Ports & Adapters) e Clean Architecture.
 *
 * <p>Invariantes Mandatórias do Projeto Paróquia:
 * 1. O núcleo de negócio ({@code domain}) é 100% puro Java 25, agnóstico a frameworks e banco de dados.
 * 2. A camada de aplicação ({@code application}) orquestra os casos de uso interagindo apenas com o domínio.
 * 3. Adaptadores de entrada ({@code api}) e saída ({@code infrastructure}) dependem das camadas internas,
 *    nunca o contrário.
 *
 * <p>As regras com legado conhecido (dependências de {@code application}/{@code api} em persistência e as
 * dependências de domínio de calendário em observabilidade) são congeladas com {@code FreezingArchRule}:
 * as violações existentes ficam em {@code src/test/resources/archunit_store} e qualquer violação nova falha.
 */
@AnalyzeClasses(packages = "br.com.nsfatima.gestao", importOptions = {ImportOption.DoNotIncludeTests.class})
public class HexagonalArchitectureTest {

    private HexagonalArchitectureTest() {
    }

    @ArchTest
    public static final ArchRule domain_nao_deve_depender_de_camadas_externas =
            FreezingArchRule.freeze(
                    noClasses()
                            .that().resideInAPackage("..domain..")
                            .should().dependOnClassesThat()
                            .resideInAnyPackage("..application..", "..api..", "..infrastructure..")
                            .as("domain nao deve depender de camadas externas"));

    @ArchTest
    public static final ArchRule domain_nao_deve_depender_de_frameworks_externos =
            noClasses()
                    .that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage(
                            "org.springframework..",
                            "jakarta.persistence..",
                            "org.hibernate..",
                            "com.fasterxml.jackson..",
                            "tools.jackson.."
                    );

    @ArchTest
    public static final ArchRule application_nao_deve_depender_de_api_nem_de_persistencia_concreta =
            FreezingArchRule.freeze(
                    noClasses()
                            .that().resideInAPackage("..application..")
                            .should().dependOnClassesThat()
                            .resideInAnyPackage("..api..", "..infrastructure.persistence..")
                            .as("application nao deve depender de api nem de persistencia concreta"));

    @ArchTest
    public static final ArchRule api_nao_deve_acessar_repositorios_jpa_diretamente =
            FreezingArchRule.freeze(
                    noClasses()
                            .that().resideInAPackage("..api..")
                            .should().dependOnClassesThat()
                            .resideInAPackage("..infrastructure.persistence.repository..")
                            .as("api nao deve acessar repositorios jpa diretamente"));
}
