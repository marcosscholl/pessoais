package io.sicredi.aberturadecontasalarioefetivador.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ValidarPayloadSolicitacaoTest {

    public static final String CPF_TITULAR = "21180506073";
    public static final String CPF_REPRESENTANTE = "74077944058";
    public static final String CNPJ_FONTE_PAGADORA = "18523110000101";
    public static final String NUM_COOPERATIVA = "0101";
    public static final String NUM_AGENCIA = "02";
    public static final String COD_CONVENIO_FONTE_PAGADORA = "ACA";
    public static final String NUM_AG_DESTINO = "12345";
    public static final String COD_BANCO_DESTINO = "748";
    private Validator validator;


    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

    }

    // Teste de validacoes para SolicitacaoRequestDTO
    @Test
    void solicitacaoRequestDTOValidoTeste() {
        SolicitacaoRequestDTO solicitacao = SolicitacaoRequestDTO.builder()
                .numCooperativa(NUM_COOPERATIVA)
                .numAgencia(NUM_AGENCIA)
                .codConvenioFontePagadora(COD_CONVENIO_FONTE_PAGADORA)
                .cnpjFontePagadora(CNPJ_FONTE_PAGADORA)
                .cadastros(List.of(cadastroRequestDTOValido()))
                .configuracao(configuracaoDTOValido())
                .build();

        Set<ConstraintViolation<SolicitacaoRequestDTO>> violations = validator.validate(solicitacao);
        assertThat(violations).isEmpty();
    }

    @Test
    void solicitacaoRequestDTOInvalidoTeste() {
        SolicitacaoRequestDTO solicitacao = SolicitacaoRequestDTO.builder()
                .numCooperativa("12")
                .numAgencia(null)
                .codConvenioFontePagadora(NUM_AG_DESTINO)
                .cnpjFontePagadora(CPF_TITULAR)
                .cadastros(Collections.emptyList())
                .build();

        Set<ConstraintViolation<SolicitacaoRequestDTO>> violations = validator.validate(solicitacao);
        assertThat(violations).hasSize(5);
    }

    // Teste de validacoes para CadastroRequestDTO
    @Test
    void cadastroRequestDTOValidoTeste() {
        CadastroRequestDTO cadastro = cadastroRequestDTOValido();

        Set<ConstraintViolation<CadastroRequestDTO>> violations = validator.validate(cadastro);
        assertThat(violations).isEmpty();
    }

    @Test
    void cadastroRequestDTOInvalidoTeste() {
        CadastroRequestDTO cadastro = CadastroRequestDTO.builder()
                .cpf(COD_BANCO_DESTINO)
                .email("mailmail.com")
                .telefone(COD_BANCO_DESTINO)
                .build();

        Set<ConstraintViolation<CadastroRequestDTO>> violations = validator.validate(cadastro);
        assertThat(violations).hasSize(4);
    }

    @Test
    void cadastroRequestDTORepresentanteInvalidoTeste() {
        CadastroRequestDTO cadastro = CadastroRequestDTO.builder()
                .cpf(CPF_TITULAR)
                .representante(RepresentanteDTO.builder().cpf(CPF_TITULAR).build())
                .build();

        Set<ConstraintViolation<CadastroRequestDTO>> violations = validator.validate(cadastro);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("O titular não pode ser o seu próprio representante");
    }

    @Test
    void listaCadastrosConstraintValidaTeste() {
        List<CadastroRequestDTO> cadastros = List.of(cadastroRequestDTOValido(), cadastroRequestDTOValido());

        Set<ConstraintViolation<List<CadastroRequestDTO>>> violations = validator.validate(cadastros);
        assertThat(violations).isEmpty();
    }

    @Test
    void listaCadastrosConstraintExcedeLimiteECPFNaoUnicoTeste() {
        String cpfUnicoMessage = "A solicitação não pode possuir mais de um cadastro com o mesmo CPF";
        String limiteCadastrosMessage = "A solicitação não pode exceder 300 cadastros";
        SolicitacaoRequestDTO solicitacao = SolicitacaoRequestDTO.builder()
                .numCooperativa(NUM_COOPERATIVA)
                .numAgencia(NUM_AGENCIA)
                .codConvenioFontePagadora(COD_CONVENIO_FONTE_PAGADORA)
                .cnpjFontePagadora(CNPJ_FONTE_PAGADORA)
                .cadastros(Collections.nCopies(302, cadastroRequestDTOValido()))
                .configuracao(configuracaoDTOValido())
                .build();
        Set<ConstraintViolation<SolicitacaoRequestDTO>> violations = validator.validate(solicitacao);
        assertThat(violations).hasSize(2);
        assertThat(violations.stream().anyMatch(violation -> cpfUnicoMessage.equals(violation.getMessage()))).isTrue();
        assertThat(violations.stream().anyMatch(violation -> limiteCadastrosMessage.equals(violation.getMessage()))).isTrue();
    }

    // Teste de validacoes para DocumentoDTO
    @Test
    void documentoDTOValidoTeste() {
        DocumentoDTO documento = documentoDTOValido();

        Set<ConstraintViolation<DocumentoDTO>> violations = validator.validate(documento);
        assertThat(violations).isEmpty();
    }

    @Test
    void documentoDTOInvalidoTeste() {
        DocumentoDTO documento = DocumentoDTO.builder()
                .numDocumento(null)
                .dataEmissaoDoc("")
                .nomeOrgaoEmissorDoc("")
                .sglUfEmissorDoc("XYZ")
                .build();

        Set<ConstraintViolation<DocumentoDTO>> violations = validator.validate(documento);
        assertThat(violations).hasSize(4);
    }

    // Teste de validacoes para EnderecoDTO
    @Test
    void enderecoDTOValidoTeste() {
        EnderecoDTO endereco = enderecoDTOValido();

        Set<ConstraintViolation<EnderecoDTO>> violations = validator.validate(endereco);
        assertThat(violations).isEmpty();
    }

    @Test
    void enderecoDTOInvalidoTeste() {
        EnderecoDTO endereco = EnderecoDTO.builder()
                .tipoLogradouro(null)
                .nomeLogradouro("Rua muito longa que excede cem caracteres e continua crescendo sem limites")
                .numCep(NUM_AG_DESTINO)
                .sglUf("XY")
                .build();

        Set<ConstraintViolation<EnderecoDTO>> violations = validator.validate(endereco);
        assertThat(violations).hasSize(5);
    }

    // Teste de validacoes para PortabilidadeRequestDTO
    @Test
    void portabilidadeRequestDTOValidoTeste() {
        PortabilidadeRequestDTO portabilidade = portabilidadeRequestDTOValido();

        Set<ConstraintViolation<PortabilidadeRequestDTO>> violations = validator.validate(portabilidade);
        assertThat(violations).isEmpty();
    }

    @Test
    void portabilidadeRequestDTOInvalidoTeste() {
        PortabilidadeRequestDTO portabilidade = PortabilidadeRequestDTO.builder()
                .codBancoDestino("12")
                .numAgDestino(null)
                .numContaDestino("123456789012345678901")
                .tipoConta("1")
                .build();

        Set<ConstraintViolation<PortabilidadeRequestDTO>> violations = validator.validate(portabilidade);
        assertThat(violations).hasSize(4);
    }

    // Teste de validacoes para RepresentanteDTO
    @Test
    void representanteDTOValidoTeste() {
        RepresentanteDTO representante = RepresentanteDTO.builder()
                .cpf(CPF_REPRESENTANTE)
                .nome("Representante Teste")
                .build();

        Set<ConstraintViolation<RepresentanteDTO>> violations = validator.validate(representante);
        assertThat(violations).isEmpty();
    }

    @Test
    void representanteDTOInvalidoTeste() {
        RepresentanteDTO representante = RepresentanteDTO.builder()
                .cpf(COD_BANCO_DESTINO)
                .build();

        Set<ConstraintViolation<RepresentanteDTO>> violations = validator.validate(representante);
        assertThat(violations).hasSize(2);
    }

    @Test
    void deveriaValidarTrueQuandoSomenteCNPJPreenchidoEvalido() {
        SolicitacaoRequestDTO dto = SolicitacaoRequestDTO.builder()
                .numCooperativa(NUM_COOPERATIVA)
                .numAgencia(NUM_AGENCIA)
                .codConvenioFontePagadora(COD_CONVENIO_FONTE_PAGADORA)
                .cnpjFontePagadora(CNPJ_FONTE_PAGADORA)
                .cpfFontePagadora(null)
                .cadastros(List.of(cadastroRequestDTOValido()))
                .configuracao(configuracaoDTOValido())
                .build();

        Set<ConstraintViolation<SolicitacaoRequestDTO>> violations = validator.validate(dto);
        boolean hasFontePagadoraError = violations.stream().anyMatch(v ->
                v.getMessage().contains("Apenas um dos campos") ||
                        v.getMessage().contains("É necessário informar"));
        assertFalse(hasFontePagadoraError);
    }

    @Test
    void deveriaValidarTrueQuandoSomenteCPFPreenchidoValido() {
        SolicitacaoRequestDTO dto = SolicitacaoRequestDTO.builder()
                .numCooperativa(NUM_COOPERATIVA)
                .numAgencia(NUM_AGENCIA)
                .codConvenioFontePagadora(COD_CONVENIO_FONTE_PAGADORA)
                .cnpjFontePagadora(null)
                .cpfFontePagadora(CPF_TITULAR)
                .cadastros(List.of(cadastroRequestDTOValido()))
                .configuracao(configuracaoDTOValido())
                .build();

        Set<ConstraintViolation<SolicitacaoRequestDTO>> violations = validator.validate(dto);

        boolean hasFontePagadoraError = violations.stream().anyMatch(v ->
                v.getMessage().contains("Apenas um dos campos") ||
                        v.getMessage().contains("É necessário informar"));
        assertFalse(hasFontePagadoraError);
    }

    @Test
    void deveriaValidarFalsoQuandoAmbosPreenchidosSendoInvalido() {
        SolicitacaoRequestDTO dto = SolicitacaoRequestDTO.builder()
                .numCooperativa(NUM_COOPERATIVA)
                .numAgencia(NUM_AGENCIA)
                .codConvenioFontePagadora(COD_CONVENIO_FONTE_PAGADORA)
                .cnpjFontePagadora(CNPJ_FONTE_PAGADORA)
                .cpfFontePagadora(CPF_TITULAR)
                .cadastros(List.of(cadastroRequestDTOValido()))
                .configuracao(configuracaoDTOValido())
                .build();

        Set<ConstraintViolation<SolicitacaoRequestDTO>> violations = validator.validate(dto);

        boolean hasFontePagadoraError = violations.stream().anyMatch(v ->
                v.getMessage().contains("Apenas um dos campos 'cnpjFontePagadora' ou 'cpfFontePagadora' deve ser informado"));
        assertTrue(hasFontePagadoraError);
    }

    @Test
    void deveriaValidarFalsoQuandoNenhumPreenchidoSendoInvalido() {
        SolicitacaoRequestDTO dto = SolicitacaoRequestDTO.builder()
                .numCooperativa(NUM_COOPERATIVA)
                .numAgencia(NUM_AGENCIA)
                .codConvenioFontePagadora(COD_CONVENIO_FONTE_PAGADORA)
                .cnpjFontePagadora(null)
                .cpfFontePagadora(null)
                .cadastros(List.of(cadastroRequestDTOValido()))
                .configuracao(configuracaoDTOValido())
                .build();

        Set<ConstraintViolation<SolicitacaoRequestDTO>> violations = validator.validate(dto);

        boolean hasFontePagadoraError = violations.stream().anyMatch(v ->
                v.getMessage().contains("É necessário informar 'cnpjFontePagadora' ou 'cpfFontePagadora'"));
        assertTrue(hasFontePagadoraError);
    }

    private CadastroRequestDTO cadastroRequestDTOValido() {
        return CadastroRequestDTO.builder()
                .cpf(CPF_TITULAR)
                .nome("Titular Teste")
                .email("titular@teste.com")
                .telefone("51987654321")
                .endereco(enderecoDTOValido())
                .documento(documentoDTOValido())
                .build();
    }

    private DocumentoDTO documentoDTOValido() {
        return DocumentoDTO.builder()
                .numDocumento("123456")
                .dataEmissaoDoc("10/12/2023")
                .nomeOrgaoEmissorDoc("SSP")
                .sglUfEmissorDoc("RS")
                .build();
    }

    private EnderecoDTO enderecoDTOValido() {
        return EnderecoDTO.builder()
                .tipoLogradouro("Rua")
                .nomeLogradouro("Principal")
                .numEndereco("123")
                .txtComplemento("Apto 101")
                .nomeBairro("Centro")
                .numCep("93700000")
                .nomeCidade("Campo Bom")
                .sglUf("RS")
                .build();
    }

    private ConfiguracaoDTO configuracaoDTOValido() {
        return ConfiguracaoDTO.builder()
                .urlWebhook("https://webhook.com.br/callback")
                .portaHttp("443")
                .build();
    }

    private PortabilidadeRequestDTO portabilidadeRequestDTOValido() {
        return PortabilidadeRequestDTO.builder()
                .codBancoDestino(COD_BANCO_DESTINO)
                .numAgDestino(NUM_AG_DESTINO)
                .numContaDestino("1234567890")
                .tipoConta("01")
                .build();
    }
}
