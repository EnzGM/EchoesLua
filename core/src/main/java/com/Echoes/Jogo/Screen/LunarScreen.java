package com.Echoes.Jogo.Screen;

import com.Echoes.Jogo.Entities.Base;
import com.Echoes.Jogo.Entities.Inimigo;
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
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class LunarScreen implements Screen {

    private com.badlogic.gdx.graphics.Texture background;

    public static final float WORLD_WIDTH = 2560;
    public static final float WORLD_HEIGHT = 1440;

    private final Main game;

    private OrthographicCamera camera;
    private OrthographicCamera hudCamera;
    private Viewport viewport;

    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;
    private final GlyphLayout layoutPausa = new GlyphLayout();

    private Rectangle player;
    private float playerSpeed = 220f;

    private Base base;
    private List<Item> itens;
    private PlayerStatus status;
    private Portal portal;
    private MissionState missao;

    // Botão de pausa (ESC)
    private boolean pausado = false;

    private static class ObstaculoPedra {
        public Rectangle bounds;
        public ObstaculoPedra(float x, float y, float w, float h) {
            this.bounds = new Rectangle(x, y, w, h);
        }
    }
    private List<ObstaculoPedra> pedras;

    public enum TipoEstacao { ANTENA, GERADOR, USINA, ESTUFA }
    private static class Estacao {
        public Rectangle bounds;
        public TipoEstacao tipo;
        public boolean reparada = false;
        public Estacao(float x, float y, TipoEstacao tipo) {
            this.bounds = new Rectangle(x, y, 90, 90);
            this.tipo = tipo;
        }
    }
    private List<Estacao> estacoes;

    // MELHORIA 3: agora usa a entidade Inimigo de verdade (patrulha/persegue + drop)
    private List<Inimigo> inimigos;

    private GameAssets assets;
    private ParticleManager particleManager;
    private Hud hud;

    private TextureAtlas.AtlasRegion playerRegion;
    private TextureAtlas.AtlasRegion baseRegion;
    private final Map<ItemType, TextureAtlas.AtlasRegion> itemRegions = new EnumMap<>(ItemType.class);

    private float timerPoeira = 0f;
    private boolean entrarEmMarte = false;

    public LunarScreen(Main game, PlayerStatus status) {
        this.game = game;
        this.status = (status != null) ? status : new PlayerStatus();
    }

    @Override
    public void show() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(1280, 720, camera);
        camera.position.set(WORLD_WIDTH / 2f, WORLD_HEIGHT / 2f, 0);

        hudCamera = new OrthographicCamera();
        hudCamera.setToOrtho(false, 1280, 720);

        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        font = new BitmapFont();

        if (Gdx.files.internal("background.png").exists()) {
            background = new com.badlogic.gdx.graphics.Texture(Gdx.files.internal("background.png"));
            background.setWrap(com.badlogic.gdx.graphics.Texture.TextureWrap.Repeat,
                com.badlogic.gdx.graphics.Texture.TextureWrap.Repeat);
        }

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        // MELHORIA 6: player nasce na posição do checkpoint (padrão = centro do mapa)
        player = new Rectangle(status.lastLuaX, status.lastLuaY, 64, 64);
        base = new Base(100, 100, 180, 180);
        portal = new Portal(2350, 1200);

        missao = new MissionState();
        missao.setEtapa(status.missaoEtapa);

        pedras = new ArrayList<>();
        pedras.add(new ObstaculoPedra(400, 300, 150, 100));
        pedras.add(new ObstaculoPedra(800, 900, 200, 120));
        pedras.add(new ObstaculoPedra(1400, 400, 120, 250));
        pedras.add(new ObstaculoPedra(1800, 1000, 180, 150));
        pedras.add(new ObstaculoPedra(600, 1200, 140, 100));

        itens = new ArrayList<>();
        itens.add(new Item(700, 500, ItemType.OXIGENIO));
        itens.add(new Item(1800, 400, ItemType.OXIGENIO));
        itens.add(new Item(2200, 200, ItemType.COMIDA));
        itens.add(new Item(1200, 1100, ItemType.COMIDA));
        itens.add(new Item(500, 900, ItemType.GELO));
        itens.add(new Item(2000, 800, ItemType.GELO));

        itens.add(new Item(300, 1100, ItemType.PECA_ANTENA));
        itens.add(new Item(1300, 200, ItemType.PECA_GERADOR));
        itens.add(new Item(1900, 300, ItemType.PECA_USINA));
        itens.add(new Item(2300, 700, ItemType.PECA_ESTUFA));
        itens.add(new Item(1000, 1300, ItemType.ARMA_PARTE_A));
        itens.add(new Item(1500, 1200, ItemType.ARMA_PARTE_B));
        itens.add(new Item(2100, 1300, ItemType.ARMA_PARTE_C));

        estacoes = new ArrayList<>();
        estacoes.add(new Estacao(300, 700, TipoEstacao.ANTENA));
        estacoes.add(new Estacao(900, 200, TipoEstacao.GERADOR));
        estacoes.add(new Estacao(1600, 800, TipoEstacao.USINA));
        estacoes.add(new Estacao(2000, 500, TipoEstacao.ESTUFA));

        // MELHORIA 3: mistura de comportamentos (patrulha e persegue)
        inimigos = new ArrayList<>();
        inimigos.add(new Inimigo(600, 800, Inimigo.TipoIA.PATRULHA));
        inimigos.add(new Inimigo(1500, 300, Inimigo.TipoIA.PERSEGUE));
        inimigos.add(new Inimigo(2100, 1000, Inimigo.TipoIA.PATRULHA));

        assets = new GameAssets();
        assets.carregar();
        playerRegion = assets.getRegion("player");
        baseRegion = assets.getRegion("base");
        itemRegions.put(ItemType.OXIGENIO, assets.getRegion("item_oxigenio"));
        itemRegions.put(ItemType.COMIDA, assets.getRegion("item_comida"));
        itemRegions.put(ItemType.GELO, assets.getRegion("item_gelo"));

        particleManager = new ParticleManager();
        hud = new Hud();
    }

    @Override
    public void render(float delta) {
        // Botão de pausa: ESC alterna entre pausado/rodando
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            pausado = !pausado;
        }

        if (pausado) {
            // Enquanto pausado, o jogo congela. M salva e volta pro menu.
            if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
                SaveManager.salvarJogo(status, missao);
                game.setScreen(new MenuScreen(game));
                dispose();
                return;
            }
        } else if (!status.missaoFalhou) {
            handleInput(delta);
            updateInimigos(delta);
            updateCamera();
            checkColisoes(delta);
            status.consumirOxigenio(delta);
            particleManager.update(delta);

            // MELHORIA 1: Quest Tracker sempre sincronizado com o progresso real
            missao.setEtapa(MissionState.calcularEtapa(status));
            status.missaoEtapa = missao.getEtapa();
        }

        if (status.missaoFalhou) {
            game.setScreen(new GameOverScreen(game));
            dispose();
            return;
        }
        if (entrarEmMarte) {
            game.setScreen(new MarsScreen(game, status));
            dispose();
            return;
        }

        Gdx.gl.glClearColor(0.08f, 0.09f, 0.12f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        desenharMundo();
        hud.render(shapeRenderer, batch, font, hudCamera, status, missao.getAtual(), null, 720);

        if (pausado) {
            desenharPausa();
        }
    }

    /** Botão de pausa: overlay escuro com o texto e as opções de ESC/M. */
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

    private void desenharMundo() {
        viewport.apply();
        camera.update();

        if (background != null) {
            batch.setProjectionMatrix(camera.combined);
            batch.begin();
            com.badlogic.gdx.graphics.g2d.TextureRegion regiaoFundo =
                new com.badlogic.gdx.graphics.g2d.TextureRegion(background, 0, 0,
                    (int) WORLD_WIDTH, (int) WORLD_HEIGHT);
            batch.draw(regiaoFundo, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);
            batch.end();
        }

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        shapeRenderer.setColor(Color.DARK_GRAY);
        for (ObstaculoPedra pedra : pedras) {
            shapeRenderer.rect(pedra.bounds.x, pedra.bounds.y, pedra.bounds.width, pedra.bounds.height);
        }

        if (baseRegion == null) {
            shapeRenderer.setColor(status.inventarioGelo > 0 || (status.armaParteA > 0 && status.armaParteB > 0 && status.armaParteC > 0 && !status.armaCraftada) ? Color.YELLOW : Color.BLUE);
            shapeRenderer.rect(base.bounds.x, base.bounds.y, base.bounds.width, base.bounds.height);
        }

        for (Estacao est : estacoes) {
            shapeRenderer.setColor(est.reparada ? Color.GREEN : Color.FIREBRICK);
            shapeRenderer.rect(est.bounds.x, est.bounds.y, est.bounds.width, est.bounds.height);
        }

        // MELHORIA 3: cor diferente por comportamento
        for (Inimigo ini : inimigos) {
            shapeRenderer.setColor(ini.tipo == Inimigo.TipoIA.PERSEGUE ? Color.SCARLET : Color.PURPLE);
            shapeRenderer.rect(ini.bounds.x, ini.bounds.y, ini.bounds.width, ini.bounds.height);
        }

        shapeRenderer.setColor(portal.ativo ? Color.MAGENTA : Color.DARK_GRAY);
        shapeRenderer.rect(portal.bounds.x, portal.bounds.y, portal.bounds.width, portal.bounds.height);

        for (Item item : itens) {
            if (item.coletado) continue;
            if (itemRegions.get(item.type) != null) continue;

            switch (item.type) {
                case OXIGENIO: shapeRenderer.setColor(Color.CYAN); break;
                case COMIDA: shapeRenderer.setColor(Color.GREEN); break;
                case GELO: shapeRenderer.setColor(Color.LIGHT_GRAY); break;
                case PECA_ANTENA: shapeRenderer.setColor(Color.ORANGE); break;
                case PECA_GERADOR: shapeRenderer.setColor(Color.YELLOW); break;
                case PECA_USINA: shapeRenderer.setColor(Color.BLUE); break;
                case PECA_ESTUFA: shapeRenderer.setColor(Color.FOREST); break;
                case ARMA_PARTE_A:
                case ARMA_PARTE_B:
                case ARMA_PARTE_C: shapeRenderer.setColor(Color.SCARLET); break;
                case MUNICAO: shapeRenderer.setColor(Color.GOLD); break;
            }
            shapeRenderer.rect(item.bounds.x, item.bounds.y, item.bounds.width, item.bounds.height);
        }

        if (playerRegion == null) {
            shapeRenderer.setColor(Color.WHITE);
            shapeRenderer.rect(player.x, player.y, player.width, player.height);
        }

        shapeRenderer.end();

        particleManager.render(shapeRenderer, camera);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        if (baseRegion != null) {
            batch.draw(baseRegion, base.bounds.x, base.bounds.y, base.bounds.width, base.bounds.height);
        }
        for (Item item : itens) {
            if (item.coletado) continue;
            TextureAtlas.AtlasRegion region = itemRegions.get(item.type);
            if (region != null) {
                batch.draw(region, item.bounds.x, item.bounds.y, item.bounds.width, item.bounds.height);
            }
        }
        if (playerRegion != null) {
            batch.draw(playerRegion, player.x, player.y, player.width, player.height);
        }

        font.getData().setScale(1.2f);

        for (ObstaculoPedra pedra : pedras) {
            font.setColor(Color.LIGHT_GRAY);
            font.draw(batch, "ROCHA", pedra.bounds.x + 10, pedra.bounds.y + pedra.bounds.height + 15);
        }

        for (Estacao est : estacoes) {
            font.setColor(est.reparada ? Color.GREEN : Color.WHITE);
            String texto = "";
            switch (est.tipo) {
                case ANTENA: texto = "EST. ANTENA (A)"; break;
                case GERADOR: texto = "EST. GERADOR (G)"; break;
                case USINA: texto = "EST. USINA (U)"; break;
                case ESTUFA: texto = "EST. ESTUFA (E)"; break;
            }
            font.draw(batch, texto, est.bounds.x - 10, est.bounds.y + est.bounds.height + 20);
        }

        font.setColor(Color.WHITE);
        for (Item item : itens) {
            if (item.coletado) continue;
            String texto = "";
            switch (item.type) {
                case PECA_ANTENA: texto = "Peca (A)"; break;
                case PECA_GERADOR: texto = "Peca (G)"; break;
                case PECA_USINA: texto = "Peca (U)"; break;
                case PECA_ESTUFA: texto = "Peca (E)"; break;
                case ARMA_PARTE_A: texto = "Arma 1/3"; break;
                case ARMA_PARTE_B: texto = "Arma 2/3"; break;
                case ARMA_PARTE_C: texto = "Arma 3/3"; break;
                case OXIGENIO: texto = "O2"; break;
                case GELO: texto = "Gelo"; break;
                case COMIDA: texto = "Racao"; break;
                case MUNICAO: texto = "Municao"; break;
            }
            font.draw(batch, texto, item.bounds.x - 5, item.bounds.y + item.bounds.height + 15);
        }

        font.setColor(Color.RED);
        for (Inimigo ini : inimigos) {
            String texto = ini.tipo == Inimigo.TipoIA.PERSEGUE ? "PERSEGUINDO!" : "PATRULHANDO";
            font.draw(batch, texto, ini.bounds.x - 15, ini.bounds.y + ini.bounds.height + 15);
        }

        font.setColor(portal.ativo ? Color.MAGENTA : Color.GRAY);
        String textoPortal = portal.ativo ? "Portal online" : "Portal bloqueado: faltam reparos/arma";
        font.draw(batch, textoPortal, portal.bounds.x - 20, portal.bounds.y + portal.bounds.height + 25);

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
        for (ObstaculoPedra pedra : pedras) {
            if (player.overlaps(pedra.bounds)) {
                player.x -= dx * playerSpeed * delta;
                break;
            }
        }

        player.y += dy * playerSpeed * delta;
        for (ObstaculoPedra pedra : pedras) {
            if (player.overlaps(pedra.bounds)) {
                player.y -= dy * playerSpeed * delta;
                break;
            }
        }

        player.x = Math.max(0, Math.min(player.x, WORLD_WIDTH - player.width));
        player.y = Math.max(0, Math.min(player.y, WORLD_HEIGHT - player.height));

        if ((dx != 0 || dy != 0)) {
            timerPoeira -= delta;
            if (timerPoeira <= 0f) {
                particleManager.spawnPoeira(player.x + player.width / 2f, player.y);
                timerPoeira = 0.08f;
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            float attackRange = 120f;
            float playerCx = player.x + player.width / 2f;
            float playerCy = player.y + player.height / 2f;

            for (int i = inimigos.size() - 1; i >= 0; i--) {
                Inimigo ini = inimigos.get(i);
                float iniCx = ini.bounds.x + ini.bounds.width / 2f;
                float iniCy = ini.bounds.y + ini.bounds.height / 2f;

                if (Vector2.dst(playerCx, playerCy, iniCx, iniCy) <= attackRange) {
                    if (status.armaCraftada) {
                        // Só ataca de verdade se tiver municao disponivel e o cooldown tiver liberado
                        if (status.podeAtirar()) {
                            ini.tomarDano(999); // arma craftada mata de um golpe
                            particleManager.spawnColeta(ini.bounds.x, ini.bounds.y);

                            // MELHORIA 3: drop de item ao morrer
                            itens.add(new Item(ini.bounds.x, ini.bounds.y, ini.getDrop()));

                            inimigos.remove(i);
                            status.inimigosDerrotados++;
                        }
                    } else {
                        Vector2 direcao = new Vector2(iniCx - playerCx, iniCy - playerCy).nor();
                        ini.bounds.x += direcao.x * 150f;
                        ini.bounds.y += direcao.y * 150f;
                        ini.bounds.x = MathUtils.clamp(ini.bounds.x, 0, WORLD_WIDTH - ini.bounds.width);
                        ini.bounds.y = MathUtils.clamp(ini.bounds.y, 0, WORLD_HEIGHT - ini.bounds.height);
                    }
                }
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            if (player.overlaps(base.bounds)) {
                status.processarGelo();

                if (status.armaParteA > 0 && status.armaParteB > 0 && status.armaParteC > 0 && !status.armaCraftada) {
                    status.armaCraftada = true;
                    status.armaParteA--;
                    status.armaParteB--;
                    status.armaParteC--;
                    particleManager.spawnColeta(player.x, player.y);
                }
            }

            for (Estacao est : estacoes) {
                if (!est.reparada && player.overlaps(est.bounds)) {
                    switch (est.tipo) {
                        case ANTENA:
                            if (status.pecaAntena > 0) {
                                status.pecaAntena--; status.comunicacaoReparada = true; est.reparada = true;
                            }
                            break;
                        case GERADOR:
                            if (status.pecaGerador > 0) {
                                status.pecaGerador--; status.energiaReparada = true; est.reparada = true;
                            }
                            break;
                        case USINA:
                            if (status.pecaUsina > 0) {
                                status.pecaUsina--; status.extracaoReparada = true; est.reparada = true;
                            }
                            break;
                        case ESTUFA:
                            if (status.pecaEstufa > 0) {
                                status.pecaEstufa--; status.estufaReparada = true; est.reparada = true;
                            }
                            break;
                    }
                }
            }

            // MELHORIA 6: portal bidirecional com checkpoint + save automatico
            if (portal.ativo && player.overlaps(portal.bounds)) {
                status.lastLuaX = player.x;
                status.lastLuaY = player.y;
                status.faseAtual = "MARTE";
                SaveManager.salvarJogo(status, missao);
                entrarEmMarte = true;
            }
        }

        // MELHORIA 4: salvar manualmente a qualquer momento
        if (Gdx.input.isKeyJustPressed(Input.Keys.F5)) {
            SaveManager.salvarJogo(status, missao);
        }

        portal.ativo = checkPortalLiberado();
    }

    private void updateInimigos(float delta) {
        // MELHORIA 3: cada inimigo cuida do proprio comportamento (patrulha ou persegue)
        for (Inimigo inimigo : inimigos) {
            if (inimigo.ativo) {
                inimigo.update(delta, player);
                // Garante que o inimigo nunca saia dos limites do mapa
                inimigo.bounds.x = MathUtils.clamp(inimigo.bounds.x, 0, WORLD_WIDTH - inimigo.bounds.width);
                inimigo.bounds.y = MathUtils.clamp(inimigo.bounds.y, 0, WORLD_HEIGHT - inimigo.bounds.height);
            }
        }
    }

    private void checkColisoes(float delta) {
        if (player.overlaps(base.bounds)) {
            status.recarregarNaBase(delta);
        }

        for (Item item : itens) {
            if (item.coletado) continue;
            if (player.overlaps(item.bounds)) {
                item.coletado = true;
                particleManager.spawnColeta(item.bounds.x, item.bounds.y);

                switch (item.type) {
                    case OXIGENIO: status.oxigenio = Math.min(100f, status.oxigenio + 20f); break;
                    case COMIDA: status.comida++; break;
                    case GELO: status.inventarioGelo++; break;
                    case PECA_ANTENA: status.pecaAntena++; status.colPecaAntena = true; break;
                    case PECA_GERADOR: status.pecaGerador++; status.colPecaGerador = true; break;
                    case PECA_USINA: status.pecaUsina++; status.colPecaUsina = true; break;
                    case PECA_ESTUFA: status.pecaEstufa++; status.colPecaEstufa = true; break;
                    case ARMA_PARTE_A: status.armaParteA++; status.colArmaParteA = true; break;
                    case ARMA_PARTE_B: status.armaParteB++; status.colArmaParteB = true; break;
                    case ARMA_PARTE_C: status.armaParteC++; status.colArmaParteC = true; break;
                    case MUNICAO: status.municao += 5; break;
                }
            }
        }

        // CORRIGIDO (bug da missão errada): marca que cada peça já foi coletada ao menos
        // uma vez, mesmo que o jogador já tenha usado ela num reparo/craft antes de
        // terminar de coletar as outras. Antes disso só funcionava se ele pegasse as
        // 7 peças de uma vez, sem reparar nada no meio do caminho.
        if (!status.pecasColetadas &&
            status.colPecaAntena && status.colPecaGerador && status.colPecaUsina && status.colPecaEstufa &&
            status.colArmaParteA && status.colArmaParteB && status.colArmaParteC) {
            status.pecasColetadas = true;
        }

        for (Inimigo inimigo : inimigos) {
            if (inimigo.ativo && player.overlaps(inimigo.bounds)) {
                status.hp -= 35f * delta;
                particleManager.spawnPoeira(player.x + player.width/2f, player.y + player.height/2f);

                // CORRIGIDO (bug dos inimigos de patrulha): o empurrão antigo não era
                // normalizado e crescia a cada frame, chutando o inimigo pra fora do
                // mapa quase instantaneamente e quebrando a rota de patrulha dele.
                Vector2 direcaoEmpurrao = new Vector2(inimigo.bounds.x - player.x, inimigo.bounds.y - player.y);
                if (direcaoEmpurrao.len2() > 0.0001f) {
                    direcaoEmpurrao.nor();
                } else {
                    direcaoEmpurrao.set(1f, 0f);
                }
                float velocidadeEmpurrao = 260f;
                inimigo.bounds.x += direcaoEmpurrao.x * velocidadeEmpurrao * delta;
                inimigo.bounds.y += direcaoEmpurrao.y * velocidadeEmpurrao * delta;

                inimigo.bounds.x = MathUtils.clamp(inimigo.bounds.x, 0, WORLD_WIDTH - inimigo.bounds.width);
                inimigo.bounds.y = MathUtils.clamp(inimigo.bounds.y, 0, WORLD_HEIGHT - inimigo.bounds.height);

                if (status.hp <= 0f) {
                    status.hp = 0f;
                    status.missaoFalhou = true;
                }
            }
        }
    }

    private boolean checkPortalLiberado() {
        boolean estacoesOks = status.comunicacaoReparada && status.energiaReparada && status.extracaoReparada && status.estufaReparada;
        boolean armaOk = status.armaCraftada;
        return estacoesOks && armaOk;
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
        assets.dispose();
        particleManager.clear();
        if (background != null) background.dispose();
    }
}
