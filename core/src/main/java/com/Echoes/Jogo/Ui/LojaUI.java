package com.Echoes.Jogo.Ui;

import com.Echoes.Jogo.Entities.PlayerStatus;
import com.Echoes.Jogo.Managers.SaveManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class LojaUI {
    private static final int PRECO_ARMA = 40;
    private static final int PRECO_DRONE = 50;
    private boolean aberta = false;
    private String mensagem = "";
    private float mensagemTimer = 0f;

    public boolean isAberta() { return aberta; }
    public void abrir() { aberta = true; mensagem = ""; mensagemTimer = 0f; }
    public void fechar() { aberta = false; }

    public void update(float delta, PlayerStatus status) {
        if (!aberta) return;
        if (mensagemTimer > 0f) mensagemTimer -= delta;
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) { fechar(); return; }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) comprarArma(status);
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) comprarDrone(status);
    }

    private void comprarArma(PlayerStatus status) {
        if (status.creditos < PRECO_ARMA) { mensagem = "SEM SALDO!"; }
        else { status.creditos -= PRECO_ARMA; status.nivelUpgradeArma++; status.inventario.add("UPGRADE_ARMA_LV" + status.nivelUpgradeArma); mensagem = "UPGRADE DA ARMA COMPRADO!"; SaveManager.salvarJogo(status, null); }
        mensagemTimer = 2.2f;
    }
    private void comprarDrone(PlayerStatus status) {
        if (status.creditos < PRECO_DRONE) { mensagem = "SEM SALDO!"; }
        else { status.creditos -= PRECO_DRONE; status.nivelUpgradeDrone++; status.inventario.add("UPGRADE_DRONE_LV" + status.nivelUpgradeDrone); mensagem = "UPGRADE DO DRONE COMPRADO!"; SaveManager.salvarJogo(status, null); }
        mensagemTimer = 2.2f;
    }

    public void render(ShapeRenderer sr, SpriteBatch batch, BitmapFont font, OrthographicCamera hudCamera, PlayerStatus status) {
        if (!aberta) return; hudCamera.update();
        float width=1000f,height=430f,x=(hudCamera.viewportWidth-width)/2f,y=(hudCamera.viewportHeight-height)/2f;
        sr.setProjectionMatrix(hudCamera.combined); sr.begin(ShapeRenderer.ShapeType.Filled); sr.setColor(0.06f,0.08f,0.12f,0.97f); sr.rect(x,y,width,height); sr.end();
        sr.begin(ShapeRenderer.ShapeType.Line); sr.setColor(Color.GOLD); sr.rect(x,y,width,height); sr.end();
        batch.setProjectionMatrix(hudCamera.combined); batch.begin();
        font.getData().setScale(1.6f); font.setColor(Color.GOLD); font.draw(batch,"--- LOJA DE UPGRADES [B] ---",x+35,y+height-35);
        font.getData().setScale(1.2f); font.setColor(Color.WHITE); font.draw(batch,"SALDO: "+status.creditos+" CREDITOS",x+35,y+height-85);
        font.setColor(status.creditos>=PRECO_ARMA?Color.GREEN:Color.GRAY); font.draw(batch,"[1] UPGRADE ARMA LV."+(status.nivelUpgradeArma+1)+" - "+PRECO_ARMA+" creditos",x+35,y+height-145);
        font.setColor(status.creditos>=PRECO_DRONE?Color.GREEN:Color.GRAY); font.draw(batch,"[2] UPGRADE DRONE LV."+(status.nivelUpgradeDrone+1)+" - "+PRECO_DRONE+" creditos",x+35,y+height-195);
        font.setColor(Color.LIGHT_GRAY); font.draw(batch,"Arma: "+status.nivelUpgradeArma+" | Drone: "+status.nivelUpgradeDrone,x+35,y+height-245);
        font.draw(batch,"Cada upgrade aumenta o poder do equipamento.",x+35,y+height-275);
        if(mensagemTimer>0f){font.setColor(mensagem.equals("SEM SALDO!")?Color.RED:Color.GREEN);font.draw(batch,mensagem,x+35,y+85);}

        // ITEM 30: o mesmo overlay aberto por B tambem exibe o Bestiario.
        float bx = x + 570f;
        font.getData().setScale(1.6f); font.setColor(Color.CYAN); font.draw(batch,"BESTIÁRIO",bx,y+height-35);
        font.getData().setScale(1.15f);
        String[] ids = {"LUA", "MARTE", "TITA", "CALISTO"};
        String[] nomes = {"Boss Lua", "Boss Marte", "Boss Titã", "Boss Calisto"};
        for (int i=0;i<4;i++) {
            boolean morto = status.chefesMortos.contains(ids[i]);
            font.setColor(morto ? Color.GREEN : Color.GRAY);
            font.draw(batch, nomes[i] + ": " + (morto ? "DERROTADO" : "???"), bx, y+height-100-i*55);
        }
        font.getData().setScale(1f); font.setColor(Color.GRAY); font.draw(batch,"1/2 comprar | ESC fechar",x+35,y+35); batch.end();
    }
}
