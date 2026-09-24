package com.Echoes.Jogo.Managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

/**
 * Carrega o atlas de texturas (Aula 07) e o background.
 * Feito pra ser "à prova de falha": se algum arquivo nao existir,
 * o jogo continua rodando normalmente com os retangulos coloridos.
 */
public class GameAssets {
    private TextureAtlas atlas;
    private boolean atlasCarregado = false;

    private Texture background;
    private boolean backgroundCarregado = false;

    public void carregar() {
        try {
            if (Gdx.files.internal("lunar.atlas").exists()) {
                atlas = new TextureAtlas(Gdx.files.internal("lunar.atlas"));
                atlasCarregado = true;
            }
        } catch (Exception e) {
            Gdx.app.error("GameAssets", "Falha ao carregar lunar.atlas: " + e.getMessage());
        }

        try {
            if (Gdx.files.internal("background.png").exists()) {
                background = new Texture(Gdx.files.internal("background.png"));
                backgroundCarregado = true;
            }
        } catch (Exception e) {
            Gdx.app.error("GameAssets", "Falha ao carregar background.png: " + e.getMessage());
        }
    }

    /** Retorna a região com esse nome, ou null se o atlas nao estiver carregado/nao tiver essa regiao. */
    public TextureAtlas.AtlasRegion getRegion(String nome) {
        if (!atlasCarregado) return null;
        return atlas.findRegion(nome);
    }

    /** Tenta achar "item_<tipo>" (ex.: item_metal). Retorna null se nao existir essa sprite. */
    public TextureAtlas.AtlasRegion getItemRegion(String tipoNome) {
        return getRegion("item_" + tipoNome.toLowerCase());
    }

    public boolean isCarregado() {
        return atlasCarregado;
    }

    public Texture getBackground() {
        return backgroundCarregado ? background : null;
    }

    public void dispose() {
        if (atlas != null) atlas.dispose();
        if (background != null) background.dispose();
    }
}
