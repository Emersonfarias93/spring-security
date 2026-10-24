# Exemplo de Spring Security

Projeto didático em Java para demonstrar autenticação e autorização com Spring Security.

Esta primeira entrega atualiza a base e torna a execução reproduzível. A aplicação inicia com a segurança padrão do Spring: formulário de login, HTTP Basic e usuário temporário em memória. Ainda não existem endpoints próprios de negócio.

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

O comando executa três testes HTTP com servidor em uma porta disponível, além de gerar `target/spring-sercurity-0.0.1-SNAPSHOT.jar`. Os testes verificam o formulário padrão de login, a exigência de autenticação para JSON e o redirecionamento do navegador para login.

A grafia atual `spring-sercurity` nos identificadores e no JAR foi preservada para manter esta entrega focada na atualização da base.

## Executar

Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

Linux / macOS:

```bash
./mvnw spring-boot:run
```

Ou, após gerar o JAR, em qualquer sistema com o JDK selecionado:

```text
java -jar target/spring-sercurity-0.0.1-SNAPSHOT.jar
```

Abra [http://localhost:8080/login](http://localhost:8080/login). O usuário é `user`; a senha temporária aparece no console após `Using generated security password:` e muda a cada inicialização. Não é necessário definir variáveis de credenciais nesta etapa.

Essas credenciais fazem parte da autoconfiguração de desenvolvimento do Spring. A política própria de usuários e permissões será implementada nas próximas entregas.

Para encerrar, pressione `Ctrl+C` no terminal da aplicação. Se a porta 8080 estiver ocupada, escolha outra ao executar o JAR, por exemplo:

```text
java -jar target/spring-sercurity-0.0.1-SNAPSHOT.jar --server.port=8081
```

Nesse caso, ajuste a porta nas URLs.

## Conferir o comportamento atual

Com a aplicação em execução, no PowerShell use `curl.exe`; no Linux / macOS use `curl`.

```powershell
# Formulário padrão: 200
curl.exe -i http://localhost:8080/login

# Cliente JSON sem credenciais: 401
curl.exe -i -H "Accept: application/json" http://localhost:8080/

# Navegador sem credenciais: 302 para /login
curl.exe -i -H "Accept: text/html" http://localhost:8080/

# Informe a senha temporária quando o curl solicitar: 404 após autenticar
curl.exe -i -u user -H "Accept: application/json" http://localhost:8080/
```

O 404 com credenciais válidas é esperado nesta etapa: a raiz `/` ainda não possui controller. O formulário de login é fornecido pelo framework; ele não representa uma API própria de autenticação ou emissão de tokens.

## Entregas por prioridade

Cada entrega reúne implementação, testes e documentação correspondentes no mesmo commit de valor.

1. **Base atualizada e execução reproduzível:** Spring Boot atualizado, validação do ambiente, testes HTTP de inicialização e instruções de execução.
2. **Consulta pública e identificação autenticada:** implementar `/api/public/hello` e `/api/me`, com credenciais didáticas configuráveis e testes de acesso.
3. **Acesso administrativo por perfil:** implementar `/api/admin/hello`, restringir a ADMIN e testar os resultados 200, 401 e 403.
4. **Verificação automática:** adicionar build/testes no GitHub Actions e acompanhamento de dependências com Dependabot.

Somente a primeira entrega está contemplada nesta revisão. As rotas `/api/**` acima são planejadas e ainda não estão disponíveis.

## Diagramas e referências

- [Fluxograma atual, arquitetura e diagrama da API planejada](docs/diagramas.md).
- [Requisitos oficiais do Spring Boot](https://docs.spring.io/spring-boot/system-requirements.html).
- [Migração para Spring Boot 4.0](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Migration-Guide).
- [Notas do Spring Boot 4.1](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.1-Release-Notes).

A validação desta entrega foi realizada no Windows com JDK 17. Os comandos Linux / macOS são fornecidos para uso do Wrapper e ainda não foram executados nesses sistemas nesta revisão.
