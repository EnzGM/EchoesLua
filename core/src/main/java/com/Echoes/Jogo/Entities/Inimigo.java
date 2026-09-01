package com.Echoes.Jogo.Entities;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

/**
 * MELHORIA 3: Inimigos com 2 comportamentos e Drop.
 */
public class Inimigo {
    public enum TipoIA {
        PATRULHA,
        PERSEGUE
    }

    public Rectangle bounds;
    public TipoIA tipo;
    public float speed = 80f;
    public int hp = 100;
    public boolean ativo = true;

    // Variáveis de Patrulha
    private Vector2 startPos;
    private Vector2 targetPatrulha;
    private boolean indo = true;

    public Inimigo(float x, float y, TipoIA tipo) {
        this.bounds = new Rectangle(x, y, 52, 52);
        this.tipo = tipo;
        this.startPos = new Vector2(x, y);
        this.targetPatrulha = new Vector2(x + 200f, y); // Patrulha em um raio de 200 pixels
    }

    public void update(float delta, Rectangle playerBounds) {
        if (!ativo) return;

        Vector2 myPos = new Vector2(bounds.x, bounds.y);
        Vector2 playerPos = new Vector2(playerBounds.x, playerBounds.y);
        float dist = myPos.dst(playerPos);

        // COMPORTAMENTO 2: Persegue ativamente se o jogador estiver no raio de visão
        if (tipo == TipoIA.PERSEGUE && dist < 250f) {
            Vector2 dir = playerPos.cpy().sub(myPos).nor();
            bounds.x += dir.x * (speed * 1.3f) * delta; // Corre mais rápido ao avistar o jogador
            bounds.y += dir.y * (speed * 1.3f) * delta;
        } else {
            // COMPORTAMENTO 1: Patrulha de um lado para o outro[cite: 21]
            patrulhar(delta, myPos);
        }
    }

    private void patrulhar(float delta, Vector2 myPos) {
        Vector2 alvo = indo ? targetPatrulha : startPos;

        if (myPos.dst(alvo) < 5f) {
            indo = !indo; // Inverte direção ao chegar no ponto de controle
        } else {
            Vector2 dir = alvo.cpy().sub(myPos).nor();
            bounds.x += dir.x * speed * delta;
            bounds.y += dir.y * speed * delta;
        }
    }

    public void tomarDano(int dano) {
        this.hp -= dano;
        if (this.hp <= 0) {
            this.ativo = false;
        }
    }

    /** Retorna qual item este inimigo vai dropar ao morrer (munição ou suprimento)[cite: 21]. */
    public ItemType getDrop() {
        return Math.random() > 0.5 ? ItemType.MUNICAO : ItemType.OXIGENIO;
    }
}
