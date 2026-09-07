package br.com.nsfatima.gestao.fiel.infrastructure.persistence.mapper;

import br.com.nsfatima.gestao.fiel.api.v1.dto.FielContatoResponse;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.entity.FielEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.UnidadeTerritorialJpaRepository;
import java.lang.reflect.RecordComponent;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class FielMapperContatoResponseTest {

    @Test
    void deveProjetarSomenteIdentificacaoMinimaENaoExporDadosCadastrais() {
        FielEntity fiel = fielComDadosCadastrais();
        FielMapper mapper = new FielMapper(mock(UnidadeTerritorialJpaRepository.class));

        FielContatoResponse resposta = mapper.toContatoResponse(fiel);

        assertThat(resposta.fielId()).isEqualTo(fiel.getId());
        assertThat(resposta.nome()).isEqualTo("Ana da Silva");
        assertThat(resposta.contato().celularFormatado()).isEqualTo("(11) 98888-7777");
        assertThat(resposta.contato().email()).isEqualTo("ana@example.com");
        assertThat(FielContatoResponse.class.getRecordComponents())
                .extracting(RecordComponent::getName)
                .containsExactly("fielId", "nome", "contato");
    }

    private FielEntity fielComDadosCadastrais() {
        FielEntity fiel = new FielEntity();
        fiel.setId(UUID.randomUUID());
        fiel.setNome("Ana da Silva");
        fiel.setCpf("529.982.247-25");
        fiel.setRg("123456789");
        fiel.setEmail("ana@example.com");
        fiel.setCelular("11988887777");
        fiel.setTelefoneFixo("1133334444");
        fiel.setWhatsapp(true);
        fiel.setEnderecoLogradouro("Rua das Flores");
        fiel.setEnderecoCep("01001000");
        return fiel;
    }
}
