package com.Echoes.Jogo.Entities;

public enum ItemType {
    OXIGENIO,
    COMIDA,
    GELO,
    PECA_ANTENA,
    PECA_GERADOR,
    PECA_USINA,
    PECA_ESTUFA,
    ARMA_PARTE_A,
    ARMA_PARTE_B,
    ARMA_PARTE_C,
    MUNICAO, // MELHORIA 3: Adicionado para o drop dos inimigos

    // ITEM 22: materiais de crafting (bancada). GELO ja existia e passa a
    // servir tambem como material de receita.
    PECA,
    METAL,
    CIRCUITO
}
