package br.com.nsfatima.gestao.conselho.application.dto;

/**
 * Identidade canônica impressa no topo dos documentos oficiais (RN-IMP-03): paróquia, diocese, endereço e logotipo.
 * O logotipo é opcional; sem ele o cabeçalho sai só com texto.
 */
public record CabecalhoParoquia(String nome, String diocese, String endereco, byte[] logo, String tipoMidiaLogo) {
}
