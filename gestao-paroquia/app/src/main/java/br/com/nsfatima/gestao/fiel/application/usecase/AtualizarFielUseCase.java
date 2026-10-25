package br.com.nsfatima.gestao.fiel.application.usecase;

import br.com.nsfatima.gestao.fiel.api.v1.dto.FielResponse;
import br.com.nsfatima.gestao.fiel.api.v1.dto.AtualizarFielRequest;
import br.com.nsfatima.gestao.fiel.domain.model.Sexo;
import br.com.nsfatima.gestao.fiel.domain.service.ValidacaoDocumentoCivil;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.mapper.FielMapper;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository.FielJpaRepository;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoUnidadeTerritorial;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.UnidadeTerritorialJpaRepository;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Use case para atualização de cadastro civil do fiel. */
@Service
public class AtualizarFielUseCase {

    private static final String ACTOR_SYSTEM = "system";

    private final FielJpaRepository repository;
    private final FielMapper mapper;
    private final UnidadeTerritorialJpaRepository unidades;
    private final ValidacaoDocumentoCivil validacaoDocumento;

    public AtualizarFielUseCase(
            FielJpaRepository repository,
            FielMapper mapper,
            UnidadeTerritorialJpaRepository unidades,
            ValidacaoDocumentoCivil validacaoDocumento) {
        this.repository = repository;
        this.mapper = mapper;
        this.unidades = unidades;
        this.validacaoDocumento = validacaoDocumento;
    }

    @Transactional
    public FielResponse execute(UUID id, AtualizarFielRequest r, String actor) {
        var fiel = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Fiel não encontrado."));

        if (r.nome() != null) {
            fiel.setNome(r.nome());
        }
        if (r.sexo() != null) {
            if (r.sexo() == Sexo.NAO_INFORMADO && fiel.getSexo() != null && fiel.getSexo() != Sexo.NAO_INFORMADO) {
                throw new IllegalArgumentException("Sexo deve ser masculino ou feminino.");
            }
            fiel.setSexo(r.sexo());
        }
        if (r.dataNascimento() != null) {
            fiel.setDataNascimento(r.dataNascimento());
        }
        if (r.paisOrigem() != null) {
            fiel.setPaisOrigem(r.paisOrigem());
        }
        if (r.cpf() != null) {
            String cleanCpf = r.cpf().replaceAll("\\D", "");
            fiel.setCpf(cleanCpf.isBlank() ? null : cleanCpf);
        }
        if (r.rg() != null) {
            fiel.setRg(r.rg());
        }
        if (r.tipoDocumentoEstrangeiro() != null) {
            fiel.setDocumentoEstrangeiroTipo(r.tipoDocumentoEstrangeiro());
        }
        if (r.numeroDocumentoEstrangeiro() != null) {
            fiel.setDocumentoEstrangeiroNumero(r.numeroDocumentoEstrangeiro());
        }
        if (r.email() != null) {
            fiel.setEmail(r.email());
        }
        if (r.celular() != null) {
            fiel.setCelular(r.celular());
        }
        if (r.isWhatsapp() != null) {
            fiel.setWhatsapp(r.isWhatsapp());
        }
        if (r.telefoneFixo() != null) {
            fiel.setTelefoneFixo(r.telefoneFixo());
        }
        if (r.unidadeTerritorialId() != null) {
            validarUnidade(r.unidadeTerritorialId());
            fiel.setUnidadeTerritorialId(r.unidadeTerritorialId());
        }
        if (r.isDizimista() != null) {
            fiel.setDizimista(r.isDizimista());
        }
        if (r.permitirDivulgacaoAniversario() != null) {
            fiel.setPermitirDivulgacaoAniversario(r.permitirDivulgacaoAniversario());
        }
        if (r.termoConsentimentoAceito() != null) {
            fiel.setTermoConsentimentoAceito(r.termoConsentimentoAceito());
            if (Boolean.TRUE.equals(r.termoConsentimentoAceito()) && fiel.getDataHoraConsentimentoUtc() == null) {
                fiel.setDataHoraConsentimentoUtc(Instant.now());
                fiel.setVersaoTermoConsentimento("v1.0");
            }
        }
        if (r.usoImagemAutorizado() != null) {
            fiel.setUsoImagemAutorizado(r.usoImagemAutorizado());
        }

        if (r.endereco() != null) {
            fiel.setEnderecoLogradouro(r.endereco().logradouro());
            fiel.setEnderecoNumero(r.endereco().numero());
            fiel.setEnderecoComplemento(r.endereco().complemento());
            fiel.setEnderecoBairro(r.endereco().bairro());
            fiel.setEnderecoCep(r.endereco().cep() != null ? r.endereco().cep().replaceAll("\\D", "") : null);
            fiel.setEnderecoCidade(r.endereco().cidade());
            String uf = r.endereco().uf() != null ? r.endereco().uf() : r.endereco().estado();
            fiel.setEnderecoEstado(uf != null ? uf.trim().toUpperCase() : null);
        }

        validacaoDocumento.validarPais(fiel.getPaisOrigem());
        validacaoDocumento.validarDocumento(
                fiel.getPaisOrigem(),
                fiel.getCpf(),
                fiel.getDocumentoEstrangeiroTipo(),
                fiel.getDocumentoEstrangeiroNumero());

        return mapper.toResponse(repository.save(fiel));
    }

    private void validarUnidade(UUID id) {
        var unidade = unidades.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Igreja não encontrada."));
        if (!unidade.isAtivo()
                || (unidade.getTipo() != TipoUnidadeTerritorial.MATRIZ
                && unidade.getTipo() != TipoUnidadeTerritorial.COMUNIDADE)) {
            throw new IllegalArgumentException("Igreja deve ser Matriz ou Comunidade ativa.");
        }
    }
}
