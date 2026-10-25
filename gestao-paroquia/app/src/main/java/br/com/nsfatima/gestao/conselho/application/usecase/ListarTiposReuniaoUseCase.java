package br.com.nsfatima.gestao.conselho.application.usecase;

import br.com.nsfatima.gestao.conselho.application.dto.AtorAutenticado;
import br.com.nsfatima.gestao.conselho.application.dto.TipoReuniaoInfo;
import br.com.nsfatima.gestao.conselho.application.port.out.OrganizacoesPort;
import br.com.nsfatima.gestao.conselho.application.service.ConselhoAuthorizationService;
import br.com.nsfatima.gestao.conselho.domain.model.ReferenciaOrganizacao;
import br.com.nsfatima.gestao.conselho.domain.model.TipoReuniao;
import br.com.nsfatima.gestao.conselho.domain.policy.RegrasTipoReuniaoPolicy;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Catálogo dos tipos de reunião com as regras de cada um, para a interface montar o formulário da ata, e os
 * órgãos pelos quais o usuário autenticado pode redigir cada tipo (a mesma regra aplicada ao criar a ata).
 */
@Service
public class ListarTiposReuniaoUseCase {

    private final ConselhoAuthorizationService autorizacao;
    private final OrganizacoesPort organizacoes;

    public ListarTiposReuniaoUseCase(ConselhoAuthorizationService autorizacao, OrganizacoesPort organizacoes) {
        this.autorizacao = autorizacao;
        this.organizacoes = organizacoes;
    }

    /**
     * Lista os tipos, suas regras e, para cada um, os órgãos em que o usuário pode redigir.
     *
     * @return todos os tipos de reunião
     */
    public List<TipoReuniaoInfo> execute() {
        AtorAutenticado ator = autorizacao.atorAtual();
        List<ReferenciaOrganizacao> vinculadas = ator.organizacoesComVinculo().stream()
                .map(organizacoes::buscar).flatMap(Optional::stream).toList();
        return Arrays.stream(TipoReuniao.values()).map(tipo -> {
            var regras = RegrasTipoReuniaoPolicy.para(tipo);
            return new TipoReuniaoInfo(tipo, regras, orgaosDeRedacao(ator, tipo, vinculadas));
        }).toList();
    }

    private Set<UUID> orgaosDeRedacao(AtorAutenticado ator, TipoReuniao tipo,
                                                List<ReferenciaOrganizacao> vinculadas) {
        var regras = RegrasTipoReuniaoPolicy.para(tipo);
        return vinculadas.stream()
                .filter(regras::aceitaConvocante)
                .map(ReferenciaOrganizacao::id)
                .filter(id -> autorizacao.podeRedigir(ator, tipo, id))
                .collect(Collectors.toSet());
    }
}
