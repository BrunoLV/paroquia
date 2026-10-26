package br.com.nsfatima.gestao.importacao.application.service;

import br.com.nsfatima.gestao.calendario.domain.exception.ViolacaoRegraNegocioException;
import br.com.nsfatima.gestao.importacao.api.v1.dto.ImportacaoConfirmacaoResultadoDTO;
import br.com.nsfatima.gestao.importacao.api.v1.dto.ImportacaoErroLinhaDTO;
import br.com.nsfatima.gestao.importacao.api.v1.dto.ImportacaoItemResultadoDTO;
import br.com.nsfatima.gestao.importacao.api.v1.dto.ImportacaoLinhaDTO;
import br.com.nsfatima.gestao.importacao.api.v1.dto.ImportacaoValidacaoResultadoDTO;
import br.com.nsfatima.gestao.importacao.domain.model.DesfechoLinha;
import br.com.nsfatima.gestao.importacao.domain.model.TipoEntidadeImportacao;
import br.com.nsfatima.gestao.organizacao.domain.exception.OrganizacaoNegocioException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Orquestra o ciclo completo de importação em lote (modelo → validação → confirmação) para os tipos de
 * {@link TipoEntidadeImportacao}. A validação (dry-run) relata cada linha sem gravar nada. A confirmação grava
 * linha a linha, cada uma em transação própria, de modo que o erro de uma não impede nem desfaz as demais
 * (RN-IMP-01); por isso este serviço não é transacional: a fronteira de transação é a linha.
 */
@Service
public class ImportacaoService {

    private static final Logger log = LoggerFactory.getLogger(ImportacaoService.class);

    /** Tamanho máximo do arquivo enviado para validação. */
    public static final long TAMANHO_MAXIMO_BYTES = 2L * 1024 * 1024;

    private static final String ERRO_INESPERADO = "Falha inesperada ao gravar a linha. Tente novamente ou "
            + "contate a secretaria.";

    private final LeitorArquivoImportacao leitorArquivoImportacao;
    private final ImportacaoLinhaExecutor linhaExecutor;

    public ImportacaoService(LeitorArquivoImportacao leitorArquivoImportacao,
                             ImportacaoLinhaExecutor linhaExecutor) {
        this.leitorArquivoImportacao = leitorArquivoImportacao;
        this.linhaExecutor = linhaExecutor;
    }

    /**
     * Monta o arquivo modelo (cabeçalhos e exemplos) para preenchimento prévio.
     *
     * @param tipo código de {@link TipoEntidadeImportacao}
     * @param formato formato desejado ("CSV" ou "XLSX")
     * @return bytes do arquivo gerado
     * @throws IOException se houver falha ao montar o modelo
     */
    public byte[] gerarTemplate(String tipo, String formato) throws IOException {
        return leitorArquivoImportacao.gerarTemplate(tipo, formato);
    }

    /**
     * Executa a etapa de pré-validação (dry-run): lê o arquivo e submete cada linha ao mesmo parse e às mesmas
     * regras de domínio da gravação, sem persistir.
     *
     * @param tipo código de {@link TipoEntidadeImportacao}
     * @param file arquivo enviado
     * @return sumário com totais e detalhe por linha
     * @throws IOException se o arquivo não puder ser lido
     * @throws IllegalArgumentException se o tipo for desconhecido ou o arquivo estiver vazio, grande demais ou
     *     com linhas demais
     */
    public ImportacaoValidacaoResultadoDTO validarArquivo(String tipo, MultipartFile file) throws IOException {
        TipoEntidadeImportacao tipoEntidade = exigirTipo(tipo);
        exigirArquivoAceitavel(file);
        String nomeArquivo = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        List<Map<String, String>> linhas = leitorArquivoImportacao.lerArquivo(nomeArquivo, file.getInputStream());

        List<ImportacaoItemResultadoDTO> itens = new ArrayList<>();
        int validos = 0;
        for (int i = 0; i < linhas.size(); i++) {
            String erro = erroDaLinha(tipoEntidade, linhas.get(i));
            if (erro == null) {
                validos++;
            }
            itens.add(new ImportacaoItemResultadoDTO(i + 2, erro == null, erro, linhas.get(i)));
        }
        return new ImportacaoValidacaoResultadoDTO(linhas.size(), validos, linhas.size() - validos, itens);
    }

    /**
     * Grava as linhas confirmadas, cada uma em transação própria, e devolve o que entrou e o que foi rejeitado.
     * As linhas são revalidadas integralmente: o cliente é quem as reenvia, então nada é presumido válido.
     *
     * @param tipo código do tipo de entidade
     * @param linhas linhas a gravar, com o número da linha no arquivo original
     * @return totais gravados, enviados para aprovação e a lista de erros por linha
     * @throws IllegalArgumentException se o tipo for desconhecido
     */
    public ImportacaoConfirmacaoResultadoDTO confirmarImportacao(String tipo, List<ImportacaoLinhaDTO> linhas) {
        TipoEntidadeImportacao tipoEntidade = exigirTipo(tipo);
        List<ImportacaoErroLinhaDTO> erros = new ArrayList<>();
        int importados = 0;
        int aguardandoAprovacao = 0;
        for (ImportacaoLinhaDTO item : linhas) {
            try {
                if (linhaExecutor.processar(tipoEntidade, item.dados()) == DesfechoLinha.AGUARDANDO_APROVACAO) {
                    aguardandoAprovacao++;
                } else {
                    importados++;
                }
            } catch (RuntimeException e) {
                // Só o tipo da exceção vai para o log: a mensagem pode carregar dado pessoal da linha.
                log.warn("Linha {} da importação de {} rejeitada ({})", item.linha(), tipoEntidade,
                        e.getClass().getSimpleName());
                erros.add(new ImportacaoErroLinhaDTO(item.linha(), mensagemDe(e)));
            }
        }
        return new ImportacaoConfirmacaoResultadoDTO(importados, aguardandoAprovacao, erros);
    }

    private String erroDaLinha(TipoEntidadeImportacao tipo, Map<String, String> dados) {
        try {
            linhaExecutor.validar(tipo, dados);
            return null;
        } catch (RuntimeException e) {
            return mensagemDe(e);
        }
    }

    /**
     * Regras de negócio e de autorização explicam o problema ao usuário; qualquer outra falha (banco, infra)
     * vira mensagem genérica para não vazar detalhe interno na resposta.
     */
    private String mensagemDe(RuntimeException e) {
        boolean deNegocio = e instanceof IllegalArgumentException
                || e instanceof OrganizacaoNegocioException
                || e instanceof ViolacaoRegraNegocioException
                || e instanceof AccessDeniedException;
        return deNegocio && e.getMessage() != null ? e.getMessage() : ERRO_INESPERADO;
    }

    private TipoEntidadeImportacao exigirTipo(String tipo) {
        TipoEntidadeImportacao tipoEntidade = TipoEntidadeImportacao.fromCode(tipo);
        if (tipoEntidade == null) {
            throw new IllegalArgumentException("Tipo de importação desconhecido: " + tipo);
        }
        return tipoEntidade;
    }

    private void exigirArquivoAceitavel(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("O arquivo enviado está vazio.");
        }
        if (file.getSize() > TAMANHO_MAXIMO_BYTES) {
            throw new IllegalArgumentException("O arquivo excede o tamanho máximo de "
                    + TAMANHO_MAXIMO_BYTES / (1024 * 1024) + " MB.");
        }
    }
}
