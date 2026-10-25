package br.com.nsfatima.gestao.governanca.domain.model;

/**
 * Estados do ciclo de vida de contenção e tratamento de um incidente de segurança (Art. 48 LGPD).
 */
public enum StatusIncidente {
    EM_ANALISE,
    CONTIDO,
    NOTIFICADO,
    ENCERRADO
}
