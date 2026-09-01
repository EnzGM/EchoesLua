package com.Echoes.Jogo.Entities;

import com.Echoes.Jogo.Main;
import com.Echoes.Jogo.Screen.LunarScreen;
import com.Echoes.Jogo.Screen.MarsScreen;
import com.badlogic.gdx.math.Rectangle;

public class Portal {

    public Rectangle bounds;
    public boolean ativo = false;

    public Portal(float x, float y) {
        this.bounds = new Rectangle(x, y, 64, 64);
    }

    // Portal bidirecional atualizado para remover dependências não declaradas
    public void verificarTransicao(Main game, Rectangle playerBounds, PlayerStatus status) {
        if (ativo && playerBounds.overlaps(this.bounds)) {
            if (status.faseAtual.equals("LUA")) {
                status.lastLuaX = playerBounds.x;
                status.lastLuaY = playerBounds.y;
                status.faseAtual = "MARTE";
                game.setScreen(new MarsScreen(game, status));
            } else {
                status.lastMarteX = playerBounds.x;
                status.lastMarteY = playerBounds.y;
                status.faseAtual = "LUA";
                game.setScreen(new LunarScreen(game, status));
            }
        }
    }
}
