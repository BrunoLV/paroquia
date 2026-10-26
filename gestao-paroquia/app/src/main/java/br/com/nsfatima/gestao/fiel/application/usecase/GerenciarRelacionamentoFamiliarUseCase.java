package br.com.nsfatima.gestao.fiel.application.usecase;

import br.com.nsfatima.gestao.clero.domain.model.TipoPessoaFamiliar;
import br.com.nsfatima.gestao.clero.infrastructure.persistence.entity.ClerigoEntity;
import br.com.nsfatima.gestao.clero.infrastructure.persistence.repository.ClerigoJpaRepository;
import br.com.nsfatima.gestao.fiel.api.v1.dto.RelacionamentoFamiliarResponse;
import br.com.nsfatima.gestao.fiel.domain.model.Sexo;
import br.com.nsfatima.gestao.fiel.domain.model.TipoRelacionamentoFamiliar;
import br.com.nsfatima.gestao.fiel.infrastructure.observability.FielAuditPublisher;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.entity.FielEntity;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.entity.RelacionamentoFamiliarEntity;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository.FielJpaRepository;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository.RelacionamentoFamiliarJpaRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Orquestra criação, listagem e remoção de vínculos familiares entre Fiéis e Clérigos. */
@Service
public class GerenciarRelacionamentoFamiliarUseCase {

    private final FielJpaRepository fieis;
    private final ClerigoJpaRepository clerigos;
    private final RelacionamentoFamiliarJpaRepository relacoes;
    private final FielAuditPublisher auditoria;

    public GerenciarRelacionamentoFamiliarUseCase(
            FielJpaRepository fieis,
            ClerigoJpaRepository clerigos,
            RelacionamentoFamiliarJpaRepository relacoes,
            FielAuditPublisher auditoria) {
        this.fieis = fieis;
        this.clerigos = clerigos;
        this.relacoes = relacoes;
        this.auditoria = auditoria;
    }

    @Transactional
    public UUID criar(
            UUID origemId,
            TipoPessoaFamiliar tipoOrigem,
            UUID destinoId,
            TipoPessoaFamiliar tipoDestino,
            TipoRelacionamentoFamiliar tipo,
            UUID atorId) {
        if (origemId.equals(destinoId) && tipoOrigem == tipoDestino) {
            throw new IllegalArgumentException("Uma pessoa não pode relacionar-se consigo mesma.");
        }

        Sexo sexoOrigem = obterSexo(origemId, tipoOrigem);
        Sexo sexoDestino = obterSexo(destinoId, tipoDestino);
        validarSexo(sexoOrigem, sexoDestino);

        if (tipo == TipoRelacionamentoFamiliar.CONJUGE && sexoOrigem == sexoDestino) {
            throw new IllegalArgumentException("Cônjuges devem ter sexos distintos.");
        }

        UUID unidadeOrigem = tipoOrigem == TipoPessoaFamiliar.FIEL
                ? fieis.findById(origemId).map(FielEntity::getUnidadeTerritorialId).orElse(null)
                : null;

        UUID par = UUID.randomUUID();
        salvar(par, origemId, tipoOrigem, destinoId, tipoDestino, tipo);
        salvar(par, destinoId, tipoDestino, origemId, tipoOrigem, inversa(tipo));

        auditoria.publicarCriacaoRelacionamento(par, origemId, atorId, unidadeOrigem, tipo.name());
        return par;
    }

    @Transactional
    public UUID criar(UUID fielId, UUID destinoId, TipoRelacionamentoFamiliar tipo, UUID atorId) {
        return criar(fielId, TipoPessoaFamiliar.FIEL, destinoId, TipoPessoaFamiliar.FIEL, tipo, atorId);
    }

