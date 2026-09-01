package com.Echoes.Jogo.Managers;

import com.Echoes.Jogo.Entities.PlayerStatus;

/**
 * MELHORIA 1: Quest Tracker (Missao em Cadeia)
 * Controla a sequencia obrigatoria de objetivos da campanha.
 */
public class MissionState {

    private int etapa = 0;

    private final String[] etapas = {
        "Coletar pecas da colonia",
        "Reparar as estacoes na base",
        "Craftar a arma na base",
        "Ativar o portal para Marte",
        "Sobreviver em Marte"
    };

    public String getAtual() {
        return etapas[Math.min(etapa, etapas.length - 1)];
    }

    public int getEtapa() {
        return etapa;
    }

    public void setEtapa(int etapa) {
        this.etapa = Math.max(0, Math.min(etapa, etapas.length - 1));
    }

    /** Avança de etapa se a condição informada for verdadeira. */
    public void avancarSe(boolean condicao) {
        if (condicao && etapa < etapas.length - 1) {
            etapa++;
        }
    }

    public boolean isFinalizada() {
        return etapa >= etapas.length - 1;
    }

    /**
     * Calcula em qual etapa da missao o jogador esta, com base no progresso
     * salvo em PlayerStatus. Chamado a cada frame pelas telas, assim o Quest
     * Tracker fica sempre sincronizado com o estado real do jogo (inclusive
     * depois de um load).
     */
    public static int calcularEtapa(PlayerStatus status) {
        if (status.faseAtual.equals("MARTE")) {
            return 4;
        }
        int concluidas = 0;
        if (status.pecasColetadas) concluidas++;
        if (status.todosReparosConcluidos()) concluidas++;
        if (status.armaCraftada) concluidas++;
        return concluidas;
    }
}
