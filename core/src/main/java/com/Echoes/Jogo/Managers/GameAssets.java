package com.Echoes.Jogo.Managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

/**
 * Carrega o atlas de texturas gerado pelo TexturePacker (Aula 07).
 * Feito pra ser "à prova de falha": se o arquivo lunar.atlas ainda não existir
 * (porque você ainda não colocou as texturas), o jogo continua rodando
 * normalmente com os retângulos coloridos.
 */
public class GameAssets {
    private TextureAtlas atlas;
    private boolean atlasCarregado = false;

    public void carregar() {
        try {
            if (Gdx.files.internal("lunar.atlas").exists()) {
                atlas = new TextureAtlas(Gdx.files.internal("lunar.atlas"));
                atlasCarregado = true;
            }
        } catch (Exception e) {
            Gdx.app.error("GameAssets", "Falha ao carregar lunar.atlas: " + e.getMessage());
        }
    }

    /** Retorna a região com esse nome, ou null se o atlas não estiver carregado/não tiver essa região. */
    public TextureAtlas.AtlasRegion getRegion(String nome) {
        if (!atlasCarregado) return null;
        return atlas.findRegion(nome);
    }

    public boolean isCarregado() {
        return atlasCarregado;
    }

    public void dispose() {
        if (atlas != null) atlas.dispose();
    }
}
