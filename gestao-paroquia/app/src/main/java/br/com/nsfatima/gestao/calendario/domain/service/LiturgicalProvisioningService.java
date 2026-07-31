package br.com.nsfatima.gestao.calendario.domain.service;

import br.com.nsfatima.gestao.calendario.domain.exception.ForbiddenOperationException;
import br.com.nsfatima.gestao.calendario.domain.exception.ImmutableEclesialRuleException;
import br.com.nsfatima.gestao.calendario.domain.model.LiturgicalOutput;
import br.com.nsfatima.gestao.calendario.domain.model.MarcoLiturgico;
import br.com.nsfatima.gestao.calendario.domain.model.TipoCicloLiturgico;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.AnoLiturgicoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.CicloLiturgicoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.AnoLiturgicoRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.CicloLiturgicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class LiturgicalProvisioningService {

    private final AnoLiturgicoRepository anoLiturgicoRepository;
    private final CicloLiturgicoRepository cicloLiturgicoRepository;
    private final LiturgicalEngine liturgicalEngine;

    public LiturgicalProvisioningService(AnoLiturgicoRepository anoLiturgicoRepository,
                                        CicloLiturgicoRepository cicloLiturgicoRepository,
                                        LiturgicalEngine liturgicalEngine) {
        this.anoLiturgicoRepository = anoLiturgicoRepository;
        this.cicloLiturgicoRepository = cicloLiturgicoRepository;
        this.liturgicalEngine = liturgicalEngine;
    }

    /**
     * Garante que o Ano Litúrgico e seus Ciclos/Solenidades existam no banco de dados.
     */
    @Transactional
    public AnoLiturgicoEntity ensureAnoLiturgicoEExiste(int targetYear) {
        return anoLiturgicoRepository.findById(targetYear)
                .orElseGet(() -> provisionNewLiturgicalYear(targetYear));
    }

    /**
     * Resolve o Ano Litúrgico para uma data em UTC e garante o seu provisionamento automático.
     */
    @Transactional
    public AnoLiturgicoEntity resolveAndEnsure(Instant targetDate) {
        int anoRef = liturgicalEngine.resolveAnoLiturgico(targetDate);
        return ensureAnoLiturgicoEExiste(anoRef);
    }

    /**
     * Executa a rotina de provisionamento automático do Ano Litúrgico a partir do LiturgicalEngine.
     */
    private AnoLiturgicoEntity provisionNewLiturgicalYear(int year) {
        LiturgicalOutput output = liturgicalEngine.calculateLiturgicalYear(year);

        Instant inicioAdvento = output.dataInicio().atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant fimAnoLiturgico = output.dataFim().atTime(23, 59, 59).atZone(ZoneOffset.UTC).toInstant();

        AnoLiturgicoEntity anoEntity = new AnoLiturgicoEntity(year, output.cicloLetra(), inicioAdvento, fimAnoLiturgico, Instant.now());
        AnoLiturgicoEntity savedAno = anoLiturgicoRepository.save(anoEntity);

        List<CicloLiturgicoEntity> ciclos = new ArrayList<>();
        Map<String, MarcoLiturgico> marcos = output.marcosCalculados();

        // 1. Advento
        LocalDate adventoInicio = output.dataInicio();
        ciclos.add(createCiclo("Advento " + year, TipoCicloLiturgico.ADVENTO, "ROXO",
                adventoInicio.getMonthValue(), adventoInicio.getDayOfMonth(), 12, 24, 1.8,
                "Tempo de preparação e expectativa do nascimento de Cristo", year));

        // 2. Natal
        ciclos.add(createCiclo("Tempo do Natal " + year, TipoCicloLiturgico.NATAL, "BRANCO",
                12, 25, 1, 10, 2.0,
                "Celebração do Nascimento do Senhor e Epifania", year));

        // 3. Quaresma
        MarcoLiturgico cinzas = marcos.get("QUARTA_CINZAS");
        MarcoLiturgico sabadoSanto = marcos.get("SABADO_SANTO");
        if (cinzas != null && sabadoSanto != null) {
            ciclos.add(createCiclo("Quaresma " + year, TipoCicloLiturgico.QUARESMA, "ROXO",
                    cinzas.data().getMonthValue(), cinzas.data().getDayOfMonth(),
                    sabadoSanto.data().getMonthValue(), sabadoSanto.data().getDayOfMonth(), 2.2,
                    "Tempo de penitência, conversão e Oração", year));
        }

        // 4. Tempo Pascal
        MarcoLiturgico easter = marcos.get("PASCOA");
        MarcoLiturgico pentecostes = marcos.get("PENTECOSTES");
        if (easter != null && pentecostes != null) {
            ciclos.add(createCiclo("Tempo Pascal " + year, TipoCicloLiturgico.PASCOA, "BRANCO",
                    easter.data().getMonthValue(), easter.data().getDayOfMonth(),
                    pentecostes.data().getMonthValue(), pentecostes.data().getDayOfMonth(), 2.5,
                    "Cinquenta dias de alegria da Ressurreição até Pentecostes", year));
        }

        // 5. Tempo Comum
        ciclos.add(createCiclo("Tempo Comum " + year, TipoCicloLiturgico.TEMPO_COMUM, "VERDE",
                1, 11, 11, 27, 1.0,
                "Caminhada ordinária da Igreja", year));

        // 6. Festa Padroeira
        ciclos.add(createCiclo("Festa Padroeira " + year, TipoCicloLiturgico.FESTA_PADROEIRA, "BRANCO",
                5, 4, 5, 13, 3.0,
                "Novena e Festa da Padroeira Nossa Senhora de Fátima", year));

        cicloLiturgicoRepository.saveAll(ciclos);

        return savedAno;
    }

    private CicloLiturgicoEntity createCiclo(String nome, TipoCicloLiturgico tipo, String cor,
                                            int mesInicio, int diaInicio, int mesFim, int diaFim,
                                            double fator, String desc, int anoRef) {
        CicloLiturgicoEntity c = new CicloLiturgicoEntity();
        c.setId(UUID.randomUUID());
        c.setNome(nome);
        c.setTipoCiclo(tipo);
        c.setCorLiturgica(cor);
        c.setMesInicio(mesInicio);
        c.setDiaInicio(diaInicio);
        c.setMesFim(mesFim);
        c.setDiaFim(diaFim);
        c.setFatorDemanda(fator);
        c.setDescricao(desc);
        c.setAnoLiturgicoRef(anoRef);
        return c;
    }

    /**
     * Retorna o peso de precedência litúrgica (1 a 5, menor número = maior prioridade).
     * 1. Tríduo Pascal
     * 2. Solenidades Instanciadas / Festa Padroeira
     * 3. Quaresma e Advento (Tempos Fortes)
     * 4. Natal e Páscoa (Tempos Festivos)
     * 5. Tempo Comum
     */
    public int resolvePrecedenciaPeso(TipoCicloLiturgico tipoCiclo) {
        if (tipoCiclo == null) {
            return 5;
        }
        return switch (tipoCiclo) {
            case TRIDUO_PASCAL -> 1;
            case SOLENIDADE, FESTA_PADROEIRA -> 2;
            case QUARESMA, ADVENTO -> 3;
            case NATAL, PASCOA -> 4;
            case TEMPO_COMUM -> 5;
        };
    }

    /**
     * Valida direitos de alteração manual: permite EXCLUSIVAMENTE para PAROCO ou VIGARIO.
     * Bloqueia alterações em campos eclesiais imutáveis (cicloLetra ou Domingo de Páscoa).
     */
    public void validateManualMutationRights(String userRole, String campoModificado, String novoValor, String justificativaPastoral) {
        String roleUpper = userRole != null ? userRole.toUpperCase().replace("ROLE_", "") : "";
        if (!"PAROCO".equals(roleUpper) && !"VIGARIO".equals(roleUpper)) {
            throw new ForbiddenOperationException("Apenas Pároco ou Vigário podem alterar cadastros litúrgicos.");
        }

        if (campoModificado != null) {
            String campoLower = campoModificado.toLowerCase();
            if (campoLower.contains("cicloletra") || campoLower.contains("pascoa") || "ciclo".equals(campoLower)) {
                throw new ImmutableEclesialRuleException("O Ciclo Litúrgico e o Domingo de Páscoa são regras eclesiais imutáveis.");
            }
        }
    }
}
