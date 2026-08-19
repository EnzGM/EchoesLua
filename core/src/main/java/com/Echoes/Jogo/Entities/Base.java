package com.Echoes.Jogo.Entities;

import com.badlogic.gdx.math.Rectangle;

public class Base {
    public Rectangle bounds;

    public Base(float x, float y, float width, float height) {
        this.bounds = new Rectangle(x, y, width, height);
    }
}
