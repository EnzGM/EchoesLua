package com.Echoes.Jogo.Entities;

public enum ItemType {
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
    CIRCUITO,
    MATERIAL_CRAFT,

    // ITEM 24: item colecionavel que libera o drone companheiro (tecla C).
    DRONE
}
