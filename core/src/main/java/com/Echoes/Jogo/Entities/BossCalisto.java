package com.Echoes.Jogo.Entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

import java.util.List;

/**
 * ITEM 18: Boss de Calisto — 3 formas/mutações.
 *
 * Quando hp <= 0 e forma < 3: sobe de forma (nunca pula direto pra forma 3),
 * fica maior, mais rápido, com hp máximo maior e MUDA o padrão de tiro, e
 * passa por uma pequena janela de "queda/recuperação" (transicaoTimer) em
 * que fica parado, invulnerável e piscando.
 *
 * Quando hp <= 0 na forma 3: mortoFinal = true (quem entrega a CHAVE_LUZ pro
 * inventário é a CallistoScreen, já que o boss não tem acesso ao PlayerStatus).
 */
public class BossCalisto extends Inimigo {

    public int forma = 1;
    public float hpMax;
    public boolean mortoFinal = false;

    private static final float TAMANHO_BASE = 120f;
    private static final float CRESCIMENTO_TAMANHO_POR_FORMA = 20f;

    // Reforçado a pedido: bem mais vida/velocidade/dano que a versão original,
    // pra virar um combate de verdade a cada mutação.
    private static final float[] HP_POR_FORMA = {300f, 520f, 800f};
    private static final float[] VELOCIDADE_POR_FORMA = {95f, 130f, 165f};
    private static final float[] DANO_CONTATO_POR_FORMA = {30f, 40f, 52f};

    private static final float TEMPO_TRANSICAO = 1.1f;
    public float transicaoTimer = 0f;

    // Dash exclusivo da forma 3, a cada 3s (além do padrão de tiro radial dela).
    private static final float DASH_INTERVALO = 3f;
    private static final float DASH_DISTANCIA = 230f;
    private float dashTimer = DASH_INTERVALO;

    // Padrão de tiro: cada forma atira diferente.
    private float tiroTimer;
    private float rotacaoPadrao = 0f;

    public BossCalisto(float x, float y) {
        super(x, y, TipoInimigo.NORMAL);

        this.hpMax = HP_POR_FORMA[0];
        this.hp = hpMax;
        this.velocidade = VELOCIDADE_POR_FORMA[0];
        this.danoContato = DANO_CONTATO_POR_FORMA[0];
        this.perseguicaoTotal = true;
        this.bounds.setSize(TAMANHO_BASE, TAMANHO_BASE);
        this.tiroTimer = intervaloTiro();
    }

    @Override
    public void update(float delta, Rectangle player, List<Projectile> projeteisInimigos) {
        if (!ativo || mortoFinal) return;

        if (transicaoTimer > 0f) {
            transicaoTimer -= delta;
            return; // "caído/se levantando": fica parado e nao atira durante a transição
        }

        super.update(delta, player, projeteisInimigos);
        atualizarTiro(delta, player, projeteisInimigos);

        if (forma == 3) {
            dashTimer -= delta;
            if (dashTimer <= 0f) {
                dashTimer = DASH_INTERVALO;
                executarDash(player);
            }
        }
    }

    private float intervaloTiro() {
        switch (forma) {
            case 2: return 2.1f;
            case 3: return 1.5f;
            default: return 2.6f;
        }
    }

    /**
     * Cada forma tem um padrão de tiro diferente:
     *  - Forma 1: um tiro simples mirado no jogador.
     *  - Forma 2: rajada em leque de 3 tiros.
     *  - Forma 3: rajada radial de 10 tiros em todas as direções (+ o dash).
     */
    private void atualizarTiro(float delta, Rectangle player, List<Projectile> lista) {
        tiroTimer -= delta;
        if (tiroTimer > 0f) return;
        tiroTimer = intervaloTiro();

        float sx = bounds.x + bounds.width / 2f;
        float sy = bounds.y + bounds.height / 2f;
        float tx = player.x + player.width / 2f;
        float ty = player.y + player.height / 2f;

        switch (forma) {
            case 1:
                lista.add(Projectile.tiroEspecialInimigo(sx, sy, tx, ty, 260f, 700f));
                break;
            case 2:
                disparaLeque(lista, sx, sy, tx, ty, 3, 18f, 300f);
                break;
            case 3:
                disparaRadial(lista, sx, sy, 10, 340f);
                break;
            default:
                break;
        }
    }

