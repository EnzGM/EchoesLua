package com.Echoes.Jogo.Ui;

import com.Echoes.Jogo.Entities.PlayerStatus;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * HUD fixa (não anda com a câmera do mundo) — compartilhada entre a Lua, Marte,
 * Tita e Calisto, pra manter as informações do jogador sempre com a mesma cara
 * (MELHORIA 5).
 */
public class Hud {

    public void render(ShapeRenderer shapeRenderer, SpriteBatch batch, BitmapFont font,
                       OrthographicCamera hudCamera, PlayerStatus status,
                       String missaoAtual, String extraLinha, int screenHeight) {

        hudCamera.update();

        boolean semMunicao = status.municao <= 0;

        int linhas = 6 + (extraLinha != null ? 1 : 0) + (semMunicao ? 1 : 0);
        float altura = 25f + linhas * 27f;

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

        // Linha 3: Combate (Arma e Munição)
        font.setColor(semMunicao ? Color.ORANGE : Color.WHITE);
        String armaStatus = status.armaCraftada ? "SIM" : "NAO";
        font.draw(batch, "MUNICAO: " + status.municao + " | ARMA: " + armaStatus, 30, y);
        y -= 27f;

        // Linha 4: Reparos (Estufa e Gerador) — nomes batendo com as pecas do chao
        String estufaStatus = status.estufaReparada ? "ON" : "OFF";
        String geradorStatus = status.energiaReparada ? "ON" : "OFF";
        font.setColor(Color.WHITE);
        font.draw(batch, "ESTUFA: " + estufaStatus + " | GERADOR: " + geradorStatus, 30, y);
        y -= 27f;

        // Linha 5: Reparos (Usina e Antena) — nomes batendo com as pecas do chao
        String usinaStatus = status.extracaoReparada ? "ON" : "OFF";
        String antenaStatus = status.comunicacaoReparada ? "ON" : "OFF";
        font.draw(batch, "USINA: " + usinaStatus + " | ANTENA: " + antenaStatus, 30, y);
        y -= 27f;

        // Linha 6: Quest Tracker (MELHORIA 1)
        font.setColor(Color.YELLOW);
        font.draw(batch, "MISSAO: " + missaoAtual, 30, y);
        y -= 27f;

        if (extraLinha != null) {
            font.setColor(Color.ORANGE);
            font.draw(batch, extraLinha, 30, y);
            y -= 27f;
        }

        if (semMunicao) {
            float restante = (1f - status.progressoRegenMunicaoZerada()) * PlayerStatus.REGEN_MUNICAO_ZERADA_DELAY;
            if (restante < 0f) restante = 0f;

            boolean piscar = (System.currentTimeMillis() / 300L) % 2 == 0;
            font.setColor(piscar ? Color.RED : Color.ORANGE);
            font.draw(batch, String.format("SEM MUNICAO! Fabricando novas balas... %.1fs", restante), 30, y);
        }

        batch.end();
    }
}
