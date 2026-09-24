package com.Echoes.Jogo.Entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

import java.util.List;

/**
 * ITEM 16 do checklist: Boss de Marte.
 *
 * REFORÇADO (a pedido): hp bem maior e um padrão de tiro próprio — uma dupla
 * de tiros em leque estreito (2 projéteis levemente abertos, nunca em linha
 * reta única), bem diferente do tiro único do BossLua.
 */
public class BossMarte extends Inimigo {

    public static final float HP_INICIAL = 320f; // era 160
    private static final float VELOCIDADE = 85f;   // baixa, mas um pouco mais rapido que o BossLua (70)
    private static final float DANO_CONTATO = 40f; // mais alto que o BossLua (35) e que o padrao de 15
    private static final float TAMANHO = 130f;      // maior que o BossLua (110), pra parecer mais avancado

    private static final float INTERVALO_TIRO = 2.2f;
    private static final float ABERTURA_GRAUS = 14f;
    private float tiroTimer = INTERVALO_TIRO;

    public BossMarte(float x, float y) {
        super(x, y, TipoInimigo.NORMAL);

        this.hp = HP_INICIAL;
        this.velocidade = VELOCIDADE;
        this.danoContato = DANO_CONTATO;
        this.perseguicaoTotal = true; // sempre persegue direto, igual o BossLua
        this.bounds.setSize(TAMANHO, TAMANHO);
    }

    @Override
    public void update(float delta, Rectangle player, List<Projectile> projeteisInimigos) {
        super.update(delta, player, projeteisInimigos);
        if (!ativo) return;

        tiroTimer -= delta;
        if (tiroTimer <= 0f) {
            tiroTimer = INTERVALO_TIRO;
            dispararDuplo(player, projeteisInimigos);
        }
    }

    /** Padrão de tiro do BossMarte: 2 projéteis levemente abertos, mirados no jogador. */
    private float padraoAngulo = 0f;

    private void dispararDuplo(Rectangle player, List<Projectile> lista) {
        float sx = bounds.x + bounds.width / 2f;
        float sy = bounds.y + bounds.height / 2f;
        float tx = player.x + player.width / 2f;
        float ty = player.y + player.height / 2f;

        float base = (float) Math.atan2(ty - sy, tx - sx);
        padraoAngulo += 0.22f;

        // Espiral em dois braços: o padrão gira a cada rajada.
        for (int i = 0; i < 10; i++) {
            float ang = base + padraoAngulo + i * (MathUtils.PI2 / 10f);
            float destX = sx + MathUtils.cos(ang) * 800f;
            float destY = sy + MathUtils.sin(ang) * 800f;
            lista.add(Projectile.tiroEspecialInimigo(
                sx, sy, destX, destY, 275f, 800f
            ).comForca(18f, 7f));
        }

        // Dois tiros mirados fecham os espaços próximos do jogador.
        lista.add(Projectile.tiroEspecialInimigo(sx, sy, tx, ty, 320f, 750f)
            .comForca(22f, 8f));
    }

    @Override
    public Color getCor() {
        // Laranja-ferrugem, cor de Marte, bem diferente do vinho do BossLua
        return new Color(0.80f, 0.35f, 0.05f, 1f);
    }
}
