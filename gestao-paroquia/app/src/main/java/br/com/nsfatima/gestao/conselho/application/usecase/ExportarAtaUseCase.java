package br.com.nsfatima.gestao.conselho.application.usecase;

import br.com.nsfatima.gestao.conselho.application.dto.ArquivoExportado;
import br.com.nsfatima.gestao.conselho.application.dto.AtaParaImpressao;
import br.com.nsfatima.gestao.conselho.application.dto.AtorAutenticado;
import br.com.nsfatima.gestao.conselho.application.dto.ConteudoExportacao;
import br.com.nsfatima.gestao.conselho.application.dto.DocumentoReuniao;
import br.com.nsfatima.gestao.conselho.application.dto.FormatoExportacaoAta;
import br.com.nsfatima.gestao.conselho.application.dto.LayoutExportacao;
import br.com.nsfatima.gestao.conselho.application.port.out.CabecalhoParoquiaPort;
import br.com.nsfatima.gestao.conselho.application.port.out.ExportadorAta;
import br.com.nsfatima.gestao.conselho.application.service.AtaAuditoriaService;
import br.com.nsfatima.gestao.conselho.application.service.ComposicaoDocumentoAta;
import br.com.nsfatima.gestao.conselho.application.service.ConselhoAuthorizationService;
import br.com.nsfatima.gestao.conselho.application.service.PreparadorImpressaoAta;
import br.com.nsfatima.gestao.conselho.domain.exception.AtaInvalidaException;
import br.com.nsfatima.gestao.conselho.domain.exception.AtaNaoEncontradaException;
import br.com.nsfatima.gestao.conselho.domain.model.AtaReuniao;
import br.com.nsfatima.gestao.conselho.domain.model.StatusAta;
import br.com.nsfatima.gestao.conselho.domain.model.TipoReuniao;
import br.com.nsfatima.gestao.conselho.domain.policy.AcaoAta;
import br.com.nsfatima.gestao.conselho.domain.policy.AutorizacaoAtaPolicy;
import br.com.nsfatima.gestao.conselho.domain.repository.AtaRepository;
import br.com.nsfatima.gestao.conselho.domain.repository.FiltroAtas;
import br.com.nsfatima.gestao.conselho.domain.repository.PaginaAtas;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * Exporta atas para Word ou PDF. Uma ata sai em qualquer situação que o ator possa ler: a selada sai como
 * documento oficial, as demais como minuta. A compilação (livro de atas e decisões de um período) inclui apenas
 * atas seladas. Cada ata exportada gera uma entrada na trilha de auditoria, por isso o caso de uso grava e não é
 * somente leitura.
 */
@Service
public class ExportarAtaUseCase {

    private static final int LIMITE_COMPILACAO = 200;
    private static final ZoneId BRASILIA = ZoneId.of("America/Sao_Paulo");
    private static final Set<StatusAta> SELADAS = Set.of(StatusAta.HOMOLOGADA, StatusAta.REGISTRADA);

    private final AtaRepository repository;
    private final ConselhoAuthorizationService autorizacao;
    private final PreparadorImpressaoAta preparador;
    private final CabecalhoParoquiaPort cabecalho;
    private final List<ExportadorAta> exportadores;
    private final AtaAuditoriaService auditoria;

    public ExportarAtaUseCase(AtaRepository repository, ConselhoAuthorizationService autorizacao,
                              PreparadorImpressaoAta preparador, CabecalhoParoquiaPort cabecalho,
                              List<ExportadorAta> exportadores, AtaAuditoriaService auditoria) {
        this.repository = repository;
        this.autorizacao = autorizacao;
        this.preparador = preparador;
        this.cabecalho = cabecalho;
        this.exportadores = List.copyOf(exportadores);
        this.auditoria = auditoria;
    }

