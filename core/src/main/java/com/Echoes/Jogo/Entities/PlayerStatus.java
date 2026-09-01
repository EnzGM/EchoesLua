package com.Echoes.Jogo.Entities;

public class PlayerStatus {

    public float oxigenio = 100f;
    public float hp = 100f;
    public int comida = 0;
    public int inventarioGelo = 0;
    public int agua = 0;
    public int combustivel = 0;

    public int pecaAntena = 0;
    public int pecaGerador = 0;
    public int pecaUsina = 0;
    public int pecaEstufa = 0;

    public int armaParteA = 0;
    public int armaParteB = 0;
    public int armaParteC = 0;

    public boolean comunicacaoReparada = false;
    public boolean energiaReparada = false;
    public boolean extracaoReparada = false;
    public boolean estufaReparada = false;

    public boolean armaCraftada = false;

    public int municao = 10;
    public float cooldownTiro = 0f;
    public static final float COOLDOWN_MAX = 0.35f;
    public int inimigosDerrotados = 0;

    public String faseAtual = "LUA";
    public float lastLuaX = 1280f;
    public float lastLuaY = 720f;
    public float lastMarteX = 1280f;
    public float lastMarteY = 720f;

    public boolean missaoFalhou = false;

    // MELHORIA 1: Quest Tracker
    public boolean pecasColetadas = false;
    public int missaoEtapa = 0;

    // CORRIGIDO: rastreamento individual de cada peça já coletada alguma vez,
    // independente de já ter sido usada num reparo/craft. Resolve o bug da
    // missão travando/mostrando a etapa errada quando o jogador repara aos poucos.
    public boolean colPecaAntena = false;
    public boolean colPecaGerador = false;
    public boolean colPecaUsina = false;
    public boolean colPecaEstufa = false;
    public boolean colArmaParteA = false;
    public boolean colArmaParteB = false;
    public boolean colArmaParteC = false;

    // MELHORIA 6: checkpoint da wave em Marte
    public int marteWaveAtual = 0;

    public void update(float delta) {
        if (missaoFalhou) return;

        oxigenio -= 2f * delta;
        if (oxigenio <= 0f) {
            oxigenio = 0f;
            missaoFalhou = true;
        }

        atualizarCombate(delta);
    }

    public void consumirOxigenio(float delta) {
        update(delta);
    }

    /** Só tira o cooldown da arma (sem mexer no oxigenio) — usado em telas onde o O2 não é consumido, como Marte. */
    public void atualizarCombate(float delta) {
        if (cooldownTiro > 0f) {
            cooldownTiro -= delta;
            if (cooldownTiro < 0f) cooldownTiro = 0f;
        }
    }

    public boolean podeAtirar() {
        if (armaCraftada && municao > 0 && cooldownTiro <= 0f) {
            municao--;
            cooldownTiro = COOLDOWN_MAX;
            return true;
        }
        return false;
    }

    public void recarregarNaBase(float delta) {
        oxigenio = Math.min(100f, oxigenio + 35f * delta);
        hp = Math.min(100f, hp + 20f * delta);
    }

    public boolean processarGelo() {
        if (inventarioGelo <= 0) return false;
        inventarioGelo--;
        agua += 1;
        oxigenio = Math.min(100f, oxigenio + 10f);
        combustivel += 1;
        return true;
    }

    public boolean todosReparosConcluidos() {
        return comunicacaoReparada && energiaReparada && extracaoReparada && estufaReparada;
    }

    public int reparosFeitos() {
        int count = 0;
        if (comunicacaoReparada) count++;
        if (energiaReparada) count++;
        if (extracaoReparada) count++;
        if (estufaReparada) count++;
        return count;
    }

    public boolean isMorto() {
        return oxigenio <= 0f || hp <= 0f;
    }
}
