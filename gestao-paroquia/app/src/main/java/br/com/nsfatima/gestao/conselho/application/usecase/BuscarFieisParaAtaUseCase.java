package br.com.nsfatima.gestao.conselho.application.usecase;

import br.com.nsfatima.gestao.calendario.domain.exception.OperacaoProibidaException;
import br.com.nsfatima.gestao.conselho.application.dto.PessoaOpcao;
import br.com.nsfatima.gestao.conselho.application.port.out.PessoasPort;
import br.com.nsfatima.gestao.conselho.application.service.ConselhoAuthorizationService;
import br.com.nsfatima.gestao.conselho.domain.exception.AtaInvalidaException;
import br.com.nsfatima.gestao.conselho.domain.model.ModoPresenca;
import br.com.nsfatima.gestao.conselho.domain.model.TipoReuniao;
import br.com.nsfatima.gestao.conselho.domain.policy.RegrasTipoReuniaoPolicy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Busca de fiéis pelo nome para a presença das reuniões de presença livre (reunião de pastoral, de coordenadores,
 * de animadores, planejamento pastoral e comissão especial), em que qualquer fiel ativo pode constar. Só o redator
 * do tipo no órgão consulta, e o resultado traz apenas identificador e nome.
 */
@Service
public class BuscarFieisParaAtaUseCase {

    static final int TERMO_MINIMO = 3;
    static final int LIMITE = 20;

    private final ConselhoAuthorizationService autorizacao;
    private final PessoasPort pessoas;

    public BuscarFieisParaAtaUseCase(ConselhoAuthorizationService autorizacao, PessoasPort pessoas) {
        this.autorizacao = autorizacao;
        this.pessoas = pessoas;
    }

    /**
     * Busca fiéis ativos pelo nome.
     *
     * @param tipo tipo de reunião da ata
     * @param organizacaoId órgão convocante
     * @param termo trecho do nome, com ao menos {@value #TERMO_MINIMO} letras
     * @return até {@value #LIMITE} fiéis, em ordem de nome
     * @throws OperacaoProibidaException se o usuário não redige o tipo no órgão, ou o tipo só admite membros do órgão
     * @throws AtaInvalidaException se o termo for curto demais
     */
    public List<PessoaOpcao> execute(TipoReuniao tipo, UUID organizacaoId, String termo) {
        autorizacao.exigirRedacao(autorizacao.atorAtual(), tipo, organizacaoId, null);
        if (RegrasTipoReuniaoPolicy.para(tipo).modoPresenca() != ModoPresenca.NOMINAL_LIVRE) {
            throw new OperacaoProibidaException("A reunião de \"" + tipo.getRotulo()
                    + "\" admite na presença só membros do órgão, não qualquer fiel.");
        }
        String procurado = termo == null ? "" : termo.trim();
        if (procurado.length() < TERMO_MINIMO) {
            throw new AtaInvalidaException("Informe ao menos " + TERMO_MINIMO + " letras do nome.");
        }
        return pessoas.buscarFieisPorNome(procurado, LIMITE);
    }
}
