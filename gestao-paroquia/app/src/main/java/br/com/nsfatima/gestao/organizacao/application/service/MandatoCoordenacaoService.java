package br.com.nsfatima.gestao.organizacao.application.service;

import br.com.nsfatima.gestao.fiel.infrastructure.persistence.entity.FielEntity;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository.FielJpaRepository;
import br.com.nsfatima.gestao.membro.domain.model.MembroGrupo;
import br.com.nsfatima.gestao.membro.domain.policy.PoliticaIdoneidadeFuncao;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.entity.MembroGrupoEntity;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.mapper.MembroGrupoMapper;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.repository.MembroGrupoJpaRepository;
import br.com.nsfatima.gestao.organizacao.domain.exception.OrganizacaoNegocioException;
import br.com.nsfatima.gestao.organizacao.domain.model.FuncaoCoordenacao;
import br.com.nsfatima.gestao.organizacao.domain.model.MandatoCoordenacao;
import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import br.com.nsfatima.gestao.organizacao.domain.repository.MandatoCoordenacaoRepository;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.OrganizacaoEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.OrganizacaoJpaRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orquestra o ciclo de vida do mandato de coordenação: a coordenação de uma pastoral, movimento ou serviço é um
 * encargo eclesial temporário, proposto pelo grupo e só legítimo após a homologação do Pároco (Cân. 519 e 317).
 *
 * <p>Ao homologar, o vínculo do fiel na organização assume o papel da função e o nome é espelhado nos campos
 * legados {@code coordenador}/{@code viceCoordenador}; ao encerrar, o vínculo volta a membro ativo e o espelho é
 * limpo. O espelho é somente leitura: a fonte da verdade é o mandato.</p>
 */
@Service
@Transactional
public class MandatoCoordenacaoService {

    private final MandatoCoordenacaoRepository mandatoRepository;
    private final OrganizacaoJpaRepository organizacaoRepository;
    private final FielJpaRepository fielRepository;
    private final MembroGrupoJpaRepository membroRepository;
    private final MembroGrupoMapper membroMapper;
    private final Clock clock;

    public MandatoCoordenacaoService(
            MandatoCoordenacaoRepository mandatoRepository,
            OrganizacaoJpaRepository organizacaoRepository,
            FielJpaRepository fielRepository,
            MembroGrupoJpaRepository membroRepository,
            MembroGrupoMapper membroMapper,
            Clock clock) {
        this.mandatoRepository = mandatoRepository;
        this.organizacaoRepository = organizacaoRepository;
        this.fielRepository = fielRepository;
        this.membroRepository = membroRepository;
        this.membroMapper = membroMapper;
        this.clock = clock;
    }

    /**
     * Registra a proposta de um mandato, pendente de homologação do Pároco.
     *
     * @param organizacaoId organização a coordenar (deve estar ativa)
     * @param fielId fiel proposto (deve ser membro ativo da organização)
     * @param funcao coordenador ou vice-coordenador
     * @param inicio primeiro dia do mandato
     * @param fim último dia do mandato (respeita a duração máxima)
     * @return o mandato proposto
     * @throws OrganizacaoNegocioException se a proposta violar as regras do mandato
     */
    public MandatoCoordenacao propor(UUID organizacaoId, UUID fielId, FuncaoCoordenacao funcao,
                                     LocalDate inicio, LocalDate fim) {
        OrganizacaoEntity organizacao = organizacaoRepository.findById(organizacaoId)
                .orElseThrow(() -> OrganizacaoNegocioException.notFound(organizacaoId));
        if (!organizacao.isAtivo()) {
            throw new OrganizacaoNegocioException("Não é possível propor mandato para organização inativa.");
        }
        if (!fielRepository.existsById(fielId)) {
            throw new OrganizacaoNegocioException("Fiel não encontrado: " + fielId);
        }
        if (membroRepository.findByFielIdAndOrganizacaoIdAndAtivoTrue(fielId, organizacaoId).isEmpty()) {
            throw new OrganizacaoNegocioException("O fiel precisa ser membro ativo da organização para coordená-la.");
        }

        exigirIdoneidade(organizacao, fielId);

        MandatoCoordenacao mandato;
        try {
            mandato = MandatoCoordenacao.propor(organizacaoId, fielId, funcao, inicio, fim);
        } catch (IllegalArgumentException ex) {
            throw new OrganizacaoNegocioException(ex.getMessage());
        }
        boolean conflito = mandatoRepository.findByOrganizacaoId(organizacaoId).stream()
                .anyMatch(m -> m.ocupaCargo() && m.getFuncao() == funcao && m.sobrepoe(inicio, fim));
        if (conflito) {
            throw new OrganizacaoNegocioException(
                    "Já existe mandato proposto ou homologado para esta função no período informado.");
        }
        mandatoRepository.save(mandato);
        return mandato;
    }

    /**
     * Homologa a proposta: o fiel passa a exercer legitimamente a função.
     *
     * @param mandatoId mandato proposto
     * @return o mandato homologado
     * @throws OrganizacaoNegocioException se o mandato não existir ou não puder ser homologado
     */
    public MandatoCoordenacao homologar(UUID mandatoId) {
        MandatoCoordenacao mandato = carregar(mandatoId);
        organizacaoRepository.findById(mandato.getOrganizacaoId())
                .ifPresent(organizacao -> exigirIdoneidade(organizacao, mandato.getFielId()));
        try {
            mandato.homologar(atorAtual(), LocalDate.now(clock), Instant.now(clock));
        } catch (IllegalStateException ex) {
            throw new OrganizacaoNegocioException(ex.getMessage());
        }
        mandatoRepository.save(mandato);
        investir(mandato);
        return mandato;
    }

