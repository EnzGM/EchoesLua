package com.Echoes.Jogo.Screen;


import com.Echoes.Jogo.Entities.Base;
import com.Echoes.Jogo.Entities.Item;
import com.Echoes.Jogo.Entities.ItemType;
import com.Echoes.Jogo.Entities.PlayerStatus;
import com.Echoes.Jogo.Main;
import com.Echoes.Jogo.Managers.GameAssets;
import com.Echoes.Jogo.Managers.ParticleManager;
import com.Echoes.Jogo.Ui.Hud;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.Echoes.Jogo.Entities.Portal;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class LunarScreen implements Screen {

    private com.badlogic.gdx.graphics.Texture background;

    public static final float WORLD_WIDTH = 1280;
    public static final float WORLD_HEIGHT = 720;

    private final Main game; // referência pro Game principal, usada pra trocar de tela

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

    private GameAssets assets;
    private ParticleManager particleManager;
    private Hud hud;

    // Regiões de sprite (ficam null se o atlas ainda não tiver sido adicionado)
    private TextureAtlas.AtlasRegion playerRegion;
    private TextureAtlas.AtlasRegion baseRegion;
    private final Map<ItemType, TextureAtlas.AtlasRegion> itemRegions = new EnumMap<>(ItemType.class);

    private float timerPoeira = 0f; // controla o intervalo entre partículas de poeira
    private boolean entrarEmMarte = false;

    public LunarScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        camera.position.set(WORLD_WIDTH / 2f, WORLD_HEIGHT / 2f, 0);

        hudCamera = new OrthographicCamera();
        hudCamera.setToOrtho(false, WORLD_WIDTH, WORLD_HEIGHT);

        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        font = new BitmapFont();

        // Carrega o fundo. Se o arquivo não existir ainda, background fica null e o jogo
        // usa a cor sólida de sempre (sem quebrar nada).
        if (Gdx.files.internal("background.png").exists()) {
            background = new com.badlogic.gdx.graphics.Texture(Gdx.files.internal("background.png"));
            background.setWrap(com.badlogic.gdx.graphics.Texture.TextureWrap.Repeat,
                com.badlogic.gdx.graphics.Texture.TextureWrap.Repeat);
        }

        // Habilita transparência — necessário pro fade-out das partículas e o painel do HUD
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        player = new Rectangle(WORLD_WIDTH / 2f - 32, WORLD_HEIGHT / 2f - 32, 64, 64);
        base = new Base(60, 60, 160, 160);
        portal = new Portal(1150, 60, 100, 100); // canto da base, ajuste como quiser

        itens = new ArrayList<>();
        itens.add(new Item(400, 500, ItemType.OXIGENIO));
        itens.add(new Item(900, 200, ItemType.OXIGENIO));
        itens.add(new Item(600, 600, ItemType.COMIDA));
        itens.add(new Item(1100, 500, ItemType.COMIDA));
        itens.add(new Item(300, 300, ItemType.GELO));
        itens.add(new Item(1000, 650, ItemType.GELO));

        status = new PlayerStatus();

        assets = new GameAssets();
        assets.carregar(); // tenta achar lunar.atlas; se não existir, tudo bem
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
            updateCamera();
            checkColisoes(delta);
            status.consumirOxigenio(delta);
            particleManager.update(delta);
        }

        // --- Verifica transições de tela ANTES de desenhar qualquer coisa ---
        // (evita usar batch/shapeRenderer depois de dispose(), que crasha)

        if (status.missaoFalhou) {
            game.setScreen(new GameOverScreen(game));
            dispose();
            return; // sai imediatamente, não desenha mais nada nessa tela
        }
        if (entrarEmMarte) {
            game.setScreen(new MarsScreen(game));
            dispose();
            return; // sai imediatamente — nada mais tenta usar o batch destruído
        }

        //if (checkVitoria()) {
        //    game.setScreen(new VictoryScreen(game));
        //    dispose();
        //    return; // idem
        //}

        Gdx.gl.glClearColor(0.08f, 0.09f, 0.12f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        desenharMundo();
        hud.render(shapeRenderer, batch, font, hudCamera, status, WORLD_HEIGHT);
    }

    private void desenharMundo() {
        viewport.apply();
        camera.update();

        if (background != null) {
            batch.setProjectionMatrix(camera.combined);
            batch.begin();
            // TextureRegion com repetição: a textura se repete lado a lado cobrindo o mundo todo,
            // em vez de esticar uma imagem só (fica mais nítido)
            com.badlogic.gdx.graphics.g2d.TextureRegion regiaoFundo =
                new com.badlogic.gdx.graphics.g2d.TextureRegion(background, 0, 0,
                    (int) WORLD_WIDTH, (int) WORLD_HEIGHT);
            batch.draw(regiaoFundo, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);
            batch.end();
        }

        // --- Camada 1: retângulos (fallback pra quem ainda não tem sprite pra aquela região) ---
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        if (baseRegion == null) {
            shapeRenderer.setColor(status.inventarioGelo > 0 ? Color.YELLOW : Color.BLUE);
            shapeRenderer.rect(base.bounds.x, base.bounds.y, base.bounds.width, base.bounds.height);
        }
        shapeRenderer.setColor(portal.ativo ? Color.MAGENTA : Color.DARK_GRAY);
        shapeRenderer.rect(portal.bounds.x, portal.bounds.y, portal.bounds.width, portal.bounds.height);

        for (Item item : itens) {
            if (item.coletado) continue;
            if (itemRegions.get(item.type) != null) continue; // vai virar sprite mais abaixo

            switch (item.type) {
                case OXIGENIO: shapeRenderer.setColor(Color.CYAN); break;
                case COMIDA: shapeRenderer.setColor(Color.GREEN); break;
                case GELO: shapeRenderer.setColor(Color.LIGHT_GRAY); break;
            }
            shapeRenderer.rect(item.bounds.x, item.bounds.y, item.bounds.width, item.bounds.height);
        }

        if (playerRegion == null) {
            shapeRenderer.setColor(Color.WHITE);
            shapeRenderer.rect(player.x, player.y, player.width, player.height);
        }

        shapeRenderer.end();

        // --- Camada 2: partículas ---
        particleManager.render(shapeRenderer, camera);

        // --- Camada 3: sprites (só desenha o que tiver região carregada) ---
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

        batch.end();
    }

    private void handleInput(float delta) {
        float dx = 0, dy = 0;

        if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP)) dy += 1;
        if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN)) dy -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) dx -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) dx += 1;

        player.x += dx * playerSpeed * delta;
        player.y += dy * playerSpeed * delta;

        player.x = Math.max(0, Math.min(player.x, WORLD_WIDTH - player.width));
        player.y = Math.max(0, Math.min(player.y, WORLD_HEIGHT - player.height));

        // Spawna poeira só enquanto anda, e não em todo frame (senão vira uma nuvem só)
        if ((dx != 0 || dy != 0)) {
            timerPoeira -= delta;
            if (timerPoeira <= 0f) {
                particleManager.spawnPoeira(player.x + player.width / 2f, player.y);
                timerPoeira = 0.08f;
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.E) && player.overlaps(base.bounds)) {
            status.processarGelo();
        }
        portal.ativo = checkVitoria();

        if (portal.ativo && Gdx.input.isKeyJustPressed(Input.Keys.E) && player.overlaps(portal.bounds)) {
            entrarEmMarte = true;
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
                particleManager.spawnColeta(item.bounds.x, item.bounds.y); // feedback visual da coleta

                switch (item.type) {
                    case OXIGENIO: status.oxigenio = Math.min(100f, status.oxigenio + 20f); break;
                    case COMIDA: status.comida++; break;
                    case GELO: status.inventarioGelo++; break;
                }
            }
        }
    }

    /** Vitória: todos os itens coletados e todo o gelo já processado (pelo menos uma vez). */
    private boolean checkVitoria() {
        boolean todosColetados = true;
        for (Item item : itens) {
            if (!item.coletado) { todosColetados = false; break; }
        }
        return todosColetados && status.inventarioGelo == 0 && status.agua > 0;
    }

    private void updateCamera() {
        camera.position.set(player.x + player.width / 2f, player.y + player.height / 2f, 0);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

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
        if (background != null) background.dispose(); // adiciona essa linha
    }
}
