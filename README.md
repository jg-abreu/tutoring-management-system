# Sistema de Gestão de Monitoria Acadêmica

Sistema backend para gerenciar grupos de estudo e sessões de monitoria acadêmica, com controle de vínculos de monitoria aprovados por administrador, sessões com vagas limitadas e lista de espera, e cascata automática de cancelamento quando um vínculo é revogado.

Projeto pessoal de portfólio, construído com foco em modelagem de domínio real, regras de negócio com máquinas de estado e autorização baseada em contexto (não em papéis fixos de usuário).

## Stack

- Java
- Spring Boot
- Hibernate / JPA
- PostgreSQL
- REST API

## O problema

Grupos de estudo e monitorias acadêmicas costumam ser organizados de forma informal (planilhas, grupos de WhatsApp), sem controle real de vagas, aprovação de quem pode dar monitoria em qual matéria, ou histórico de quem participou do quê. Este sistema formaliza esse fluxo:

- Qualquer usuário pode propor uma matéria nova, sujeita à aprovação de um administrador
- Usuários se tornam monitores de uma matéria específica através de um vínculo de monitoria (aprovado por admin)
- Monitores criam sessões com vagas limitadas; estudantes se matriculam, com lista de espera opcional e promoção automática quando uma vaga é liberada
- Papéis são dinâmicos: o mesmo usuário pode ser monitor em uma matéria e estudante em outra

## Decisões de design

- **Papéis dinâmicos**: não existe um campo fixo de "role" no usuário — o papel de monitor é inferido pela existência de um vínculo de monitoria aprovado para aquela matéria específica
- **Sessões referenciam o vínculo de monitoria, não o usuário diretamente**, o que torna a cascata de revogação estruturalmente simples: revogar um vínculo cancela automaticamente todas as sessões futuras associadas a ele
- **Nenhuma exclusão física de dados** — todas as ações destrutivas (desativar usuário, revogar vínculo, cancelar sessão) são reversíveis por natureza, preservando histórico
- **Chaves primárias em UUID** e **status representados como ENUM nativo do PostgreSQL**, priorizando robustez sobre simplicidade

## Diagramas de domínio

O domínio foi modelado por completo antes de qualquer linha de código. Diagramas disponíveis em [`docs/diagrams`](./docs/diagrams):

- **Diagrama ER** — entidades do banco de dados e seus relacionamentos
- **Diagrama de classes** — classes da aplicação (entidades + services) com atributos e métodos
- **Fluxos de negócio** — passo a passo dos 8 principais fluxos do sistema (aprovação de matéria, aprovação de vínculo, criação de sessão, matrícula, cancelamentos, revogação, desativação de usuário)

## Status atual

🚧 Em desenvolvimento.

- [x] Modelagem de domínio completa (diagramas ER, classes, fluxos de negócio)
- [x] Schema de banco de dados desenhado e documentado
- [x] Schema implementado em PostgreSQL (Neon, banco na nuvem)
- [x] Projeto Spring Boot criado e configurado (Maven, Java 21, Web/JPA/PostgreSQL Driver/Validation/DevTools)
- [x] Conexão da aplicação com o banco funcionando (aplicação sobe e conecta com sucesso)
- [ ] Entidades JPA mapeando o schema
- [ ] Repositories, services e regras de negócio
- [ ] Endpoints REST

## Stack de infraestrutura

- **Banco de dados:** PostgreSQL hospedado no [Neon](https://neon.com) (plano gratuito), escolhido para não depender de uma máquina específica
- **Configuração de conexão:** via variáveis de ambiente (host, nome do banco, usuário, senha), nunca commitadas no repositório
