package br.com.nsfatima.gestao.organizacao.domain.repository;

import br.com.nsfatima.gestao.organizacao.domain.model.MandatoCoordenacao;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Porta de persistência do agregado {@link MandatoCoordenacao}.
 */
public interface MandatoCoordenacaoRepository {

    /**
     * Persiste (insere ou atualiza) o mandato.
     *
     * @param mandato mandato a salvar
     */
    void save(MandatoCoordenacao mandato);

    /**
     * Busca um mandato pelo identificador.
     *
     * @param id identificador do mandato
     * @return o mandato, ou vazio se não existir
     */
    Optional<MandatoCoordenacao> findById(UUID id);

    /**
     * Lista todos os mandatos de uma organização, do mais recente ao mais antigo.
     *
     * @param organizacaoId identificador da organização
     * @return mandatos da organização
     */
    List<MandatoCoordenacao> findByOrganizacaoId(UUID organizacaoId);

    /**
     * Lista os mandatos homologados cujo término é anterior à data informada, candidatos a encerramento.
     *
     * @param data data de referência
     * @return mandatos homologados vencidos
     */
    List<MandatoCoordenacao> findHomologadosVencidosAntesDe(LocalDate data);

    /**
     * Informa se a organização possui mandato homologado em vigor na data informada.
     *
     * @param organizacaoId identificador da organização
     * @param data data de referência
     * @return {@code true} se houver mandato vigente
     */
    boolean existeVigente(UUID organizacaoId, LocalDate data);
}
