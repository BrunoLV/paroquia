package br.com.nsfatima.gestao.architecture;

import br.com.nsfatima.gestao.support.infrastructure.persistence.entity.BaseAuditableEntity;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.core.domain.JavaCall.Predicates.target;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.assignableTo;
import static com.tngtech.archunit.core.domain.properties.HasName.Predicates.nameMatching;
import static com.tngtech.archunit.core.domain.properties.HasOwner.Predicates.With.owner;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Validação arquitetural automatizada de Entidades JPA e Padrão de Auditoria.
 *
 * <p>Invariantes Mandatórias:
 * 1. Todas as entidades JPA mapeadas ({@code @Entity}) residem exclusivamente em {@code infrastructure.persistence.entity}.
 * 2. Modelo Único de Auditoria: Todas as entidades JPA estendem {@link BaseAuditableEntity} para auditoria automática.
 * 3. A camada de domínio é estritamente livre de anotações de persistência relacional.
 * 4. Os campos de auditoria (criado/atualizado em/por) são preenchidos só pelo listener do Spring Data:
 *    nenhuma classe fora das próprias entidades chama os setters de {@link BaseAuditableEntity}.
 */
@AnalyzeClasses(packages = "br.com.nsfatima.gestao", importOptions = {ImportOption.DoNotIncludeTests.class})
public class JpaEntityArchitectureTest {

    private JpaEntityArchitectureTest() {
    }

    @ArchTest
    public static final ArchRule entidades_jpa_devem_residir_em_pacote_de_persistencia =
            classes()
                    .that().areAnnotatedWith("jakarta.persistence.Entity")
                    .should().resideInAPackage("..infrastructure.persistence.entity..");

    @ArchTest
    public static final ArchRule entidades_jpa_devem_estender_base_auditable_entity =
            classes()
                    .that().areAnnotatedWith("jakarta.persistence.Entity")
                    .should().beAssignableTo(BaseAuditableEntity.class);

    @ArchTest
    public static final ArchRule dominio_nao_deve_conter_anotacoes_de_entidade_jpa =
            noClasses()
                    .that().resideInAPackage("..domain..")
                    .should().beAnnotatedWith("jakarta.persistence.Entity")
                    .orShould().beAnnotatedWith("jakarta.persistence.Table");

    @ArchTest
    public static final ArchRule auditoria_so_pode_ser_preenchida_pelo_listener =
            noClasses()
                    .that().areNotAssignableTo(BaseAuditableEntity.class)
                    .should().callMethodWhere(
                            target(nameMatching("set(CriadoEmUtc|AtualizadoEmUtc|CriadoPor|AtualizadoPor"
                                    + "|CreatedAt|UpdatedAt)"))
                                    .and(target(owner(assignableTo(BaseAuditableEntity.class)))));
}
