package com.Echoes.Jogo.Screen;

import com.Echoes.Jogo.Entities.Inimigo;
import com.Echoes.Jogo.Entities.PlayerStatus;
import com.Echoes.Jogo.Entities.Projectile;
import com.Echoes.Jogo.Main;
import com.Echoes.Jogo.Managers.GameAssets;
import com.Echoes.Jogo.Managers.MissionState;
import com.Echoes.Jogo.Managers.ParticleManager;
import com.Echoes.Jogo.Managers.SaveManager;
import com.Echoes.Jogo.Ui.DialogueSystem;
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
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.ArrayList;
import java.util.List;

public class TitanScreen implements Screen {

    public static final float WORLD_WIDTH = 1920f;
    public static final float WORLD_HEIGHT = 1080f;

    // Cor exclusiva do player em Tita, pra diferenciar visualmente da Lua/Marte
    // (item 12 do checklist: "TitanScreen: cor diferente e personagem anda").
    private static final Color COR_PLAYER_TITA = Color.ORANGE;

    private final Main game;
    private final PlayerStatus status;

    private OrthographicCamera camera;
    private OrthographicCamera hudCamera;
    private Viewport viewport;

    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;
    private final GlyphLayout layoutPausa = new GlyphLayout();

    private GameAssets assets;
    private TextureAtlas.AtlasRegion regPlayer;

    private Rectangle player;
    private float playerSpeed = 320f; // Aumentado (era 230f)

    private Rectangle boss;
    private float bossHp = 500f;
    private float bossMaxHp = 500f;
    private boolean bossAtivo = true;
    private float bossSpeed = 120f;
    private int bossDirecaoX = 1;

    // Ataque radial do boss: a cada X segundos, atira em varias direcoes ao mesmo tempo.
    private List<Projectile> projeteisBoss;
    private float bossTiroTimer = 0f;
    private static final float BOSS_TIRO_INTERVALO = 3f;
    private static final int BOSS_NUM_PROJETEIS = 12;

    // MELHORIA (item 11): inimigos extras em Tita além do boss — guardioes
    // menores que perseguem o jogador enquanto o Guardiao do Nucleo ainda vive.
    private List<Inimigo> guardioes;

    private DialogueSystem dialogueSystem;
    private boolean dialogoInicialFeito = false;

    private List<Projectile> projeteis;
    private ParticleManager particleManager;

    private Hud hud;
    private MissionState missao;
    private String textoMissao = "Atencao: Sinal desconhecido detectado em Tita!";

    // Regeneracao periodica de municao, pra nao travar a luta contra o boss.
    private float regenMunicaoTimer = 0f;
    private static final float REGEN_MUNICAO_INTERVALO = 4f;
    private static final int REGEN_MUNICAO_QTD = 3;

    private boolean pausado = false;
    private boolean trocandoTela = false;

    public TitanScreen(Main game) {
        this.game = game;
        this.status = new PlayerStatus();
        this.status.faseAtual = "TITA";
    }

    public TitanScreen(Main game, PlayerStatus status) {
        this.game = game;
        this.status = (status != null) ? status : new PlayerStatus();
        this.status.faseAtual = "TITA";
    }

    @Override
    public void show() {
        // Garante que nenhum Stage de outra tela (ex: menu) continue interceptando
        // cliques aqui — era isso que causava a fase reiniciar sozinha ao clicar.
        Gdx.input.setInputProcessor(null);

        camera = new OrthographicCamera();
        viewport = new FitViewport(1280, 720, camera);
        camera.position.set(WORLD_WIDTH / 2f, WORLD_HEIGHT / 2f, 0);

        hudCamera = new OrthographicCamera();
        hudCamera.setToOrtho(false, 1280, 720);

        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        font = new BitmapFont();

        assets = new GameAssets();
        assets.carregar();
        regPlayer = assets.getRegion("player");

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        player = new Rectangle(100, WORLD_HEIGHT / 2f, 64, 64);
        boss = new Rectangle(1400, WORLD_HEIGHT / 2f - 80, 160, 160);

        projeteis = new ArrayList<>();
        projeteisBoss = new ArrayList<>();
        particleManager = new ParticleManager();
        hud = new Hud();
        missao = new MissionState();

        guardioes = new ArrayList<>();
        spawnGuardioes();

        String[] falasTita = new String[]{
            "SISTEMA: Voce pousou na superficie gelada de Tita.",
            "ALERTA: O Guardiao do Nucleo detectou sua presenca!",
            "OBJETIVO: Destrua o Boss para garantir a sobrevivencia da colonia."
        };
        dialogueSystem = new DialogueSystem(falasTita);

        SaveManager.salvarJogo(status, missao);
    }

