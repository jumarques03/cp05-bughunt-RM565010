# Checkpoint 5 — Bug Hunt PetFiap

## Identificação

**Grupo:** Júlia Souza Marques (RM565010) — entrega individual, sem outros integrantes

| Integrante | RM | Turma |
|---|---|---|
| Júlia Souza Marques | 565010 | 2CCPW |

| Campo | |
|---|---|
| **Total de bugs corrigidos** | 12 / 12 (contrato) + 1 bug extra (bug13, fora da contagem oficial — ver Parte 1) |
| **Total de ajustes de Clean Code** | 6 / 6 |
| **Total de testes novos escritos** | 6 / 6 |
| **Suíte final (Run As → JUnit Test)** | 26 testes, 0 falhas |

---

## Parte 1 — Bugs encontrados

| # | Sintoma observado (o que fiz/vi) | Causa raiz (arquivo e linha aproximada) | Correção aplicada | Conceito da disciplina |
|---|---|---|---|---|
| bug01 | `GeradorProtocoloTest` vermelho: `assertSame` falha (duas instâncias diferentes) e `deveGerarProtocolosSequenciais` devolve 1,1,1 em vez de 1,2,3 | `GeradorProtocolo.java`, método `getInstancia()`: o `if (instancia == null)` cria `new GeradorProtocolo()` mas nunca atribui o resultado ao campo estático `instancia` | Passei a atribuir: `instancia = new GeradorProtocolo();` antes de retornar | Padrão Singleton (Aula 14) — a instância precisa ser guardada em um campo estático para ser reaproveitada |
| bug02 | `AtendimentoBuilderTest.deveMontarAtendimentoCompleto` falha: `expected: <Rex> but was: <null>` | `AtendimentoBuilder.java`, método `comPet(String petNome, String petPorte)`: a linha `petNome = petNome;` reatribui o **parâmetro** a ele mesmo, `this.petNome` nunca é setado | Troquei para `this.petNome = petNome;` | Shadowing de variável — sem `this.`, o parâmetro só se refere a si mesmo, nunca ao atributo da classe |
| bug03 | `deveRecusarMontagemSemNomeDoPet` e `deveRecusarMontagemSemPorte` esperavam `IllegalArgumentException` que nunca era lançada | `AtendimentoBuilder.java`, método `construir(int protocolo)`: delegava direto para a Factory, sem nenhuma validação | Adicionei checagem de `petNome`/`petPorte` nulos ou em branco, lançando `IllegalArgumentException` antes de chamar a Factory | "O objeto só nasce válido" — validação concentrada no ponto de construção (Builder, Aula 14) |
| bug04 | `AtendimentoFactoryTest.deveCriarTosaQuandoTipoForTosa` falha: `assertInstanceOf(Tosa.class, ...)` recebe um `Banho` | `AtendimentoFactory.java`, `switch`/`case`: `case "TOSA" -> new Banho(...)` | Troquei para `case "TOSA" -> new Tosa(...)` | Factory (Aula 14) — o `switch` é o único lugar que conhece as subclasses concretas; `case` trocado é clássico erro de copy-paste |
| bug05 | `AtendimentoFactoryTest.devePreencherOsDadosDoPetNaConsulta` falha: `getPetNome()`, `getPetPorte()`, `getTutorNome()` vêm todos `null` | `ConsultaVeterinaria.java`, construtor: chamava `super()` (vazio), descartando todos os parâmetros recebidos | Troquei para `super(protocolo, petNome, petPorte, tutorNome, dataHora);` | Encadeamento de construtores (`super(...)`) — copiar a assinatura de um construtor sem repassar os argumentos ao pai quebra o objeto inteiro |
| bug06 | `AgendaServiceTest.deveRecusarAgendamentoComHorarioJaOcupado` falha: em vez da `HorarioOcupadoException`, o serviço lança `NullPointerException` (o horário conflitante não era detectado) | `AgendaService.java`, `agendar()`: comparava `a.getDataHora() == novo.getDataHora()` (e `petNome` também) por referência (`==`) em vez de valor | Troquei ambas as comparações para `.equals()` | `==` vs `.equals()` (Aula 7) para tipos referência — `==` compara identidade de objeto, não o conteúdo |
| bug07 | `AgendaServiceTest.deveLancarExcecaoQuandoAtendimentoNaoExiste` falha: nenhuma exceção é lançada, `buscarPorId` devolve `null` | `AgendaService.java`, `buscarPorId()`: um `try { ... } catch (Exception e) { return null; }` engolia a própria `AtendimentoNaoEncontradoException` lançada duas linhas acima | Removi o `try/catch` genérico — o método agora deixa a exceção propagar normalmente | Exceções unchecked (Aula 11) — `catch` genérico "engolindo" uma exceção de negócio é um antipadrão clássico |
| bug08 | Teste novo `TosaTest.deveDurar60Minutos` nasceu vermelho: `expected: <60> but was: <30>` | `Tosa.java`: existia `public int getDuracaoMinutos(String porte)` — assinatura diferente da superclasse, então é **overload**, não **override** — o objeto continuava usando o `return 30;` herdado | Removi o método com assinatura errada e criei `@Override public int getDuracaoMinutos() { return DURACAO_MINUTOS; }` | Override vs overload (Aula 7) — assinatura diferente cria um método novo, nunca chamado; `@Override` faz o compilador acusar esse tipo de erro na hora |
| bug09 | Teste novo `BanhoTest.deveCustar60ReaisParaPortePequeno` nasceu vermelho: `expected: <60.0> but was: <100.0>` | `Banho.java`, `calcularPreco()`: os valores de porte PEQUENO (100.0) e GRANDE (60.0, no `else`) estavam invertidos em relação ao contrato | Troquei os valores: PEQUENO → 60.0, GRANDE (else) → 100.0 | Regra de negócio no model (Aula 13/14) — nenhum teste original cobria preço de Banho, então o valor errado nunca havia sido detectado |
| bug10 | Teste novo `AgendaServiceTest.deveRecusarAgendamentoComDataHoraNoPassado` nasceu vermelho: era lançada `NullPointerException` em vez de `IllegalArgumentException` | `AgendaService.java`, `agendar()`: não existia nenhuma validação de data/hora no passado | Adicionei checagem `if (novo.getDataHora().isBefore(LocalDateTime.now()))` **antes** de qualquer chamada ao `repository` (confirmado com `verify(repository, never())...`) | Validação de regra de negócio na camada de serviço, antes de tocar a persistência |
| bug11 | Testes novos `AtendimentoTest.deveRecusarCancelamentoQuandoAtendimentoJaConcluido` e `...JaCancelado` nasceram vermelhos: nenhuma exceção era lançada | `Atendimento.java`, `cancelar()`: setava `status = "CANCELADO"` sem checar o status atual | Adicionei a mesma validação que `concluir()` já tinha: só cancela se o status atual for AGENDADO, senão lança `StatusInvalidoException` | Transições de estado (Aula 13/14) — o objeto deve proteger seu próprio ciclo de vida |
| bug12 | Nenhum teste unitário acusa (todos usam `@Mock`, sem JPA real). Encontrei lendo o mapeamento da entidade e **confirmei rodando a API de verdade**, com H2 em memória: `POST /api/atendimentos` respondia **HTTP 500**, com `org.hibernate.id.IdentifierGenerationException: Identifier of entity 'br.com.fiap.petfiap.model.Banho' must be manually assigned before calling 'persist()'` no log — o Hibernate recusa salvar porque não sabe gerar o `id` sozinho, não devolve um `id: null` silencioso | `Atendimento.java`: `@Id private Long id;` sem `@GeneratedValue` — a chave primária nunca era gerada pelo banco | Adicionei `@GeneratedValue(strategy = GenerationType.IDENTITY)`; refiz o mesmo teste manual e o `POST` passou a responder `201` com `id` preenchido | Mapeamento JPA (Aula 12/13) — sem `@GeneratedValue` o Hibernate não tem estratégia de geração de PK e recusa o `persist()` |
| bug13 (extra, fora dos 12 do contrato) | Nenhum teste unitário acusa — os 26 testes são single-threaded. `GeradorProtocolo` tinha um comentário dizendo "Thread-safe para o uso concorrente", mas isso não era verdade: `getInstancia()` podia criar duas instâncias em uma corrida na inicialização, e `contador++` não é uma operação atômica (é leitura + incremento + escrita), então duas requisições simultâneas na API podiam gerar o mesmo protocolo | `GeradorProtocolo.java`: `getInstancia()` e `proximo()` sem nenhuma sincronização | Adicionei `synchronized` nos dois métodos, tornando o comentário da classe finalmente verdadeiro | Concorrência básica (condição de corrida / race condition) — um Singleton "feito à mão" só é thread-safe se o próprio código garantir isso explicitamente; o container do Spring não ajuda aqui porque essa classe não é um bean gerenciado |

