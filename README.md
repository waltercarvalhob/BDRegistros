# BDRegistros

Cadastro de titulares (nome, CPF, titulo de eleitor, telefone, endereco) com
controle de consentimento, pensado para atender a LGPD desde o desenho:

- **Sem imagem de documento e sem digital.** So os campos de texto necessarios
  sao armazenados. Impressao digital e dado biometrico (dado sensivel, LGPD
  art. 5 II) e nao ha necessidade dele para este cadastro.
- **Autocadastro com consentimento explicito.** O formulario e preenchido
  pela propria pessoa (ou por um agente, com ela presente), e o consentimento
  (finalidade + canal + quem coletou) e gravado junto com o registro, nao
  implicito.
- **Consulta por CPF restrita e auditada.** So usuarios internos autenticados
  (role `OPERADOR`) podem buscar um cadastro por CPF; toda consulta gera uma
  linha em `log_acesso`.
- **Revogacao de consentimento.** Existe um endpoint para marcar o
  consentimento como revogado e o titular como `INATIVO`, atendendo ao
  direito de exclusao (LGPD art. 18).

## Estrutura

```
backend/   Spring Boot 3 (Java 17) + PostgreSQL + Flyway
frontend/  Angular standalone (formulario de autocadastro)
```

## Como completar o setup

### Backend (Eclipse)

1. Importar `backend/` como projeto Maven existente no Eclipse
   (File > Import > Maven > Existing Maven Projects).
2. Criar um banco Postgres local `bdregistros` e definir `DB_USER` /
   `DB_PASSWORD` como variaveis de ambiente (ou editar
   `application.properties` diretamente para desenvolvimento).
3. Rodar `BdRegistrosApplication`. O Flyway aplica `V1__init.sql`
   automaticamente na primeira subida.
4. Usuarios internos (`ADMIN` / `OPERADOR`) ja tem autenticacao real via
   `BdRegistrosUserDetailsService` + tabela `usuario` (Flyway `V2`).
   Para criar o primeiro administrador, defina as variaveis de ambiente
   `ADMIN_LOGIN` e `ADMIN_PASSWORD` (senha com 8+ caracteres) antes de subir
   a aplicacao pela primeira vez — o `AdminBootstrap` cria esse usuario
   automaticamente se a tabela `usuario` estiver vazia. Depois disso, o
   proprio admin cria os operadores via `POST /api/usuarios`
   (autenticado, role `ADMIN`).
5. Autenticacao e via JWT (`POST /api/auth/login` com `login`/`senha`
   retorna um token; use `Authorization: Bearer <token>` nas demais
   chamadas autenticadas). Duas variaveis de ambiente obrigatorias:
   - `JWT_SECRET`: string aleatoria com 32+ caracteres (ex.:
     `openssl rand -base64 48`). A aplicacao nao sobe sem isso definido.
   - `FRONTEND_ORIGIN`: origem exata liberada no CORS (padrao
     `http://localhost:4200` em dev; trocar pelo dominio real em producao).

### Frontend

Projeto Angular standalone completo (`angular.json`, `tsconfig*.json`,
`package.json`, `proxy.conf.json`).

```bash
cd frontend
npm install
npm start
```

`npm start` roda `ng serve` com o proxy (`proxy.conf.json`) ja apontando
`/api` para `http://localhost:8080`, entao o backend precisa estar de pe
para o formulario funcionar. Acesse `http://localhost:4200`.

Tres rotas:

- `/` — formulario publico de autocadastro (`POST /api/titulares`).
- `/login` — tela de login (`POST /api/auth/login`), guarda o token JWT em
  `sessionStorage`.
- `/busca` — protegida por `authGuard`; busca um titular por CPF
  (`GET /api/titulares/{cpf}`), anexando o token via `authInterceptor`.
  Em caso de `401` (token expirado/invalido), desloga e volta para `/login`
  automaticamente.

Para testar o fluxo completo: suba o backend com `ADMIN_LOGIN`/
`ADMIN_PASSWORD` definidos, entre em `/login` com essas credenciais, e use
`/busca` para consultar um CPF ja cadastrado pelo formulario publico.

## Hospedagem

Para manter tudo responsivo e acessivel de qualquer dispositivo sem apps
nativos, hospedar como aplicacao web:

- **Banco de dados:** Postgres gerenciado (ex.: Render, Railway, Supabase).
- **Backend:** o mesmo tipo de servico web usado no clamAtiradores-spring.
- **Frontend:** build estatico (`ng build`) servido por um static site host,
  ou empacotado dentro do backend em `src/main/resources/static`.

Antes de publicar de verdade: revisar politica de retencao (quando um
titular deixa de ser beneficiario, os dados devem ser anonimizados ou
excluidos, nao mantidos indefinidamente) e — se o frontend guardar o token
JWT em `localStorage` — avaliar `sessionStorage` ou um cookie `HttpOnly`
para reduzir exposicao a XSS.

## O que este projeto deliberadamente NAO faz

Nao ha (e nao deve ser adicionado) nenhum fluxo de extracao automatica de
dados a partir de fotos de documentos de identidade de terceiros (RG, CNH,
titulo de eleitor). Esse tipo de coleta em massa, sem o consentimento
individual documentado de cada pessoa, e o principal risco de LGPD e de
fraude de identidade que este desenho evita.
