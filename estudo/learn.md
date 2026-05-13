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
- Posso passar um especificidades da coluna com o **@Column(name, nullable, unique, length...)**
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
>   <br>

![hash](hash.png)

### Validação de criação de tarefas

- Toda classe que o Spring gerencie deve ter a anotação **@Component**
- Toda requisição, antes de chegar na rota requisitada, passa pelo Filter.
- A classe `FilterTaskAuth` extende a superclasse `OncePerRequestFilter`. Para receber a implementação automatica, basta clicar em `FilterTaskAuth` e pressionar **CTRL + .** para vir a "template".

```java
@Component
public class FilterTaskAuth extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    filterChain.doFilter(request, response); // Após o filtro, continua a execução da requisição
  }
}
```

- Dentro do Postman, foi adicionado um **Basic Auth** para geração de um **token básico**, e dentro da classe `doFilterInternal`, foi possível obter este através da `substring`:

```java
    // Pegar a requisição
    var auth = request.getHeader("Authorization");

    // Basic é o tipo de autenticação, o que vem depois é o token, então a gente remove a palavra Basic e os espaços
    var user_data_encoded = auth.substring("Basic".length()).trim();
```

- Agora fazemos a decodificação deste **Base64**:

```java
    // Decodifica e retorna um array de bytes
    byte[] user_data_decoded = Base64.getDecoder().decode(user_data_encoded);

    // Trasnforma esse array de bytes em uma String
    String user_data_String = new String(user_data_decoded);

    // Divide a string em duas (forma um array), com o ':' como divisor
    String[] user_data = user_data_String.split(":");

    // Então é só buscar os dados no array.
    System.out.println("User: " + user_data[0]);
    System.out.println("Password: " + user_data[1]);
```

### Verificar usuário e senha no banco de dados:

- Utilizamos a mesma classe `IUserRepository` para usar o método `findByEmail`, passando o email enviado pelo header do usuário.
- Se ele não existir, envia erro 401.
- Se existir, verificar a senha através da verificação do Bcrypt.
- Se não passar da validação, envia erro 401.
  Se passar, segue normalmente a passagem pelo site.

- Devemos envolver todo o código em `servletPath.equals("/tasks")` para que as verificações somente ocorram no momento em que a rota é `/tasks`

```java
var servletPath = request.getServletPath();
    if (servletPath.equals("/tasks")) {

      // Pegar a header Authorization da requisição e verificar se ela existe e se
      // começa com "Basic "
      var auth = request.getHeader("Authorization");
      if (auth == null || !auth.startsWith("Basic ")) {
        response.sendError(401, "Authorization header inválida");
        return;
      }

      // Pegar os dados do usuário e senha da header Authorization
      var user_data_encoded = auth.substring("Basic".length()).trim();

      // Decodificar os dados do usuário e senha
      byte[] user_data_decoded = Base64.getDecoder().decode(user_data_encoded);

      // Converter os dados do usuário e senha para String
      String user_data_String = new String(user_data_decoded);

      // Separar os dados do usuário e senha pelo caractere ":"
      String[] user_data = user_data_String.split(":");

      System.out.println("Email: " + user_data[0]);
      System.out.println("Password: " + user_data[1]);

      // Verificar se o usuário existe no banco de dados
      var user = this.userRepository.findByEmail(user_data[0]);

      if (user == null) {
        response.sendError(401, "Usuário sem autorização");
      } else {
        // Se o usuário existir, verificar se a senha é válida
        var password_verify = BCrypt.verifyer().verify(user_data[1].toCharArray(), user.getPassword());

        if (!password_verify.verified) {
          response.sendError(401, "Senha inválida");
        } else {
          filterChain.doFilter(request, response);
        }
      }
    } else {
      // Se a rota não for /tasks, continuar com a requisição normalmente
      filterChain.doFilter(request, response);
    }
```

### Adicionar ID do usuário no header

- Setamos como atributo do header o id do usuário para que nas próximas criações de tarefa o ID dele venha diretamente do header, não manualmente escrito por ele.

```java
if (!password_verify.verified) {
  response.sendError(401, "Senha inválida");
} else {
  // Se a senha for válida, adicionar o id do usuário na requisição e continuar com a requisição normalmente
  request.setAttribute("user_id", user.getId());
  filterChain.doFilter(request, response);
}
```

