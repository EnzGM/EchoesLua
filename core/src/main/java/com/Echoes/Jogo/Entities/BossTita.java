package com.Echoes.Jogo.Entities;

import com.badlogic.gdx.graphics.Color;

/**
 * ITEM 17 do checklist: Boss de Tita.
 *
 * Mesma lógica do BossLua/BossMarte: só deve ser instanciado depois que
 * MissionState.titaMissoesOk(status) for true (guardiões de Tita derrotados)
 * — isso é responsabilidade da TitanScreen, não desta classe.
 *
 * hp 180 e o chase mais agressivo dos três até aqui: velocidade mais alta
 * que o BossLua (70) e o BossMarte (85), pra sentir a escalada de dificuldade
 * pedida no checklist ("chase mais agressivo"). Reaproveita toda a IA de
 * perseguição/dano de Inimigo (extends), só troca os números e a aparência.
 */
public class BossTita extends Inimigo {

    public static final float HP_INICIAL = 180f;
    private static final float VELOCIDADE = 150f;  // mais agressivo que o BossLua (70) e o BossMarte (85)
    private static final float DANO_CONTATO = 45f; // mais alto que o BossMarte (40)
    private static final float TAMANHO = 140f;      // maior que o BossMarte (130)

    public BossTita(float x, float y) {
        super(x, y, TipoInimigo.NORMAL);

        this.hp = HP_INICIAL;
        this.velocidade = VELOCIDADE;
        this.danoContato = DANO_CONTATO;
        this.perseguicaoTotal = true;
        this.bounds.setSize(TAMANHO, TAMANHO);
    }

    @Override
    public Color getCor() {
        // Azul-gelo intenso, condizente com a atmosfera fria de Tita e bem
        // diferente do vinho do BossLua e do laranja-ferrugem do BossMarte
        return new Color(0.15f, 0.55f, 0.80f, 1f);
    }
}
