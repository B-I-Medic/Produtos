# CD de produção

## Fluxo de publicação

1. O merge em `main` executa o workflow `deploy-hml`.
2. O workflow testa o projeto com Java 21, aplica as migrations de homologação e publica um artefato único com os JARs e `release-manifest.json`.
3. Depois da validação de HML, crie uma tag no formato `vX.Y.Z` a partir do commit publicado.
4. `deploy-prod` valida a tag, localiza a execução HML bem-sucedida do mesmo SHA e baixa o mesmo artefato.
5. O workflow valida o artefato, executa Flyway no database de produção e publica os JARs nos serviços NSSM.
6. O deploy valida Web, ETL e o smoke test autenticado da consulta Anvisa.

Uma tag sem execução HML bem-sucedida para o mesmo SHA é rejeitada. A produção não recompila a tag.

## Environment `producao`

Configure os seguintes secrets:

- `FLYWAY_URL`, `FLYWAY_USER`, `FLYWAY_PASSWORD`: database de produção, separado do database de homologação.
- `SSH_PRIVATE_KEY`, `SSH_KNOWN_HOSTS`, `SSH_USER`, `SSH_HOST`: acesso ao Windows cloud.
- `SMOKE_USERNAME`, `SMOKE_PASSWORD`: usuário técnico somente leitura.

Configure as seguintes variables:

- `DEPLOY_ROOT`: normalmente `C:/apps/produto/prod`.
- `WEB_SERVER_JAR` e `ETL_SERVER_JAR`: caminhos absolutos dos JARs ativos.
- `WEB_SERVICE_NAME` e `ETL_SERVICE_NAME`: nomes dos serviços NSSM.
- `SMOKE_BASE_URL`: URL da aplicação Web de produção.

O Environment `producao` já é referenciado pelo workflow para separar configuração e secrets. Enquanto o plano GitHub não disponibilizar aprovadores obrigatórios, a criação da tag é a autorização operacional do deploy. Quando o recurso estiver disponível, configure required reviewers, restrição às tags `v*.*.*` e bloqueio de autoaprovação nesse Environment.

## Portas e profiles no mesmo host

| Serviço        |   HML | Produção |
| -------------- | ----: | -------: |
| Web            |  9091 |     9092 |
| ETL            |  9093 |     9094 |
| Profile Spring | `hml` |   `prod` |

Os serviços de produção devem possuir `SPRING_PROFILES_ACTIVE=prod`. O ETL de produção deve usar `ETL_SERVER_PORT=9094` ou o default definido no profile `prod`.

O Web de produção deve apontar `ANVISA_ETL_BASE_URL` para o ETL de produção e ambos os serviços devem compartilhar a `ANVISA_ETL_API_KEY` de produção. As URLs `PRODUTO_WEB_PROD_URL` e `PRODUTO_ETL_PROD_URL` devem apontar para o database produtivo.

## Backup, migration e rollback

Antes de criar a tag, confirme o backup/snapshot operacional do database de produção.

O workflow não faz rollback automático do banco. A migration V19 mantém `modelo_chave` para preservar a compatibilidade com o JAR anterior; a remoção física da coluna e do índice deve ser feita em uma migration posterior, depois da janela de rollback da aplicação.

Se o health check remoto falhar, o script restaura os JARs anteriores. Se o smoke test falhar depois do deploy, o workflow chama o mesmo script em modo `-Rollback`.
