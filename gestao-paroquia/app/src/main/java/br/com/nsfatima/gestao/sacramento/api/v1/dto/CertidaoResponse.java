package br.com.nsfatima.gestao.sacramento.api.v1.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Certidão narrativa de assento homologado. Nunca contém filiação adotiva, notas reservadas nem
 * dados de livro reservado (LGPD, spec §5.5).
 *
 * @param texto extrato narrativo pronto para impressão
 * @param averbacoes notas marginais homologadas e não reservadas
 */
public record CertidaoResponse(
        UUID assentoId,
        String tipo,
        String livro,
        String folha,
        String termo,
        String paroquiaCustodiante,
        LocalDateTime dataCelebracao,
        String localCelebracao,
        String texto,
        List<String> averbacoes) {
}