- Agora dentro de `TaskController`:

```java
@PostMapping("")
  public TaskModel createTask(@RequestBody TaskModel task, HttpServletRequest request) {
    // Pegar o user_id do request e setar no task (coluna user_id)
    task.setUserId((UUID) request.getAttribute("user_id"));
    var savedTask = this.taskRepository.save(task);
    return savedTask;
  }
```

### Validação das horas

- Obtemos a data atual com `LocalDateTime.now()` e a partir dela temos os métodos `isAfter()` e `isBefore()`.
- Então fica simples a validação:

```java
// Verificar se a data de início e término estão no futuro e se a data de início
// é antes da data de término
var currentDate = LocalDateTime.now();
if (currentDate.isAfter(task.getStartAt()) || currentDate.isAfter(task.getEndAt())) {
  return ResponseEntity.status(400).body("A data de início e término devem ser no futuro");
} else if (task.getStartAt().isAfter(task.getEndAt())) {
  return ResponseEntity.status(400).body("A data de início deve ser antes da data de término");
}
```

- Adicionei também um calculo para a duração disponível para executar a tarefa com `Period`:

```java
// Formatação das datas
var startDate = task.getStartAt().toLocalDate();
var endDate = task.getEndAt().toLocalDate();

// Periodo entre elas
var period = Period.between(startDate, endDate);
// Atributo recebe string formatada
task.setTimeSpan(period.getYears() + " anos, " + period.getMonths() + " meses e " + period.getDays() + " dias");
```

### Método **GET** para listar tarefas de um usuário:

- Retornamos uma `List<TaskModel>`

```java
@GetMapping("")
public List<TaskModel> listTasks(HttpServletRequest request) {
  // Pegamos o ID do header
  var user_id = request.getAttribute("user_id");
  // Procuramos baseado só no userId
  var tasksFound = this.taskRepository.findByUserId((UUID) user_id);
  return tasksFound;
}
```

- O mais interessante é que o método `findByUserId` foi implementado automaticamente pelo Spring! :D

### Fazer Update na task.

- Uma alteração importante foi no `Filter`, para que aceitasse qualquer endpoin que começasse com `/tasks`:

```java
if (servletPath.startsWith("/tasks")) {
  // ...
}
```

- Para atualizar, recebemos via parâmetro (Padrão REST) o id com `@PathVariable UUID id`
  - Verificamos que o usuário é dono da task
  - Caso for, utilizamos um algoritmo com operador ternário para somente atualizar aquilo que o usuário enviou via Body, senão enviou, mantemos como era na task antiga.

```java
@PutMapping("/{id}")
public ResponseEntity updateTask(@RequestBody TaskModel updateTaskModel, HttpServletRequest request,
    @PathVariable UUID id) {

  // Verificar se usuário é dono daquela task
  var user_id = request.getAttribute("user_id");
  var existingTask = this.taskRepository.findById(id).orElse(null);
  if (existingTask == null) {
    return ResponseEntity.status(404).body("Task não encontrada");
  } else if (!existingTask.getUserId().equals(user_id)) {
    return ResponseEntity.status(403).body("Acesso negado");
  } else {

    // Se o usuário for dono da task, atualizar os campos da task com os dados
    // enviados pelo body, caso eles existam, se não, manter os dados atuais da
    // task.
    existingTask
        .setTitle((updateTaskModel.getTitle() != null) ? updateTaskModel.getTitle() : existingTask.getTitle());

    existingTask.setDescription((updateTaskModel.getDescription() != null) ? updateTaskModel.getDescription()
        : existingTask.getDescription());

    existingTask.setPriority(
        (updateTaskModel.getPriority() != null) ? updateTaskModel.getPriority() : existingTask.getPriority());

    existingTask.setStartAt(
        (updateTaskModel.getStartAt() != null) ? updateTaskModel.getStartAt() : existingTask.getStartAt());

    existingTask
        .setEndAt((updateTaskModel.getEndAt() != null) ? updateTaskModel.getEndAt() : existingTask.getEndAt());

    this.taskRepository.save(existingTask);
    return ResponseEntity.status(200).body(existingTask);
  }
}
```
