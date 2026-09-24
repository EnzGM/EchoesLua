package com.Echoes.Jogo.Screen;

import com.Echoes.Jogo.Entities.BossCalisto;
import com.Echoes.Jogo.Entities.Drone;
import com.Echoes.Jogo.Entities.Inimigo;
import com.Echoes.Jogo.Entities.PlayerStatus;
import com.Echoes.Jogo.Entities.Portal;
import com.Echoes.Jogo.Entities.Projectile;
import com.Echoes.Jogo.Main;
import com.Echoes.Jogo.Managers.GameAssets;
import com.Echoes.Jogo.Managers.MissionState;
import com.Echoes.Jogo.Managers.ParticleManager;
import com.Echoes.Jogo.Managers.SaveManager;
import com.Echoes.Jogo.Ui.DialogueSystem;
import com.Echoes.Jogo.Ui.Hud;
import com.Echoes.Jogo.Ui.LojaUI;
import com.Echoes.Jogo.Ui.InventoryUI;
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

/** ITEM 18/19: planeta Calisto — gelo-azul/navy, boss de 3 formas, portal dourado pra Aharin. */
public class CallistoScreen implements Screen {

    public static final float WORLD_WIDTH = 1920f;
    public static final float WORLD_HEIGHT = 1080f;

    private static final Color COR_PLAYER_CALISTO = new Color(0.80f, 0.93f, 1f, 1f);

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
    private float playerSpeed = 320f;

    private BossCalisto bossCalisto;
    private int formaAnterior = 1;

    // ITEM 18 (reforçado): tiros do boss, um padrão diferente por forma.
    private List<Projectile> projeteisBoss;

    // ITEM 19: portal dourado pra Aharin. So consulta inventario.tem("CHAVE_LUZ").
    private Portal portalAharin;
    private boolean portalAberto = false;

    private DialogueSystem dialogueSystem;

    private List<Projectile> projeteis;
    private Drone drone;
    private List<Projectile> projeteisDrone;
    private ParticleManager particleManager;

    private Hud hud;
    private LojaUI lojaUI;
    private InventoryUI inventoryUI;
    private MissionState missao;
    private String textoMissao = "Calisto: o gelo aqui esconde algo que muda de forma.";

    private float regenMunicaoTimer = 0f;
    private static final float REGEN_MUNICAO_INTERVALO = 4f;
    private static final int REGEN_MUNICAO_QTD = 3;

    private boolean pausado = false;
    private boolean trocandoTela = false;
    private Rectangle checkpoint;

    public CallistoScreen(Main game) {
        this.game = game;
        this.status = new PlayerStatus();
        this.status.faseAtual = "CALISTO";
    }

    public CallistoScreen(Main game, PlayerStatus status) {
        this.game = game;
        this.status = (status != null) ? status : new PlayerStatus();
        this.status.faseAtual = "CALISTO";
    }

    @Override
    public void show() {
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

        checkpoint = new Rectangle(300f, 300f, 70f, 70f);
        if (status.temCheckpoint && "CALISTO".equals(status.checkpointFase)) {
            player.setPosition(status.checkpointX, status.checkpointY);
        }

        projeteis = new ArrayList<>();
        projeteisBoss = new ArrayList<>();
        projeteisDrone = new ArrayList<>();
        drone = (status.droneAtivo && status.inventario.tem("DRONE"))
            ? new Drone(player.x - 55f, player.y - 35f, status.nivelUpgradeDrone) : null;
        particleManager = new ParticleManager();
        hud = new Hud();
        inventoryUI = new InventoryUI();
        lojaUI = new LojaUI();
        missao = new MissionState();

        if (!status.inventario.tem("CHAVE_LUZ")) {
            float bx = WORLD_WIDTH - 340f;
            float by = WORLD_HEIGHT / 2f - 75f;
            bossCalisto = new BossCalisto(bx, by);
            formaAnterior = bossCalisto.forma;
        } else {
            bossCalisto = null;
        }

        portalAharin = new Portal(WORLD_WIDTH - 220f, WORLD_HEIGHT / 2f - 45f, 90, 90);
        portalAberto = status.inventario.tem("CHAVE_LUZ");
        portalAharin.ativo = portalAberto;

        String[] falasCalisto = new String[]{
            "SISTEMA: Voce chegou a Calisto. O ar aqui e denso e silencioso.",
            "ALERTA: Algo sob o gelo esta se movendo... e nao parece morrer facil.",
            "OBJETIVO: Derrote a entidade em todas as suas formas."
        };
        dialogueSystem = new DialogueSystem(falasCalisto);

        atualizarTextoMissao();
        SaveManager.salvarJogo(status, missao);
    }

