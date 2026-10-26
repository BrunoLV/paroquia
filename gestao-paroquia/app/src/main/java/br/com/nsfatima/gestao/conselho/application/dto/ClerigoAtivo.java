package br.com.nsfatima.gestao.conselho.application.dto;

import java.util.UUID;

/**
 * Clérigo ativo da paróquia que pode constar numa ata. {@code funcao} é a função na reunião que o encargo sugere
 * (Pároco, Vigário paroquial, Diácono), ou nula; {@code paroco} marca quem preside por direito próprio.
 */
public record ClerigoAtivo(UUID id, String nome, String funcao, boolean paroco) {
}
