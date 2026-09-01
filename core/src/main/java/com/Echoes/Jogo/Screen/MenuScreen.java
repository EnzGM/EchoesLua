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

/** Tela inicial: título + "pressione ENTER". */
public class MenuScreen implements Screen {

    private final Main game;
    private OrthographicCamera camera;
    private Viewport viewport;
    private SpriteBatch batch;
    private BitmapFont fontTitulo, fontTexto;
    private final GlyphLayout layout = new GlyphLayout(); // usado pra centralizar texto

    public MenuScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(1280, 720, camera);
        camera.position.set(640, 360, 0);

        batch = new SpriteBatch();
        fontTitulo = new BitmapFont();
        fontTitulo.getData().setScale(3f);
        fontTexto = new BitmapFont();
        fontTexto.getData().setScale(1.3f);
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            game.setScreen(new LunarScreen(game));
            dispose();
            return;
        }

        Gdx.gl.glClearColor(0.05f, 0.05f, 0.09f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        fontTitulo.setColor(Color.CYAN);
        layout.setText(fontTitulo, "MISSÃO ARTEMIS");
        fontTitulo.draw(batch, layout, 640 - layout.width / 2f, 460);

        fontTexto.setColor(Color.WHITE);
        layout.setText(fontTexto, "Colete oxigenio, comida e gelo. Processe o gelo na base.");
        fontTexto.draw(batch, layout, 640 - layout.width / 2f, 360);

        layout.setText(fontTexto, "Pressione ENTER para iniciar");
        fontTexto.draw(batch, layout, 640 - layout.width / 2f, 300);

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
