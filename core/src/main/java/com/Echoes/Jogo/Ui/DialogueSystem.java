package com.Echoes.Jogo.Ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * Sistema de diálogo simples que atende ao requisito 08:
 * Possui índice, comporta múltiplas falas e fecha sem dar crash.
 */
public class DialogueSystem {
    private String[] linhas;
    private int indiceAtual;
    public boolean ativo;

    public DialogueSystem(String[] falas) {
        this.linhas = falas;
        this.indiceAtual = 0;
        this.ativo = true; // Já começa ativo ao ser instanciado
    }

    public void update() {
        if (!ativo) return;

        // Avança o diálogo ao apertar ESPAÇO ou ENTER
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            indiceAtual++;

            // Se passou da última fala, encerra o diálogo em segurança (sem crash)
            if (indiceAtual >= linhas.length) {
                ativo = false;
            }
        }
    }

    public void render(ShapeRenderer shape, SpriteBatch batch, BitmapFont font, float screenWidth, float screenHeight) {
        if (!ativo) return;

        // Fundo escuro translúcido para o diálogo
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(0f, 0f, 0f, 0.7f);
        // Posicionado na parte inferior da tela
        shape.rect(50, 30, screenWidth - 100, 120);
        shape.end();

        // Renderização do texto
        batch.begin();
        font.getData().setScale(1.2f);
        font.setColor(Color.WHITE);
        font.draw(batch, linhas[indiceAtual], 70, 120);

        font.getData().setScale(1f);
        font.setColor(Color.YELLOW);
        font.draw(batch, "Pressione ESPACO para continuar...", 70, 60);
        batch.end();
    }
}
