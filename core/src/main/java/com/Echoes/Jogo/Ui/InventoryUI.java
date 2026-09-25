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
import com.badlogic.gdx.math.Vector3;

/** Inventario com crafting por arrastar e soltar dois materiais. */
public class InventoryUI {
    private boolean isOpen = false;
    private String arrastando = null;
    private String slotA = null;
    private String slotB = null;
    private String mensagem = "";
    private float mensagemTimer = 0f;
    private final Vector3 mouseHud = new Vector3();

    private final String[] materiais = {"METAL", "CIRCUITO", "GELO", "PECA"};

    public void update(PlayerStatus status) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.I)) {
            isOpen = !isOpen;
            arrastando = null;
            if (!isOpen) limparSlots();
        }
        if (!isOpen) return;

        float delta = Gdx.graphics.getDeltaTime();
        if (mensagemTimer > 0f) mensagemTimer -= delta;

        // A HUD usa uma camera 1280x720. Converte o mouse para a mesma
        // coordenada usada no desenho, inclusive em janelas com escala diferente.
        mouseHud.set(Gdx.input.getX(), Gdx.input.getY(), 0f);
        hudToWorld(mouseHud);
        float mouseX = mouseHud.x;
        float mouseY = mouseHud.y;

        float x = 100f, y = 80f, width = 1080f, height = 560f;
        float listaX = x + 35f;
        float listaTop = y + height - 125f;

        if (Gdx.input.justTouched()) {
            // Itens produzidos pelo craft: use o mesmo retangulo que aparece no render.
            if (mouseX >= x + 35f && mouseX <= x + 435f && mouseY >= y + 112f && mouseY <= y + 145f) {
                if (status.inventario.tem("MUNICAO_X3")) {
                    status.inventario.remover("MUNICAO_X3", 1);
                    status.municao += 3;
                    mensagem = "MUNICAO +3"; mensagemTimer = 2f;
                    return;
                }
            }
            if (mouseX >= x + 35f && mouseX <= x + 435f && mouseY >= y + 62f && mouseY <= y + 95f) {
                if (status.inventario.tem("VIDA_X25")) {
                    status.inventario.remover("VIDA_X25", 1);
                    status.hp = Math.min(100f, status.hp + 25f);
                    mensagem = "VIDA +25 HP"; mensagemTimer = 2f;
                    return;
                }
            }

            // A area inteira da linha e clicavel, nao apenas o texto.
            for (int i = 0; i < materiais.length; i++) {
                float rowY = listaTop - i * 55f - 42f;
                float rowH = 46f;
                if (mouseX >= listaX - 10f && mouseX <= listaX + 410f &&
                    mouseY >= rowY && mouseY <= rowY + rowH &&
                    status.inventario.getQuantidade(materiais[i]) > 0) {
                    arrastando = materiais[i];
                    break;
                }
            }
        }

        if (arrastando != null && !Gdx.input.isTouched()) {
            // Mesmos retangulos usados visualmente para os dois slots.
            if (mouseX >= x + 500f && mouseX <= x + 720f && mouseY >= y + 255f && mouseY <= y + 405f) {
                slotA = arrastando;
                tentarCraftar(status);
            } else if (mouseX >= x + 735f && mouseX <= x + 955f && mouseY >= y + 255f && mouseY <= y + 405f) {
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
            mensagem = "RECEITA INVALIDA";
            mensagemTimer = 2.5f;
            limparSlots();
            return;
        }
        if (!status.inventario.tem(slotA) || !status.inventario.tem(slotB)) {
            mensagem = "FALTAM MATERIAIS";
            mensagemTimer = 2.5f;
            limparSlots();
            return;
        }
        status.inventario.remover(slotA, 1);
        status.inventario.remover(slotB, 1);

        // Aplica o efeito imediatamente: o resultado do crafting e um efeito
        // de status, e nao um item que precisa ser usado depois.
        if ("MUNICAO_X3".equals(resultado)) {
            status.municao += 3;
            mensagem = "CRAFT: MUNICAO +3!";
        } else if ("VIDA_X25".equals(resultado)) {
            float hpAntes = status.hp;
            status.hp = Math.min(100f, status.hp + 25f);
            float recuperado = status.hp - hpAntes;
            mensagem = "CRAFT: VIDA +" + (int) recuperado + " HP!";
        } else {
            status.inventario.add(resultado, 1);
            mensagem = "CRAFT: " + resultado + "!";
        }
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

    private void hudToWorld(Vector3 v) {
        v.x = v.x * 1280f / Math.max(1, Gdx.graphics.getWidth());
        v.y = 720f - (v.y * 720f / Math.max(1, Gdx.graphics.getHeight()));
    }

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

        // Caixas dos slots ficam visiveis para deixar claro onde soltar os materiais.
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.LIGHT_GRAY);
        shapeRenderer.rect(x, y, width, height);
        shapeRenderer.setColor(Color.CYAN);
        shapeRenderer.rect(x + 500f, y + 255f, 220f, 150f);
        shapeRenderer.rect(x + 735f, y + 255f, 220f, 150f);
        shapeRenderer.end();

        batch.setProjectionMatrix(hudCamera.combined);
        batch.begin();
        font.setColor(Color.YELLOW); font.getData().setScale(1.5f);
        font.draw(batch, "INVENTARIO", x + 35f, y + height - 35f);
        font.getData().setScale(1.05f); font.setColor(Color.WHITE);
        font.draw(batch, "ARRASTE um material para cada slot", x + 35f, y + height - 75f);

        float iy = y + height - 125f;
        for (int i = 0; i < materiais.length; i++) {
            String m = materiais[i];
            int qtd = status.inventario.getQuantidade(m);
            font.setColor(qtd > 0 ? Color.WHITE : Color.DARK_GRAY);
            font.draw(batch, m + " x" + qtd, x + 45f, iy);
            iy -= 55f;
        }
        font.setColor(Color.GREEN);
        font.draw(batch, "CRAFT: METAL + CIRCUITO = MUNICAO +3", x + 45f, y + 130f);
        font.draw(batch, "CRAFT: GELO + PECA = VIDA +25 HP", x + 45f, y + 80f);

        font.setColor(Color.ORANGE); font.getData().setScale(1.25f);
        font.draw(batch, "CRAFTING", x + 520f, y + height - 75f);
        font.getData().setScale(1f); font.setColor(Color.LIGHT_GRAY);
        font.draw(batch, "SOLTE AQUI", x + 570f, y + 385f);
        font.draw(batch, "SOLTE AQUI", x + 805f, y + 385f);
        font.setColor(Color.WHITE);
        font.draw(batch, "SLOT 1", x + 575f, y + 300f);
        font.draw(batch, slotA == null ? "[ vazio ]" : slotA, x + 525f, y + 275f);
        font.draw(batch, "SLOT 2", x + 810f, y + 300f);
        font.draw(batch, slotB == null ? "[ vazio ]" : slotB, x + 760f, y + 275f);

        font.setColor(Color.CYAN);
        font.draw(batch, "METAL + CIRCUITO  ->  MUNICAO +3", x + 500f, y + 205f);
        font.draw(batch, "GELO + PECA      ->  VIDA +25 HP", x + 500f, y + 170f);

        if (arrastando != null) {
            font.setColor(Color.YELLOW);
            font.draw(batch, "ARRASTANDO: " + arrastando, x + 500f, y + 125f);
        } else if (mensagemTimer > 0f) {
            font.setColor(Color.GREEN);
            font.draw(batch, mensagem, x + 500f, y + 125f);
        }

        font.setColor(Color.GRAY);
        font.draw(batch, "I para fechar", x + width - 140f, y + 30f);
        batch.end();
    }
}
