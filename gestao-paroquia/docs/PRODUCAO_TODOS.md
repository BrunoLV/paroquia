# TODOs para produção

Este documento reúne pendências deliberadamente mantidas durante o desenvolvimento. Elas devem ser revisadas e concluídas antes da primeira implantação produtiva.

## Segurança de criptografia

- [ ] Gerar uma chave criptográfica aleatória forte, com ao menos 32 bytes, para `APP_CRYPTO_KEY`.
- [ ] Armazenar a chave exclusivamente em um gerenciador de segredos aprovado; nunca em código, arquivos versionados, imagens Docker ou massa de seed.
- [ ] Configurar a injeção de `APP_CRYPTO_KEY` no ambiente de produção.
- [ ] Bloquear a inicialização em produção quando `APP_CRYPTO_KEY` estiver ausente, vazia ou usar a chave padrão de desenvolvimento.
- [ ] Registrar procedimento de backup, recuperação e controle de acesso à chave. A perda da chave impede recuperar PII já cifrada.
- [ ] Definir rotação de chaves com versionamento e plano de recifragem gradual dos dados existentes.
- [ ] Migrar a implementação criptográfica de AES-CBC para AES-GCM, com nonce único e autenticação do conteúdo.

## Operação e conformidade

- [ ] Confirmar TLS seguro no gateway/ingress e redirecionamento obrigatório para HTTPS.
- [ ] Validar backups restauráveis, retenção e controle de acesso ao banco de dados.
- [ ] Executar migrações e validar a massa de seed em ambiente de homologação.
- [ ] Realizar teste E2E com os perfis Admin, Pároco, Secretaria, Coordenador e Fiel.
- [ ] Revisar trilhas de auditoria e acessos a PII com o responsável pela LGPD antes do go-live.
