package com.Echoes.Jogo.Entities;

import com.Echoes.Jogo.Managers.Difficulty;
import java.util.HashSet;
import java.util.Set;

public class PlayerStatus {

    public Difficulty dificuldade = Difficulty.NORMAL;
    public Set<String> chefesMortos = new HashSet<>();

    public float hp = 100f;
    public int comida = 0;
    public int inventarioGelo = 0;
    public int agua = 0;
    public int combustivel = 0;

    // ITEM 22: moeda da loja.
    public int creditos = 0;

    // Loja: upgrades permanentes de arma e drone.
    public int nivelUpgradeArma = 0;
    public int nivelUpgradeDrone = 0;

    // ITEM 23: status especial temporario recebido por hits especiais.
    public String efeitoTipo = "";
    public float efeitoTimer = 0f;

    // ITEM 24: checkpoint independente do sistema CONTINUAR do menu.
    public boolean temCheckpoint = false;
    public float checkpointX = 200f;
    public float checkpointY = 200f;
    public String checkpointFase = "";

    // ITEM 28: recurso perdido no local da ultima morte. Uma nova morte substitui o drop antigo.
    public Drop dropMorte = null;

    // ITEM 25: carga do ataque com SPACE.
    public float cargaAtaque = 0f;
    private boolean spaceAnterior = false;
    private static final float EFEITO_DURACAO = 4f;
    private static final float VENENO_HP_POR_SEGUNDO = 6f;

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

    // REGRA VISIVEL: se a municao zerar, depois de 8s sem municao o jogador
    // recebe 10 de volta automaticamente (pra nunca ficar travado sem poder atirar).
    private float timerMunicaoZerada = 0f;
    public static final float REGEN_MUNICAO_ZERADA_DELAY = 8f;
    public static final int REGEN_MUNICAO_ZERADA_QTD = 10;

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

    // ITEM 16: true assim que as 3 waves de Marte forem vencidas (antes do Boss
    // aparecer). Junto com estufaReparada (trazido da Lua), forma a condicao
    // marteMissoesOk que libera o spawn do Boss de Marte.
    public boolean marteWavesConcluidas = false;

    // ITEM 17: true assim que os 3 guardioes comuns de Tita forem derrotados
    // (o "combate de prova" que libera o Boss de Tita).
    public boolean titaGuardioesDerrotados = false;

    // ITEM 22: materiais brutos coletados no mapa, usados na bancada de crafting.
    // Contáveis (diferente das chaves em "inventario", que são só posse/ausência).
    public int metal = 0;
    public int circuito = 0;

    // ITEM 22: itens craftados na bancada.
    public int filtroO2 = 0;

    // ITEM 15+: inventário de posse (chaves dos bosses, amostras, etc).
    // Usado pelo BossLua (CHAVE_LUA) e por todos os próximos bosses/portais
    // especiais (Marte, Titã, Calisto, Aharin).
    public Inventario inventario = new Inventario();

    // ITEM 24: true enquanto o drone companheiro estiver "chamado" (tecla C).
    // Persistido no save pra ele voltar sozinho apos um checkpoint/continue,
    // sem o jogador precisar chamar de novo.
    public boolean droneAtivo = false;

    public void update(float delta) {
        if (missaoFalhou) return;

        atualizarCombate(delta);
    }


    // ITEM 24: grava a posição da estátua/painel como ponto de respawn.
    public void salvarCheckpoint(float x, float y, String fase) {
        checkpointX = x;
        checkpointY = y;
        checkpointFase = fase != null ? fase : "";
        temCheckpoint = true;
    }

    // ITEM 24: ao morrer, volta ao último checkpoint e não ao ponto inicial.
    public void respawnNoCheckpoint() {
        if (!temCheckpoint) return;
        hp = 25f;
        missaoFalhou = false;
        efeitoTipo = "";
        efeitoTimer = 0f;
    }

    // ITEM 28: ao morrer, perde uma parte dos recursos e deixa um marcador no chao.
    public void criarDropMorte(float x, float y) {
        int perdeuCreditos = Math.min(10, Math.max(0, creditos));
        int perdeuMunicao = Math.min(3, Math.max(0, municao));
        creditos -= perdeuCreditos;
        municao -= perdeuMunicao;
        dropMorte = new Drop(x + 16f, y + 16f, "CREDITOS_MUNICAO", perdeuCreditos, perdeuMunicao);
    }

