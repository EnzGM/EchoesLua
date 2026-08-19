package com.Echoes.Jogo.Entities;

/**
 * Guarda todo o "status" do jogador/missão num lugar só.
 * Facilita passar pro HUD e pra lógica de processamento de gelo.
 */
public class PlayerStatus {
    public float oxigenio = 100f;      // 0 a 100. Se chegar a 0, missão falha.
    public int comida = 0;             // contador simples de comida coletada
    public int inventarioGelo = 0;     // rochas de gelo carregadas, ainda não processadas
    public int agua = 0;               // gerada ao processar gelo na base
    public int combustivel = 0;        // opcional/desejável, geramos também

    public boolean missaoFalhou = false;

    /**
     * Consumo passivo de oxigênio, chamado a cada frame.
     * @param delta tempo desde o último frame (segundos)
     */
    public void consumirOxigenio(float delta) {
        if (missaoFalhou) return; // já morreu, não precisa mais consumir

        oxigenio -= 2f * delta; // 2 unidades de O2 por segundo (ajuste como quiser)
        if (oxigenio <= 0f) {
            oxigenio = 0f;
            missaoFalhou = true; // dispara o game over
        }
    }

    /**
     * Chamado quando o jogador está dentro da base: recarrega O2.
     */
    public void recarregarNaBase(float delta) {
        oxigenio = Math.min(100f, oxigenio + 30f * delta);
    }

    /**
     * Processa 1 rocha de gelo em recursos, se houver gelo disponível.
     * Regra do desafio: "não processa se gelo == 0".
     * @return true se processou, false se não tinha gelo
     */
    public boolean processarGelo() {
        if (inventarioGelo <= 0) {
            return false; // nada a processar
        }

        inventarioGelo--;      // consome 1 gelo
        agua += 1;              // gera água
        oxigenio = Math.min(100f, oxigenio + 5f); // gelo também gera um pouco de O2
        combustivel += 1;       // bônus "desejável" do checklist, já incluído

        return true;
    }
}
