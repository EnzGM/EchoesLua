package com.Echoes.Jogo.Entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import java.util.List;

public class Inimigo {

    public enum TipoInimigo { RAPIDO, ATIRADOR, NORMAL }

    public Rectangle bounds;
    public TipoInimigo tipo;
    public float hp;
    public boolean ativo = true;
    public float velocidade;
    private float timerTiro = 0f;

    // Distancia a partir da qual o inimigo "acorda" e persegue o jogador de verdade.
    private static final float RAIO_PERSEGUICAO = 450f;
    private float wanderTimer = 0f;
    private float wanderDirX = 0f;
    private float wanderDirY = 0f;

    // Quando true, ignora o "vagar" e sempre persegue o jogador direto — usado
    // em combates de wave (Marte) pra garantir que a wave sempre termine.
    public boolean perseguicaoTotal = false;

    public Inimigo(float x, float y, TipoInimigo tipo) {
        this.bounds = new Rectangle(x, y, 48, 48);
        this.tipo = tipo;

        switch (tipo) {
            case RAPIDO:
                this.velocidade = 210f;
                this.hp = 50f;
                break;
            case ATIRADOR:
                this.velocidade = 90f;
                this.hp = 80f;
                break;
            default: // NORMAL
                this.velocidade = 130f;
                this.hp = 100f;
                break;
        }
    }

    public void update(float delta, Rectangle player, List<Projectile> projeteisInimigos) {
        if (!ativo) return;

        // Vetor de direção até o jogador
        float centroPlayerX = player.x + player.width / 2f;
        float centroPlayerY = player.y + player.height / 2f;
        float centroInimigoX = bounds.x + bounds.width / 2f;
        float centroInimigoY = bounds.y + bounds.height / 2f;

        float dx = centroPlayerX - centroInimigoX;
        float dy = centroPlayerY - centroInimigoY;
        float dist = (float) Math.hypot(dx, dy);

        if (dist > 0) {
            dx /= dist;
            dy /= dist;
        }

        // IA por Tipo
        if (tipo == TipoInimigo.RAPIDO || tipo == TipoInimigo.NORMAL) {
            if (dist > RAIO_PERSEGUICAO && !perseguicaoTotal) {
                // Player longe: vagueia devagar em vez de vir reto pra ele
                wanderTimer -= delta;
                if (wanderTimer <= 0f) {
                    wanderTimer = MathUtils.random(1.5f, 3f);
                    float angulo = MathUtils.random(0f, 360f) * MathUtils.degreesToRadians;
                    wanderDirX = MathUtils.cos(angulo);
                    wanderDirY = MathUtils.sin(angulo);
                }
                bounds.x += wanderDirX * (velocidade * 0.4f) * delta;
                bounds.y += wanderDirY * (velocidade * 0.4f) * delta;
            } else {
                // Player perto: avança diretamente em direção a ele
                bounds.x += dx * velocidade * delta;
                bounds.y += dy * velocidade * delta;
            }
        } else if (tipo == TipoInimigo.ATIRADOR) {
            // Se estiver longe aproxima, se estiver perto mantém distância e atira
            if (dist > 250f) {
                bounds.x += dx * velocidade * delta;
                bounds.y += dy * velocidade * delta;
            } else if (dist < 150f) {
                bounds.x -= dx * velocidade * delta;
                bounds.y -= dy * velocidade * delta;
            }

            // Disparo do Inimigo Atirador
            timerTiro -= delta;
            if (timerTiro <= 0f && dist < 500f) {
                timerTiro = 1.8f; // Intervalo de tiro
                projeteisInimigos.add(new Projectile(
                    centroInimigoX, centroInimigoY,
                    centroPlayerX, centroPlayerY,
                    320f, 600f
                ));
            }
        }
    }

    public void tomarDano(float dano) {
        this.hp -= dano;
        if (this.hp <= 0) {
            this.hp = 0;
            this.ativo = false;
        }
    }

    public ItemType getDrop() {
        return (tipo == TipoInimigo.ATIRADOR) ? ItemType.MUNICAO : ItemType.OXIGENIO;
    }

    public Color getCor() {
        switch (tipo) {
            case RAPIDO: return Color.YELLOW;
            case ATIRADOR: return Color.PURPLE;
            default: return Color.RED;
        }
    }
}
