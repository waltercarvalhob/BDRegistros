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
- **Revogacao de consentimento e exclusao.** Alem de revogar o consentimento
  (titular vira `INATIVO`), ha um endpoint dedicado de exclusao
  (`DELETE /api/titulares/{id}`, titular vira `EXCLUIDO` e some das buscas
  por padrao), atendendo ao direito de exclusao (LGPD art. 18).

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

Rotas:

- `/` — formulario publico de autocadastro (`POST /api/titulares`); a
  finalidade do cadastro e um campo livre preenchido por quem coleta os
  dados, nao um texto fixo.
- `/login` — tela de login (`POST /api/auth/login`), guarda o token JWT em
  `sessionStorage`.
- `/titulares` — protegida por `authGuard` (OPERADOR ou ADMIN); busca a
  lista automaticamente ao abrir a tela (sem exigir filtro) e pesquisa por
  CPF/nome/cidade/titulo de eleitor/status (`GET /api/titulares`, paginado
  — `page`/`size`, 20 por pagina por padrao), edita (`PUT
  /api/titulares/{id}`) e exclui (`DELETE /api/titulares/{id}`, soft
  delete para `EXCLUIDO`). Usuarios ADMIN tambem veem um botao "Importar
  arquivo" nessa tela, que reaproveita o mesmo fluxo de importacao com
  pre-visualizacao da tela Backup (`/api/backup/titulares/import`). Anexa
  o token via `authInterceptor`; em caso de `401` (token expirado/
  invalido), desloga e volta para `/login` automaticamente.
- `/usuarios` — protegida por `authGuard` + `adminGuard` (somente ADMIN);
  cadastra, edita, desativa e filtra usuarios internos
  (`GET/POST/PUT/DELETE /api/usuarios`).
- `/backup` — protegida por `authGuard` + `adminGuard` (somente ADMIN);
  exporta os titulares em CSV/Excel e restaura a partir de um arquivo CSV,
  Excel (`.xlsx`) ou Word (`.docx`) no mesmo layout (`/api/backup/titulares
  /export` e `/import`). No `.docx` o layout e uma tabela unica, com os
  nomes das colunas (`nomeCompleto`, `cpf`, `logradouro`, etc., os mesmos
  do CSV/Excel exportado) na primeira linha e um titular por linha
  seguinte; o formato e detectado pela extensao do arquivo enviado. A
  importacao tem uma etapa de pre-visualizacao: `/import?dryRun=true` roda
  a mesma validacao/upsert de cada linha, mas a transacao de cada linha e
  desfeita no final (nada e gravado); so ao confirmar a tela reenvia o
  mesmo arquivo com `dryRun=false` para gravar de fato.

Para testar o fluxo completo: suba o backend com `ADMIN_LOGIN`/
`ADMIN_PASSWORD` definidos, entre em `/login` com essas credenciais, e use
`/titulares` para pesquisar um titular ja cadastrado pelo formulario
publico.

## Hospedagem (Render)

O projeto e hospedado no Render como dois servicos separados, descritos em
`render.yaml` na raiz (usado como referencia/Blueprint; os nomes reais dos
servicos ja em producao podem ter um sufixo aleatorio que o Render adiciona
quando o nome pedido ja esta em uso por outra conta):

- **Backend** (real: `registro-backend-s5ab`,
  https://registro-backend-s5ab.onrender.com): servico Web com
  `runtime: docker`, construido a partir de `backend/Dockerfile`
  (multi-stage: build com Maven + imagem final so com o JRE). Expoe
  `GET /health` sem autenticacao, usado como `healthCheckPath` pelo Render.
- **Frontend** (real: `bdregistros`, https://bdregistros.onrender.com):
  Static Site (`npm install && npm run build`, publica
  `dist/bdregistros/browser`). O `render.yaml` configura dois rewrites:
  `/api/*` para a URL publica do backend, e `/*` para `/index.html`
  (necessario para as rotas client-side do Angular Router funcionarem em
  recarregamento direto). Na pratica o rewrite de `/api/*` nao faz proxy
  reverso completo (POST retornava 200 vazio) — o frontend chama o backend
  direto por URL absoluta (`frontend/src/environments/environment.prod.ts`),
  entao esse rewrite e o `frontend/src/_redirects` ficaram vestigiais.
- **Banco de dados:** Postgres gerenciado no Render.

Variaveis de ambiente do servico de backend (alem de `JWT_SECRET` e
`FRONTEND_ORIGIN` ja documentados acima):

- `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`: dados do
  Postgres gerenciado do Render (a URL do banco deixou de ser fixa em
  `localhost`; sem essas variaveis o deploy nao consegue conectar).
- `ADMIN_LOGIN`, `ADMIN_PASSWORD`: bootstrap do primeiro administrador
  (so tem efeito se a tabela `usuario` estiver vazia).
- `FRONTEND_ORIGIN`: URL publica do servico de frontend (Static Site),
  para o CORS aceitar as chamadas de `/api/**`. Precisa bater exatamente
  com a origem do frontend (`https://bdregistros.onrender.com`, sem barra
  no final) — o `SecurityConfig` so libera essa unica origem, sem
  wildcard. Se estiver ausente ou divergente, toda chamada do frontend
  falha com `403 Invalid CORS request` (visivel no preflight `OPTIONS`).

Se os servicos ja existirem no Render conectados a outro repositorio,
repita o repositorio conectado (Settings > Build & Deploy) para
`waltercarvalhob/BDRegistros` em ambos, e confira se o "Publish Directory"
do Static Site aponta para `dist/bdregistros/browser` (o nome do projeto
Angular aqui e `bdregistros`, definido em `frontend/angular.json`).

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
