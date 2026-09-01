package com.Echoes.Jogo.Ui;

import com.Echoes.Jogo.Entities.PlayerStatus;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * HUD atualizado com inventário completo, status de reparos e objetivo dinâmico.
 */
public class Hud {

    private static final float PAINEL_X = 16;
    private static final float PAINEL_LARGURA = 360;
    private static final float PAINEL_ALTURA = 260; // Aumentado para caber mais texto

    public void render(ShapeRenderer shapeRenderer, SpriteBatch batch, BitmapFont font,
                       OrthographicCamera hudCamera, PlayerStatus status, float worldHeight) {

        float painelY = worldHeight - PAINEL_ALTURA - 16;

        // --- Painel de fundo ---
        shapeRenderer.setProjectionMatrix(hudCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0f, 0f, 0f, 0.65f);
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
        font.getData().setScale(0.9f); // Texto ligeiramente menor para caber tudo

        font.setColor(Color.WHITE);
        font.draw(batch, "OXIGENIO", barraX, barraY + barraAltura + 14);

        float textY = barraY - 15;
        font.draw(batch, "Comida: " + status.comida + " | Gelo: " + status.inventarioGelo, barraX, textY);

        textY -= 20;
        font.draw(batch, "Pecas: Antena(" + status.pecaAntena + ") Gerador(" + status.pecaGerador + ")", barraX, textY);

        textY -= 20;
        font.draw(batch, "       Usina(" + status.pecaUsina + ") Estufa(" + status.pecaEstufa + ")", barraX, textY);

        textY -= 20;
        font.draw(batch, "Partes da Arma: A(" + status.armaParteA + ") B(" + status.armaParteB + ") C(" + status.armaParteC + ")", barraX, textY);

        textY -= 25;
        font.setColor(Color.CYAN);
        font.draw(batch, "REPAROS: " +
            (status.comunicacaoReparada ? "[ON]" : "[OFF]") + " Comunicacao | " +
            (status.energiaReparada ? "[ON]" : "[OFF]") + " Energia", barraX, textY);

        textY -= 20;
        font.draw(batch, "         " +
            (status.extracaoReparada ? "[ON]" : "[OFF]") + " Extracao    | " +
            (status.estufaReparada ? "[ON]" : "[OFF]") + " Estufa", barraX, textY);

        textY -= 25;
        font.setColor(status.armaCraftada ? Color.GREEN : Color.RED);
        font.draw(batch, "ARMA CRAFTADA: " + (status.armaCraftada ? "SIM" : "NAO"), barraX, textY);

        // --- Objetivo Dinâmico ---
        textY -= 25;
        font.setColor(Color.YELLOW);
        String objetivo = "Objetivo: ";
        boolean reparosOks = status.comunicacaoReparada && status.energiaReparada && status.extracaoReparada && status.estufaReparada;

        if (!reparosOks) {
            objetivo += "Coletar pecas e reparar as 4 estacoes.";
        } else if (!status.armaCraftada) {
            objetivo += "Coletar partes (A,B,C) e craftar Arma na Base.";
        } else {
            objetivo += "Portal liberado! Siga para Marte.";
        }
        font.draw(batch, objetivo, barraX, textY);

        font.getData().setScale(1f); // Restaura escala
        batch.end();
    }
}
