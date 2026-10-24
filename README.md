# Exemplo de Spring Security

Projeto didático em Java para demonstrar autenticação e autorização com Spring Security.

API didática com consulta pública, identificação por HTTP Basic e consulta administrativa restrita ao papel `ADMIN`. Os usuários de demonstração ficam em memória, com senhas configuradas por variáveis de ambiente e protegidas por BCrypt.

## Requisitos

| Ferramenta | Versão |
| --- | --- |
| JDK | 17 como referência de compilação e validação |
| Spring Boot | 4.1.2 |
| Maven | 3.9.7 pelo Wrapper incluído |

Não é necessário instalar Maven separadamente. A primeira execução precisa de acesso à internet para baixar o Maven e as dependências. Não há banco de dados ou frontend para configurar.

O build verifica Java 17 ou superior e Maven 3.6.3 ou superior antes de compilar. Para reproduzir a validação desta entrega, use JDK 17. As dependências Spring são gerenciadas pela versão do Spring Boot no `pom.xml`.

## Preparar o Java

Confira o Java efetivamente usado pelo Maven, que pode ser diferente daquele configurado na IDE.

### Windows / PowerShell

Na pasta do projeto:

```powershell
.\mvnw.cmd -version
```

Se o comando mostrar Java 8 ou outro JDK inadequado, ajuste a sessão. Substitua o caminho abaixo pela pasta do seu JDK 17, que deve conter `bin\javac.exe`:

```powershell
$env:JAVA_HOME = "C:\caminho\para\jdk-17"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
.\mvnw.cmd -version
```

Esses comandos alteram somente o terminal atual.

### Linux / macOS

Substitua o caminho pelo seu JDK 17:

```bash
export JAVA_HOME="/caminho/para/jdk-17"
export PATH="$JAVA_HOME/bin:$PATH"
chmod +x mvnw
./mvnw -version
```

## Compilar e testar

Windows:

```powershell
.\mvnw.cmd -B -ntp clean verify
```

Linux / macOS:

```bash
./mvnw -B -ntp clean verify
```

O comando executa 29 testes, incluindo consultas HTTP com servidor em uma porta disponível e validações da configuração de credenciais. Cobre acesso público, identificação, autorização administrativa, autenticação inválida, ausência de sessão de autenticação, proteção CSRF e bloqueio de rotas não liberadas. Os testes usam credenciais próprias de teste e não exigem variáveis de ambiente.

O JAR é gerado em `target/spring-sercurity-0.0.1-SNAPSHOT.jar`. A grafia original `spring-sercurity` nos identificadores foi preservada.

## Executar

Defina as credenciais no mesmo terminal em que iniciará a aplicação:

| Variável | Obrigatória | Comportamento |
| --- | --- | --- |
| `DEMO_USER_NAME` | Não | Padrão `user`; não aceita valor vazio |
| `DEMO_USER_PASSWORD` | Sim | Senha de demonstração; sem valor, a inicialização é interrompida com orientação |
| `DEMO_ADMIN_NAME` | Não | Padrão `admin`; não aceita valor vazio ou igual ao nome do usuário comum, mesmo com diferença de maiúsculas |
| `DEMO_ADMIN_PASSWORD` | Sim | Senha do administrador de demonstração; sem valor, a inicialização é interrompida com orientação |

Defina as duas senhas para iniciar a aplicação. Os papéis são fixos: o usuário comum possui `USER`, e o administrador possui `USER` e `ADMIN`. O cliente não escolhe ou altera esses papéis pela requisição.

Windows / PowerShell:

```powershell
$env:DEMO_USER_NAME = "user"
$demoPassword = Read-Host "Defina a senha de demonstração" -AsSecureString
$env:DEMO_USER_PASSWORD = [System.Net.NetworkCredential]::new("", $demoPassword).Password
$env:DEMO_ADMIN_NAME = "admin"
$adminPassword = Read-Host "Defina a senha do administrador de demonstração" -AsSecureString
$env:DEMO_ADMIN_PASSWORD = [System.Net.NetworkCredential]::new("", $adminPassword).Password
.\mvnw.cmd spring-boot:run
```

Linux / macOS, em Bash:

```bash
export DEMO_USER_NAME="user"
read -r -s -p "Defina a senha de demonstração: " DEMO_USER_PASSWORD
export DEMO_USER_PASSWORD
export DEMO_ADMIN_NAME="admin"
read -r -s -p "Defina a senha do administrador de demonstração: " DEMO_ADMIN_PASSWORD
export DEMO_ADMIN_PASSWORD
./mvnw spring-boot:run
```

Ou, após definir as mesmas variáveis e gerar o JAR, em qualquer sistema com o JDK selecionado:

```text
java -jar target/spring-sercurity-0.0.1-SNAPSHOT.jar
```

### Executar pelo IntelliJ IDEA

As variáveis definidas no terminal integrado não são automaticamente aplicadas ao botão Run da IDE. Configure-as também na execução da aplicação:

1. Abra **Run > Edit Configurations** e selecione a configuração de `SpringSercurityApplication`.
2. Em **Modify options**, habilite **Environment variables**, se o campo estiver oculto.
3. Adicione `DEMO_USER_NAME=user`, `DEMO_ADMIN_NAME=admin` e as duas senhas escolhidas por você em `DEMO_USER_PASSWORD` e `DEMO_ADMIN_PASSWORD`.
4. Clique em **Apply** e execute novamente.

