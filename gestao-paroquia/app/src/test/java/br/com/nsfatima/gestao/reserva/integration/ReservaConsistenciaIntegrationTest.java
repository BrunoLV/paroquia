package br.com.nsfatima.gestao.reserva.integration;

import br.com.nsfatima.gestao.local.application.service.LocalService;
import br.com.nsfatima.gestao.local.domain.exception.LocalNegocioException;
import br.com.nsfatima.gestao.reserva.application.usecase.CriarReservaUseCase;
import br.com.nsfatima.gestao.reserva.application.usecase.CriarReservaUseCase.ComandoCriarReserva;
import br.com.nsfatima.gestao.reserva.domain.exception.ReservaConflitoException;
import br.com.nsfatima.gestao.reserva.domain.exception.ReservaOperacaoInvalidaException;
import br.com.nsfatima.gestao.reserva.domain.model.TipoItemReserva;
import br.com.nsfatima.gestao.reserva.domain.service.ReservaAuthorizationService.UsuarioActor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Garantias de consistência da criação de reservas contra um PostgreSQL real (Testcontainers): serialização de
 * pedidos concorrentes pelo bloqueio do item e validação do item reservado. Não é transacional: cada thread
 * precisa de sua própria transação confirmada para que o cenário de concorrência seja verdadeiro.
 */
@SpringBootTest
@ActiveProfiles("test")
class ReservaConsistenciaIntegrationTest {

    @Autowired private CriarReservaUseCase criarReservaUseCase;
    @Autowired private LocalService localService;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private DataSource dataSource;

    private UUID usuarioId;
    private UUID organizacaoId;
    private UUID localId;
    private UsuarioActor administrador;

    @BeforeEach
    void criarFixtures() throws SQLException {
        usuarioId = UUID.randomUUID();
        organizacaoId = UUID.randomUUID();
        localId = UUID.randomUUID();
        administrador = new UsuarioActor(usuarioId, "admin-it", List.of("ROLE_ADMIN"), List.of(), List.of());
        try (Connection c = dataSource.getConnection(); Statement s = c.createStatement()) {
            // Sem verificação de FK apenas para montar o cenário (clérigo/fiel/unidade fora do escopo do teste).
            s.execute("SET session_replication_role = replica");
            s.execute("INSERT INTO calendario.usuarios (id, username, password_hash, clerigo_id) VALUES ('"
                    + usuarioId + "', 'it-" + usuarioId + "', 'x', '" + UUID.randomUUID() + "')");
            s.execute("INSERT INTO calendario.organizacao (id, nome) VALUES ('" + organizacaoId + "', 'Pastoral IT')");
            s.execute("INSERT INTO calendario.locais (id, nome, tipo, ativo) VALUES ('"
                    + localId + "', 'Salão IT', 'SALAO_PAROQUIAL', true)");
            s.execute("SET session_replication_role = DEFAULT");
        }
    }

    @AfterEach
    void limpar() {
        jdbc.update("DELETE FROM calendario.reservas WHERE solicitante_id = ?", usuarioId);
        jdbc.update("DELETE FROM calendario.locais WHERE id = ?", localId);
        jdbc.update("DELETE FROM calendario.ativos_patrimoniais WHERE nome LIKE 'Ativo IT %'");
        jdbc.update("DELETE FROM calendario.organizacao WHERE id = ?", organizacaoId);
        jdbc.update("DELETE FROM calendario.usuarios WHERE id = ?", usuarioId);
    }

    private ComandoCriarReserva comando(TipoItemReserva tipo, UUID itemId, Instant inicio) {
        return new ComandoCriarReserva(tipo, itemId, null, inicio, inicio.plus(Duration.ofHours(2)), 30,
                "Reunião de pastoral", organizacaoId);
    }

