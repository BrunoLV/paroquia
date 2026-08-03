package br.com.nsfatima.gestao.patrimonio.domain.model;

/**
 * Categoria do acervo a que pertence um ativo patrimonial, usada para agrupar métricas de
 * ociosidade e organizar o inventário.
 */
public enum AtivoCategoria {
    ELETRONICO,
    MOBILIARIO,
    /** Vestes e objetos litúrgicos usados nas celebrações. */
    PARAMENTO,
    INFRAESTRUTURA
}
