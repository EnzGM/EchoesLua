package com.Echoes.Jogo.Entities;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Inventário de posse — chaves dos bosses (quantidade sempre 1, nunca
 * consumidas) E, a partir do ITEM 22, materiais empilháveis de crafting
 * (GELO, PECA, METAL, CIRCUITO, etc.), que podem ter mais de 1 unidade e
 * são consumidos ao craftar.
 *
 * Por baixo dos panos virou um Map<String,Integer> (era um Set<String>),
 * mas tem/add/getItens/serializar/carregarDe continuam funcionando pra quem
 * já usava isso só como "tem ou não tem" (BossLua/Marte/Tita/Calisto).
 */
public class Inventario {

    private final Map<String, Integer> itens = new LinkedHashMap<>();

    /** Adiciona 1 unidade (uso normal pra chaves). */
    public void add(String item) {
        add(item, 1);
    }

    /** Adiciona "qtd" unidades — usado pelos materiais empilháveis do crafting. */
    public void add(String item, int qtd) {
        if (item == null || item.isEmpty() || qtd <= 0) return;
        itens.merge(item, qtd, Integer::sum);
    }

    public boolean tem(String item) {
        return getQuantidade(item) > 0;
    }

    public boolean tem(String item, int qtd) {
        return getQuantidade(item) >= qtd;
    }

    public int getQuantidade(String item) {
        Integer q = itens.get(item);
        return q != null ? q : 0;
    }

    /** Remove o item por completo, seja qual for a quantidade (chaves, etc). */
    public void remove(String item) {
        itens.remove(item);
    }

    /**
     * ITEM 22: consome "qtd" unidades de um material empilhável — usado pela
     * bancada de crafting. Retorna false (e não mexe em nada) se não tiver
     * quantidade suficiente.
     */
    public boolean remover(String item, int qtd) {
        int atual = getQuantidade(item);
        if (atual < qtd) return false;

        int restante = atual - qtd;
        if (restante <= 0) itens.remove(item);
        else itens.put(item, restante);
        return true;
    }

    public Set<String> getItens() {
        return itens.keySet();
    }

    /** Serializa pra salvar no Preferences, ex.: "CHAVE_LUA:1,GELO:3". */
    public String serializar() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Integer> e : itens.entrySet()) {
            if (sb.length() > 0) sb.append(",");
            sb.append(e.getKey()).append(":").append(e.getValue());
        }
        return sb.toString();
    }

    /** Reconstrói a partir da String salva — aceita tambem o formato antigo sem ":qtd" (assume 1). */
    public void carregarDe(String serializado) {
        itens.clear();
        if (serializado == null || serializado.isEmpty()) return;

        for (String par : serializado.split(",")) {
            if (par.isEmpty()) continue;
            String[] partes = par.split(":");
            if (partes.length == 2) {
                try {
                    itens.put(partes[0], Integer.parseInt(partes[1]));
                } catch (NumberFormatException ignored) {
                    itens.put(partes[0], 1);
                }
            } else {
                itens.put(partes[0], 1); // save antigo, sem quantidade
            }
        }
    }
}
