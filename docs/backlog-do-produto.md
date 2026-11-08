# Backlog do Produto

Este documento reúne os épicos, histórias de usuário, casos de uso, estimativas, prioridades (MoSCoW) e critérios de conclusão (Definition of Done) do produto Way-Go.

| Épico | ID | História do Usuário | Caso de Uso | Story Points | Prioridade | Definition of Done |
| :--- | :--- | :--- | :--- | :---: | :---: | :--- |
| **Status das linhas** | US01 | Como sistema, quero consultar o status das linhas na API Trilhos Agora, para ter dados oficiais e atualizados. | Consultar status da linha | 8 | Must | Integração com a API retorna o status de todas as linhas com data e hora; falha ou timeout usa o último status válido e registra log; testes de integração passando. |
| **Status das linhas** | US02 | Como passageiro, quero visualizar o status de todas as linhas, para saber se há atrasos ou paralisações antes de sair. | Visualizar status das linhas | 3 | Must | Tela lista cada linha com nome, cor, status e horário da última atualização; funciona sem login; aviso quando os dados estão desatualizados; testada em Android e iOS. |
| **Status das linhas** | US03 | Como passageiro, quero consultar linhas e estações, para conhecer a rede e planejar meu trajeto. | Consultar linhas e estações | 3 | Must | Lista de linhas com estações em ordem; busca de estação por nome; dados carregados do backend; testes de interface aprovados. |
| **Status das linhas** | US04 | Como sistema, quero atualizar o status das linhas automaticamente a partir da API, para manter a base sempre atual. | Atualizar status da linha | 5 | Must | Rotina de atualização roda em intervalo configurável; cada mudança grava histórico com origem e horário; monitoramento de falhas ativo. |
| **Status das linhas** | US05 | Como administrador, quero atualizar manualmente o status de uma linha, para corrigir ou complementar a API. | Atualizar status da linha | 3 | Should | Apenas perfil administrador acessa a função; alteração aparece para os passageiros em até 1 minuto; histórico registra o administrador responsável. |
| **Rotas** | US06 | Como sistema, quero calcular a melhor rota entre origem e destino, considerando o status das linhas. | Calcular rota | 8 | Must | Endpoint devolve linhas, baldeações e tempo estimado; linhas paralisadas são excluídas; testes unitários cobrem os principais trajetos; tempo de resposta dentro do limite definido. |
| **Rotas** | US07 | Como passageiro, quero marcar origem e destino no mapa, para ver a rota sugerida. | Marcar rota no mapa | 8 | Must | Origem e destino selecionáveis no mapa ou por busca; rota desenhada no mapa com o status de cada linha; tratamento de erro quando não há rota possível. |
| **Rotas** | US08 | Como passageiro, quero salvar uma rota marcada, para reutilizá-la sem refazer a busca. | Salvar rota | 3 | Should | Rota salva vinculada à conta do usuário; exige login; aparece na lista de rotas salvas em qualquer dispositivo. |
| **Rotas** | US09 | Como passageiro, quero editar uma rota salva, para ajustá-la quando meu trajeto mudar. | Editar rota | 2 | Should | Usuário altera nome, origem ou destino; rota é recalculada e salva; exige login. |
| **Rotas** | US10 | Como passageiro, quero excluir uma rota salva, para manter só as que uso. | Excluir rota | 1 | Should | Exclusão pede confirmação; rota some da lista e das notificações; exige login. |
| **Rotas** | US11 | Como passageiro, quero agendar uma rota salva para um dia e horário, para ser avisado do status antes de sair. | Agendar rota | 5 | Could | Usuário define dias e horário; notificação com o status da rota é enviada antes do horário agendado; agendamento pode ser editado e cancelado. |
| **Conta e acesso** | US12 | Como passageiro, quero me cadastrar, para ter uma conta e guardar minhas preferências. | Cadastrar-se | 3 | Must | Cadastro com nome, e-mail e senha; validação de e-mail duplicado e senha fraca; senha armazenada com hash; testes aprovados. |
| **Conta e acesso** | US13 | Como passageiro, quero fazer login, para acessar minhas rotas salvas em qualquer dispositivo. | Realizar login | 3 | Must | Login com e-mail e senha; sessão mantida no dispositivo; recuperação de senha disponível; funções restritas pedem autenticação. |
| **Conta e acesso** | US14 | Como administrador ou moderador, quero acessar o sistema com perfil próprio, para usar funções restritas. | Realizar login | 3 | Should | Perfis administrador e moderador criados; cada perfil acessa só suas funções; tentativas de acesso indevido são bloqueadas e registradas. |
| **Alertas** | US15 | Como passageiro, quero ser notificado quando uma linha das minhas rotas mudar de status, para reagir a tempo. | Receber notificação | 5 | Should | Notificação push enviada quando muda o status de uma linha de rota salva; usuário pode ativar ou desativar alertas; envio testado em Android e iOS. |
| **Alertas** | US16 | Como passageiro, quero receber uma rota alternativa quando minha linha tiver problema, para seguir viagem sem atraso. | Receber rota alternativa | 8 | Could | Ao detectar problema em linha da rota, o sistema recalcula sem essa linha e envia a alternativa ao passageiro; ao tocar, a rota abre no mapa. |
| **Relatos da comunidade** | US17 | Como passageiro, quero relatar um problema em uma estação, para alertar a operação e outros passageiros. | Relatar problema na estação | 3 | Could | Formulário com estação, tipo de problema e descrição; exige login; relato fica com status "pendente" para moderação. |
| **Relatos da comunidade** | US18 | Como moderador, quero monitorar os relatos recebidos, para identificar problemas recorrentes. | Monitorar relatos | 3 | Could | Painel lista relatos com filtros por estação, tipo e data; acesso só para moderador. |
| **Relatos da comunidade** | US19 | Como moderador, quero confirmar ou descartar relatos, para que só informações verificadas sejam divulgadas. | Confirmar relatos | 3 | Could | Moderador confirma ou descarta cada relato; relatos confirmados ficam visíveis aos passageiros; decisão registrada com responsável e horário. |

---

### Resumo por Épico

| Épico | Quantidade de Histórias | Story Points |
| :--- | :---: | :---: |
| Status das linhas | 5 | 22 |
| Rotas | 6 | 27 |
| Conta e acesso | 3 | 9 |
| Alertas | 2 | 13 |
| Relatos da comunidade | 3 | 9 |
| **Total** | **19** | **80** |
