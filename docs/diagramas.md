# Fluxograma e diagrama da API

Os diagramas distinguem o comportamento disponível das próximas entregas. O GitHub renderiza os blocos Mermaid diretamente.

## Fluxograma atual

Fluxo das consultas GET desta terceira entrega. Credenciais inválidas enviadas explicitamente são rejeitadas antes da autorização, inclusive na rota pública.

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
    D -->|/api/admin/hello| G{Autenticado nesta chamada?}
    G -->|Não| U
    G -->|Sim| H{Possui papel ADMIN?}
    H -->|Sim| Z[200 JSON com mensagem administrativa]
    H -->|Não| X[403 JSON]
    D -->|Outra rota| F{Autenticado?}
    F -->|Sim| X[403 JSON]
    F -->|Não| U
```

## Arquitetura atual

```mermaid
flowchart LR
    C[Cliente HTTP] --> T[Tomcat embutido]
    T --> S[SecurityFilterChain]
    S <--> U[Usuários USER e ADMIN com BCrypt]
    V[Credenciais por ambiente] --> U
    S --> E[Erros de segurança em JSON]
    S --> M[Spring MVC]
    M --> R[IdentityController]
    R --> P[Consulta pública]
    R --> I[Identificação autenticada]
    M --> A[AdminController]
    A --> Q[Consulta restrita a ADMIN]
```

A autenticação é HTTP Basic e não é persistida em sessão. A proteção CSRF permanece ativa; o fluxo acima resume somente GETs. Não há formulário de login, cadastro, emissão de tokens, frontend ou banco de dados.

## API disponível

Os três endpoints abaixo estão implementados e cobertos por testes HTTP com autenticação real.

| Método e rota | Acesso | Resposta |
| --- | --- | --- |
| `GET /api/public/hello` | Livre | 200 com mensagem |
| `GET /api/me` | Autenticado | 200 com nome e papéis; 401 sem autenticação válida |
| `GET /api/admin/hello` | ADMIN | 200 para administrador; 403 para USER; 401 sem autenticação válida |

```mermaid
sequenceDiagram
    actor Cliente
    participant Seguranca as Spring Security
    participant Usuarios as Usuários em memória
    participant API as IdentityController
    participant Admin as AdminController

    Cliente->>Seguranca: GET /api/public/hello
    Seguranca->>API: Permitir acesso público
    API-->>Cliente: 200 JSON

    Cliente->>Seguranca: GET /api/me com HTTP Basic
    Seguranca->>Usuarios: Validar credenciais
    Usuarios-->>Seguranca: Resultado da comparação BCrypt e papéis
    alt Credenciais inválidas ou ausentes
        Seguranca-->>Cliente: 401 JSON
    else Usuário autenticado
        Seguranca->>API: Encaminhar usuário
        API-->>Cliente: 200 JSON com nome e papéis
    end

    Cliente->>Seguranca: GET /api/admin/hello com HTTP Basic
    Seguranca->>Usuarios: Validar credenciais
    Usuarios-->>Seguranca: Resultado e papéis
    alt Sem autenticação válida
        Seguranca-->>Cliente: 401 JSON
    else Autenticado somente com USER
        Seguranca-->>Cliente: 403 JSON
    else Autenticado com ADMIN
        Seguranca->>Admin: Permitir consulta administrativa
        Admin-->>Cliente: 200 JSON com mensagem
    end
```

## Configuração dos usuários

O usuário comum é configurado por `DEMO_USER_NAME` e `DEMO_USER_PASSWORD`. O administrador usa `DEMO_ADMIN_NAME` e `DEMO_ADMIN_PASSWORD`, com os papéis `USER` e `ADMIN`. As duas senhas são obrigatórias; os nomes precisam ser distintos mesmo desconsiderando maiúsculas. `/api/me` retorna apenas nome e papéis, inclusive para ADMIN.

O administrador continua sujeito ao bloqueio das rotas não liberadas e não transfere permissões para a próxima requisição sem credenciais. A próxima entrega adicionará a verificação automática no GitHub e o acompanhamento de dependências.
