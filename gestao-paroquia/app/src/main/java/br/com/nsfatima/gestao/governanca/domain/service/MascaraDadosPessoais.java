package br.com.nsfatima.gestao.governanca.domain.service;

/**
 * Mascaramento de dados pessoais para respostas públicas (sem autenticação). Quem possui apenas o protocolo
 * de uma solicitação ao DPO consegue confirmar que o pedido é seu, mas não recupera CPF, e-mail ou nome
 * completo: princípios da necessidade e da segurança (Art. 6º, III e VII da LGPD). Funções puras e sem estado.
 */
public final class MascaraDadosPessoais {

    private static final String SEPARADOR_EMAIL = "@";
    private static final String OCULTO = "***";
    private static final String EMAIL_OCULTO = OCULTO + "@" + OCULTO;

    private MascaraDadosPessoais() {
    }

    /**
     * Mantém a primeira e a última letra do usuário do e-mail e o domínio ({@code m***a@exemplo.com}).
     *
     * @param email e-mail em claro
     * @return e-mail mascarado; {@code ***@***} se nulo ou malformado
     */
    public static String email(String email) {
        if (email == null || !email.contains(SEPARADOR_EMAIL)) {
            return EMAIL_OCULTO;
        }
        String[] partes = email.split(SEPARADOR_EMAIL, 2);
        String usuario = partes[0];
        if (usuario.isEmpty()) {
            return EMAIL_OCULTO;
        }
        if (usuario.length() <= 2) {
            return usuario.charAt(0) + OCULTO + SEPARADOR_EMAIL + partes[1];
        }
        return usuario.charAt(0) + OCULTO + usuario.charAt(usuario.length() - 1) + SEPARADOR_EMAIL + partes[1];
    }

    /**
     * Mantém apenas os dois últimos dígitos do CPF ({@code ***.***.***-25}).
     *
     * @param cpf CPF com ou sem máscara
     * @return CPF mascarado, ou {@code null} se nulo ou em branco
     */
    public static String cpf(String cpf) {
        if (cpf == null || cpf.isBlank()) {
            return null;
        }
        String digitos = cpf.replaceAll("\\D", "");
        String final2 = digitos.length() >= 2 ? digitos.substring(digitos.length() - 2) : "**";
        return OCULTO + "." + OCULTO + "." + OCULTO + "-" + final2;
    }

    /**
     * Mantém a inicial de cada palavra do nome ({@code M*** S***}).
     *
     * @param nome nome em claro
     * @return nome mascarado, ou {@code null} se nulo ou em branco
     */
    public static String nome(String nome) {
        if (nome == null || nome.isBlank()) {
            return null;
        }
        StringBuilder mascarado = new StringBuilder();
        for (String palavra : nome.trim().split("\\s+")) {
            if (mascarado.length() > 0) {
                mascarado.append(' ');
            }
            mascarado.append(palavra.charAt(0)).append(OCULTO);
        }
        return mascarado.toString();
    }
}
