package com.Echoes.Jogo.Managers;

import com.Echoes.Jogo.Entities.PlayerStatus;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

public class SaveManager {

    private static final String PREF_NAME = "EchoesSaveData";

    // v3 (ITEM 15): adicionado o campo "inventario" (chaves dos bosses: CHAVE_LUA, etc).
    // v4 (ITEM 16): adicionado o campo "marteWavesConcluidas" (Boss de Marte).
    // v5 (ITEM 17): adicionado o campo "titaGuardioesDerrotados" (Boss de Tita).
    private static final int SAVE_VERSION = 5;

    // Ordem de progresso das fases, usada só pra saber qual é "mais avançada".
    private static final String[] ORDEM_FASES = {"LUA", "MARTE", "TITA", "CALISTO", "AHARIN"};

    public static boolean hasSave() {
        Preferences prefs = Gdx.app.getPreferences(PREF_NAME);
        return prefs.contains("faseAtual") && prefs.getInteger("saveVersion", -1) == SAVE_VERSION;
    }

    /** Le apenas a fase salva (LUA/MARTE/TITA), sem precisar carregar o PlayerStatus inteiro. Usado no Menu. */
    public static String getFaseSalva() {
        Preferences prefs = Gdx.app.getPreferences(PREF_NAME);
        return prefs.getString("faseAtual", "LUA");
    }

    private static int rankFase(String fase) {
        for (int i = 0; i < ORDEM_FASES.length; i++) {
            if (ORDEM_FASES[i].equals(fase)) return i;
        }
        return 0;
    }

    /**
     * Atualiza a fase mais avançada que o jogador já alcançou, usada só pra
     * exibir no botão CONTINUAR do menu. Nunca regride, mesmo se o jogador
     * voltar pra uma fase anterior (ex.: usar o portal Lua<->Marte de novo
     * depois de já ter chegado em Tita).
     */
    private static void atualizarFaseMaisLonga(String faseAtual) {
        Preferences prefs = Gdx.app.getPreferences(PREF_NAME);
        String faseGuardada = prefs.getString("faseMaisLonga", "LUA");
        if (rankFase(faseAtual) > rankFase(faseGuardada)) {
            prefs.putString("faseMaisLonga", faseAtual);
            prefs.flush();
        }
    }

    /** Le a fase mais longe que o jogador ja alcancou nesse save. Usado no botao CONTINUAR do Menu. */
    public static String getFaseMaisLonga() {
        Preferences prefs = Gdx.app.getPreferences(PREF_NAME);
        return prefs.getString("faseMaisLonga", "LUA");
    }

    public static void salvarJogo(PlayerStatus status, MissionState missao) {
        Preferences prefs = Gdx.app.getPreferences(PREF_NAME);

        prefs.putInteger("saveVersion", SAVE_VERSION);

        prefs.putFloat("hp", status.hp);
        prefs.putFloat("oxigenio", status.oxigenio);
        prefs.putInteger("municao", status.municao);

        prefs.putBoolean("estufaReparada", status.estufaReparada);
        prefs.putBoolean("energiaReparada", status.energiaReparada);
        prefs.putBoolean("extracaoReparada", status.extracaoReparada);
        prefs.putBoolean("comunicacaoReparada", status.comunicacaoReparada);
        prefs.putBoolean("armaCraftada", status.armaCraftada);

        prefs.putBoolean("colPecaAntena", status.colPecaAntena);
        prefs.putBoolean("colPecaGerador", status.colPecaGerador);
        prefs.putBoolean("colPecaUsina", status.colPecaUsina);
        prefs.putBoolean("colPecaEstufa", status.colPecaEstufa);
        prefs.putBoolean("colArmaParteA", status.colArmaParteA);
        prefs.putBoolean("colArmaParteB", status.colArmaParteB);
        prefs.putBoolean("colArmaParteC", status.colArmaParteC);

        prefs.putFloat("lastMarteX", status.lastMarteX);
        prefs.putFloat("lastMarteY", status.lastMarteY);
        prefs.putInteger("marteWaveAtual", status.marteWaveAtual);

        // ITEM 16: persiste se as waves de Marte ja foram vencidas (condicao
        // do Boss de Marte), pra sobreviver a um save/load no meio da fase.
        prefs.putBoolean("marteWavesConcluidas", status.marteWavesConcluidas);

        // ITEM 17: persiste se os guardioes de Tita ja foram derrotados
        // (condicao do Boss de Tita).
        prefs.putBoolean("titaGuardioesDerrotados", status.titaGuardioesDerrotados);

        prefs.putString("faseAtual", status.faseAtual != null ? status.faseAtual : "LUA");

        // ITEM 15: persiste as chaves/itens de posse (ex.: CHAVE_LUA) como uma
        // string separada por vírgula, pra sobreviver a um save/load.
        prefs.putString("inventario", status.inventario.serializar());

        prefs.flush();

        // Atualiza (sem regredir) o recorde de fase mais longe alcancada.
        atualizarFaseMaisLonga(status.faseAtual != null ? status.faseAtual : "LUA");
    }

