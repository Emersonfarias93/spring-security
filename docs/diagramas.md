# Fluxograma e diagrama da API

Os diagramas distinguem o comportamento disponível das próximas entregas. O GitHub renderiza os blocos Mermaid diretamente.

## Fluxograma atual

Fluxo das consultas `GET /` e acesso ao formulário padrão nesta primeira entrega:

```mermaid
flowchart TD
    A[Cliente faz GET /] --> B[Spring Security padrão]
    B --> C{Credenciais Basic válidas?}
    C -->|Sim| D[Spring MVC procura a rota]
    D --> E[Não há controller para a raiz]
    E --> F[404 Not Found]
    C -->|Senha inválida| G[401 Unauthorized]
    C -->|Sem credenciais| H{Resposta solicitada}
    H -->|JSON| G
    H -->|HTML| I[302 para /login]
    I --> J[Formulário de login retorna 200]
```

## Arquitetura atual

```mermaid
flowchart LR
    C[Cliente HTTP] --> T[Tomcat embutido]
    T --> S[Spring Security padrão]
    S <--> U[Usuário temporário em memória]
    S --> L[Formulário de login do framework]
    S --> M[Spring MVC]
    M --> R[Sem controllers próprios]
```

A aplicação possui segurança padrão, mas ainda não expõe API de negócio, cadastro ou tokens JWT. Não há frontend ou banco de dados.

## API planejada para as entregas 2 e 3

**Este contrato ainda não está implementado.** A proposta é demonstrar HTTP Basic, respostas JSON e acesso por perfil em três consultas.

| Método e rota planejada | Acesso | Resposta esperada |
| --- | --- | --- |
| `GET /api/public/hello` | Livre | 200 com mensagem |
| `GET /api/me` | Autenticado | 200 com nome e papéis; 401 sem autenticação válida |
| `GET /api/admin/hello` | ADMIN | 200 para ADMIN; 401 sem autenticação válida; 403 para USER |

```mermaid
sequenceDiagram
    actor Cliente
    participant Seguranca as Spring Security
    participant Usuarios as Usuários em memória
    participant API as Controllers REST

    Cliente->>Seguranca: GET /api/public/hello
    Seguranca->>API: Permitir acesso público
    API-->>Cliente: 200 JSON

    Cliente->>Seguranca: GET /api/me com HTTP Basic
    Seguranca->>Usuarios: Validar credenciais
    Usuarios-->>Seguranca: Resultado e papéis
    alt Credenciais inválidas ou ausentes
        Seguranca-->>Cliente: 401 JSON
    else Usuário autenticado
        Seguranca->>API: Encaminhar usuário
        API-->>Cliente: 200 JSON com nome e papéis
    end

    Cliente->>Seguranca: GET /api/admin/hello com HTTP Basic
    Seguranca->>Usuarios: Validar credenciais e papéis
    Usuarios-->>Seguranca: Resultado da validação
    alt Credenciais inválidas ou ausentes
        Seguranca-->>Cliente: 401 JSON
    else Autenticado sem ADMIN
        Seguranca-->>Cliente: 403 JSON
    else Autenticado com ADMIN
        Seguranca->>API: Permitir consulta administrativa
        API-->>Cliente: 200 JSON
    end
```

Atualizar estes diagramas junto com os testes e a documentação de cada entrega. A política de sessão e CSRF será explicitada quando a configuração própria da API for implementada.
