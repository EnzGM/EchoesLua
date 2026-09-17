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
        "Sobreviver em Marte",
        "Derrotar o Guardiao de Tita"
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
     *
     * CORRIGIDO: antes so existia checagem pra "MARTE", entao ao chegar em
     * Tita a etapa calculada ficava igual a de Marte (4) e o texto mostrado
     * pro jogador ficava errado ("Sobreviver em Marte" estando em Tita).
     */
    public static int calcularEtapa(PlayerStatus status) {
        if (status.faseAtual.equals("TITA")) {
            return 5;
        }
        if (status.faseAtual.equals("MARTE")) {
            return 4;
        }
        int concluidas = 0;
        if (status.pecasColetadas) concluidas++;
        if (status.todosReparosConcluidos()) concluidas++;
        if (status.armaCraftada) concluidas++;
        return concluidas;
    }

    /**
     * ITEM 15: flag que indica se as "missões da Lua" (peças coletadas + as 4
     * estações reparadas + arma craftada) já foram concluídas.
     *
     * Enquanto isso for false:
     *  - o Boss da Lua (BossLua) não pode spawnar;
     *  - a cratera (portal pra Marte) continua mostrando BLOQUEADO.
     *
     * É estático (recebe o PlayerStatus) pra poder ser chamado a qualquer
     * momento pela tela, sem precisar guardar estado duplicado aqui.
     */
    public static boolean luaMissoesOk(PlayerStatus status) {
        return status.pecasColetadas
            && status.todosReparosConcluidos()
            && status.armaCraftada;
    }

    /**
     * ITEM 16: flag que indica se as "missões de Marte" já foram concluídas.
     *
     * Usa estufaReparada (trazido da Lua) + marteWavesConcluidas (o "combate
     * de prova": vencer as 3 waves da MarsScreen) como exigido pelo checklist.
     *
     * Enquanto isso for false:
     *  - o Boss de Marte (BossMarte) não pode spawnar;
     *  - o portal pra Titã continua BLOQUEADO.
     */
    public static boolean marteMissoesOk(PlayerStatus status) {
        return status.estufaReparada && status.marteWavesConcluidas;
    }

    /**
     * ITEM 17: flag que indica se as "missões de Tita" já foram concluídas.
     *
     * Usa titaGuardioesDerrotados (derrotar os 3 guardiões comuns da
     * TitanScreen) como o "combate de prova" exigido pelo checklist.
     *
     * Enquanto isso for false:
     *  - o Boss de Tita (BossTita) não pode spawnar;
     *  - o portal pra Calisto continua BLOQUEADO.
     */
    public static boolean titaMissoesOk(PlayerStatus status) {
        return status.titaGuardioesDerrotados;
    }
}