    public static boolean carregarJogo(PlayerStatus status, MissionState missao) {
        Preferences prefs = Gdx.app.getPreferences(PREF_NAME);

        if (!hasSave()) {
            return false;
        }

        status.hp = prefs.getFloat("hp", 100f);
        status.oxigenio = prefs.getFloat("oxigenio", 100f);
        status.municao = prefs.getInteger("municao", 20);

        status.estufaReparada = prefs.getBoolean("estufaReparada", false);
        status.energiaReparada = prefs.getBoolean("energiaReparada", false);
        status.extracaoReparada = prefs.getBoolean("extracaoReparada", false);
        status.comunicacaoReparada = prefs.getBoolean("comunicacaoReparada", false);
        status.armaCraftada = prefs.getBoolean("armaCraftada", false);

        status.colPecaAntena = prefs.getBoolean("colPecaAntena", false);
        status.colPecaGerador = prefs.getBoolean("colPecaGerador", false);
        status.colPecaUsina = prefs.getBoolean("colPecaUsina", false);
        status.colPecaEstufa = prefs.getBoolean("colPecaEstufa", false);
        status.colArmaParteA = prefs.getBoolean("colArmaParteA", false);
        status.colArmaParteB = prefs.getBoolean("colArmaParteB", false);
        status.colArmaParteC = prefs.getBoolean("colArmaParteC", false);
        status.pecasColetadas = status.colPecaEstufa && status.colPecaGerador
            && status.colPecaUsina && status.colPecaAntena;

        status.lastMarteX = prefs.getFloat("lastMarteX", 100f);
        status.lastMarteY = prefs.getFloat("lastMarteY", 100f);
        status.marteWaveAtual = prefs.getInteger("marteWaveAtual", 1);

        // ITEM 16: recarrega se as waves de Marte ja foram vencidas.
        status.marteWavesConcluidas = prefs.getBoolean("marteWavesConcluidas", false);

        // ITEM 17: recarrega se os guardioes de Tita ja foram derrotados.
        status.titaGuardioesDerrotados = prefs.getBoolean("titaGuardioesDerrotados", false);

        status.faseAtual = prefs.getString("faseAtual", "LUA");

        // ITEM 15: recarrega as chaves/itens de posse salvos.
        status.inventario.carregarDe(prefs.getString("inventario", ""));

        // CORRIGIDO: antes o MissionState passado aqui ficava sempre na etapa 0
        // (recem-criado), entao ao clicar em "CONTINUAR" o Quest Tracker voltava
        // pro comeco mesmo o jogador ja tendo progredido. Agora recalculamos a
        // etapa certa com base no que foi carregado do save.
        if (missao != null) {
            missao.setEtapa(MissionState.calcularEtapa(status));
        }

        return true;
    }

    /** Apaga qualquer save existente — usado quando o jogador clica em NOVO JOGO. */
    public static void limparSave() {
        Preferences prefs = Gdx.app.getPreferences(PREF_NAME);
        prefs.clear();
        prefs.flush();
    }
}
