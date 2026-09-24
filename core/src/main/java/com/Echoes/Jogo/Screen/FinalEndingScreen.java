package com.Echoes.Jogo.Screen;

import com.Echoes.Jogo.Entities.PlayerStatus;
import com.Echoes.Jogo.Main;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/** Cutscene final: cada um dos tres finais possui sua propria sequencia de cenas. */
public class FinalEndingScreen implements Screen {
    private final Main game;
    private final PlayerStatus status;
    private final int finalId;
    private OrthographicCamera camera;
    private Viewport viewport;
    private SpriteBatch batch;
    private BitmapFont title, text, hint;
    private final GlyphLayout layout = new GlyphLayout();
    private float tempo;
    private int cena;
    private static final float DURACAO_CENA = 3.6f;

    public FinalEndingScreen(Main game, PlayerStatus status, int finalId) {
        this.game = game;
        this.status = status;
        this.finalId = finalId;
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(null);
        camera = new OrthographicCamera();
        viewport = new FitViewport(1280, 720, camera);
        camera.position.set(640, 360, 0);
        batch = new SpriteBatch();
        title = new BitmapFont();
        title.getData().setScale(2.7f);
        text = new BitmapFont();
        text.getData().setScale(1.35f);
        hint = new BitmapFont();
        hint.getData().setScale(1.0f);
        tempo = 0f;
        cena = 0;
        status.chefesMortos.add("FINAL_" + finalId);
    }

    @Override
    public void render(float delta) {
        tempo += delta;

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            avancarCena();
            return;
        }

        if (tempo >= DURACAO_CENA) {
            avancarCena();
            return;
        }

        viewport.apply();
        camera.update();

        Color fundo = corFundo();
        Gdx.gl.glClearColor(fundo.r, fundo.g, fundo.b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        float entrada = MathUtils.clamp(tempo / 0.55f, 0f, 1f);
        float saida = MathUtils.clamp((DURACAO_CENA - tempo) / 0.55f, 0f, 1f);
        float alpha = Math.min(entrada, saida);

        String[] dados = dadosCena();
        title.setColor(0.85f, 0.95f, 1f, alpha);
        layout.setText(title, dados[0]);
        title.draw(batch, layout, 640 - layout.width / 2f, 490);

        text.setColor(Color.WHITE.r, Color.WHITE.g, Color.WHITE.b, alpha);
        String[] linhas = dados[1].split("\\n");
        for (int i = 0; i < linhas.length; i++) {
            layout.setText(text, linhas[i]);
            text.draw(batch, layout, 640 - layout.width / 2f, 365 - i * 48);
        }

        hint.setColor(0.72f, 0.78f, 0.86f, alpha);
        String dica = cena < 3 ? "ENTER / SPACE  -  continuar" : "ENTER / SPACE  -  finalizar";
        layout.setText(hint, dica);
        hint.draw(batch, layout, 640 - layout.width / 2f, 115);

        batch.end();
    }

    private void avancarCena() {
        tempo = 0f;
        cena++;
        if (cena >= 4) {
            game.setScreen(new MenuScreen(game));
            dispose();
        }
    }

    private Color corFundo() {
        if (finalId == 1) {
            return new Color(0.06f + cena * 0.015f, 0.08f + cena * 0.012f, 0.16f + cena * 0.025f, 1f);
        }
        if (finalId == 2) {
            return new Color(0.035f + cena * 0.012f, 0.055f + cena * 0.014f, 0.085f + cena * 0.018f, 1f);
        }
        return new Color(0.10f + cena * 0.02f, 0.035f + cena * 0.01f, 0.025f + cena * 0.008f, 1f);
    }

    private String[] dadosCena() {
        if (finalId == 1) {
            switch (cena) {
                case 0: return new String[]{"FINAL DA LUZ", "A Chave de Luz deixa as maos do astronauta.\nUma onda luminosa atravessa Aharin e alcanca os quatro mundos."};
                case 1: return new String[]{"A TERRA RESPONDE", "Os sinais dos portais estabilizam.\nAs comunicacoes perdidas voltam uma a uma."};
                case 2: return new String[]{"UM NOVO AMANHECER", "Lua, Marte, Tita e Calisto permanecem conectados.\nA viagem que parecia impossivel finalmente valeu a pena."};
                default: return new String[]{"FIM DA JORNADA", "A luz permanece acesa.\nMas toda exploracao deixa uma nova historia para contar."};
            }
        }
        if (finalId == 2) {
            switch (cena) {
                case 0: return new String[]{"FINAL DO EQUILIBRIO", "A Chave de Luz e colocada no nucleo do sistema.\nOs quatro portais comecam a perder sua instabilidade."};
                case 1: return new String[]{"OS PORTAIS SE FECHAM", "As passagens entre os mundos sao seladas uma por uma.\nNenhum planeta e consumido pelo caos."};
                case 2: return new String[]{"SILENCIO ENTRE AS ESTRELAS", "O universo fica quieto por alguns segundos.\nA tripulacao sabe que o equilibrio foi preservado."};
                default: return new String[]{"FIM DA JORNADA", "Nem tudo precisa permanecer aberto para continuar existindo.\nO equilibrio agora protege os quatro mundos."};
            }
        }
        switch (cena) {
            case 0: return new String[]{"FINAL DA FRONTEIRA", "A Chave permanece com o astronauta.\nEm vez de fechar o ultimo portal, ele decide atravessa-lo."};
            case 1: return new String[]{"UM CAMINHO DESCONHECIDO", "Uma nova passagem se abre alem dos mapas conhecidos.\nNenhum registro consegue identificar o destino."};
            case 2: return new String[]{"O PRIMEIRO PASSO", "O drone ilumina o caminho.\nAo longe, dezenas de pontos desconhecidos respondem ao chamado."};
            default: return new String[]{"FIM... POR ENQUANTO", "A jornada termina aqui, mas uma nova expedicao acaba de comecar.\nO que existe depois da fronteira ainda esta esperando."};
        }
    }

    @Override public void resize(int width, int height) { if (viewport != null) viewport.update(width, height); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() {
        if (batch != null) batch.dispose();
        if (title != null) title.dispose();
        if (text != null) text.dispose();
        if (hint != null) hint.dispose();
    }
}
