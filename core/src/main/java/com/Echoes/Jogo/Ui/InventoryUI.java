package com.Echoes.Jogo.Ui;

import com.Echoes.Jogo.Entities.PlayerStatus;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class InventoryUI {
    private boolean isOpen = false;

    public void update() {
        // Alterna entre abrir e fechar o inventário ao apertar 'I'
        if (Gdx.input.isKeyJustPressed(Input.Keys.I)) {
            isOpen = !isOpen;
        }
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void render(ShapeRenderer shapeRenderer, SpriteBatch batch, BitmapFont font,
                       OrthographicCamera hudCamera, PlayerStatus status) {

        // Só renderiza se estiver aberto
        if (!isOpen) return;

        hudCamera.update();

        // Configuração do tamanho da janela do inventário
        float width = 400;
        float height = 500;

        // Centralizar na tela baseando-se na câmera da HUD
        float x = (hudCamera.viewportWidth - width) / 2;
        float y = (hudCamera.viewportHeight - height) / 2;

        // --- DESENHAR O FUNDO DA JANELA ---
        shapeRenderer.setProjectionMatrix(hudCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Fundo escuro levemente transparente
        shapeRenderer.setColor(0.1f, 0.1f, 0.1f, 0.95f);
        shapeRenderer.rect(x, y, width, height);
        shapeRenderer.end();

        // Borda do menu (linha grossa)
        Gdx.gl.glLineWidth(3); // Deixa a linha da borda um pouco mais grossa
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.LIGHT_GRAY);
        shapeRenderer.rect(x, y, width, height);
        shapeRenderer.end();
        Gdx.gl.glLineWidth(1); // Reseta a grossura da linha

        // --- DESENHAR OS TEXTOS E ITENS ---
        batch.setProjectionMatrix(hudCamera.combined);
        batch.begin();

        // Título
        font.getData().setScale(1.5f);
        font.setColor(Color.YELLOW);
        font.draw(batch, "--- INVENTARIO ---", x + 60, y + height - 30);

        // Itens
        font.getData().setScale(1.2f);
        font.setColor(Color.WHITE);
        float itemY = y + height - 100;

        // Se você usou List<String> inventario no PlayerStatus:
        if (status.inventario == null || status.inventario.isEmpty()) {
            font.setColor(Color.GRAY);
            font.draw(batch, "O inventario esta vazio.", x + 40, itemY);
        } else {
            for (String item : status.inventario) {
                font.draw(batch, "- " + item, x + 40, itemY);
                itemY -= 35; // Espaçamento entre os itens
            }
        }

        // Dica para fechar
        font.getData().setScale(1f);
        font.setColor(Color.GRAY);
        font.draw(batch, "Pressione 'I' para fechar", x + 115, y + 40);

        batch.end();
    }
}
