# Setup inicial
Antes de rodar o projeto, crie uma cópia do arquivo application.properties.example
no mesmo diretório, com o nome application.properties, e preencha os valores de conexão
de acordo com a necessidade

# Compilando o Projeto
No terminal, na raiz do reposiório, rodar o seguinte comando:
```
./gradlew build
```
Ou então utilizar a sua IDE de preferência.

# Rodando o Projeto
Utilizar o comando da IDE de sua preferência, ou, no terminal, navegar até o diretório onde
está o .jar (padrão: `build/libs/`), e rodar o comando:
```
java -jar pdv-api.jar
```