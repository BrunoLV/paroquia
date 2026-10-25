package br.com.nsfatima.gestao.conselho.application.service;

import br.com.nsfatima.gestao.conselho.application.dto.AtorAutenticado;
import br.com.nsfatima.gestao.conselho.application.port.out.PublicadorAuditoriaAtaPort;
import br.com.nsfatima.gestao.conselho.domain.model.AtaReuniao;
import br.com.nsfatima.gestao.conselho.domain.model.RegistroAuditoriaAta;
import br.com.nsfatima.gestao.conselho.domain.repository.AtaHistoricoRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Grava a trilha da ata na mesma transação da mutação: autor, perfil, IP, ação e o estado antes e depois, ou
 * os parâmetros de cada exportação. A trilha é append-only (o banco veda UPDATE e DELETE). O log global recebe
 * só identificadores opacos, sem conteúdo da ata nem nomes (LGPD).
 */
@Service
public class AtaAuditoriaService {

    private final AtaHistoricoRepository historico;
    private final PublicadorAuditoriaAtaPort publicador;
    private final ObjectMapper objectMapper;

    public AtaAuditoriaService(AtaHistoricoRepository historico, PublicadorAuditoriaAtaPort publicador,
                               ObjectMapper objectMapper) {
        this.historico = historico;
        this.publicador = publicador;
        this.objectMapper = objectMapper;
    }

    /** Serializa o estado do agregado para guardar o "antes" de uma mutação (o agregado muda in-place). */
    public String capturar(AtaReuniao ata) {
        return objectMapper.writeValueAsString(ata.estado());
    }

    /**
     * Registra uma mutação.
     *
     * @param antes estado capturado antes da mutação, ou {@code null} na criação
     * @param depois agregado já mutado
     */
    public void registrar(AtorAutenticado ator, String acao, String antes, AtaReuniao depois) {
        gravar(ator, acao, antes, capturar(depois), depois);
    }

    /** Registra uma exportação, com formato, layout, conteúdo e a situação da ata no momento. */
    public void registrarExportacao(AtorAutenticado ator, AtaReuniao ata, String formato, String layout,
                                    String conteudo) {
        String parametros = objectMapper.writeValueAsString(Map.of("formato", formato, "layout", layout,
                "conteudo", conteudo, "status", ata.getStatus().name()));
        gravar(ator, "EXPORTAR_" + formato, null, parametros, ata);
    }

    private void gravar(AtorAutenticado ator, String acao, String antes, String depois, AtaReuniao ata) {
        historico.registrar(new RegistroAuditoriaAta(UUID.randomUUID(), ata.getId(), acao, ator.usuarioId(),
                ator.login(), ator.perfil(ata.getOrganizacaoConvocanteId()), enderecoIp(), antes, depois,
                Instant.now()));
        publicador.publicar(ator.login(), ator.usuarioId(), acao, ata);
    }

    private static String enderecoIp() {
        RequestAttributes atributos = RequestContextHolder.getRequestAttributes();
        return atributos instanceof ServletRequestAttributes servlet ? servlet.getRequest().getRemoteAddr() : null;
    }
}
