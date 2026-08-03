package br.com.nsfatima.gestao.support.infrastructure.persistence.entity;

import jakarta.persistence.MappedSuperclass;

/**
 * Superclasse mapeada para entidades JPA que precisam expor um número de versão sem participar do
 * controle de bloqueio otimista real do JPA (ex.: entidades legadas ou somente-leitura que ainda
 * precisam satisfazer um contrato de resposta/serialização que inclui versão). A implementação
 * padrão retorna sempre {@code 0L}; subclasses que precisem de versionamento efetivo devem
 * sobrescrever este método ou usar {@code @Version} diretamente.
 */
@MappedSuperclass
@SuppressWarnings("PMD.AbstractClassWithoutAbstractMethod")
public abstract class BaseVersionedEntity {

    /**
     * Retorna o número de versão da entidade para fins de exposição/serialização.
     *
     * @return sempre {@code 0L} na implementação base; subclasses podem sobrescrever
     */
    public Long getVersion() {
        return 0L;
    }
}
