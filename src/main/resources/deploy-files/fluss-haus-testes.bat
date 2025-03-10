@ECHO OFF
:: Definindo parametros
SET branchBE=master
SET branchFE=master
SET porta=8081
SET base=teste
SET environment=live

SET webservices_url=http://172.25.14.254:28080/
SET api_url=http://172.25.14.254:%porta%
SET impressao=true

SET java_home_old=%JAVA_HOME%
SET java_home_temp="C:\Program Files\Java\jdk-17"


:: Garantindo que estamos no diretorio correto
CD "C:\PDV - Testes"


:: Mensagem Inicial
ECHO [SCRIPT] Iniciando atualizacao do PDV


:: Definindo JAVA_HOME para 17
ECHO: & ECHO [SCRIPT] Definindo JAVA_HOME para 17
SETX /m JAVA_HOME %java_home_temp%
SET JAVA_HOME=%java_home_temp%
ECHO [SCRIPT] JAVA_HOME definido


:: Removendo pasta para repos se existente
IF EXIST repos ECHO: & ECHO [SCRIPT] Removendo pasta temporaria & RMDIR /s /q repos


:: Criar pasta para repos
ECHO: & ECHO [SCRIPT] Criando pasta temporaria para repositorios de codigo
MKDIR repos
ECHO [SCRIPT] Pasta temporaria criada


:: Parando API
ECHO: & ECHO [SCRIPT] Parando API
CALL API\pdv-service.exe stop
ECHO [SCRIPT] API parada


:: Configuracao git
ECHO: & ECHO [SCRIPT] Executando configuracao do git
git config --global http.postBuffer 5242880000
git config --global http.sslVerify "false"
ECHO [SCRIPT] Comando executado com sucesso


:: Clonando repo backend
ECHO: & ECHO [SCRIPT] Iniciando processo para atualizar a API
CD repos
git clone https://github.com/cadumancini/datasig-pdv-api.git
CD datasig-pdv-api
ECHO: & ECHO [SCRIPT] Fazendo checkout em %branchBE%
git checkout %branchBE%


:: Definindo variaveis para URL dos WebServices e porta para rodar a API
ECHO: & ECHO [SCRIPT] Definindo URL dos WebServices e porta para rodar a API
COPY src\main\resources\application.properties.example src\main\resources\application.properties
(
  ECHO:
  ECHO server.port=%porta%
  ECHO webservicesUrl=%webservices_url%
  ECHO environment=%environment%
) >> src\main\resources\application.properties
ECHO [SCRIPT] Valores definidos


:: Fazendo build do projeto da API
ECHO: & ECHO [SCRIPT] Construindo pacote da API
CALL .\gradlew clean build
ECHO: & ECHO [SCRIPT] Pacote da API construido com sucesso

:: Definindo JAVA_HOME para versao anterior
ECHO: & ECHO [SCRIPT] Restaurando JAVA_HOME anterior
SETX /m JAVA_HOME %java_home_old%
SET JAVA_HOME=%java_home_old%
ECHO [SCRIPT] JAVA_HOME restaurado


:: Substituindo arquivo .jar
ECHO: & ECHO [SCRIPT] Copiando novo arquivo do app da API
CD ..\..
COPY repos\datasig-pdv-api\build\libs\pdv-api.jar API\pdv-api.jar
ECHO [SCRIPT] Arquivo copiado com sucesso


:: Iniciando API
ECHO: & ECHO [SCRIPT] Iniciando API
CALL API\pdv-service.exe start
ECHO [SCRIPT] API iniciada


:: Clonando repo frontend
ECHO: & ECHO [SCRIPT] Iniciando processo para atualizar o FrontEnd
CD repos
git clone https://github.com/cadumancini/datasig-pdv-frontend.git
CD datasig-pdv-frontend
ECHO: & ECHO [SCRIPT] Fazendo checkout em %branchFE%
git checkout %branchFE%


:: Definindo variavel para URL da API
ECHO: & ECHO [SCRIPT] Definindo URL para consumo de API
(
  ECHO VUE_APP_API_URL=%api_url%
  ECHO VUE_APP_BASE=%base%
  ECHO VUE_APP_IMPRESSAO=%impressao%
) >> .env
ECHO [SCRIPT] Valor definido


:: Assegurando vue-cli-service esta instalado
ECHO: & ECHO [SCRIPT] Instalando pacote vue-cli-service
CALL npm install -g vue-cli-service
ECHO [SCRIPT] Pacote instalado com sucesso


:: Rodando npm install
ECHO: & ECHO [SCRIPT] Instalando pacotes de dependencia para FrontEnd
CALL npm install
ECHO [SCRIPT] Dependencias instaladas com sucesso


:: Rodando audix fix
ECHO: & ECHO [SCRIPT] Rodando auditoria nas dependencias para remover vulnerabilidades
CALL npm audit fix
ECHO [SCRIPT] Auditoria executada com sucesso


:: Criando pacote de distribuicao
ECHO: & ECHO [SCRIPT] Construindo pacote do FrontEnd
CALL npm run build
ECHO [SCRIPT] Pacote do FrontEnd construido com sucesso


:: Substituindo pacote do FrontEnd
ECHO: & ECHO [SCRIPT] Copiando novos arquivos do FrontEnd
CD ..\..
RMDIR /s /q frontend
ROBOCOPY repos\datasig-pdv-frontend\dist frontend /E
ECHO [SCRIPT] Arquivos copiados com sucesso


:: Removendo pasta para repos
ECHO: & ECHO [SCRIPT] Removendo pasta temporaria
RMDIR /s /q repos
ECHO [SCRIPT] Pasta temporaria removida


:: Finalizando
ECHO: & ECHO [SCRIPT] Atualizacao concluida com sucesso!
PAUSE
