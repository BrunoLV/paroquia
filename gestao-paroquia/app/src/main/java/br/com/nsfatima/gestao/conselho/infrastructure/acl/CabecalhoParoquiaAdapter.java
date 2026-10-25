package br.com.nsfatima.gestao.conselho.infrastructure.acl;

import br.com.nsfatima.gestao.calendario.application.port.out.ConsultaInstitucionalPort;
import br.com.nsfatima.gestao.calendario.application.port.out.ConsultaInstitucionalPort.DadosInstitucionais;
import br.com.nsfatima.gestao.calendario.application.usecase.configuracao.ObterLogoParoquiaUseCase;
import br.com.nsfatima.gestao.calendario.domain.model.VarianteFundoLogo;
import br.com.nsfatima.gestao.conselho.application.dto.CabecalhoParoquia;
import br.com.nsfatima.gestao.conselho.application.port.out.CabecalhoParoquiaPort;
import org.springframework.stereotype.Component;

/**
 * Camada anticorrupção sobre a identidade institucional: dados canônicos da paróquia e da diocese vêm da
 * consulta institucional e o logotipo para fundo claro (papel branco) vem do caso de uso de logotipo.
 */
@Component
public class CabecalhoParoquiaAdapter implements CabecalhoParoquiaPort {

    private final ConsultaInstitucionalPort institucional;
    private final ObterLogoParoquiaUseCase logos;

    public CabecalhoParoquiaAdapter(ConsultaInstitucionalPort institucional, ObterLogoParoquiaUseCase logos) {
        this.institucional = institucional;
        this.logos = logos;
    }

    @Override
    public CabecalhoParoquia obter() {
        DadosInstitucionais dados = institucional.obterDadosInstitucionais();
        return logos.execute(VarianteFundoLogo.CLARO)
                .map(logo -> new CabecalhoParoquia(dados.nome(), dados.diocese(), dados.endereco(), logo.bytes(),
                        logo.contentType()))
                .orElseGet(() -> new CabecalhoParoquia(dados.nome(), dados.diocese(), dados.endereco(), null, null));
    }
}
