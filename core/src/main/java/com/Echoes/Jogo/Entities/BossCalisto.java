package com.Echoes.Jogo.Entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;

import java.util.List;

/**
 * ITEM 18: Boss de Calisto — 3 formas/mutações.
 *
 * Quando hp <= 0 e forma < 3: sobe de forma (nunca pula direto pra forma 3),
 * fica maior, mais rápido e com hp máximo maior, e passa por uma pequena
 * janela de "queda/recuperação" (transicaoTimer) em que fica parado,
 * invulnerável e piscando — é o "cai e volta maior/mais vermelho" do checklist.
 *
 * Quando hp <= 0 na forma 3: mortoFinal = true (quem entrega a CHAVE_LUZ pro
 * inventário é a CallistoScreen, já que o boss não tem acesso ao PlayerStatus).
 */
public class BossCalisto extends Inimigo {

    public int forma = 1;
    public float hpMax;
    public boolean mortoFinal = false;

    private static final float HP_BASE = 100f;         // forma 1: hp 100
    private static final float VELOCIDADE_BASE = 90f;   // forma 1: speed 90
    private static final float TAMANHO_BASE = 120f;
    private static final float CRESCIMENTO_TAMANHO_POR_FORMA = 20f;
    private static final float FATOR_VELOCIDADE_POR_FORMA = 1.18f;

    // Tempo "caído/se levantando" após cada mutação: parado e invulnerável.
    private static final float TEMPO_TRANSICAO = 1.1f;
    public float transicaoTimer = 0f;

    // Dash exclusivo da forma 3, a cada 3s.
    private static final float DASH_INTERVALO = 3f;
    private static final float DASH_DISTANCIA = 230f;
    private float dashTimer = DASH_INTERVALO;

    public BossCalisto(float x, float y) {
        super(x, y, TipoInimigo.NORMAL);

        this.hpMax = HP_BASE;
        this.hp = HP_BASE;
        this.velocidade = VELOCIDADE_BASE;
        this.danoContato = 30f;
        this.perseguicaoTotal = true; // sempre persegue direto, igual os outros bosses
        this.bounds.setSize(TAMANHO_BASE, TAMANHO_BASE);
    }

    @Override
    public void update(float delta, Rectangle player, List<Projectile> projeteisInimigos) {
        if (!ativo || mortoFinal) return;

        if (transicaoTimer > 0f) {
            transicaoTimer -= delta;
            return; // "caído/se levantando": fica parado durante a transição de forma
        }

        super.update(delta, player, projeteisInimigos);

        if (forma == 3) {
            dashTimer -= delta;
            if (dashTimer <= 0f) {
                dashTimer = DASH_INTERVALO;
                executarDash(player);
            }
        }
    }

    /** Investida rápida em direção ao jogador — só na forma 3. */
    private void executarDash(Rectangle player) {
        float centroPlayerX = player.x + player.width / 2f;
        float centroPlayerY = player.y + player.height / 2f;
        float centroBossX = bounds.x + bounds.width / 2f;
        float centroBossY = bounds.y + bounds.height / 2f;

        float dx = centroPlayerX - centroBossX;
        float dy = centroPlayerY - centroBossY;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        if (dist > 0) {
            dx /= dist;
            dy /= dist;
        }

        bounds.x += dx * DASH_DISTANCIA;
        bounds.y += dy * DASH_DISTANCIA;
    }

    @Override
    public void tomarDano(float dano) {
        if (mortoFinal || transicaoTimer > 0f) return; // invulnerável durante a transição

        this.hp -= dano;
        if (this.hp <= 0f) {
            if (forma < 3) {
                forma++; // nunca pula pra forma 3 direto — só +1 por vez
                hpMax = HP_BASE * (1f + 0.5f * (forma - 1)); // forma2=150, forma3=200
                hp = hpMax;
                velocidade *= FATOR_VELOCIDADE_POR_FORMA;
                float novoTamanho = TAMANHO_BASE + CRESCIMENTO_TAMANHO_POR_FORMA * (forma - 1);
                bounds.setSize(novoTamanho, novoTamanho);
                transicaoTimer = TEMPO_TRANSICAO;
            } else {
                hp = 0f;
                ativo = false;
                mortoFinal = true;
            }
        }
    }

    /** True enquanto o boss deve "piscar"/ficar invulnerável (transição de forma). */
    public boolean estaPiscando() {
        return transicaoTimer > 0f;
    }

    @Override
    public Color getCor() {
        switch (forma) {
            case 3: return new Color(0.85f, 0.05f, 0.05f, 1f); // vermelho intenso
            case 2: return new Color(0.75f, 0.30f, 0.15f, 1f); // laranja-avermelhado
            default: return new Color(0.25f, 0.55f, 0.85f, 1f); // azul-gelo (forma 1)
        }
    }
}
