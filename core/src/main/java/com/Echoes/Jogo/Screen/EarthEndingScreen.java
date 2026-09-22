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
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * ITEM 21: encerramento de verdade — sem depender de arquivo de vídeo
 * (gdx-video exigiria uma extensão à parte que o projeto não tem). Em vez
 * disso: uma sequência de frases com fade in/out, fundo que vai clareando
 * do espaço escuro pra uma luz dourada, e um campo de estrelas cintilando.
 * No fim, título + créditos e volta ao menu (ENTER/M), igual as outras
 * telas de encerramento do jogo.
 */
public class EarthEndingScreen implements Screen {

    private final Main game;
    private OrthographicCamera camera;
    private Viewport viewport;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont fontTitulo, fontTexto, fontDica;
    private final GlyphLayout layout = new GlyphLayout();

    private static class Frase {
        final String texto;
        final float duracao;
        Frase(String texto, float duracao) {
            this.texto = texto;
            this.duracao = duracao;
        }
    }

    private final Frase[] frases = {
        new Frase("A JORNADA CHEGOU AO FIM", 3f),
        new Frase("Quatro mundos atravessados.", 2.6f),
        new Frase("Guardioes silenciados, nao por odio, mas por necessidade.", 3.2f),
        new Frase("Uma chave. Depois outra. Depois a ultima: a Luz.", 3.2f),
        new Frase("Ela nao se guarda. Ela se entrega a quem nao destroi.", 3.4f),
        new Frase("A TERRA ESCOLHEU A LUZ", 3.6f),
    };

    private float duracaoTotalFrases;
    private float tempoDecorrido = 0f;
    private boolean fraseFaseTerminada = false;

    // Campo de estrelas de fundo (só decorativo, gerado uma vez).
    private static final int NUM_ESTRELAS = 90;
    private final float[] estrelaX = new float[NUM_ESTRELAS];
    private final float[] estrelaY = new float[NUM_ESTRELAS];
    private final float[] estrelaFase = new float[NUM_ESTRELAS];

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
        shapeRenderer = new ShapeRenderer();

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        fontTitulo = new BitmapFont();
        fontTitulo.getData().setScale(2.6f);
        fontTexto = new BitmapFont();
        fontTexto.getData().setScale(1.4f);
        fontDica = new BitmapFont();
        fontDica.getData().setScale(1.1f);

        duracaoTotalFrases = 0f;
        for (Frase f : frases) duracaoTotalFrases += f.duracao;

        for (int i = 0; i < NUM_ESTRELAS; i++) {
            estrelaX[i] = MathUtils.random(0f, 1280f);
            estrelaY[i] = MathUtils.random(0f, 720f);
            estrelaFase[i] = MathUtils.random(0f, MathUtils.PI2);
        }
    }

    @Override
    public void render(float delta) {
        boolean pedirVoltarAoMenu = Gdx.input.isKeyJustPressed(Input.Keys.ENTER)
            || Gdx.input.isKeyJustPressed(Input.Keys.M);

        if (pedirVoltarAoMenu) {
            game.setScreen(new MenuScreen(game));
            dispose();
            return;
        }

        tempoDecorrido += delta;
        if (tempoDecorrido >= duracaoTotalFrases) {
            fraseFaseTerminada = true;
        }

        viewport.apply();
        camera.update();

        // Progresso 0..1 ao longo das frases — usado pra clarear o fundo aos poucos.
        float progresso = MathUtils.clamp(tempoDecorrido / duracaoTotalFrases, 0f, 1f);
        Color corFundoEscuro = new Color(0.02f, 0.02f, 0.06f, 1f);
        Color corFundoClaro = new Color(0.55f, 0.42f, 0.12f, 1f);
        Color corFundo = new Color(
            MathUtils.lerp(corFundoEscuro.r, corFundoClaro.r, progresso),
            MathUtils.lerp(corFundoEscuro.g, corFundoClaro.g, progresso),
            MathUtils.lerp(corFundoEscuro.b, corFundoClaro.b, progresso),
            1f
        );

        Gdx.gl.glClearColor(corFundo.r, corFundo.g, corFundo.b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        desenharEstrelas(progresso);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        if (!fraseFaseTerminada) {
            desenharFraseAtual();
        } else {
            desenharCreditos();
        }

        batch.end();
    }

    /** Estrelas cintilando, ficando mais discretas conforme o fundo vai clareando. */
    private void desenharEstrelas(float progresso) {
        float opacidadeMax = 1f - progresso * 0.7f; // somem parcialmente com a luz do final
        if (opacidadeMax <= 0f) return;

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < NUM_ESTRELAS; i++) {
            float brilho = (MathUtils.sin(tempoDecorrido * 2f + estrelaFase[i]) + 1f) / 2f;
            float alpha = brilho * opacidadeMax;
            shapeRenderer.setColor(1f, 1f, 1f, alpha);
            shapeRenderer.circle(estrelaX[i], estrelaY[i], 1.6f);
        }
        shapeRenderer.end();
    }

    /** Acha em qual frase o tempo atual cai e desenha ela com fade in/out. */
    private void desenharFraseAtual() {
        float acumulado = 0f;
        for (Frase f : frases) {
            float inicio = acumulado;
            float fim = acumulado + f.duracao;

            if (tempoDecorrido >= inicio && tempoDecorrido < fim) {
                float tempoNaFrase = tempoDecorrido - inicio;
                float fadeDuracao = Math.min(0.6f, f.duracao * 0.3f);

                float alpha;
                if (tempoNaFrase < fadeDuracao) {
                    alpha = tempoNaFrase / fadeDuracao; // fade in
                } else if (tempoNaFrase > f.duracao - fadeDuracao) {
                    alpha = (f.duracao - tempoNaFrase) / fadeDuracao; // fade out
                } else {
                    alpha = 1f;
                }
                alpha = MathUtils.clamp(alpha, 0f, 1f);

                BitmapFont fonteUsada = (f == frases[0] || f == frases[frases.length - 1])
                    ? fontTitulo
                    : fontTexto;

                fonteUsada.setColor(1f, 1f, 1f, alpha);
                layout.setText(fonteUsada, f.texto);
                fonteUsada.draw(batch, layout, 640 - layout.width / 2f, 380);
                return;
            }
            acumulado = fim;
        }
    }

    private void desenharCreditos() {
        fontTitulo.setColor(Color.WHITE);
        layout.setText(fontTitulo, "MISSAO ARTEMIS");
        fontTitulo.draw(batch, layout, 640 - layout.width / 2f, 440);

        fontTexto.setColor(new Color(1f, 0.95f, 0.8f, 1f));
        layout.setText(fontTexto, "Obrigado por jogar.");
        fontTexto.draw(batch, layout, 640 - layout.width / 2f, 370);

        // Pisca suavemente pra chamar atenção sem ser irritante.
        float piscar = (MathUtils.sin(tempoDecorrido * 3f) + 1f) / 2f;
        fontDica.setColor(1f, 1f, 1f, 0.5f + piscar * 0.5f);
        layout.setText(fontDica, "Pressione ENTER ou M para voltar ao menu");
        fontDica.draw(batch, layout, 640 - layout.width / 2f, 280);
    }

    @Override public void resize(int width, int height) { viewport.update(width, height); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        fontTitulo.dispose();
        fontTexto.dispose();
        fontDica.dispose();
    }
}