    // ITEM 28: recolher o marcador devolve exatamente o que caiu.
    public boolean recolherDrop() {
        if (dropMorte == null) return false;
        creditos += dropMorte.creditos;
        municao += dropMorte.municao;
        dropMorte = null;
        return true;
    }

    // ITEM 25: devolve 1 quando soltou SPACE com carga suficiente e 0 para tiro normal.
    public int atualizarCargaAtaque(float delta, boolean spacePressionado) {
        if (spacePressionado) {
            cargaAtaque = Math.min(2.5f, cargaAtaque + delta);
        } else if (spaceAnterior) {
            int resultado = cargaAtaque >= 0.8f ? 2 : 1;
            cargaAtaque = 0f;
            spaceAnterior = false;
            return resultado;
        }
        spaceAnterior = spacePressionado;
        return 0;
    }

    public float progressoCargaAtaque() {
        return Math.min(1f, cargaAtaque / 0.8f);
    }

    /** Atualiza apenas o combate e os efeitos temporarios. */
    public void atualizarCombate(float delta) {
        if (cooldownTiro > 0f) {
            cooldownTiro -= delta;
            if (cooldownTiro < 0f) cooldownTiro = 0f;
        }

        atualizarRegenMunicaoZerada(delta);
        atualizarEfeitoEspecial(delta);
    }

    // ITEM 23: gelo dura alguns segundos e reduz a velocidade; veneno causa dano por segundo.
    private void atualizarEfeitoEspecial(float delta) {
        if (efeitoTimer <= 0f) {
            efeitoTimer = 0f;
            efeitoTipo = "";
            return;
        }

        if ("VENENO".equals(efeitoTipo)) {
            hp -= VENENO_HP_POR_SEGUNDO * dificuldade.multiplicadorDanoRecebido * delta;
            if (hp <= 0f) {
                hp = 0f;
                missaoFalhou = true;
            }
        }

        efeitoTimer -= delta;
        if (efeitoTimer <= 0f) {
            efeitoTimer = 0f;
            efeitoTipo = "";
        }
    }

    public void aplicarEfeitoEspecial(String tipo) {
        if (!"GELO".equals(tipo) && !"VENENO".equals(tipo)) return;
        efeitoTipo = tipo;
        efeitoTimer = EFEITO_DURACAO;
    }

    public float getMultiplicadorVelocidade() {
        return "GELO".equals(efeitoTipo) && efeitoTimer > 0f ? 0.5f : 1f;
    }

    /** REGRA VISIVEL: municao zerada -> 8s depois, +10 municao automaticamente. */
    private void atualizarRegenMunicaoZerada(float delta) {
        if (municao <= 0) {
            timerMunicaoZerada += delta;
            if (timerMunicaoZerada >= REGEN_MUNICAO_ZERADA_DELAY) {
                municao += REGEN_MUNICAO_ZERADA_QTD;
                timerMunicaoZerada = 0f;
            }
        } else {
            timerMunicaoZerada = 0f;
        }
    }

    /** 0 a 1: quanto falta pro proximo "tick" de regen de municao (util pra HUD, se quiser mostrar). */
    public float progressoRegenMunicaoZerada() {
        return municao > 0 ? 0f : Math.min(1f, timerMunicaoZerada / REGEN_MUNICAO_ZERADA_DELAY);
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
        hp = Math.min(100f, hp + 20f * delta);
    }

    /** Regenera parte da vida ao trocar de fase (Lua -> Marte -> Tita), via portal. */
    public void curarAoTrocarFase() {
        hp = Math.min(100f, hp + 40f);
    }

    public boolean processarGelo() {
        if (inventarioGelo <= 0) return false;
        inventarioGelo--;
        agua += 1;
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

    public void sofrerDano(float dano) {
        hp -= dano * dificuldade.multiplicadorDanoRecebido;
        if (hp <= 0f) { hp = 0f; missaoFalhou = true; }
    }

    /** Drop de materiais: todo inimigo deixa um material usado no crafting. */
    public String materialDrop(int seed) {
        switch (Math.abs(seed) % 4) {
            case 0: return "METAL";
            case 1: return "CIRCUITO";
            case 2: return "GELO";
            default: return "PECA";
        }
    }

    public void registrarChefeMorto(String id) { if (id != null) chefesMortos.add(id); }

    public boolean isMorto() {
        return hp <= 0f;
    }
}
