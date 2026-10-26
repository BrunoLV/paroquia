package br.com.nsfatima.gestao.conselho.application.port.out;

import br.com.nsfatima.gestao.conselho.application.dto.CabecalhoParoquia;

/** Porta de saída que entrega a identidade institucional da paróquia, vinda de outro contexto. */
public interface CabecalhoParoquiaPort {

    /**
     * Identidade canônica para o cabeçalho dos documentos.
     *
     * @return paróquia, diocese, endereço e logotipo
     */
    CabecalhoParoquia obter();
}
