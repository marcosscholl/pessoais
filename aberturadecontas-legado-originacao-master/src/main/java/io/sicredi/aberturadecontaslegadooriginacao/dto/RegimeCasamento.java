package io.sicredi.aberturadecontaslegadooriginacao.dto;

public enum RegimeCasamento {
    PARTIAL_COMMUNION_GOODS("COMUNHAO_PARCIAL_DE_BENS"),
    UNIVERSAL_COMMUNION_GOODS("COMUNHAO_UNIVERSAL_DE_BENS"),
    FINAL_PARTICIPATION_PROCEEDINGS("PARTICIPACAO_FINAL_NOS_AQUESTOS"),
    TOTAL_SEPARATION_GOODS("SEPARACAO_DE_BENS");

    private final String descricao;

    RegimeCasamento(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
