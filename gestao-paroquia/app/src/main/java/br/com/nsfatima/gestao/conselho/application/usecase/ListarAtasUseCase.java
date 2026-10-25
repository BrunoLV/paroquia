package br.com.nsfatima.gestao.conselho.application.usecase;

import br.com.nsfatima.gestao.conselho.application.dto.AtaVisao;
import br.com.nsfatima.gestao.conselho.application.dto.AtorAutenticado;
import br.com.nsfatima.gestao.conselho.application.dto.PaginaVisoes;
import br.com.nsfatima.gestao.conselho.application.service.AtaVisaoMontador;
import br.com.nsfatima.gestao.conselho.application.service.ConselhoAuthorizationService;
import br.com.nsfatima.gestao.conselho.domain.model.StatusAta;
import br.com.nsfatima.gestao.conselho.domain.model.TipoReuniao;
import br.com.nsfatima.gestao.conselho.domain.policy.AutorizacaoAtaPolicy;
import br.com.nsfatima.gestao.conselho.domain.repository.AtaRepository;
import br.com.nsfatima.gestao.conselho.domain.repository.FiltroAtas;
import br.com.nsfatima.gestao.conselho.domain.repository.PaginaAtas;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Lista atas. O escopo de leitura entra no filtro e é aplicado na consulta: clero e secretaria paroquial
 * veem todas; os demais, só as de órgãos a que estão vinculados.
 */
@Service
public class ListarAtasUseCase {

    private final AtaRepository repository;
    private final ConselhoAuthorizationService autorizacao;
    private final AtaVisaoMontador montador;

    public ListarAtasUseCase(AtaRepository repository, ConselhoAuthorizationService autorizacao,
                             AtaVisaoMontador montador) {
        this.repository = repository;
        this.autorizacao = autorizacao;
        this.montador = montador;
    }

    /**
     * Lista uma página de atas visíveis ao ator.
     *
     * @param organizacaoId filtro opcional pelo órgão convocante
     * @param tipo filtro opcional pelo tipo de reunião
     * @param status filtro opcional pela situação
     * @param de início opcional do período da reunião
     * @param ate fim opcional do período da reunião
     * @param pagina índice da página, iniciado em zero
     * @param tamanho quantidade por página
     * @return a página e o total que atende ao filtro
     */
    @Transactional(readOnly = true)
    public PaginaVisoes execute(UUID organizacaoId, TipoReuniao tipo, StatusAta status, LocalDate de,
                                LocalDate ate, int pagina, int tamanho) {
        AtorAutenticado ator = autorizacao.atorAtual();
        FiltroAtas filtro = new FiltroAtas(AutorizacaoAtaPolicy.leituraEmQualquerOrganizacao(ator.papeisGlobais()),
                ator.organizacoesComVinculo(), organizacaoId, tipo, status == null ? Set.of() : Set.of(status),
                de, ate);
        PaginaAtas resultado = repository.listar(filtro, pagina, tamanho);
        List<AtaVisao> itens = resultado.itens().stream().map(ata -> montador.montar(ator, ata)).toList();
        return new PaginaVisoes(itens, resultado.total());
    }
}
