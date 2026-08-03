package br.com.nsfatima.gestao.patrimonio.domain.service;

import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.OrganizacaoEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.OrganizacaoJpaRepository;
import br.com.nsfatima.gestao.patrimonio.domain.exception.AtivoOrganizacaoInvalidaException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AtivoOrganizacaoPolicy {

    private final OrganizacaoJpaRepository organizacaoRepository;

    public AtivoOrganizacaoPolicy(OrganizacaoJpaRepository organizacaoRepository) {
        this.organizacaoRepository = organizacaoRepository;
    }

    public void assertOrganizacaoProprietariaPermitida(UUID organizacaoId) {
        OrganizacaoEntity organizacao = organizacaoRepository.findById(organizacaoId)
                .orElseThrow(() -> new AtivoOrganizacaoInvalidaException(
                        "Organização não encontrada: " + organizacaoId));

        TipoOrganizacao tipo = organizacao.getTipo();
        if (tipo != TipoOrganizacao.MATRIZ && tipo != TipoOrganizacao.COMUNIDADE) {
            throw new AtivoOrganizacaoInvalidaException(
                    "Ativos patrimoniais só podem pertencer a organizações do tipo MATRIZ ou COMUNIDADE");
        }
    }
}
