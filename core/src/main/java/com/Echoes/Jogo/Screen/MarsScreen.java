package com.Echoes.Jogo.Screen;

import com.Echoes.Jogo.Entities.*;
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
    private TextureAtlas.AtlasRegion regItemOxigenio;

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

    // ITEM 16: Boss de Marte — só existe depois que MissionState.marteMissoesOk(status)
    // vira true (estufa reparada na Lua + as 3 waves vencidas). Guardamos a referência
    // separada (além de estar dentro de "inimigos") pra identificar a morte DELE
    // especificamente e desenhar a barra de vida/nome, igual o BossLua na LunarScreen.
    private BossMarte bossMarte;

    private ParticleManager particleManager;
    private Hud hud;
    private MissionState missao;
    private DialogueSystem dialogueSystem;

    private boolean trocandoTela = false;

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
        regItemOxigenio = assets.getItemRegion("oxigenio");

        player = new Rectangle(WORLD_WIDTH / 2f, 200, 54, 54);

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

        // ITEM 16: o boss ainda não existe ao entrar na fase — só spawna quando
        // MissionState.marteMissoesOk(status) for true (ver checkBossMarte()).
        bossMarte = null;

        // Se o jogador ja tinha derrotado o boss numa sessao anterior (chave ja
        // no inventario), o portal ja comeca aberto — sem isso o checkBossMarte()
        // ressuscitaria o boss so por causa de um reload/F5.
        portalAberto = status.inventario.tem("CHAVE_MARTE");
        portalTita.ativo = portalAberto;

        particleManager = new ParticleManager();
        hud = new Hud();
        missao = new MissionState();

        // Retoma da wave salva em vez de sempre voltar pra wave 1
        waveAtual = Math.max(1, Math.min(status.marteWaveAtual, MAX_WAVES));
        status.marteWaveAtual = waveAtual;

        String[] falasMarte = new String[]{
            "ESTACAO DE SUPORTE: Mate eles e depois vá salvar Titã!"
        };
        dialogueSystem = new DialogueSystem(falasMarte);

        // ITEM 16: se as waves ja tinham sido concluidas numa sessao anterior
        // (jogador salvou/saiu no meio da espera do boss), nao spawna wave de
        // novo — deixa vazio pra checkBossMarte() cuidar do resto.
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
        inventoryUI.update();
        if (!status.missaoFalhou) {
            if (dialogueSystem.ativo) {
                dialogueSystem.update();
            } else {
                handleInput(delta);
                updateInimigos(delta);
                updateProjeteis(delta);
                checkColisoes(delta);
                updateWaveManager();
                checkBossMarte();
                atualizarTextoMissao();
                updateCamera();
                particleManager.update(delta);
                status.atualizarCombate(delta); // corrige o bug de nao conseguir atirar de novo
            }
        } else {
            game.setScreen(new GameOverScreen(game));
            dispose();
            return;
        }

        // A troca de tela foi agendada (portal usado) — para aqui
        if (trocandoTela) {
            dispose();
            return;
        }

        Gdx.gl.glClearColor(0.25f, 0.08f, 0.05f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        desenharMundo();

        String extraHud = (bossMarte != null && bossMarte.ativo)
            ? "BOSS DE MARTE: " + (int) bossMarte.hp + "/" + (int) BossMarte.HP_INICIAL
            : "WAVE: " + waveAtual + "/" + MAX_WAVES;
        hud.render(shapeRenderer, batch, font, hudCamera, status, textoMissaoAtual, extraHud, 720);

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

        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            if (status.podeAtirar()) {
                Vector2 mouseWorld = viewport.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
                float startX = player.x + player.width / 2f;
                float startY = player.y + player.height / 2f;
                projeteisPlayer.add(new Projectile(startX, startY, mouseWorld.x, mouseWorld.y, 600f, 500f));
            }
        }

    }

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
                    ini.tomarDano(50);
                    particleManager.spawnColeta(ini.bounds.x, ini.bounds.y);

                    if (!ini.ativo) {
                        inimigos.remove(j);
                        // ITEM 16: se quem morreu foi o Boss de Marte, entrega a chave
                        // em vez do drop de item comum (oxigenio/municao).
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
                status.hp -= 15f;
                if (status.hp <= 0) status.missaoFalhou = true;
                projeteisInimigos.remove(i);
            }
        }
    }

    /** ITEM 16: recompensa por derrotar o Boss de Marte — abre o portal pra Tita. */
    private void onBossMarteDerrotado() {
        status.inventario.add("CHAVE_MARTE");
        portalAberto = true;
        portalTita.ativo = true;
        particleManager.spawnColeta(
            bossMarte.bounds.x + bossMarte.bounds.width / 2f,
            bossMarte.bounds.y + bossMarte.bounds.height / 2f
        );
        SaveManager.salvarJogo(status, missao);
    }

    private void checkColisoes(float delta) {
        for (Inimigo ini : inimigos) {
            if (ini.ativo && player.overlaps(ini.bounds)) {
                // CORRIGIDO: antes o dano de contato era um valor fixo (20f) pra
                // qualquer inimigo, ignorando o campo danoContato — isso fazia o
                // "dano alto" do Boss de Marte nao ter efeito nenhum. Agora usa
                // o danoContato de cada inimigo, igual a LunarScreen ja fazia.
                status.hp -= ini.danoContato * delta;
                if (status.hp <= 0) {
                    status.hp = 0;
                    status.missaoFalhou = true;
                }
            }
        }

        for (int i = itensDropados.size() - 1; i >= 0; i--) {
            Item item = itensDropados.get(i);
            if (!item.coletado && player.overlaps(item.bounds)) {
                item.coletado = true;
                if (item.type == ItemType.MUNICAO) status.municao += 8;
                else status.oxigenio = Math.min(100f, status.oxigenio + 20f);
                itensDropados.remove(i);
            }
        }

        if (portalAberto && !trocandoTela && player.overlaps(portalTita.bounds)) {
            status.curarAoTrocarFase(); // regenera parte da vida na troca de fase
            status.faseAtual = "TITA";
            SaveManager.salvarJogo(status, missao);
            game.setScreen(new TitanScreen(game, status));
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

    /**
     * ITEM 16: spawna o Boss de Marte assim que MissionState.marteMissoesOk(status)
     * vira true (estufa reparada na Lua + as 3 waves vencidas). So roda uma vez
     * (bossMarte fica != null depois do primeiro spawn) e nao spawna de novo se
     * a chave ja tiver sido conquistada (evita ressuscitar o boss num reload).
     */
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

        for (Inimigo ini : inimigos) {
            shapeRenderer.setColor(ini.getCor());
            shapeRenderer.rect(ini.bounds.x, ini.bounds.y, ini.bounds.width, ini.bounds.height);
        }

        // ITEM 16: barra de vida do Boss de Marte, enquanto ele estiver ativo
        if (bossMarte != null && bossMarte.ativo) {
            float barraLargura = bossMarte.bounds.width;
            shapeRenderer.setColor(Color.RED);
            shapeRenderer.rect(bossMarte.bounds.x, bossMarte.bounds.y + bossMarte.bounds.height + 12, barraLargura, 10);
            shapeRenderer.setColor(Color.GREEN);
            shapeRenderer.rect(bossMarte.bounds.x, bossMarte.bounds.y + bossMarte.bounds.height + 12,
                barraLargura * (bossMarte.hp / BossMarte.HP_INICIAL), 10);
        }

        for (Item item : itensDropados) {
            if (item.type != ItemType.OXIGENIO || regItemOxigenio == null) {
                shapeRenderer.setColor(Color.GOLD);
                shapeRenderer.rect(item.bounds.x, item.bounds.y, item.bounds.width, item.bounds.height);
            }
        }

        shapeRenderer.setColor(portalAberto ? Color.MAGENTA : Color.DARK_GRAY);
        shapeRenderer.rect(portalTita.bounds.x, portalTita.bounds.y, portalTita.bounds.width, portalTita.bounds.height);

        for (Projectile p : projeteisPlayer) {
            shapeRenderer.setColor(Color.CYAN);
            shapeRenderer.rect(p.x - 4, p.y - 4, 8, 8);
        }
        for (Projectile p : projeteisInimigos) {
            shapeRenderer.setColor(Color.MAGENTA);
            shapeRenderer.rect(p.x - 5, p.y - 5, 10, 10);
        }

        if (regPlayer == null) {
            shapeRenderer.setColor(Color.CYAN);
            shapeRenderer.rect(player.x, player.y, player.width, player.height - 12);
            shapeRenderer.setColor(Color.YELLOW);
            shapeRenderer.rect(player.x + 12, player.y + player.height - 16, player.width - 24, 12);
        }

        shapeRenderer.end();

        particleManager.render(shapeRenderer, camera);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        if (regPlayer != null) {
            batch.draw(regPlayer, player.x, player.y, player.width, player.height);
        }
        if (regItemOxigenio != null) {
            for (Item item : itensDropados) {
                if (item.type == ItemType.OXIGENIO) {
                    batch.draw(regItemOxigenio, item.bounds.x, item.bounds.y, item.bounds.width, item.bounds.height);
                }
            }
        }

        // ITEM 16: nome do boss acima dele, enquanto ativo
        if (bossMarte != null && bossMarte.ativo) {
            font.setColor(bossMarte.getCor());
            font.draw(batch, "SENTINELA DE MARTE", bossMarte.bounds.x - 20, bossMarte.bounds.y + bossMarte.bounds.height + 45);
        }

        font.setColor(portalAberto ? Color.MAGENTA : Color.GRAY);
        String txt = portalAberto ? "PORTAL PARA TITA [ABERTO]" : "PORTAL BLOQUEADO";
        font.draw(batch, txt, portalTita.bounds.x - 30, portalTita.bounds.y + portalTita.bounds.height + 20);
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
