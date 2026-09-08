package com.Echoes.Jogo.Entities;

import com.Echoes.Jogo.Main;
import com.Echoes.Jogo.Screen.LunarScreen;
import com.Echoes.Jogo.Screen.MarsScreen;
import com.badlogic.gdx.math.Rectangle;

/**
 * Classe responsável por verificar a transição entre a Lua e Marte.
 */
public class Portal {
    public Rectangle bounds;
    public boolean ativo;

    // Construtor ANTIGO (Resolve o erro da sua LunarScreen que passa só x e y)
    public Portal(float x, float y) {
        this.bounds = new Rectangle(x, y, 64, 64); // Tamanho padrão 64x64
        this.ativo = true;
    }

    // Construtor NOVO (Usado se quiser especificar largura e altura)
    public Portal(float x, float y, float width, float height) {
        this.bounds = new Rectangle(x, y, width, height);
        this.ativo = true;
    }

    public void verificarTransicao(Main game, Rectangle playerBounds, PlayerStatus status) {
        if (ativo && playerBounds.overlaps(this.bounds)) {
            status.curarAoTrocarFase(); // regenera parte da vida na troca de fase

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
