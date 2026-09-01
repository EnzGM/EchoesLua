package com.Echoes.Jogo.Entities;

public class PlayerStatus {
    public float oxigenio = 100f;
    public int comida = 0;
    public int inventarioGelo = 0;
    public int agua = 0;
    public int combustivel = 0;

    // --- ETAPA 2: Inventário de Peças ---
    public int pecaAntena = 0;
    public int pecaGerador = 0;
    public int pecaUsina = 0;
    public int pecaEstufa = 0;

    public int armaParteA = 0;
    public int armaParteB = 0;
    public int armaParteC = 0;

    // --- ETAPA 3 e 5: Status de Reparos e Arma (Já deixando preparado) ---
    public boolean comunicacaoReparada = false;
    public boolean energiaReparada = false;
    public boolean extracaoReparada = false;
    public boolean estufaReparada = false;
    public boolean armaCraftada = false;

    public boolean missaoFalhou = false;

    public void consumirOxigenio(float delta) {
        if (missaoFalhou) return;
        oxigenio -= 2f * delta;
        if (oxigenio <= 0f) {
            oxigenio = 0f;
            missaoFalhou = true;
        }
    }

    public void recarregarNaBase(float delta) {
        oxigenio = Math.min(100f, oxigenio + 30f * delta);
    }

    public boolean processarGelo() {
        if (inventarioGelo <= 0) return false;
        inventarioGelo--;
        agua += 1;
        oxigenio = Math.min(100f, oxigenio + 5f);
        combustivel += 1;
        return true;
    }
}