## Parte 2 — Ajustes de Clean Code

| # | Onde estava | Qual princípio/boas práticas era violado | O que eu mudei |
|---|---|---|---|
| clean01 | `Banho.java`, `Tosa.java`, `ConsultaVeterinaria.java` — `calcularPreco()`, `calcularPontosFidelidade()`, `getDuracaoMinutos()`; depois também `Atendimento.java` (`getDuracaoMinutos()` padrão) | Números mágicos (`60.0`, `80.0`, `20`, `45`, etc.) espalhados direto no código, incluindo o `return 30;` padrão da classe abstrata que passou batido na primeira revisão | Extraí constantes privadas nomeadas (`PRECO_PEQUENO`, `PRECO_MEDIO`, `PRECO_GRANDE`, `PONTOS_FIDELIDADE`, `DURACAO_MINUTOS`, `PRECO_FIXO`) em cada subclasse, e `DURACAO_PADRAO_MINUTOS` em `Atendimento` |
| clean02 | `AtendimentoFactory.java`, método `criar(...)` | Parâmetros de uma letra só (`p, t, n, po, tu, d`), difícil de ler sem abrir a implementação | Renomeei para `protocolo, tipo, petNome, petPorte, tutorNome, dataHora` |
| clean03 | `AgendaService.agendar()` e `GeradorProtocolo` (construtor) | `System.out.println` usado como log de produção | Troquei o "recibo" do `AgendaService` por `log.info(...)` com SLF4J (`LoggerFactory.getLogger`); removi o `println` do `GeradorProtocolo` (Singleton simples não precisa de logger) |
| clean04 | `AgendaService.agendar()` | Método fazendo validação de data + laço de verificação de conflito + persistência + log tudo junto, muito aninhamento; o laço ainda comparava `petNome` de novo dentro do `for`, redundante porque `findByPetNome` já devolve só os atendimentos daquele pet | Extraí `validarDataHoraFutura(Atendimento)` e `validarHorarioLivre(Atendimento)` como métodos privados (`agendar()` ficou um resumo de 3 passos) e removi a comparação de `petNome` redundante dentro do laço |
| clean05 | `AtendimentoController.java` | Método privado `calcularDescontoFidelidade`, nunca chamado por ninguém, guardado "para o futuro" com comentário especulativo | Removi o método e o comentário (violação de YAGNI — você não vai precisar disso) |
| clean06 | `Atendimento.java` (`concluir`/`cancelar`/construtor) e `AgendaService.java` (`validarHorarioLivre`) | Strings literais `"AGENDADO"`, `"CONCLUIDO"`, `"CANCELADO"` repetidas em mais de uma classe | Extraí `STATUS_AGENDADO`, `STATUS_CONCLUIDO`, `STATUS_CANCELADO` como constantes públicas em `Atendimento` e usei em todos os pontos que comparavam essas strings |

