package br.com.nsfatima.gestao.calendario.application.usecase.evento;

import br.com.nsfatima.gestao.calendario.application.export.ExportadorCalendario;
import br.com.nsfatima.gestao.calendario.domain.type.CategoriaEvento;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.ParoquiaConfiguracaoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.ParoquiaConfiguracaoRepository;
import br.com.nsfatima.gestao.local.infrastructure.persistence.entity.LocalEntity;
import br.com.nsfatima.gestao.local.infrastructure.persistence.repository.LocalJpaRepository;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.OrganizacaoEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.OrganizacaoJpaRepository;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.entity.ProjetoEventoEntity;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.repository.ProjetoEventoJpaRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Caso de uso responsável por montar e exportar o calendário de eventos em um arquivo
 * (delegando a geração binária a um {@link ExportadorCalendario} escolhido por formato).
 * Resolve o período de exportação (ano, mês ou intervalo customizado, sempre convertido
 * para UTC a partir do fuso America/Sao_Paulo), busca os eventos elegíveis com os filtros
 * de organização/categoria informados e carrega os mapas auxiliares (local, organização,
 * configuração da paróquia) necessários para renderizar o documento sem consultas N+1.
 */
@Service
public class ExportacaoCalendarioUseCase {

    private static final ZoneId BR_ZONE = ZoneId.of("America/Sao_Paulo");
    private final EventoJpaRepository eventoRepository;
    private final LocalJpaRepository localRepository;
    private final OrganizacaoJpaRepository organizacaoRepository;
    private final ProjetoEventoJpaRepository projetoEventoRepository;
    private final ParoquiaConfiguracaoRepository paroquiaConfigRepository;
    private final List<ExportadorCalendario> exportadores;

    public ExportacaoCalendarioUseCase(
            EventoJpaRepository eventoRepository,
            LocalJpaRepository localRepository,
            OrganizacaoJpaRepository organizacaoRepository,
            ProjetoEventoJpaRepository projetoEventoRepository,
            ParoquiaConfiguracaoRepository paroquiaConfigRepository,
            List<ExportadorCalendario> exportadores) {
        this.eventoRepository = eventoRepository;
        this.localRepository = localRepository;
        this.organizacaoRepository = organizacaoRepository;
        this.projetoEventoRepository = projetoEventoRepository;
        this.paroquiaConfigRepository = paroquiaConfigRepository;
        this.exportadores = exportadores;
    }

    /**
     * Resolve o período de exportação a partir da opção informada (ANO, MES ou PERIODO),
     * busca os eventos elegíveis dentro desse período aplicando os filtros de categoria e
     * organização, e delega a geração do arquivo binário ao exportador que suporta o
     * formato solicitado.
     *
     * @param formato identificador do formato de saída (ex.: "PDF", "ICS"), repassado ao
     *                 {@link CalendarioExporter} correspondente
     * @param opcao modo de resolução do período: "ANO", "MES" ou "PERIODO"
     * @param ano ano alvo quando {@code opcao} é "ANO" ou "MES"; usa o ano corrente se nulo
     * @param mes mês alvo (1-12) quando {@code opcao} é "MES"; usa o mês corrente se nulo
     * @param dataInicio início do intervalo quando {@code opcao} é "PERIODO"
     * @param dataFim fim do intervalo quando {@code opcao} é "PERIODO"
     * @param categorias categorias de evento usadas para filtrar o conjunto exportado
     * @param organizacaoIds organizações usadas para filtrar o conjunto exportado
     * @param visualizacao modo de apresentação visual ("AGENDA" ou "GRADE"), repassado ao exportador
     * @return conteúdo binário do calendário exportado no formato solicitado
     * @throws IllegalArgumentException se {@code opcao} for inválida ou se nenhum
     *                                    exportador suportar o {@code formato} informado
     */
    @Transactional(readOnly = true)
    public byte[] execute(String formato, String opcao, Integer ano, Integer mes, Instant dataInicio, Instant dataFim,
                          List<String> categorias, List<UUID> organizacaoIds, String visualizacao) {
        // 1. Determina o período de tempo (UTC)
        Instant start;
        Instant end;

        if ("ANO".equalsIgnoreCase(opcao)) {
            int targetYear = ano != null ? ano : LocalDate.now(BR_ZONE).getYear();
            start = LocalDate.of(targetYear, 1, 1).atStartOfDay(ZoneOffset.UTC).toInstant();
            end = LocalDate.of(targetYear, 12, 31).atTime(23, 59, 59, 999999999).atZone(ZoneOffset.UTC).toInstant();
        } else if ("MES".equalsIgnoreCase(opcao)) {
            int targetYear = ano != null ? ano : LocalDate.now(BR_ZONE).getYear();
            int targetMonth = mes != null ? mes : LocalDate.now(BR_ZONE).getMonthValue();
            LocalDate firstDay = LocalDate.of(targetYear, targetMonth, 1);
            start = firstDay.atStartOfDay(ZoneOffset.UTC).toInstant();
            end = firstDay.plusMonths(1).minusDays(1).atTime(23, 59, 59, 999999999).atZone(ZoneOffset.UTC).toInstant();
        } else if ("PERIODO".equalsIgnoreCase(opcao)) {
            start = dataInicio != null ? dataInicio : Instant.EPOCH;
            end = dataFim != null ? dataFim : Instant.now().plusSeconds(31536000); // +1 ano default
        } else {
            throw new IllegalArgumentException("Opção de exportação inválida: " + opcao);
        }

        // 2. Busca os eventos no período com filtros adicionais
        Pageable pageable = PageRequest.of(
                0,
                100000,
                Sort.by(Sort.Direction.ASC, "inicioUtc")
        );

        List<CategoriaEvento> categoriasEnum = categorias == null ? null
                : categorias.stream().map(CategoriaEvento::fromValue).toList();
        List<EventoEntity> eventos = eventoRepository.findAllWithFilters(
                start,
                true,
                end,
                true,
                organizacaoIds,
                organizacaoIds != null && !organizacaoIds.isEmpty(),
                null,
                false,
                null,
                false,
                categoriasEnum,
                categoriasEnum != null && !categoriasEnum.isEmpty(),
                null,
                false,
                pageable
        ).getContent();

        // 3. Carrega mapeamentos auxiliares para resolver nomes
        Map<UUID, String> localMap = localRepository.findAll().stream()
                .collect(Collectors.toMap(LocalEntity::getId, LocalEntity::getNome));

        Map<UUID, String> orgMap = organizacaoRepository.findAll().stream()
                .collect(Collectors.toMap(OrganizacaoEntity::getId, OrganizacaoEntity::getNome));

        Map<UUID, String> projetoMap = projetoEventoRepository.findAll().stream()
                .collect(Collectors.toMap(ProjetoEventoEntity::getId, ProjetoEventoEntity::getNome));

        // 4. Carrega a configuração da paróquia
        ParoquiaConfiguracaoEntity config = paroquiaConfigRepository.findAll().stream()
                .findFirst()
                .orElse(null);

        // 5. Executa a exportação baseada no formato desejado
        ExportadorCalendario exportador = exportadores.stream()
                .filter(e -> e.suporta(formato))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Formato de exportação não suportado: " + formato));

        return exportador.exportar(eventos, localMap, orgMap, projetoMap, start, end, config, visualizacao);
    }
}
