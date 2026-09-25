package com.Echoes.Jogo.Screen;

import com.Echoes.Jogo.Entities.*;
import com.Echoes.Jogo.Entities.Drone;
import com.Echoes.Jogo.Main;
import com.Echoes.Jogo.Managers.GameAssets;
import com.Echoes.Jogo.Managers.MissionState;
import com.Echoes.Jogo.Managers.ParticleManager;
import com.Echoes.Jogo.Managers.SaveManager;
import com.Echoes.Jogo.Managers.Tempestade;
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

public class MarsScreen implements Screen {

    public static final float WORLD_WIDTH = 2560f;
    public static final float WORLD_HEIGHT = 1440f;

    private final Main game;
    private final PlayerStatus status;
    private InventoryUI inventoryUI;

    private OrthographicCamera camera;
    private OrthographicCamera hudCamera;
    private Viewport viewport;

    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;

    private GameAssets assets;
    private TextureAtlas.AtlasRegion regPlayer;

    private Rectangle player;
    private float playerSpeed = 320f; // Aumentado (era 240f)

    private List<Inimigo> inimigos;
    private List<Projectile> projeteisPlayer;
    private List<Projectile> projeteisInimigos;
    private List<Item> itensDropados;
    private List<Rectangle> obstaculos;

    private Portal portalTita;
    private boolean portalAberto = false;

    private int waveAtual = 1;
    private final int MAX_WAVES = 3;
    private String textoMissaoAtual = "";

    private BossMarte bossMarte;

    // ITEM 24: drone companheiro
    private Drone drone;
    private List<Projectile> projeteisDrone;

    // ITEM 23: tempestade ciclica + abrigo. Fora do abrigo, durante a fase
    // TEMPESTADE, o jogador toma dano por segundo; dentro do abrigo, nada.
    private Tempestade tempestade;
    private Rectangle abrigo;
    private static final float DANO_TEMPESTADE_POR_SEGUNDO = 10f;

    private ParticleManager particleManager;
    private Hud hud;
    private LojaUI lojaUI;
    private MissionState missao;
    private DialogueSystem dialogueSystem;

    private boolean trocandoTela = false;
    private Rectangle checkpoint;

    public MarsScreen(Main game, PlayerStatus status) {
        this.game = game;
        this.status = (status != null) ? status : new PlayerStatus();
        this.status.faseAtual = "MARTE";
    }

    @Override
    public void show() {
        // Garante que nenhum Stage de outra tela (ex: menu) continue interceptando
        // cliques aqui — era isso que causava a fase reiniciar sozinha ao clicar.
        Gdx.input.setInputProcessor(null);

        inventoryUI = new InventoryUI();

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

        player = new Rectangle(WORLD_WIDTH / 2f, 200, 54, 54);

        checkpoint = new Rectangle(320f, 300f, 70f, 70f);
        if (status.temCheckpoint && "MARTE".equals(status.checkpointFase)) {
            player.setPosition(status.checkpointX, status.checkpointY);
        }

        inimigos = new ArrayList<>();
        projeteisPlayer = new ArrayList<>();
        projeteisInimigos = new ArrayList<>();
        itensDropados = new ArrayList<>();

        // Pedras espalhadas pelo campo de batalha
        obstaculos = new ArrayList<>();
        obstaculos.add(new Rectangle(900, 900, 90, 90));
        obstaculos.add(new Rectangle(1700, 900, 90, 90));
        obstaculos.add(new Rectangle(900, 500, 90, 90));
        obstaculos.add(new Rectangle(1700, 500, 90, 90));
        obstaculos.add(new Rectangle(1280, 1100, 110, 110));

        portalTita = new Portal(WORLD_WIDTH / 2f - 45, WORLD_HEIGHT - 200, 90, 90);

        bossMarte = null;

        projeteisDrone = new ArrayList<>();
        drone = (status.droneAtivo && status.inventario.tem("DRONE"))
            ? new Drone(player.x - 55f, player.y - 35f, status.nivelUpgradeDrone)
            : null;

        // ITEM 23: tempestade e abrigo — perto do ponto de spawn, pra o jogador
        // sempre ter uma referencia proxima de onde se proteger.
        tempestade = new Tempestade();
        abrigo = new Rectangle(WORLD_WIDTH / 2f - 110, 90, 220, 170);

        portalAberto = status.inventario.tem("CHAVE_MARTE");
        portalTita.ativo = portalAberto;

        particleManager = new ParticleManager();
        hud = new Hud();
        lojaUI = new LojaUI();
        missao = new MissionState();

        waveAtual = Math.max(1, Math.min(status.marteWaveAtual, MAX_WAVES));
        status.marteWaveAtual = waveAtual;

        String[] falasMarte = new String[]{
            "ESTACAO DE SUPORTE: Mate eles e depois vá salvar Titã! E cuidado com as tempestades — abrigue-se quando o ceu mudar!"
        };
        dialogueSystem = new DialogueSystem(falasMarte);

        if (status.marteWavesConcluidas) {
            inimigos.clear();
        } else {
            iniciarWave(waveAtual);
        }

        atualizarTextoMissao();
        SaveManager.salvarJogo(status, missao);
    }

