# Configuração do Ambiente

## Maven

- Gerencias as dependências da aplicação.
- Buscamos na [MVN Repository](https://mvnrepository.com/) todas as libs e dependências para intalação.

## Por que usar Spring Boot?

- Abstrai muitos recursos e dependências para criar aplicações web.

## Anotações

### Estruturação das pastas em Maven

- Dentro de src/resources/application.properties colocamos **variaveis de ambiente**.
- **@** são _Annotations_, como decorators. A **@SpringBootApplication** indica qual é a classe inicial do projeto.

### Syntax - Decorators e seu uso

- Usamos **@Controller** como decorator (ou annotation) em cima de **classes controller**. Devemos sempre ter uma classe, e um método. O retorno do método, seu nome e os parametros.
- Annotation **@RequestMapping("/rota")** acima da classe para que seja possível **criar uma estruturação REST organizada**. Semelhante ao _APIRouter_ do FastAPI, onde nas outras rotas é utilizado somente o resto da rota inicial.
- **@GetMapping("")** indica um método **GET** para o acesso do método da classe. (Basicamente aqui os **métodos de uma classe** são as **funções que cada rota executa.**)
- Para criar **schemas** (Como no _Pydantic_) é possível somente criar uma classe (Como a **UserModel.java**) com atributos.

### Estruturação de métodos

- Para adicionar isto nos parametros do método POST é usado a seguinte sintaxe:

```java
@PostMapping("")
  public String createUser(@RequestBody UserModel userModel)
```

- **@RequestBody** indica ao Spring que o que vamos receber como paramêtro segue o schema do UserModel (Lembrando que Java é tipado, então **userModel** deve ter do TIPO **UserModel**)

- Não é boa prática ter atributos públicos nos schemas em projetos reais.

### Métodos Getters e Setters para atributos privados dos schemas.