## Parte 3 — Testes novos (regras que estavam sem cobertura)

| # | Teste escrito (classe.método) | Regra coberta | Resultado ao escrever (vermelho/verde) |
|---|---|---|---|
| teste01 | `TosaTest.deveDurar60Minutos` | Contrato de duração: TOSA deve durar 60 minutos | Vermelho — revelou o bug08 (overload em vez de override) |
| teste02 | `BanhoTest.deveCustar60ReaisParaPortePequeno` | Contrato de preço: BANHO porte PEQUENO deve custar R$ 60 | Vermelho — revelou o bug09 (preços PEQUENO/GRANDE invertidos) |
| teste03 | `AgendaServiceTest.deveRecusarAgendamentoComDataHoraNoPassado` | Regra de agendamento: data/hora no passado deve ser recusada com `IllegalArgumentException`, sem consultar o banco | Vermelho — revelou o bug10 (validação inexistente) |
| teste04 | `AtendimentoTest.deveRecusarCancelamentoQuandoAtendimentoJaConcluido` | Regra de status: `cancelar()` deve recusar quando o atendimento já está CONCLUIDO | Vermelho — revelou o bug11 (cancelar sem validação de status) |
| teste05 | `AtendimentoTest.deveRecusarCancelamentoQuandoAtendimentoJaCancelado` | Regra de status: `cancelar()` deve recusar quando o atendimento já está CANCELADO | Vermelho — mesma causa do teste04 (bug11), corrigida pela mesma alteração |
| teste06 | `ConsultaVeterinariaTest.deveCustar150ReaisIndependenteDoPorte` | Contrato de preço: CONSULTA custa R$ 150 fixo, independente do porte (PEQUENO, MEDIO e GRANDE) | Verde de cara — a regra já estava implementada corretamente, o teste passou a proteger contra regressão futura |

