package br.com.nsfatima.gestao.importacao.application.service;

import br.com.nsfatima.gestao.calendario.application.service.MotorLiturgico;
import br.com.nsfatima.gestao.calendario.application.service.ProvisionamentoLiturgicoService;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.CicloLiturgicoRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.governanca.infrastructure.persistence.repository.AnoParoquialJpaRepository;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import br.com.nsfatima.gestao.importacao.api.v1.dto.ImportacaoItemResultadoDTO;
import br.com.nsfatima.gestao.importacao.api.v1.dto.ImportacaoValidacaoResultadoDTO;
import br.com.nsfatima.gestao.importacao.domain.model.TipoEntidadeImportacao;
import br.com.nsfatima.gestao.membro.application.usecase.AdicionarMembroUseCase;
import br.com.nsfatima.gestao.organizacao.domain.repository.PastoralMovimentoOficialRepository;
import br.com.nsfatima.gestao.organizacao.application.service.GrupoParoquialService;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.GrupoParoquialJpaRepository;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.UnidadeTerritorialJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Orquestra o ciclo completo de importação em lote (template → validação → confirmação) para
 * os tipos suportados por {@link TipoEntidadeImportacao}: membro, organização e evento.
 * A validação (dry-run) apenas relata problemas linha a linha sem persistência. A confirmação
 * processa os registros de modo resiliente por linha.
 */
@Service
public class ImportacaoService {

    private static final Logger log = LoggerFactory.getLogger(ImportacaoService.class);

    private final ImportacaoFileParser importacaoFileParser;
    private final ImportacaoLinhaExecutor linhaExecutor;

    public ImportacaoService(AdicionarMembroUseCase adicionarMembroUseCase,
                              UsuarioJpaRepository usuarioJpaRepository,
                              GrupoParoquialService grupoParoquialService,
                              EventoJpaRepository eventoJpaRepository,
                              GrupoParoquialJpaRepository grupoParoquialJpaRepository,
                              UnidadeTerritorialJpaRepository unidadeTerritorialJpaRepository,
                              PastoralMovimentoOficialRepository pastoralOficialRepository,
                              ImportacaoFileParser importacaoFileParser,
                              AnoParoquialJpaRepository anoParoquialJpaRepository,
                              MotorLiturgico liturgicalEngine,
                              CicloLiturgicoRepository cicloLiturgicoRepository,
                              ProvisionamentoLiturgicoService liturgicalProvisioningService) {
        this.importacaoFileParser = importacaoFileParser;
        this.linhaExecutor = new ImportacaoLinhaExecutor(
                adicionarMembroUseCase,
                usuarioJpaRepository,
                grupoParoquialService,
                eventoJpaRepository,
                grupoParoquialJpaRepository,
                unidadeTerritorialJpaRepository,
                pastoralOficialRepository,
                anoParoquialJpaRepository,
                liturgicalEngine,
                cicloLiturgicoRepository,
                liturgicalProvisioningService);
    }

    /**
     * Gera o arquivo modelo (CSV ou XLSX) para guia de preenchimento.
     *
     * @param tipo código de {@link TipoEntidadeImportacao}
     * @param formato "CSV" ou "XLSX"
     * @return bytes do arquivo gerado
     * @throws IOException se houver falha ao montar o modelo
     */
    public byte[] gerarTemplate(String tipo, String formato) throws IOException {
        return importacaoFileParser.gerarTemplate(tipo, formato);
    }

    /**
     * Executa a etapa de pré-validação (dry-run): faz o parse e valida cada linha isoladamente.
     *
     * @param tipo código de {@link TipoEntidadeImportacao}
     * @param file arquivo enviado
     * @return sumário com totais e detalhe por linha
     * @throws IOException se o arquivo não puder ser lido
     */
    public ImportacaoValidacaoResultadoDTO validarArquivo(String tipo, MultipartFile file) throws IOException {
        String filename = Optional.ofNullable(file.getOriginalFilename()).orElse("").toLowerCase();
        List<Map<String, String>> rows = importacaoFileParser.parseFile(filename, file.getInputStream());

        List<ImportacaoItemResultadoDTO> itens = new ArrayList<>();
        int valids = 0;
        int invalidos = 0;

        for (int i = 0; i < rows.size(); i++) {
            int linhaNum = i + 2;
            Map<String, String> rowData = rows.get(i);
            String errorMsg = linhaExecutor.validarLinha(tipo, rowData);
            boolean isValido = (errorMsg == null);

            if (isValido) {
                valids++;
            } else {
                invalidos++;
            }
            itens.add(new ImportacaoItemResultadoDTO(linhaNum, isValido, errorMsg, rowData));
        }

        return new ImportacaoValidacaoResultadoDTO(rows.size(), valids, invalidos, itens);
    }

    /**
     * Persiste os itens validados processando e capturando falhas por linha de forma isolada.
     *
     * @param tipo código do tipo de entidade
     * @param validItems lista de mapas com as colunas
     * @param actor autor da importação
     * @return total de registros importados com sucesso
     */
    @Transactional
    public int confirmarImportacao(String tipo, List<Map<String, String>> validItems, String actor) {
        int count = 0;
        TipoEntidadeImportacao tipoEntidade = TipoEntidadeImportacao.fromCode(tipo);
        if (tipoEntidade == null) {
            throw new IllegalArgumentException("Tipo de importação desconhecido: " + tipo);
        }
        for (Map<String, String> data : validItems) {
            try {
                linhaExecutor.processarImportacao(tipoEntidade, data, actor);
                count++;
            } catch (Exception e) {
                log.error("Erro ao importar registro de {}: {}", tipo, e.getMessage());
            }
        }
        return count;
    }
}
