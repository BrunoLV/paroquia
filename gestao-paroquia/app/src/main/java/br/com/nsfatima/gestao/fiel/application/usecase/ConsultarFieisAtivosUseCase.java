package br.com.nsfatima.gestao.fiel.application.usecase;

import br.com.nsfatima.gestao.fiel.api.v1.dto.FielNomeResponse;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.entity.FielEntity;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository.FielJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.Collection;
import java.util.Comparator;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Consultas mínimas sobre fiéis ativos para quem só precisa escolher uma pessoa: devolvem identificador e nome,
 * nunca contato ou documento (minimização de dados, LGPD).
 */
@Service
public class ConsultarFieisAtivosUseCase {

    private final FielJpaRepository fielRepository;

    public ConsultarFieisAtivosUseCase(FielJpaRepository fielRepository) {
        this.fielRepository = fielRepository;
    }

    /**
     * Busca fiéis ativos cujo nome contém o termo, sem diferenciar maiúsculas nem acentos.
     *
     * @param termo trecho do nome
     * @param limite máximo de resultados
     * @return fiéis encontrados, em ordem de nome
     */
    @Transactional(readOnly = true)
    public List<FielNomeResponse> buscarPorNome(String termo, int limite) {
        String procurado = normalizar(termo);
        // ponytail: o nome é gravado criptografado, então o filtro roda em memória; se a base passar de algumas
        // dezenas de milhares de fiéis, indexar um hash de busca ou trazer o nome em coluna pesquisável.
        return fielRepository.findByAtivoTrue().stream()
                .filter(f -> normalizar(f.getNome()).contains(procurado))
                .sorted(Comparator.comparing(f -> normalizar(f.getNome())))
                .limit(limite)
                .map(f -> new FielNomeResponse(f.getId(), f.getNome()))
                .toList();
    }

    /**
     * Filtra os identificadores que são de fiéis ativos.
     *
     * @param ids identificadores a conferir
     * @return os que existem como fiel com cadastro ativo
     */
    @Transactional(readOnly = true)
    public Set<UUID> idsAtivos(Collection<UUID> ids) {
        return fielRepository.findAllById(ids).stream()
                .filter(FielEntity::isAtivo)
                .map(FielEntity::getId)
                .collect(Collectors.toUnmodifiableSet());
    }

    private static String normalizar(String texto) {
        return Normalizer.normalize(texto == null ? "" : texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "").toLowerCase(Locale.ROOT).trim();
    }
}
