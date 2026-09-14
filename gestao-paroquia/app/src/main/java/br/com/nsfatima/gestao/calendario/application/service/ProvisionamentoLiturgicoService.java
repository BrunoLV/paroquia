package br.com.nsfatima.gestao.calendario.application.service;

import br.com.nsfatima.gestao.calendario.domain.exception.ForbiddenOperationException;
import br.com.nsfatima.gestao.calendario.domain.exception.ImmutableEclesialRuleException;
import br.com.nsfatima.gestao.calendario.domain.model.SaidaLiturgica;
import br.com.nsfatima.gestao.calendario.domain.type.CategoriaEvento;
import br.com.nsfatima.gestao.calendario.domain.model.MarcoLiturgico;
import br.com.nsfatima.gestao.calendario.domain.model.TipoCicloLiturgico;
import br.com.nsfatima.gestao.calendario.domain.type.EventoStatusInput;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.AnoLiturgicoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.CicloLiturgicoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.SolenidadeLiturgicaEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.AnoLiturgicoRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.CicloLiturgicoRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.SolenidadeLiturgicaJpaRepository;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoUnidadeTerritorial;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.UnidadeTerritorialEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.UnidadeTerritorialJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ProvisionamentoLiturgicoService {

    private static final String COR_BRANCO = "BRANCO";

    private final AnoLiturgicoRepository anoLiturgicoRepository;
    private final CicloLiturgicoRepository cicloLiturgicoRepository;
    private final MotorLiturgico liturgicalEngine;
    private final EventoJpaRepository eventoJpaRepository;
    private final UnidadeTerritorialJpaRepository unidadeTerritorialJpaRepository;
    private final SolenidadeLiturgicaJpaRepository solenidadeLiturgicaJpaRepository;

    public ProvisionamentoLiturgicoService(AnoLiturgicoRepository anoLiturgicoRepository,
                                        CicloLiturgicoRepository cicloLiturgicoRepository,
                                        MotorLiturgico liturgicalEngine,
                                        EventoJpaRepository eventoJpaRepository,
                                        UnidadeTerritorialJpaRepository unidadeTerritorialJpaRepository,
                                        SolenidadeLiturgicaJpaRepository solenidadeLiturgicaJpaRepository) {
        this.anoLiturgicoRepository = anoLiturgicoRepository;
        this.cicloLiturgicoRepository = cicloLiturgicoRepository;
        this.liturgicalEngine = liturgicalEngine;
        this.eventoJpaRepository = eventoJpaRepository;
        this.unidadeTerritorialJpaRepository = unidadeTerritorialJpaRepository;
        this.solenidadeLiturgicaJpaRepository = solenidadeLiturgicaJpaRepository;
    }

    /**
     * Garante que o Ano Litúrgico, seus Ciclos e Eventos de Solenidades existam no banco de dados.
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
     * Executa a rotina de provisionamento automático do Ano Litúrgico a partir das Solenidades cadastradas
     * no banco de dados (`SolenidadeLiturgicaEntity`), persistem o Ano Litúrgico, os Ciclos Litúrgicos e os
     * Eventos do tipo SOLENIDADE/LITURGICO na Matriz e Comunidades na base de dados.
     */
    private AnoLiturgicoEntity provisionNewLiturgicalYear(int year) {
        List<SolenidadeLiturgicaEntity> solenidadesDoBanco = solenidadeLiturgicaJpaRepository.findByAtivoTrue();

        SaidaLiturgica output = liturgicalEngine.calcularAnoLiturgico(year, solenidadesDoBanco);

        Instant inicioAdvento = output.dataInicio().atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant fimAnoLiturgico = output.dataFim().atTime(23, 59, 59).atZone(ZoneOffset.UTC).toInstant();

        AnoLiturgicoEntity anoEntity = new AnoLiturgicoEntity(
                year, output.cicloLetra(), inicioAdvento, fimAnoLiturgico, Instant.now());
        AnoLiturgicoEntity savedAno = anoLiturgicoRepository.save(anoEntity);

        List<CicloLiturgicoEntity> ciclos = new ArrayList<>();
        Map<String, MarcoLiturgico> marcos = output.marcosCalculados();

        // 1. Advento
        LocalDate adventoInicio = output.dataInicio();
        ciclos.add(createCiclo("Advento " + year, TipoCicloLiturgico.ADVENTO, "ROXO",
                adventoInicio.getMonthValue(), adventoInicio.getDayOfMonth(), 12, 24, 1.8,
                "Tempo de preparação e expectativa do nascimento de Cristo", year));

        // 2. Natal
        ciclos.add(createCiclo("Tempo do Natal " + year, TipoCicloLiturgico.NATAL, COR_BRANCO,
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
            ciclos.add(createCiclo("Tempo Pascal " + year, TipoCicloLiturgico.PASCOA, COR_BRANCO,
                    easter.data().getMonthValue(), easter.data().getDayOfMonth(),
                    pentecostes.data().getMonthValue(), pentecostes.data().getDayOfMonth(), 2.5,
                    "Cinquenta dias de alegria da Ressurreição até Pentecostes", year));
        }

        // 5. Tempo Comum
        ciclos.add(createCiclo("Tempo Comum " + year, TipoCicloLiturgico.TEMPO_COMUM, "VERDE",
                1, 11, 11, 27, 1.0,
                "Caminhada ordinária da Igreja", year));

        // 6. Festa Padroeira
        ciclos.add(createCiclo("Festa Padroeira " + year, TipoCicloLiturgico.FESTA_PADROEIRA, COR_BRANCO,
                5, 4, 5, 13, 3.0,
                "Novena e Festa da Padroeira Nossa Senhora de Fátima", year));

        cicloLiturgicoRepository.saveAll(ciclos);

        // Provisionar os Eventos de Solenidades/Marcos Litúrgicos na Matriz e Comunidades
        provisionSolemnityEvents(year, output.marcosCalculados());

        return savedAno;
    }

    /**
     * Cria e persiste os registros de Eventos (categoria LITURGICO / SOLENIDADE) na tabela `eventos`
     * para a Matriz e todas as Comunidades da Paróquia.
     */
    private void provisionSolemnityEvents(int year, Map<String, MarcoLiturgico> marcos) {
        List<UnidadeTerritorialEntity> igrejasAlvo = unidadeTerritorialJpaRepository.findAll().stream()
                .filter(UnidadeTerritorialEntity::isAtivo)
                .filter(o -> o.getTipo() == TipoUnidadeTerritorial.MATRIZ
                        || o.getTipo() == TipoUnidadeTerritorial.COMUNIDADE)
                .toList();

        if (igrejasAlvo.isEmpty()) {
            UnidadeTerritorialEntity matrizPadrao = new UnidadeTerritorialEntity();
            matrizPadrao.setId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
            igrejasAlvo = List.of(matrizPadrao);
        }

        List<EventoEntity> eventosParaSalvar = new ArrayList<>();

        for (UnidadeTerritorialEntity igreja : igrejasAlvo) {
            for (MarcoLiturgico marco : marcos.values()) {
                EventoEntity evento = new EventoEntity();
                evento.setId(UUID.randomUUID());
                evento.setTitulo(marco.nome());
                evento.setDescricao(
                "Solenidade / Marco Litúrgico provisionado automaticamente"
                        + " a partir do banco de dados (" + marco.chave() + ")");
                evento.setGrupoParoquialResponsavelId(igreja.getId());
                evento.setCategoria(CategoriaEvento.LITURGICO);
                evento.setStatus(EventoStatusInput.CONFIRMADO.name());

                int prioridade = 2;
                if ("QUINTA_SANTA".equals(marco.chave()) || "SEXTA_SANTA".equals(marco.chave())
                        || "SABADO_SANTO".equals(marco.chave()) || "PASCOA".equals(marco.chave())) {
                    prioridade = 1;
                }
                evento.setNivelPrioridade(prioridade);
                evento.setAnoLiturgicoRef(year);
                evento.setInicioUtc(marco.data().atStartOfDay(ZoneOffset.UTC).toInstant());
                evento.setFimUtc(marco.data().atTime(23, 59, 59).atZone(ZoneOffset.UTC).toInstant());
                evento.setIgrejaVinculoId(igreja.getId());

                eventosParaSalvar.add(evento);
            }
        }

        eventoJpaRepository.saveAll(eventosParaSalvar);
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
     */
    public void validateManualMutationRights(
            String userRole, String campoModificado, String novoValor, String justificativaPastoral) {
        String roleUpper = userRole != null ? userRole.toUpperCase().replace("ROLE_", "") : "";
        if (!"PAROCO".equals(roleUpper) && !"VIGARIO".equals(roleUpper)) {
            throw new ForbiddenOperationException("Apenas Pároco ou Vigário podem alterar cadastros litúrgicos.");
        }

        if (campoModificado != null) {
            String campoLower = campoModificado.toLowerCase();
            if (campoLower.contains("cicloletra") || campoLower.contains("pascoa") || "ciclo".equals(campoLower)) {
                throw new ImmutableEclesialRuleException(
                    "O Ciclo Litúrgico e o Domingo de Páscoa são regras eclesiais imutáveis.");
            }
        }
    }
}