    private UUID criarAtivo(boolean reservavel, int numeroTombamento) throws SQLException {
        UUID id = UUID.randomUUID();
        try (Connection c = dataSource.getConnection(); Statement s = c.createStatement()) {
            s.execute("SET session_replication_role = replica");
            s.execute("INSERT INTO calendario.ativos_patrimoniais (id, nome, codigo_tombamento, categoria, "
                    + "is_reservavel, organizacao_id, data_aquisicao, valor_aquisicao, criado_em_utc, "
                    + "atualizado_em_utc) VALUES ('" + id + "', 'Ativo IT " + id + "', 'PAT-2026-"
                    + numeroTombamento + "', 'ELETRONICO', " + reservavel + ", '" + organizacaoId
                    + "', CURRENT_DATE, 10, now(), now())");
            s.execute("SET session_replication_role = DEFAULT");
        }
        return id;
    }

    @Test
    @DisplayName("Dois pedidos simultâneos para o mesmo local e horário: um é criado e o outro recebe conflito")
    void pedidosConcorrentesNaoDuplicamReserva() throws Exception {
        Instant inicio = Instant.now().plus(Duration.ofDays(30));
        CountDownLatch largada = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            List<Future<Boolean>> resultados = new ArrayList<>();
            for (int i = 0; i < 2; i++) {
                Callable<Boolean> pedido = () -> {
                    largada.await();
                    try {
                        criarReservaUseCase.executar(comando(TipoItemReserva.LOCAL, localId, inicio), administrador);
                        return true;
                    } catch (ReservaConflitoException ex) {
                        return false;
                    }
                };
                resultados.add(pool.submit(pedido));
            }
            largada.countDown();

            int criadas = 0;
            for (Future<Boolean> f : resultados) {
                criadas += aguardar(f) ? 1 : 0;
            }
            Integer gravadas = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM calendario.reservas WHERE item_id = ? AND status <> 'CANCELADA'",
                    Integer.class, localId);

            assertThat(criadas).isEqualTo(1);
            assertThat(gravadas).isEqualTo(1);
        } finally {
            pool.shutdownNow();
        }
    }

    private boolean aguardar(Future<Boolean> futuro) throws InterruptedException, ExecutionException {
        return futuro.get();
    }

    @Test
    @DisplayName("Item inexistente ou local inativo não pode ser reservado")
    void itemInexistenteOuInativoERecusado() {
        Instant inicio = Instant.now().plus(Duration.ofDays(31));
        jdbc.update("UPDATE calendario.locais SET ativo = false WHERE id = ?", localId);

        assertThatThrownBy(() -> criarReservaUseCase.executar(
                comando(TipoItemReserva.LOCAL, localId, inicio), administrador))
                .isInstanceOf(ReservaOperacaoInvalidaException.class);
        assertThatThrownBy(() -> criarReservaUseCase.executar(
                comando(TipoItemReserva.LOCAL, UUID.randomUUID(), inicio), administrador))
                .isInstanceOf(ReservaOperacaoInvalidaException.class);
    }

    @Test
    @DisplayName("Ativo que não é reservável é recusado; o reservável é aceito")
    void ativoNaoReservavelERecusado() throws SQLException {
        Instant inicio = Instant.now().plus(Duration.ofDays(32));
        UUID naoReservavel = criarAtivo(false, 9101);
        UUID reservavel = criarAtivo(true, 9102);

        assertThatThrownBy(() -> criarReservaUseCase.executar(
                comando(TipoItemReserva.ATIVO, naoReservavel, inicio), administrador))
                .isInstanceOf(ReservaOperacaoInvalidaException.class)
                .hasMessageContaining("não é elegível");
        assertThat(criarReservaUseCase.executar(comando(TipoItemReserva.ATIVO, reservavel, inicio), administrador)
                .getItemId()).isEqualTo(reservavel);
    }

    @Test
    @DisplayName("Local com reserva futura não pode ser excluído nem inativado (exclusão sempre lógica)")
    void localComReservaFuturaNaoEExcluido() {
        criarReservaUseCase.executar(
                comando(TipoItemReserva.LOCAL, localId, Instant.now().plus(Duration.ofDays(33))), administrador);

        assertThatThrownBy(() -> localService.excluir(localId)).isInstanceOf(LocalNegocioException.class)
                .hasMessageContaining("reservas");
        assertThat(localService.obterPorId(localId)).isPresent();
    }
}
