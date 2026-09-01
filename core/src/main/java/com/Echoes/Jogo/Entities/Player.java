package com.Echoes.Jogo.Entities;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

public class Player {
    private int x, y;
    private int largura = 32;
    private int altura = 32;
    private int velocidade = 4;

    // Status do Jogador
    private int pecasColetadas = 0;
    private boolean armaCraftada = false;
    private boolean emMarte = false;
    private int municao = 10; // Munição inicial

    // Regras de Tiro e Cooldown (Etapa 2)
    private long ultimoTiroTempo = 0;
    private long cooldownTiro = 400; // 400 milissegundos entre os tiros
    private List<Projetil> projeteis;

    public Player(int x, int y) {
        this.x = x;
        this.y = y;
        this.projeteis = new ArrayList<>();
    }

    public void update() {
        // Atualiza a posição dos projéteis ativos
        for (int i = 0; i < projeteis.size(); i++) {
            Projetil p = projeteis.get(i);
            if (p.isAtivo()) {
                p.update();
            } else {
                projeteis.remove(i);
                i--;
            }
        }
    }

    // Método de Atirar (Verifica Craft, Munição e Cooldown)
    public void atirar(int dirX, int dirY) {
        long tempoAtual = System.currentTimeMillis();

        if (armaCraftada && municao > 0 && (tempoAtual - ultimoTiroTempo >= cooldownTiro)) {
            projeteis.add(new Projetil(x + largura / 2, y + altura / 2, dirX, dirY));
            municao--;
            ultimoTiroTempo = tempoAtual;
        }
    }

    public void render(Graphics g) {
        // Desenha o Player
        g.setColor(Color.CYAN);
        g.fillRect(x, y, largura, altura);

        // Desenha os projéteis na tela
        for (Projetil p : projeteis) {
            p.render(g);
        }
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, largura, altura);
    }

    // Getters e Setters
    public int getX() { return x; }
    public void setX(int x) { this.x = x; }
    public int getY() { return y; }
    public void setY(int y) { this.y = y; }

    public int getPecasColetadas() { return pecasColetadas; }
    public void addPecas(int qtd) { this.pecasColetadas += qtd; }

    public boolean hasArmaCraftada() { return armaCraftada; }
    public void setArmaCraftada(boolean armaCraftada) { this.armaCraftada = armaCraftada; }

    public boolean isEmMarte() { return emMarte; }
    public void setEmMarte(boolean emMarte) { this.emMarte = emMarte; }

    public int getMunicao() { return municao; }
    public void addMunicao(int qtd) { this.municao += qtd; }

    public List<Projetil> getProjeteis() { return projeteis; }
}
