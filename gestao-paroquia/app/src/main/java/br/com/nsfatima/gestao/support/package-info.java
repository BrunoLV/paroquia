/**
 * Utilitários técnicos transversais sem regra de negócio própria: criptografia de dados sensíveis
 * em repouso para conformidade LGPD ({@code CryptoUtils}, AES/CBC/PKCS5Padding com IV aleatório por
 * chamada, chave derivada via SHA-256), os conversores JPA que a utilizam
 * ({@code EncryptedStringConverter}/{@code EncryptedLocalDateConverter}), configuração de OpenAPI, e
 * tratamento de exceções de domínio genéricas não específicas a um bounded context.
 */
package br.com.nsfatima.gestao.support;
