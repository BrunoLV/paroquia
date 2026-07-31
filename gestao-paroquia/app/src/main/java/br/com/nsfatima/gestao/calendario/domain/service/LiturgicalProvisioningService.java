package br.com.nsfatima.gestao.calendario.domain.service;

import br.com.nsfatima.gestao.calendario.domain.model.TipoCicloLiturgico;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.AnoLiturgicoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.CicloLiturgicoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.AnoLiturgicoRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.CicloLiturgicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
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
     * Garante que o Ano Litúrgico e seus Ciclos Litúrgicos existam no banco de dados.
     * Caso não existam, calcula e cria automaticamente seguindo as regras eclesiais.
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

    private AnoLiturgicoEntity provisionNewLiturgicalYear(int year) {
        ZonedDateTime inicioAdventoZdt = liturgicalEngine.calculateAdventoInicio(year);
        ZonedDateTime proximoAdventoZdt = liturgicalEngine.calculateAdventoInicio(year + 1);

        Instant inicioAdvento = inicioAdventoZdt.toInstant();
        Instant fimComum = proximoAdventoZdt.minusSeconds(1).toInstant();

        String cicloLetra = liturgicalEngine.resolveCicloLetra(year);

        AnoLiturgicoEntity anoEntity = new AnoLiturgicoEntity(year, cicloLetra, inicioAdvento, fimComum, Instant.now());
        AnoLiturgicoEntity savedAno = anoLiturgicoRepository.save(anoEntity);

        // Datas Móveis calculadas dinamicamente via Algoritmo de Meeus/Jones/Butcher
        LocalDate quartaCinzas = liturgicalEngine.calculateQuartaDeCinzas(year);
        LocalDate vesperaPascoa = liturgicalEngine.calculateVesperaPascoa(year);
        LocalDate easter = liturgicalEngine.calculateEaster(year);
        LocalDate pentecostes = liturgicalEngine.calculatePentecostes(year);
        LocalDate adventoLocalDate = inicioAdventoZdt.toLocalDate();

        List<CicloLiturgicoEntity> ciclos = new ArrayList<>();

        // 1. Advento
        ciclos.add(createCiclo("Advento " + year, TipoCicloLiturgico.ADVENTO, "ROXO",
                adventoLocalDate.getMonthValue(), adventoLocalDate.getDayOfMonth(), 12, 24, 1.8,
                "Tempo de preparação e expectativa do nascimento de Cristo", year));

        // 2. Natal
        ciclos.add(createCiclo("Tempo do Natal " + year, TipoCicloLiturgico.NATAL, "BRANCO",
                12, 25, 1, 10, 2.0,
                "Celebração do Nascimento do Senhor e Epifania", year));

        // 3. Quaresma (Data Móvel: Quarta de Cinzas até Sábado Santo)
        ciclos.add(createCiclo("Quaresma " + year, TipoCicloLiturgico.QUARESMA, "ROXO",
                quartaCinzas.getMonthValue(), quartaCinzas.getDayOfMonth(),
                vesperaPascoa.getMonthValue(), vesperaPascoa.getDayOfMonth(), 2.2,
                "Tempo de penitência, conversão e Oração", year));

        // 4. Tempo Pascal (Data Móvel: Domingo de Páscoa até Pentecostes)
        ciclos.add(createCiclo("Tempo Pascal " + year, TipoCicloLiturgico.PASCOA, "BRANCO",
                easter.getMonthValue(), easter.getDayOfMonth(),
                pentecostes.getMonthValue(), pentecostes.getDayOfMonth(), 2.5,
                "Cinquenta dias de alegria da Ressurreição até Pentecostes", year));

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
}
