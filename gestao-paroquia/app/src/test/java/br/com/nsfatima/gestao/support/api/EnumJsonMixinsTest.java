package br.com.nsfatima.gestao.support.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.nsfatima.gestao.calendario.domain.type.CategoriaEvento;
import br.com.nsfatima.gestao.calendario.domain.type.EventoStatusInput;
import br.com.nsfatima.gestao.calendario.domain.type.FormatoEvento;
import br.com.nsfatima.gestao.fiel.domain.model.Vocacao;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.boot.jackson.JacksonMixinModule;
import org.springframework.boot.jackson.JacksonMixinModuleEntries;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import tools.jackson.databind.json.JsonMapper;

/**
 * Garante que os mix-ins Jackson da camada {@code api} são descobertos pelo Spring Boot e preservam
 * a desserialização tolerante dos enums de domínio, que não carregam mais anotações de JSON.
 */
class EnumJsonMixinsTest {

    private AnnotationConfigApplicationContext contexto;
    private JsonMapper mapper;

    @BeforeEach
    void montarMapperComMixinsEscaneados() {
        contexto = new AnnotationConfigApplicationContext();
        AutoConfigurationPackages.register(contexto, "br.com.nsfatima.gestao");
        contexto.refresh();
        JacksonMixinModuleEntries entradas =
                JacksonMixinModuleEntries.scan(contexto, AutoConfigurationPackages.get(contexto));
        JacksonMixinModule modulo = new JacksonMixinModule();
        modulo.registerEntries(entradas, getClass().getClassLoader());
        mapper = JsonMapper.builder().addModule(modulo).build();
    }

    @AfterEach
    void fecharContexto() {
        contexto.close();
    }

    @Test
    void normalizaCaixaEEspacosNosEnumsDeRequisicao() {
        assertThat(mapper.readValue("\" confirmado \"", EventoStatusInput.class))
                .isEqualTo(EventoStatusInput.CONFIRMADO);
        assertThat(mapper.readValue("\"solenidade\"", CategoriaEvento.class))
                .isEqualTo(CategoriaEvento.SOLENIDADE);
    }

    @Test
    void preservaParticularidadesDeCadaEnum() {
        assertThat(mapper.readValue("\"\"", FormatoEvento.class)).isEqualTo(FormatoEvento.PRESENCIAL);
        assertThat(mapper.readValue("\"inexistente\"", Vocacao.class)).isNull();
        assertThat(mapper.readValue("\"cpp\"", TipoOrganizacao.class)).isEqualTo(TipoOrganizacao.CONSELHO);
    }

    @Test
    void rejeitaValorInvalidoDeEnumEstrito() {
        assertThatThrownBy(() -> mapper.readValue("\"inexistente\"", EventoStatusInput.class))
                .hasRootCauseInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void serializaEnumPeloNome() {
        assertThat(mapper.writeValueAsString(CategoriaEvento.PASTORAL)).isEqualTo("\"PASTORAL\"");
    }
}
