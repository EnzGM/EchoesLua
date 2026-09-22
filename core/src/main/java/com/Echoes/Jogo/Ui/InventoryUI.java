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
        if (Gdx.input.isKeyJustPressed(Input.Keys.I)) {
            isOpen = !isOpen;
        }
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void render(ShapeRenderer shapeRenderer, SpriteBatch batch, BitmapFont font,
                       OrthographicCamera hudCamera, PlayerStatus status) {

        if (!isOpen) return;

        hudCamera.update();

        float width = 400;
        float height = 500;

        float x = (hudCamera.viewportWidth - width) / 2;
        float y = (hudCamera.viewportHeight - height) / 2;

        shapeRenderer.setProjectionMatrix(hudCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.1f, 0.1f, 0.1f, 0.95f);
        shapeRenderer.rect(x, y, width, height);
        shapeRenderer.end();

        Gdx.gl.glLineWidth(3);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.LIGHT_GRAY);
        shapeRenderer.rect(x, y, width, height);
        shapeRenderer.end();
        Gdx.gl.glLineWidth(1);

        batch.setProjectionMatrix(hudCamera.combined);
        batch.begin();

        font.getData().setScale(1.5f);
        font.setColor(Color.YELLOW);
        font.draw(batch, "--- INVENTARIO ---", x + 60, y + height - 30);

        font.getData().setScale(1.2f);
        font.setColor(Color.WHITE);
        float itemY = y + height - 100;

        if (status.inventario == null || status.inventario.getItens().isEmpty()) {
            font.setColor(Color.GRAY);
            font.draw(batch, "O inventario esta vazio.", x + 40, itemY);
        } else {
            for (String item : status.inventario.getItens()) {
                int qtd = status.inventario.getQuantidade(item);
                String linha = (qtd > 1) ? ("- " + item + " x" + qtd) : ("- " + item);
                font.draw(batch, linha, x + 40, itemY);
                itemY -= 35;
            }
        }

        font.getData().setScale(1f);
        font.setColor(Color.GRAY);
        font.draw(batch, "Pressione 'I' para fechar", x + 115, y + 40);

        batch.end();
    }
}
