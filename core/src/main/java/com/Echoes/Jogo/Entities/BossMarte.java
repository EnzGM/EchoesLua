package com.Echoes.Jogo.Entities;

import com.badlogic.gdx.graphics.Color;

/**
 * ITEM 16 do checklist: Boss de Marte.
 *
 * Mesma lógica do BossLua (item 15): só deve ser instanciado depois que
 * MissionState.marteMissoesOk(status) for true — isso é responsabilidade da
 * MarsScreen, não desta classe.
 *
 * hp 160, velocidade baixa e dano de contato bem mais alto que os inimigos
 * comuns de Marte. Reaproveita toda a IA de perseguição/dano de Inimigo
 * (extends), só troca os números e a aparência — maior e com cor diferente
 * do BossLua, pra ficar visualmente distinto (item pede "chefe distinto").
 */
public class BossMarte extends Inimigo {

    public static final float HP_INICIAL = 160f;
    private static final float VELOCIDADE = 85f;   // baixa, mas um pouco mais rapido que o BossLua (70)
    private static final float DANO_CONTATO = 40f; // mais alto que o BossLua (35) e que o padrao de 15
    private static final float TAMANHO = 130f;      // maior que o BossLua (110), pra parecer mais avancado

    public BossMarte(float x, float y) {
        super(x, y, TipoInimigo.NORMAL);

        this.hp = HP_INICIAL;
        this.velocidade = VELOCIDADE;
        this.danoContato = DANO_CONTATO;
        this.perseguicaoTotal = true; // sempre persegue direto, igual o BossLua
        this.bounds.setSize(TAMANHO, TAMANHO);
    }

    @Override
    public Color getCor() {
        // Laranja-ferrugem, cor de Marte, bem diferente do vinho do BossLua
        return new Color(0.80f, 0.35f, 0.05f, 1f);
    }
}
