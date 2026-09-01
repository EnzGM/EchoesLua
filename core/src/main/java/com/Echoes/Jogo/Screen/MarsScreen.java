package com.Echoes.Jogo.Screen;

import com.Echoes.Jogo.Entities.Item;
import com.Echoes.Jogo.Entities.ItemType;
import com.Echoes.Jogo.Entities.PlayerStatus;
import com.Echoes.Jogo.Entities.Portal;
import com.Echoes.Jogo.Main;
import com.Echoes.Jogo.Managers.GameAssets;
import com.Echoes.Jogo.Managers.MissionState;
import com.Echoes.Jogo.Managers.ParticleManager;
import com.Echoes.Jogo.Managers.SaveManager;
import com.Echoes.Jogo.Ui.Hud;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
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
    private OrthographicCamera hudCamera;
    private Viewport viewport;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;
    private Hud hud;
    private MissionState missao;

    private Rectangle player;
    private float playerSpeed = 220f;

    private GameAssets assets;
    private TextureAtlas.AtlasRegion playerRegion;
    private TextureAtlas.AtlasRegion inimigoRegion;
    private TextureAtlas.AtlasRegion armaRegion;

    private ParticleManager particleManager;
    private float timerTempestade = 0f;

    // Tecla de pausa/menu (igual à LunarScreen): ESC pausa, M salva e volta ao menu principal
    private boolean pausado = false;
    private final GlyphLayout layoutPausa = new GlyphLayout();

    // MELHORIA 6: portal de volta pra Lua (bidirecional)
    private Portal portalRetorno;
    private boolean voltarParaLua = false;

    // MELHORIA 3: itens dropados pelos inimigos (municao ou kit de vida)
    private Array<Item> itensChao;

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

        hudCamera = new OrthographicCamera();
        hudCamera.setToOrtho(false, 1280, 720);

        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        font = new BitmapFont();
        hud = new Hud();

        missao = new MissionState();
        missao.setEtapa(status.missaoEtapa);

        // MELHORIA 6: player nasce no checkpoint salvo (ou no centro, na 1a vez)
        player = new Rectangle(status.lastMarteX, status.lastMarteY, 64, 64);

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
        itensChao = new Array<>();

        portalRetorno = new Portal(2400, 1300);
        portalRetorno.ativo = true; // sempre disponivel pra voltar pra Lua

        // MELHORIA 6: retoma a wave onde parou, em vez de sempre reiniciar do zero
        if (status.armaCraftada) {
            if (status.marteWaveAtual > 0) {
                currentWave = status.marteWaveAtual - 1;
            }
            proximaWave();
        }
    }

    @Override
    public void render(float delta) {
        if (transicaoVictoryPendente) {
            SaveManager.apagarSave(); // campanha concluida, encerra o save
            game.setScreen(new VictoryScreen(game));
            dispose();
            return;
        }
        if (transicaoGameOverPendente) {
            game.setScreen(new GameOverScreen(game));
            dispose();
            return;
        }
        if (voltarParaLua) {
            game.setScreen(new LunarScreen(game, status));
            dispose();
            return;
        }

        // Tecla de pausa/menu: ESC alterna entre pausado/rodando (igual à Lua)
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            pausado = !pausado;
        }

        if (pausado) {
            // Enquanto pausado, o jogo congela. M salva e volta pro menu principal.
            if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
                SaveManager.salvarJogo(status, missao);
                game.setScreen(new MenuScreen(game));
                dispose();
                return;
            }
        } else {
            handleInput(delta);
            updateLogica(delta);
            updateCamera();
            particleManager.update(delta);

            // MELHORIA 1: Quest Tracker
            missao.setEtapa(MissionState.calcularEtapa(status));
            status.missaoEtapa = missao.getEtapa();

            timerTempestade -= delta;
            if (timerTempestade <= 0f) {
                particleManager.spawnPoeira(MathUtils.random(0, WORLD_WIDTH), MathUtils.random(0, WORLD_HEIGHT));
                timerTempestade = 0.15f;
            }
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

        shapeRenderer.setColor(portalRetorno.ativo ? Color.MAGENTA : Color.DARK_GRAY);
        shapeRenderer.rect(portalRetorno.bounds.x, portalRetorno.bounds.y, portalRetorno.bounds.width, portalRetorno.bounds.height);

        for (Item item : itensChao) {
            if (item.coletado) continue;
            shapeRenderer.setColor(item.type == ItemType.MUNICAO ? Color.GOLD : Color.CYAN);
            shapeRenderer.rect(item.bounds.x, item.bounds.y, item.bounds.width, item.bounds.height);
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
        font.getData().setScale(1.1f);

        font.setColor(portalRetorno.ativo ? Color.MAGENTA : Color.GRAY);
        font.draw(batch, "Portal para a Lua (E)", portalRetorno.bounds.x - 30, portalRetorno.bounds.y + portalRetorno.bounds.height + 20);

        font.setColor(Color.WHITE);
        for (Item item : itensChao) {
            if (item.coletado) continue;
            String texto = item.type == ItemType.MUNICAO ? "Municao" : "Kit de Vida";
            font.draw(batch, texto, item.bounds.x - 5, item.bounds.y + item.bounds.height + 15);
        }

        font.getData().setScale(1f);
        batch.end();

        hud.render(shapeRenderer, batch, font, hudCamera, status, missao.getAtual(),
            "MARTE - WAVE " + currentWave + "/3", 720);

        if (pausado) {
            desenharPausa();
        }
    }

    /** Botão de pausa: overlay escuro com o texto e as opções de ESC/M (igual à LunarScreen). */
    private void desenharPausa() {
        hudCamera.update();

        shapeRenderer.setProjectionMatrix(hudCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0f, 0f, 0f, 0.6f);
        shapeRenderer.rect(0, 0, 1280, 720);
        shapeRenderer.end();

        batch.setProjectionMatrix(hudCamera.combined);
        batch.begin();

        font.setColor(Color.WHITE);
        font.getData().setScale(2.4f);
        layoutPausa.setText(font, "PAUSADO");
        font.draw(batch, layoutPausa, 640 - layoutPausa.width / 2f, 420);

        font.getData().setScale(1.2f);
        layoutPausa.setText(font, "ESC para continuar  |  M para salvar e voltar ao menu");
        font.draw(batch, layoutPausa, 640 - layoutPausa.width / 2f, 350);

        font.getData().setScale(1f);
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

        // Cooldown da arma precisa "andar" mesmo em Marte, onde o oxigenio nao eh consumido
        status.atualizarCombate(delta);

        if (status.armaCraftada && Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            if (status.podeAtirar()) {
                calculaPosicaoMouse();
                float centroX = player.x + player.width / 2f;
                float centroY = player.y + player.height / 2f;

                Vector2 direcao = new Vector2(mousePos.x - centroX, mousePos.y - centroY).nor();
                tirosJogador.add(new Projilel(centroX, centroY, direcao, 600f));
            }
        }

        // MELHORIA 6: portal bidirecional de volta pra Lua, com checkpoint + save
        if (Gdx.input.isKeyJustPressed(Input.Keys.E) && portalRetorno.ativo && player.overlaps(portalRetorno.bounds)) {
            status.lastMarteX = player.x;
            status.lastMarteY = player.y;
            status.faseAtual = "LUA";
            SaveManager.salvarJogo(status, missao);
            voltarParaLua = true;
        }

        // MELHORIA 4: salvar manualmente a qualquer momento
        if (Gdx.input.isKeyJustPressed(Input.Keys.F5)) {
            SaveManager.salvarJogo(status, missao);
        }
    }

    private void updateLogica(float delta) {
        checkItensChao();

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
                        status.inimigosDerrotados++;

                        // MELHORIA 3: drop ao morrer
                        ItemType tipoDrop = MathUtils.randomBoolean(0.6f) ? ItemType.MUNICAO : ItemType.OXIGENIO;
                        itensChao.add(new Item(e.bounds.x, e.bounds.y, tipoDrop));
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
                    status.hp -= 15f * delta;
                    if (status.hp <= 0f) {
                        status.hp = 0f;
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
                    status.hp -= 15f;
                    ti.ativo = false;
                    if (status.hp <= 0f) {
                        status.hp = 0f;
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

    private void checkItensChao() {
        for (Item item : itensChao) {
            if (item.coletado) continue;
            if (player.overlaps(item.bounds)) {
                item.coletado = true;
                particleManager.spawnColeta(item.bounds.x, item.bounds.y);

                if (item.type == ItemType.MUNICAO) {
                    status.municao += 5;
                } else if (item.type == ItemType.OXIGENIO) {
                    status.hp = Math.min(100f, status.hp + 25f);
                }
            }
        }
    }

    private void proximaWave() {
        currentWave++;
        status.marteWaveAtual = currentWave; // MELHORIA 6: checkpoint da wave
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
