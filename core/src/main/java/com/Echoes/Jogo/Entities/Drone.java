package com.Echoes.Jogo.Entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;

import java.util.List;

/**
 * ITEM 24: Drone companheiro.
 *
 * Nao gruda no player: persegue uma posicao "atras" dele com velocidade
 * PROPRIA mais lenta que a do jogador, entao em curvas fechadas ele
 * visivelmente atrasa e recupera a distancia depois — tem update() proprio,
 * nao e so um sprite desenhado em cima do personagem.
 *
 * Em combate, atira sozinho no inimigo mais proximo a cada INTERVALO_TIRO
 * segundos (modoIluminar = false). Em Tita (escuro), em vez de atirar, so
 * ilumina uma area ao redor dele (modoIluminar = true) — a tela decide qual
 * modo usar.
 */
public class Drone {

    public Rectangle bounds;
    public boolean ativo = true;
    public boolean modoIluminar = false;

    private static final float TAMANHO = 26f;
    private static final float VELOCIDADE = 260f; // mais lento que o player (320f) -> atrasa nas curvas
    private static final float OFFSET_X = -55f;   // posicao alvo: atras/esquerda do player
    private static final float OFFSET_Y = -35f;

    private static final float ALCANCE_TIRO = 380f;
    private final float danoTiro;
    private final float tamanhoTiro;
    private static final float INTERVALO_TIRO = 2f;
    private float tiroTimer = INTERVALO_TIRO;

    public Drone(float x, float y) {
        this(x, y, 0);
    }

    public Drone(float x, float y, int nivelUpgrade) {
        this.bounds = new Rectangle(x, y, TAMANHO, TAMANHO);
        this.danoTiro = 25f + nivelUpgrade * 15f;
        this.tamanhoTiro = 5f + nivelUpgrade * 1.5f;
    }

    /**
     * @param inimigos       lista de inimigos pra mirar (pode ser null se so estiver iluminando)
     * @param projeteisDrone lista onde o tiro do drone e adicionado (pode ser null se modoIluminar)
     */
    public void update(float delta, Rectangle player, List<Inimigo> inimigos, List<Projectile> projeteisDrone) {
        if (!ativo) return;

        // Persegue a posicao-alvo (atras do player) com velocidade propria e mais lenta —
        // e isso que causa o atraso visivel nas curvas, em vez de grudar no player.
        float alvoX = player.x + OFFSET_X;
        float alvoY = player.y + OFFSET_Y;

        float dx = alvoX - bounds.x;
        float dy = alvoY - bounds.y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);

        if (dist > 2f) {
            float passo = Math.min(dist, VELOCIDADE * delta);
            bounds.x += (dx / dist) * passo;
            bounds.y += (dy / dist) * passo;
        }

        if (modoIluminar || inimigos == null || projeteisDrone == null) return;

        tiroTimer -= delta;
        if (tiroTimer <= 0f) {
            Inimigo alvo = inimigoMaisProximo(inimigos);
            if (alvo != null) {
                tiroTimer = INTERVALO_TIRO;
                float sx = bounds.x + bounds.width / 2f;
                float sy = bounds.y + bounds.height / 2f;
                float tx = alvo.bounds.x + alvo.bounds.width / 2f;
                float ty = alvo.bounds.y + alvo.bounds.height / 2f;
                projeteisDrone.add(new Projectile(sx, sy, tx, ty, 480f, ALCANCE_TIRO).comForca(danoTiro, tamanhoTiro));
            }
        }
    }

    private Inimigo inimigoMaisProximo(List<Inimigo> inimigos) {
        Inimigo maisProximo = null;
        float menorDist = ALCANCE_TIRO;
        float cx = bounds.x + bounds.width / 2f;
        float cy = bounds.y + bounds.height / 2f;

        for (Inimigo ini : inimigos) {
            if (!ini.ativo) continue;
            float dx = (ini.bounds.x + ini.bounds.width / 2f) - cx;
            float dy = (ini.bounds.y + ini.bounds.height / 2f) - cy;
            float dist = (float) Math.sqrt(dx * dx + dy * dy);
            if (dist < menorDist) {
                menorDist = dist;
                maisProximo = ini;
            }
        }
        return maisProximo;
    }

    public Color getCor() {
        return modoIluminar ? new Color(0.95f, 0.95f, 0.55f, 1f) : new Color(0.35f, 0.85f, 1f, 1f);
    }

    /** Raio do "halo" de luz desenhado ao redor do drone quando modoIluminar = true. */
    public float getRaioLuz() {
        return 140f;
    }
}
