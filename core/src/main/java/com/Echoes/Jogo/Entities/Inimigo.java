package com.Echoes.Jogo.Entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import java.util.List;

public class Inimigo {

    public static float MULTIPLICADOR_VELOCIDADE_INIMIGO = 1f;

    public enum TipoInimigo { RAPIDO, ATIRADOR, NORMAL }

    public Rectangle bounds;
    public TipoInimigo tipo;
    public float hp;
    public boolean ativo = true;
    public float velocidade;
    private float timerTiro = 0f;

    // ITEM 15: dano por segundo causado ao jogador em colisão de contato.
    // Fica aqui (em vez de hardcoded nas telas) pra que subclasses como o
    // BossLua possam sobrescrever com um valor mais alto ("dano alto" do
    // checklist) sem precisar duplicar a lógica de colisão em cada Screen.
    public float danoContato = 15f;

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

        float velocidadeAtual = velocidade * MULTIPLICADOR_VELOCIDADE_INIMIGO;

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
                bounds.x += wanderDirX * (velocidadeAtual * 0.4f) * delta;
                bounds.y += wanderDirY * (velocidadeAtual * 0.4f) * delta;
            } else {
                // Player perto: avança diretamente em direção a ele
                bounds.x += dx * velocidadeAtual * delta;
                bounds.y += dy * velocidadeAtual * delta;
            }
        } else if (tipo == TipoInimigo.ATIRADOR) {
            // Se estiver longe aproxima, se estiver perto mantém distância e atira
            if (dist > 250f) {
                bounds.x += dx * velocidadeAtual * delta;
                bounds.y += dy * velocidadeAtual * delta;
            } else if (dist < 150f) {
                bounds.x -= dx * velocidadeAtual * delta;
                bounds.y -= dy * velocidadeAtual * delta;
            }

            // Inimigos comuns mantêm tiros simples; os padrões bullet hell ficam nos bosses.
            timerTiro -= delta;
            if (timerTiro <= 0f && dist < 650f) {
                timerTiro = 1.55f;
                float destX = centroPlayerX;
                float destY = centroPlayerY;
                projeteisInimigos.add(Projectile.tiroEspecialInimigo(
                    centroInimigoX, centroInimigoY, destX, destY,
                    270f, 700f
                ).comForca(12f, 7f));
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
        // Todo inimigo agora deixa material de crafting.
        int rolagem = MathUtils.random(0, 3);
        switch (rolagem) {
            case 0: return ItemType.METAL;
            case 1: return ItemType.CIRCUITO;
            case 2: return ItemType.GELO;
            default: return ItemType.PECA;
        }
    }

    public Color getCor() {
        switch (tipo) {
            case RAPIDO: return Color.YELLOW;
            case ATIRADOR: return Color.PURPLE;
            default: return Color.RED;
        }
    }
}
