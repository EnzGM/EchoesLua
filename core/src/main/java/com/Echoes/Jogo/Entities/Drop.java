package com.Echoes.Jogo.Entities;

import com.badlogic.gdx.math.Rectangle;

/** ITEM 28: marcador deixado no local da morte para recuperar recursos. */
public class Drop {
    public float x;
    public float y;
    public String item;
    public int creditos;
    public int municao;
    public final Rectangle bounds = new Rectangle();

    public Drop(float x, float y, String item, int creditos, int municao) {
        this.x = x;
        this.y = y;
        this.item = item;
        this.creditos = creditos;
        this.municao = municao;
        bounds.set(x - 28f, y - 28f, 56f, 56f);
    }

    public boolean vazio() {
        return creditos <= 0 && municao <= 0;
    }
}
