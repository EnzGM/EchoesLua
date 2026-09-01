package com.Echoes.Jogo.Managers;

import com.Echoes.Jogo.Entities.PlayerStatus;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

/**
 * MELHORIA 4: Persistência de Dados (Save/Load da campanha unificada).
 * Um único save cobre Lua e Marte: posição de checkpoint, progresso da
 * missão (Quest Tracker), wave atual em Marte, reparos, munição, etc.
 */
public class SaveManager {

    private static final String PREF_NAME = "EchoesSaveData";

    public static void salvarJogo(PlayerStatus status, MissionState missao) {
        Preferences prefs = Gdx.app.getPreferences(PREF_NAME);

        // Missão e Fase
        prefs.putString("faseAtual", status.faseAtual);
        prefs.putInteger("missaoEtapa", missao.getEtapa());
        prefs.putBoolean("pecasColetadas", status.pecasColetadas);
        prefs.putInteger("marteWave", status.marteWaveAtual);
        prefs.putInteger("inimigosDerrotados", status.inimigosDerrotados);

        // CORRIGIDO: rastreamento individual de peças (pro Quest Tracker sobreviver ao save/load)
        prefs.putBoolean("colPecaAntena", status.colPecaAntena);
        prefs.putBoolean("colPecaGerador", status.colPecaGerador);
        prefs.putBoolean("colPecaUsina", status.colPecaUsina);
        prefs.putBoolean("colPecaEstufa", status.colPecaEstufa);
        prefs.putBoolean("colArmaParteA", status.colArmaParteA);
        prefs.putBoolean("colArmaParteB", status.colArmaParteB);
        prefs.putBoolean("colArmaParteC", status.colArmaParteC);

        // Posições (checkpoint do portal bidirecional)
        prefs.putFloat("lastLuaX", status.lastLuaX);
        prefs.putFloat("lastLuaY", status.lastLuaY);
        prefs.putFloat("lastMarteX", status.lastMarteX);
        prefs.putFloat("lastMarteY", status.lastMarteY);

        // Status e Combate
        prefs.putFloat("oxigenio", status.oxigenio);
        prefs.putFloat("hp", status.hp);
        prefs.putInteger("municao", status.municao);

        // Inventário e Peças
        prefs.putInteger("comida", status.comida);
        prefs.putInteger("inventarioGelo", status.inventarioGelo);
        // CORRIGIDO: agua e combustivel não estavam sendo salvos
        prefs.putInteger("agua", status.agua);
        prefs.putInteger("combustivel", status.combustivel);
        prefs.putInteger("pecaAntena", status.pecaAntena);
        prefs.putInteger("pecaGerador", status.pecaGerador);
        prefs.putInteger("pecaUsina", status.pecaUsina);
        prefs.putInteger("pecaEstufa", status.pecaEstufa);
        prefs.putInteger("armaA", status.armaParteA);
        prefs.putInteger("armaB", status.armaParteB);
        prefs.putInteger("armaC", status.armaParteC);

        // Reparos e Arma Craftada
        prefs.putBoolean("comunicacao", status.comunicacaoReparada);
        prefs.putBoolean("energia", status.energiaReparada);
        prefs.putBoolean("extracao", status.extracaoReparada);
        prefs.putBoolean("estufa", status.estufaReparada);
        prefs.putBoolean("armaCraftada", status.armaCraftada);

        prefs.flush();
        Gdx.app.log("SaveManager", "Jogo salvo com sucesso!");
    }

    public static boolean carregarJogo(PlayerStatus status, MissionState missao) {
        Preferences prefs = Gdx.app.getPreferences(PREF_NAME);

        if (!prefs.contains("faseAtual")) {
            return false; // Não existe save anterior
        }

        status.faseAtual = prefs.getString("faseAtual", "LUA");
        missao.setEtapa(prefs.getInteger("missaoEtapa", 0));
        status.pecasColetadas = prefs.getBoolean("pecasColetadas", false);
        status.marteWaveAtual = prefs.getInteger("marteWave", 0);
        status.inimigosDerrotados = prefs.getInteger("inimigosDerrotados", 0);

        status.colPecaAntena = prefs.getBoolean("colPecaAntena", false);
        status.colPecaGerador = prefs.getBoolean("colPecaGerador", false);
        status.colPecaUsina = prefs.getBoolean("colPecaUsina", false);
        status.colPecaEstufa = prefs.getBoolean("colPecaEstufa", false);
        status.colArmaParteA = prefs.getBoolean("colArmaParteA", false);
        status.colArmaParteB = prefs.getBoolean("colArmaParteB", false);
        status.colArmaParteC = prefs.getBoolean("colArmaParteC", false);

        status.lastLuaX = prefs.getFloat("lastLuaX", 1280f);
        status.lastLuaY = prefs.getFloat("lastLuaY", 720f);
        status.lastMarteX = prefs.getFloat("lastMarteX", 1280f);
        status.lastMarteY = prefs.getFloat("lastMarteY", 720f);

        status.oxigenio = prefs.getFloat("oxigenio", 100f);
        status.hp = prefs.getFloat("hp", 100f);
        status.municao = prefs.getInteger("municao", 10);

        status.comida = prefs.getInteger("comida", 0);
        status.inventarioGelo = prefs.getInteger("inventarioGelo", 0);
        status.agua = prefs.getInteger("agua", 0);
        status.combustivel = prefs.getInteger("combustivel", 0);
        status.pecaAntena = prefs.getInteger("pecaAntena", 0);
        status.pecaGerador = prefs.getInteger("pecaGerador", 0);
        status.pecaUsina = prefs.getInteger("pecaUsina", 0);
        status.pecaEstufa = prefs.getInteger("pecaEstufa", 0);
        status.armaParteA = prefs.getInteger("armaA", 0);
        status.armaParteB = prefs.getInteger("armaB", 0);
        status.armaParteC = prefs.getInteger("armaC", 0);

        status.comunicacaoReparada = prefs.getBoolean("comunicacao", false);
        status.energiaReparada = prefs.getBoolean("energia", false);
        status.extracaoReparada = prefs.getBoolean("extracao", false);
        status.estufaReparada = prefs.getBoolean("estufa", false);
        status.armaCraftada = prefs.getBoolean("armaCraftada", false);

        status.missaoEtapa = missao.getEtapa();

        Gdx.app.log("SaveManager", "Jogo carregado com sucesso!");
        return true;
    }

    /** Permite habilitar o botão de Continuar no Menu Principal se o jogador já tiver salvo antes. */
    public static boolean hasSave() {
        return Gdx.app.getPreferences(PREF_NAME).contains("faseAtual");
    }

    /** Apaga o save — chamado quando a campanha é concluída (vitória). */
    public static void apagarSave() {
        Preferences prefs = Gdx.app.getPreferences(PREF_NAME);
        prefs.clear();
        prefs.flush();
        Gdx.app.log("SaveManager", "Save apagado (campanha concluida).");
    }
}
