package com.Echoes.Jogo.Screen;

import com.Echoes.Jogo.Entities.Base;
import com.Echoes.Jogo.Entities.Item;
import com.Echoes.Jogo.Entities.ItemType;
import com.Echoes.Jogo.Entities.PlayerStatus;
import com.Echoes.Jogo.Entities.Portal;
import com.Echoes.Jogo.Main;
import com.Echoes.Jogo.Managers.GameAssets;
import com.Echoes.Jogo.Managers.ParticleManager;
import com.Echoes.Jogo.Ui.Hud;
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

    private Rectangle player;
    private float playerSpeed = 220f;

    private Base base;
    private List<Item> itens;
    private PlayerStatus status;
    private Portal portal;

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

    private static class InimigoLunar {
        public Rectangle bounds;
        public float speed = 90f;
        public int hp = 100; // HP para permitir destruição
        public InimigoLunar(float x, float y) {
            this.bounds = new Rectangle(x, y, 52, 52);
        }
    }
    private List<InimigoLunar> inimigos;

    private GameAssets assets;
    private ParticleManager particleManager;
    private Hud hud;

    private TextureAtlas.AtlasRegion playerRegion;
    private TextureAtlas.AtlasRegion baseRegion;
    private final Map<ItemType, TextureAtlas.AtlasRegion> itemRegions = new EnumMap<>(ItemType.class);

    private float timerPoeira = 0f;
    private boolean entrarEmMarte = false;

    public LunarScreen(Main game) {
        this.game = game;
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

        player = new Rectangle(WORLD_WIDTH / 2f - 32, WORLD_HEIGHT / 2f - 32, 64, 64);
        base = new Base(100, 100, 180, 180);
        portal = new Portal(2350, 1200, 110, 110);

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

        inimigos = new ArrayList<>();
        inimigos.add(new InimigoLunar(600, 800));
        inimigos.add(new InimigoLunar(1500, 300));
        inimigos.add(new InimigoLunar(2100, 1000));

        status = new PlayerStatus();

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
        if (!status.missaoFalhou) {
            handleInput(delta);
            updateInimigos(delta);
            updateCamera();
            checkColisoes(delta);
            status.consumirOxigenio(delta);
            particleManager.update(delta);
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
        hud.render(shapeRenderer, batch, font, hudCamera, status, 720);
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

        shapeRenderer.setColor(Color.PURPLE);
        for (InimigoLunar ini : inimigos) {
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
            }
            font.draw(batch, texto, item.bounds.x - 5, item.bounds.y + item.bounds.height + 15);
        }

        font.setColor(Color.RED);
        for (InimigoLunar ini : inimigos) {
            font.draw(batch, "PERIGO!", ini.bounds.x - 10, ini.bounds.y + ini.bounds.height + 15);
        }

        // Feedback textual do Portal (Etapa 1)
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

        // Combate na fase lunar (Etapa 4 e 5)
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            float attackRange = 120f;
            float playerCx = player.x + player.width / 2f;
            float playerCy = player.y + player.height / 2f;

            for (int i = inimigos.size() - 1; i >= 0; i--) {
                InimigoLunar ini = inimigos.get(i);
                float iniCx = ini.bounds.x + ini.bounds.width / 2f;
                float iniCy = ini.bounds.y + ini.bounds.height / 2f;

                if (Vector2.dst(playerCx, playerCy, iniCx, iniCy) <= attackRange) {
                    if (status.armaCraftada) {
                        // Destrói o inimigo
                        particleManager.spawnColeta(ini.bounds.x, ini.bounds.y);
                        inimigos.remove(i);
                    } else {
                        // Empurra o inimigo temporariamente
                        Vector2 direcao = new Vector2(iniCx - playerCx, iniCy - playerCy).nor();
                        ini.bounds.x += direcao.x * 150f;
                        ini.bounds.y += direcao.y * 150f;
                    }
                }
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            if (player.overlaps(base.bounds)) {
                status.processarGelo();

                // Craftar a Arma na base Lunar (Etapa 5)
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

            if (portal.ativo && player.overlaps(portal.bounds)) {
                entrarEmMarte = true;
            }
        }

        portal.ativo = checkPortalLiberado();
    }

    private void updateInimigos(float delta) {
        float centroPlayerX = player.x + player.width / 2f;
        float centroPlayerY = player.y + player.height / 2f;

        for (InimigoLunar inimigo : inimigos) {
            float centroInimigoX = inimigo.bounds.x + inimigo.bounds.width / 2f;
            float centroInimigoY = inimigo.bounds.y + inimigo.bounds.height / 2f;

            Vector2 direcao = new Vector2(centroPlayerX - centroInimigoX, centroPlayerY - centroInimigoY).nor();

            inimigo.bounds.x += direcao.x * inimigo.speed * delta;
            inimigo.bounds.y += direcao.y * inimigo.speed * delta;
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
                    case PECA_ANTENA: status.pecaAntena++; break;
                    case PECA_GERADOR: status.pecaGerador++; break;
                    case PECA_USINA: status.pecaUsina++; break;
                    case PECA_ESTUFA: status.pecaEstufa++; break;
                    case ARMA_PARTE_A: status.armaParteA++; break;
                    case ARMA_PARTE_B: status.armaParteB++; break;
                    case ARMA_PARTE_C: status.armaParteC++; break;
                }
            }
        }

        for (InimigoLunar inimigo : inimigos) {
            if (player.overlaps(inimigo.bounds)) {
                status.oxigenio -= 35f * delta;
                particleManager.spawnPoeira(player.x + player.width/2f, player.y + player.height/2f);

                // Empurra o inimigo para não drenar direto o oxigênio sem o jogador reagir
                inimigo.bounds.x += (inimigo.bounds.x - player.x) * 2f;
                inimigo.bounds.y += (inimigo.bounds.y - player.y) * 2f;

                if (status.oxigenio <= 0f) {
                    status.oxigenio = 0f;
                    status.missaoFalhou = true;
                }
            }
        }
    }

    private boolean checkPortalLiberado() {
        boolean estacoesOks = status.comunicacaoReparada && status.energiaReparada && status.extracaoReparada && status.estufaReparada;
        boolean armaOk = status.armaCraftada; // Condição: Arma já tem que estar craftada
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
