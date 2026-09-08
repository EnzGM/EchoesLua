package com.Echoes.Jogo;

import com.badlogic.gdx.Game;
import com.Echoes.Jogo.Screen.MenuScreen;

public class Main extends Game {
    @Override
    public void create() {
        setScreen(new MenuScreen(this));
    }
}
