package io.sicredi.aberturadecontasalarioefetivador.controller;

import io.sicredi.aberturadecontasalarioefetivador.dto.CadastroRequestDTO;
import io.sicredi.aberturadecontasalarioefetivador.dto.ConfiguracaoDTO;
import io.sicredi.aberturadecontasalarioefetivador.dto.SolicitacaoRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.SecureRandom;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RestController
@RequestMapping("/geradorsolicitacao")
@RequiredArgsConstructor
@Slf4j
public class GeradorSolicitacaoCadastroContaSalarioController {

    private static final Random RANDOM = new SecureRandom();
    private static final String FOLHA_IB = "FOLHA_IB";

    private final SolicitacaoContaSalarioController solicitacaoContaSalarioController;
    private final TransactionIdController transactionIdController;

    @PostMapping
    public ResponseEntity<?> criarSolicitacao(@RequestParam(defaultValue = "10") int quantidadeCadastros,
                                              @RequestParam(defaultValue = "true") boolean apenasCPFRegular,
                                              @RequestParam(defaultValue = "true") boolean cadastroMinimo,
                                              @RequestParam(required = false) String urlWebhook) {
        String transactionId = (String) transactionIdController.transactionId().getBody();

        var solicitacaoRequestDTO = criarSolicitacaoRequestDTO(quantidadeCadastros, apenasCPFRegular, cadastroMinimo, urlWebhook);

        return solicitacaoContaSalarioController.solicitacao(solicitacaoRequestDTO, transactionId, FOLHA_IB, null);
    }

    private SolicitacaoRequestDTO criarSolicitacaoRequestDTO(int quantidadeCadastros,
                                                             boolean apenasCPFRegular,
                                                             boolean cadastroMinimo,
                                                             String urlWehbook) {
        var cadastros = gerarListaCPF(quantidadeCadastros, apenasCPFRegular).stream()
                .map(cpf -> criarCadastro(cadastroMinimo, cpf))
                .toList();

        SolicitacaoRequestDTO.SolicitacaoRequestDTOBuilder solicitacaoBuilder = SolicitacaoRequestDTO.builder()
                .numCooperativa("0167")
                .numAgencia("17")
                .codConvenioFontePagadora("3AO")
                .cnpjFontePagadora("18523110000101")
                .cadastros(cadastros);

        if (StringUtils.isNotBlank(urlWehbook))  {
            solicitacaoBuilder.configuracao(ConfiguracaoDTO.builder()
                    .urlWebhook(urlWehbook)
                    .build());
        }
        return solicitacaoBuilder.build();
    }

    private CadastroRequestDTO criarCadastro(boolean cadastrominimo, String cpf) {
        if (cadastrominimo) {
            return criarCadastroMinimoRequestDTO(cpf);
        }
        return criarCadastroRequestDTO(cpf);

    }
    private CadastroRequestDTO criarCadastroRequestDTO(String cpf) {
        return CadastroRequestDTO.builder()
                .cpf(cpf)
                .nome("ASSOCIADO CONTA SALARIO " + cpf)
                .dataNascimento("08/05/1978")
                .flgSexo("M")
                .email("associado.conta.salario@gmail.com")
                .telefone("51999999999")
                .build();
    }

    private CadastroRequestDTO criarCadastroMinimoRequestDTO(String cpf) {
        return CadastroRequestDTO.builder()
                .cpf(cpf)
                .build();
    }

    private List<String> gerarListaCPF(int quantidade,  boolean apenasRegular) {
        return IntStream.range(0, quantidade)
                .mapToObj(i -> geradorCPF(apenasRegular))
                .toList();
    }

    private String geradorCPF(boolean apenasRegular) {
        var cpf = IntStream.range(0, 9)
                .map(i -> RANDOM.nextInt(10))
                .toArray();
        cpf = geraCPFRegularEIregular(cpf, apenasRegular);
        cpf = addDigitoVerificador(cpf);
        return IntStream.of(cpf)
                .mapToObj(String::valueOf)
                .collect(Collectors.joining());
    }

    private int[] geraCPFRegularEIregular(int[] cpf, boolean apenasRegular) {
        int[] fullCpf = new int[9];
        System.arraycopy(cpf, 0, fullCpf, 0, 8);
        int digitForRegular = RANDOM.nextBoolean() ? 2 : 0;
        fullCpf[8] = apenasRegular ? digitForRegular : RANDOM.nextInt(10);
        return fullCpf;
    }

    private int[] addDigitoVerificador(int[] cpf) {
        int[] fullCpf = new int[11];
        System.arraycopy(cpf, 0, fullCpf, 0, 9);
        fullCpf[9] = calcularDigito(cpf, 10);
        fullCpf[10] = calcularDigito(fullCpf, 11);
        return fullCpf;
    }

    private int calcularDigito(int[] cpf, int fator) {
        int soma = IntStream.range(0, fator - 1)
                .map(i -> cpf[i] * (fator - i))
                .sum();
        int resto = 11 - (soma % 11);
        return resto >= 10 ? 0 : resto;
    }
}