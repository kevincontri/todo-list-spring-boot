# Anotações Spring Boot

## Maven

- Gerencias as dependências da aplicação.
- Buscamos na [MVN Repository](https://mvnrepository.com/) todas as libs e dependências para intalação.

## Por que usar Spring Boot?

- Abstrai muitos recursos e dependências para criar aplicações web.

### Estruturação das pastas em Maven

- Dentro de src/resources/application.properties colocamos **variaveis de ambiente**.
- **@** são _Annotations_, como decorators. A **@SpringBootApplication** indica qual é a classe inicial do projeto.

```java
@SpringBootApplication
public class TodolistApplication {

	public static void main(String[] args) {
		SpringApplication.run(TodolistApplication.class, args);
	}

}
```

### Syntax - Decorators e seu uso

- Usamos **@RestController** como decorator (ou annotation) em cima de **classes controller**. Devemos sempre ter uma classe, e um método. O retorno do método, seu nome e os parametros.
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

- Para atributos privados, é necessário ter métodos de acesso getters e setters.
- Através deles, o próprio **@RequestBody** do Spring vai utilizar o método set para atribuir o valor aos atributos.

### Uso do Lombok

- Nos permite abstrai métodos construtores e de acesso.
- Adicionamos no projeto essa funcionalidade por copiar e adicionar no _pom.xml_
- Para adicionar apenas **getters** -> Basta adicionar **@Getter** no topo da classe.
- Para apenas **setters** -> Basta adicionar **@Setter** no topo.
- Para ambos em todos os atributos -> **@Data**
- Para atributos especificos, só adicionar @Getter ou @Setter em cima do **atributo** não da **classe**, e só aquele atributo recebe os métodos de acesso.

### Banco de Dados

- **Spring Data JPA** nos permite fazer a comunicação com o banco de dados, para inserção e manipulação com o banco.
- Utiliza o conceito de **ORM** (Transforma o SQL puro em classes na sintaxe Java)
- H2 Database é um banco em memória, super rápido. Utilizado apenas em desenvolvimento, não em produção.
- Devemos adicionar estas informações em _application.properties_:

```java
spring.datasource.url=jdbc:h2:mem:todolist
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=admin
spring.datasource.password=admin
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.h2.console.enabled=true
```

- E devemos abrir o _localhost:8080/h2-console_ para poder acessá-lo.

### Criação de tabelas

- Introduz o conceito de entidade (uma classe que representa uma tabela)
- Adicionamos **@Entity** acima da classe com um nome para identificar a tabela (esse nome é o que vai ser usado nas queries SQL internamente)

```java
@Data
@Entity(name = "tb_users")
public class UserModel {
  private String username;
  private String email;
  private String password;
}
```

- Cria-se a primary key com o **@Id** no tipo de dado UUID e também o informamos para gerar de forma automática esse comando UUID com o **@GeneratedValue**:

```java
  @Id
  @GeneratedValue(generator="UUID")
  private UUID id;
```

- Os atributos serão colunas no banco de dados.
- Posso passar um especificidades da coluna com o **@Column(name, nullable, unique ...)**
- Data de criação automatica com o **@CreationTimestamp**:

```java
  @CreationTimestamp
  private LocalDateTime createdAt;
```

### Repositorio

- Dentro de **<>** em _IUserRepository_ colocamos atributos dinâmicos, o **T**, ou a classe que o repositório está representando, e qual o tipo de **ID** que a entidade tem, que é o UUID.

- Dentro do Controller, instanciamos um **userRepository**, para poder usar todos os métodos do JpaRepository. Acima dele adicionamos **@Autowired** para que todo o ciclo de vida do repositório possa ser estabelecido corretamente.

- O tipo de dado **var** em Java é usado para a **inferência de tipos de variáveis locais**, o que significa que o compilador do Java determina automaticamente o tipo de dado baseado no **valor atribuído à direita**.

- Podemos salvar o body recebido:

```java
@RestController
@RequestMapping("/users")
public class UserController {

  @Autowired
  private IUserRepository userRepository;

  @PostMapping("")
  public UserModel createUser(@RequestBody UserModel user) {
    var userCreated = this.userRepository.save(user);
    return userCreated;
  }
}
```

- Após isto, simplesmente podemos criar novos usuários e visualizá-los no H2
  ![h2-working](h2-working.png)

### Validação de dados

- No **UserModel**, podemos adicionar além do nome da coluna, dados como **nullable** (se eles podem ser nulos) ou **unique** (que não podem repetir). Ao adicionar, por exemplo, o campo `email` como **unique**, se um novo POST enviar o mesmo campo que já temos no banco, vai lançar um erro 500!

- Podemos adicionar novos métodos ao JpaRepository, como por exemplo:

```java
public interface IUserRepository extends JpaRepository<UserModel, UUID> {
  UserModel findByEmail(String email);
  // Método para encontrar um usuário pelo email, UserModel é a entidade retornada e String é o tipo do email
}
```

> O JpaRepository tem capacidade de já entender exatamente o que este método vai fazer, e já o implementa (automaticamente)

- Podemos adicionar a validação no controller da seguinte forma:

```java
  @PostMapping("")
  public UserModel createUser(@RequestBody UserModel user) {
    // Verificar se o usuário já existe pelo email
    var existingUser = this.userRepository.findByEmail(user.getEmail());
    if (existingUser != null) {
      throw new RuntimeException("User already exists");
    }

    // Criar o usuário
    var userCreated = this.userRepository.save(user);
    return userCreated;
  }
```

- Agora se tentar enviar um JSON com o mesmo email:
  ![status500](status500.png)

### Request Entity - Retorno de status HTTP corretos

- O valor de retorno para nosso controller é um **`ResponseEntity`**, que retorna tanto em casos de sucesso como em casos de erro
- Se houver algum erro, retornamos com o status code e um body de retorno.

```java
if (existingUser != null) {
    return ResponseEntity.status(400).body("Usuário já existe");
    } // Status code e mensagem de erro

var userCreated = this.userRepository.save(user);
return ResponseEntity.status(200).body(userCreated);
// Status code e objeto usuário devolvido.
```

### Hash de Senha

- Podemos utilizar a dependência do **bcrypt** para realizar a criptografia da senha, para só após guardar no banco de dados.

```java
    var passwordHashed = BCrypt.withDefaults().hashToString(12, user.getPassword().toCharArray());
    user.setPassword(passwordHashed);
```

> `withDefaults()` permite usar as configurações padrão do Bcrypt
> `hashToString` é o método para a operação de transformar a senha em um hash em formato de String, que recebe alguns parâmetros:
>
> - `cost` (ou força de encriptação)
> - `string` (a string a passar pelo hash)
>   Lembrar de adicionar `toCharArray` que é o padrão que `hashToString()` recebe.
<br>

![hash](hash.png)

### Tabela de task