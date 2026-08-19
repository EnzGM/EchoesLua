package com.Echoes.Jogo.Managers;

import com.Echoes.Jogo.Entities.DustParticle;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

/**
 * Gerencia partículas com Pooling: em vez de criar (new) e destruir um objeto
 * a cada partícula (o que gera lixo de memória e pode travar o jogo — Aula 07,
 * slide "Pooling: reciclar em vez de criar"), pedimos uma instância do pool,
 * usamos, e devolvemos quando a partícula morre.
 */
public class ParticleManager {

    // O Pool sabe criar novas instâncias quando precisa (newObject) e reaproveitar
    // as que já foram devolvidas (free).
    private final Pool<DustParticle> pool = new Pool<DustParticle>() {
        @Override
        protected DustParticle newObject() {
            return new DustParticle();
        }
    };

    // Lista das partículas ativas nesse exato momento (as "em uso" do pool).
    private final Array<DustParticle> ativas = new Array<>();

    /** Poeira sutil nos pés do personagem enquanto ele anda. */
    public void spawnPoeira(float x, float y) {
        DustParticle p = pool.obtain(); // pega uma instância livre (ou cria, se não tiver nenhuma)
        p.x = x;
        p.y = y;
        p.vx = MathUtils.random(-20f, 20f);
        p.vy = MathUtils.random(10f, 40f);
        p.maxLife = 0.4f;
        p.life = p.maxLife;
        ativas.add(p);
    }

    /** Pequena explosão de partículas ao coletar um item — feedback visual. */
    public void spawnColeta(float x, float y) {
        for (int i = 0; i < 8; i++) {
            DustParticle p = pool.obtain();
            p.x = x;
            p.y = y;
            float angulo = MathUtils.random(0f, 360f) * MathUtils.degreesToRadians;
            float velocidade = MathUtils.random(60f, 120f);
            p.vx = MathUtils.cos(angulo) * velocidade;
            p.vy = MathUtils.sin(angulo) * velocidade;
            p.maxLife = 0.5f;
            p.life = p.maxLife;
            ativas.add(p);
        }
    }

    /** Atualiza posição/vida de todas as partículas ativas; devolve pro pool as que morreram. */
    public void update(float delta) {
        for (int i = ativas.size - 1; i >= 0; i--) {
            DustParticle p = ativas.get(i);
            p.life -= delta;
            p.x += p.vx * delta;
            p.y += p.vy * delta;

            if (p.life <= 0f) {
                ativas.removeIndex(i);
                pool.free(p); // devolve pro pool — não vira lixo pro garbage collector
            }
        }
    }

    public void render(ShapeRenderer shapeRenderer, OrthographicCamera camera) {
        if (ativas.size == 0) return;

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (DustParticle p : ativas) {
            float alpha = p.life / p.maxLife; // vai ficando transparente conforme morre
            shapeRenderer.setColor(0.8f, 0.8f, 0.85f, alpha);
            shapeRenderer.circle(p.x, p.y, 3);
        }
        shapeRenderer.end();
    }

    /** Libera tudo de uma vez (chamado no dispose() da tela). */
    public void clear() {
        pool.freeAll(ativas);
        ativas.clear();
    }
}
