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
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/** Tela de escolha entre os tres finais. */
public class EndingSelectScreen implements Screen {
    private final Main game; private final PlayerStatus status;
    private OrthographicCamera camera; private Viewport viewport; private SpriteBatch batch;
    private BitmapFont title, text; private final GlyphLayout layout = new GlyphLayout();

    public EndingSelectScreen(Main game, PlayerStatus status) { this.game = game; this.status = status; }
    @Override public void show() {
        Gdx.input.setInputProcessor(null);
        camera = new OrthographicCamera(); viewport = new FitViewport(1280,720,camera); camera.position.set(640,360,0);
        batch = new SpriteBatch(); title = new BitmapFont(); title.getData().setScale(2.5f); text = new BitmapFont(); text.getData().setScale(1.2f);
    }
    @Override public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) { game.setScreen(new FinalEndingScreen(game, status, 1)); dispose(); return; }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) { game.setScreen(new FinalEndingScreen(game, status, 2)); dispose(); return; }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) { game.setScreen(new FinalEndingScreen(game, status, 3)); dispose(); return; }
        Gdx.gl.glClearColor(0.02f,0.03f,0.07f,1); Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT); viewport.apply(); camera.update();
        batch.setProjectionMatrix(camera.combined); batch.begin();
        title.setColor(Color.WHITE); layout.setText(title,"A ESCOLHA FINAL"); title.draw(batch,layout,640-layout.width/2f,540);
        text.setColor(Color.WHITE);
        String[] lines={"1 - FINAL DA LUZ: entregue a Chave e salve os mundos.","2 - FINAL DO EQUILIBRIO: use a Chave para selar os portais.","3 - FINAL DA FRONTEIRA: mantenha a Chave e abra um novo caminho."};
        for(int i=0;i<lines.length;i++){ layout.setText(text,lines[i]); text.draw(batch,layout,640-layout.width/2f,390-i*75); }
        layout.setText(text,"Pressione 1, 2 ou 3"); text.draw(batch,layout,640-layout.width/2f,120); batch.end();
    }
    @Override public void resize(int w,int h){viewport.update(w,h);} @Override public void pause(){} @Override public void resume(){} @Override public void hide(){}
    @Override public void dispose(){batch.dispose(); title.dispose(); text.dispose();}
}
