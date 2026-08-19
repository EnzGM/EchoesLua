package com.Echoes.Jogo.Ui;

import com.Echoes.Jogo.Entities.PlayerStatus;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * HUD com painel de fundo semi-transparente e barra de oxigênio colorida
 * (verde -> amarelo -> vermelho conforme o nível), em vez de só texto solto.
 */
public class Hud {

    private static final float PAINEL_X = 16;
    private static final float PAINEL_LARGURA = 260;
    private static final float PAINEL_ALTURA = 114;

    public void render(ShapeRenderer shapeRenderer, SpriteBatch batch, BitmapFont font,
                       OrthographicCamera hudCamera, PlayerStatus status, float worldHeight) {

        float painelY = worldHeight - 130;

        // --- Painel de fundo ---
        shapeRenderer.setProjectionMatrix(hudCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0f, 0f, 0f, 0.45f);
        shapeRenderer.rect(PAINEL_X, painelY, PAINEL_LARGURA, PAINEL_ALTURA);

        // --- Barra de oxigênio ---
        float barraX = PAINEL_X + 16;
        float barraY = painelY + PAINEL_ALTURA - 30;
        float barraLargura = PAINEL_LARGURA - 32;
        float barraAltura = 14;

        shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 1f); // fundo da barra
        shapeRenderer.rect(barraX, barraY, barraLargura, barraAltura);

        float percentual = status.oxigenio / 100f;
        if (percentual > 0.5f) shapeRenderer.setColor(Color.GREEN);
        else if (percentual > 0.2f) shapeRenderer.setColor(Color.YELLOW);
        else shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(barraX, barraY, barraLargura * percentual, barraAltura); // preenchimento

        shapeRenderer.end();

        // --- Textos ---
        batch.setProjectionMatrix(hudCamera.combined);
        batch.begin();

        font.setColor(Color.WHITE);
        font.draw(batch, "OXIGENIO", barraX, barraY + barraAltura + 14);
        font.draw(batch, "Comida: " + status.comida, barraX, barraY - 12);
        font.draw(batch, "Gelo: " + status.inventarioGelo, barraX, barraY - 32);
        font.draw(batch, "Agua: " + status.agua + "   Combustivel: " + status.combustivel,
            barraX, barraY - 52);

        if (status.inventarioGelo > 0) {
            font.setColor(Color.YELLOW);
            font.draw(batch, "Pressione E na base para processar gelo", PAINEL_X, 40);
        }

        batch.end();
    }
}
