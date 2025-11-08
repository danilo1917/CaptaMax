# captaMax - INF311 Projeto Prático


## Descrição

Este repositório contém o **captaMax**, um aplicativo Android desenvolvido como projeto prático para a disciplina **INF311 - Programação para Dispositivos Móveis**. O captaMax foi concebido a partir de ideias e requisitos fornecidos pela **Rubeus**, uma empresa de tecnologia que colaborou com o desenvolvimento de propostas reais para o projeto.

![CaptaMax](./images/Banner.png)

## Objetivo

O principal objetivo deste projeto é aplicar os conceitos estudados na disciplina, desenvolvendo um **aplicativo de monitoramento comercial** que se conecta a serviços fornecidos pela Rubeus. O aplicativo auxilia vendedores a gerenciar e acompanhar informações sobre **leads**, funis de conversão e desempenho individual e da equipe, utilizando recursos modernos de autenticação e notificações.

## Tecnologias Utilizadas

- **Linguagem de Programação:** Java
- **Plataforma:** Android
- **IDE:** Android Studio
- **API da Rubeus:** Integração com os serviços fornecidos pela empresa
- **Cloud Firestore:** Banco de dados em nuvem para armazenamento de informações usuários
- **Firebase Authentication:** Gerenciamento seguro de autenticação de usuários
- **Firebase Cloud Messaging (FCM):** Envio de alertas e notificações push

## Arquitetura e Padrões Utilizados

- **Singleton Backend:** Implementação de um singleton para gerenciar o acesso unificado ao Firebase e à API da Rubeus, garantindo reutilização e controle centralizado das chamadas.
- **Data Provider Pattern:** Separação clara entre a camada de dados e a camada de controladores, promovendo modularidade e manutenção facilitada.
- **Fragment-Based Views:** Uso de fragments para compor a interface do usuário, evitando renderizações desnecessárias e melhorando a performance do aplicativo.
- **Data Caching:** Implementação de cache local para evitar requisições repetidas ao backend, reduzindo latência e consumo de rede.

## Estrutura do Repositório

- **/app**  
  Código-fonte completo do captaMax.

## Funcionalidades Principais

- **Adicionar leads**: cadastro de novos leads no sistema.
- **Acompanhar leads**: consulta e atualização de informações de leads.
- **Funil de conversão**: visualização do progresso dos leads em diferentes etapas de conversão.
- **Dashboard individual de desempenho**: painel de métricas para o vendedor acompanhar seu próprio desempenho.
- **Dashboard de desempenho da equipe**: visão consolidada do desempenho geral da equipe de vendas (para o administrador).
- **Cadastro de atividades***: registro de atividades comerciais relacionadas aos leads.
- **Listagem de atividades**: visualização e acompanhamento das atividades cadastradas.
- **Alertas e notificações**: recebimento de notificações push com informações relevantes.

\* A funcionalidade de cadastro de atividades está implementada no app, porém o envio da mensagem para cadastro não é permitido pela API da Rubeus.

![tela1](./images/Screenshot_20250626_153715.png) ![tela2](./images/Screenshot_20250626_153829.png) ![tela3](./images/Screenshot_20250626_153854.png) ![tela4](./images/Screenshot_20250626_153915.png) ![tela5](./images/Screenshot_20250626_153933.png) !![tela6](./images/Screenshot_20250626_154015.png) !

## Diagrama das telas

```mermaid
  flowchart TB
    login(Login)
    toolbar{Toolbar}
    dashboard{{Dashboard}}
    leads{{Leads}}
    newlead{{Novo Lead}}
    notifications{{Notificações}}
    profile{{Perfil}}
    team{{Metricas da equipe}}
    funnel{{Funil de conversão}}
    lead{{Lead}}
    newactivity{{Cadastro de atividade}}
    login --> dashboard
    toolbar --- dashboard
    toolbar --- leads
    toolbar --- newlead
    toolbar --- notifications
    toolbar --- profile
    dashboard --> team
    dashboard --> funnel
    dashboard --> lead
    leads --> lead
    lead --> newactivity
```

## Licença

Este projeto foi desenvolvido para fins de aprendizado e demonstração de boas práticas de Engenharia de Software e de programação para dispositivos movéis, podendo ser utilizado, aprimorado e expandido pela **Rubeus** ou por outras equipes interessadas.
