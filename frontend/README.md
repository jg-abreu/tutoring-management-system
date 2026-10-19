# Frontend — Sistema de Monitoria

Interface web da API de monitoria acadêmica. Cobre o fluxo completo do sistema no navegador: sugerir e aprovar matérias, pedir e avaliar vínculos de monitoria, criar e cancelar sessões, matricular alunos e cancelar matrículas (com lista de espera).

## Stack

- **React 19** + **TypeScript**
- **Vite** (servidor de desenvolvimento e build)
- **oxlint** (lint)
- Sem bibliotecas de UI, estado ou HTTP: `fetch` nativo, Context API, CSS puro com variáveis e modo escuro automático

## Como rodar

Pré-requisitos: Node.js 20+ e a API rodando em `http://localhost:8080` (ver o README da raiz).

```bash
npm install
npm run dev
```

Abra `http://localhost:5173`. A API já libera essa origem via CORS (`CorsConfig`).

Para apontar para outra API, crie um arquivo `.env.local` com:

```
VITE_API_URL=http://outro-host:8080
```

Outros comandos:

| Comando | O que faz |
|---|---|
| `npm run build` | checagem de tipos + build de produção em `dist/` |
| `npm run lint` | lint com oxlint |
| `npm run preview` | serve o build de produção localmente |

## Estrutura

```
src/
├── api/          cliente HTTP central e uma função por endpoint da API
├── components/   componentes reutilizáveis (badge de status, mensagens, seletor de usuário)
├── config/       dados de teste provisórios (usuários e registros do banco)
├── context/      estado compartilhado (usuário atual, registros conhecidos)
├── features/     uma pasta por tela: subjects, bonds, sessions
├── hooks/        useLocalStorage, useApiAction
└── lib/          formatação de datas e fusos
```

## Limitações conhecidas (planejadas para a v2.0)

- **Sem login:** o seletor "Agindo como" escolhe o usuário que executa cada ação; a lista de usuários fica fixa em `src/config/testData.ts`, apontando para registros do banco de desenvolvimento.
- **A API ainda não tem endpoints `GET`:** as listas mostram só o que foi criado ou alterado pela própria tela (guardado no `localStorage`). Mudanças feitas pelo servidor em outros registros — como a promoção automática da lista de espera ou o cancelamento em cascata — não aparecem até existir uma forma de consultá-las.
- **Sem papéis de usuário:** os botões de aprovar/rejeitar aparecem para qualquer usuário; quem decide é a API.
