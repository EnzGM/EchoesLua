package com.Echoes.Jogo.Screen;

import com.Echoes.Jogo.Entities.PlayerStatus;
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
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/** Cutscenes curtas do inicio e das transicoes entre portais. */
public class CutsceneScreen implements Screen {
    public enum Destino { LUA, MARTE, TITA, CALISTO, AHARIN }

    private final Main game;
    private final PlayerStatus status;
    private final Destino destino;
    private OrthographicCamera camera;
    private Viewport viewport;
    private SpriteBatch batch;
    private BitmapFont titulo, texto, dica;
    private final GlyphLayout layout = new GlyphLayout();
    private float tempo;
    private float duracao = 3.2f;
    private String tituloTexto, textoTexto;

    public CutsceneScreen(Main game, PlayerStatus status, Destino destino) {
        this.game = game;
        this.status = status != null ? status : new PlayerStatus();
        this.destino = destino;
        configurarTexto();
    }

    private void configurarTexto() {
        switch (destino) {
            case LUA:
                tituloTexto = "ECHOES: A JORNADA";
                textoTexto = "Uma transmissao perdida aponta para a Lua.\nEncontre as chaves, sobreviva aos guardioes e descubra o que existe alem dos portais.";
                break;
            case MARTE:
                tituloTexto = "PORTAL DA LUA";
                textoTexto = "O portal se abre.\nMarte responde ao chamado e uma nova ameaca aguarda do outro lado.";
                break;
            case TITA:
                tituloTexto = "PORTAL DE MARTE";
                textoTexto = "A chave pulsa nas maos do astronauta.\nO caminho para Tita esta aberto, mas os sinais ficam cada vez mais estranhos.";
                break;
            case CALISTO:
                tituloTexto = "PORTAL DE TITA";
                textoTexto = "A escuridao engole o portal.\nCalisto e o ultimo mundo antes da verdade sobre a Luz.";
                break;
            default:
                tituloTexto = "PORTAL DE CALISTO";
                textoTexto = "A Chave de Luz desperta.\nAharin espera do outro lado e a escolha final se aproxima.";
                break;
        }
    }

    @Override public void show() {
        Gdx.input.setInputProcessor(null);
        camera = new OrthographicCamera();
        viewport = new FitViewport(1280, 720, camera);
        camera.position.set(640, 360, 0);
        batch = new SpriteBatch();
        titulo = new BitmapFont(); titulo.getData().setScale(2.5f);
        texto = new BitmapFont(); texto.getData().setScale(1.35f);
        dica = new BitmapFont(); dica.getData().setScale(1.05f);
        tempo = 0f;
    }

    @Override public void render(float delta) {
        tempo += delta;
        boolean pular = Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE);
        if (pular || tempo >= duracao) {
            abrirDestino();
            return;
        }
        viewport.apply(); camera.update();
        float p = MathUtils.clamp(tempo / duracao, 0f, 1f);
        float brilho = 0.04f + p * 0.10f;
        Gdx.gl.glClearColor(brilho, brilho * 0.9f, brilho * 1.4f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        float alpha = MathUtils.clamp(Math.min(tempo / 0.6f, (duracao - tempo) / 0.6f), 0f, 1f);
        titulo.setColor(0.75f, 0.9f, 1f, alpha);
        layout.setText(titulo, tituloTexto);
        titulo.draw(batch, layout, 640 - layout.width / 2f, 470);
        texto.setColor(1f, 1f, 1f, alpha);
        String[] linhas = textoTexto.split("\\n");
        for (int i = 0; i < linhas.length; i++) {
            layout.setText(texto, linhas[i]);
            texto.draw(batch, layout, 640 - layout.width / 2f, 350 - i * 42);
        }
        dica.setColor(0.7f, 0.7f, 0.7f, alpha);
        layout.setText(dica, "ENTER / SPACE para continuar");
        dica.draw(batch, layout, 640 - layout.width / 2f, 120);
        batch.end();
    }

    private void abrirDestino() {
        switch (destino) {
            case LUA: game.setScreen(new LunarScreen(game, status)); break;
            case MARTE: game.setScreen(new MarsScreen(game, status)); break;
            case TITA: game.setScreen(new TitanScreen(game, status)); break;
            case CALISTO: game.setScreen(new CallistoScreen(game, status)); break;
            case AHARIN: game.setScreen(new AharinScreen(game, status)); break;
        }
        dispose();
    }

    @Override public void resize(int width, int height) { if (viewport != null) viewport.update(width, height); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() { if (batch != null) batch.dispose(); if (titulo != null) titulo.dispose(); if (texto != null) texto.dispose(); if (dica != null) dica.dispose(); }
}
