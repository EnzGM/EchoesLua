package com.Echoes.Jogo.Screen;

import com.Echoes.Jogo.Entities.Base;
import com.Echoes.Jogo.Entities.BossLua;
import com.Echoes.Jogo.Entities.Drone;
import com.Echoes.Jogo.Entities.Inimigo;
import com.Echoes.Jogo.Entities.Item;
import com.Echoes.Jogo.Entities.ItemType;
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
import com.Echoes.Jogo.Ui.InventoryUI;
import com.Echoes.Jogo.Ui.LojaUI;
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

public class LunarScreen implements Screen {

    public static final float WORLD_WIDTH = 2560f;
    public static final float WORLD_HEIGHT = 1440f;

    private final Main game;
    private final PlayerStatus status;
    private MissionState missao;
    private InventoryUI inventoryUI;
    private LojaUI lojaUI;

    // ITEM 22: NPC lojista da Lua.
    private Rectangle lojista;

    private OrthographicCamera camera;
    private OrthographicCamera hudCamera;
    private Viewport viewport;

    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;

    private GameAssets assets;
    private TextureAtlas.AtlasRegion regPlayer;
    private TextureAtlas.AtlasRegion regBase;

    private Rectangle player;
    private float playerSpeed = 320f;

    private Rectangle estufa, energia, extracao, comunicacao;

    private Base base;

    // ITEM 22: bancada de crafting — separada da Base (que so crafta a arma).
    private Rectangle bancada;

    private List<Item> itens;

    private List<Rectangle> obstaculos;

    private List<Inimigo> inimigos;
    private List<Projectile> projeteisInimigos;
    private List<Projectile> projeteisPlayer;

    private BossLua bossLua;

    // ITEM 24: drone companheiro
    private Drone drone;
    private List<Projectile> projeteisDrone;

    private Portal portalMarte;
    private boolean portalAberto = false;
    private String textoMissao = "Colete as pecas e repare as 4 estacoes da Lua!";

    private ParticleManager particleManager;
    private Hud hud;
    private DialogueSystem dialogueSystem;

    private boolean trocandoTela = false;
    private Rectangle checkpoint;

    public LunarScreen(Main game, PlayerStatus status) {
        this.game = game;
        this.status = (status != null) ? status : new PlayerStatus();
        this.status.faseAtual = "LUA";
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(null);

        inventoryUI = new InventoryUI();
        lojaUI = new LojaUI();

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
        regBase = assets.getRegion("base");

        player = new Rectangle(WORLD_WIDTH / 2f, WORLD_HEIGHT / 2f, 64, 64);

        checkpoint = new Rectangle(360f, 620f, 70f, 70f);
        if (status.temCheckpoint && "LUA".equals(status.checkpointFase)) {
            player.setPosition(status.checkpointX, status.checkpointY);
        }

        estufa = new Rectangle(400, 1000, 150, 150);
        energia = new Rectangle(2000, 1000, 150, 150);
        extracao = new Rectangle(400, 300, 150, 150);
        comunicacao = new Rectangle(2000, 300, 150, 150);

        base = new Base(WORLD_WIDTH / 2f - 75, WORLD_HEIGHT / 2f - 260, 150, 150);

        // ITEM 22: bancada nova, num canto livre do mapa, longe da Base de arma.
        bancada = new Rectangle(WORLD_WIDTH / 2f - 75, WORLD_HEIGHT / 2f + 180, 150, 150);

        // ITEM 22: NPC lojista perto da base.
        lojista = new Rectangle(WORLD_WIDTH / 2f + 180, WORLD_HEIGHT / 2f - 80, 90, 90);

        itens = new ArrayList<>();
        itens.add(new Item(700, 850, ItemType.PECA_ESTUFA));
        itens.add(new Item(1850, 850, ItemType.PECA_GERADOR));
        itens.add(new Item(700, 450, ItemType.PECA_USINA));
        itens.add(new Item(1850, 450, ItemType.PECA_ANTENA));
        itens.add(new Item(1280, 950, ItemType.ARMA_PARTE_A));
        itens.add(new Item(200, 700, ItemType.ARMA_PARTE_B));
        itens.add(new Item(2300, 700, ItemType.ARMA_PARTE_C));

        // ITEM 22: materiais de crafting espalhados perto da bancada
        itens.add(new Item(1150, 1200, ItemType.GELO));
        itens.add(new Item(1400, 1200, ItemType.PECA));
        itens.add(new Item(1150, 1080, ItemType.METAL));
        itens.add(new Item(1400, 1080, ItemType.CIRCUITO));

        // ITEM 24: item que libera o drone companheiro
        itens.add(new Item(2300, 1200, ItemType.DRONE));

        obstaculos = new ArrayList<>();
        obstaculos.add(new Rectangle(750, 950, 90, 90));
        obstaculos.add(new Rectangle(1900, 950, 90, 90));
        obstaculos.add(new Rectangle(750, 550, 90, 90));
        obstaculos.add(new Rectangle(1900, 550, 90, 90));
        obstaculos.add(new Rectangle(1500, 1050, 100, 100));

        inimigos = new ArrayList<>();
        inimigos.add(new Inimigo(900, 1150, Inimigo.TipoInimigo.RAPIDO));
        inimigos.add(new Inimigo(1650, 1150, Inimigo.TipoInimigo.NORMAL));
        inimigos.add(new Inimigo(1280, 220, Inimigo.TipoInimigo.NORMAL));
        projeteisInimigos = new ArrayList<>();
        projeteisPlayer = new ArrayList<>();

        bossLua = null;

        projeteisDrone = new ArrayList<>();
        // ITEM 24: se o drone estava chamado antes (save/checkpoint), ele volta sozinho
        drone = (status.droneAtivo && status.inventario.tem("DRONE"))
            ? new Drone(player.x - 55f, player.y - 35f, status.nivelUpgradeDrone)
            : null;

        portalMarte = new Portal(WORLD_WIDTH / 2f - 45, WORLD_HEIGHT - 200, 90, 90);
        portalMarte.ativo = false;

        particleManager = new ParticleManager();
        hud = new Hud();
        missao = new MissionState();

        String[] falasLua = new String[]{
            "ESTACAO DE SUPORTE: Saudacoes, precisamos que voce conserte as estacoes, a arma, e va salvar Marte!"
        };
        dialogueSystem = new DialogueSystem(falasLua);

        SaveManager.salvarJogo(status, missao);
    }

