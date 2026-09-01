package com.Echoes.Jogo.Entities;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

public class Projetil {
    private int x, y;
    private int velX, velY;
    private int largura = 8;
    private int altura = 8;
    private boolean ativo = true;

    public Projetil(int x, int y, int dirX, int dirY) {
        this.x = x;
        this.y = y;
        int velocidade = 10;
        this.velX = dirX * velocidade;
        this.velY = dirY * velocidade;
    }

    public void update() {
        x += velX;
        y += velY;

        // Desativa se sair muito da tela
        if (x < -100 || x > 2000 || y < -100 || y > 2000) {
            ativo = false;
        }
    }

    public void render(Graphics g) {
        if (ativo) {
            g.setColor(Color.ORANGE);
            g.fillOval(x, y, largura, altura);
        }
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, largura, altura);
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }
}
