package br.com.nsfatima.gestao.membro.application.usecase;

import br.com.nsfatima.gestao.membro.api.v1.dto.IdoneidadeRequest;
import br.com.nsfatima.gestao.membro.api.v1.dto.IdoneidadeResponse;
import br.com.nsfatima.gestao.membro.domain.model.IdoneidadeAgente;
import br.com.nsfatima.gestao.membro.domain.model.MembroGrupo;
import br.com.nsfatima.gestao.membro.domain.policy.PoliticaIdoneidadeFuncao;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.entity.MembroGrupoEntity;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.mapper.MembroGrupoMapper;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.repository.MembroGrupoJpaRepository;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.OrganizacaoEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.OrganizacaoJpaRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Registra e consulta a idoneidade pastoral de um agente (formação, vivência sacramental, proteção de menores e
 * envio). O vínculo exige discernimento pastoral antes de a pessoa assumir uma função; a decisão sobre as
 * pendências é da {@link PoliticaIdoneidadeFuncao}, aqui apenas carregada e aplicada.
 */
@Service
public class RegistrarIdoneidadeUseCase {

    private final MembroGrupoJpaRepository membroRepository;
    private final OrganizacaoJpaRepository organizacaoRepository;
    private final MembroGrupoMapper mapper;
    private final Clock clock;

    public RegistrarIdoneidadeUseCase(MembroGrupoJpaRepository membroRepository,
                                      OrganizacaoJpaRepository organizacaoRepository,
                                      MembroGrupoMapper mapper, Clock clock) {
        this.membroRepository = membroRepository;
        this.organizacaoRepository = organizacaoRepository;
        this.mapper = mapper;
        this.clock = clock;
    }

    /**
     * Substitui o registro de idoneidade do vínculo. O ator é gravado como responsável pelo envio apenas
     * quando a data de envio muda.
     *
     * @param membroId identificador do vínculo
     * @param request datas atestadas
     * @param ator papel/identificador de quem registra
     * @return a idoneidade atualizada com as pendências para a função atual
     * @throws IllegalArgumentException se o vínculo não existir ou as datas forem inválidas
     */
    @Transactional
    public IdoneidadeResponse registrar(UUID membroId, IdoneidadeRequest request, String ator) {
        MembroGrupoEntity entity = carregar(membroId);
        MembroGrupo membro = mapper.toDomain(entity);
        IdoneidadeAgente atual = membro.getIdoneidade();
        boolean envioMudou = request.enviadoEm() != null && !request.enviadoEm().equals(atual.enviadoEm());
        membro.registrarIdoneidade(new IdoneidadeAgente(
                request.formacaoConcluidaEm(),
                request.vivenciaSacramentalAtestadaEm(),
                request.termoProtecaoMenoresEm(),
                request.termoProtecaoMenoresValidoAte(),
                request.enviadoEm(),
                request.enviadoEm() == null ? null : envioMudou ? ator : atual.enviadoPor()));
        mapper.aplicar(membro, entity);
        membroRepository.save(entity);
        return resposta(membro, entity);
    }

    /**
     * Consulta a idoneidade do vínculo com as pendências para a função atual.
     *
     * @param membroId identificador do vínculo
     * @return a idoneidade e as pendências
     * @throws IllegalArgumentException se o vínculo não existir
     */
    @Transactional(readOnly = true)
    public IdoneidadeResponse obter(UUID membroId) {
        MembroGrupoEntity entity = carregar(membroId);
        return resposta(mapper.toDomain(entity), entity);
    }

    private MembroGrupoEntity carregar(UUID membroId) {
        return membroRepository.findById(membroId)
                .orElseThrow(() -> new IllegalArgumentException("Vínculo não encontrado: " + membroId));
    }

    private IdoneidadeResponse resposta(MembroGrupo membro, MembroGrupoEntity entity) {
        var pendencias = PoliticaIdoneidadeFuncao.pendencias(
                membro.getPapel().isCoordenacao(),
                organizacaoRepository.findById(entity.getOrganizacaoId())
                        .map(OrganizacaoEntity::getSetorPastoral).orElse(null),
                membro.getIdoneidade(), LocalDate.now(clock));
        IdoneidadeAgente i = membro.getIdoneidade();
        return new IdoneidadeResponse(i.formacaoConcluidaEm(), i.vivenciaSacramentalAtestadaEm(),
                i.termoProtecaoMenoresEm(), i.termoProtecaoMenoresValidoAte(), i.enviadoEm(), i.enviadoPor(),
                pendencias.isEmpty(), pendencias);
    }
}
