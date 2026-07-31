package br.com.nsfatima.gestao.calendario.api.v1.dto;

import br.com.nsfatima.gestao.calendario.domain.model.TipoSolenidade;

public record SolenidadeLiturgicaRequestDTO(
        String chave,
        String nome,
        TipoSolenidade tipoSolenidade,
        Integer mesFixo,
        Integer diaFixo,
        Integer offsetDiasPascoa,
        String corLiturgica,
        Double fatorDemanda,
        Integer nivelPrioridade,
        String descricao,
        Boolean ativo
) {}