Mantenha a configuração com as senhas local à IDE; não a compartilhe como arquivo do projeto. As mensagens `Defina DEMO_USER_PASSWORD antes de iniciar a aplicação.` e `Defina DEMO_ADMIN_PASSWORD antes de iniciar a aplicação.` indicam qual senha não foi recebida pela execução.

Veja também a [documentação de configuração Spring Boot no IntelliJ](https://www.jetbrains.com/help/idea/run-debug-configuration-spring-boot.html).

Abra [http://localhost:8080/api/public/hello](http://localhost:8080/api/public/hello). Não há formulário `/login`, senha gerada no console, cadastro, persistência ou emissão de tokens.

Para encerrar, pressione `Ctrl+C` no terminal da aplicação. Se a porta 8080 estiver ocupada, escolha outra ao executar o JAR, por exemplo:

```text
java -jar target/spring-sercurity-0.0.1-SNAPSHOT.jar --server.port=8081
```

Nesse caso, ajuste a porta nas URLs.

## Consultar a API

| Método e rota | Acesso | Resposta |
| --- | --- | --- |
| `GET /api/public/hello` | Público | 200 com mensagem |
| `GET /api/me` | HTTP Basic válido | 200 com nome e papéis; 401 sem credenciais válidas |
| `GET /api/admin/hello` | HTTP Basic com papel ADMIN | 200 com mensagem; 401 sem credenciais válidas; 403 para usuário comum |

Com a aplicação em execução, no PowerShell use `curl.exe`; no Linux / macOS use `curl`.

```powershell
# Consulta pública: 200
curl.exe -i http://localhost:8080/api/public/hello

# Identificação sem credenciais: 401 em JSON
curl.exe -i http://localhost:8080/api/me

# O curl solicita a senha definida na inicialização: 200
curl.exe -i -u user http://localhost:8080/api/me

# Credencial inválida de exemplo: 401
curl.exe -i -u user:senha-incorreta http://localhost:8080/api/me

# Usuário comum autenticado: 403
curl.exe -i -u user http://localhost:8080/api/admin/hello

# Administrador autenticado: 200
curl.exe -i -u admin http://localhost:8080/api/admin/hello

# Identificação do administrador: 200 com os papéis ADMIN e USER
curl.exe -i -u admin http://localhost:8080/api/me
```

Se alterar `DEMO_USER_NAME` ou `DEMO_ADMIN_NAME`, ajuste os nomes nas chamadas. O curl solicita a senha do usuário informado. A rota pública dispensa o cabeçalho `Authorization`; credenciais inválidas enviadas explicitamente também são rejeitadas nessa rota.

Resposta pública:

```json
{"message":"Olá! Este endpoint é público."}
```

Identificação autenticada, usando o nome padrão:

```json
{"username":"user","roles":["USER"]}
```

Erro de autenticação:

```json
{"status":401,"error":"Unauthorized","message":"Autenticação necessária ou credenciais inválidas."}
```

Erro de autorização para um usuário autenticado sem permissão:

```json
{"status":403,"error":"Forbidden","message":"Acesso não permitido."}
```

As respostas não incluem senha, hash ou detalhes internos de exceções.

## Política de segurança

- HTTP Basic: envie as credenciais em cada chamada protegida. Para exposição remota, use HTTPS.
- A autenticação não é persistida em sessão; uma chamada autenticada não libera a próxima chamada sem credenciais.
- O formulário de login e o logout padrão estão desativados. Clientes sem autenticação recebem 401 em JSON, inclusive quando solicitam HTML; não há redirecionamento para login.
- Somente os três GETs documentados são liberados conforme sua regra de acesso. A consulta administrativa exige `ADMIN` no backend. Nas demais rotas, um GET com credenciais válidas recebe 403, inclusive para o administrador; sem credenciais válidas, recebe 401. A raiz `/` continua bloqueada.
- A proteção CSRF permanece ativa para métodos que podem alterar estado. Um POST sem token pode retornar 403 antes de chegar ao controller. Não há endpoints de escrita ou de obtenção de token CSRF nesta entrega. HTTP Basic não elimina o risco de CSRF em navegadores.
- As credenciais de demonstração vêm do ambiente, e os usuários são armazenados em memória com senhas BCrypt. Os nomes precisam ser distintos, inclusive sem distinguir maiúsculas, para impedir colisão entre as contas. Não há persistência de usuários entre inicializações.

## Entregas por prioridade

Cada entrega reúne implementação, testes e documentação correspondentes no mesmo commit de valor.

1. **Base atualizada e execução reproduzível:** integrada à `main` no PR #1.
2. **Consulta pública e identificação autenticada:** integrada à `main` no PR #2.
3. **Acesso administrativo por perfil:** implementado nesta entrega, com `/api/admin/hello` restrito a ADMIN e testes dos resultados 200, 401 e 403.
4. **Verificação automática:** adicionar build/testes no GitHub Actions e acompanhamento de dependências com Dependabot.

A quarta entrega permanece planejada.

## Diagramas e referências

- [Fluxograma, arquitetura e diagrama da API atual](docs/diagramas.md).
- [Requisitos oficiais do Spring Boot](https://docs.spring.io/spring-boot/system-requirements.html).
- [Migração para Spring Boot 4.0](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Migration-Guide).
- [Notas do Spring Boot 4.1](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.1-Release-Notes).

A validação desta entrega foi realizada no Windows com JDK 17. Os comandos Linux / macOS são fornecidos para uso do Wrapper e ainda não foram executados nesses sistemas nesta revisão.
