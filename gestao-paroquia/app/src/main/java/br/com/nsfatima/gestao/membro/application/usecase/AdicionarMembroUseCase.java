package br.com.nsfatima.gestao.membro.application.usecase;

import br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository.FielJpaRepository;
import br.com.nsfatima.gestao.membro.api.v1.dto.MembroRequest;
import br.com.nsfatima.gestao.membro.api.v1.dto.MembroResponse;
import br.com.nsfatima.gestao.membro.domain.model.MembroGrupo;
import br.com.nsfatima.gestao.membro.infrastructure.observability.MembroAuditPublisher;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.entity.MembroGrupoEntity;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.mapper.MembroGrupoMapper;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.mapper.MembroMapper;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.repository.MembroGrupoJpaRepository;
import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import br.com.nsfatima.gestao.organizacao.domain.policy.PapeisPorTipoOrganizacao;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.OrganizacaoEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.OrganizacaoJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Cria o vínculo de um {@code Fiel} a uma organização com um papel de membro comum, implementando o Cenário 1
 * da spec de domínio: cadastrar um paroquiano/dizimista sem nunca exigir, ler ou criar um {@code Usuario}
 * (login). Diferente de {@link br.com.nsfatima.gestao.organizacao.application.usecase.AdicionarVinculoUseCase},
 * que continua servindo o fluxo administrativo por {@code usuarioId} de
 * {@code /usuarios/{id}/membros}, este caso de uso é a porta de entrada de
 * {@code POST /api/v1/membros}, keyada diretamente por {@code fielId}.
 */
@Service
public class AdicionarMembroUseCase {

    private final MembroGrupoJpaRepository repository;
    private final FielJpaRepository fielRepository;
    private final OrganizacaoJpaRepository organizacaoRepository;
    private final MembroMapper mapper;
    private final MembroGrupoMapper membroGrupoMapper;
    private final MembroAuditPublisher auditPublisher;

    public AdicionarMembroUseCase(
            MembroGrupoJpaRepository repository,
            FielJpaRepository fielRepository,
            OrganizacaoJpaRepository organizacaoRepository,
            MembroMapper mapper,
            MembroGrupoMapper membroGrupoMapper,
            MembroAuditPublisher auditPublisher) {
        this.repository = repository;
        this.fielRepository = fielRepository;
        this.organizacaoRepository = organizacaoRepository;
        this.mapper = mapper;
        this.membroGrupoMapper = membroGrupoMapper;
        this.auditPublisher = auditPublisher;
    }

    /**
     * Cria o vínculo Fiel-organização, validando a existência de ambas as partes e a ausência de vínculo ativo
     * prévio antes de persistir. Sincroniza o campo de liderança da organização quando o papel atribuído for
     * de coordenação (FR5), resolvendo o nome de exibição diretamente do {@code Fiel}.
     *
     * @param request dados do vínculo a criar
     * @param actor   identificador do agente executando a ação, usado para auditoria
     * @return o vínculo criado, com dados de exibição resolvidos
     * @throws IllegalArgumentException se o Fiel ou a organização não existirem, se o papel informado for
     *                                  inválido, ou se já houver vínculo ativo entre eles
     */
    @Transactional
    public MembroResponse execute(MembroRequest request, String actor) {
        fielRepository.findById(request.fielId())
                .orElseThrow(() -> new IllegalArgumentException("Fiel não encontrado: " + request.fielId()));

        OrganizacaoEntity organizacao = organizacaoRepository.findById(request.organizacaoId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Organização não encontrada: " + request.organizacaoId()));

        PapelOrganizacional papel = PapelOrganizacional.fromStoredValue(request.papel());
        if (papel == null) {
            throw new IllegalArgumentException("Papel inválido: " + request.papel());
        }
        PapeisPorTipoOrganizacao.exigir(organizacao.getTipo(), papel);

        repository.findByFielIdAndOrganizacaoIdAndAtivoTrue(request.fielId(), request.organizacaoId())
                .ifPresent(m -> {
                    throw new IllegalArgumentException("Fiel já possui vínculo ativo com esta organização.");
                });

        MembroGrupo membro = MembroGrupo.novo(
                UUID.randomUUID(),
                request.fielId(),
                request.organizacaoId(),
                organizacao.getTipo(),
                papel,
                request.igrejaId(),
                request.dataEntrada());
        if (request.ativo() != null && !request.ativo()) {
            membro.desativar();
        }

        MembroGrupoEntity saved = repository.save(membroGrupoMapper.toEntity(membro));


        auditPublisher.publish(actor, "criar", saved.getId().toString(), "sucesso");

        return mapper.toResponse(saved);
    }
}
