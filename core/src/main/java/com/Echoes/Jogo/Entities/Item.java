package com.Echoes.Jogo.Entities;

import com.badlogic.gdx.math.Rectangle;

public class Item {
    public Rectangle bounds;
    public ItemType type;
    public boolean coletado = false;

    public Item(float x, float y, ItemType type) {
        this.bounds = new Rectangle(x, y, 48, 48);
        this.type = type;
    }
}