    @Override
    public void render(float delta) {
        com.Echoes.Jogo.Entities.Inimigo.MULTIPLICADOR_VELOCIDADE_INIMIGO = status.dificuldade.multiplicadorVelocidadeInimigo;
        inventoryUI.update(status);
        // Inventario e uma pausa real: nenhuma logica de jogo roda enquanto ele esta aberto.
        if (!inventoryUI.isOpen()) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.B) && !lojaUI.isAberta()) lojaUI.abrir();

        if (dialogueSystem.ativo) {
            dialogueSystem.update();
        } else if (lojaUI.isAberta()) {
            // Enquanto a loja estiver aberta, so ela recebe input.
            lojaUI.update(delta, status);
        } else if (lojaUI.isAberta()) {
            lojaUI.update(delta, status);
        } else {
            handleInput(delta);
            checkColetaItens();
            if (status.dropMorte != null && player.overlaps(status.dropMorte.bounds)) {
                status.recolherDrop();
            }
            checkReparos();
            checkBossLua();
            updateInimigos(delta);
            updateProjeteisInimigos(delta);
            updateProjeteisPlayer(delta);
            updateDrone(delta);
            checkColisaoInimigos(delta);
            checkPortal();
            atualizarTextoMissao();
            updateCamera();
            particleManager.update(delta);
            status.atualizarCombate(delta);

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

        Gdx.gl.glClearColor(0.15f, 0.15f, 0.15f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        desenharMundo();

        int pecasArma = (status.colArmaParteA ? 1 : 0) + (status.colArmaParteB ? 1 : 0) + (status.colArmaParteC ? 1 : 0);
        String extra = "PECAS ARMA: " + pecasArma + "/3";
        hud.render(shapeRenderer, batch, font, hudCamera, status, textoMissao, extra, 720);

        if (dialogueSystem.ativo) {
            dialogueSystem.render(shapeRenderer, batch, font, 1280, 720);
        }

        inventoryUI.render(shapeRenderer, batch, font, hudCamera, status);
        lojaUI.render(shapeRenderer, batch, font, hudCamera, status);
    }

    private void handleInput(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.B)) { lojaUI.abrir(); return; }
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

        if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            if (player.overlaps(checkpoint)) {
                status.salvarCheckpoint(checkpoint.x + checkpoint.width / 2f - player.width / 2f,
                    checkpoint.y + checkpoint.height / 2f - player.height / 2f, "LUA");
                SaveManager.salvarJogo(status, missao);
            } else {
            if (player.overlaps(base.bounds)) {
                if (!status.armaCraftada && status.colArmaParteA && status.colArmaParteB && status.colArmaParteC) {
                    status.armaCraftada = true;
                    particleManager.spawnColeta(base.bounds.x + 75, base.bounds.y + 75);
                }
            }
        }
        }

        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            if (status.podeAtirar()) {
                Vector2 mouseWorld = viewport.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
                float startX = player.x + player.width / 2f;
                float startY = player.y + player.height / 2f;
                projeteisPlayer.add(new Projectile(startX, startY, mouseWorld.x, mouseWorld.y, 600f, 500f).comForca(50f * (1f + status.nivelUpgradeArma * 0.5f), 5f + status.nivelUpgradeArma));
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.F5)) {
            SaveManager.salvarJogo(status, missao);
        }

        // ITEM 24: C chama/dispensa o drone (precisa ter o item DRONE no inventario)
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

    private void checkColetaItens() {
        for (int i = itens.size() - 1; i >= 0; i--) {
            Item item = itens.get(i);
            if (!item.coletado && player.overlaps(item.bounds)) {
                item.coletado = true;
                particleManager.spawnColeta(item.bounds.x, item.bounds.y);

                // ITEM 22: qualquer coleta gera creditos.
                status.creditos += 2;

                switch (item.type) {
                    case PECA_ESTUFA:  status.colPecaEstufa = true;  status.pecaEstufa++;  break;
                    case PECA_GERADOR: status.colPecaGerador = true; status.pecaGerador++; break;
                    case PECA_USINA:   status.colPecaUsina = true;   status.pecaUsina++;   break;
                    case PECA_ANTENA:  status.colPecaAntena = true;  status.pecaAntena++;  break;
                    case ARMA_PARTE_A: status.colArmaParteA = true;  status.armaParteA++;  break;
                    case ARMA_PARTE_B: status.colArmaParteB = true;  status.armaParteB++;  break;
                    case ARMA_PARTE_C: status.colArmaParteC = true;  status.armaParteC++;  break;
                    // ITEM 22: materiais de crafting vao pro Inventario (com quantidade)
                    case GELO:     status.inventario.add("GELO");     break;
                    case PECA:     status.inventario.add("PECA");     break;
                    case METAL:    status.inventario.add("METAL");    break;
                    case CIRCUITO: status.inventario.add("CIRCUITO"); break;
                    case DRONE:    status.inventario.add("DRONE");    break;
                    default: break;
                }

                itens.remove(i);
            }
        }

        status.pecasColetadas = status.colPecaEstufa && status.colPecaGerador
            && status.colPecaUsina && status.colPecaAntena;
    }

    private void checkReparos() {
        if (player.overlaps(estufa) && !status.estufaReparada && status.colPecaEstufa) status.estufaReparada = true;
        if (player.overlaps(energia) && !status.energiaReparada && status.colPecaGerador) status.energiaReparada = true;
        if (player.overlaps(extracao) && !status.extracaoReparada && status.colPecaUsina) status.extracaoReparada = true;
        if (player.overlaps(comunicacao) && !status.comunicacaoReparada && status.colPecaAntena) status.comunicacaoReparada = true;
    }

    private void checkBossLua() {
        if (bossLua == null && MissionState.luaMissoesOk(status)) {
            float bx = portalMarte.bounds.x - 10f;
            float by = portalMarte.bounds.y - 260f;
            bossLua = new BossLua(bx, by);
            inimigos.add(bossLua);
        }
    }

    private void updateInimigos(float delta) {
        for (Inimigo ini : inimigos) {
            ini.update(delta, player, projeteisInimigos);
        }
    }

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

    private void updateProjeteisPlayer(float delta) {
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

                        // ITEM 22: derrotar inimigo rende creditos.
                        status.inimigosDerrotados++;
                        status.creditos += (ini == bossLua ? 10 : 5);
                        status.inventario.add(status.materialDrop((int) (ini.bounds.x + ini.bounds.y + status.inimigosDerrotados)));

                        if (ini == bossLua) {
                            onBossLuaDerrotado();
                        }
                    }
                    projeteisPlayer.remove(i);
                    break;
                }
            }
        }
    }

    private void onBossLuaDerrotado() {
        status.registrarChefeMorto("LUA");
        status.inventario.add("CHAVE_LUA");
        particleManager.spawnColeta(
            bossLua.bounds.x + bossLua.bounds.width / 2f,
            bossLua.bounds.y + bossLua.bounds.height / 2f
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

                        // ITEM 22: derrotas causadas pelo drone tambem rendem creditos.
                        status.inimigosDerrotados++;
                        status.creditos += (ini == bossLua ? 10 : 5);
                        status.inventario.add(status.materialDrop((int) (ini.bounds.x + ini.bounds.y + status.inimigosDerrotados)));

                        if (ini == bossLua) onBossLuaDerrotado();
                    }
                    projeteisDrone.remove(i);
                    break;
                }
            }
        }
    }

    private void checkColisaoInimigos(float delta) {
        for (Inimigo ini : inimigos) {
            if (ini.ativo && player.overlaps(ini.bounds)) {
                status.sofrerDano(ini.danoContato * delta);
            }
        }
    }

    private void checkPortal() {
        if (!portalAberto && status.todosReparosConcluidos() && status.armaCraftada) {
            portalAberto = true;
            portalMarte.ativo = true;
        }

        if (portalAberto && !trocandoTela && player.overlaps(portalMarte.bounds)) {
            status.faseAtual = "MARTE";
            SaveManager.salvarJogo(status, missao);
            game.setScreen(new CutsceneScreen(game, status, CutsceneScreen.Destino.MARTE));
            trocandoTela = true;
        }
    }

    private void atualizarTextoMissao() {
        if (bossLua != null && bossLua.ativo) {
            textoMissao = "GUARDIAO DA CRATERA desperta! Derrote-o para conseguir a chave!";
        } else if (status.inventario.tem("CHAVE_LUA")) {
            textoMissao = "Leve a chave ao portal de Marte!";
        } else if (portalAberto) {
            textoMissao = "Sistemas online e arma pronta! Entre no portal para Marte!";
        } else if (status.armaCraftada) {
            textoMissao = "Arma pronta! Termine de reparar as estacoes restantes.";
        } else if (status.todosReparosConcluidos()) {
            textoMissao = "Estacoes reparadas! Pegue as 3 pecas da arma e volte a base (E)!";
        } else {
            textoMissao = "Colete as pecas e repare as 4 estacoes da Lua!";
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

        shapeRenderer.setColor(status.estufaReparada ? Color.GREEN : Color.RED);
        shapeRenderer.rect(estufa.x, estufa.y, estufa.width, estufa.height);
        shapeRenderer.setColor(status.energiaReparada ? Color.GREEN : Color.RED);
        shapeRenderer.rect(energia.x, energia.y, energia.width, energia.height);
        shapeRenderer.setColor(status.extracaoReparada ? Color.GREEN : Color.RED);
        shapeRenderer.rect(extracao.x, extracao.y, extracao.width, extracao.height);
        shapeRenderer.setColor(status.comunicacaoReparada ? Color.GREEN : Color.RED);
        shapeRenderer.rect(comunicacao.x, comunicacao.y, comunicacao.width, comunicacao.height);

        if (regBase == null) {
            shapeRenderer.setColor(status.armaCraftada ? Color.SKY : Color.NAVY);
            shapeRenderer.rect(base.bounds.x, base.bounds.y, base.bounds.width, base.bounds.height);
        }

        // ITEM 22: bancada de crafting (roxa, pra diferenciar da Base de arma)
        shapeRenderer.setColor(new Color(0.55f, 0.25f, 0.75f, 1f));
        shapeRenderer.rect(bancada.x, bancada.y, bancada.width, bancada.height);

        // ITEM 22: NPC lojista.
        shapeRenderer.setColor(Color.GOLD);
        shapeRenderer.rect(lojista.x, lojista.y, lojista.width, lojista.height);

        shapeRenderer.setColor(Color.DARK_GRAY);
        for (Rectangle o : obstaculos) {
            shapeRenderer.rect(o.x, o.y, o.width, o.height);
        }

        for (Item item : itens) {
            shapeRenderer.setColor(Color.GOLD);
            shapeRenderer.rect(item.bounds.x, item.bounds.y, item.bounds.width, item.bounds.height);
        }

        for (Inimigo ini : inimigos) {
            shapeRenderer.setColor(ini.getCor());
            shapeRenderer.rect(ini.bounds.x, ini.bounds.y, ini.bounds.width, ini.bounds.height);
        }

        if (bossLua != null && bossLua.ativo) {
            float barraLargura = bossLua.bounds.width;
            shapeRenderer.setColor(Color.RED);
            shapeRenderer.rect(bossLua.bounds.x, bossLua.bounds.y + bossLua.bounds.height + 12, barraLargura, 10);
            shapeRenderer.setColor(Color.GREEN);
            shapeRenderer.rect(bossLua.bounds.x, bossLua.bounds.y + bossLua.bounds.height + 12,
                barraLargura * (bossLua.hp / BossLua.HP_INICIAL), 10);
        }

        for (Projectile p : projeteisPlayer) {
            shapeRenderer.setColor(Color.CYAN);
            shapeRenderer.rect(p.x - p.tamanho, p.y - p.tamanho, p.tamanho * 2f, p.tamanho * 2f);
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

        for (Projectile p : projeteisInimigos) {
            if (p.ativo) {
                shapeRenderer.setColor(Color.MAGENTA);
                shapeRenderer.rect(p.x - 5, p.y - 5, 10, 10);
            }
        }

        shapeRenderer.setColor(portalAberto ? Color.ORANGE : Color.DARK_GRAY);
        shapeRenderer.rect(portalMarte.bounds.x, portalMarte.bounds.y, portalMarte.bounds.width, portalMarte.bounds.height);

        // ITEM 24: estatua/painel visual do checkpoint.
        shapeRenderer.setColor(Color.GOLD);
        shapeRenderer.rect(checkpoint.x, checkpoint.y, checkpoint.width, checkpoint.height);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(checkpoint.x + 10, checkpoint.y + 10, checkpoint.width - 20, checkpoint.height - 20);

        if (regPlayer == null) {
            shapeRenderer.setColor(Color.CYAN);
            shapeRenderer.rect(player.x, player.y, player.width, player.height);
        }

        shapeRenderer.end();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        if (status.dropMorte != null) {
            font.setColor(Color.YELLOW);
            font.draw(batch, "RECUPERE: " + status.dropMorte.creditos + " C / " + status.dropMorte.municao + " M",
                status.dropMorte.x - 55f, status.dropMorte.y + 35f);
        }

        if (regBase != null) {
            batch.draw(regBase, base.bounds.x, base.bounds.y, base.bounds.width, base.bounds.height);
        }
        font.setColor(Color.GOLD);
        font.draw(batch, status.temCheckpoint && "LUA".equals(status.checkpointFase) ? "CHECKPOINT SALVO" : "CHECKPOINT [E]", checkpoint.x - 10, checkpoint.y + checkpoint.height + 25);

        if (regPlayer != null) {
            batch.draw(regPlayer, player.x, player.y, player.width, player.height);
        }

        font.setColor(Color.WHITE);
        font.draw(batch, "ESTUFA", estufa.x + 40, estufa.y - 10);
        font.draw(batch, "GERADOR", energia.x + 30, energia.y - 10);
        font.draw(batch, "USINA", extracao.x + 45, extracao.y - 10);
        font.draw(batch, "ANTENA", comunicacao.x + 40, comunicacao.y - 10);
        font.draw(batch, "BASE (E)", base.bounds.x + 25, base.bounds.y - 10);
        font.setColor(new Color(0.85f, 0.65f, 1f, 1f));
        font.draw(batch, "BANCADA (E)", bancada.x + 5, bancada.y - 10);

        font.setColor(Color.GOLD);
        font.draw(batch, "LOJISTA", lojista.x + 5, lojista.y - 10);
        if (player.overlaps(lojista)) {
            font.draw(batch, "E - LOJA", lojista.x - 5, lojista.y + lojista.height + 22);
        }

        for (Item item : itens) {
            font.setColor(Color.WHITE);
            font.draw(batch, item.type.name(), item.bounds.x - 10, item.bounds.y - 8);
        }

        if (bossLua != null && bossLua.ativo) {
            font.setColor(bossLua.getCor());
            font.draw(batch, "GUARDIAO DA CRATERA", bossLua.bounds.x - 20, bossLua.bounds.y + bossLua.bounds.height + 45);
        }

        font.setColor(portalAberto ? Color.ORANGE : Color.GRAY);
        String txtPortal = portalAberto ? "PORTAL MARTE" : "CRATERA BLOQUEADA";
        font.draw(batch, txtPortal, portalMarte.bounds.x - 20, portalMarte.bounds.y - 15);

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