---

## Parte 4 — Perguntas de reflexão

### 1. A suíte como contrato (Aula 15)

O projeto chegou com 20 testes e 9 vermelhos. Rodei a suíte inteira primeiro e fui classe por
classe: em `GeradorProtocoloTest`, o `assertSame` acusava `expected: <GeradorProtocolo@32c4e8b2>
but was: <GeradorProtocolo@64bce832>` — duas referências diferentes, sinal claro de que o Singleton
não estava guardando a instância. Em `AtendimentoFactoryTest`, `expected: <Tosa> but was: <Banho>`
apontou direto para o `switch` da Factory. Em `AtendimentoBuilderTest`,
`expected: <Rex> but was: <null>` mandou eu olhar exatamente o método `comPet()`. Cada mensagem de
falha é uma bússola: ela diz o que era esperado e o que realmente veio, e isso encurta muito a busca
pela causa raiz, porque elimina hipóteses erradas antes mesmo de eu abrir o código.

A vantagem sobre testar tudo na mão com `curl` é a velocidade e a cobertura: os 26 testes rodam em
menos de 2 segundos, sem subir Spring, sem banco, sem eu precisar montar manualmente cada cenário
(porte PEQUENO, MEDIO, GRANDE; status AGENDADO, CONCLUIDO, CANCELADO) toda vez que eu mudo uma
linha. Com `curl` eu só descobriria o bug09 (preço do Banho trocado) se por acaso testasse os três
portes manualmente; com o teste novo, isso vira parte permanente da suíte.

### 2. Mock e injeção de dependência (Aulas 13 a 15)

Em produção, o `@Service` do `AgendaService` e o `@Autowired private AtendimentoRepository
repository` são resolvidos pelo container do Spring: na inicialização, o Spring cria os beans (o
`AtendimentoRepository` é uma interface `JpaRepository`, e o Spring Data gera uma implementação real
por trás dela, conectada ao Oracle) e injeta a instância real no campo `repository` via reflection.

