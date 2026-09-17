package com.Echoes.Jogo.Entities;

import com.badlogic.gdx.graphics.Color;

/**
 * ITEM 15 do checklist: Boss da Lua.
 *
 * Só deve ser instanciado depois que MissionState.luaMissoesOk(status) for
 * true (isso é responsabilidade da LunarScreen, não desta classe).
 *
 * hp 120, velocidade baixa (não foge nem é ágil) e dano de contato bem mais
 * alto que os inimigos comuns da Lua — usa o campo danoContato herdado de
 * Inimigo, então nenhuma tela precisa saber que existe uma subclasse: o
 * mesmo loop de colisão que já existia funciona sem alterações.
 *
 * Reaproveita toda a IA de perseguição/tiro/dano de Inimigo (extends), só
 * troca os números e a aparência.
 */
public class BossLua extends Inimigo {

    public static final float HP_INICIAL = 120f;
    private static final float VELOCIDADE = 70f;   // baixa, comparada ao NORMAL (130) e RAPIDO (210)
    private static final float DANO_CONTATO = 35f; // alto, comparado ao padrão de 15 dos inimigos comuns
    private static final float TAMANHO = 110f;      // maior que os 48x48 padrão, pra parecer um chefe

    public BossLua(float x, float y) {
        super(x, y, TipoInimigo.NORMAL);

        this.hp = HP_INICIAL;
        this.velocidade = VELOCIDADE;
        this.danoContato = DANO_CONTATO;
        this.perseguicaoTotal = true; // sempre persegue direto, nunca "vagueia" feito os inimigos comuns
        this.bounds.setSize(TAMANHO, TAMANHO);
    }

    @Override
    public Color getCor() {
        // Vermelho escuro/vinho, pra ficar visualmente diferente dos inimigos comuns (RED/YELLOW/PURPLE)
        return new Color(0.65f, 0.05f, 0.15f, 1f);
    }
}
