package br.com.nsfatima.gestao.clero.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.nsfatima.gestao.clero.api.v1.dto.ClerigoRequest;
import br.com.nsfatima.gestao.clero.api.v1.dto.ClerigoResponse;
import br.com.nsfatima.gestao.clero.api.v1.dto.EnderecoClerigoDto;
import br.com.nsfatima.gestao.clero.domain.model.FuncaoEclesiastica;
import br.com.nsfatima.gestao.clero.domain.model.GrauOrdem;
import br.com.nsfatima.gestao.clero.infrastructure.observability.ClerigoAuditPublisher;
import br.com.nsfatima.gestao.clero.infrastructure.persistence.entity.ClerigoEntity;
import br.com.nsfatima.gestao.clero.infrastructure.persistence.entity.EncargoClericalEntity;
import br.com.nsfatima.gestao.clero.infrastructure.persistence.mapper.ClerigoMapper;
import br.com.nsfatima.gestao.clero.infrastructure.persistence.repository.ClerigoJpaRepository;
import br.com.nsfatima.gestao.clero.infrastructure.persistence.repository.EncargoClericalJpaRepository;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.entity.UsuarioEntity;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoUnidadeTerritorial;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.UnidadeTerritorialEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.UnidadeTerritorialJpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GerenciarClerigoUseCaseTest {

    @Mock private ClerigoJpaRepository clerigos;
    @Mock private EncargoClericalJpaRepository encargos;
    @Mock private UnidadeTerritorialJpaRepository unidades;
    @Mock private UsuarioJpaRepository usuarios;
    @Mock private ClerigoAuditPublisher auditoria;

    private ClerigoMapper mapper;
    private GerenciarClerigoUseCase useCase;

    @BeforeEach
    void setUp() {
        mapper = new ClerigoMapper("Paróquia Teste");
        useCase = new GerenciarClerigoUseCase(clerigos, encargos, unidades, usuarios, auditoria, mapper);
    }

    @Test
    void deveCriarClerigoComEncargo() {
        UUID unidadeId = UUID.randomUUID();
        UnidadeTerritorialEntity unidade = new UnidadeTerritorialEntity();
        unidade.setId(unidadeId);
        unidade.setTipo(TipoUnidadeTerritorial.CLERO);
        when(unidades.findByTipoAndAtivoTrue(TipoUnidadeTerritorial.CLERO)).thenReturn(List.of(unidade));

        ClerigoRequest request = new ClerigoRequest(
                "João da Silva",
                "Padre João",
                LocalDate.of(1980, 1, 1),
                "BRA",
                "12345678901",
                "1234567",
                null,
                "padre@igreja.test",
                "11999998888",
                true,
                "1133334444",
                new EnderecoClerigoDto("08000-000", "Rua da Igreja", "1", null, "Centro", "São Paulo", "SP"),
                GrauOrdem.PADRE_DIOCESANO,
                "Diocese de Teste",
                LocalDate.of(2005, 1, 1),
                LocalDate.of(2006, 1, 1),
                null,
                FuncaoEclesiastica.PAROCO);

        ClerigoResponse response = useCase.criar(request, "admin");

        assertThat(response.nomeCivil()).isEqualTo("João da Silva");
        assertThat(response.nomeEclesiastico()).isEqualTo("Padre João");
        verify(clerigos).save(any(ClerigoEntity.class));
        verify(encargos).save(any(EncargoClericalEntity.class));
    }

    @Test
    void deveListarEObterClerigos() {
        UUID id = UUID.randomUUID();
        ClerigoEntity entity = new ClerigoEntity();
        entity.setId(id);
        entity.setNomeCivil("João da Silva");
        entity.setNomeEclesiastico("Padre João");
        entity.setDataNascimento(LocalDate.of(1980, 1, 1));
        entity.setPaisOrigem("BRA");
        entity.setCpf("12345678901");
        entity.setEmail("padre@test.com");
        entity.setCelular("11999998888");
        entity.setGrauOrdem(GrauOrdem.PADRE_DIOCESANO);
        entity.setDioceseIncardinacao("Diocese de Teste");
        entity.setAtivo(true);

        when(clerigos.findByAtivoTrueOrderByNomeEclesiasticoAsc()).thenReturn(List.of(entity));
        when(clerigos.findById(id)).thenReturn(Optional.of(entity));

        var lista = useCase.listar();
        assertThat(lista).hasSize(1);
        assertThat(lista.get(0).nomeEclesiastico()).isEqualTo("Padre João");

        var obtido = useCase.obter(id);
        assertThat(obtido.nomeEclesiastico()).isEqualTo("Padre João");

        var diretorio = useCase.diretorio();
        assertThat(diretorio).hasSize(1);
        assertThat(diretorio.get(0).nomeEclesiastico()).isEqualTo("Padre João");
    }

    @Test
    void deveObterPorUsuario() {
        UUID usuarioId = UUID.randomUUID();
        UUID clerigoId = UUID.randomUUID();

        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setId(usuarioId);
        usuario.setClerigoId(clerigoId);

        ClerigoEntity entity = new ClerigoEntity();
        entity.setId(clerigoId);
        entity.setNomeCivil("João da Silva");
        entity.setNomeEclesiastico("Padre João");
        entity.setDataNascimento(LocalDate.of(1980, 1, 1));
        entity.setPaisOrigem("BRA");
        entity.setEmail("padre@test.com");
        entity.setCelular("11999998888");
        entity.setGrauOrdem(GrauOrdem.PADRE_DIOCESANO);
        entity.setDioceseIncardinacao("Diocese de Teste");
        entity.setAtivo(true);

        when(usuarios.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(clerigos.findById(clerigoId)).thenReturn(Optional.of(entity));

        ClerigoResponse response = useCase.obterPorUsuario(usuarioId);
        assertThat(response.nomeEclesiastico()).isEqualTo("Padre João");
    }

    @Test
    void deveInativarClerigoEUsuarioAssociado() {
        UUID id = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();

        ClerigoEntity entity = new ClerigoEntity();
        entity.setId(id);
        entity.setAtivo(true);

        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setId(usuarioId);
        usuario.setClerigoId(id);
        usuario.setEnabled(true);

        EncargoClericalEntity encargo = new EncargoClericalEntity();
        encargo.setId(UUID.randomUUID());
        encargo.setClerigoId(id);
        encargo.setAtual(true);

        when(clerigos.findById(id)).thenReturn(Optional.of(entity));
        when(encargos.findByClerigoIdAndAtualTrue(id)).thenReturn(List.of(encargo));
        when(usuarios.findByClerigoId(id)).thenReturn(Optional.of(usuario));

        useCase.inativar(id, "admin", UUID.randomUUID());

        assertThat(entity.isAtivo()).isFalse();
        assertThat(encargo.isAtual()).isFalse();
        assertThat(usuario.isEnabled()).isFalse();
        verify(usuarios).save(usuario);
        verify(auditoria).publicarInativacao(any(), any());
    }

    @Test
    void deveListarEncargosHistoricos() {
        UUID clerigoId = UUID.randomUUID();
        ClerigoEntity entity = new ClerigoEntity();
        entity.setId(clerigoId);
        entity.setAtivo(true);

        EncargoClericalEntity encargo = new EncargoClericalEntity();
        encargo.setId(UUID.randomUUID());
        encargo.setClerigoId(clerigoId);
        encargo.setUnidadeTerritorialId(UUID.randomUUID());
        encargo.setFuncao(FuncaoEclesiastica.PAROCO);
        encargo.setAtual(true);

        when(clerigos.findById(clerigoId)).thenReturn(Optional.of(entity));
        when(encargos.findByClerigoIdOrderByCriadoEmUtcDesc(clerigoId)).thenReturn(List.of(encargo));

        var lista = useCase.listarEncargos(clerigoId);
        assertThat(lista).hasSize(1);
        assertThat(lista.get(0).funcao()).isEqualTo(FuncaoEclesiastica.PAROCO);
    }
}
