# JavaFaceDetect

Módulo Java de cadastramento e reconhecimento facial, exposto como uma API REST (Spring Boot).
Todo o processamento de visão computacional roda localmente com OpenCV/JavaCV (Haar Cascade para
detecção + LBPH para reconhecimento) e os dados são persistidos em um banco H2 embarcado — sem
nenhuma dependência de serviços de nuvem em tempo de execução.

## Pré-requisitos

- Java 21+
- Maven 3.8+
- Conexão com a internet apenas para o build (download de dependências do Maven Central) — em
  tempo de execução a aplicação não depende de rede/nuvem.

## Build e execução

```bash
# compilar e rodar os testes
mvn clean verify

# executar a aplicação (porta 8080 por padrão)
mvn spring-boot:run

# ou gerar o jar executável e rodá-lo
mvn clean package
java -jar target/javafacedetect-0.1.0-SNAPSHOT.jar
```

O schema do banco H2 (arquivo local em `./data/javafacedetect`) é criado automaticamente na
inicialização.

### Configuração

Principais propriedades em `src/main/resources/application.yml`:

| Propriedade                                  | Descrição                                                         | Padrão   |
|-----------------------------------------------|--------------------------------------------------------------------|----------|
| `face-recognition.confidence-threshold`       | Distância LBPH máxima aceita (menor = mais parecido) para reconhecer uma pessoa | `80.0`   |
| `face-recognition.max-image-size-bytes`       | Tamanho máximo aceito por imagem enviada                          | `5242880` (5MB) |
| `spring.datasource.url`                       | URL do banco H2                                                    | arquivo local |

## Exemplos de uso da API

### Cadastrar uma pessoa

```bash
curl -X POST http://localhost:8080/api/pessoas \
  -F "nome=Maria Silva" \
  -F "imagens=@/caminho/para/foto1.jpg" \
  -F "imagens=@/caminho/para/foto2.jpg"
```

Resposta (`201 Created`):

```json
{ "id": 1, "nome": "Maria Silva", "criadoEm": "2026-09-04T18:00:00Z" }
```

### Consultar e remover uma pessoa

```bash
curl http://localhost:8080/api/pessoas/1
curl -X DELETE http://localhost:8080/api/pessoas/1
```

### Reconhecer um rosto

```bash
curl -X POST http://localhost:8080/api/reconhecimento \
  -F "imagem=@/caminho/para/foto-desconhecida.jpg"
```

Resposta quando reconhecido:

```json
{ "reconhecido": true, "pessoaId": 1, "nome": "Maria Silva", "distancia": 42.1 }
```

Resposta quando não reconhecido (ou base de cadastros vazia):

```json
{ "reconhecido": false, "pessoaId": null, "nome": null, "distancia": null }
```

### Erros

Toda imagem enviada (cadastro ou reconhecimento) deve conter **exatamente um rosto detectável**,
estar em um formato suportado (JPEG/PNG) e respeitar o tamanho máximo configurado. Erros são
retornados com um corpo padronizado:

```json
{ "codigo": "ROSTO_NAO_DETECTADO", "mensagem": "Nenhum rosto foi detectado na imagem enviada" }
```

| Código                        | HTTP | Situação                                             |
|--------------------------------|------|-------------------------------------------------------|
| `ROSTO_NAO_DETECTADO`          | 422  | Nenhum rosto encontrado na imagem                     |
| `MULTIPLOS_ROSTOS_DETECTADOS`  | 422  | Mais de um rosto encontrado na imagem                 |
| `FORMATO_IMAGEM_INVALIDO`      | 400  | Arquivo não é uma imagem em formato suportado         |
| `ARQUIVO_MUITO_GRANDE`         | 413  | Arquivo excede o tamanho máximo permitido             |
| `PESSOA_NAO_ENCONTRADA`        | 404  | Id de pessoa inexistente em consulta/remoção          |

## Limitações conhecidas

- Sem autenticação/autorização — não exponha esta API diretamente à internet sem adicionar essa
  camada antes.
- Reconhecimento local (LBPH) é adequado para bases pequenas/médias de pessoas; não há suporte a
  vídeo/stream em tempo real, apenas imagens estáticas enviadas via upload.
