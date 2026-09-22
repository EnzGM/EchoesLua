package com.Echoes.Jogo.Managers;

/**
 * ITEM 23: ciclo de tempestade — calmo -> alerta -> tempestade -> calmo,
 * se repetindo pra sempre enquanto a tela estiver ativa. Não guarda nada
 * do PlayerStatus; a tela (MarsScreen/TitanScreen) que decide o que fazer
 * com a fase atual (dano fora do abrigo, cor do ceu, etc).
 */
public class Tempestade {

    public enum Fase { CALMO, ALERTA, TEMPESTADE }

    private static final float DURACAO_CALMO = 15f;
    private static final float DURACAO_ALERTA = 3f;
    private static final float DURACAO_TEMPESTADE = 8f;

    private float timer = 0f;
    private Fase faseAtual = Fase.CALMO;

    public void update(float delta) {
        timer += delta;

        switch (faseAtual) {
            case CALMO:
                if (timer >= DURACAO_CALMO) {
                    faseAtual = Fase.ALERTA;
                    timer = 0f;
                }
                break;
            case ALERTA:
                if (timer >= DURACAO_ALERTA) {
                    faseAtual = Fase.TEMPESTADE;
                    timer = 0f;
                }
                break;
            case TEMPESTADE:
                if (timer >= DURACAO_TEMPESTADE) {
                    faseAtual = Fase.CALMO;
                    timer = 0f;
                }
                break;
        }
    }

    public Fase getFase() {
        return faseAtual;
    }

    public boolean estaEmTempestade() {
        return faseAtual == Fase.TEMPESTADE;
    }

    public boolean estaEmAlerta() {
        return faseAtual == Fase.ALERTA;
    }

    private float duracaoDaFase() {
        switch (faseAtual) {
            case ALERTA: return DURACAO_ALERTA;
            case TEMPESTADE: return DURACAO_TEMPESTADE;
            default: return DURACAO_CALMO;
        }
    }

    public float getTempoRestanteFase() {
        return Math.max(0f, duracaoDaFase() - timer);
    }

    /** Texto pronto pro HUD, ex.: "TEMPESTADE 8 s" / "ALERTA 3 s" / "CEU LIMPO". */
    public String getTextoHud() {
        switch (faseAtual) {
            case ALERTA: return "ALERTA " + (int) Math.ceil(getTempoRestanteFase()) + " s";
            case TEMPESTADE: return "TEMPESTADE " + (int) Math.ceil(getTempoRestanteFase()) + " s";
            default: return "CEU LIMPO";
        }
    }
}
