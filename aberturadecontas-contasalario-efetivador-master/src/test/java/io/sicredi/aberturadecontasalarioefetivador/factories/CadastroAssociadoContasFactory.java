package io.sicredi.aberturadecontasalarioefetivador.factories;

import io.sicredi.aberturadecontasalarioefetivador.dto.CadastroAssociadoContasDTO;

import java.util.List;

public class CadastroAssociadoContasFactory {

    public static final String ATIVO = "ATIVO";
    public static final String ATIVA = "ATIVA";
    public static final String TIPO_CONTA_SALARIO = "CONTA_SALARIO";
    public static final String TIPO_RELACIONAMENTO = "TITULAR";
    public static final String NOME = "SCHOLL CONTA SALARIO";
    public static final String DATA_NASCIMENTO_CONSTITUICAO = "08/05/1978";
    public static final String ORIGINACAO_LEGADO = "LEGADO";
    public static final String ORIGINACAO_DIGITAL = "DIGITAL";
    public static final String ENCERRADA = "ENCERRADA";


    public static CadastroAssociadoContasDTO criaCadastroAssociadoContas(boolean isDigital) {
        return new CadastroAssociadoContasDTO(
                "0167",
                "17",
                "903677",
                "2024-11-07",
                "4712-01-01",
                "4712-01-01",
                false,
                isDigital ? ORIGINACAO_DIGITAL : ORIGINACAO_LEGADO,
                "1",
                "1",
                new CadastroAssociadoContasDTO.StatusRelacionamento(ATIVO),
                List.of(new CadastroAssociadoContasDTO.Tipo(TIPO_CONTA_SALARIO, ATIVA)),
                List.of(new CadastroAssociadoContasDTO.Relacionamento(TIPO_RELACIONAMENTO, NOME, "00000000000", DATA_NASCIMENTO_CONSTITUICAO, ORIGINACAO_LEGADO))
        );
    }

    public static CadastroAssociadoContasDTO criaCadastroAssociadoContaEncerrada() {
        return new CadastroAssociadoContasDTO(
                "0167",
                "17",
                "903676",
                "2024-11-07",
                "2024-10-07",
                "4712-01-01",
                false,
                ORIGINACAO_LEGADO,
                "1",
                "1",
                new CadastroAssociadoContasDTO.StatusRelacionamento(ATIVO),
                List.of(new CadastroAssociadoContasDTO.Tipo(TIPO_CONTA_SALARIO, ENCERRADA)),
                List.of(new CadastroAssociadoContasDTO.Relacionamento(TIPO_RELACIONAMENTO, NOME, "00000000000", DATA_NASCIMENTO_CONSTITUICAO, ORIGINACAO_LEGADO))
        );
    }

    public static String criaCadastroAssociadoContasJson() {
        return """
                [
                  {
                    "cooperativa": "0167",
                    "posto": "17",
                    "conta": "903677",
                    "dataAbertura": "2024-11-07",
                    "dataEncerramento": "4712-01-01",
                    "clienteDesde": "4712-01-01",
                    "identificacaoDepositante": false,
                    "originacao": "LEGADO",
                    "tipoPessoa": "1",
                    "tipoAplicador": "1",
                    "statusRelacionamento": {
                      "status": "ATIVO"
                    },
                    "tipos": [
                      {
                        "tipo": "CONTA_SALARIO",
                        "status": "ATIVA"
                      }
                    ],
                    "relacionamentos": [
                      {
                        "tipoRelacionamento": "TITULAR",
                        "nome": "SCHOLL CONTA SALARIO",
                        "documento": "00000000000",
                        "dataNascimentoConstituicao": "08/05/1978",
                        "originacao": "LEGADO"
                      }
                    ]
                  }
                ]
                """;
    }

    public static String criaCadastroAssociadoContasJsonComPropriedadesDesconhecidas() {
        return """
                [
                  {
                    "cooperativa": "0167",
                    "posto": "17",
                    "conta": "903677",
                    "dataAbertura": "2024-11-07",
                    "dataEncerramento": "4712-01-01",
                    "clienteDesde": "4712-01-01",
                    "identificacaoDepositante": false,
                    "originacao": "LEGADO",
                    "tipoPessoa": "1",
                    "tipoAplicador": "1",
                    "statusRelacionamento": {
                      "status": "ATIVO"
                    },
                    "tipos": [
                      {
                        "tipo": "CONTA_SALARIO",
                        "status": "ATIVA"
                      }
                    ],
                    "relacionamentos": [
                      {
                        "tipoRelacionamento": "TITULAR",
                        "nome": "SCHOLL CONTA SALARIO",
                        "documento": "20643481400",
                        "dataNascimentoConstituicao": "08/05/1978",
                        "originacao": "LEGADO"
                      }
                    ],
                    "propriedadeDesconhecida": "valorDesconhecido"
                  }
                ]
                """;
    }

    public static String criaCadastroAssociadoContasJsonComValoresNulos() {
        return """
                [
                  {
                    "cooperativa": null,
                    "posto": "17",
                    "conta": "903677",
                    "dataAbertura": null,
                    "dataEncerramento": "4712-01-01",
                    "clienteDesde": "4712-01-01",
                    "identificacaoDepositante": null,
                    "originacao": "LEGADO",
                    "tipoPessoa": "1",
                    "tipoAplicador": "1",
                    "statusRelacionamento": {
                      "status": "ATIVO"
                    },
                    "tipos": [
                      {
                        "tipo": "CONTA_SALARIO",
                        "status": "ATIVA"
                      }
                    ],
                    "relacionamentos": [
                      {
                        "tipoRelacionamento": "TITULAR",
                        "nome": "SCHOLL CONTA SALARIO",
                        "documento": "20643481400",
                        "dataNascimentoConstituicao": "08/05/1978",
                        "originacao": "LEGADO"
                      }
                    ]
                  }
                ]
                """;
    }
}
