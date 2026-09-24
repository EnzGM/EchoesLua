package com.Echoes.Jogo.Entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.MathUtils;

import java.util.List;

/**
 * ITEM 17 do checklist: Boss de Tita.
 *
 * REFORÇADO (a pedido): hp bem maior e um padrão de tiro próprio — uma
 * rajada em cruz (4 tiros simultâneos, N/S/L/O), que obriga o jogador a se
 * mover na diagonal pra escapar. Combina com o chase mais agressivo dele.
 */
public class BossTita extends Inimigo {

    public static final float HP_INICIAL = 420f; // era 180
    private static final float VELOCIDADE = 150f;  // mais agressivo que o BossLua (70) e o BossMarte (85)
    private static final float DANO_CONTATO = 45f; // mais alto que o BossMarte (40)
    private static final float TAMANHO = 140f;      // maior que o BossMarte (130)

    private static final float INTERVALO_TIRO = 2f;
    private float tiroTimer = INTERVALO_TIRO;

    public BossTita(float x, float y) {
        super(x, y, TipoInimigo.NORMAL);

        this.hp = HP_INICIAL;
        this.velocidade = VELOCIDADE;
        this.danoContato = DANO_CONTATO;
        this.perseguicaoTotal = true;
        this.bounds.setSize(TAMANHO, TAMANHO);
    }

    @Override
    public void update(float delta, Rectangle player, List<Projectile> projeteisInimigos) {
        super.update(delta, player, projeteisInimigos);
        if (!ativo) return;

        tiroTimer -= delta;
        if (tiroTimer <= 0f) {
            tiroTimer = INTERVALO_TIRO;
            dispararCruz(projeteisInimigos);
        }
    }

    /** Padrão de tiro do BossTita: 4 projéteis simultâneos nas direções cardeais. */
    private float rotacao = 0f;

    private void dispararCruz(List<Projectile> lista) {
        float sx = bounds.x + bounds.width / 2f;
        float sy = bounds.y + bounds.height / 2f;

        // Flor de 12 projéteis que gira a cada rajada.
        rotacao += 0.17f;
        for (int i = 0; i < 12; i++) {
            float ang = rotacao + i * (MathUtils.PI2 / 12f);
            float destX = sx + MathUtils.cos(ang) * 850f;
            float destY = sy + MathUtils.sin(ang) * 850f;
            lista.add(Projectile.tiroEspecialInimigo(
                sx, sy, destX, destY, 300f, 850f
            ).comForca(20f, 7f));
        }

        // Quatro tiros um pouco mais rápidos, formando outra camada do padrão.
        for (int i = 0; i < 4; i++) {
            float ang = rotacao + 0.26f + i * (MathUtils.PI2 / 4f);
            float destX = sx + MathUtils.cos(ang) * 850f;
            float destY = sy + MathUtils.sin(ang) * 850f;
            lista.add(Projectile.tiroEspecialInimigo(
                sx, sy, destX, destY, 360f, 850f
            ).comForca(24f, 6f));
        }
    }

    @Override
    public Color getCor() {
        // Azul-gelo intenso, condizente com a atmosfera fria de Tita
        return new Color(0.15f, 0.55f, 0.80f, 1f);
    }
}
