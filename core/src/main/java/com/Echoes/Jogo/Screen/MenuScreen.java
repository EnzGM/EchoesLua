package com.Echoes.Jogo.Screen;

import com.Echoes.Jogo.Entities.PlayerStatus;
import com.Echoes.Jogo.Main;
import com.Echoes.Jogo.Managers.MissionState;
import com.Echoes.Jogo.Managers.SaveManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.ArrayList;
import java.util.List;

/** Tela inicial: fundo com gradiente + botões clicáveis (CONTINUAR / NOVO JOGO / SAIR). */
public class MenuScreen implements Screen {

    private final Main game;

    private OrthographicCamera camera;
    private Viewport viewport;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;

    private BitmapFont fontTitulo;
    private BitmapFont fontSubtitulo;
    private BitmapFont fontBotao;
    private final GlyphLayout layout = new GlyphLayout();

    private Stage stage;
    private final List<Texture> texturasGeradas = new ArrayList<>();

    public MenuScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(1280, 720, camera);
        camera.position.set(640, 360, 0);

        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        fontTitulo = new BitmapFont();
        fontTitulo.getData().setScale(4f);
        fontSubtitulo = new BitmapFont();
        fontSubtitulo.getData().setScale(1.2f);

        stage = new Stage(new FitViewport(1280, 720));
        Gdx.input.setInputProcessor(stage);

        TextButton.TextButtonStyle estiloBotao = criarEstiloBotao();
        boolean temSave = SaveManager.hasSave();

        Table table = new Table();
        table.setFillParent(true);
        table.center();
        table.padTop(80f);

        if (temSave) {
            TextButton btnContinuar = new TextButton("CONTINUAR", estiloBotao);
            btnContinuar.getLabel().setFontScale(1.4f);
            btnContinuar.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    continuarJogo();
                }
            });
            table.add(btnContinuar).width(340f).height(72f).padBottom(20f).row();
        }

        TextButton btnNovoJogo = new TextButton("NOVO JOGO", estiloBotao);
        btnNovoJogo.getLabel().setFontScale(1.4f);
        btnNovoJogo.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                iniciarNovoJogo();
            }
        });
        table.add(btnNovoJogo).width(340f).height(72f).padBottom(20f).row();

        TextButton btnSair = new TextButton("SAIR", estiloBotao);
        btnSair.getLabel().setFontScale(1.4f);
        btnSair.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });
        table.add(btnSair).width(340f).height(72f);

        stage.addActor(table);
    }

    private TextButton.TextButtonStyle criarEstiloBotao() {
        TextButton.TextButtonStyle estilo = new TextButton.TextButtonStyle();
        fontBotao = new BitmapFont();
        estilo.font = fontBotao;
        estilo.up = criarDrawable(new Color(0.15f, 0.35f, 0.55f, 1f));
        estilo.over = criarDrawable(new Color(0.24f, 0.50f, 0.74f, 1f));
        estilo.down = criarDrawable(new Color(0.10f, 0.24f, 0.38f, 1f));
        estilo.fontColor = Color.WHITE;
        return estilo;
    }

    private TextureRegionDrawable criarDrawable(Color cor) {
        Pixmap pixmap = new Pixmap(4, 4, Pixmap.Format.RGBA8888);
        pixmap.setColor(cor);
        pixmap.fill();
        Texture textura = new Texture(pixmap);
        pixmap.dispose();
        texturasGeradas.add(textura);
        return new TextureRegionDrawable(new TextureRegion(textura));
    }

    private void iniciarNovoJogo() {
        game.setScreen(new LunarScreen(game, new PlayerStatus()));
        dispose();
    }

    /** MELHORIA 4: carrega o save unificado e manda pra tela certa (Lua ou Marte). */
    private void continuarJogo() {
        PlayerStatus status = new PlayerStatus();
        MissionState missao = new MissionState();
        boolean ok = SaveManager.carregarJogo(status, missao);

        if (!ok) {
            iniciarNovoJogo();
            return;
        }

        status.missaoEtapa = missao.getEtapa();

        if (status.faseAtual.equals("MARTE")) {
            game.setScreen(new MarsScreen(game, status));
        } else {
            game.setScreen(new LunarScreen(game, status));
        }
        dispose();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.04f, 0.05f, 0.09f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        camera.update();

        // Fundo com gradiente vertical, efeito "espaço"
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.rect(0, 0, 1280, 720,
            new Color(0.02f, 0.02f, 0.06f, 1f),
            new Color(0.02f, 0.02f, 0.06f, 1f),
            new Color(0.10f, 0.17f, 0.30f, 1f),
            new Color(0.10f, 0.17f, 0.30f, 1f));
        shapeRenderer.end();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        fontTitulo.setColor(Color.CYAN);
        layout.setText(fontTitulo, "MISSAO ARTEMIS");
        fontTitulo.draw(batch, layout, 640 - layout.width / 2f, 640);

        fontSubtitulo.setColor(Color.LIGHT_GRAY);
        layout.setText(fontSubtitulo, "Sobreviva na Lua. Repare a base. Enfrente Marte.");
        fontSubtitulo.draw(batch, layout, 640 - layout.width / 2f, 575);

        batch.end();

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
        stage.getViewport().update(width, height, true);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        fontTitulo.dispose();
        fontSubtitulo.dispose();
        if (fontBotao != null) fontBotao.dispose();
        stage.dispose();
        for (Texture t : texturasGeradas) t.dispose();
        texturasGeradas.clear();
    }
}
