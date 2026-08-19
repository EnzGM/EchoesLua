package com.Echoes.Jogo.Entities;

import com.badlogic.gdx.utils.Pool;

/**
 * Uma partícula de poeira/efeito. Implementa Pool.Poolable pra que o Pool
 * saiba como "limpar" a partícula quando ela volta pro pool (reset()).
 */
public class DustParticle implements Pool.Poolable {
    public float x, y;
    public float vx, vy; // velocidade em x/y
    public float life;   // tempo de vida restante
    public float maxLife; // tempo de vida total (usado pra calcular o "fade out")

    @Override
    public void reset() {
        x = 0; y = 0; vx = 0; vy = 0; life = 0; maxLife = 0;
    }
}
