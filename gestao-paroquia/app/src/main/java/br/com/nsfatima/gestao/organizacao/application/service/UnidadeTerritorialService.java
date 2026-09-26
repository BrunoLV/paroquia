package br.com.nsfatima.gestao.organizacao.application.service;

import br.com.nsfatima.gestao.observabilidade.application.service.AuditoriaPersistenceService;
import br.com.nsfatima.gestao.organizacao.api.v1.dto.UnidadeTerritorialRequest;
import br.com.nsfatima.gestao.organizacao.domain.exception.GrupoParoquialNegocioException;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoUnidadeTerritorial;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.UnidadeTerritorialEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.UnidadeTerritorialJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Orquestra o ciclo de vida de {@link UnidadeTerritorialEntity} (criação, atualização, exclusão e consulta)
 * e registra cada mutação na auditoria.
 */
@Service
@Transactional
public class UnidadeTerritorialService {

    private static final String AUDIT_ACTOR = "admin";
    private static final String AUDIT_ACTION = "acao-admin";
    private static final String AUDIT_TARGET = "unidadeTerritorial";
    private static final String AUDIT_RESULT = "sucesso";
    private static final String AUDIT_KEY_RESOURCE_TYPE = "resourceType";
    private static final String AUDIT_KEY_RESOURCE_ID = "resourceId";
    private static final String AUDIT_KEY_ACTION = "action";
    private static final String AUDIT_VALUE_UNIDADE_TERRITORIAL = "UNIDADE_TERRITORIAL";

    private final UnidadeTerritorialJpaRepository repository;
    private final AuditoriaPersistenceService auditLogService;

