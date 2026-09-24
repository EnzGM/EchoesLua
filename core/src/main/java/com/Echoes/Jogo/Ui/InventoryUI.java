package com.Echoes.Jogo.Ui;

import com.Echoes.Jogo.Entities.Inventario;
import com.Echoes.Jogo.Entities.PlayerStatus;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import java.util.ArrayList;
import java.util.List;

/** Inventario com crafting por arrastar dois materiais para os slots. */
public class InventoryUI {
    private boolean isOpen = false;
    private String arrastando = null;
    private String slotA = null;
    private String slotB = null;
    private String mensagem = "";
    private float mensagemTimer = 0f;

    private final String[] materiais = {"METAL", "CIRCUITO", "GELO", "PECA"};

    public void update(PlayerStatus status) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.I)) {
            isOpen = !isOpen;
            arrastando = null;
            if (!isOpen) limparSlots();
        }
        if (!isOpen) return;

        if (mensagemTimer > 0f) mensagemTimer -= Gdx.graphics.getDeltaTime();

        float mouseX = Gdx.input.getX();
        float mouseY = 720f - Gdx.input.getY();
        float x = 100f, y = 80f, width = 1080f, height = 560f;
        float listaX = x + 35f;
        float listaTop = y + height - 115f;

        if (Gdx.input.justTouched()) {
            // Itens craftados podem ser usados diretamente pelo inventario.
            if (mouseX >= x + 35f && mouseX <= x + 430f && mouseY >= y + 95f && mouseY <= y + 150f) {
                if (status.inventario.tem("MUNICAO_X3")) {
                    status.inventario.remover("MUNICAO_X3", 1);
                    status.municao += 3;
                    mensagem = "MUNICAO +3"; mensagemTimer = 2f;
                }
            } else if (mouseX >= x + 35f && mouseX <= x + 430f && mouseY >= y + 45f && mouseY <= y + 95f) {
                if (status.inventario.tem("VIDA_X25")) {
                    status.inventario.remover("VIDA_X25", 1);
                    status.hp = Math.min(100f, status.hp + 25f);
                    mensagem = "VIDA +25 HP"; mensagemTimer = 2f;
                }
            } else if (mouseX >= listaX && mouseX <= listaX + 390f) {
                for (int i = 0; i < materiais.length; i++) {
                    float iy = listaTop - i * 55f;
                    if (mouseY >= iy - 28f && mouseY <= iy + 12f && status.inventario.getQuantidade(materiais[i]) > 0) {
                        arrastando = materiais[i];
                        break;
                    }
                }
            }
        }

        if (arrastando != null && !Gdx.input.isTouched()) {
            if (mouseX >= x + 500f && mouseX <= x + 700f && mouseY >= y + 280f && mouseY <= y + 390f) {
                slotA = arrastando;
                tentarCraftar(status);
            } else if (mouseX >= x + 730f && mouseX <= x + 930f && mouseY >= y + 280f && mouseY <= y + 390f) {
                slotB = arrastando;
                tentarCraftar(status);
            }
            arrastando = null;
        }
    }

    private void tentarCraftar(PlayerStatus status) {
        if (slotA == null || slotB == null) return;
        String resultado = receita(slotA, slotB);
        if (resultado == null) {
            mensagem = "Receita invalida: use os materiais certos.";
            mensagemTimer = 2.5f;
            return;
        }
        if (!status.inventario.tem(slotA) || !status.inventario.tem(slotB)) {
            mensagem = "Faltam materiais.";
            mensagemTimer = 2.5f;
            limparSlots();
            return;
        }
        status.inventario.remover(slotA, 1);
        status.inventario.remover(slotB, 1);
        status.inventario.add(resultado, 1);
        mensagem = "CRAFT: " + resultado + "!";
        mensagemTimer = 2.5f;
        limparSlots();
    }

    private String receita(String a, String b) {
        if (("METAL".equals(a) && "CIRCUITO".equals(b)) || ("CIRCUITO".equals(a) && "METAL".equals(b))) return "MUNICAO_X3";
        if (("GELO".equals(a) && "PECA".equals(b)) || ("PECA".equals(a) && "GELO".equals(b))) return "VIDA_X25";
        return null;
    }

    private void limparSlots() { slotA = null; slotB = null; }

    public boolean isOpen() { return isOpen; }

    public void render(ShapeRenderer shapeRenderer, SpriteBatch batch, BitmapFont font,
                       OrthographicCamera hudCamera, PlayerStatus status) {
        if (!isOpen) return;
        hudCamera.update();
        float x = 100f, y = 80f, width = 1080f, height = 560f;

        shapeRenderer.setProjectionMatrix(hudCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.035f, 0.04f, 0.07f, 0.98f);
        shapeRenderer.rect(x, y, width, height);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.LIGHT_GRAY);
        shapeRenderer.rect(x, y, width, height);
        shapeRenderer.end();

        batch.setProjectionMatrix(hudCamera.combined);
        batch.begin();
        font.setColor(Color.YELLOW); font.getData().setScale(1.5f);
        font.draw(batch, "INVENTARIO", x + 35f, y + height - 35f);
        font.getData().setScale(1.05f); font.setColor(Color.WHITE);
        font.draw(batch, "MATERIAIS — arraste 2 itens para os slots", x + 35f, y + height - 75f);

        float iy = y + height - 125f;
        for (String m : materiais) {
            int qtd = status.inventario.getQuantidade(m);
            font.setColor(qtd > 0 ? Color.WHITE : Color.DARK_GRAY);
            font.draw(batch, m + " x" + qtd, x + 45f, iy);
            iy -= 55f;
        }
        font.setColor(Color.GREEN);
        font.draw(batch, "MUNICAO_X3 x" + status.inventario.getQuantidade("MUNICAO_X3"), x + 45f, y + 130f);
        font.draw(batch, "VIDA_X25 x" + status.inventario.getQuantidade("VIDA_X25"), x + 45f, y + 80f);
        font.setColor(Color.GRAY);
        font.draw(batch, "Clique em um item craftado para usar", x + 45f, y + 50f);

        font.setColor(Color.ORANGE); font.getData().setScale(1.25f);
        font.draw(batch, "CRAFTING", x + 520f, y + height - 75f);
        font.getData().setScale(1f); font.setColor(Color.LIGHT_GRAY);
        font.draw(batch, "SOLTE OS DOIS MATERIAIS AQUI", x + 500f, y + 325f);
        font.setColor(Color.WHITE);
        font.draw(batch, "SLOT 1: " + (slotA == null ? "[ vazio ]" : slotA), x + 515f, y + 295f);
        font.draw(batch, "SLOT 2: " + (slotB == null ? "[ vazio ]" : slotB), x + 745f, y + 295f);

        font.setColor(Color.CYAN);
        font.draw(batch, "METAL + CIRCUITO  ->  MUNICAO +3", x + 500f, y + 205f);
        font.draw(batch, "GELO + PECA      ->  VIDA +25 HP", x + 500f, y + 170f);

        if (mensagemTimer > 0f) {
            font.setColor(Color.GREEN);
            font.draw(batch, mensagem, x + 500f, y + 115f);
        }

        font.setColor(Color.GRAY);
        font.draw(batch, "I para fechar", x + width - 140f, y + 30f);
        batch.end();
    }
}
