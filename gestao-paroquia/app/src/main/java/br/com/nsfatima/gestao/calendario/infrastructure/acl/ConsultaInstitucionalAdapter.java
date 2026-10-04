package br.com.nsfatima.gestao.calendario.infrastructure.acl;

import br.com.nsfatima.gestao.calendario.application.port.out.ConsultaInstitucionalPort;
import br.com.nsfatima.gestao.clero.infrastructure.persistence.repository.DioceseJpaRepository;
import br.com.nsfatima.gestao.clero.infrastructure.persistence.repository.GovernoEclesiasticoJpaRepository;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoUnidadeTerritorial;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.UnidadeTerritorialEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.UnidadeTerritorialJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Adaptador Anti-Corruption Layer (ACL) para leitura consolidada dos dados canônicos e civis da Paróquia.
 * Consulta a Unidade Territorial Matriz e o Governo Eclesiástico sem acoplar a camada de aplicação de
 * calendário aos detalhes de persistência de clero e organização.
 */
@Component
public class ConsultaInstitucionalAdapter implements ConsultaInstitucionalPort {

    private final UnidadeTerritorialJpaRepository unidadeTerritorialRepository;
    private final GovernoEclesiasticoJpaRepository governoEclesiasticoRepository;
    private final DioceseJpaRepository dioceseRepository;

    public ConsultaInstitucionalAdapter(
            UnidadeTerritorialJpaRepository unidadeTerritorialRepository,
            GovernoEclesiasticoJpaRepository governoEclesiasticoRepository,
            DioceseJpaRepository dioceseRepository) {
        this.unidadeTerritorialRepository = unidadeTerritorialRepository;
        this.governoEclesiasticoRepository = governoEclesiasticoRepository;
        this.dioceseRepository = dioceseRepository;
    }

    @Override
    public DadosInstitucionais obterDadosInstitucionais() {
        // 1. Dados da Matriz (Civil / Predial)
        List<UnidadeTerritorialEntity> matrizes =
                unidadeTerritorialRepository.findByTipoAndAtivoTrue(TipoUnidadeTerritorial.MATRIZ);
        UnidadeTerritorialEntity matriz = matrizes.isEmpty() ? null : matrizes.getFirst();

        String nome = matriz != null && matriz.getNome() != null && !matriz.getNome().isBlank()
                ? matriz.getNome()
                : "Paróquia Nossa Senhora de Fátima";

        String endereco = formatarEndereco(matriz);
        String cnpj = matriz != null ? matriz.getCnpj() : null;
        String telefone = matriz != null ? matriz.getTelefone() : null;
        String email = matriz != null ? matriz.getEmail() : null;

        // 2. Dados Canônicos (Diocese)
        String dioceseNome = governoEclesiasticoRepository.findFirstByOrderByCriadoEmUtcDesc()
                .flatMap(gov -> dioceseRepository.findById(gov.getDioceseId()))
                .map(d -> d.getNome())
                .orElse("Diocese de São Miguel Paulista");

        return new DadosInstitucionais(nome, dioceseNome, endereco, cnpj, telefone, email);
    }

    private String formatarEndereco(UnidadeTerritorialEntity matriz) {
        if (matriz == null) {
            return "Rua Gijoca, 7 - Vila Curuçá Velha - São Paulo/SP";
        }
        StringBuilder sb = new StringBuilder();
        if (matriz.getEnderecoLogradouro() != null && !matriz.getEnderecoLogradouro().isBlank()) {
            sb.append(matriz.getEnderecoLogradouro());
        }
        if (matriz.getEnderecoNumero() != null && !matriz.getEnderecoNumero().isBlank()) {
            if (!sb.isEmpty()) {
                sb.append(", ");
            }
            sb.append(matriz.getEnderecoNumero());
        }
        if (matriz.getEnderecoBairro() != null && !matriz.getEnderecoBairro().isBlank()) {
            if (!sb.isEmpty()) {
                sb.append(" - ");
            }
            sb.append(matriz.getEnderecoBairro());
        }
        if (matriz.getEnderecoCidade() != null && !matriz.getEnderecoCidade().isBlank()) {
            if (!sb.isEmpty()) {
                sb.append(" - ");
            }
            sb.append(matriz.getEnderecoCidade());
        }
        if (matriz.getEnderecoUf() != null && !matriz.getEnderecoUf().isBlank()) {
            if (!sb.isEmpty()) {
                sb.append("/");
            }
            sb.append(matriz.getEnderecoUf());
        }
        return sb.isEmpty() ? "Rua Gijoca, 7 - Vila Curuçá Velha - São Paulo/SP" : sb.toString();
    }
}