    @Transactional(readOnly = true)
    public List<RelacionamentoFamiliarResponse> listar(UUID pessoaId, TipoPessoaFamiliar tipoPessoa) {
        return relacoes.findByPessoaOrigemIdAndTipoPessoaOrigemAndDeletedAtIsNull(pessoaId, tipoPessoa).stream()
                .map(r -> {
                    InfoPessoaDestino info = obterInfoDestino(r.getPessoaDestinoId(), r.getTipoPessoaDestino());
                    return new RelacionamentoFamiliarResponse(
                            r.getId(),
                            r.getPessoaDestinoId(),
                            r.getTipoPessoaDestino(),
                            info.nome(),
                            rotulo(r.getTipo(), info.sexo()),
                            r.getTipo());
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RelacionamentoFamiliarResponse> listar(UUID fielId) {
        return listar(fielId, TipoPessoaFamiliar.FIEL);
    }

    @Transactional
    public void remover(UUID relacionamentoId, UUID atorId) {
        RelacionamentoFamiliarEntity relacao = relacoes.findById(relacionamentoId)
                .orElseThrow(() -> new IllegalArgumentException("Relacionamento não encontrado."));

        UUID unidadeOrigem = null;
        if (relacao.getTipoPessoaOrigem() == TipoPessoaFamiliar.FIEL) {
            unidadeOrigem = fieis.findById(relacao.getPessoaOrigemId())
                    .map(FielEntity::getUnidadeTerritorialId)
                    .orElse(null);
        }

        Instant agora = Instant.now();
        relacoes.findByParIdAndDeletedAtIsNull(relacao.getParId()).forEach(parte -> {
            parte.setDeletedAt(agora);
            relacoes.save(parte);
        });

        auditoria.publicarInativacaoRelacionamento(
                relacao.getParId(),
                relacao.getPessoaOrigemId(),
                atorId,
                unidadeOrigem);
    }

    private void salvar(
            UUID par,
            UUID origemId,
            TipoPessoaFamiliar tipoOrigem,
            UUID destinoId,
            TipoPessoaFamiliar tipoDestino,
            TipoRelacionamentoFamiliar tipo) {
        RelacionamentoFamiliarEntity r = new RelacionamentoFamiliarEntity();
        r.setId(UUID.randomUUID());
        r.setParId(par);
        r.setPessoaOrigemId(origemId);
        r.setTipoPessoaOrigem(tipoOrigem);
        r.setPessoaDestinoId(destinoId);
        r.setTipoPessoaDestino(tipoDestino);
        r.setTipo(tipo);
        relacoes.save(r);
    }

    private Sexo obterSexo(UUID id, TipoPessoaFamiliar tipoPessoa) {
        if (tipoPessoa == TipoPessoaFamiliar.FIEL) {
            return fieis.findById(id)
                    .map(FielEntity::getSexo)
                    .orElseThrow(() -> new IllegalArgumentException("Fiel não encontrado: " + id));
        }
        return clerigos.findById(id)
                .filter(ClerigoEntity::isAtivo)
                .map(c -> Sexo.MASCULINO)
                .orElseThrow(() -> new IllegalArgumentException("Clérigo não encontrado ou inativo: " + id));
    }

    private InfoPessoaDestino obterInfoDestino(UUID id, TipoPessoaFamiliar tipoPessoa) {
        if (tipoPessoa == TipoPessoaFamiliar.FIEL) {
            FielEntity f = fieis.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Fiel não encontrado: " + id));
            return new InfoPessoaDestino(f.getNome(), f.getSexo());
        }
        ClerigoEntity c = clerigos.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Clérigo não encontrado: " + id));
        return new InfoPessoaDestino(c.getNomeCivil(), Sexo.MASCULINO);
    }

    private TipoRelacionamentoFamiliar inversa(TipoRelacionamentoFamiliar tipo) {
        return switch (tipo) {
            case GENITOR -> TipoRelacionamentoFamiliar.FILHO;
            case FILHO -> TipoRelacionamentoFamiliar.GENITOR;
            default -> tipo;
        };
    }

    private void validarSexo(Sexo a, Sexo b) {
        if (a == Sexo.NAO_INFORMADO || b == Sexo.NAO_INFORMADO) {
            throw new IllegalStateException("Os dois envolvidos precisam ter sexo M ou F.");
        }
    }

    private String rotulo(TipoRelacionamentoFamiliar tipo, Sexo sexo) {
        return switch (tipo) {
            case CONJUGE -> sexo == Sexo.MASCULINO ? "Marido" : "Esposa";
            case GENITOR -> sexo == Sexo.MASCULINO ? "Pai" : "Mãe";
            case FILHO -> sexo == Sexo.MASCULINO ? "Filho" : "Filha";
            case IRMAO -> sexo == Sexo.MASCULINO ? "Irmão" : "Irmã";
        };
    }

    private record InfoPessoaDestino(String nome, Sexo sexo) {}
}
