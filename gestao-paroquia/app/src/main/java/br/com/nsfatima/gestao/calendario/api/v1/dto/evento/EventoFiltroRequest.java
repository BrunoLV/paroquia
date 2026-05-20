package br.com.nsfatima.gestao.calendario.api.v1.dto.evento;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import br.com.nsfatima.gestao.calendario.domain.type.CategoriaEvento;
import br.com.nsfatima.gestao.calendario.domain.type.EventoStatusInput;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Filtros para listagem de eventos")
public record EventoFiltroRequest(
        @Parameter(description = "Data inicial para filtragem (ISO-8601)")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        Instant dataInicio,
        
        @Parameter(description = "Data final para filtragem (ISO-8601)")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        Instant dataFim,
        
        @Parameter(description = "Filtrar por organização responsável")
        UUID organizacaoId,
        
        @Parameter(description = "Filtrar por projeto")
        UUID projetoId,
        
        @Parameter(description = "Filtrar por ID de organização envolvida (responsável ou apoio)")
        UUID envolvidoId,
        
        @Parameter(description = "Filtrar por múltiplas categorias")
        List<CategoriaEvento> categoria,
        
        @Parameter(description = "Filtrar por múltiplos status")
        List<EventoStatusInput> status) {
}
