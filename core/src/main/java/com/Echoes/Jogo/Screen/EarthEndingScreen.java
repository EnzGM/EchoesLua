package com.Echoes.Jogo.Screen;

import com.Echoes.Jogo.Main;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * ITEM 20 (preparo do 21): disparada depois da terceira fala de Aharin.
 * Por enquanto so um texto de fechamento — o ITEM 21 substitui este arquivo
 * inteiro pelo video (ending.webm/.ogg) ou pela sequencia de 8-12s de frases
 * com fade. Nada em AharinScreen precisa mudar de novo quando isso acontecer.
 */
public class EarthEndingScreen implements Screen {

    private final Main game;
    private OrthographicCamera camera;
    private Viewport viewport;
    private SpriteBatch batch;
    private BitmapFont fontTitulo, fontTexto;
    private final GlyphLayout layout = new GlyphLayout();

    public EarthEndingScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(null);

        camera = new OrthographicCamera();
        viewport = new FitViewport(1280, 720, camera);
        camera.position.set(640, 360, 0);

        batch = new SpriteBatch();
        fontTitulo = new BitmapFont();
        fontTitulo.getData().setScale(2.8f);
        fontTexto = new BitmapFont();
        fontTexto.getData().setScale(1.2f);
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            game.setScreen(new MenuScreen(game));
            dispose();
            return;
        }

        Gdx.gl.glClearColor(0.02f, 0.02f, 0.05f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        fontTitulo.setColor(Color.WHITE);
        layout.setText(fontTitulo, "A TERRA AINDA PODE ESCOLHER");
        fontTitulo.draw(batch, layout, 640 - layout.width / 2f, 420);

        fontTexto.setColor(Color.LIGHT_GRAY);
        layout.setText(fontTexto, "(ITEM 21: video/fade de encerramento entra aqui)");
        fontTexto.draw(batch, layout, 640 - layout.width / 2f, 340);

        layout.setText(fontTexto, "Pressione ENTER ou M para voltar ao menu");
        fontTexto.draw(batch, layout, 640 - layout.width / 2f, 280);

        batch.end();
    }

    @Override public void resize(int width, int height) { viewport.update(width, height); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        fontTitulo.dispose();
        fontTexto.dispose();
    }
}
