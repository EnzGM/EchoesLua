package com.Echoes.Jogo.Entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.MathUtils;

import java.util.List;

/**
 * ITEM 15 do checklist: Boss da Lua.
 *
 * Só deve ser instanciado depois que MissionState.luaMissoesOk(status) for
 * true (isso é responsabilidade da LunarScreen, não desta classe).
 *
 * REFORÇADO (a pedido): hp bem maior e agora tem um padrão de tiro próprio —
 * um único tiro mirado no jogador, de tempos em tempos, além do dano de
 * contato já existente. É o boss mais simples dos quatro (só 1 tiro por vez),
 * já que é a introdução do jogador aos chefes.
 */
public class BossLua extends Inimigo {

    public static final float HP_INICIAL = 220f; // era 120
    private static final float VELOCIDADE = 70f;   // baixa, comparada ao NORMAL (130) e RAPIDO (210)
    private static final float DANO_CONTATO = 35f; // alto, comparado ao padrão de 15 dos inimigos comuns
    private static final float TAMANHO = 110f;      // maior que os 48x48 padrão, pra parecer um chefe

    private static final float INTERVALO_TIRO = 2.5f;
    private float tiroTimer = INTERVALO_TIRO;

    public BossLua(float x, float y) {
        super(x, y, TipoInimigo.NORMAL);

        this.hp = HP_INICIAL;
        this.velocidade = VELOCIDADE;
        this.danoContato = DANO_CONTATO;
        this.perseguicaoTotal = true; // sempre persegue direto, nunca "vagueia" feito os inimigos comuns
        this.bounds.setSize(TAMANHO, TAMANHO);
    }

    @Override
    public void update(float delta, Rectangle player, List<Projectile> projeteisInimigos) {
        super.update(delta, player, projeteisInimigos);
        if (!ativo) return;

        tiroTimer -= delta;
        if (tiroTimer <= 0f) {
            tiroTimer = INTERVALO_TIRO;

            float sx = bounds.x + bounds.width / 2f;
            float sy = bounds.y + bounds.height / 2f;
            float tx = player.x + player.width / 2f;
            float ty = player.y + player.height / 2f;

            float base = (float) Math.atan2(ty - sy, tx - sx);
            for (int i = -2; i <= 2; i++) {
                float ang = base + i * 0.16f;
                float dx = MathUtils.cos(ang);
                float dy = MathUtils.sin(ang);
                projeteisInimigos.add(Projectile.tiroEspecialInimigo(
                    sx, sy, sx + dx * 700f, sy + dy * 700f,
                    250f, 700f
                ).comForca(16f, 7f));
            }
        }
    }

    @Override
    public Color getCor() {
        // Vermelho escuro/vinho, pra ficar visualmente diferente dos inimigos comuns (RED/YELLOW/PURPLE)
        return new Color(0.65f, 0.05f, 0.15f, 1f);
    }
}
