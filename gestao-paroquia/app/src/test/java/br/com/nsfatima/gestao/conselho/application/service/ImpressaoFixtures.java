package br.com.nsfatima.gestao.conselho.application.service;

import br.com.nsfatima.gestao.conselho.application.dto.AtaParaImpressao;
import br.com.nsfatima.gestao.conselho.application.dto.CabecalhoParoquia;
import br.com.nsfatima.gestao.conselho.domain.model.AtaReuniao;
import br.com.nsfatima.gestao.conselho.domain.model.AtaReuniaoId;
import br.com.nsfatima.gestao.conselho.domain.model.ConflitoInteresse;
import br.com.nsfatima.gestao.conselho.domain.model.Deliberacao;
import br.com.nsfatima.gestao.conselho.domain.model.ResultadoDeliberacao;
import br.com.nsfatima.gestao.conselho.domain.model.TipoReuniao;
import br.com.nsfatima.gestao.conselho.domain.model.Votacao;
import br.com.nsfatima.gestao.organizacao.domain.model.SubtipoConselho;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static br.com.nsfatima.gestao.conselho.domain.model.AtaFixtures.AGORA;
import static br.com.nsfatima.gestao.conselho.domain.model.AtaFixtures.FIEL_A;
import static br.com.nsfatima.gestao.conselho.domain.model.AtaFixtures.FIEL_B;
import static br.com.nsfatima.gestao.conselho.domain.model.AtaFixtures.FIEL_C;
import static br.com.nsfatima.gestao.conselho.domain.model.AtaFixtures.conselho;
import static br.com.nsfatima.gestao.conselho.domain.model.AtaFixtures.conteudo;
import static br.com.nsfatima.gestao.conselho.domain.model.AtaFixtures.item;
import static br.com.nsfatima.gestao.conselho.domain.model.AtaFixtures.tresPresentes;

/** Cenários de impressão compartilhados pelos testes de composição e dos exportadores. */
public final class ImpressaoFixtures {

    public static final CabecalhoParoquia CABECALHO = new CabecalhoParoquia("Paróquia Nossa Senhora de Fátima",
            "Diocese de São Miguel Paulista", "Rua Gijoca, 7 - São Paulo/SP", null, null);

    private static final Map<UUID, String> NOMES = Map.of(FIEL_A, "Maria da Silva",
            FIEL_B, "João Pereira", FIEL_C, "Ana Souza");

    private ImpressaoFixtures() {
    }

    public static AtaParaImpressao paraImpressao(AtaReuniao ata) {
        return new AtaParaImpressao(ata, NOMES, "Conselho Pastoral Paroquial", null, null);
    }

    /** Ata de CPP com votação, conflito de interesses e dois itens de pauta, ainda em rascunho. */
    public static AtaReuniao rascunhoCompleto() {
        Deliberacao comVotos = new Deliberacao(1, "Aprovar o orçamento da reforma do telhado",
                ResultadoDeliberacao.PARECER_FAVORAVEL, new Votacao(1, 0, 0),
                List.of(new ConflitoInteresse(FIEL_B, "Irmão do fornecedor")));
        Deliberacao adiada = new Deliberacao(1, "Adiar a compra do som", ResultadoDeliberacao.ADIADA, null,
                List.of());
        return AtaReuniao.rascunhar(
                AtaReuniaoId.gerar(),
                TipoReuniao.REUNIAO_CPP,
                conselho(SubtipoConselho.CPP),
                null, conteudo(tresPresentes(), item(1, comVotos), item(2, adiada)), "secretario-1",
                AGORA);
    }
}
