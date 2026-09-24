package com.Echoes.Jogo.Screen;

import com.Echoes.Jogo.Entities.BossTita;
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
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
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

    public static final float WORLD_WIDTH = 1400f;
    public static final float WORLD_HEIGHT = 800f;

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
    private float playerSpeed = 320f;

    private List<Rectangle> obstaculos;

    private List<Inimigo> guardioes;
    private BossTita bossTita;

    // ITEM 24: em Tita o drone so ilumina (nao atira) — combina com o pedido
    // "ou apenas iluminar (luz no escuro de Tita)".
    private Drone drone;

    private Portal portalCalisto;
    private boolean portalAberto = false;

    private DialogueSystem dialogueSystem;

    private List<Projectile> projeteis;           // tiros do jogador
    private List<Projectile> projeteisInimigos;    // agora processado de verdade (boss atira)
    private ParticleManager particleManager;

    private Hud hud;
    private LojaUI lojaUI;
    private InventoryUI inventoryUI;
    private MissionState missao;
    private String textoMissao = "Atencao: Sinal desconhecido detectado em Tita!";

    private float regenMunicaoTimer = 0f;
    private static final float REGEN_MUNICAO_INTERVALO = 4f;
    private static final int REGEN_MUNICAO_QTD = 3;

    private boolean pausado = false;
    private boolean trocandoTela = false;
    private Rectangle checkpoint;

    // ITEM 26: puzzle das tres alavancas de Tita. Ordem correta: 2-1-3.
    private final int[] ordemEsperada = {2, 1, 3};
    private final List<Integer> ordemAlavancas = new ArrayList<>();
    private final Rectangle[] alavancas = new Rectangle[3];
    private boolean portaPuzzleAberta = false;
    private String textoPuzzle = "";
    private float textoPuzzleTimer = 0f;
    private Rectangle portaPuzzle;

    // ITEM 27: lanterna manual de Tita, usando a mesma area de luz do drone.
    private boolean lanterna = false;
    private Texture lanternaOverlay;

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

        lanternaOverlay = criarOverlayLanterna();

        player = new Rectangle(100, WORLD_HEIGHT / 2f, 64, 64);

        checkpoint = new Rectangle(360f, 330f, 70f, 70f);
        if (status.temCheckpoint && "TITA".equals(status.checkpointFase)) {
            player.setPosition(status.checkpointX, status.checkpointY);
        }

        obstaculos = new ArrayList<>();
        obstaculos.add(new Rectangle(500, 250, 90, 90));
        obstaculos.add(new Rectangle(900, 480, 90, 90));

        // ITEM 26: tres alavancas visiveis no setor norte de Tita.
        alavancas[0] = new Rectangle(600, 610, 42, 70);
        alavancas[1] = new Rectangle(690, 610, 42, 70);
        alavancas[2] = new Rectangle(780, 610, 42, 70);
        portaPuzzle = new Rectangle(1050, 610, 110, 130);
        portaPuzzleAberta = status.inventario.tem("PORTA_TITA_ABERTA");

        projeteis = new ArrayList<>();
        projeteisInimigos = new ArrayList<>();
        particleManager = new ParticleManager();
        hud = new Hud();
        inventoryUI = new InventoryUI();
        lojaUI = new LojaUI();
        missao = new MissionState();

        guardioes = new ArrayList<>();
        if (!status.titaGuardioesDerrotados) {
            spawnGuardioes();
        }
        bossTita = null;

        // ITEM 24: em Tita o drone reaparece em modo iluminar, se estava chamado
        if (status.droneAtivo && status.inventario.tem("DRONE")) {
            drone = new Drone(player.x - 55f, player.y - 35f, status.nivelUpgradeDrone);
            drone.modoIluminar = lanterna;
        } else {
            drone = null;
        }

        portalCalisto = new Portal(WORLD_WIDTH - 220f, WORLD_HEIGHT / 2f - 45f, 90, 90);
        portalAberto = status.inventario.tem("CHAVE_TITA");
        portalCalisto.ativo = portalAberto;

        String[] falasTita = new String[]{
            "SISTEMA: Voce pousou na superficie gelada de Tita.",
            "ALERTA: Guardioes do Nucleo detectaram sua presenca!",
            "OBJETIVO: Elimine os guardioes para atrair o Guardiao Maior."
        };
        dialogueSystem = new DialogueSystem(falasTita);

        atualizarTextoMissao();
        SaveManager.salvarJogo(status, missao);
    }

    private void spawnGuardioes() {
        Inimigo.TipoInimigo[] tipos = {
            Inimigo.TipoInimigo.NORMAL,
            Inimigo.TipoInimigo.RAPIDO,
            Inimigo.TipoInimigo.NORMAL
        };

        for (Inimigo.TipoInimigo tipo : tipos) {
            float gx = MathUtils.random(500f, WORLD_WIDTH - 150f);
            float gy = MathUtils.random(120f, WORLD_HEIGHT - 120f);
            guardioes.add(new Inimigo(gx, gy, tipo));
        }
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
            if (lojaUI.isAberta()) {
                lojaUI.update(delta, status);
            } else if (dialogueSystem.ativo) {
                dialogueSystem.update();
            } else {
                handleInput(delta);
                if (textoPuzzleTimer > 0f) textoPuzzleTimer -= delta;
                updateGuardioes(delta);
                updateProjeteisInimigos(delta);
                updateProjeteis(delta);
                if (drone != null) drone.update(delta, player, null, null);
                checkBossTita();
                checkColisoes(delta);
                if (status.dropMorte != null && player.overlaps(status.dropMorte.bounds)) {
                    status.recolherDrop();
                }
                checkPortalCalisto();
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

        Gdx.gl.glClearColor(0.05f, 0.12f, 0.20f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        desenharMundo();
        desenharEscuridao();

        String extraHud;
        if (bossTita != null && bossTita.ativo) {
            extraHud = "BOSS TITA: " + (int) bossTita.hp + "/" + (int) BossTita.HP_INICIAL;
        } else if (!status.titaGuardioesDerrotados) {
            int restantes = 0;
            for (Inimigo g : guardioes) if (g.ativo) restantes++;
            extraHud = "GUARDIOES: " + restantes + " restantes";
        } else {
            extraHud = portalAberto ? "PORTAL CALISTO: ONLINE" : "Area segura";
        }

        String dica = calcularDicaDirecao();
        if (dica != null) {
            extraHud += " | " + dica;
        }
        if (!portaPuzzleAberta) {
            extraHud += " | ALAVANCAS " + ordemAlavancas.size() + "/3";
        }
        if (textoPuzzleTimer > 0f) {
            extraHud += " | " + textoPuzzle;
        }
        extraHud += " | LANTERNA: " + (lanterna ? "ON" : "OFF") + " [F]";

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
                status.salvarCheckpoint(checkpoint.x + checkpoint.width / 2f - player.width / 2f, checkpoint.y + checkpoint.height / 2f - player.height / 2f, "TITA");
                SaveManager.salvarJogo(status, missao);
                textoPuzzle = "CHECKPOINT SALVO";
                textoPuzzleTimer = 2f;
            } else {
                for (int i = 0; i < alavancas.length; i++) {
                    if (!player.overlaps(alavancas[i]) || portaPuzzleAberta) continue;
                    acionarAlavanca(i + 1);
                    break;
                }
            }
        }

        // ITEM 27: F liga/desliga a lanterna. O drone continua sendo outra fonte de luz.
        if (Gdx.input.isKeyJustPressed(Input.Keys.F)) {
            lanterna = !lanterna;
            if (drone != null) drone.modoIluminar = lanterna;
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

        moverJogador(dx * playerSpeed * delta, dy * playerSpeed * delta);

        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            if (status.podeAtirar()) {
                Vector2 mouseWorld = viewport.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
                float startX = player.x + player.width / 2f;
                float startY = player.y + player.height / 2f;
                projeteis.add(new Projectile(startX, startY, mouseWorld.x, mouseWorld.y, 600f, 500f).comForca(50f * (1f + status.nivelUpgradeArma * 0.5f), 5f + status.nivelUpgradeArma));
            }
        }

        // ITEM 24: C chama/dispensa o drone (em Tita ele so ilumina, nao atira)
        if (Gdx.input.isKeyJustPressed(Input.Keys.C) && status.inventario.tem("DRONE")) {
            if (drone == null) {
                drone = new Drone(player.x - 55f, player.y - 35f, status.nivelUpgradeDrone);
                drone.modoIluminar = lanterna;
                status.droneAtivo = true;
            } else {
                drone = null;
                status.droneAtivo = false;
            }
        }
    }

    private void acionarAlavanca(int numero) {
        if (ordemAlavancas.size() >= ordemEsperada.length) return;

        int esperado = ordemEsperada[ordemAlavancas.size()];
        if (numero != esperado) {
            ordemAlavancas.clear();
            textoPuzzle = "ORDEM ERRADA! ALAVANCAS 0/3";
            textoPuzzleTimer = 2.5f;
            return;
        }

        ordemAlavancas.add(numero);
        if (ordemAlavancas.size() == ordemEsperada.length) {
            portaPuzzleAberta = true;
            status.inventario.add("PORTA_TITA_ABERTA");
            status.municao += 5;
            textoPuzzle = "ORDEM CORRETA! PORTA ABERTA +5 MUNICAO";
            textoPuzzleTimer = 4f;
            SaveManager.salvarJogo(status, missao);
        } else {
            textoPuzzle = "ALAVANCAS " + ordemAlavancas.size() + "/3";
            textoPuzzleTimer = 1.5f;
        }
    }

    private void moverJogador(float moveX, float moveY) {
        float multiplicadorVelocidade = status.getMultiplicadorVelocidade();
        moveX *= multiplicadorVelocidade;
        moveY *= multiplicadorVelocidade;
        float novoX = MathUtils.clamp(player.x + moveX, 0, WORLD_WIDTH - player.width);
        Rectangle testeX = new Rectangle(novoX, player.y, player.width, player.height);
        if (!colideComObstaculo(testeX)) player.x = novoX;

        float novoY = MathUtils.clamp(player.y + moveY, 0, WORLD_HEIGHT - player.height);
        Rectangle testeY = new Rectangle(player.x, novoY, player.width, player.height);
        if (!colideComObstaculo(testeY)) player.y = novoY;
    }

    private boolean colideComObstaculo(Rectangle r) {
        for (Rectangle o : obstaculos) {
            if (r.overlaps(o)) return true;
        }
        return false;
    }

    private void updateGuardioes(float delta) {
        for (Inimigo g : guardioes) {
            if (!g.ativo) continue;
            g.update(delta, player, projeteisInimigos);
        }
    }

    /** Move os tiros dos guardiões/boss e aplica dano no jogador ao acertar. */
    private void updateProjeteisInimigos(float delta) {
        for (int i = projeteisInimigos.size() - 1; i >= 0; i--) {
            Projectile p = projeteisInimigos.get(i);
            p.update(delta);

            if (!p.ativo) {
                projeteisInimigos.remove(i);
                continue;
            }

            if (player.contains(p.x, p.y)) {
                p.ativo = false;
                status.sofrerDano(12f);
                status.aplicarEfeitoEspecial(p.efeitoTipo);
                projeteisInimigos.remove(i);
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

            for (Inimigo g : guardioes) {
                if (g.ativo && g.bounds.contains(p.x, p.y)) {
                    p.ativo = false;
                    g.tomarDano(p.dano);
                    particleManager.spawnColeta(p.x, p.y);

                    if (!g.ativo) {
                        // ITEM 22: derrotar um guardiao rende creditos.
                        status.inimigosDerrotados++;
                        status.creditos += (g == bossTita ? 10 : 5);
                        status.inventario.add(status.materialDrop((int) (g.bounds.x + g.bounds.y + status.inimigosDerrotados)));
                        if (g == bossTita) {
                            onBossTitaDerrotado();
                        } else {
                            status.municao += 3;
                            checkGuardioesDerrotados();
                        }
                    }
                    projeteis.remove(i);
                    break;
                }
            }
        }
    }

    private void checkGuardioesDerrotados() {
        if (status.titaGuardioesDerrotados) return;

        for (Inimigo g : guardioes) {
            if (g != bossTita && g.ativo) return;
        }

        status.titaGuardioesDerrotados = true;
        SaveManager.salvarJogo(status, missao);
    }

    private void checkBossTita() {
        if (bossTita == null && !status.inventario.tem("CHAVE_TITA") && MissionState.titaMissoesOk(status)) {
            float bx = WORLD_WIDTH - 280f;
            float by = WORLD_HEIGHT / 2f - 70f;
            bossTita = new BossTita(bx, by);
            guardioes.add(bossTita);
        }
    }

    private void onBossTitaDerrotado() {
        status.registrarChefeMorto("TITA");
        status.inventario.add("CHAVE_TITA");
        portalAberto = true;
        portalCalisto.ativo = true;
        particleManager.spawnColeta(
            bossTita.bounds.x + bossTita.bounds.width / 2f,
            bossTita.bounds.y + bossTita.bounds.height / 2f
        );
        SaveManager.salvarJogo(status, missao);
    }

    private void checkColisoes(float delta) {
        for (Inimigo g : guardioes) {
            if (g.ativo && player.overlaps(g.bounds)) {
                status.sofrerDano(g.danoContato * delta);
            }
        }
    }

    private void checkPortalCalisto() {
        if (portalAberto && !trocandoTela && player.overlaps(portalCalisto.bounds)) {
            status.curarAoTrocarFase();
            status.faseAtual = "CALISTO";
            SaveManager.salvarJogo(status, missao);
            game.setScreen(new CutsceneScreen(game, status, CutsceneScreen.Destino.CALISTO));
            trocandoTela = true;
        }
    }

    private void atualizarTextoMissao() {
        if (bossTita != null && bossTita.ativo) {
            textoMissao = "GUARDIAO DO NUCLEO desperta! Derrote-o para conseguir a chave!";
        } else if (status.inventario.tem("CHAVE_TITA")) {
            textoMissao = "Chave conquistada! O portal de Calisto esta ONLINE.";
        } else if (status.titaGuardioesDerrotados) {
            textoMissao = "Guardioes eliminados! Algo maior desperta em Tita...";
        } else {
            textoMissao = "Elimine os guardioes que protegem Tita!";
        }
    }

    private String calcularDicaDirecao() {
        Inimigo alvo = null;
        float melhorDist = Float.MAX_VALUE;

        for (Inimigo g : guardioes) {
            if (!g.ativo) continue;
            float cx = g.bounds.x + g.bounds.width / 2f;
            float cy = g.bounds.y + g.bounds.height / 2f;
            float dist = Vector2.dst(player.x, player.y, cx, cy);
            if (dist < melhorDist) {
                melhorDist = dist;
                alvo = g;
            }
        }

        if (alvo != null) {
            float cx = alvo.bounds.x + alvo.bounds.width / 2f;
            float cy = alvo.bounds.y + alvo.bounds.height / 2f;
            return "Inimigo mais proximo: " + direcaoParaAlvo(cx, cy);
        }

        if (portalAberto) {
            float cx = portalCalisto.bounds.x + portalCalisto.bounds.width / 2f;
            float cy = portalCalisto.bounds.y + portalCalisto.bounds.height / 2f;
            return "Portal Calisto: " + direcaoParaAlvo(cx, cy);
        }

        return null;
    }

    private String direcaoParaAlvo(float alvoX, float alvoY) {
        float px = player.x + player.width / 2f;
        float py = player.y + player.height / 2f;
        float anguloGraus = (float) Math.toDegrees(Math.atan2(alvoY - py, alvoX - px));
        if (anguloGraus < 0) anguloGraus += 360f;

        String[] direcoes = {"L", "NE", "N", "NO", "O", "SO", "S", "SE"};
        int indice = Math.round(anguloGraus / 45f) % 8;
        return direcoes[indice];
    }

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

        shapeRenderer.setColor(0.55f, 0.68f, 0.80f, 1f);
        for (Rectangle o : obstaculos) {
            shapeRenderer.rect(o.x, o.y, o.width, o.height);
        }

        for (Inimigo g : guardioes) {
            if (!g.ativo) continue;
            shapeRenderer.setColor(g.getCor());
            shapeRenderer.rect(g.bounds.x, g.bounds.y, g.bounds.width, g.bounds.height);
        }

        if (bossTita != null && bossTita.ativo) {
            float barraLargura = bossTita.bounds.width;
            shapeRenderer.setColor(Color.RED);
            shapeRenderer.rect(bossTita.bounds.x, bossTita.bounds.y + bossTita.bounds.height + 12, barraLargura, 10);
            shapeRenderer.setColor(Color.GREEN);
            shapeRenderer.rect(bossTita.bounds.x, bossTita.bounds.y + bossTita.bounds.height + 12,
                barraLargura * (bossTita.hp / BossTita.HP_INICIAL), 10);
        }

        for (Projectile p : projeteis) {
            p.render(shapeRenderer);
        }

        // Tiros dos guardiões/boss (magenta, pra diferenciar do tiro do jogador)
        for (Projectile p : projeteisInimigos) {
            if (p.ativo) {
                shapeRenderer.setColor(Color.MAGENTA);
                shapeRenderer.rect(p.x - 5, p.y - 5, 10, 10);
            }
        }

        shapeRenderer.setColor(portalAberto ? Color.MAGENTA : Color.DARK_GRAY);
        shapeRenderer.rect(portalCalisto.bounds.x, portalCalisto.bounds.y, portalCalisto.bounds.width, portalCalisto.bounds.height);

        // ITEM 24: halo de luz do drone (desenhado antes do player, por baixo)
        if (drone != null && drone.modoIluminar) {
            shapeRenderer.setColor(1f, 1f, 0.75f, 0.12f);
            shapeRenderer.circle(drone.bounds.x + drone.bounds.width / 2f, drone.bounds.y + drone.bounds.height / 2f, drone.getRaioLuz());
        }

        // ITEM 26: alavancas e porta do puzzle.
        for (int i = 0; i < alavancas.length; i++) {
            boolean acionada = ordemAlavancas.contains(i + 1) || portaPuzzleAberta;
            shapeRenderer.setColor(acionada ? Color.GREEN : Color.ORANGE);
            shapeRenderer.rect(alavancas[i].x, alavancas[i].y, alavancas[i].width, alavancas[i].height);
        }
        shapeRenderer.setColor(portaPuzzleAberta ? Color.GREEN : Color.RED);
        shapeRenderer.rect(portaPuzzle.x, portaPuzzle.y, portaPuzzle.width, portaPuzzle.height);

        // ITEM 24: estatua/painel visual do checkpoint.
        shapeRenderer.setColor(Color.GOLD);
        shapeRenderer.rect(checkpoint.x, checkpoint.y, checkpoint.width, checkpoint.height);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(checkpoint.x + 10, checkpoint.y + 10, checkpoint.width - 20, checkpoint.height - 20);

        if (regPlayer == null) {
            shapeRenderer.setColor(COR_PLAYER_TITA);
            shapeRenderer.rect(player.x, player.y, player.width, player.height);
        }

        if (drone != null) {
            shapeRenderer.setColor(drone.getCor());
            shapeRenderer.rect(drone.bounds.x, drone.bounds.y, drone.bounds.width, drone.bounds.height);
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
        font.draw(batch, status.temCheckpoint && "TITA".equals(status.checkpointFase) ? "CHECKPOINT SALVO" : "CHECKPOINT [E]", checkpoint.x - 10, checkpoint.y + checkpoint.height + 25);

        font.setColor(Color.WHITE);
        for (int i = 0; i < alavancas.length; i++) {
            font.draw(batch, String.valueOf(i + 1), alavancas[i].x + 15, alavancas[i].y + 42);
        }
        font.draw(batch, portaPuzzleAberta ? "PORTA ABERTA" : "PORTA [ALAVANCAS]", portaPuzzle.x - 20, portaPuzzle.y - 15);

        if (regPlayer != null) {
            batch.setColor(COR_PLAYER_TITA);
            batch.draw(regPlayer, player.x, player.y, player.width, player.height);
            batch.setColor(Color.WHITE);
        }

        if (bossTita != null && bossTita.ativo) {
            font.setColor(bossTita.getCor());
            font.draw(batch, "GUARDIAO DO NUCLEO", bossTita.bounds.x - 10, bossTita.bounds.y + bossTita.bounds.height + 45);
        }

        font.setColor(portalAberto ? Color.MAGENTA : Color.GRAY);
        String txtPortal = portalAberto ? "PORTAL CALISTO [ONLINE]" : "PORTAL CALISTO [BLOQUEADO]";
        font.draw(batch, txtPortal, portalCalisto.bounds.x - 30, portalCalisto.bounds.y - 20);

        batch.end();
    }

    // ITEM 27: mascara escura com uma area transparente de luz.
    private void desenharEscuridao() {
        Vector2 luz = new Vector2(player.x + player.width / 2f, player.y + player.height / 2f);

        if (lanterna || (drone != null && drone.modoIluminar)) {
            if (drone != null && drone.modoIluminar) {
                Vector2 ld = new Vector2(drone.bounds.x + drone.bounds.width / 2f, drone.bounds.y + drone.bounds.height / 2f);
                luz.lerp(ld, 0.35f);
            }

            batch.setProjectionMatrix(camera.combined);
            batch.begin();
            batch.setColor(Color.WHITE);
            batch.draw(lanternaOverlay, luz.x - 1100f, luz.y - 1100f, 2200f, 2200f);
            batch.setColor(Color.WHITE);
            batch.end();
        } else {
            shapeRenderer.setProjectionMatrix(camera.combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(0f, 0f, 0f, 0.92f);
            shapeRenderer.rect(camera.position.x - 640f, camera.position.y - 360f, 1280f, 720f);
            shapeRenderer.end();
        }
    }

    private Texture criarOverlayLanterna() {
        final int tamanho = 512;
        final float raioLivre = 125f;
        final float raioTotal = 195f;
        Pixmap pixmap = new Pixmap(tamanho, tamanho, Pixmap.Format.RGBA8888);
        float centro = tamanho / 2f;

        for (int y = 0; y < tamanho; y++) {
            for (int x = 0; x < tamanho; x++) {
                float dx = x - centro;
                float dy = y - centro;
                float dist = (float) Math.sqrt(dx * dx + dy * dy);
                float alpha;
                if (dist <= raioLivre) alpha = 0f;
                else if (dist >= raioTotal) alpha = 0.92f;
                else {
                    float t = (dist - raioLivre) / (raioTotal - raioLivre);
                    alpha = t * t * 0.92f;
                }
                pixmap.setColor(0f, 0f, 0f, alpha);
                pixmap.drawPixel(x, y);
            }
        }

        Texture textura = new Texture(pixmap);
        textura.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pixmap.dispose();
        return textura;
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
        if (lanternaOverlay != null) lanternaOverlay.dispose();
        batch.dispose();
        shapeRenderer.dispose();
        font.dispose();
        particleManager.clear();
        if (assets != null) assets.dispose();
    }
}
