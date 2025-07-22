package io.sicredi.aberturadecontaslegadooriginacao.dto;

public enum CondicaoPessoal {
    UNDERAGE("MENOR"),
    EMANCIPATED("EMANCIPADO"),
    OF_AGE("MAIOR"),
    OLD_AGE("MAIOR_60");

    private final String descricao;

    CondicaoPessoal(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
