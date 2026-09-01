package com.Echoes.Jogo.Ui;

import com.Echoes.Jogo.Entities.PlayerStatus;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * HUD fixa (não anda com a câmera do mundo) — compartilhada entre a Lua e Marte,
 * pra manter as informações do jogador sempre com a mesma cara (MELHORIA 5).
 */
public class Hud {

    public void render(ShapeRenderer shapeRenderer, SpriteBatch batch, BitmapFont font,
                       OrthographicCamera hudCamera, PlayerStatus status,
                       String missaoAtual, String extraLinha, int screenHeight) {

        hudCamera.update();

        int linhas = 6 + (extraLinha != null ? 1 : 0);
        float altura = 25f + linhas * 27f;

        // Painel translúcido atrás do texto, pra ficar legível sobre qualquer fundo
        shapeRenderer.setProjectionMatrix(hudCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0f, 0f, 0f, 0.45f);
        shapeRenderer.rect(15, screenHeight - altura - 10, 460, altura);
        shapeRenderer.end();

        batch.setProjectionMatrix(hudCamera.combined);
        batch.begin();

        font.getData().setScale(1f);
        float y = screenHeight - 30;

        // Linha 1: Oxigênio
        font.setColor(status.oxigenio <= 25f ? Color.RED : Color.CYAN);
        font.draw(batch, "O2: " + (int) status.oxigenio + "%", 30, y);
        y -= 27f;

        // Linha 2: HP
        font.setColor(status.hp <= 25f ? Color.RED : Color.WHITE);
        font.draw(batch, "HP: " + (int) status.hp, 30, y);
        y -= 27f;

        // Linha 3: Combate (Arma e Munição) — igual na Lua e em Marte
        font.setColor(Color.WHITE);
        String armaStatus = status.armaCraftada ? "SIM" : "NAO";
        font.draw(batch, "MUNICAO: " + status.municao + " | ARMA: " + armaStatus, 30, y);
        y -= 27f;

        // Linha 4: Reparos (Estufa e Energia)
        String estufaStatus = status.estufaReparada ? "ON" : "OFF";
        String energiaStatus = status.energiaReparada ? "ON" : "OFF";
        font.draw(batch, "ESTUFA: " + estufaStatus + " | ENERGIA: " + energiaStatus, 30, y);
        y -= 27f;

        // Linha 5: Reparos (Extração e Comunicação)
        String extracaoStatus = status.extracaoReparada ? "ON" : "OFF";
        String comStatus = status.comunicacaoReparada ? "ON" : "OFF";
        font.draw(batch, "EXTRACAO: " + extracaoStatus + " | COMUNICACAO: " + comStatus, 30, y);
        y -= 27f;

        // Linha 6: Quest Tracker (MELHORIA 1)
        font.setColor(Color.YELLOW);
        font.draw(batch, "MISSAO: " + missaoAtual, 30, y);
        y -= 27f;

        // Linha extra opcional — Marte usa pra mostrar a wave atual
        if (extraLinha != null) {
            font.setColor(Color.ORANGE);
            font.draw(batch, extraLinha, 30, y);
        }

        batch.end();
    }
}
