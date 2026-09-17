package com.Echoes.Jogo.Screen;

import com.Echoes.Jogo.Entities.Base;
import com.Echoes.Jogo.Entities.BossLua;
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

    private OrthographicCamera camera;
    private OrthographicCamera hudCamera;
    private Viewport viewport;

    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;

    // Assets (Aula 07)
    private GameAssets assets;
    private TextureAtlas.AtlasRegion regPlayer;
    private TextureAtlas.AtlasRegion regBase;

    private Rectangle player;
    private float playerSpeed = 320f; // Aumentado (era 200f)

    // Estações para consertar
    private Rectangle estufa, energia, extracao, comunicacao;

    // Base: onde a arma é craftada apertando E
    private Base base;

    // Itens espalhados pelo mapa: 4 peças de reator + 3 peças de arma
    private List<Item> itens;

    // Obstáculos (pedras) — bloqueiam o caminho
    private List<Rectangle> obstaculos;

    // Inimigos que perseguem o jogador na Lua
    private List<Inimigo> inimigos;
    private List<Projectile> projeteisInimigos; // exigido pela assinatura de Inimigo.update
    private List<Projectile> projeteisPlayer;   // tiros do jogador (só funciona com a arma craftada)

    // ITEM 15: Boss da Lua — só existe depois que MissionState.luaMissoesOk(status) vira true.
    // Guardamos a referência separada (além de estar dentro de "inimigos") só pra
    // conseguirmos identificar a morte DELE especificamente e desenhar a barra de vida.
    private BossLua bossLua;

    // Portal para Marte
    private Portal portalMarte;
    private boolean portalAberto = false;
    private String textoMissao = "Colete as pecas e repare as 4 estacoes da Lua!";

    private ParticleManager particleManager;
    private Hud hud;
    private DialogueSystem dialogueSystem;

    // Trava usada pra nao desenhar com recursos ja descartados no frame da troca de tela
    private boolean trocandoTela = false;

    public LunarScreen(Main game, PlayerStatus status) {
        this.game = game;
        this.status = (status != null) ? status : new PlayerStatus();
        this.status.faseAtual = "LUA";
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
        regBase = assets.getRegion("base");

        player = new Rectangle(WORLD_WIDTH / 2f, WORLD_HEIGHT / 2f, 64, 64);

        estufa = new Rectangle(400, 1000, 150, 150);
        energia = new Rectangle(2000, 1000, 150, 150);
        extracao = new Rectangle(400, 300, 150, 150);
        comunicacao = new Rectangle(2000, 300, 150, 150);

        base = new Base(WORLD_WIDTH / 2f - 75, WORLD_HEIGHT / 2f - 260, 150, 150);

        itens = new ArrayList<>();
        itens.add(new Item(700, 850, ItemType.PECA_ESTUFA));
        itens.add(new Item(1850, 850, ItemType.PECA_GERADOR));
        itens.add(new Item(700, 450, ItemType.PECA_USINA));
        itens.add(new Item(1850, 450, ItemType.PECA_ANTENA));
        itens.add(new Item(1280, 950, ItemType.ARMA_PARTE_A)); // Corrigido: estava em (1280,1250), em cima do portal
        itens.add(new Item(200, 700, ItemType.ARMA_PARTE_B));
        itens.add(new Item(2300, 700, ItemType.ARMA_PARTE_C));

        // Pedras: bloqueiam a passagem, dá pra desviar (longe do ponto de spawn do jogador)
        obstaculos = new ArrayList<>();
        obstaculos.add(new Rectangle(750, 950, 90, 90));
        obstaculos.add(new Rectangle(1900, 950, 90, 90));
        obstaculos.add(new Rectangle(750, 550, 90, 90));
        obstaculos.add(new Rectangle(1900, 550, 90, 90));
        obstaculos.add(new Rectangle(1500, 1050, 100, 100));

        inimigos = new ArrayList<>();
        inimigos.add(new Inimigo(900, 1150, Inimigo.TipoInimigo.RAPIDO));
        inimigos.add(new Inimigo(1650, 1150, Inimigo.TipoInimigo.NORMAL));
        inimigos.add(new Inimigo(1280, 220, Inimigo.TipoInimigo.NORMAL)); // Corrigido: estava em (1280,650), quase em cima do player
        projeteisInimigos = new ArrayList<>();
        projeteisPlayer = new ArrayList<>();

        // ITEM 15: o boss ainda não existe ao entrar na fase — só spawna quando
        // as missões da Lua forem concluídas (ver checkBossLua()).
        bossLua = null;

        portalMarte = new Portal(WORLD_WIDTH / 2f - 45, WORLD_HEIGHT - 200, 90, 90);
        portalMarte.ativo = false;

        particleManager = new ParticleManager();
        hud = new Hud();
        missao = new MissionState();

        String[] falasLua = new String[]{
            "ESTACAO DE SUPORTE: Saudacoes, precisamos que voce conserte as estacoes, a arma, e va salvar Marte!"
        };
        dialogueSystem = new DialogueSystem(falasLua);

        SaveManager.salvarJogo(status, missao); // Checkpoint ao entrar na fase
    }

    @Override
    public void render(float delta) {
        inventoryUI.update();
        if (dialogueSystem.ativo) {
            dialogueSystem.update();
        } else {
            handleInput(delta);
            checkColetaItens();
            checkReparos();
            checkBossLua();
            updateInimigos(delta);
            updateProjeteisPlayer(delta);
            checkColisaoInimigos(delta);
            checkPortal();
            atualizarTextoMissao();
            updateCamera();
            particleManager.update(delta);
            status.atualizarCombate(delta); // essencial pra poder atirar mais de uma vez

            status.oxigenio -= 1.5f * delta;
            if (status.oxigenio <= 0) {
                status.oxigenio = 0;
                status.missaoFalhou = true;
            }
        }

        if (status.missaoFalhou) {
            game.setScreen(new GameOverScreen(game));
            dispose();
            return;
        }

        // A troca de tela foi agendada (portal usado) — para aqui, sem desenhar mais nada
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
        // hud.render(...)
// dialogueSystem.render(...)

        inventoryUI.render(shapeRenderer, batch, font, hudCamera, status); // ou de onde vem o seu PlayerStatus
    }

    private void handleInput(float delta) {
        float dx = 0, dy = 0;
        if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP)) dy += 1;
        if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN)) dy -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) dx -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) dx += 1;

        moverJogador(dx * playerSpeed * delta, dy * playerSpeed * delta);

        // Craft da arma na base, apertando E
        if (Gdx.input.isKeyJustPressed(Input.Keys.E) && player.overlaps(base.bounds)) {
            if (!status.armaCraftada && status.colArmaParteA && status.colArmaParteB && status.colArmaParteC) {
                status.armaCraftada = true;
                particleManager.spawnColeta(base.bounds.x + 75, base.bounds.y + 75);
            }
        }

        // Tiro (só funciona depois da arma craftada)
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            if (status.podeAtirar()) {
                Vector2 mouseWorld = viewport.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
                float startX = player.x + player.width / 2f;
                float startY = player.y + player.height / 2f;
                projeteisPlayer.add(new Projectile(startX, startY, mouseWorld.x, mouseWorld.y, 600f, 500f));
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.F5)) {
            SaveManager.salvarJogo(status, missao);
        }
    }

    /** Move o jogador eixo a eixo, sem atravessar obstáculos. */
    private void moverJogador(float moveX, float moveY) {
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

                switch (item.type) {
                    case PECA_ESTUFA:  status.colPecaEstufa = true;  status.pecaEstufa++;  break;
                    case PECA_GERADOR: status.colPecaGerador = true; status.pecaGerador++; break;
                    case PECA_USINA:   status.colPecaUsina = true;   status.pecaUsina++;   break;
                    case PECA_ANTENA:  status.colPecaAntena = true;  status.pecaAntena++;  break;
                    case ARMA_PARTE_A: status.colArmaParteA = true;  status.armaParteA++;  break;
                    case ARMA_PARTE_B: status.colArmaParteB = true;  status.armaParteB++;  break;
                    case ARMA_PARTE_C: status.colArmaParteC = true;  status.armaParteC++;  break;
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

    /**
     * ITEM 15: spawna o Boss da Lua assim que MissionState.luaMissoesOk(status)
     * vira true. Só roda uma vez (bossLua fica != null depois do primeiro spawn).
     */
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
                    ini.tomarDano(50);
                    particleManager.spawnColeta(ini.bounds.x, ini.bounds.y);
                    if (!ini.ativo) {
                        inimigos.remove(j);
                        // ITEM 15: se quem morreu foi o Boss da Lua, entrega a chave.
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

    /** ITEM 15: recompensa por derrotar o Boss da Lua. */
    private void onBossLuaDerrotado() {
        status.inventario.add("CHAVE_LUA");
        particleManager.spawnColeta(
            bossLua.bounds.x + bossLua.bounds.width / 2f,
            bossLua.bounds.y + bossLua.bounds.height / 2f
        );
    }

    /** Dano por contato dos inimigos perseguidores. */
    private void checkColisaoInimigos(float delta) {
        for (Inimigo ini : inimigos) {
            if (ini.ativo && player.overlaps(ini.bounds)) {
                status.hp -= ini.danoContato * delta;
                if (status.hp <= 0) {
                    status.hp = 0;
                    status.missaoFalhou = true;
                }
            }
        }
    }

    /** Portal só abre quando os 4 sistemas estiverem reparados E a arma craftada. */
    private void checkPortal() {
        if (!portalAberto && status.todosReparosConcluidos() && status.armaCraftada) {
            portalAberto = true;
            portalMarte.ativo = true;
        }

        if (portalAberto && !trocandoTela && player.overlaps(portalMarte.bounds)) {
            status.faseAtual = "MARTE";
            SaveManager.salvarJogo(status, missao);
            game.setScreen(new MarsScreen(game, status));
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

        // Obstáculos (pedras)
        shapeRenderer.setColor(Color.DARK_GRAY);
        for (Rectangle o : obstaculos) {
            shapeRenderer.rect(o.x, o.y, o.width, o.height);
        }

        for (Item item : itens) {
            shapeRenderer.setColor(Color.GOLD);
            shapeRenderer.rect(item.bounds.x, item.bounds.y, item.bounds.width, item.bounds.height);
        }

        // Inimigos (o BossLua também é desenhado aqui, pois está dentro de "inimigos")
        for (Inimigo ini : inimigos) {
            shapeRenderer.setColor(ini.getCor());
            shapeRenderer.rect(ini.bounds.x, ini.bounds.y, ini.bounds.width, ini.bounds.height);
        }

        // ITEM 15: barra de vida do Boss da Lua, enquanto ele estiver ativo
        if (bossLua != null && bossLua.ativo) {
            float barraLargura = bossLua.bounds.width;
            shapeRenderer.setColor(Color.RED);
            shapeRenderer.rect(bossLua.bounds.x, bossLua.bounds.y + bossLua.bounds.height + 12, barraLargura, 10);
            shapeRenderer.setColor(Color.GREEN);
            shapeRenderer.rect(bossLua.bounds.x, bossLua.bounds.y + bossLua.bounds.height + 12,
                barraLargura * (bossLua.hp / BossLua.HP_INICIAL), 10);
        }

        // Tiros do jogador
        for (Projectile p : projeteisPlayer) {
            shapeRenderer.setColor(Color.CYAN);
            shapeRenderer.rect(p.x - 4, p.y - 4, 8, 8);
        }

        shapeRenderer.setColor(portalAberto ? Color.ORANGE : Color.DARK_GRAY);
        shapeRenderer.rect(portalMarte.bounds.x, portalMarte.bounds.y, portalMarte.bounds.width, portalMarte.bounds.height);

        if (regPlayer == null) {
            shapeRenderer.setColor(Color.CYAN);
            shapeRenderer.rect(player.x, player.y, player.width, player.height);
        }

        shapeRenderer.end();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        if (regBase != null) {
            batch.draw(regBase, base.bounds.x, base.bounds.y, base.bounds.width, base.bounds.height);
        }
        if (regPlayer != null) {
            batch.draw(regPlayer, player.x, player.y, player.width, player.height);
        }

        font.setColor(Color.WHITE);
        font.draw(batch, "ESTUFA", estufa.x + 40, estufa.y - 10);
        font.draw(batch, "ENERGIA", energia.x + 40, energia.y - 10);
        font.draw(batch, "EXTRACAO", extracao.x + 30, extracao.y - 10);
        font.draw(batch, "COMUNICACAO", comunicacao.x + 20, comunicacao.y - 10);
        font.draw(batch, "BASE (E)", base.bounds.x + 25, base.bounds.y - 10);

        for (Item item : itens) {
            font.draw(batch, item.type.name(), item.bounds.x - 10, item.bounds.y - 8);
        }

        // ITEM 15: nome do boss acima dele, enquanto ativo
        if (bossLua != null && bossLua.ativo) {
            font.setColor(bossLua.getCor());
            font.draw(batch, "GUARDIAO DA CRATERA", bossLua.bounds.x - 20, bossLua.bounds.y + bossLua.bounds.height + 45);
        }

        // ITEM 15: a cratera (portal de Marte) mostra BLOQUEADO enquanto não estiver aberta
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
