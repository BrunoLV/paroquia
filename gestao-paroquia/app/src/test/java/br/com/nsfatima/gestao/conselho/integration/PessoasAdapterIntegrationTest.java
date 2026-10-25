package br.com.nsfatima.gestao.conselho.integration;

import br.com.nsfatima.gestao.conselho.infrastructure.acl.PessoasAdapter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Resolver o nome de quem não é fiel (o clero, por exemplo) acontece dentro da transação do caso de uso. A busca
 * do fiel não pode lançar exceção ao não encontrar: uma exceção que atravessa um método transacional marca a
 * transação inteira como "rollback-only" e o commit do caso de uso falha. A classe não é {@code @Transactional}
 * de propósito, para que o commit aconteça de verdade.
 */
@SpringBootTest
@DisplayName("Nomes das pessoas citadas na ata")
class PessoasAdapterIntegrationTest {

    @Autowired
    private PessoasAdapter pessoas;
    @Autowired
    private PlatformTransactionManager transactionManager;

    @Test
    @DisplayName("id que não é fiel não deixa a transação do chamador marcada para rollback")
    void idQueNaoEFielNaoMarcaRollback() {
        Map<UUID, String> nomes = new TransactionTemplate(transactionManager)
                .execute(status -> pessoas.nomes(List.of(UUID.randomUUID())));

        assertThat(nomes).isEmpty();
    }
}