No teste, `@ExtendWith(MockitoExtension.class)` + `@Mock private AtendimentoRepository repository` +
`@InjectMocks private AgendaService service` fazem o Mockito "brincar de Spring": ele cria um
`AtendimentoRepository` falso (um proxy que só faz o que eu ensinei com `when(...).thenReturn(...)`)
e injeta esse falso no `AgendaService`, sem precisar subir contexto Spring nenhum. É por isso que
`AgendaServiceTest` roda sem banco e sem rede — não existe conexão JDBC real em nenhum momento, só
um objeto que devolve exatamente o que o teste configurou.

### 3. `==` vs `.equals()` (Aula 7)

Esse foi o bug06. No `deveRecusarAgendamentoComHorarioJaOcupado`, o teste cria um novo `Banho` usando
`LocalDateTime.parse(existente.getDataHora().toString())` — de propósito, um objeto **diferente**,
com o **mesmo valor**. O código original comparava `a.getDataHora() == novo.getDataHora()`, que
verifica se são o **mesmo objeto na memória** (mesma referência), não se representam o mesmo
instante. Como `parse` sempre cria uma instância nova, `==` dava `false` mesmo os horários sendo
idênticos, e o conflito nunca era detectado.

A comparação de `petNome` com `==` "funcionava por sorte" nesse teste porque ambos usam o literal
`"Rex"` no código-fonte, e a JVM faz *string interning*: literais idênticos apontam para o mesmo
objeto no pool de strings. Mas isso é uma armadilha — bastaria o nome vir de uma linha do banco (via
`resultSet.getString(...)`) ou de `new String("Rex")` para quebrar, porque aí seriam objetos
diferentes com o mesmo conteúdo. Troquei as duas comparações para `.equals()`, que compara o
**conteúdo** dos objetos, independentemente de serem a mesma instância — é a forma correta de
comparar `String` e `LocalDateTime` (ou qualquer tipo referência) por igualdade de valor.

### 4. Sobrescrita vs sobrecarga (Aula 7)

Esse foi o bug08. `Tosa` tinha `public int getDuracaoMinutos(String porte)`, com um parâmetro a
mais do que `Atendimento.getDuracaoMinutos()` (sem parâmetro nenhum). Assinaturas diferentes
(mesmo nome, parâmetros diferentes) fazem o Java tratar isso como **sobrecarga** (overload): um
método novo e independente, que só é chamado se alguém explicitamente passar um `String` como
argumento. Como nada no projeto chamava `getDuracaoMinutos("PEQUENO")`, esse método simplesmente
nunca era executado — e todo `Tosa` continuava usando o `return 30;` herdado de `Atendimento`
(o valor padrão da classe abstrata), em vez dos 60 minutos que o contrato exige.

**Sobrescrita** (override) exige a mesma assinatura (mesmo nome, mesmos parâmetros) e é isso que
permite o polimorfismo: o objeto `Tosa` substitui o comportamento herdado. A anotação `@Override`
existe justamente para isso — se eu tivesse escrito `@Override public int
getDuracaoMinutos(String porte)`, o compilador teria recusado a compilação com "method does not
override a method from its superclass", porque `Atendimento` não tem nenhum método com essa
assinatura. O bug só existiu porque a anotação estava ausente; corrigi removendo o método errado e
criando `@Override public int getDuracaoMinutos() { return DURACAO_MINUTOS; }`, com a assinatura
correta.

### 5. Singleton manual vs bean do Spring (Aula 14)

`GeradorProtocolo` é um Singleton escrito à mão: um construtor privado, um campo estático
`instancia`, e um método `getInstancia()` que deveria devolver sempre a mesma referência. O bug01
estava em `getInstancia()`: o `if (instancia == null) { return new GeradorProtocolo(); }` criava um
objeto novo a cada chamada, mas **nunca atribuía esse objeto ao campo `instancia`** — então na
próxima chamada, `instancia` continuava `null`, e um outro objeto novo era criado de novo. Isso
quebrava tanto a identidade do Singleton (`assertSame` falhava) quanto a numeração sequencial dos
protocolos (cada objeto novo começava com `contador = 0`). A correção foi simples: atribuir
`instancia = new GeradorProtocolo();` antes de retornar, garantindo que a segunda chamada em diante
reaproveite a mesma instância.

