package br.com.nsfatima.gestao.patrimonio.domain.repository;

import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoPatrimonial;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoPatrimonialId;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Porta de saída (Driven Port) para persistência e consulta do agregado {@link AtivoPatrimonial}.
 * Todos os métodos de listagem excluem bens com exclusão lógica ({@code deleted_at IS NULL});
 * {@link #findById(AtivoPatrimonialId)} retorna também os excluídos, pois casos de uso de leitura
 * pontual precisam distinguir "não existe" de "foi excluído".
 */
public interface AtivoPatrimonialRepository {

    AtivoPatrimonial save(AtivoPatrimonial ativo);

    Optional<AtivoPatrimonial> findById(AtivoPatrimonialId id);

    /**
     * Como {@link #findById}, mas bloqueia a linha do bem até o fim da transação (exige transação ativa).
     * Reservas e envio para manutenção disputam este bloqueio, de modo que uma reserva não é criada sobre um
     * período em que a manutenção está sendo registrada, e vice-versa.
     */
    Optional<AtivoPatrimonial> findByIdComBloqueio(AtivoPatrimonialId id);

    List<AtivoPatrimonial> findByOrganizacao(UUID organizacaoId);

    List<AtivoPatrimonial> findTodos();

    /** Bens não excluídos e não em manutenção — alvos do job de sincronização de RESERVADO. */
    List<AtivoPatrimonial> findSincronizaveisDeReserva();

    long contarPorStatus(AtivoStatus status);
}
