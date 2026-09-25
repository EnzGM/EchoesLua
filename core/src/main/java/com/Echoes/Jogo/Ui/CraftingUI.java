package com.Echoes.Jogo.Ui;

import com.Echoes.Jogo.Entities.PlayerStatus;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * ITEM 22: menu da bancada de crafting. Cada receita pede exatamente 2
 * materiais (1 unidade de cada) do Inventario e aplica o efeito do resultado
 * diretamente no status do jogador. Se faltar QUALQUER material da receita,
 * nada e consumido — so aparece o aviso de "faltam materiais".
 */
public class CraftingUI {

    public static class Receita {
        public final String materialA, materialB, resultado;
        public final int tecla;

        public Receita(String materialA, String materialB, String resultado, int tecla) {
            this.materialA = materialA;
            this.materialB = materialB;
            this.resultado = resultado;
            this.tecla = tecla;
        }
    }

    private final Receita[] receitas = {
        new Receita("GELO", "PECA", "VIDA_X25", Input.Keys.NUM_1),
        new Receita("METAL", "CIRCUITO", "MUNICAO_X3", Input.Keys.NUM_2),
    };

    private boolean aberta = false;
    private String ultimaMensagem = "";
    private float mensagemTimer = 0f;

    public boolean isAberta() {
        return aberta;
    }

    public void abrir() {
        aberta = true;
        ultimaMensagem = "";
        mensagemTimer = 0f;
    }

    public void fechar() {
        aberta = false;
    }

    /** Processa as teclas 1/2 (craftar) e E/ESC (fechar), só enquanto estiver aberta. */
    public void update(float delta, PlayerStatus status) {
        if (!aberta) return;

        if (mensagemTimer > 0f) mensagemTimer -= delta;

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            fechar();
            return;
        }

        for (Receita r : receitas) {
            if (Gdx.input.isKeyJustPressed(r.tecla)) {
                tentarCraftar(r, status);
            }
        }
    }

    private void tentarCraftar(Receita r, PlayerStatus status) {
        boolean temA = status.inventario.tem(r.materialA);
        boolean temB = status.inventario.tem(r.materialB);

        if (temA && temB) {
            status.inventario.remover(r.materialA, 1);
            status.inventario.remover(r.materialB, 1);

            // O resultado do craft e aplicado imediatamente ao status do jogador.
            // Antes, a bancada apenas criava um item no inventario e nao alterava
            // HP/municao, dando a impressao de que o craft nao funcionava.
            if ("MUNICAO_X3".equals(r.resultado)) {
                status.municao += 3;
                ultimaMensagem = "Craft concluido: MUNICAO +3!";
            } else if ("VIDA_X25".equals(r.resultado)) {
                float hpAntes = status.hp;
                status.hp = Math.min(100f, status.hp + 25f);
                float recuperado = status.hp - hpAntes;
                ultimaMensagem = "Craft concluido: VIDA +" + (int) recuperado + " HP!";
            } else {
                // Fallback para futuras receitas que realmente produzam um item.
                status.inventario.add(r.resultado, 1);
                ultimaMensagem = "Craft concluido: " + r.resultado + "!";
            }
        } else {
            // Se faltar material, a receita NAO consome o outro item (nao mexe em nada).
            ultimaMensagem = "Faltam materiais para " + r.resultado + ".";
        }
        mensagemTimer = 2.5f;
    }

    public void render(ShapeRenderer shapeRenderer, SpriteBatch batch, BitmapFont font,
                       OrthographicCamera hudCamera, PlayerStatus status) {
        if (!aberta) return;

        hudCamera.update();

        float width = 500, height = 70 + receitas.length * 70f;
        float x = (hudCamera.viewportWidth - width) / 2f;
        float y = (hudCamera.viewportHeight - height) / 2f;

        shapeRenderer.setProjectionMatrix(hudCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.08f, 0.08f, 0.12f, 0.95f);
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

        font.getData().setScale(1.4f);
        font.setColor(Color.YELLOW);
        font.draw(batch, "--- BANCADA DE CRAFTING ---", x + 40, y + height - 25);

        font.getData().setScale(1.1f);
        float linhaY = y + height - 75;
        int numero = 1;
        for (Receita r : receitas) {
            int qtdA = status.inventario.getQuantidade(r.materialA);
            int qtdB = status.inventario.getQuantidade(r.materialB);
            boolean pronto = qtdA >= 1 && qtdB >= 1;

            font.setColor(pronto ? Color.GREEN : Color.GRAY);
            String resultadoTexto = "MUNICAO_X3".equals(r.resultado) ? "MUNICAO +3"
                : "VIDA_X25".equals(r.resultado) ? "VIDA +25 HP" : r.resultado;
            String txt = "[" + numero + "] " + r.materialA + " (" + qtdA + ") + "
                + r.materialB + " (" + qtdB + ") -> " + resultadoTexto;
            font.draw(batch, txt, x + 30, linhaY);
            linhaY -= 40f;
            numero++;
        }

        if (mensagemTimer > 0f) {
            font.setColor(Color.WHITE);
            font.draw(batch, ultimaMensagem, x + 30, y + 40);
        }

        font.getData().setScale(1f);
        font.setColor(Color.GRAY);
        font.draw(batch, "Pressione o numero da receita | E ou ESC para fechar", x + 30, y + 15);

        batch.end();
    }
}
