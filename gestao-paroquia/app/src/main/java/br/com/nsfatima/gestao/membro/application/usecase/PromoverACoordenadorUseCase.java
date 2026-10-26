package br.com.nsfatima.gestao.membro.application.usecase;

import br.com.nsfatima.gestao.fiel.infrastructure.persistence.entity.FielEntity;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository.FielJpaRepository;
import br.com.nsfatima.gestao.iam.application.usecase.CriarUsuarioUseCase;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import br.com.nsfatima.gestao.membro.api.v1.dto.PromoverACoordenadorRequest;
import br.com.nsfatima.gestao.membro.api.v1.dto.PromoverACoordenadorResponse;
import br.com.nsfatima.gestao.membro.domain.model.MembroGrupo;
import br.com.nsfatima.gestao.membro.domain.policy.PoliticaIdoneidadeFuncao;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.entity.MembroGrupoEntity;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.mapper.MembroGrupoMapper;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.repository.MembroGrupoJpaRepository;
import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import br.com.nsfatima.gestao.organizacao.domain.policy.PapeisPorTipoOrganizacao;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.OrganizacaoEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.OrganizacaoJpaRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Promove um {@code Fiel} já membro ativo de uma organização a um papel de liderança e, se ele ainda não
 * possuir {@code Usuario}, cria um novo login vinculado a ele — o único ponto do sistema em que a
 * participação pastoral comum "vira" acesso administrativo. Restrito exclusivamente à provisão canônica do Pároco.
 */
@Service
public class PromoverACoordenadorUseCase {

    private final MembroGrupoJpaRepository membroRepository;
    private final FielJpaRepository fielRepository;
    private final OrganizacaoJpaRepository organizacaoRepository;
    private final UsuarioJpaRepository usuarioRepository;
    private final CriarUsuarioUseCase createUsuarioUseCase;
    private final MembroGrupoMapper membroGrupoMapper;

    public PromoverACoordenadorUseCase(
            MembroGrupoJpaRepository membroRepository,
            FielJpaRepository fielRepository,
            OrganizacaoJpaRepository organizacaoRepository,
            UsuarioJpaRepository usuarioRepository,
            CriarUsuarioUseCase createUsuarioUseCase,
            MembroGrupoMapper membroGrupoMapper) {
        this.membroRepository = membroRepository;
        this.fielRepository = fielRepository;
        this.organizacaoRepository = organizacaoRepository;
        this.usuarioRepository = usuarioRepository;
        this.createUsuarioUseCase = createUsuarioUseCase;
        this.membroGrupoMapper = membroGrupoMapper;
    }

    /**
     * Promove o Fiel ao papel de liderança informado, exigindo que ele já seja membro ativo da organização e
     * que o ator autenticado tenha autoridade para promover. Cria um {@code Usuario} novo apenas se o Fiel
     * ainda não possuir um.
     *
     * @param request dados da promoção
     * @return o vínculo atualizado e o {@code usuarioId} (existente ou recém-criado)
     * @throws IllegalArgumentException se o Fiel/organização não existirem, se o papel for inválido, ou se o
     *                                   Fiel não for membro ativo da organização
     * @throws AccessDeniedException se o ator não tiver autoridade
     */
    @Transactional
    public PromoverACoordenadorResponse execute(PromoverACoordenadorRequest request) {
        requireGlobalOuMesaCpp();

        FielEntity fiel = fielRepository.findById(request.fielId())
                .orElseThrow(() -> new IllegalArgumentException("Fiel não encontrado: " + request.fielId()));

        OrganizacaoEntity organizacao = organizacaoRepository.findById(request.organizacaoId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Organização não encontrada: " + request.organizacaoId()));

        PapelOrganizacional papel = PapelOrganizacional.fromStoredValue(request.papel());
        if (papel == null) {
            throw new IllegalArgumentException("Papel inválido: " + request.papel());
        }
        PapeisPorTipoOrganizacao.exigir(organizacao.getTipo(), papel);

        MembroGrupoEntity membro = membroRepository
                .findByFielIdAndOrganizacaoIdAndAtivoTrue(request.fielId(), request.organizacaoId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Fiel não é membro ativo desta organização: " + request.fielId()));

        if (papel.isCoordenacao()) {
            exigirSemOutroCoordenadorAtivo(request.organizacaoId(), membro);
        }

        MembroGrupo membroAgregado = membroGrupoMapper.toDomain(membro);
        List<String> pendencias = PoliticaIdoneidadeFuncao.pendencias(papel.isCoordenacao(),
                organizacao.getSetorPastoral(), membroAgregado.getIdoneidade(), LocalDate.now());
        if (!pendencias.isEmpty()) {
            throw new IllegalArgumentException("Agente sem idoneidade para a função: " + String.join(" ", pendencias));
        }
        membroAgregado.atribuirPapel(papel);
        membroGrupoMapper.aplicar(membroAgregado, membro);
        membroRepository.save(membro);


        var usuarioExistente = usuarioRepository.findByFielId(request.fielId());
        boolean usuarioCriado = usuarioExistente.isEmpty();
        var usuarioId = usuarioExistente
                .map(u -> u.getId())
                .orElseGet(() -> createUsuarioUseCase.execute(request.username(), request.password(),
                        request.fielId()));

        return new PromoverACoordenadorResponse(membro.getId(), fiel.getId(), usuarioId, usuarioCriado);
    }

    /**
     * Limite de coordenação: a organização (e a igreja de contexto do vínculo) tem um único coordenador ativo;
     * trocá-lo passa pelo mandato de coordenação ou pelo vínculo administrativo, que rebaixa o anterior.
     */
    private void exigirSemOutroCoordenadorAtivo(UUID organizacaoId, MembroGrupoEntity membro) {
        boolean jaExiste = membroRepository.findByOrganizacaoIdAndAtivoTrue(organizacaoId).stream()
                .filter(m -> !m.getId().equals(membro.getId()))
                .filter(m -> Objects.equals(m.getIgrejaVinculoId(), membro.getIgrejaVinculoId()))
                .anyMatch(m -> PapelOrganizacional.fromStoredValue(m.getPapel()) == PapelOrganizacional.COORDENADOR);
        if (jaExiste) {
            throw new IllegalArgumentException("A organização já possui coordenador ativo; "
                    + "encerre ou substitua a coordenação atual antes de promover outro Fiel.");
        }
    }

    /**
     * Restringe a promoção/nomeação de coordenador à provisão canônica do Pároco.
     */
    private void requireGlobalOuMesaCpp() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("Não autenticado");
        }

        boolean autorizado = auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_GLOBAL_PAROCO".equals(a.getAuthority())
                        || "ROLE_CLERO_PAROCO".equals(a.getAuthority())
                        || "ROLE_PAROCO".equals(a.getAuthority())
                        || "PAROCO".equals(a.getAuthority())
                        || "ROLE_BISPO_DIOCESANO".equals(a.getAuthority())
                        || "ROLE_GLOBAL_BISPO_DIOCESANO".equals(a.getAuthority()));
        if (!autorizado) {
            throw new AccessDeniedException(
                    "Apenas o Pároco possui provisão canônica para promover um Fiel a coordenador");
        }
    }
}
