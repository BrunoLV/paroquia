package br.com.nsfatima.gestao.clero.infrastructure.persistence.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.nsfatima.gestao.clero.domain.model.FuncaoEclesiastica;
import br.com.nsfatima.gestao.clero.domain.model.GrauOrdem;
import br.com.nsfatima.gestao.clero.infrastructure.persistence.entity.ClerigoEntity;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ClerigoMapperTest {

    private final ClerigoMapper mapper = new ClerigoMapper("Paróquia Teste");

    @Test
    void deveMapearCompletoEInstitucional() {
        ClerigoEntity entity = new ClerigoEntity();
        entity.setId(UUID.randomUUID());
        entity.setNomeCivil("José da Silva");
        entity.setNomeEclesiastico("Padre José");
        entity.setDataNascimento(LocalDate.of(1975, 5, 20));
        entity.setPaisOrigem("BRA");
        entity.setCpf("12345678901");
        entity.setRg("MG1234567");
        entity.setEmail("padre.jose@igreja.test");
        entity.setCelular("11988887777");
        entity.setTelefoneFixo("1133332222");
        entity.setWhatsapp(true);
        entity.setEndereco("Rua das Flores, 10");
        entity.setGrauOrdem(GrauOrdem.PADRE_DIOCESANO);
        entity.setDioceseIncardinacao("Diocese de Teste");
        entity.setDataOrdenacaoDiaconal(LocalDate.of(2000, 1, 1));
        entity.setDataOrdenacaoPresbiteral(LocalDate.of(2001, 1, 1));
        entity.setAtivo(true);

        var completo = mapper.completo(entity, FuncaoEclesiastica.PAROCO);
        assertThat(completo.cpfFormatado()).isEqualTo("123.456.789-01");
        assertThat(completo.celularFormatado()).isEqualTo("(11) 98888-7777");
        assertThat(completo.telefoneFixoFormatado()).isEqualTo("(11) 3333-2222");
        assertThat(completo.funcaoAtual()).isEqualTo(FuncaoEclesiastica.PAROCO);

        var institucional = mapper.institucional(entity, FuncaoEclesiastica.PAROCO);
        assertThat(institucional.nomeEclesiastico()).isEqualTo("Padre José");
        assertThat(institucional.paroquia()).isEqualTo("Paróquia Teste");
    }
}
