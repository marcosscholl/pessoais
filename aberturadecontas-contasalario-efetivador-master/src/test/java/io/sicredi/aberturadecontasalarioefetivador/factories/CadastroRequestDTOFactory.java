package io.sicredi.aberturadecontasalarioefetivador.factories;

import io.sicredi.aberturadecontasalarioefetivador.dto.CadastroRequestDTO;
import io.sicredi.aberturadecontasalarioefetivador.dto.DocumentoDTO;
import io.sicredi.aberturadecontasalarioefetivador.dto.EnderecoDTO;

public class CadastroRequestDTOFactory {
    public static CadastroRequestDTO cadastroCompleto(String cpf) {
        return CadastroRequestDTO.builder()
                .cpf(cpf)
                .nome("Cadastro Conta Salario")
                .dataNascimento("08/05/1978")
                .flgSexo("M")
                .email("cadastro.conta.salario@gmail.com")
                .telefone("51999999999")
                .documento(documento())
                .endereco(endereco())
                .build();
    }

    private static EnderecoDTO endereco() {
        return EnderecoDTO.builder()
                .tipoLogradouro("Rua")
                .nomeLogradouro("Rua Boa Saúde")
                .numEndereco("266")
                .txtComplemento("APTO 999")
                .nomeBairro("Primavera")
                .numCep("93344460")
                .nomeCidade("Novo Hamburgo")
                .sglUf("RS")
                .build();
    }

    private static DocumentoDTO documento() {
        return DocumentoDTO.builder()
                .numDocumento("111111111")
                .dataEmissaoDoc("08/05/2010")
                .nomeOrgaoEmissorDoc("SSP")
                .sglUfEmissorDoc("RS")
                .build();
    }
}