    /**
     * Exporta uma ata.
     *
     * @param id ata a exportar
     * @param formato PDF ou DOCX
     * @param layout estruturado ou narrativo
     * @param conteudo ata completa ou só as decisões
     * @return o arquivo gerado
     */
    @Transactional
    public ArquivoExportado exportarAta(UUID id, FormatoExportacaoAta formato, LayoutExportacao layout,
                                        ConteudoExportacao conteudo) {
        AtorAutenticado ator = autorizacao.atorAtual();
        AtaReuniao ata = autorizacao.carregar(id, ator, AcaoAta.EXPORTAR);
        ArquivoExportado arquivo = gerar(List.of(ata), formato, layout, conteudo, nomeDaAta(ata, conteudo, formato));
        auditoria.registrarExportacao(ator, ata, formato.name(), layout.name(), conteudo.name());
        return arquivo;
    }

    /**
     * Exporta a compilação das atas seladas de um órgão e período.
     *
     * @param organizacaoId órgão convocante
     * @param tipo tipo de reunião, ou {@code null} para todos
     * @param de início do período da reunião, ou {@code null}
     * @param ate fim do período da reunião, ou {@code null}
     * @param formato PDF ou DOCX
     * @param layout estruturado ou narrativo
     * @param conteudo ata completa ou só as decisões
     * @return o arquivo gerado
     */
    @Transactional
    public ArquivoExportado exportarCompilacao(UUID organizacaoId, TipoReuniao tipo, LocalDate de, LocalDate ate,
                                               FormatoExportacaoAta formato, LayoutExportacao layout,
                                               ConteudoExportacao conteudo) {
        AtorAutenticado ator = autorizacao.atorAtual();
        FiltroAtas filtro = new FiltroAtas(AutorizacaoAtaPolicy.leituraEmQualquerOrganizacao(ator.papeisGlobais()),
                ator.organizacoesComVinculo(), organizacaoId, tipo, SELADAS, de, ate);
        PaginaAtas pagina = repository.listar(filtro, 0, LIMITE_COMPILACAO);
        if (pagina.itens().isEmpty()) {
            throw new AtaNaoEncontradaException("Nenhuma ata selada encontrada para os filtros informados.");
        }
        if (pagina.total() > LIMITE_COMPILACAO) {
            throw new AtaInvalidaException("O filtro alcança mais de " + LIMITE_COMPILACAO
                    + " atas; reduza o período.");
        }
        List<AtaReuniao> atas = pagina.itens().reversed();
        ArquivoExportado arquivo = gerar(atas, formato, layout, conteudo, nomeDaCompilacao(de, ate, formato));
        atas.forEach(ata -> auditoria.registrarExportacao(ator, ata, formato.name(), layout.name(), conteudo.name()));
        return arquivo;
    }

    private ArquivoExportado gerar(List<AtaReuniao> atas, FormatoExportacaoAta formato, LayoutExportacao layout,
                                   ConteudoExportacao conteudo, String nomeArquivo) {
        ExportadorAta exportador = exportadores.stream().filter(e -> e.suporta(formato)).findFirst()
                .orElseThrow(() -> new AtaInvalidaException("Formato de exportação não suportado."));
        List<AtaParaImpressao> impressao = preparador.preparar(atas);
        DocumentoReuniao documento = ComposicaoDocumentoAta.compor(cabecalho.obter(), impressao, layout, conteudo,
                ZonedDateTime.now(BRASILIA));
        return new ArquivoExportado(exportador.exportar(documento), nomeArquivo, formato.getTipoMidia());
    }

    private static String nomeDaAta(AtaReuniao ata, ConteudoExportacao conteudo, FormatoExportacaoAta formato) {
        String prefixo = conteudo == ConteudoExportacao.SOMENTE_DECISOES ? "decisoes" : "ata";
        String identificacao = ata.identificacao().map(n -> n.replace('/', '-')).orElse("minuta");
        return prefixo + "-" + identificacao + "-" + slug(ata.getTipo().name()) + "." + formato.getExtensao();
    }

    private static String nomeDaCompilacao(LocalDate de, LocalDate ate, FormatoExportacaoAta formato) {
        String periodo = (de == null ? "" : "-de-" + de) + (ate == null ? "" : "-ate-" + ate);
        return "livro-de-atas" + periodo + "." + formato.getExtensao();
    }

    private static String slug(String texto) {
        String semAcento = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return semAcento.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
    }
}
