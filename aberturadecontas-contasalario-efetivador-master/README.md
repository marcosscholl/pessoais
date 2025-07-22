---
title: aberturadecontas-contasalario-efetivador
linktitle: aberturadecontas-contasalario-efetivador
geekdocCollapseSection: true
---


[![Quality Gate Status](https://sonarqube.sicredi.net/api/project_badges/measure?project=aberturadecontas-contasalario.aberturadecontas-contasalario-efetivador&metric=alert_status&token=sqb_7e64041b8a776eebf1921b71bfd09da8db5c74d5)](https://sonarqube.sicredi.net/dashboard?id=aberturadecontas-contasalario.aberturadecontas-contasalario-efetivador)
[![Bugs](https://sonarqube.sicredi.net/api/project_badges/measure?project=aberturadecontas-contasalario.aberturadecontas-contasalario-efetivador&metric=bugs&token=sqb_7e64041b8a776eebf1921b71bfd09da8db5c74d5)](https://sonarqube.sicredi.net/dashboard?id=aberturadecontas-contasalario.aberturadecontas-contasalario-efetivador)
[![Code Smells](https://sonarqube.sicredi.net/api/project_badges/measure?project=aberturadecontas-contasalario.aberturadecontas-contasalario-efetivador&metric=code_smells&token=sqb_7e64041b8a776eebf1921b71bfd09da8db5c74d5)](https://sonarqube.sicredi.net/dashboard?id=aberturadecontas-contasalario.aberturadecontas-contasalario-efetivador)
[![Duplicated Lines (%)](https://sonarqube.sicredi.net/api/project_badges/measure?project=aberturadecontas-contasalario.aberturadecontas-contasalario-efetivador&metric=duplicated_lines_density&token=sqb_7e64041b8a776eebf1921b71bfd09da8db5c74d5)](https://sonarqube.sicredi.net/dashboard?id=aberturadecontas-contasalario.aberturadecontas-contasalario-efetivador)
[![Coverage](https://sonarqube.sicredi.net/api/project_badges/measure?project=aberturadecontas-contasalario.aberturadecontas-contasalario-efetivador&metric=coverage&token=sqb_7e64041b8a776eebf1921b71bfd09da8db5c74d5)](https://sonarqube.sicredi.net/dashboard?id=aberturadecontas-contasalario.aberturadecontas-contasalario-efetivador)

---

- [🎯 Objetivo](#-objetivo)
- [🛠️ Stack](#-stack-utilizada)
- [👀 Visão da solução](#-visão-da-solução)
- [🔗 APIs](#-apis)
- [🚀 Rodando a aplicação](#-rodando-a-aplicação)
- [📁 Documentação Complementar](#-documentação-complementar)
- [🤔 Dúvidas ou contribuições](#-dúvidas-ou-contribuições)
- [📖 Decision Logs](#-decision-logs)

---

## 🎯 Objetivo

A solução API Conta Salário tem como objetivo permitir que empresas integrem facilmente seus sistemas próprios ou terceiros
(plataformas ERPs), com o serviço de abertura de conta salário, tornando mais simples e eficiente o processo de inclusão de
seu assalariado. Esta aplicação permite a solicitação de abertura de conta salário através de uma requisição REST,
que possui resposta assíncrona, informando o usuário sobre o sucesso ou falha na criação da solicitação de conta salário.
A partir do sucesso na solicitação são realizadas algumas integrações de forma assíncrona que efetivam a abertura de conta salário.
O associado receberá o status da solicitação via Webhook ou poderá consultar o status em um enpoint de consulta.

- **Funcionalidades da API Conta Salário:**
- - Criação de cadastro com Conta Salário
- - Solicitação de conta salário modalidade Saque
- - Solicitação de conta salário modalidade Portabilidade
- - Consultar solicitações
- - Consultar instituições financeiras
- - Configurar o Webhook

### Responsáveis
```
- Diretoria: DEPN
- Vertical: Associação e Contas
- Time: Capitão Esteira
```

### Equipe
```
- Devs    
    - almeida_cassio@sicredi.com.br
    - bruno.miossi@level4.com.br
    - marcos.takata@level4.com.br
    - saulo_machado@sicredi.com.br
- TechLead
    - vinicius.scholl@level4.com.br
- QAs
    - assucena.araujo@dbccompany.com.br
    - bruna_lemos@sicredi.com.br
    - joao.martins@dbccompany.com.br
    - limaa@dbserver.com.br
- Bsa
    - marcia_andrades@sicredi.com.br
- Ba
    - karoline_rocha@sicredi.com.br
- Pm
    - gomes_william@sicredi.com.br
```
### Referências

- *DevConsole:* https://devconsole.sicredi.in/catalog/application/aberturadecontas-contasalario/aberturadecontas-contasalario-efetivador

## 🛠️ Stack Utilizada

**Back-end:**

- Java 21
- Lombok 8.6
- Gradle 8.5
- Spring Boot 3.2.5
- Jib 3.4.1
- Sicredi Observability 2.2.+
- Junit Jupiter 5.10.2
- Mockito 5.7.0
- Assertj Core 3.24.2
- Wiremock 4.1.2

## 👀 Visão da solução

Micro serviço que possibilita abrir conta salario individualmente ou por lote, através de requisição rest, a um canal pré-configurado. As solicitações são processadas de forma assíncrona, com notificações via Webhook e suporte a consultas detalhadas.

## 🔗 APIs

### Swagger
- DEV: https://aberturadecontas-contasalario-efetivador.dev.sicredi.cloud/swagger-ui/index.html
- UAT: https://aberturadecontas-contasalario-efetivador.uat.sicredi.cloud/swagger-ui/index.html

### **Endpoints**

A documentação dos endpoints abaixo possui dados pré-montados para maior praticidade no momento da integração e testes, logo os exemplos de requisições são validos.
Em ambiente produtivo os contratos e rotas são os mesmos, sendo necessário apenas realizar a troca da URL base pela URL base do ambiente produtivo.
A solução permite realizar solicitação de conta salário em três modalidades, além de possibilitar a consulta de solicitação recebida e uma consulta de instituições financeiras.
- **Conta Salário Saque:** Abertura de conta para uso do saldo na própria conta ou saque
- **Conta Salário Portabilidade:** Abertura de conta com saldo em portabilidade interna ou outra instituição financeira
- **Conta Salário com Representante:** Abertura de conta com representante ao titular
- **Consultar Solicitação:** Buscar uma solicitação preexistente
- **Consultar Instituições Financeiras:** Buscar lista de instituição financeiras para portabilidade

Nos exemplos abaixo, as modalidades serão exibidas de maneira individual, e é apresentado a opção de uso de dados mínimos e dados completos, a diferença se dá apenas no preenchimento das informações cadastrais opcionais.

Os processos transacionais ou de encerramento não fazem parte dessa componente até o presente momento.

Ambientes requisição:
- Dev: https://aberturadecontas-contasalario-efetivador.dev.sicredi.cloud/
- Uat: https://aberturadecontas-contasalario-efetivador.uat.sicredi.cloud/
- Prd: https://aberturadecontas-contasalario-efetivador.prd.sicredi.cloud/

### **- Especificação Header Canal**
O canal representa a origem da solicitação de abertura de conta salário. Cada canal é configurado e ativado especificamente para um parceiro. Ele é composto por:

| Nome       | Descrição                                      |
|------------|------------------------------------------------|
| Canal      | Nome único enviado no cabeçalho da solicitação |
| Código     | Código numérico de 4 dígitos associado ao canal |

### **- Especificação Header TransactionId**
O `TransactionId` é o identificador único da transação da solicitação, composto por:

| Componente       | Descrição                             | Exemplo         |
|-------------------|---------------------------------------|-----------------|
| Data da requisição | Data no formato YYYYMMDD             | `20250113`      |
| Canal             | Código do canal de 4 dígitos         | `1234`          |
| Chave aleatória   | Sequência de 16 dígitos aleatórios    | `1234567890123456` |

Exemplo completo: `2025011312341234567890123456`

---

### Lista de endpoints disponíveis:


<details>
<summary>1.Criar solicitação de conta salário - Modalidade Saque</summary>

**Descrição:** Endpoint para criar uma solicitação de conta salário.

- **[POST] /solicitacao**
- Descrição: Realiza uma solicitação de cadastro de conta salário para a empresa convênio informada.
- Escopo: abertura.contasalario.solicitar


Estrutura completa do Body request para criação de solicitação.
Os objetos que não são obrigatórios podem ser suprimidos da requsição, conforme exemplo.

**Parâmetros de Entrada : Body Request**

| Atributo                | Local  | Tipo              | Limitações                                | Descrição                                                                 |
|-------------------------|--------|-------------------|-------------------------------------------|---------------------------------------------------------------------------|
| Authorization           | header | texto             | Deve ser um token válido                  | Bearer token de autenticação                                             |
| TransactionId           | header | texto             | Não em branco, comprimento fixo 28 dígitos | Identificador único para rastrear a solicitação e garantir idempotência  |
| Canal                   | header | texto             | Não em branco                             | Canal de origem da solicitação                                           |
| Authorization-Callback  | header | texto             | Não em branco                             | Api Key para ser cabeçalho na requisição de resposta do Webhook          |
| numCooperativa          | body   | texto             | Não nulo, comprimento fixo de 4 dígitos  | Número da cooperativa onde a conta salário está sendo aberta             |
| numAgencia              | body   | texto             | Não nulo, comprimento fixo de 2 dígitos  | Número da agência onde a conta salário está sendo aberta                 |
| codConvenioFontePagadora| body   | texto             | Não nulo                                  | Código do convênio da fonte pagadora                                     |
| cnpjFontePagadora       | body   | texto             | Não nulo, deve ser um CNPJ válido (14 dígitos) | CNPJ da fonte pagadora do associado, sem formatação                      |
| cadastros               | body   | Lista<Cadastro>   | Não nulo                                  | Lista de cadastros dos associados para a abertura de contas              |
| configuracao            | body   | Configuracao      | Não nulo                                  | Configuração para integração com webhook                                 |


**Parâmetro Interno de Entrada - Cadastro**

| Atributo       | Local | Tipo              | Limitações                                  | Descrição                                                     |
|----------------|-------|-------------------|---------------------------------------------|---------------------------------------------------------------|
| cpf            | body  | texto             | Não nulo, deve ser um CPF válido (11 dígitos) | CPF do assalariado, sem formatação                            |
| nome           | body  | texto             | Opcional                                    | Nome completo do assalariado                                  |
| dataNascimento | body  | texto             | Opcional, data formatada dd/MM/yyyy         | Data de nascimento do assalariado                            |
| flgSexo        | body  | texto             | Opcional, comprimento fixo de 1 dígito (F/M) | Sexo do assalariado                                           |
| email          | body  | texto             | Opcional, email válido                      | Email do assalariado                                          |
| telefone       | body  | texto             | Opcional, telefone com DDD, 10 ou 11 dígitos | Telefone do assalariado                                       |
| documento      | body  | Documento         | Opcional                                    | Detalhes do documento de identificação                        |
| endereco       | body  | Endereco          | Opcional                                    | Detalhes do endereço                                          |
| portabilidade  | body  | Portabilidade     | Opcional em caso de portabilidade           | Dados de conta para portabilidade, se aplicável               |
| representante  | body  | Representante     | Opcional em caso de maior de idade          | Dados do representante legal, se aplicável                   |


**Parâmetro Interno de Entrada de Cadastro - Documento**

| Atributo            | Local | Tipo  | Limitações                              | Descrição                          |
|---------------------|-------|-------|-----------------------------------------|------------------------------------|
| numDocumento        | body  | texto | Não nulo                                | Número do documento (ex.: RG)     |
| dataEmissaoDoc      | body  | texto | Não nulo, data formatada dd/MM/yyyy     | Data de emissão do documento      |
| nomeOrgaoEmissorDoc | body  | texto | Não nulo                                | Nome do órgão emissor             |
| sglUfEmissorDoc     | body  | texto | Não nulo, comprimento fixo 2 caracteres | Sigla do estado do órgão emissor  |


**Parâmetro Interno de Entrada de Cadastro - Endereço**

| Atributo        | Local | Tipo  | Limitações                                   | Descrição                         |
|-----------------|-------|-------|----------------------------------------------|-----------------------------------|
| tipoLogradouro  | body  | texto | Não nulo                                    | Tipo do logradouro                |
| nomeLogradouro  | body  | texto | Não nulo, máximo 100 caracteres             | Nome do logradouro                |
| numEndereco     | body  | texto | Não nulo                                    | Número do endereço                |
| txtComplemento  | body  | texto | Opcional, máximo 100 caracteres             | Complemento do endereço           |
| nomeBairro      | body  | texto | Não nulo, máximo 50 caracteres              | Bairro do endereço                |
| numCep          | body  | texto | Não nulo, cep válido e sem formatação       | CEP do endereço                   |
| nomeCidade      | body  | texto | Não nulo, nome válido, máximo 30 caracteres | Cidade do endereço                |
| sglUf           | body  | texto | Não nulo, comprimento fixo 2 caracteres     | Sigla do estado (UF)              |


**Parâmetro Interno de Entrada de Cadastro - Portabilidade**

| Atributo        | Local | Tipo  | Limitações                               | Descrição                             |
|-----------------|-------|-------|------------------------------------------|---------------------------------------|
| codBancoDestino | body  | texto | Não nulo, comprimento fixo 3 dígitos    | Código banco destino                  |
| numAgDestino    | body  | texto | Não nulo, comprimento máximo 10 dígitos | Código agência destino                |
| numContaDestino | body  | texto | Não nulo, sem formatação, comprimento máximo 20 dígitos | Número conta destino com dígito      |
| tpoConta        | body  | texto | Não nulo, comprimento fixo 2 dígito     | Tipo conta destino conforme preestabelecido, completar com zeros a esquerda |


**Parâmetro Interno de Entrada de Cadastro - Representante**

| Atributo | Local | Tipo  | Limitações                                  | Descrição                    |
|----------|-------|-------|---------------------------------------------|------------------------------|
| cpf      | body  | texto | Não nulo, deve ser um CPF válido (11 dígitos) | CPF do assalariado, sem formatação |
| nome     | body  | texto | Opcional                                    | Nome completo do assalariado |


**Parâmetro Interno de Entrada - Configuração**

| Atributo    | Local | Tipo  | Limitações                               | Descrição                                      |
|-------------|-------|-------|------------------------------------------|-----------------------------------------------|
| urlWebhook  | body  | texto | Não nulo, url válida, máximo 150 caracteres | URL do webhook para notificações             |
| portaHttp   | body  | texto | Opcional                                 | Porta HTTP para comunicação, pode ser passada na URL |


**Críticas ao Criar Solicitação**

| Código | Tipo        | Crítica                                                       | Descrição                                                                 |
|--------|-------------|---------------------------------------------------------------|---------------------------------------------------------------------------|
| RFB001 | Bloqueante  | CPF em situação irregular na base Receita Federal.            | Situação do CPF Cancelado ou Óbito                                       |
| RFB002 | Bloqueante  | Cadastro de menor de idade informado sem representante legal.  | CPF Menor, necessário inclusão de representante legal                    |
| RFB003 | Bloqueante  | Representante legal informado é menor de idade.               | Representante legal informado é uma pessoa em menoridade                 |
| RFB004 | Informativo | Nome informado no cadastro diferente do nome na Receita Federal. | Nome informado no cadastro é divergente                                 |
| RFB005 | Informativo | Data de nascimento informada no cadastro diferente na Receita Federal. | Data de nascimento informada é divergente                               |
| RFB006 | Informativo | Sexo do cliente informado no cadastro diferente na Receita Federal. | Sexo informado é divergente                                              |
| RFB007 | Bloqueante  | Receita Federal indisponível.                                 | Serviço da Receita Federal indisponível. Efetuar nova solicitação        |
| CCS001 | Bloqueante  | Erro no cadastro de conta salário.                            | Revisar os dados                                                         |
| CCS005 | Bloqueante  | O Banco ou a Agência de destino não existem.                  | Revisar os dados de Portabilidade                                        |
| CCS006 | Bloqueante  | Conta de destino inválida.                                    | Revisar os dados de Portabilidade                                        |
| CCS017 | Bloqueante  | Tipo de conta destino é obrigatório.                          | Revisar os dados de Portabilidade                                        |
| CCS018 | Informativo | Associado Digital - Cadastro não atualizado.                  | Cadastro possui relacionamento Sicredi Digital, Cadastro não atualizado  |


**Exemplo de Requisição - Request Completo - Modalidade Saque:**
```json
curl -X 'POST' \
'https://aberturadecontas-contasalario-efetivador.dev.sicredi.cloud/solicitacao' \
-H 'accept: */*' \
-H 'Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldU' \
-H 'TransactionId: 2024112143673247324160202872' \
-H 'Canal: EXTERNO' \
-H 'Authorization-Callback: 21152dPsXvSy151sft' \
-H 'Content-Type: application/json' \
-d '{
  "numCooperativa":"0101",
  "numAgencia":"01",
  "codConvenioFontePagadora":"XPTO",
  "cnpjFontePagadora":11111111111111,
  "cadastros":[
    {
      "cpf":"11111111111",
      "nome":"ASSALARIADO SICREDI",
      "dataNascimento":"01/01/1978",
      "flgSexo":"M",
      "email":"assalariado@sicred.com.br",
      "telefone":"51999999999",
      "documento":{
        "numDocumento":"9999999999",
        "dataEmissaoDoc":"04/10/2010",
        "nomeOrgaoEmissorDoc":"SSP",
        "sglUfEmissorDoc":"RS"
      },
      "endereco":{
        "tipoLogradouro":"RUA",
        "nomeLogradouro":"RUA BARAO DO AMAZONAS",
        "numEndereco":"11111",
        "txtComplemento":"Complemento 123",
        "nomeBairro":"PETROPOLIS",
        "numCep":"90670001",
        "nomeCidade":"PORTO ALEGRE",
        "sglUf":"RS"
      }
    }
  ],
  "configuracao":{
    "urlWebhook":"https://localhost/webhook",
    "portaHttp":"443"
  }
}'
```
**Exemplo de Requisição - Response:**
```json
{
  "idTransacao":"2024112143673247324160202872",
  "canal":"EXTERNO",
  "numCooperativa":"0101",
  "numAgencia":"01",
  "codConvenioFontePagadora":"XPTO",
  "cnpjFontePagadora":"11111111111111",
  "status":"PENDENTE",
  "resultado":"RECEBIDO",
  "critica":false,
  "dataCriacao":"2024-11-21T15:20:50.337129811",
  "cadastros":[
    {
      "cpf":"11111111111",
      "nome":"ASSALARIADO SICREDI",
      "situacao":"EM_PROCESSAMENTO"
    }
  ]
}
```
**Retornos:**

| Status Code | Descrição                                                                                  | Retorno                                                                                               |
|-------------|--------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------|
| 202         | Solicitação recebida                                                                       | Retorna no body o objeto de solicitação recebido.                                                    |
| 400         | Parâmetros de entrada incorretos.                                                          | Retorna no body um objeto contendo uma lista com os atributos inválidos e uma breve descrição.        |
| 401         | Token inválido ou expirado                                                                 | Retorna no body a mensagem informando que o token é inválido ou está expirado.                       |
| 403         | Ocorre quando o token utilizado na requisição não possui o escopo apropriado               | Retorna no body a mensagem informando que não possui acesso ao recurso.                              |
| 409         | Ocorre quando existe conflito com o identificador de transação única e corpo de solicitação preexistente | Não retorna body.                                                                                    |
| 422         | Ocorre quando alguma regra de negócio impede que a solicitação seja recebida               | Retorna no body a mensagem informando qual regra barrou o recebimento da solicitação.                |
| 500         | Ocorre quando existe uma falha no funcionamento da aplicação.                              | Retorna no body um objeto informando que ocorreu um erro inesperado.                                 |

</details>

<details>
<summary>2. Criar solicitação de conta salário - Portabilidade</summary>

**Descrição:** Cria uma solicitação de conta salário com opção de portabilidade de saldo.

- **[POST] /solicitacao**
- Descrição: Realiza uma solicitação de cadastro de conta salário para a empresa convênio informada, com o pedido de Portabilidade de saldo.
- Escopo: abertura.contasalario.solicitar

**Parâmetro Interno de Entrada de Cadastro - Portabilidade**

| Atributo         | Local | Tipo  | Limitações                              | Descrição                                    |
|------------------|-------|-------|-----------------------------------------|---------------------------------------------|
| codBancoDestino  | body  | texto | Não nulo, comprimento fixo 3 dígitos    | Código banco destino                        |
| numAgDestino     | body  | texto | Não nulo, comprimento máximo 10 dígitos | Código agência destino                      |
| numContaDestino  | body  | texto | Não nulo, sem formatação, comprimento máximo 20 dígitos | Número conta destino com dígito           |
| tpoConta         | body  | texto | Não nulo, comprimento fixo 2 dígitos    | Tipo conta destino conforme preestabelecido |

**Parâmetro interno de entrada de Portabilidade - tpoConta:** : (Código referente ao tipo da conta do
titular no destino do valor creditado. A conta destino deve pertencer ao titular da Conta Salário)

| tpoConta | Descrição                                                                                   |
|----------|---------------------------------------------------------------------------------------------|
| 01       | Conta Corrente Individual independente de Instituição Financeira                           |
| 02       | Conta Poupança Individual independente de Instituição Financeira                           |
| 03       | Apenas Conta Plataforma Digital/Woop (Banco Sicredi) ou Conta Pagamento de outra Instituição Financeira. Se outra IF, a conta deve ser do tipo Conta Pagamento. |
| 11       | Conta Corrente Conjunta independente de Instituição Financeira                             |
| 12       | Conta Poupança Conjunta independente de Instituição Financeira                             |


**Exemplo de Requisição - Request Dados Mínimos - Modalidade Portabilidade:**
```json
curl -X 'POST' \
'https://aberturadecontas-contasalario-efetivador.dev.sicredi.cloud/solicitacao' \
-H 'accept: */*' \
-H 'Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldU' \
-H 'TransactionId: 2024112143673247324160202872' \
-H 'Canal: EXTERNO' \
-H 'Authorization-Callback: 21152dPsXvSy151sft' \
-H 'Content-Type: application/json' \
-d '{
  "numCooperativa":"0101",
  "numAgencia":"01",
  "codConvenioFontePagadora":"XPTO",
  "cnpjFontePagadora":11111111111111,
  "cadastros":[
    {
      "cpf":"11111111111",
      "email":"assalariado@sicred.com.br",
      "telefone":"51999999999",
      "portabilidade":{
        "codBancoDestino":"748",
        "numAgDestino":"0101",
        "numContaDestino":"123456",
        "tipoConta":"01"
      }
    }
  ],
  "configuracao":{
    "urlWebhook":"https://localhost/webhook",
    "portaHttp":"443"
  }
}'
```
**Exemplo de Requisição - Response:**
```json
{
  "idTransacao":"2024112143673247324160202872",
  "canal":"EXTERNO",
  "numCooperativa":"0101",
  "numAgencia":"01",
  "codConvenioFontePagadora":"XPTO",
  "cnpjFontePagadora":"11111111111111",
  "status":"PENDENTE",
  "resultado":"RECEBIDO",
  "critica":false,
  "dataCriacao":"2024-11-21T15:20:50.337129811",
  "cadastros":[
    {
      "cpf":"11111111111",
      "nome":"ASSALARIADO SICREDI",
      "situacao":"EM_PROCESSAMENTO"
    }
  ]
}
```

**Retornos:**

| Status Code | Descrição                                                                               | Retorno                                                                                          |
|-------------|-----------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------|
| 202         | Solicitação recebida                                                                    | Retorna no body o objeto de solicitação recebido.                                               |
| 400         | Parâmetros de entrada incorretos.                                                       | Retorna no body um objeto contendo uma lista com os atributos inválidos e uma breve descrição.  |
| 401         | Token inválido ou expirado                                                              | Retorna no body a mensagem informando que o token é inválido ou está expirado.                  |
| 403         | Ocorre quando o token utilizado na requisição não possui o escopo apropriado            | Retorna no body a mensagem informando que não possui acesso ao recurso.                        |
| 409         | Ocorre quando existe conflito com o identificador de transação única e corpo de solicitação preexistente | Não retorna body.                                                                               |
| 422         | Ocorre quando alguma regra de negócio impede que a solicitação seja recebida            | Retorna no body a mensagem informando qual regra barrou o recebimento da solicitação.          |
| 500         | Ocorre quando existe uma falha no funcionamento da aplicação                            | Retorna no body um objeto informando que ocorreu um erro inesperado.                           |
</details>

<details>
<summary>3. Criar solicitação de conta salário - Representante</summary>

**Descrição:** Endpoint para criar uma solicitação de conta salário com inclusão de representante legal para menores de idade.

- **[POST] /solicitacao**
- Descrição: Realiza uma solicitação de cadastro de conta salário para a empresa convênio para menor
  com representante
- Escopo: abertura.contasalario.solicitar


| Atributo | Local | Tipo  | Limitações                                  | Descrição                    |
|----------|-------|-------|---------------------------------------------|------------------------------|
| cpf      | body  | texto | Não nulo, deve ser um CPF válido (11 dígitos) | CPF do assalariado, sem formatação |
| nome     | body  | texto | Opcional                                    | Nome completo do assalariado |

**Exemplo de Requisição - Request Dados Mínimos - Modalidade Representante:**
```json
curl -X 'POST' \
'https://aberturadecontas-contasalario-efetivador.dev.sicredi.cloud/solicitacao' \
-H 'accept: */*' \
-H 'Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldU' \
-H 'TransactionId: 2024112143673247324160202872' \
-H 'Canal: EXTERNO' \
-H 'Authorization-Callback: 21152dPsXvSy151sft' \
-H 'Content-Type: application/json' \
-d '{
  "numCooperativa":"0101",
  "numAgencia":"01",
  "codConvenioFontePagadora":"XPTO",
  "cnpjFontePagadora":11111111111111,
  "cadastros":[
    {
      "cpf":"11111111111",
      "email":"assalariado@sicred.com.br",
      "telefone":"51999999999",
      "representante":{
        "cpf":" 83648420054"
      }
    }
  ],
  "configuracao":{
    "urlWebhook":"https://localhost/webhook",
    "portaHttp":"443"
  }
}'
```
**Exemplo de Requisição - Response:**
```json
{
  "idTransacao":"2024112143673247324160202872",
  "canal":"EXTERNO",
  "numCooperativa":"0101",
  "numAgencia":"01",
  "codConvenioFontePagadora":"XPTO",
  "cnpjFontePagadora":"11111111111111",
  "status":"PENDENTE",
  "resultado":"RECEBIDO",
  "critica":false,
  "dataCriacao":"2024-11-21T15:20:50.337129811",
  "cadastros":[
    {
      "cpf":"11111111111",
      "nome":"ASSALARIADO SICREDI",
      "situacao":"EM_PROCESSAMENTO"
    }
  ]
}
```

**Retornos:**

| Status Code | Descrição                                                                               | Retorno                                                                                          |
|-------------|-----------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------|
| 202         | Solicitação recebida                                                                    | Retorna no body o objeto de solicitação recebido.                                               |
| 400         | Parâmetros de entrada incorretos.                                                       | Retorna no body um objeto contendo uma lista com os atributos inválidos e uma breve descrição.  |
| 401         | Token inválido ou expirado                                                              | Retorna no body a mensagem informando que o token é inválido ou está expirado.                  |
| 403         | Ocorre quando o token utilizado na requisição não possui o escopo apropriado            | Retorna no body a mensagem informando que não possui acesso ao recurso.                        |
| 409         | Ocorre quando existe conflito com o identificador de transação única e corpo de solicitação preexistente | Não retorna body.                                                                               |
| 422         | Ocorre quando alguma regra de negócio impede que a solicitação seja recebida            | Retorna no body a mensagem informando qual regra barrou o recebimento da solicitação.          |
| 500         | Ocorre quando existe uma falha no funcionamento da aplicação                            | Retorna no body um objeto informando que ocorreu um erro inesperado.                           |

</details>

<details>
<summary>4. Consultar solicitação</summary>

**Descrição:** Consulta uma solicitação específica usando o `TransactionId` como referência.

- **[GET] /solicitação/{TransactionId}**
- Descrição: Realiza consulta de solicitação por id Transacao
- Escopo: abertura.contasalario.consultar

| Atributo       | Local  | Tipo  | Limitações                | Descrição                               |
|----------------|--------|-------|---------------------------|-----------------------------------------|
| Authorization  | header | texto | Deve ser um token válido  | Bearer token de autenticação           |
| Canal          | header | texto | Não em branco             | Canal de origem da solicitação         |
| TransactionId  | body   | texto | Não em branco             | Identificador único de solicitação     |

**Exemplo de Requisição - Request**
```
curl -X 'GET' \
'https://aberturadecontas-contasalario-efetivador.dev.sicredi.cloud/solicitacao/2024112143673247324160202872' \
-H 'accept: */*' \
-H 'Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIi' 
-H 'Canal: EXTERNO'
```

**Exemplo de Requisição - Response:**
```json
{
  "idTransacao":"2024112143671054855866876664",
  "canal":"EXTERNO",
  "numCooperativa":"0167",
  "numAgencia":"17",
  "codConvenioFontePagadora":"XPTO",
  "cnpjFontePagadora":"11111111111111",
  "status":"FINALIZADO",
  "resultado":"CONCLUIDO",
  "critica":true,
  "dataCriacao":"2024-11-21T09:53:11.946021",
  "dataAtualizacao":"2024-11-21T09:53:22.657147",
  "cadastros":[
    {
      "cpf":"11111111111",
      "nome":"ASSOCIADO CONTA SALARIO 11111111111",
      "conta":"254554",
      "situacao":"CONCLUIDO",
      "criticas":[
        {
          "codigo":"RFB005",
          "descricao":"Data de nascimento informada no cadastro diferente da data de nascimento na Receita Federal.",
          "tipo":"INFORMATIVO"
        },
        {
          "codigo":"RFB004",
          "descricao":"Nome informado no cadastro diferente do nome na Receita Federal.",
          "tipo":"INFORMATIVO"
        }
      ]
    }
  ]
}
```
**Retornos:**

| Status Code | Descrição                                                                    | Retorno                                                                                          |
|-------------|------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------|
| 200         | OK                                                                           | Retorna no body um objeto JSON.                                                                 |
| 401         | Token inválido ou expirado                                                  | Retorna no body a mensagem informando que o token é inválido ou está expirado.                  |
| 403         | Ocorre quando o token utilizado na requisição não possui o escopo apropriado | Retorna no body a mensagem informando que não possui acesso ao recurso.                        |
| 500         | Ocorre quando existe uma falha no funcionamento da aplicação                | Retorna no body um objeto informando que ocorreu um erro inesperado.                           |

</details>

<details>
<summary>5. Buscar instituições financeiras</summary>

**Descrição:** Retorna a lista de instituições financeiras autorizadas para portabilidade.

- **[GET] /instituicoes-financeiras**
- Descrição: Realiza consulta de instituições financeiras autorizadas
- Escopo: abertura.contasalario.consultar

| Atributo      | Local  | Tipo  | Limitações                | Descrição                   |
|---------------|--------|-------|---------------------------|-----------------------------|
| Authorization | header | texto | Deve ser um token válido  | Bearer token de autenticação |
| Canal         | header | texto | Não em branco             | Canal de origem da solicitação |

**Exemplo de Requisição - Request**
```
curl --location 'https://aberturadecontas-contasalario-efetivador.dev.sicredi.cloud/instituicoes-financeiras' \
--H 'Canal: EXTERNO
```

**Exemplo de Requisição - Response:**
```json
[
  {
    "codigo":"001",
    "nomeBanco":"BCO XPTO"
  },
  {
    "codigo":"002",
    "nomeBanco":"BCO XPTO 2."
  },
  {
    "codigo":"748",
    "nomeBanco":"BCO COOPERATIVO SICREDI S A"
  }
]
```
**Retornos:**

| Status Code | Descrição                                                                    | Retorno                                                                                          |
|-------------|------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------|
| 200         | OK                                                                           | Retorna no body a lista de instituições financeiras.                                            |
| 401         | Token inválido ou expirado                                                  | Retorna no body a mensagem informando que o token é inválido ou está expirado.                  |
| 403         | Ocorre quando o token utilizado na requisição não possui o escopo apropriado | Retorna no body a mensagem informando que não possui acesso ao recurso.                        |

</details>

<details>
<summary>6. Webhook</summary>

**Descrição:** Configuração que envia uma resposta via Webhook para notificar sobre o status de processamento da solicitação.


| Tipo      | Nome                 | Descrição                                | Obrigatório |
|-----------|----------------------|------------------------------------------|-------------|
| Body      | urlWebhook           | URL para envio de notificações          | Sim         |
| Body      | portaHttp            | Porta HTTP para comunicação             | Não         |


Com webhook é possível inscrever uma URL da sua aplicação que esteja disponível para a
internet, para receber as notificações referentes a finalização de solicitação da API Conta Salário. Para configurarmos o Webhook, precisamos que o associado passe a URL na corpo da solicitação
juntamente com o cabeçalho de resposta como API KEY para receber um POST.

**Retorno de Solicitação pelo Webhook**
- **[POST] {urlWebhook}**
- Descrição: Realiza consulta de instituições financeiras autorizadas

| Tipo      | Nome                 | Descrição                                | Obrigatório |
|-----------|----------------------|------------------------------------------|-------------|
| Header    | Authorization-Callback | Chave para autenticação do Webhook   | Sim         |
| Body      | resultado            | Resultado do processamento da solicitação | Sim         |

**Exemplo de Requisição - Response:**
```json
{
  "idTransacao":"2024112143671054855866876664",
  "status":"FINALIZADO",
  "resultado":"CONCLUIDO"
}
```

</details>


## ✉️ Mensageria

### **Tópico:** `aberturadecontas-contasalario-efetivador-cadastros-resultado-v1`
- Tópico kafka que recebe os resultados de evento de cadastro de conta salário com sucesso na abertura de conta e tambem do resultado completo de solicitação quando processamento finalizado.


## 🚀 Rodando a aplicação

### Com a infraestrutura local
Execute a aplicação com os seguintes profiles:
```bash
-Dspring.profiles.active=local -Dfile.encoding=UTF-8
```

## 📁 Documentação Complementar
* [Guia Técnico Integrações API Conta Salário_v2.0](https://teams.sicredi.io/secure/attachment/4343377/Guia%20T%C3%A9cnico%20Integra%C3%A7%C3%B5es%20API%20Conta%20Sal%C3%A1rio%20Sicredi_V2.0.pdf)

#### Arquitetura
![Desenho Técnico Arquitetura](/images/DesenhoTecnico.png "Desenho Técnico Arquitetura.")


#### Diagrama de fluxo (Status processamento e resultado de solicitação)
![Status processamento e resultado de solicitação](/images/Fluxograma.png "Status processamento e resultado de solucutação.")



## 🤔 Dúvidas ou contribuições

Entre em contato com o [Time de Abertura de Contas Salário](mailto:time_esteira_relacionamento@confederacaosicredi.onmicrosoft.com).

## 📖 Decision Logs
| Data       | Autor          | Motivo               |
|:-----------|:---------------|:---------------------|
| 13/01/2025 | @marcos_scholl | Criação do documento |