    private void iniciarWave(int wave) {
        inimigos.clear();
        projeteisInimigos.clear();

        int numInimigos = 4 + (wave * 2);
        for (int i = 0; i < numInimigos; i++) {
            float rx = MathUtils.random(200f, WORLD_WIDTH - 200f);
            float ry = MathUtils.random(600f, WORLD_HEIGHT - 200f);

            Inimigo.TipoInimigo tipo;
            if (i % 3 == 0) tipo = Inimigo.TipoInimigo.RAPIDO;
            else if (i % 3 == 1) tipo = Inimigo.TipoInimigo.ATIRADOR;
            else tipo = Inimigo.TipoInimigo.NORMAL;

            Inimigo novoInimigo = new Inimigo(rx, ry, tipo);
            novoInimigo.perseguicaoTotal = true; // garante que a wave sempre pode ser concluida
            inimigos.add(novoInimigo);
        }
    }

    @Override
    public void render(float delta) {
        com.Echoes.Jogo.Entities.Inimigo.MULTIPLICADOR_VELOCIDADE_INIMIGO = status.dificuldade.multiplicadorVelocidadeInimigo;
        inventoryUI.update(status);
        // Inventario e uma pausa real: nenhuma logica de jogo roda enquanto ele esta aberto.
        if (!inventoryUI.isOpen()) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.B) && !lojaUI.isAberta()) lojaUI.abrir();
        if (!status.missaoFalhou) {
            if (dialogueSystem.ativo) {
                dialogueSystem.update();
            } else if (lojaUI.isAberta()) {
                lojaUI.update(delta, status);
            } else {
                handleInput(delta);
                updateInimigos(delta);
                updateProjeteis(delta);
                updateDrone(delta);
                checkColisoes(delta);
                if (status.dropMorte != null && player.overlaps(status.dropMorte.bounds)) {
                    status.recolherDrop();
                }
                updateWaveManager();
                checkBossMarte();
                atualizarTempestade(delta);
                atualizarTextoMissao();
                updateCamera();
                particleManager.update(delta);
                status.atualizarCombate(delta); // corrige o bug de nao conseguir atirar de novo
            }
        } else {
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

        }

        // A troca de tela foi agendada (portal usado) — para aqui
        if (trocandoTela) {
            dispose();
            return;
        }

        // ITEM 23: cor do ceu muda conforme a fase da tempestade
        Color corCeu = corDoCeu();
        Gdx.gl.glClearColor(corCeu.r, corCeu.g, corCeu.b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        desenharMundo();

        String extraHud = (bossMarte != null && bossMarte.ativo)
            ? "BOSS DE MARTE: " + (int) bossMarte.hp + "/" + (int) BossMarte.HP_INICIAL
            : "WAVE: " + waveAtual + "/" + MAX_WAVES;
        extraHud += " | " + tempestade.getTextoHud();

        hud.render(shapeRenderer, batch, font, hudCamera, status, textoMissaoAtual, extraHud, 720);

        if (dialogueSystem.ativo) {
            dialogueSystem.render(shapeRenderer, batch, font, 1280, 720);
        }

        inventoryUI.render(shapeRenderer, batch, font, hudCamera, status);
        lojaUI.render(shapeRenderer, batch, font, hudCamera, status);
    }

    /** ITEM 23: avança o ciclo da tempestade e aplica dano se o jogador estiver exposto. */
    private void atualizarTempestade(float delta) {
        tempestade.update(delta);

        if (tempestade.estaEmTempestade() && !player.overlaps(abrigo)) {
            status.sofrerDano(DANO_TEMPESTADE_POR_SEGUNDO * delta);
        }
    }

    private Color corDoCeu() {
        switch (tempestade.getFase()) {
            case ALERTA: return new Color(0.35f, 0.15f, 0.05f, 1f);
            case TEMPESTADE: return new Color(0.12f, 0.05f, 0.10f, 1f);
            default: return new Color(0.25f, 0.08f, 0.05f, 1f);
        }
    }

    private void handleInput(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.B)) { lojaUI.abrir(); return; }
        if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            if (player.overlaps(checkpoint)) {
                status.salvarCheckpoint(checkpoint.x + checkpoint.width / 2f - player.width / 2f, checkpoint.y + checkpoint.height / 2f - player.height / 2f, "MARTE");
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
            projeteisPlayer.add(new Projectile(startX, startY, mouseWorld.x, mouseWorld.y, 600f, 500f).comForca(dano, tamanho));
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
                projeteisPlayer.add(new Projectile(startX, startY, mouseWorld.x, mouseWorld.y, 600f, 500f).comForca(50f * (1f + status.nivelUpgradeArma * 0.5f), 5f + status.nivelUpgradeArma));
            }
        }

        // ITEM 24: C chama/dispensa o drone
        if (Gdx.input.isKeyJustPressed(Input.Keys.C) && status.inventario.tem("DRONE")) {
            if (drone == null) {
                drone = new Drone(player.x - 55f, player.y - 35f, status.nivelUpgradeDrone);
                status.droneAtivo = true;
            } else {
                drone = null;
                status.droneAtivo = false;
            }
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

    private void updateInimigos(float delta) {
        for (Inimigo ini : inimigos) {
            ini.update(delta, player, projeteisInimigos);
        }
    }

    private void updateProjeteis(float delta) {
        for (int i = projeteisPlayer.size() - 1; i >= 0; i--) {
            Projectile p = projeteisPlayer.get(i);
            p.update(delta);

            if (!p.ativo) {
                projeteisPlayer.remove(i);
                continue;
            }

            for (int j = inimigos.size() - 1; j >= 0; j--) {
                Inimigo ini = inimigos.get(j);
                if (ini.ativo && ini.bounds.contains(p.x, p.y)) {
                    p.ativo = false;
                    ini.tomarDano(p.dano);
                    particleManager.spawnColeta(ini.bounds.x, ini.bounds.y);

                    if (!ini.ativo) {
                        inimigos.remove(j);
                        status.inimigosDerrotados++;
                        status.creditos += (ini == bossMarte ? 10 : 5);
                        status.inventario.add(status.materialDrop((int) (ini.bounds.x + ini.bounds.y + status.inimigosDerrotados)));
                        if (ini == bossMarte) {
                            onBossMarteDerrotado();
                        } else {
                            itensDropados.add(new Item(ini.bounds.x, ini.bounds.y, ini.getDrop()));
                        }
                    }
                    break;
                }
            }
        }

        for (int i = projeteisInimigos.size() - 1; i >= 0; i--) {
            Projectile p = projeteisInimigos.get(i);
            p.update(delta);

            if (!p.ativo) {
                projeteisInimigos.remove(i);
                continue;
            }

            if (player.contains(p.x, p.y)) {
                p.ativo = false;
                status.sofrerDano(15f);
                status.aplicarEfeitoEspecial(p.efeitoTipo);
                projeteisInimigos.remove(i);
            }
        }
    }

    private void onBossMarteDerrotado() {
        status.registrarChefeMorto("MARTE");
        status.inventario.add("CHAVE_MARTE");
        portalAberto = true;
        portalTita.ativo = true;
        particleManager.spawnColeta(
            bossMarte.bounds.x + bossMarte.bounds.width / 2f,
            bossMarte.bounds.y + bossMarte.bounds.height / 2f
        );
        SaveManager.salvarJogo(status, missao);
    }

    /** ITEM 24: move o drone e processa os tiros que ele disparou sozinho. */
    private void updateDrone(float delta) {
        if (drone == null) return;
        drone.update(delta, player, inimigos, projeteisDrone);

        for (int i = projeteisDrone.size() - 1; i >= 0; i--) {
            Projectile p = projeteisDrone.get(i);
            p.update(delta);

            if (!p.ativo) {
                projeteisDrone.remove(i);
                continue;
            }

            for (int j = inimigos.size() - 1; j >= 0; j--) {
                Inimigo ini = inimigos.get(j);
                if (ini.ativo && ini.bounds.contains(p.x, p.y)) {
                    p.ativo = false;
                    ini.tomarDano(35);
                    particleManager.spawnColeta(ini.bounds.x, ini.bounds.y);
                    if (!ini.ativo) {
                        inimigos.remove(j);
                        status.inimigosDerrotados++;
                        status.creditos += (ini == bossMarte ? 10 : 5);
                        status.inventario.add(status.materialDrop((int) (ini.bounds.x + ini.bounds.y + status.inimigosDerrotados)));
                        if (ini == bossMarte) {
                            onBossMarteDerrotado();
                        } else {
                            itensDropados.add(new Item(ini.bounds.x, ini.bounds.y, ini.getDrop()));
                        }
                    }
                    projeteisDrone.remove(i);
                    break;
                }
            }
        }
    }

    private void checkColisoes(float delta) {
        for (Inimigo ini : inimigos) {
            if (ini.ativo && player.overlaps(ini.bounds)) {
                status.sofrerDano(ini.danoContato * delta);
            }
        }

        for (int i = itensDropados.size() - 1; i >= 0; i--) {
            Item item = itensDropados.get(i);
            if (!item.coletado && player.overlaps(item.bounds)) {
                item.coletado = true;
                status.creditos += 2;
                switch (item.type) {
                    case MUNICAO: status.municao += 8; break;
                    case METAL: status.inventario.add("METAL"); break;
                    case CIRCUITO: status.inventario.add("CIRCUITO"); break;
                    case GELO: status.inventario.add("GELO"); break;
                    case PECA: status.inventario.add("PECA"); break;
                    default: break;
                }
                itensDropados.remove(i);
            }
        }

        if (portalAberto && !trocandoTela && player.overlaps(portalTita.bounds)) {
            status.curarAoTrocarFase(); // regenera parte da vida na troca de fase
            status.faseAtual = "TITA";
            SaveManager.salvarJogo(status, missao);
            game.setScreen(new CutsceneScreen(game, status, CutsceneScreen.Destino.TITA));
            trocandoTela = true;
        }
    }

    private void updateWaveManager() {
        if (portalAberto || bossMarte != null) return; // ja resolvido ou boss em andamento

        if (inimigos.isEmpty()) {
            if (waveAtual < MAX_WAVES) {
                waveAtual++;
                status.marteWaveAtual = waveAtual;
                iniciarWave(waveAtual);
                SaveManager.salvarJogo(status, missao); // checkpoint a cada wave concluida
            } else if (!status.marteWavesConcluidas) {
                status.marteWavesConcluidas = true;
                SaveManager.salvarJogo(status, missao); // checkpoint: waves vencidas, aguardando o boss
            }
        }
    }

    private void checkBossMarte() {
        if (bossMarte == null && !status.inventario.tem("CHAVE_MARTE") && MissionState.marteMissoesOk(status)) {
            float bx = portalTita.bounds.x - 10f;
            float by = portalTita.bounds.y - 220f;
            bossMarte = new BossMarte(bx, by);
            inimigos.add(bossMarte);
        }
    }

    private void atualizarTextoMissao() {
        if (bossMarte != null && bossMarte.ativo) {
            textoMissaoAtual = "SENTINELA DE MARTE desperta! Derrote-a para conseguir a chave!";
        } else if (status.inventario.tem("CHAVE_MARTE")) {
            textoMissaoAtual = "Leve a chave ao portal de Tita!";
        } else if (portalAberto) {
            textoMissaoAtual = "Ameaca contida! Entre no Portal para Tita!";
        } else if (status.marteWavesConcluidas) {
            textoMissaoAtual = "Ondas repelidas! Um sinal poderoso se aproxima...";
        } else {
            textoMissaoAtual = "Sobreviva a Wave " + waveAtual + "/" + MAX_WAVES + " em Marte!";
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

        // Obstáculos (pedras)
        shapeRenderer.setColor(Color.BROWN);
        for (Rectangle o : obstaculos) {
            shapeRenderer.rect(o.x, o.y, o.width, o.height);
        }

        // ITEM 23: abrigo — verde-azulado quando o jogador esta dentro, cinza normalmente
        boolean playerNoAbrigo = player.overlaps(abrigo);
        shapeRenderer.setColor(playerNoAbrigo ? new Color(0.25f, 0.55f, 0.55f, 1f) : new Color(0.4f, 0.4f, 0.42f, 1f));
        shapeRenderer.rect(abrigo.x, abrigo.y, abrigo.width, abrigo.height);

        for (Inimigo ini : inimigos) {
            shapeRenderer.setColor(ini.getCor());
            shapeRenderer.rect(ini.bounds.x, ini.bounds.y, ini.bounds.width, ini.bounds.height);
        }

        if (bossMarte != null && bossMarte.ativo) {
            float barraLargura = bossMarte.bounds.width;
            shapeRenderer.setColor(Color.RED);
            shapeRenderer.rect(bossMarte.bounds.x, bossMarte.bounds.y + bossMarte.bounds.height + 12, barraLargura, 10);
            shapeRenderer.setColor(Color.GREEN);
            shapeRenderer.rect(bossMarte.bounds.x, bossMarte.bounds.y + bossMarte.bounds.height + 12,
                barraLargura * (bossMarte.hp / BossMarte.HP_INICIAL), 10);
        }

        for (Item item : itensDropados) {
            shapeRenderer.setColor(Color.GOLD);
            shapeRenderer.rect(item.bounds.x, item.bounds.y, item.bounds.width, item.bounds.height);
        }

        shapeRenderer.setColor(portalAberto ? Color.MAGENTA : Color.DARK_GRAY);
        shapeRenderer.rect(portalTita.bounds.x, portalTita.bounds.y, portalTita.bounds.width, portalTita.bounds.height);

        for (Projectile p : projeteisPlayer) {
            shapeRenderer.setColor(Color.CYAN);
            shapeRenderer.rect(p.x - p.tamanho, p.y - p.tamanho, p.tamanho * 2f, p.tamanho * 2f);
        }
        for (Projectile p : projeteisInimigos) {
            shapeRenderer.setColor(Color.MAGENTA);
            shapeRenderer.rect(p.x - 5, p.y - 5, 10, 10);
        }

        // ITEM 24: drone e os tiros dele
        for (Projectile p : projeteisDrone) {
            if (p.ativo) {
                shapeRenderer.setColor(new Color(0.35f, 0.85f, 1f, 1f));
                shapeRenderer.circle(p.x, p.y, 4);
            }
        }
        if (drone != null) {
            shapeRenderer.setColor(drone.getCor());
            shapeRenderer.rect(drone.bounds.x, drone.bounds.y, drone.bounds.width, drone.bounds.height);
        }

        // ITEM 24: estatua/painel visual do checkpoint.
        shapeRenderer.setColor(Color.GOLD);
        shapeRenderer.rect(checkpoint.x, checkpoint.y, checkpoint.width, checkpoint.height);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(checkpoint.x + 10, checkpoint.y + 10, checkpoint.width - 20, checkpoint.height - 20);

        if (regPlayer == null) {
            shapeRenderer.setColor(Color.CYAN);
            shapeRenderer.rect(player.x, player.y, player.width, player.height - 12);
            shapeRenderer.setColor(Color.YELLOW);
            shapeRenderer.rect(player.x + 12, player.y + player.height - 16, player.width - 24, 12);
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
        font.draw(batch, status.temCheckpoint && "MARTE".equals(status.checkpointFase) ? "CHECKPOINT SALVO" : "CHECKPOINT [E]", checkpoint.x - 10, checkpoint.y + checkpoint.height + 25);

        if (regPlayer != null) {
            batch.draw(regPlayer, player.x, player.y, player.width, player.height);
        }

        if (bossMarte != null && bossMarte.ativo) {
            font.setColor(bossMarte.getCor());
            font.draw(batch, "SENTINELA DE MARTE", bossMarte.bounds.x - 20, bossMarte.bounds.y + bossMarte.bounds.height + 45);
        }

        font.setColor(portalAberto ? Color.MAGENTA : Color.GRAY);
        String txt = portalAberto ? "PORTAL PARA TITA [ABERTO]" : "PORTAL BLOQUEADO";
        font.draw(batch, txt, portalTita.bounds.x - 30, portalTita.bounds.y + portalTita.bounds.height + 20);

        // ITEM 23: rotulo do abrigo
        font.setColor(playerNoAbrigo ? Color.CYAN : Color.LIGHT_GRAY);
        font.draw(batch, "ABRIGO", abrigo.x + 75, abrigo.y - 12);

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
