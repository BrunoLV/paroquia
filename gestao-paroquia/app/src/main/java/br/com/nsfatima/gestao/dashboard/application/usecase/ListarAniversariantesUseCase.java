package br.com.nsfatima.gestao.dashboard.application.usecase;

import br.com.nsfatima.gestao.dashboard.api.v1.dto.AniversarianteDTO;
import br.com.nsfatima.gestao.dashboard.domain.model.PeriodoDashboard;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.entity.FielEntity;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository.FielJpaRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.MonthDay;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Lista os aniversariantes do período para o dashboard. Como {@code dataNascimento} é
 * criptografada em repouso, não há como filtrar por dia/mês em SQL: o caso de uso varre os fiéis
 * ativos em memória (o conversor descriptografa transparente na leitura) e compara o dia/mês.
 * O resultado é cacheado para não pagar essa varredura a cada abertura da tela.
 *
 * <p>Só entram fiéis ativos que autorizaram a divulgação do aniversário, e o DTO carrega apenas
 * primeiro nome + dia + mês — nunca ano ou data completa (regras duras de LGPD).
 *
 * <p>ponytail: varredura O(n) com descriptografia por cache-miss; se a base de fiéis crescer
 * muito, criar colunas-sombra {@code dia_nascimento}/{@code mes_nascimento} não criptografadas.
 */
@Service
public class ListarAniversariantesUseCase {

    /** Cache compartilhado (expiração de 10 min via {@code CacheConfig}); ver ponytail acima. */
    static final String CACHE = "dashboardAniversariantes";

    private final FielJpaRepository fielRepository;

    public ListarAniversariantesUseCase(FielJpaRepository fielRepository) {
        this.fielRepository = fielRepository;
    }

    /**
     * Lista os aniversariantes cujo dia/mês cai na janela do recorte informado.
     *
     * @param periodo recorte: {@code HOJE} (dia corrente), {@code SEMANA} (próximos 7 dias) ou
     *     {@code MES} (mês vigente); {@code null} equivale a {@code SEMANA}
     * @return aniversariantes ordenados por mês e dia
     */
    @Cacheable(cacheNames = CACHE, key = "#periodo")
    @Transactional(readOnly = true)
    public List<AniversarianteDTO> execute(PeriodoDashboard periodo) {
        Set<MonthDay> alvo = diasAlvo(periodo == null ? PeriodoDashboard.SEMANA : periodo);
        return fielRepository.findByAtivoTrue().stream()
                .filter(FielEntity::isAtivo)
                .filter(FielEntity::isPermitirDivulgacaoAniversario)
                .filter(fiel -> fiel.getDataNascimento() != null)
                .filter(fiel -> alvo.contains(MonthDay.from(fiel.getDataNascimento())))
                .map(this::paraDto)
                .sorted(Comparator.comparingInt(AniversarianteDTO::mes).thenComparingInt(AniversarianteDTO::dia))
                .toList();
    }

    private Set<MonthDay> diasAlvo(PeriodoDashboard periodo) {
        LocalDate hoje = LocalDate.now(ZoneOffset.UTC);
        return switch (periodo) {
            case HOJE -> Set.of(MonthDay.from(hoje));
            case SEMANA -> IntStream.range(0, 7)
                    .mapToObj(d -> MonthDay.from(hoje.plusDays(d)))
                    .collect(Collectors.toSet());
            case MES -> IntStream.rangeClosed(1, hoje.lengthOfMonth())
                    .mapToObj(d -> MonthDay.of(hoje.getMonthValue(), d))
                    .collect(Collectors.toSet());
        };
    }

    private AniversarianteDTO paraDto(FielEntity fiel) {
        MonthDay nascimento = MonthDay.from(fiel.getDataNascimento());
        String primeiroNome = fiel.getNome() == null ? "" : fiel.getNome().trim().split("\\s+")[0];
        return new AniversarianteDTO(primeiroNome, nascimento.getDayOfMonth(), nascimento.getMonthValue());
    }
}
