package enums;

public enum MissionState {
    COLETAR_PECAS("Missão: Coletar 5 Peças na Lua"),
    REPARAR_ESTUFA("Missão: Reparar a Estufa da Base"),
    CRAFTAR_ARMA("Missão: Craftar a Arma"),
    IR_PARA_MARTE("Missão: Entrar no Portal para Marte"),
    VITORIA("Missão Concluída! Você sobreviveu.");

    private String descricaoHUD;

    // Construtor
    MissionState(String descricaoHUD) {
        this.descricaoHUD = descricaoHUD;
    }

    // Retorna o texto formatado para o HUD
    public String getDescricaoHUD() {
        return descricaoHUD;
    }
}