    /** Rajada em leque: N tiros espalhados em torno da direção do jogador. */
    private void disparaLeque(List<Projectile> lista, float sx, float sy, float tx, float ty,
                              int qtd, float anguloEntreTiros, float velocidade) {
        float anguloBase = (float) Math.toDegrees(Math.atan2(ty - sy, tx - sx));
        float inicio = anguloBase - anguloEntreTiros * (qtd - 1) / 2f;

        for (int i = 0; i < qtd; i++) {
            float ang = (inicio + i * anguloEntreTiros) * MathUtils.degreesToRadians;
            float destinoX = sx + MathUtils.cos(ang) * 400f;
            float destinoY = sy + MathUtils.sin(ang) * 400f;
            lista.add(Projectile.tiroEspecialInimigo(sx, sy, destinoX, destinoY, velocidade, 650f));
        }
    }

    /** Rajada radial: N tiros distribuídos igualmente em 360 graus. */
    private void disparaRadial(List<Projectile> lista, float sx, float sy, int qtd, float velocidade) {
        rotacaoPadrao += 0.20f;
        // Forma 3: dois anéis defasados. O padrão gira e cria corredores
        // móveis, no estilo bullet-hell, em vez de uma cruz previsível.
        for (int anel = 0; anel < 2; anel++) {
            for (int i = 0; i < qtd; i++) {
                float ang = rotacaoPadrao + anel * (MathUtils.PI / qtd)
                        + (MathUtils.PI2 / qtd) * i;
                float alcance = anel == 0 ? 760f : 900f;
                float vel = anel == 0 ? velocidade : velocidade + 45f;
                float destinoX = sx + MathUtils.cos(ang) * alcance;
                float destinoY = sy + MathUtils.sin(ang) * alcance;
                lista.add(Projectile.tiroEspecialInimigo(
                    sx, sy, destinoX, destinoY, vel, alcance
                ).comForca(20f + anel * 5f, anel == 0 ? 7f : 6f));
            }
        }
    }

    /** Investida rápida em direção ao jogador — só na forma 3. */
    private void executarDash(Rectangle player) {
        float centroPlayerX = player.x + player.width / 2f;
        float centroPlayerY = player.y + player.height / 2f;
        float centroBossX = bounds.x + bounds.width / 2f;
        float centroBossY = bounds.y + bounds.height / 2f;

        float dx = centroPlayerX - centroBossX;
        float dy = centroPlayerY - centroBossY;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        if (dist > 0) {
            dx /= dist;
            dy /= dist;
        }

        bounds.x += dx * DASH_DISTANCIA;
        bounds.y += dy * DASH_DISTANCIA;
    }

    @Override
    public void tomarDano(float dano) {
        if (mortoFinal || transicaoTimer > 0f) return; // invulnerável durante a transição

        this.hp -= dano;
        if (this.hp <= 0f) {
            if (forma < 3) {
                forma++; // nunca pula pra forma 3 direto — só +1 por vez
                hpMax = HP_POR_FORMA[forma - 1];
                hp = hpMax;
                velocidade = VELOCIDADE_POR_FORMA[forma - 1];
                danoContato = DANO_CONTATO_POR_FORMA[forma - 1];
                float novoTamanho = TAMANHO_BASE + CRESCIMENTO_TAMANHO_POR_FORMA * (forma - 1);
                redimensionarCentralizado(novoTamanho);
                transicaoTimer = TEMPO_TRANSICAO;
                tiroTimer = intervaloTiro();
            } else {
                hp = 0f;
                ativo = false;
                mortoFinal = true;
            }
        }
    }

    /** Cresce mantendo o boss centralizado no mesmo lugar (em vez de esticar so pra um lado). */
    private void redimensionarCentralizado(float novoTamanho) {
        float centroX = bounds.x + bounds.width / 2f;
        float centroY = bounds.y + bounds.height / 2f;
        bounds.setSize(novoTamanho, novoTamanho);
        bounds.setPosition(centroX - novoTamanho / 2f, centroY - novoTamanho / 2f);
    }

    /** True enquanto o boss deve "piscar"/ficar invulnerável (transição de forma). */
    public boolean estaPiscando() {
        return transicaoTimer > 0f;
    }

    @Override
    public Color getCor() {
        switch (forma) {
            case 3: return new Color(0.85f, 0.05f, 0.05f, 1f); // vermelho intenso
            case 2: return new Color(0.75f, 0.30f, 0.15f, 1f); // laranja-avermelhado
            default: return new Color(0.25f, 0.55f, 0.85f, 1f); // azul-gelo (forma 1)
        }
    }
}
