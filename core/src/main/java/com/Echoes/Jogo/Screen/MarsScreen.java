package com.Echoes.Jogo.Screen;

import com.Echoes.Jogo.Entities.PlayerStatus;
import com.Echoes.Jogo.Main;
import com.Echoes.Jogo.Managers.GameAssets;
import com.Echoes.Jogo.Managers.ParticleManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class MarsScreen implements Screen {

    public static final float WORLD_WIDTH = 2560;
    public static final float WORLD_HEIGHT = 1440;

    private final Main game;
    private PlayerStatus status;

    private OrthographicCamera camera;
    private Viewport viewport;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;

    private Rectangle player;
    private float playerSpeed = 220f;
    private int playerVida = 100;

    private GameAssets assets;
    private TextureAtlas.AtlasRegion playerRegion;
    private TextureAtlas.AtlasRegion inimigoRegion;
    private TextureAtlas.AtlasRegion armaRegion;

    private ParticleManager particleManager;
    private float timerTempestade = 0f;

    private static class ObstaculoPedra {
        public Rectangle bounds;
        public ObstaculoPedra(float x, float y, float w, float h) {
            this.bounds = new Rectangle(x, y, w, h);
        }
    }
    private Array<ObstaculoPedra> pedrasMarte;

    private static class Projilel {
        public Vector2 pos;
        public Vector2 vel;
        public boolean ativo = true;
        public Projilel(float x, float y, Vector2 dir, float speed) {
            this.pos = new Vector2(x, y);
            this.vel = new Vector2(dir).scl(speed);
        }
    }
    private Array<Projilel> tirosJogador;

    private static class ProjilelInimigo {
        public Vector2 pos;
        public Vector2 vel;
        public boolean ativo = true;
        public ProjilelInimigo(float x, float y, Vector2 dir, float speed) {
            this.pos = new Vector2(x, y);
            this.vel = new Vector2(dir).scl(speed);
        }
    }
    private Array<ProjilelInimigo> tirosInimigos;

    public enum TipoInimigo { RANGED, MELEE }
    private static class Inimigo {
        public Rectangle bounds;
        public boolean vivo = true;
        public float cooldownTiro = 0f;
        public TipoInimigo tipo;
        public Inimigo(float x, float y, TipoInimigo tipo) {
            this.bounds = new Rectangle(x, y, 52, 52);
            this.tipo = tipo;
        }
    }
    private Array<Inimigo> inimigos;
    private boolean combatStarted = false;
    private int currentWave = 0;

    private boolean transicaoVictoryPendente = false;
    private boolean transicaoGameOverPendente = false;

    private final Vector3 mousePos = new Vector3();

    public MarsScreen(Main game, PlayerStatus status) {
        this.game = game;
        this.status = status;
    }

    @Override
    public void show() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(1280, 720, camera);
        camera.position.set(WORLD_WIDTH / 2f, WORLD_HEIGHT / 2f, 0);
        camera.update();

        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        font = new BitmapFont();

        player = new Rectangle(WORLD_WIDTH / 2f - 32, WORLD_HEIGHT / 2f - 32, 64, 64);

        assets = new GameAssets();
        assets.carregar();
        playerRegion = assets.getRegion("player");
        inimigoRegion = assets.getRegion("inimigo");
        armaRegion = assets.getRegion("arma");

        particleManager = new ParticleManager();

        pedrasMarte = new Array<>();
        pedrasMarte.add(new ObstaculoPedra(600, 400, 200, 120));
        pedrasMarte.add(new ObstaculoPedra(1200, 900, 250, 150));
        pedrasMarte.add(new ObstaculoPedra(1800, 500, 180, 200));
        pedrasMarte.add(new ObstaculoPedra(1000, 300, 150, 100));

        inimigos = new Array<>();
        tirosJogador = new Array<>();
        tirosInimigos = new Array<>();

        // Se a arma já foi craftada na Lua, o combate já se inicia direto!
        if (status.armaCraftada) {
            proximaWave();
        }
    }

    @Override
    public void render(float delta) {
        if (transicaoVictoryPendente) {
            game.setScreen(new VictoryScreen(game));
            dispose();
            return;
        }
        if (transicaoGameOverPendente) {
            game.setScreen(new GameOverScreen(game));
            dispose();
            return;
        }

        handleInput(delta);
        updateLogica(delta);
        updateCamera();
        particleManager.update(delta);

        timerTempestade -= delta;
        if (timerTempestade <= 0f) {
            particleManager.spawnPoeira(MathUtils.random(0, WORLD_WIDTH), MathUtils.random(0, WORLD_HEIGHT));
            timerTempestade = 0.15f;
        }

        Gdx.gl.glClearColor(0.42f, 0.16f, 0.08f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        camera.update();

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        shapeRenderer.setColor(Color.BROWN);
        for (ObstaculoPedra pedra : pedrasMarte) {
            shapeRenderer.rect(pedra.bounds.x, pedra.bounds.y, pedra.bounds.width, pedra.bounds.height);
        }

        if (combatStarted && inimigoRegion == null) {
            for (Inimigo e : inimigos) {
                if (e.vivo) {
                    shapeRenderer.setColor(e.tipo == TipoInimigo.RANGED ? Color.SCARLET : Color.MAROON);
                    shapeRenderer.rect(e.bounds.x, e.bounds.y, e.bounds.width, e.bounds.height);
                }
            }
        }

        shapeRenderer.setColor(Color.CYAN);
        for (Projilel t : tirosJogador) {
            shapeRenderer.circle(t.pos.x, t.pos.y, 6);
        }

        shapeRenderer.setColor(Color.YELLOW);
        for (ProjilelInimigo ti : tirosInimigos) {
            shapeRenderer.circle(ti.pos.x, ti.pos.y, 5);
        }

        if (playerRegion == null) {
            shapeRenderer.setColor(Color.WHITE);
            shapeRenderer.rect(player.x, player.y, player.width, player.height);
        }

        shapeRenderer.end();

        particleManager.render(shapeRenderer, camera);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        if (playerRegion != null) {
            batch.draw(playerRegion, player.x, player.y, player.width, player.height);
        }

        if (combatStarted && inimigoRegion != null) {
            for (Inimigo e : inimigos) {
                if (e.vivo) batch.draw(inimigoRegion, e.bounds.x, e.bounds.y, e.bounds.width, e.bounds.height);
            }
        }

        calculaPosicaoMouse();
        float centroPlayerX = player.x + player.width / 2f;
        float centroPlayerY = player.y + player.height / 2f;
        float anguloMouse = MathUtils.atan2(mousePos.y - centroPlayerY, mousePos.x - centroPlayerX) * MathUtils.radiansToDegrees;

        if (status.armaCraftada) {
            if (armaRegion != null) {
                batch.draw(armaRegion, centroPlayerX, centroPlayerY - 8, 0, 8, 32, 16, 1, 1, anguloMouse);
            } else {
                batch.end();
                shapeRenderer.setProjectionMatrix(camera.combined);
                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                shapeRenderer.setColor(Color.DARK_GRAY);
                shapeRenderer.rectLine(centroPlayerX, centroPlayerY, centroPlayerX + MathUtils.cosDeg(anguloMouse)*25, centroPlayerY + MathUtils.sinDeg(anguloMouse)*25, 6);
                shapeRenderer.end();
                batch.begin();
            }
        }

        batch.end();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        font.setColor(Color.WHITE);
        font.draw(batch, "MARTE - MISSAO ARTEMIS (WAVE " + currentWave + "/3)", camera.position.x - 220, camera.position.y + 330);

        font.setColor(Color.RED);
        font.draw(batch, "VIDA: " + playerVida + " | SOBREVIVA AOS INIMIGOS!", camera.position.x - 220, camera.position.y + 300);

        batch.end();
    }

    private void handleInput(float delta) {
        float dx = 0, dy = 0;
        if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP)) dy += 1;
        if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN)) dy -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) dx -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) dx += 1;

        player.x += dx * playerSpeed * delta;
        for (ObstaculoPedra pedra : pedrasMarte) {
            if (player.overlaps(pedra.bounds)) {
                player.x -= dx * playerSpeed * delta;
                break;
            }
        }

        player.y += dy * playerSpeed * delta;
        for (ObstaculoPedra pedra : pedrasMarte) {
            if (player.overlaps(pedra.bounds)) {
                player.y -= dy * playerSpeed * delta;
                break;
            }
        }

        player.x = Math.max(0, Math.min(player.x, WORLD_WIDTH - player.width));
        player.y = Math.max(0, Math.min(player.y, WORLD_HEIGHT - player.height));

        if (status.armaCraftada && Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            calculaPosicaoMouse();
            float centroX = player.x + player.width / 2f;
            float centroY = player.y + player.height / 2f;

            Vector2 direcao = new Vector2(mousePos.x - centroX, mousePos.y - centroY).nor();
            tirosJogador.add(new Projilel(centroX, centroY, direcao, 600f));
        }
    }

    private void updateLogica(float delta) {
        for (int i = tirosJogador.size - 1; i >= 0; i--) {
            Projilel t = tirosJogador.get(i);
            t.pos.mulAdd(t.vel, delta);

            if (t.pos.x < 0 || t.pos.x > WORLD_WIDTH || t.pos.y < 0 || t.pos.y > WORLD_HEIGHT) {
                tirosJogador.removeIndex(i);
                continue;
            }

            if (combatStarted) {
                for (Inimigo e : inimigos) {
                    if (e.vivo && e.bounds.contains(t.pos.x, t.pos.y)) {
                        e.vivo = false;
                        t.ativo = false;
                        particleManager.spawnColeta(e.bounds.x, e.bounds.y);
                        break;
                    }
                }
            }
            if (!t.ativo) tirosJogador.removeIndex(i);
        }

        if (combatStarted) {
            float centroPlayerX = player.x + player.width / 2f;
            float centroPlayerY = player.y + player.height / 2f;

            for (Inimigo e : inimigos) {
                if (!e.vivo) continue;

                float centroInimigoX = e.bounds.x + e.bounds.width / 2f;
                float centroInimigoY = e.bounds.y + e.bounds.height / 2f;

                Vector2 direcaoPlayer = new Vector2(centroPlayerX - centroInimigoX, centroPlayerY - centroInimigoY).nor();

                float velocidadeInimigo = (e.tipo == TipoInimigo.MELEE) ? 120f : 80f;
                e.bounds.x += direcaoPlayer.x * velocidadeInimigo * delta;
                e.bounds.y += direcaoPlayer.y * velocidadeInimigo * delta;

                if (e.tipo == TipoInimigo.MELEE && player.overlaps(e.bounds)) {
                    playerVida -= 15 * delta;
                    if (playerVida <= 0) {
                        transicaoGameOverPendente = true;
                        return;
                    }
                }

                if (e.tipo == TipoInimigo.RANGED) {
                    e.cooldownTiro -= delta;
                    if (e.cooldownTiro <= 0f) {
                        e.cooldownTiro = 1.2f;
                        tirosInimigos.add(new ProjilelInimigo(centroInimigoX, centroInimigoY, direcaoPlayer, 350f));
                    }
                }
            }

            for (int i = tirosInimigos.size - 1; i >= 0; i--) {
                ProjilelInimigo ti = tirosInimigos.get(i);
                ti.pos.mulAdd(ti.vel, delta);

                if (player.contains(ti.pos.x, ti.pos.y)) {
                    playerVida -= 15;
                    ti.ativo = false;
                    if (playerVida <= 0) {
                        transicaoGameOverPendente = true;
                        return;
                    }
                }

                if (!ti.ativo || ti.pos.x < 0 || ti.pos.x > WORLD_WIDTH || ti.pos.y < 0 || ti.pos.y > WORLD_HEIGHT) {
                    tirosInimigos.removeIndex(i);
                }
            }

            if (todosInimigosMortos()) {
                if (currentWave < 3) {
                    proximaWave();
                } else {
                    transicaoVictoryPendente = true;
                }
            }
        }
    }

    private void proximaWave() {
        currentWave++;
        combatStarted = true;
        inimigos.clear();
        tirosInimigos.clear();

        if (currentWave == 1) {
            inimigos.add(new Inimigo(500, 200, TipoInimigo.RANGED));
            inimigos.add(new Inimigo(2000, 1200, TipoInimigo.RANGED));
            inimigos.add(new Inimigo(1500, 200, TipoInimigo.MELEE));
        } else if (currentWave == 2) {
            inimigos.add(new Inimigo(300, 1000, TipoInimigo.MELEE));
            inimigos.add(new Inimigo(2200, 300, TipoInimigo.MELEE));
            inimigos.add(new Inimigo(1200, 1300, TipoInimigo.RANGED));
            inimigos.add(new Inimigo(800, 200, TipoInimigo.RANGED));
        } else if (currentWave == 3) {
            inimigos.add(new Inimigo(400, 400, TipoInimigo.MELEE));
            inimigos.add(new Inimigo(2000, 1000, TipoInimigo.MELEE));
            inimigos.add(new Inimigo(1500, 1000, TipoInimigo.RANGED));
            inimigos.add(new Inimigo(1000, 600, TipoInimigo.RANGED));
            inimigos.add(new Inimigo(2200, 1200, TipoInimigo.MELEE));
        }
    }

    private void calculaPosicaoMouse() {
        mousePos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(mousePos, viewport.getScreenX(), viewport.getScreenY(), viewport.getScreenWidth(), viewport.getScreenHeight());
    }

    private boolean todosInimigosMortos() {
        if (!combatStarted) return false;
        for (Inimigo e : inimigos) {
            if (e.vivo) return false;
        }
        return true;
    }

    private void updateCamera() {
        camera.position.set(player.x + player.width / 2f, player.y + player.height / 2f, 0);
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
        particleManager.clear();
    }
}
