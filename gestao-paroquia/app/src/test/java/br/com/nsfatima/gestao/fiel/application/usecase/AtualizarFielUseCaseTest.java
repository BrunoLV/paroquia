package br.com.nsfatima.gestao.fiel.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.nsfatima.gestao.fiel.api.v1.dto.AtualizarFielRequest;
import br.com.nsfatima.gestao.fiel.api.v1.dto.EnderecoDto;
import br.com.nsfatima.gestao.fiel.api.v1.dto.FielResponse;
import br.com.nsfatima.gestao.fiel.domain.model.Sexo;
import br.com.nsfatima.gestao.fiel.domain.service.ValidacaoDocumentoCivil;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.entity.FielEntity;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.mapper.FielMapper;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository.FielJpaRepository;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoUnidadeTerritorial;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.UnidadeTerritorialEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.UnidadeTerritorialJpaRepository;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AtualizarFielUseCaseTest {

    @Mock private FielJpaRepository repository;
    @Mock private FielMapper mapper;
    @Mock private UnidadeTerritorialJpaRepository unidades;
    @Mock private ValidacaoDocumentoCivil validacaoDocumento;
    @Captor private ArgumentCaptor<FielEntity> fielCaptor;

    private final UUID fielId = UUID.randomUUID();
    private final FielResponse resposta = mockResponse();

    @BeforeEach
    void setUp() {
        lenient().when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(mapper.toResponse(any())).thenReturn(resposta);
    }

    private AtualizarFielUseCase useCase() {
        return new AtualizarFielUseCase(repository, mapper, unidades, validacaoDocumento);
    }

    private FielEntity fielExistente() {
        FielEntity fiel = new FielEntity();
        fiel.setId(fielId);
        fiel.setSexo(Sexo.MASCULINO);
        fiel.setPaisOrigem("BRA");
        return fiel;
    }

    private static FielResponse mockResponse() {
        return org.mockito.Mockito.mock(FielResponse.class);
    }

    private AtualizarFielRequest requestVazio() {
        return new AtualizarFielRequest(null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null);
    }

    @Test
    void execute_deveLancarQuandoFielNaoEncontrado() {
        when(repository.findById(fielId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase().execute(fielId, requestVazio(), "system"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Fiel não encontrado.");
    }

    @Test
    void execute_deveAtualizarTodosOsCamposInformados() {
        when(repository.findById(fielId)).thenReturn(Optional.of(fielExistente()));
        UUID unidadeId = UUID.randomUUID();
        UnidadeTerritorialEntity unidade = unidadeAtiva(TipoUnidadeTerritorial.MATRIZ);
        when(unidades.findById(unidadeId)).thenReturn(Optional.of(unidade));

        AtualizarFielRequest request = new AtualizarFielRequest(
                "Novo Nome", Sexo.FEMININO, LocalDate.of(1990, 1, 1), "BRA", "529.982.247-25", "123456789",
                null, null, "novo@email.com", "11988887777", Boolean.TRUE, "1133334444",
                new EnderecoDto("01000-000", "Rua A", "1", "Casa", "Centro", "São Paulo", "SP"),
                unidadeId, Boolean.TRUE, Boolean.FALSE);

        FielResponse resultado = useCase().execute(fielId, request, "system");

        assertThat(resultado).isSameAs(resposta);
        verify(repository).save(fielCaptor.capture());
        FielEntity salvo = fielCaptor.getValue();
        assertThat(salvo.getNome()).isEqualTo("Novo Nome");
        assertThat(salvo.getSexo()).isEqualTo(Sexo.FEMININO);
        assertThat(salvo.getDataNascimento()).isEqualTo(LocalDate.of(1990, 1, 1));
        assertThat(salvo.getCpf()).isEqualTo("52998224725");
        assertThat(salvo.getRg()).isEqualTo("123456789");
        assertThat(salvo.getEmail()).isEqualTo("novo@email.com");
        assertThat(salvo.getCelular()).isEqualTo("11988887777");
        assertThat(salvo.isWhatsapp()).isTrue();
        assertThat(salvo.getTelefoneFixo()).isEqualTo("1133334444");
        assertThat(salvo.getUnidadeTerritorialId()).isEqualTo(unidadeId);
        assertThat(salvo.getEnderecoLogradouro()).isEqualTo("Rua A");
        assertThat(salvo.getEnderecoCep()).isEqualTo("01000000");
        assertThat(salvo.getEnderecoEstado()).isEqualTo("SP");
        verify(validacaoDocumento).validarPais("BRA");
        verify(validacaoDocumento).validarDocumento("BRA", "52998224725", null, null);
    }

    @Test
    void execute_naoDeveAlterarNadaQuandoRequestTotalmenteVazio() {
        FielEntity existente = fielExistente();
        when(repository.findById(fielId)).thenReturn(Optional.of(existente));

        useCase().execute(fielId, requestVazio(), "system");

        verify(repository).save(fielCaptor.capture());
        assertThat(fielCaptor.getValue().getNome()).isNull();
        assertThat(fielCaptor.getValue().getSexo()).isEqualTo(Sexo.MASCULINO);
        verify(validacaoDocumento).validarPais("BRA");
    }

    @Test
    void execute_deveLancarQuandoTentaLimparSexoJaDefinido() {
        when(repository.findById(fielId)).thenReturn(Optional.of(fielExistente()));
        AtualizarFielRequest request = requestComSexo(Sexo.NAO_INFORMADO);

        assertThatThrownBy(() -> useCase().execute(fielId, request, "system"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Sexo deve ser masculino ou feminino.");
    }

    @Test
    void execute_devePermitirSexoNaoInformadoQuandoAindaNaoDefinido() {
        FielEntity fiel = fielExistente();
        fiel.setSexo(null);
        when(repository.findById(fielId)).thenReturn(Optional.of(fiel));

        useCase().execute(fielId, requestComSexo(Sexo.NAO_INFORMADO), "system");

        verify(repository).save(fielCaptor.capture());
        assertThat(fielCaptor.getValue().getSexo()).isEqualTo(Sexo.NAO_INFORMADO);
    }

    @Test
    void execute_deveLimparCpfParaNuloQuandoSemDigitos() {
        when(repository.findById(fielId)).thenReturn(Optional.of(fielExistente()));
        AtualizarFielRequest request = new AtualizarFielRequest(
                null, null, null, null, "---", null, null, null, null, null,
                null, null, null, null, null, null);

        useCase().execute(fielId, request, "system");

        verify(repository).save(fielCaptor.capture());
        assertThat(fielCaptor.getValue().getCpf()).isNull();
    }

    @Test
    void execute_deveUsarEstadoQuandoUfNaoInformado() {
        when(repository.findById(fielId)).thenReturn(Optional.of(fielExistente()));
        AtualizarFielRequest request = new AtualizarFielRequest(
                null, null, null, null, null, null, null, null, null, null,
                null, null, new EnderecoDto(null, null, null, null, null, null, null), null, null, null);

        useCase().execute(fielId, request, "system");

        verify(repository).save(fielCaptor.capture());
        assertThat(fielCaptor.getValue().getEnderecoEstado()).isNull();
    }

    @Test
    void execute_deveLancarQuandoUnidadeNaoEncontrada() {
        UUID unidadeId = UUID.randomUUID();
        when(repository.findById(fielId)).thenReturn(Optional.of(fielExistente()));
        when(unidades.findById(unidadeId)).thenReturn(Optional.empty());
        AtualizarFielRequest request = requestComUnidade(unidadeId);

        assertThatThrownBy(() -> useCase().execute(fielId, request, "system"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Igreja não encontrada.");
    }

    @Test
    void execute_deveLancarQuandoUnidadeInativa() {
        UUID unidadeId = UUID.randomUUID();
        UnidadeTerritorialEntity unidade = unidadeAtiva(TipoUnidadeTerritorial.MATRIZ);
        unidade.setAtivo(false);
        when(repository.findById(fielId)).thenReturn(Optional.of(fielExistente()));
        when(unidades.findById(unidadeId)).thenReturn(Optional.of(unidade));
        AtualizarFielRequest request = requestComUnidade(unidadeId);

        assertThatThrownBy(() -> useCase().execute(fielId, request, "system"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Igreja deve ser Matriz ou Comunidade ativa.");
    }

    @Test
    void execute_deveLancarQuandoUnidadeTipoInvalido() {
        UUID unidadeId = UUID.randomUUID();
        UnidadeTerritorialEntity unidade = unidadeAtiva(null);
        when(repository.findById(fielId)).thenReturn(Optional.of(fielExistente()));
        when(unidades.findById(unidadeId)).thenReturn(Optional.of(unidade));
        AtualizarFielRequest request = requestComUnidade(unidadeId);

        assertThatThrownBy(() -> useCase().execute(fielId, request, "system"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Igreja deve ser Matriz ou Comunidade ativa.");
    }

    private UnidadeTerritorialEntity unidadeAtiva(TipoUnidadeTerritorial tipo) {
        UnidadeTerritorialEntity unidade = new UnidadeTerritorialEntity();
        unidade.setAtivo(true);
        unidade.setTipo(tipo);
        return unidade;
    }

    private AtualizarFielRequest requestComSexo(Sexo sexo) {
        return new AtualizarFielRequest(null, sexo, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null);
    }

    private AtualizarFielRequest requestComUnidade(UUID unidadeId) {
        return new AtualizarFielRequest(null, null, null, null, null, null, null, null, null, null,
                null, null, null, unidadeId, null, null);
    }
}
