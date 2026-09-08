package com.Echoes.Jogo.Entities;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * Item 10: Tiro ou ataque com alcance máximo (regra visível).
 * O projétil se desativa automaticamente após viajar uma certa distância.
 */
public class Projectile {
    public float x, y;
    public float velX, velY;
    public boolean ativo;

    private float distanciaPercorrida;
    private float alcanceMaximo;

    // Recebe de onde sai (startX, startY), para onde vai (targetX, targetY), velocidade e alcance
    public Projectile(float startX, float startY, float targetX, float targetY, float speed, float maxRange) {
        this.x = startX;
        this.y = startY;
        this.alcanceMaximo = maxRange;
        this.distanciaPercorrida = 0;
        this.ativo = true;

        // Calcula a direção do tiro usando matemática básica de vetores
        float dx = targetX - startX;
        float dy = targetY - startY;
        float length = (float) Math.sqrt(dx * dx + dy * dy);

        if (length != 0) {
            this.velX = (dx / length) * speed;
            this.velY = (dy / length) * speed;
        } else {
            this.velX = speed;
            this.velY = 0;
        }
    }

    public void update(float delta) {
        if (!ativo) return; // Se já sumiu, não faz nada

        // Movimento do projétil
        float moveX = velX * delta;
        float moveY = velY * delta;

        x += moveX;
        y += moveY;

        // Calcula o quanto andou neste frame (Teorema de Pitágoras)
        float stepDistance = (float) Math.sqrt(moveX * moveX + moveY * moveY);
        distanciaPercorrida += stepDistance;

        // ITEM 10: Regra visível de alcance. Se passou do limite, desativa.
        if (distanciaPercorrida >= alcanceMaximo) {
            ativo = false;
        }
    }

    public void render(ShapeRenderer shapeRenderer) {
        if (!ativo) return;

        // Desenha um projétil circular (pode ser substituído por textura depois)
        shapeRenderer.setColor(1f, 1f, 0f, 1f); // Amarelo
        shapeRenderer.circle(x, y, 5);
    }
}
