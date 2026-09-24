package com.Echoes.Jogo.Screen;

import com.Echoes.Jogo.Entities.PlayerStatus;
import com.Echoes.Jogo.Main;
import com.Echoes.Jogo.Managers.MissionState;
import com.Echoes.Jogo.Managers.SaveManager;
import com.Echoes.Jogo.Ui.DialogueSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * ITEM 20: Aharin, planeta proximo a Rigel. Paleta azul-branco luminosa —
 * de proposito bem diferente do navy gelado de Calisto (sem gelo aqui).
 *
 * Sem combate: so as 3 falas das entidades de Luz (reaproveitando o
 * DialogueSystem que ja existe). No fim da terceira fala, dispara a
 * transicao pro encerramento (ITEM 21 substitui a EarthEndingScreen
 * placeholder pelo video/fade final — nada neste arquivo muda de novo).
 */
public class AharinScreen implements Screen {

    private final Main game;
    private final PlayerStatus status;
    private MissionState missao;

    private OrthographicCamera camera;
    private Viewport viewport;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;       // usado pelo DialogueSystem
    private BitmapFont fontTitulo;
    private final GlyphLayout layout = new GlyphLayout();

    private DialogueSystem dialogueSystem;
    private boolean trocandoTela = false;

    public AharinScreen(Main game, PlayerStatus status) {
        this.game = game;
        this.status = (status != null) ? status : new PlayerStatus();
        this.status.faseAtual = "AHARIN";
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(null);

        camera = new OrthographicCamera();
        viewport = new FitViewport(1280, 720, camera);
        camera.position.set(640, 360, 0);

        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        font = new BitmapFont();
        fontTitulo = new BitmapFont();
        fontTitulo.getData().setScale(2.6f);

        missao = new MissionState();

        String[] falasAharin = new String[]{
            "Voce atravessou quatro mundos por uma pergunta, nao por uma arma.",
            "A Luz nao se guarda: ela se entrega a quem nao destroi.",
            "Volte. A Terra ainda pode escolher."
        };
        dialogueSystem = new DialogueSystem(falasAharin);

        SaveManager.salvarJogo(status, missao);
    }

    @Override
    public void render(float delta) {
        if (dialogueSystem.ativo) {
            dialogueSystem.update();
        } else if (!trocandoTela) {
            // ITEM 20: fim da terceira fala -> dispara o encerramento.
            trocandoTela = true;
            SaveManager.salvarJogo(status, missao);
            game.setScreen(new EndingSelectScreen(game, status));
            dispose();
            return;
        }

        viewport.apply();
        camera.update();

        Gdx.gl.glClearColor(0.85f, 0.92f, 1f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Gradiente azul-branco luminoso — bem diferente do navy gelado de Calisto.
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.rect(0, 0, 1280, 720,
            new Color(0.55f, 0.75f, 0.97f, 1f),
            new Color(0.55f, 0.75f, 0.97f, 1f),
            new Color(0.96f, 0.98f, 1f, 1f),
            new Color(0.96f, 0.98f, 1f, 1f));
        shapeRenderer.end();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        fontTitulo.setColor(new Color(0.15f, 0.28f, 0.55f, 1f));
        layout.setText(fontTitulo, "AHARIN");
        fontTitulo.draw(batch, layout, 640 - layout.width / 2f, 660);
        batch.end();

        if (dialogueSystem.ativo) {
            dialogueSystem.render(shapeRenderer, batch, font, 1280, 720);
        }
    }

    @Override public void resize(int width, int height) { viewport.update(width, height); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        font.dispose();
        fontTitulo.dispose();
    }
}
