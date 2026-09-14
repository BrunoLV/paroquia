package br.com.nsfatima.gestao.fiel.application.usecase;

import br.com.nsfatima.gestao.fiel.api.v1.dto.CreateFielRequest;
import br.com.nsfatima.gestao.fiel.api.v1.dto.FielResponse;
import br.com.nsfatima.gestao.fiel.domain.model.Sexo;
import br.com.nsfatima.gestao.fiel.domain.service.ValidacaoDocumentoCivil;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.entity.FielEntity;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.mapper.FielMapper;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository.FielJpaRepository;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoUnidadeTerritorial;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.UnidadeTerritorialJpaRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Use case para criação de fiel e validação dos dados civis e de residência. */
@Service
public class CriarFielUseCase {

    private static final String ACTOR_SYSTEM = "system";

    private final FielJpaRepository repository;
    private final FielMapper mapper;
    private final UnidadeTerritorialJpaRepository unidades;
    private final ValidacaoDocumentoCivil validacaoDocumento;

    public CriarFielUseCase(
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
    public FielResponse execute(CreateFielRequest request, String actor) {
        validacaoDocumento.validarPais(request.paisOrigem());
        validacaoDocumento.validarDocumento(
                request.paisOrigem(),
                request.cpf(),
                request.tipoDocumentoEstrangeiro(),
                request.numeroDocumentoEstrangeiro());

        if (request.sexo() == Sexo.NAO_INFORMADO) {
            throw new IllegalArgumentException("Sexo deve ser masculino ou feminino.");
        }
        validarUnidade(request.unidadeTerritorialId());

        FielEntity fiel = new FielEntity();
        fiel.setId(UUID.randomUUID());
        aplicar(fiel, request);

        return mapper.toResponse(repository.save(fiel));
    }

    private void aplicar(FielEntity fiel, CreateFielRequest r) {
        fiel.setNome(r.nome());
        fiel.setSexo(r.sexo());
        fiel.setDataNascimento(r.dataNascimento());
        fiel.setPaisOrigem(r.paisOrigem());
        fiel.setCpf(r.cpf());
        fiel.setRg(r.rg());
        fiel.setDocumentoEstrangeiroTipo(r.tipoDocumentoEstrangeiro());
        fiel.setDocumentoEstrangeiroNumero(r.numeroDocumentoEstrangeiro());
        fiel.setEmail(r.email());
        fiel.setCelular(r.celular());
        fiel.setWhatsapp(r.isWhatsapp());
        fiel.setTelefoneFixo(r.telefoneFixo());
        fiel.setUnidadeTerritorialId(r.unidadeTerritorialId());
        fiel.setDizimista(r.isDizimista());
        fiel.setPermitirDivulgacaoAniversario(r.permitirDivulgacaoAniversario());

        fiel.setEnderecoLogradouro(r.endereco().logradouro());
        fiel.setEnderecoNumero(r.endereco().numero());
        fiel.setEnderecoComplemento(r.endereco().complemento());
        fiel.setEnderecoBairro(r.endereco().bairro());
        fiel.setEnderecoCep(r.endereco().cep());
        fiel.setEnderecoCidade(r.endereco().cidade());
        fiel.setEnderecoEstado(r.endereco().estado());
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
