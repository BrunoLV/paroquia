package br.com.nsfatima.gestao.fiel.application.usecase;

import br.com.nsfatima.gestao.fiel.api.v1.dto.FielResponse;
import br.com.nsfatima.gestao.fiel.api.v1.dto.UpdateFielRequest;
import br.com.nsfatima.gestao.fiel.domain.model.Sexo;
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
public class UpdateFielUseCase {

    private static final String ACTOR_SYSTEM = "system";

    private final FielJpaRepository repository;
    private final FielMapper mapper;
    private final UnidadeTerritorialJpaRepository unidades;

    public UpdateFielUseCase(
            FielJpaRepository repository,
            FielMapper mapper,
            UnidadeTerritorialJpaRepository unidades) {
        this.repository = repository;
        this.mapper = mapper;
        this.unidades = unidades;
    }

    @Transactional
    public FielResponse execute(UUID id, UpdateFielRequest r, String actor) {
        var fiel = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Fiel não encontrado."));

        if (r.nome() != null) {
            fiel.setNome(r.nome());
        }
        if (r.sexo() != null) {
            if (r.sexo() == Sexo.NAO_INFORMADO) {
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
            fiel.setCpf(r.cpf());
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

        if (r.endereco() != null) {
            fiel.setEnderecoLogradouro(r.endereco().logradouro());
            fiel.setEnderecoNumero(r.endereco().numero());
            fiel.setEnderecoComplemento(r.endereco().complemento());
            fiel.setEnderecoBairro(r.endereco().bairro());
            fiel.setEnderecoCep(r.endereco().cep());
            fiel.setEnderecoCidade(r.endereco().cidade());
            fiel.setEnderecoEstado(r.endereco().estado());
        }

        CreateFielUseCase.validarPais(fiel.getPaisOrigem());
        CreateFielUseCase.validarDocumento(
                fiel.getPaisOrigem(),
                fiel.getCpf(),
                fiel.getDocumentoEstrangeiroTipo(),
                fiel.getDocumentoEstrangeiroNumero());

        fiel.setAtualizadoEmUtc(Instant.now());
        fiel.setAtualizadoPor(actor == null ? ACTOR_SYSTEM : actor);
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
