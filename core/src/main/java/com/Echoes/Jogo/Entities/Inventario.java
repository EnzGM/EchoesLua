package com.Echoes.Jogo.Entities;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Inventário simples de posse (chaves, amostras, etc.) — não guarda quantidade,
 * só se o jogador "tem" ou não um item identificado por String.
 *
 * Usado pelo Boss da Lua (item 15) e por todos os próximos bosses/portais
 * especiais do checklist (Marte, Titã, Calisto, Aharin), que fazem
 * inventario.add("CHAVE_X") ao derrotar um chefe e inventario.tem("CHAVE_X")
 * pra liberar o portal correspondente.
 */
public class Inventario {

    private final Set<String> itens = new LinkedHashSet<>();

    public void add(String item) {
        if (item != null && !item.isEmpty()) {
            itens.add(item);
        }
    }

    public boolean tem(String item) {
        return itens.contains(item);
    }

    public void remove(String item) {
        itens.remove(item);
    }

    public Set<String> getItens() {
        return itens;
    }

    /** Serializa pra salvar no Preferences (SaveManager), ex.: "CHAVE_LUA,CHAVE_MARTE". */
    public String serializar() {
        return String.join(",", itens);
    }

    /** Reconstrói o inventário a partir da String salva pelo serializar(). */
    public void carregarDe(String serializado) {
        itens.clear();
        if (serializado == null || serializado.isEmpty()) return;
        for (String s : serializado.split(",")) {
            if (!s.isEmpty()) {
                itens.add(s);
            }
        }
    }
}
