# Fluxograma e diagrama da API

Os diagramas distinguem o comportamento disponível das próximas entregas. O GitHub renderiza os blocos Mermaid diretamente.

## Fluxograma atual

Fluxo das consultas GET desta segunda entrega. Credenciais inválidas enviadas explicitamente são rejeitadas antes da autorização, inclusive na rota pública.

```mermaid
flowchart TD
    A[Cliente envia GET] --> B{Enviou HTTP Basic?}
    B -->|Sim| C{Credenciais válidas?}
    C -->|Não| U[401 JSON]
    C -->|Sim| D{Rota solicitada}
    B -->|Não| D
    D -->|/api/public/hello| P[200 JSON com mensagem]
    D -->|/api/me| E{Autenticado nesta chamada?}
    E -->|Sim| M[200 JSON com nome e papéis]
    E -->|Não| U
    D -->|Outra rota| F{Autenticado?}
    F -->|Sim| X[403 JSON]
    F -->|Não| U
```

## Arquitetura atual

```mermaid
flowchart LR
    C[Cliente HTTP] --> T[Tomcat embutido]
    T --> S[SecurityFilterChain]
    S <--> U[Usuário em memória com BCrypt]
    V[Credenciais por ambiente] --> U
    S --> E[Erros de segurança em JSON]
    S --> M[Spring MVC]
    M --> R[IdentityController]
    R --> P[Consulta pública]
    R --> I[Identificação autenticada]
```

A autenticação é HTTP Basic e não é persistida em sessão. A proteção CSRF permanece ativa; o fluxo acima resume somente GETs. Não há formulário de login, cadastro, emissão de tokens, frontend ou banco de dados.

## API disponível

Os dois endpoints abaixo estão implementados e cobertos por testes HTTP com autenticação real.

| Método e rota | Acesso | Resposta |
| --- | --- | --- |
| `GET /api/public/hello` | Livre | 200 com mensagem |
| `GET /api/me` | Autenticado | 200 com nome e papéis; 401 sem autenticação válida |

```mermaid
sequenceDiagram
    actor Cliente
    participant Seguranca as Spring Security
    participant Usuarios as Usuários em memória
    participant API as IdentityController

    Cliente->>Seguranca: GET /api/public/hello
    Seguranca->>API: Permitir acesso público
    API-->>Cliente: 200 JSON

    Cliente->>Seguranca: GET /api/me com HTTP Basic
    Seguranca->>Usuarios: Validar credenciais
    Usuarios-->>Seguranca: Resultado da comparação BCrypt e papel USER
    alt Credenciais inválidas ou ausentes
        Seguranca-->>Cliente: 401 JSON
    else Usuário autenticado
        Seguranca->>API: Encaminhar usuário
        API-->>Cliente: 200 JSON com nome e papéis
    end
```

## Próxima entrega: acesso administrativo

`GET /api/admin/hello` ainda não está implementado. A terceira entrega incluirá o usuário ADMIN, a regra administrativa e testes dos resultados 200, 401 e 403. Atualmente essa rota segue o bloqueio padrão das rotas não liberadas.

Atualizar estes diagramas junto com os testes e a documentação da terceira entrega.
