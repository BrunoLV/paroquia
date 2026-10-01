package br.com.nsfatima.gestao.sacramento.domain.repository;

import br.com.nsfatima.gestao.sacramento.domain.model.AssentoSacramental;
import br.com.nsfatima.gestao.sacramento.domain.model.AssentoSacramentalId;
import br.com.nsfatima.gestao.sacramento.domain.model.TipoSacramento;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Porta de persistência do agregado. Sem operação de exclusão: assentos são perpétuos (Cân. 535). */
public interface AssentoSacramentalRepository {

    AssentoSacramental save(AssentoSacramental assento);

    Optional<AssentoSacramental> findById(AssentoSacramentalId id);

    /**
     * Lista assentos, opcionalmente por tipo.
     *
     * @param tipo filtro opcional
     * @param incluirReservados {@code false} oculta o livro reservado (secretaria)
     * @return assentos visíveis ao ator
     */
    List<AssentoSacramental> listar(TipoSacramento tipo, boolean incluirReservados);

    /** Unicidade do termo por tipo no livro físico, ignorando o próprio assento em atualizações. */
    boolean existeReferencia(TipoSacramento tipo, String livro, String folha, String termo, UUID ignorarId);

    /** Visão 360º: assentos em que o fiel ou clérigo está vinculado. */
    List<AssentoSacramental> buscarPorVinculo(UUID pessoaId, boolean incluirReservados);
}