    @Override
    public void render(float delta) {
        com.Echoes.Jogo.Entities.Inimigo.MULTIPLICADOR_VELOCIDADE_INIMIGO = status.dificuldade.multiplicadorVelocidadeInimigo;
        inventoryUI.update(status);
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
            } else if (lojaUI.isAberta()) {
                lojaUI.update(delta, status);
            } else {
                handleInput(delta);
                updateBoss(delta);
                updateProjeteisBoss(delta);
                updateProjeteis(delta);
                updateDrone(delta);
                checkColisoes(delta);
                if (status.dropMorte != null && player.overlaps(status.dropMorte.bounds)) {
                    status.recolherDrop();
                }
                checkPortalAharin();
                atualizarTextoMissao();
                updateCamera();
                particleManager.update(delta);
                status.atualizarCombate(delta);
                updateRegenMunicao(delta);
            }
        }

        if (status.missaoFalhou) {
            if (status.temCheckpoint && "LUA".equals(status.checkpointFase)) {
                status.criarDropMorte(player.x, player.y);
                player.setPosition(status.checkpointX, status.checkpointY);
                status.respawnNoCheckpoint();
                SaveManager.salvarJogo(status, missao);
            } else {
                game.setScreen(new GameOverScreen(game));
                dispose();
                return;
            }
        }

        if (trocandoTela) {
            dispose();
            return;
        }

        Gdx.gl.glClearColor(0.03f, 0.07f, 0.16f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        desenharMundo();

        String extraHud;
        if (bossCalisto != null && bossCalisto.ativo) {
            extraHud = "BOSS CALISTO FORMA " + bossCalisto.forma + "/3 HP "
                + (int) bossCalisto.hp + "/" + (int) bossCalisto.hpMax;
        } else if (status.inventario.tem("CHAVE_LUZ")) {
            extraHud = portalAberto ? "PORTAL AHARIN: ONLINE" : "Chave de Luz conquistada!";
        } else {
            extraHud = "Area segura";
        }
        hud.render(shapeRenderer, batch, font, hudCamera, status, textoMissao, extraHud, 720);
        lojaUI.render(shapeRenderer, batch, font, hudCamera, status);
        inventoryUI.render(shapeRenderer, batch, font, hudCamera, status);

        if (dialogueSystem.ativo) {
            dialogueSystem.render(shapeRenderer, batch, font, 1280, 720);
        }

        if (pausado) {
            desenharPausa();
        }
    }

    private void handleInput(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.B)) { lojaUI.abrir(); return; }
        if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            if (player.overlaps(checkpoint)) {
                status.salvarCheckpoint(checkpoint.x + checkpoint.width / 2f - player.width / 2f, checkpoint.y + checkpoint.height / 2f - player.height / 2f, "CALISTO");
                SaveManager.salvarJogo(status, missao);
            }
        }


        int ataqueSpace = status.atualizarCargaAtaque(delta, Gdx.input.isKeyPressed(Input.Keys.SPACE));
        if (ataqueSpace > 0 && status.podeAtirar()) {
            Vector2 mouseWorld = viewport.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
            float startX = player.x + player.width / 2f;
            float startY = player.y + player.height / 2f;
            float dano = (ataqueSpace == 2 ? 100f : 50f) * (1f + status.nivelUpgradeArma * 0.5f);
            float tamanho = ataqueSpace == 2 ? 11f : 5f;
            projeteis.add(new Projectile(startX, startY, mouseWorld.x, mouseWorld.y, 600f, 500f).comForca(dano, tamanho));
        }

        float dx = 0, dy = 0;

        if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP)) dy += 1;
        if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN)) dy -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) dx -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) dx += 1;

        player.x += dx * playerSpeed * delta;
        player.y += dy * playerSpeed * delta;

        player.x = MathUtils.clamp(player.x, 0, WORLD_WIDTH - player.width);
        player.y = MathUtils.clamp(player.y, 0, WORLD_HEIGHT - player.height);

        if (Gdx.input.isKeyJustPressed(Input.Keys.C) && status.inventario.tem("DRONE")) {
            if (drone == null) {
                drone = new Drone(player.x - 55f, player.y - 35f, status.nivelUpgradeDrone);
                status.droneAtivo = true;
            } else {
                drone = null;
                status.droneAtivo = false;
            }
        }

        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            if (status.podeAtirar()) {
                Vector2 mouseWorld = viewport.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
                float startX = player.x + player.width / 2f;
                float startY = player.y + player.height / 2f;
                projeteis.add(new Projectile(startX, startY, mouseWorld.x, mouseWorld.y, 600f, 500f).comForca(50f * (1f + status.nivelUpgradeArma * 0.5f), 5f + status.nivelUpgradeArma));
            }
        }
    }

    private void updateBoss(float delta) {
        if (bossCalisto == null || !bossCalisto.ativo) return;
        bossCalisto.update(delta, player, projeteisBoss);
    }

    /** ITEM 18 (reforçado): move/expira os tiros do boss e aplica dano no jogador. */
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
                status.sofrerDano(12f);
                status.aplicarEfeitoEspecial(p.efeitoTipo);
                projeteisBoss.remove(i);
            }
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

            if (bossCalisto != null && bossCalisto.ativo && bossCalisto.bounds.contains(p.x, p.y)) {
                p.ativo = false;
                bossCalisto.tomarDano(p.dano);
                particleManager.spawnColeta(p.x, p.y);

                if (bossCalisto.forma != formaAnterior) {
                    formaAnterior = bossCalisto.forma;
                    for (int k = 0; k < 3; k++) {
                        particleManager.spawnColeta(
                            bossCalisto.bounds.x + bossCalisto.bounds.width / 2f,
                            bossCalisto.bounds.y + bossCalisto.bounds.height / 2f
                        );
                    }
                }

                if (bossCalisto.mortoFinal && !status.inventario.tem("CHAVE_LUZ")) {
                    status.inimigosDerrotados++;
                    status.creditos += 10;
                    status.inventario.add(status.materialDrop(3));
                    status.registrarChefeMorto("CALISTO");
                    onBossCalistoDerrotado();
                }

                projeteis.remove(i);
            }
        }
    }

    private void onBossCalistoDerrotado() {
        status.inventario.add("CHAVE_LUZ");
        portalAberto = true;
        portalAharin.ativo = true;
        particleManager.spawnColeta(
            bossCalisto.bounds.x + bossCalisto.bounds.width / 2f,
            bossCalisto.bounds.y + bossCalisto.bounds.height / 2f
        );
        SaveManager.salvarJogo(status, missao);
    }

    private void checkColisoes(float delta) {
        if (bossCalisto == null || !bossCalisto.ativo) return;
        if (bossCalisto.estaPiscando()) return;

        if (player.overlaps(bossCalisto.bounds)) {
            status.sofrerDano(bossCalisto.danoContato * delta);
        }
    }

    /**
     * ITEM 19: o portal dourado so consulta inventario.tem("CHAVE_LUZ").
     * Sem a chave o jogador simplesmente esbarra nele (nada acontece, o texto
     * BLOQUEADO ja avisa). Com a chave, entra em AharinScreen.
     */
    private void checkPortalAharin() {
        if (portalAberto && !trocandoTela && player.overlaps(portalAharin.bounds)) {
            status.curarAoTrocarFase();
            status.faseAtual = "AHARIN";
            SaveManager.salvarJogo(status, missao);
            game.setScreen(new CutsceneScreen(game, status, CutsceneScreen.Destino.AHARIN));
            trocandoTela = true;
        }
    }

    private void atualizarTextoMissao() {
        if (bossCalisto != null && bossCalisto.ativo) {
            textoMissao = "ENTIDADE DE CALISTO desperta! Forma " + bossCalisto.forma + " de 3.";
        } else if (status.inventario.tem("CHAVE_LUZ")) {
            textoMissao = "Chave de Luz conquistada! O portal para Aharin esta ONLINE.";
        } else {
            textoMissao = "Encontre e derrote a entidade escondida no gelo.";
        }
    }

    private void updateRegenMunicao(float delta) {
        regenMunicaoTimer -= delta;
        if (regenMunicaoTimer <= 0f) {
            regenMunicaoTimer = REGEN_MUNICAO_INTERVALO;
            status.municao += REGEN_MUNICAO_QTD;
        }
    }

    private void updateDrone(float delta) {
        if (drone == null) return;
        List<Inimigo> alvos = new ArrayList<>();
        if (bossCalisto != null && bossCalisto.ativo) alvos.add(bossCalisto);
        drone.update(delta, player, alvos, projeteisDrone);
        for (int i = projeteisDrone.size() - 1; i >= 0; i--) {
            Projectile p = projeteisDrone.get(i);
            p.update(delta);
            if (!p.ativo) { projeteisDrone.remove(i); continue; }
            if (bossCalisto != null && bossCalisto.ativo && bossCalisto.bounds.contains(p.x, p.y)) {
                p.ativo = false;
                bossCalisto.tomarDano(35f);
                projeteisDrone.remove(i);
            }
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

        if (bossCalisto != null && bossCalisto.ativo) {
            boolean piscando = bossCalisto.estaPiscando();
            float alpha = 1f;
            if (piscando) {
                alpha = ((int) (bossCalisto.transicaoTimer * 10) % 2 == 0) ? 1f : 0.35f;
            }
            Color cor = bossCalisto.getCor();
            shapeRenderer.setColor(cor.r, cor.g, cor.b, alpha);
            shapeRenderer.rect(bossCalisto.bounds.x, bossCalisto.bounds.y, bossCalisto.bounds.width, bossCalisto.bounds.height);

            float barraLargura = bossCalisto.bounds.width;
            shapeRenderer.setColor(Color.RED);
            shapeRenderer.rect(bossCalisto.bounds.x, bossCalisto.bounds.y + bossCalisto.bounds.height + 12, barraLargura, 10);
            shapeRenderer.setColor(Color.GREEN);
            shapeRenderer.rect(bossCalisto.bounds.x, bossCalisto.bounds.y + bossCalisto.bounds.height + 12,
                barraLargura * (bossCalisto.hp / bossCalisto.hpMax), 10);
        }

        if (drone != null) {
            shapeRenderer.setColor(drone.getCor());
            shapeRenderer.rect(drone.bounds.x, drone.bounds.y, drone.bounds.width, drone.bounds.height);
        }
        for (Projectile p : projeteisDrone) {
            if (p.ativo) {
                shapeRenderer.setColor(Color.CYAN);
                shapeRenderer.circle(p.x, p.y, 4);
            }
        }

        for (Projectile p : projeteis) {
            p.render(shapeRenderer);
        }

        // Tiros do boss - rosa-choque, pra diferenciar dos tiros amarelos do jogador
        for (Projectile p : projeteisBoss) {
            if (p.ativo) {
                shapeRenderer.setColor(1f, 0.15f, 0.55f, 1f);
                shapeRenderer.circle(p.x, p.y, 6);
            }
        }

        shapeRenderer.setColor(portalAberto ? new Color(1f, 0.85f, 0.15f, 1f) : Color.DARK_GRAY);
        shapeRenderer.rect(portalAharin.bounds.x, portalAharin.bounds.y, portalAharin.bounds.width, portalAharin.bounds.height);

        // ITEM 24: estatua/painel visual do checkpoint.
        shapeRenderer.setColor(Color.GOLD);
        shapeRenderer.rect(checkpoint.x, checkpoint.y, checkpoint.width, checkpoint.height);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(checkpoint.x + 10, checkpoint.y + 10, checkpoint.width - 20, checkpoint.height - 20);

        if (regPlayer == null) {
            shapeRenderer.setColor(COR_PLAYER_CALISTO);
            shapeRenderer.rect(player.x, player.y, player.width, player.height);
        }

        // ITEM 28: marcador do cadaver com os recursos perdidos.
        if (status.dropMorte != null) {
            shapeRenderer.setColor(Color.YELLOW);
            shapeRenderer.circle(status.dropMorte.x, status.dropMorte.y, 18f);
            shapeRenderer.setColor(Color.WHITE);
            shapeRenderer.rect(status.dropMorte.x - 4f, status.dropMorte.y - 12f, 8f, 24f);
            shapeRenderer.rect(status.dropMorte.x - 12f, status.dropMorte.y - 4f, 24f, 8f);
        }

        shapeRenderer.end();

        particleManager.render(shapeRenderer, camera);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        if (status.dropMorte != null) {
            font.setColor(Color.YELLOW);
            font.draw(batch, "RECUPERE: " + status.dropMorte.creditos + " C / " + status.dropMorte.municao + " M",
                status.dropMorte.x - 55f, status.dropMorte.y + 35f);
        }

        font.setColor(Color.GOLD);
        font.draw(batch, status.temCheckpoint && "CALISTO".equals(status.checkpointFase) ? "CHECKPOINT SALVO" : "CHECKPOINT [E]", checkpoint.x - 10, checkpoint.y + checkpoint.height + 25);

        if (regPlayer != null) {
            batch.setColor(COR_PLAYER_CALISTO);
            batch.draw(regPlayer, player.x, player.y, player.width, player.height);
            batch.setColor(Color.WHITE);
        }

        if (bossCalisto != null && bossCalisto.ativo) {
            font.setColor(bossCalisto.getCor());
            font.draw(batch, "ENTIDADE DE CALISTO - FORMA " + bossCalisto.forma,
                bossCalisto.bounds.x - 20, bossCalisto.bounds.y + bossCalisto.bounds.height + 45);
        }

        font.setColor(portalAberto ? new Color(1f, 0.85f, 0.15f, 1f) : Color.GRAY);
        String txtPortal = portalAberto
            ? "ONLINE - Portal para Aharin"
            : "BLOQUEADO - A chave de Luz ainda nao existe.";
        font.draw(batch, txtPortal, portalAharin.bounds.x - 70, portalAharin.bounds.y - 20);

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