    /**
     * Veta a proposta, informando o motivo.
     *
     * @param mandatoId mandato proposto
     * @param motivo motivo do veto (obrigatório)
     * @return o mandato vetado
     * @throws OrganizacaoNegocioException se o mandato não existir ou não puder ser vetado
     */
    public MandatoCoordenacao vetar(UUID mandatoId, String motivo) {
        MandatoCoordenacao mandato = carregar(mandatoId);
        try {
            mandato.vetar(atorAtual(), motivo, Instant.now(clock));
        } catch (IllegalStateException | IllegalArgumentException ex) {
            throw new OrganizacaoNegocioException(ex.getMessage());
        }
        mandatoRepository.save(mandato);
        return mandato;
    }

    /**
     * Encerra um mandato homologado e devolve o fiel ao papel de membro ativo.
     *
     * @param mandatoId mandato homologado
     * @return o mandato encerrado
     * @throws OrganizacaoNegocioException se o mandato não existir ou não puder ser encerrado
     */
    public MandatoCoordenacao encerrar(UUID mandatoId) {
        MandatoCoordenacao mandato = carregar(mandatoId);
        try {
            mandato.encerrar(Instant.now(clock));
        } catch (IllegalStateException ex) {
            throw new OrganizacaoNegocioException(ex.getMessage());
        }
        mandatoRepository.save(mandato);
        desinvestir(mandato);
        return mandato;
    }

    /**
     * Encerra todos os mandatos homologados cujo prazo terminou, tornando a coordenação temporária de fato.
     *
     * @return quantidade de mandatos encerrados
     */
    public int encerrarVencidos() {
        List<MandatoCoordenacao> vencidos = mandatoRepository.findHomologadosVencidosAntesDe(LocalDate.now(clock));
        vencidos.forEach(m -> encerrar(m.getId()));
        return vencidos.size();
    }

    /**
     * Lista os mandatos de uma organização, do mais recente ao mais antigo.
     *
     * @param organizacaoId identificador da organização
     * @return mandatos da organização
     */
    @Transactional(readOnly = true)
    public List<MandatoCoordenacao> listar(UUID organizacaoId) {
        return mandatoRepository.findByOrganizacaoId(organizacaoId);
    }

    /**
     * Exige que o agente seja idôneo para coordenar: formação, vivência sacramental atestada, termo de proteção
     * de menores vigente e envio conferido pelo Pároco, conforme {@link PoliticaIdoneidadeFuncao}.
     */
    private void exigirIdoneidade(OrganizacaoEntity organizacao, UUID fielId) {
        MembroGrupoEntity entity = membroRepository
                .findByFielIdAndOrganizacaoIdAndAtivoTrue(fielId, organizacao.getId())
                .orElseThrow(() -> new OrganizacaoNegocioException(
                        "O fiel precisa ser membro ativo da organização para coordená-la."));
        List<String> pendencias = PoliticaIdoneidadeFuncao.pendencias(true, organizacao.getSetorPastoral(),
                membroMapper.toDomain(entity).getIdoneidade(), LocalDate.now(clock));
        if (!pendencias.isEmpty()) {
            throw new OrganizacaoNegocioException("Agente sem idoneidade para coordenar: "
                    + String.join(" ", pendencias));
        }
    }

    private MandatoCoordenacao carregar(UUID mandatoId) {
        return mandatoRepository.findById(mandatoId)
                .orElseThrow(() -> new OrganizacaoNegocioException("Mandato não encontrado: " + mandatoId));
    }

    private void investir(MandatoCoordenacao mandato) {
        aplicarPapel(mandato, mandato.getFuncao().papelEquivalente());
        espelhar(mandato, fielRepository.findById(mandato.getFielId()).map(FielEntity::getNome).orElse(null));
    }

    private void desinvestir(MandatoCoordenacao mandato) {
        aplicarPapel(mandato, PapelOrganizacional.MEMBRO_ATIVO);
        espelhar(mandato, null);
    }

    private void aplicarPapel(MandatoCoordenacao mandato, PapelOrganizacional papel) {
        membroRepository.findByFielIdAndOrganizacaoIdAndAtivoTrue(mandato.getFielId(), mandato.getOrganizacaoId())
                .ifPresent(entity -> {
                    MembroGrupo membro = membroMapper.toDomain(entity);
                    membro.atribuirPapel(papel);
                    MembroGrupoEntity atualizado = entity;
                    membroMapper.aplicar(membro, atualizado);
                    membroRepository.save(atualizado);
                });
    }

    private void espelhar(MandatoCoordenacao mandato, String nome) {
        organizacaoRepository.findById(mandato.getOrganizacaoId()).ifPresent(organizacao -> {
            if (mandato.getFuncao() == FuncaoCoordenacao.COORDENADOR) {
                organizacao.setCoordenador(nome);
            } else {
                organizacao.setViceCoordenador(nome);
            }
            organizacaoRepository.save(organizacao);
        });
    }

    private String atorAtual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth == null ? "sistema" : auth.getName();
    }
}
