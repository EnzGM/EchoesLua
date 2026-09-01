package com.Echoes.Jogo.Entities;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

public class Municao {
    private int x, y;
    private int largura = 12;
    private int altura = 12;
    private boolean coletado = false;
    private int quantidadeMunicao = 5;

    public Municao(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void render(Graphics g) {
        if (!coletado) {
            g.setColor(Color.YELLOW);
            g.fillRect(x, y, largura, altura);
            g.setColor(Color.BLACK);
            g.drawRect(x, y, largura, altura);
        }
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, largura, altura);
    }

    public boolean isColetado() {
        return coletado;
    }

    public void setColetado(boolean coletado) {
        this.coletado = coletado;
    }

    public int getQuantidadeMunicao() {
        return quantidadeMunicao;
    }
}
