# Projeto de Sistemas Distribuídos 2015-2016 #

Grupo de SD 61 - Campus Alameda

Duarte Barreira 64752 duarte.barreira@tecnico.ulisboa.pt

[Leo Valente](https://github.com/LeoVal) 67030 leo.valente@tecnico.ulisboa.pt

[João Feneja](https://github.com/joaofeneja) 73978 joao.feneja@tecnico.ulisboa.pt


Repositório:
[tecnico-distsys/A_61-project](https://github.com/tecnico-distsys/A_61-project/)

-------------------------------------------------------------------------------

## Instruções de instalação 


### Ambiente

[0] Iniciar sistema operativo (LINUX)

[1] Iniciar servidores de apoio

JUDDI:
```
juddi-startup
```


[2] Obter código fonte do projeto (versão entregue)

```
git clone https://github.com/tecnico-distsys/A_61-project.git
```


[3] Instalar módulos de bibliotecas auxiliares

WARNING: 
- É imperativo instalar a biblioteca uddi-naming antes de qualquer outra.
- É necessário ter o ca-ws-cli instalado antes de instalar o transporter-ws-cli porque este depende do primeiro.
- É necessário ter 2 transporter-ws a correr para que o transporter-ws-cli possa ser instalado por causa dos testes de integração!
Consultar a secção Transporter abaixo e Certificate Authority para ver como lançar os serviços.

```
cd uddi-naming
mvn clean install
cd ../ca-ws-cli
mvn clean install
cd ../ws-handlers
mvn clean install
* Lançar 2 Transporters *
cd ../transporter-ws-cli
mvn clean install
```


-------------------------------------------------------------------------------

### Serviço Certificate Authority

[1] Construir e executar **servidor**

```
cd ca-ws
mvn clean compile
mvn exec:java
```

-------------------------------------------------------------------------------

### Serviço TRANSPORTER

[1] Construir e executar **servidor**

```
cd transporter-ws
mvn clean compile
mvn exec:java
```

Para lançar mais servidores sem ter que se alterar o pom.xml basta definir o valor da variável ws.i para um número maior do que 1:

```
mvn exec:java -Dws.i=2
```

[2] Construir **cliente** e executar testes

```
cd transporter-ws-cli
mvn clean compile
mvn test
```


-------------------------------------------------------------------------------

### Serviço BROKER

[1] Construir e executar **servidor**

```
cd broker-ws
mvn clean compile
mvn exec:java
```


[2] Construir **cliente** e executar testes

```
cd broker-ws-cli
mvn clean compile
mvn test
```

-------------------------------------------------------------------------------
**FIM**
