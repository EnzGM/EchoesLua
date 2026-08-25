package com.Echoes.Jogo.Entities;

import com.badlogic.gdx.math.Rectangle;

/**
 * O portal é tipo a "porta" que leva da Lua pra Marte.
 * Ele só deixa passar (ativo = true) se as condições da missão
 * forem cumpridas — isso a gente vai controlar de fora, na LunarScreen.
 */
public class Portal {
    public Rectangle bounds;
    public boolean ativo = false; // false = bloqueado, true = liberado

    public Portal(float x, float y, float width, float height) {
        this.bounds = new Rectangle(x, y, width, height);
    }
}
