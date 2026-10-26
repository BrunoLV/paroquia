package br.com.nsfatima.gestao.conselho.application.usecase;

import br.com.nsfatima.gestao.conselho.application.dto.ClerigoAtivo;
import br.com.nsfatima.gestao.conselho.application.port.out.PessoasPort;
import br.com.nsfatima.gestao.conselho.application.service.ConselhoAuthorizationService;
import org.springframework.stereotype.Service;

import java.util.List;

/** Clérigos que o redator pode incluir na presença da ata. Quem não redige nenhum órgão não os vê. */
@Service
public class ListarClerigosParaAtaUseCase {

    private final ConselhoAuthorizationService autorizacao;
    private final PessoasPort pessoas;

    public ListarClerigosParaAtaUseCase(ConselhoAuthorizationService autorizacao, PessoasPort pessoas) {
        this.autorizacao = autorizacao;
        this.pessoas = pessoas;
    }

    /**
     * Lista os clérigos ativos.
     *
     * @return os clérigos, ou vazio se o usuário não tem vínculo com nenhum órgão
     */
    public List<ClerigoAtivo> execute() {
        return autorizacao.atorAtual().organizacoesComVinculo().isEmpty() ? List.of() : pessoas.clerigosAtivos();
    }
}