O `AgendaService` (anotado com `@Service`) não corre esse risco porque quem cria e guarda a
instância não é código meu, e sim o **container do Spring**: por padrão, todo bean gerenciado pelo
Spring tem escopo `singleton`, e o próprio container garante — de forma testada e centralizada pelo
framework — que existe só uma instância por contexto de aplicação, sem que eu precise escrever
nenhuma lógica de "se for nulo, crie; senão, reaproveite". Um Singleton manual depende de eu acertar
essa lógica sozinho (e é fácil errar, como o bug01 mostrou); um bean Spring terceiriza essa
responsabilidade para um framework maduro.

Vale um adendo: depois de corrigir o bug01, notei que o comentário da classe dizia "Thread-safe para
o uso concorrente do pet shop", mas isso ainda não era verdade — `getInstancia()` podia criar duas
instâncias em uma corrida na inicialização, e `contador++` não é atômico (é leitura, soma e escrita
em três passos separados), então duas requisições simultâneas poderiam gerar o mesmo número de
protocolo. Registrei isso como **bug13** (fora da contagem oficial de 12) e resolvi com
`synchronized` em `getInstancia()` e `proximo()`. Isso reforça o ponto: um bean `@Service` do Spring
também não ganha thread-safety de graça só por estar no container — o Spring garante uma instância
única, não que o *estado mutável* dentro dela seja acessado com segurança por múltiplas threads. No
caso do `AgendaService` isso não é problema porque ele não guarda estado próprio (todo estado vive no
banco, via `repository`); já o `GeradorProtocolo` guarda estado (`contador`) na própria instância, e
por isso precisava da sincronização explícita.

### 6. Cobertura de testes: onde parar? (Aula 15)

Dos 6 testes novos que escrevi, 5 ficaram vermelhos de primeira (teste01, teste02, teste03, teste04
e teste05 — os dois últimos revelando a mesma causa raiz, o bug11) e só 1 nasceu verde de cara
(teste06, confirmando que o preço fixo da Consulta já estava correto). Mesmo o teste que nasceu
verde valeu a pena manter: ele documenta uma regra de negócio importante (preço não muda por porte
na Consulta) e passa a barrar qualquer alteração futura que quebre isso sem ninguém perceber — sem
ele, um refactor descuidado em `ConsultaVeterinaria` poderia reintroduzir um bug parecido com o
bug05 sem nenhum teste apontando.

Em um projeto real com prazo apertado, eu priorizaria nesta ordem: primeiro os **caminhos de erro
que envolvem dinheiro ou integridade de dado** (preço errado, chave primária não gerada — bug09 e
bug12 deste checkpoint são exatamente esse tipo de problema, e nenhum dos dois aparecia em teste
nenhum até eu procurar ativamente), depois as **transições de estado** (status inválido permitindo
operação que não deveria, como o bug11), e só depois perseguiria 100% de cobertura de linha. 100% de
cobertura não garante nada sozinho — dá pra ter um teste "verde" que não afirma o valor certo (como
os testes de duração e preço que faltavam neste projeto antes de eu escrevê-los); o que importa é
cobrir as regras de negócio que, se quebrarem, custam dinheiro ou dado corrompido primeiro.

---

## Parte 5 — Espaço livre (opcional)

Nenhuma dificuldade relevante. O projeto compila, a suíte roda 26/26 verde via `mvn test`, e a API
sobe normalmente (testada localmente com H2 em memória, sem alterar o `application.properties`
versionado — ele continua com `SEU_RM`/`SUA_SENHA` como o enunciado pede).

Um cuidado que tomei: antes de descrever o bug12 no README, rodei a API de propósito **na versão sem
a correção** (`git worktree` num commit anterior) só para ver o erro real, em vez de supor. O erro
real foi `HTTP 500` com `IdentifierGenerationException`, não um `id: null` silencioso como eu tinha
escrito numa versão anterior deste README — corrigi a redação depois de reproduzir o comportamento de
verdade.