    public UnidadeTerritorialService(
            UnidadeTerritorialJpaRepository repository,
            AuditoriaPersistenceService auditLogService) {
        this.repository = repository;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public List<UnidadeTerritorialEntity> listar() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<UnidadeTerritorialEntity> obterPorId(UUID id) {
        return repository.findById(id);
    }

    @Transactional
    public UnidadeTerritorialEntity criar(UnidadeTerritorialRequest request) {
        if (request.parentId() != null) {
            var parent = repository.findById(request.parentId())
                    .orElseThrow(() -> new GrupoParoquialNegocioException(
                            "Unidade territorial mãe não encontrada com ID: " + request.parentId()));
            if (!parent.isAtivo()) {
                throw new GrupoParoquialNegocioException("Unidade territorial mãe não está ativa.");
            }
        }

        UnidadeTerritorialEntity entity = new UnidadeTerritorialEntity();
        entity.setId(UUID.randomUUID());
        copyRequestToEntity(request, entity);

        repository.save(entity);

        auditLogService.log(AUDIT_ACTOR, AUDIT_ACTION, AUDIT_TARGET, AUDIT_RESULT, Map.of(
                AUDIT_KEY_RESOURCE_TYPE, AUDIT_VALUE_UNIDADE_TERRITORIAL,
                AUDIT_KEY_RESOURCE_ID, entity.getId(),
                AUDIT_KEY_ACTION, "CRIAR",
                "nome", entity.getNome()
        ));

        return entity;
    }

    @Transactional
    public UnidadeTerritorialEntity criar(
            String nome, TipoUnidadeTerritorial tipo, String contato, boolean ativo, UUID parentId) {
        if (parentId != null) {
            var parent = repository.findById(parentId)
                    .orElseThrow(() -> new GrupoParoquialNegocioException(
                            "Unidade territorial mãe não encontrada com ID: " + parentId));
            if (!parent.isAtivo()) {
                throw new GrupoParoquialNegocioException("Unidade territorial mãe não está ativa.");
            }
        }

        UnidadeTerritorialEntity entity = new UnidadeTerritorialEntity();
        entity.setId(UUID.randomUUID());
        entity.setNome(nome);
        entity.setTipo(tipo);
        entity.setContato(contato);
        entity.setAtivo(ativo);
        entity.setParentId(parentId);

        repository.save(entity);

        auditLogService.log(AUDIT_ACTOR, AUDIT_ACTION, AUDIT_TARGET, AUDIT_RESULT, Map.of(
                AUDIT_KEY_RESOURCE_TYPE, AUDIT_VALUE_UNIDADE_TERRITORIAL,
                AUDIT_KEY_RESOURCE_ID, entity.getId(),
                AUDIT_KEY_ACTION, "CRIAR",
                "nome", nome
        ));

        return entity;
    }

    @Transactional
    public UnidadeTerritorialEntity atualizar(UUID id, UnidadeTerritorialRequest request) {
        UnidadeTerritorialEntity entity = repository.findById(id)
                .orElseThrow(() -> GrupoParoquialNegocioException.notFound(id));

        if (request.parentId() != null) {
            if (request.parentId().equals(id)) {
                throw new GrupoParoquialNegocioException("Uma unidade territorial não pode ser mãe de si mesma.");
            }
            var parent = repository.findById(request.parentId())
                    .orElseThrow(() -> new GrupoParoquialNegocioException(
                            "Unidade territorial mãe não encontrada com ID: " + request.parentId()));
            if (!parent.isAtivo()) {
                throw new GrupoParoquialNegocioException("Unidade territorial mãe não está ativa.");
            }
        }

        copyRequestToEntity(request, entity);

        repository.save(entity);

        auditLogService.log(AUDIT_ACTOR, AUDIT_ACTION, AUDIT_TARGET, AUDIT_RESULT, Map.of(
                AUDIT_KEY_RESOURCE_TYPE, AUDIT_VALUE_UNIDADE_TERRITORIAL,
                AUDIT_KEY_RESOURCE_ID, id,
                AUDIT_KEY_ACTION, "ATUALIZAR"
        ));

        return entity;
    }

    @Transactional
    public UnidadeTerritorialEntity atualizar(
            UUID id, String nome, TipoUnidadeTerritorial tipo, String contato, boolean ativo, UUID parentId) {
        UnidadeTerritorialEntity entity = repository.findById(id)
                .orElseThrow(() -> GrupoParoquialNegocioException.notFound(id));

        if (parentId != null) {
            if (parentId.equals(id)) {
                throw new GrupoParoquialNegocioException("Uma unidade territorial não pode ser mãe de si mesma.");
            }
            var parent = repository.findById(parentId)
                    .orElseThrow(() -> new GrupoParoquialNegocioException(
                            "Unidade territorial mãe não encontrada com ID: " + parentId));
            if (!parent.isAtivo()) {
                throw new GrupoParoquialNegocioException("Unidade territorial mãe não está ativa.");
            }
        }

        entity.setNome(nome);
        entity.setTipo(tipo);
        entity.setContato(contato);
        entity.setAtivo(ativo);
        entity.setParentId(parentId);

        repository.save(entity);

        auditLogService.log(AUDIT_ACTOR, AUDIT_ACTION, AUDIT_TARGET, AUDIT_RESULT, Map.of(
                AUDIT_KEY_RESOURCE_TYPE, AUDIT_VALUE_UNIDADE_TERRITORIAL,
                AUDIT_KEY_RESOURCE_ID, id,
                AUDIT_KEY_ACTION, "ATUALIZAR"
        ));

        return entity;
    }

    private void copyRequestToEntity(UnidadeTerritorialRequest req, UnidadeTerritorialEntity entity) {
        entity.setNome(req.nome());
        entity.setTipo(req.tipo());
        entity.setContato(req.contato());
        entity.setAtivo(req.ativo());
        entity.setParentId(req.parentId());

        // Canônicos
        entity.setPadroeiro(req.padroeiro());
        entity.setDataFestaPadroeira(req.dataFestaPadroeira());
        entity.setDataDedicacaoAltar(req.dataDedicacaoAltar());
        entity.setPossuiSacrario(req.possuiSacrario() != null ? req.possuiSacrario() : true);
        entity.setPossuiPiaBatismal(req.possuiPiaBatismal() != null ? req.possuiPiaBatismal() : (req.tipo() == TipoUnidadeTerritorial.MATRIZ));
        entity.setClerigoResponsavelId(req.clerigoResponsavelId());

        // Fiscais e Jurídicos
        entity.setCnpj(req.cnpj());
        entity.setInscricaoMunicipal(req.inscricaoMunicipal());
        entity.setInscricaoImobiliariaIptu(req.inscricaoImobiliariaIptu());
        entity.setProcessoImunidadeIptu(req.processoImunidadeIptu());

        // Registro de Imóveis (RGI)
        entity.setCartorioRgi(req.cartorioRgi());
        entity.setMatriculaImovel(req.matriculaImovel());
        entity.setTipoAquisicao(req.tipoAquisicao());
        entity.setAreaTerrenoM2(req.areaTerrenoM2());
        entity.setAreaConstruidaM2(req.areaConstruidaM2());

        // Segurança e Concessionárias
        entity.setAvcbNumero(req.avcbNumero());
        entity.setAvcbValidade(req.avcbValidade());
        entity.setLotacaoMaximaBombeiros(req.lotacaoMaximaBombeiros());
        entity.setCodigoConsumidorEnergia(req.codigoConsumidorEnergia());
        entity.setCodigoConsumidorAgua(req.codigoConsumidorAgua());

        // Contato e Endereço Estruturado
        entity.setTelefone(req.telefone());
        entity.setEmail(req.email());
        entity.setEnderecoCep(req.enderecoCep());
        entity.setEnderecoLogradouro(req.enderecoLogradouro());
        entity.setEnderecoNumero(req.enderecoNumero());
        entity.setEnderecoComplemento(req.enderecoComplemento());
        entity.setEnderecoBairro(req.enderecoBairro());
        entity.setEnderecoCidade(req.enderecoCidade());
        entity.setEnderecoUf(req.enderecoUf());
    }

    @Transactional
    public void excluir(UUID id) {
        UnidadeTerritorialEntity entity = repository.findById(id)
                .orElseThrow(() -> GrupoParoquialNegocioException.notFound(id));

        repository.delete(entity);

        auditLogService.log(AUDIT_ACTOR, AUDIT_ACTION, AUDIT_TARGET, AUDIT_RESULT, Map.of(
                AUDIT_KEY_RESOURCE_TYPE, AUDIT_VALUE_UNIDADE_TERRITORIAL,
                AUDIT_KEY_RESOURCE_ID, id,
                AUDIT_KEY_ACTION, "EXCLUIR"
        ));
    }
}
