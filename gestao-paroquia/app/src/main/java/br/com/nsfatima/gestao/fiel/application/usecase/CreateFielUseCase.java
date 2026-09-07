package br.com.nsfatima.gestao.fiel.application.usecase;

import br.com.nsfatima.gestao.fiel.api.v1.dto.CreateFielRequest;
import br.com.nsfatima.gestao.fiel.api.v1.dto.FielResponse;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.entity.FielEntity;
import br.com.nsfatima.gestao.fiel.domain.model.Sexo;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.mapper.FielMapper;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository.FielJpaRepository;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoUnidadeTerritorial;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.UnidadeTerritorialJpaRepository;
import java.time.Instant;
import java.util.UUID;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateFielUseCase {
    private static final Set<String> PAISES_ISO3 = java.util.Arrays.stream(Locale.getISOCountries()).map(c -> new Locale.Builder().setRegion(c).build().getISO3Country()).collect(Collectors.toUnmodifiableSet());
    private final FielJpaRepository repository;
    private final FielMapper mapper;
    private final UnidadeTerritorialJpaRepository unidades;
    public CreateFielUseCase(FielJpaRepository repository, FielMapper mapper, UnidadeTerritorialJpaRepository unidades) { this.repository = repository; this.mapper = mapper; this.unidades = unidades; }
    @Transactional
    public FielResponse execute(CreateFielRequest request, String actor) {
        validarPais(request.paisOrigem()); validarDocumento(request.paisOrigem(), request.cpf(), request.tipoDocumentoEstrangeiro(), request.numeroDocumentoEstrangeiro());
        if (request.sexo() == Sexo.NAO_INFORMADO) throw new IllegalArgumentException("Sexo deve ser masculino ou feminino.");
        validarUnidade(request.unidadeTerritorialId());
        FielEntity fiel = new FielEntity(); fiel.setId(UUID.randomUUID()); aplicar(fiel, request);
        Instant agora = Instant.now(); fiel.setCriadoEmUtc(agora); fiel.setAtualizadoEmUtc(agora);
        fiel.setCriadoPor(actor == null ? "system" : actor); fiel.setAtualizadoPor(actor == null ? "system" : actor);
        return mapper.toResponse(repository.save(fiel));
    }
    private void aplicar(FielEntity fiel, CreateFielRequest r) {
        fiel.setNome(r.nome()); fiel.setSexo(r.sexo()); fiel.setDataNascimento(r.dataNascimento()); fiel.setPaisOrigem(r.paisOrigem()); fiel.setCpf(r.cpf()); fiel.setRg(r.rg()); fiel.setDocumentoEstrangeiroTipo(r.tipoDocumentoEstrangeiro()); fiel.setDocumentoEstrangeiroNumero(r.numeroDocumentoEstrangeiro()); fiel.setEmail(r.email()); fiel.setCelular(r.celular()); fiel.setWhatsapp(r.isWhatsapp()); fiel.setTelefoneFixo(r.telefoneFixo()); fiel.setUnidadeTerritorialId(r.unidadeTerritorialId()); fiel.setDizimista(r.isDizimista()); fiel.setPermitirDivulgacaoAniversario(r.permitirDivulgacaoAniversario());
        fiel.setEnderecoLogradouro(r.endereco().logradouro()); fiel.setEnderecoNumero(r.endereco().numero()); fiel.setEnderecoComplemento(r.endereco().complemento()); fiel.setEnderecoBairro(r.endereco().bairro()); fiel.setEnderecoCep(r.endereco().cep()); fiel.setEnderecoCidade(r.endereco().cidade()); fiel.setEnderecoEstado(r.endereco().estado());
    }
    static void validarDocumento(String pais, String cpf, String tipo, String numero) {
        boolean brasil = "BRA".equalsIgnoreCase(pais); boolean cpfPresente = cpf != null && cpf.replaceAll("\\D", "").length() == 11;
        if (brasil && (!cpfPresente || !cpfValido(cpf.replaceAll("\\D", "")))) throw new IllegalArgumentException("CPF brasileiro é obrigatório e deve ser válido.");
        if (!brasil && (tipo == null || numero == null || numero.isBlank())) throw new IllegalArgumentException("RNE/RNM ou passaporte é obrigatório para estrangeiros.");
        if (!brasil && !"RNE_RNM".equals(tipo) && !"PASSAPORTE".equals(tipo)) throw new IllegalArgumentException("Documento estrangeiro deve ser RNE_RNM ou PASSAPORTE.");
    }
    static void validarPais(String pais) { if (pais == null || !PAISES_ISO3.contains(pais.toUpperCase(Locale.ROOT))) throw new IllegalArgumentException("País de origem deve usar ISO-3166 alpha-3."); }
    private static boolean cpfValido(String cpf) { if (cpf.chars().distinct().count() == 1) return false; return digito(cpf, 9) == cpf.charAt(9) - '0' && digito(cpf, 10) == cpf.charAt(10) - '0'; }
    private static int digito(String cpf, int limite) { int soma = 0; for (int i = 0; i < limite; i++) soma += (cpf.charAt(i) - '0') * (limite + 1 - i); int resto = soma % 11; return resto < 2 ? 0 : 11 - resto; }
    private void validarUnidade(UUID id) { var unidade = unidades.findById(id).orElseThrow(() -> new IllegalArgumentException("Igreja não encontrada.")); if (!unidade.isAtivo() || (unidade.getTipo() != TipoUnidadeTerritorial.MATRIZ && unidade.getTipo() != TipoUnidadeTerritorial.COMUNIDADE)) throw new IllegalArgumentException("Igreja deve ser Matriz ou Comunidade ativa."); }
}
