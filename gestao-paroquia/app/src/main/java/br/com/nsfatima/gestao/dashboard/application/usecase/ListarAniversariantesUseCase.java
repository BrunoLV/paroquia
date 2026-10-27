package br.com.nsfatima.gestao.dashboard.application.usecase;

import br.com.nsfatima.gestao.dashboard.api.v1.dto.AniversarianteDTO;
import br.com.nsfatima.gestao.dashboard.domain.model.PeriodoDashboard;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.entity.FielEntity;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository.FielJpaRepository;
import br.com.nsfatima.gestao.support.domain.time.RelogioNegocio;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.MonthDay;
import java.time.Year;
import java.util.Comparator;
import java.util.HashSet;
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
 * <p>O dia é o civil do fuso da paróquia. Quem nasceu em 29/02 comemora em 28/02 nos anos não
 * bissextos. Só entram fiéis ativos que autorizaram a divulgação do aniversário, e o DTO carrega apenas
 * primeiro nome + dia + mês — nunca ano ou data completa (regras duras de LGPD).
 *
 * <p>ponytail: varredura O(n) com descriptografia por cache-miss; se a base de fiéis crescer
 * muito, criar colunas-sombra {@code dia_nascimento}/{@code mes_nascimento} não criptografadas.
 */
@Service
public class ListarAniversariantesUseCase {

    /**
     * Cache compartilhado (expiração de 10 min via {@code CacheConfig}); a chave inclui o dia de negócio
     * para que o resultado de ontem não seja servido após a virada do dia em Brasília.
     */
    static final String CACHE = "dashboardAniversariantes";

    private static final MonthDay VINTE_E_NOVE_DE_FEVEREIRO = MonthDay.of(2, 29);

    private final FielJpaRepository fielRepository;
    private final RelogioNegocio relogio;

    public ListarAniversariantesUseCase(FielJpaRepository fielRepository, RelogioNegocio relogio) {
        this.fielRepository = fielRepository;
        this.relogio = relogio;
    }

    /**
     * Lista os aniversariantes cujo dia/mês cai na janela do recorte informado.
     *
     * @param periodo recorte: {@code HOJE} (dia corrente), {@code SEMANA} (próximos 7 dias) ou
     *     {@code MES} (mês vigente); {@code null} equivale a {@code SEMANA}
     * @return aniversariantes ordenados por mês e dia
     */
    @Cacheable(cacheNames = CACHE, key = "#periodo + '@' + @relogioNegocio.hoje()")
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
        LocalDate hoje = relogio.hoje();
        Set<MonthDay> dias = switch (periodo) {
            case HOJE -> Set.of(MonthDay.from(hoje));
            case SEMANA -> IntStream.range(0, 7)
                    .mapToObj(d -> MonthDay.from(hoje.plusDays(d)))
                    .collect(Collectors.toSet());
            case MES -> IntStream.rangeClosed(1, hoje.lengthOfMonth())
                    .mapToObj(d -> MonthDay.of(hoje.getMonthValue(), d))
                    .collect(Collectors.toSet());
        };
        return comNascidosEmBissexto(dias, periodo, hoje);
    }

    /**
     * Quem nasceu em 29/02 comemora em 28/02 nos anos não bissextos. A regra considera o ano de cada
     * dia da janela (uma semana de 25/02 a 03/03 de 2027 inclui 28/02 sem bissexto), e não só o ano
     * corrente.
     */
    private Set<MonthDay> comNascidosEmBissexto(Set<MonthDay> dias, PeriodoDashboard periodo, LocalDate hoje) {
        boolean janelaContem28DeFevereiroSemBissexto = switch (periodo) {
            case HOJE -> dia28DeFevereiroSemBissexto(hoje);
            case SEMANA -> IntStream.range(0, 7).anyMatch(d -> dia28DeFevereiroSemBissexto(hoje.plusDays(d)));
            case MES -> hoje.getMonthValue() == 2 && !Year.isLeap(hoje.getYear());
        };
        if (!janelaContem28DeFevereiroSemBissexto) {
            return dias;
        }
        Set<MonthDay> ampliado = new HashSet<>(dias);
        ampliado.add(VINTE_E_NOVE_DE_FEVEREIRO);
        return ampliado;
    }

    private boolean dia28DeFevereiroSemBissexto(LocalDate dia) {
        return dia.getMonthValue() == 2 && dia.getDayOfMonth() == 28 && !Year.isLeap(dia.getYear());
    }

    private AniversarianteDTO paraDto(FielEntity fiel) {
        MonthDay nascimento = MonthDay.from(fiel.getDataNascimento());
        String primeiroNome = fiel.getNome() == null ? "" : fiel.getNome().trim().split("\\s+")[0];
        return new AniversarianteDTO(primeiroNome, nascimento.getDayOfMonth(), nascimento.getMonthValue());
    }
}