    /** Cria alguns guardioes menores espalhados pelo mapa (inimigo extra do checklist). */
    private void spawnGuardioes() {
        Inimigo.TipoInimigo[] tipos = {
            Inimigo.TipoInimigo.NORMAL,
            Inimigo.TipoInimigo.RAPIDO,
            Inimigo.TipoInimigo.NORMAL
        };

        for (Inimigo.TipoInimigo tipo : tipos) {
            float gx = MathUtils.random(600f, WORLD_WIDTH - 200f);
            float gy = MathUtils.random(150f, WORLD_HEIGHT - 150f);
            guardioes.add(new Inimigo(gx, gy, tipo));
        }
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            pausado = !pausado;
        }

        if (pausado) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
                SaveManager.salvarJogo(status, missao);
                game.setScreen(new MenuScreen(game));
                dispose();
                return;
            }
        } else if (!status.missaoFalhou) {
            if (dialogueSystem.ativo) {
                dialogueSystem.update();
                if (!dialogueSystem.ativo && !dialogoInicialFeito) {
                    dialogoInicialFeito = true;
                    textoMissao = "DESTRUA O BOSS DE TITA!";
                }
            } else {
                handleInput(delta);
                updateGuardioes(delta);
                updateProjeteis(delta);
                updateProjeteisBoss(delta);
                updateBoss(delta);
                updateBossAtaque(delta);
                checkColisoes(delta);
                updateCamera();
                particleManager.update(delta);
                status.atualizarCombate(delta);
                updateRegenMunicao(delta);
            }
        }

        if (status.missaoFalhou) {
            game.setScreen(new GameOverScreen(game));
            dispose();
            return;
        }

        // A troca de tela foi agendada (boss derrotado) — para aqui
        if (trocandoTela) {
            dispose();
            return;
        }

        Gdx.gl.glClearColor(0.05f, 0.12f, 0.20f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        desenharMundo();
        hud.render(shapeRenderer, batch, font, hudCamera, status, textoMissao, "AMPLITUDE: Batalha Final", 720);

        if (dialogueSystem.ativo) {
            dialogueSystem.render(shapeRenderer, batch, font, 1280, 720);
        }

        if (pausado) {
            desenharPausa();
        }
    }

    private void handleInput(float delta) {
        float dx = 0, dy = 0;

        if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP)) dy += 1;
        if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN)) dy -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) dx -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) dx += 1;

        player.x += dx * playerSpeed * delta;
        player.y += dy * playerSpeed * delta;

        player.x = MathUtils.clamp(player.x, 0, WORLD_WIDTH - player.width);
        player.y = MathUtils.clamp(player.y, 0, WORLD_HEIGHT - player.height);

        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            if (status.podeAtirar()) {
                Vector2 mouseWorld = viewport.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
                float startX = player.x + player.width / 2f;
                float startY = player.y + player.height / 2f;
                projeteis.add(new Projectile(startX, startY, mouseWorld.x, mouseWorld.y, 600f, 500f));
            }
        }
    }

    private void updateBoss(float delta) {
        if (!bossAtivo) return;

        boss.y += bossSpeed * bossDirecaoX * delta;
        if (boss.y <= 100 || boss.y >= WORLD_HEIGHT - 260) {
            bossDirecaoX *= -1;
        }

        if (boss.x > player.x + 300) {
            boss.x -= 40f * delta;
        } else if (boss.x < player.x + 100) {
            boss.x += 40f * delta;
        }
    }

    /** A cada BOSS_TIRO_INTERVALO segundos, o boss dispara em varias direcoes ao mesmo tempo. */
    private void updateBossAtaque(float delta) {
        if (!bossAtivo) return;

        bossTiroTimer -= delta;
        if (bossTiroTimer <= 0f) {
            bossTiroTimer = BOSS_TIRO_INTERVALO;
            disparoRadialBoss();
        }
    }

    /** Cria um "leque" de projeteis saindo do boss em todas as direcoes (360 graus). */
    private void disparoRadialBoss() {
        float centroX = boss.x + boss.width / 2f;
        float centroY = boss.y + boss.height / 2f;

        for (int i = 0; i < BOSS_NUM_PROJETEIS; i++) {
            float angulo = (360f / BOSS_NUM_PROJETEIS) * i;
            float rad = angulo * MathUtils.degreesToRadians;
            float targetX = centroX + MathUtils.cos(rad) * 100f;
            float targetY = centroY + MathUtils.sin(rad) * 100f;
            projeteisBoss.add(new Projectile(centroX, centroY, targetX, targetY, 260f, 900f));
        }
    }

    private void updateProjeteisBoss(float delta) {
        for (int i = projeteisBoss.size() - 1; i >= 0; i--) {
            Projectile p = projeteisBoss.get(i);
            p.update(delta);

            if (!p.ativo) {
                projeteisBoss.remove(i);
                continue;
            }

            if (player.contains(p.x, p.y)) {
                p.ativo = false;
                status.hp -= 10f;
                if (status.hp <= 0) {
                    status.hp = 0;
                    status.missaoFalhou = true;
                }
                projeteisBoss.remove(i);
            }
        }
    }

    /** Move os guardiões (inimigo extra) em direção ao jogador. */
    private void updateGuardioes(float delta) {
        for (Inimigo g : guardioes) {
            if (!g.ativo) continue;
            // NORMAL/RAPIDO nao atiram, entao a lista de projeteis inimigos nunca e usada aqui.
            g.update(delta, player, new ArrayList<Projectile>());
        }
    }

    private void updateProjeteis(float delta) {
        for (int i = projeteis.size() - 1; i >= 0; i--) {
            Projectile p = projeteis.get(i);
            p.update(delta);

            if (!p.ativo) {
                projeteis.remove(i);
                continue;
            }

            // Acerto no boss
            if (bossAtivo && boss.contains(p.x, p.y)) {
                p.ativo = false;
                bossHp -= 25f;
                particleManager.spawnColeta(p.x, p.y);

                if (bossHp <= 0) {
                    bossHp = 0;
                    bossAtivo = false;
                    game.setScreen(new VictoryScreen(game));
                    trocandoTela = true;
                }
                projeteis.remove(i);
                continue;
            }

            // Acerto em algum guardiao
            for (Inimigo g : guardioes) {
                if (g.ativo && g.bounds.contains(p.x, p.y)) {
                    p.ativo = false;
                    g.tomarDano(50f);
                    particleManager.spawnColeta(p.x, p.y);
                    if (!g.ativo) {
                        status.municao += 3; // recompensa por derrotar o guardiao
                    }
                    projeteis.remove(i);
                    break;
                }
            }
        }
    }

    private void checkColisoes(float delta) {
        if (bossAtivo && player.overlaps(boss)) {
            status.hp -= 40f * delta;
            if (status.hp <= 0) {
                status.hp = 0;
                status.missaoFalhou = true;
            }
        }

        for (Inimigo g : guardioes) {
            if (g.ativo && player.overlaps(g.bounds)) {
                status.hp -= 15f * delta;
                if (status.hp <= 0) {
                    status.hp = 0;
                    status.missaoFalhou = true;
                }
            }
        }
    }

    /** Regenera um pouco de municao periodicamente, pra a luta contra o boss nao travar por falta de tiro. */
    private void updateRegenMunicao(float delta) {
        regenMunicaoTimer -= delta;
        if (regenMunicaoTimer <= 0f) {
            regenMunicaoTimer = REGEN_MUNICAO_INTERVALO;
            status.municao += REGEN_MUNICAO_QTD;
        }
    }

    private void updateCamera() {
        camera.position.set(player.x + player.width / 2f, player.y + player.height / 2f, 0);
    }

    private void desenharMundo() {
        viewport.apply();
        camera.update();

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        if (bossAtivo) {
            shapeRenderer.setColor(Color.PURPLE);
            shapeRenderer.rect(boss.x, boss.y, boss.width, boss.height);

            shapeRenderer.setColor(Color.RED);
            shapeRenderer.rect(boss.x, boss.y + boss.height + 15, boss.width, 12);
            shapeRenderer.setColor(Color.GREEN);
            shapeRenderer.rect(boss.x, boss.y + boss.height + 15, boss.width * (bossHp / bossMaxHp), 12);
        }

        for (Inimigo g : guardioes) {
            if (!g.ativo) continue;
            shapeRenderer.setColor(g.getCor());
            shapeRenderer.rect(g.bounds.x, g.bounds.y, g.bounds.width, g.bounds.height);
        }

        for (Projectile p : projeteis) {
            p.render(shapeRenderer);
        }

        if (!projeteisBoss.isEmpty()) {
            shapeRenderer.setColor(Color.RED);
            for (Projectile p : projeteisBoss) {
                if (p.ativo) shapeRenderer.circle(p.x, p.y, 6);
            }
        }

        if (regPlayer == null) {
            shapeRenderer.setColor(COR_PLAYER_TITA);
            shapeRenderer.rect(player.x, player.y, player.width, player.height);
        }

        shapeRenderer.end();

        particleManager.render(shapeRenderer, camera);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        if (regPlayer != null) {
            // Tinta a sprite de laranja pra diferenciar o player de Tita mesmo usando a mesma textura da Lua/Marte
            batch.setColor(COR_PLAYER_TITA);
            batch.draw(regPlayer, player.x, player.y, player.width, player.height);
            batch.setColor(Color.WHITE);
        }

        font.setColor(Color.WHITE);
        if (bossAtivo) {
            font.draw(batch, "GUARDIAO DE TITA", boss.x + 15, boss.y + boss.height + 45);
        }
        batch.end();
    }

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
        layoutPausa.setText(font, "ESC para continuar | M para salvar e ir ao menu");
        font.draw(batch, layoutPausa, 640 - layoutPausa.width / 2f, 350);
        font.getData().setScale(1f);
        batch.end();
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
        if (assets != null) assets.dispose();
    }
}
