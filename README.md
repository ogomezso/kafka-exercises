Kafka - de 0 a PRO - Práctica Guiada
====================================

Índice
======

<!--ts-->

* [Prerequisitos](#prerequisitos)
* [Organización del Repositorio](#organizaci\xC3\xB3n-del-repositorio)
* [Arrancando el Clúster](#arrancando-el-cl\xC3\xBAster)
* [Admin API](#admin-api)
    * [Settings Básicos](#settings-b\xC3\xA1sicos)
    * [Creando un Topic](#creando-un-topic)
* [Producer / Consumer API](#producer--consumer-api)
    * [Console Producer](#console-producer)
    * [Java Producer / Consumer](#java-producer--consumer)
    * [Python Producer / Consumer](#python-producer--consumer)
* [Streams API](#streams-api)
    * [Java Streams](#java-streams)
    * [Python Streams](#python-streams)
* [Kafka Connect](#kafka-connect)
* [KSQL](#ksql)

<!-- Added by: ogomez, at: mar 29 dic 2020 20:10:47 CET -->

<!--te-->

## Prerequisitos

 * Docker Instalado: Para facilitar la práctica y el manejo montaremos nuestro propio "cluster" de Kafka en contenedores docker.
    
     [Instala Docker](https://docs.docker.com/get-docker/)
* JDK 11+ Instalado
* Maven Instalado

**Nota:** Para la instalación de SDKs mi recomendación es usar [SDKman](https://sdkman.io/)

## Organización del Repositorio

El repositorio estará organizado en carpetas, por temática (API), dentro de la cual encontraréis una con el ejercicio propuesto a resolver y otra con el ejercicio resuelto.

## Arrancando el Clúster

Abre la carpeta _**Environment**_ y ejecuta:

```
docker-compose -f zk-simple-kafka-multiple.yml up -d
```
## Admin API

En este apartado veremos como setear algunas de las propiedades basicas de Kafka.

Para ver el listado de todas las configuraciones posibles:

[Kafka Broker-Level Config](http://kafka.apache.org/10/documentation.html#brokerconfigs)

[Kafka Topic-Level Configs](http://kafka.apache.org/10/documentation.html#topicconfigs)

### Settings Básicos

Utilizaremos el comando kafka-configs que nos da la instalación de kafka para comprobar el estado de algunos settings básicos de nuestro clúster, para ello deberemos ejecutar
dicho comando dentro de cualquiera de nuestros broker.

Por tanto lo primero que necesitaremos será habilitar una consola interactiva dentro del contenedor de uno de nuestros broker para lo que ejecutamos:

```
docker exec -it kafka-broker-1 /bin/bash
```

una vez dentro ejecutaremos el comando **kafka-configs** para listar la configuración de brokers activa en este momento:

```
kafka-configs --bootstrap-server kafka1:19092 --entity-type brokers --describe --all
```

####Ejercicio 1 - Administración de Configuración básica desde línea de comandos

````
1. Utiliza el comando **kafka-configs** para setear la propiedad _message.max.bytes_ a _512_ en el broker 1

2. Utiliza el comando **kafka-configs** para comprobar el efecto de tu acción.

3. Utiliza el comando **kafka-configs** para setear la propiedad _message.max.bytes_ a _512_ en todos los brokers

4. Revierte la propiedad al valor por defecto para todos los broker.
````

### Creando un Topic

Utilizaremos el comando **kafka-topics** para crear y administrar topics dentro de nuestro cluster:

Para monitorizar lo que está pasando en nuestro cluster, abriremos el log de cada broker en una consola aparte ejecutando:

````
docker logs -f kafka-broker-<id>
````

Dentro del contenedor (recuerda docker exec...) de cual quiera de nuestros broker ejecutaremos:

````
5
````

Vamos a modificar el numero de particiones y replicas de nuestro topic y observemos lo que pasa:

Para el número de particiones:
````
kafka-topics --bootstrap-server kafka1:19092 --alter --topic my-topic --partitions 2
````
El incremento de réplicas más "tricky", necesitaremos reasignar la réplica de cada partición a mano (algo a evitar tanto como sea posible).

Primero necesitamos saber cual es la configuración actual del topic:

```
kafka-topics --bootstrap-server kafka1:19092 --topic my-topic --describe
```

También necesitaremos un fichero JSON que describa esta reasignación, increase-replication-factor.json:

```JSON
{"version":1,
 "partitions":[
    {"topic":"my-topic",
     "partition":0,
     "replicas":[1,3,2]
    },
   {"topic":"my-topic",
     "partition":1,
     "replicas":[2,3,1]
   }
  ]
}
```

Para crear el archivo dentro de nuestro broker podemos usar el comando:

```
cat << EOF > increase-replication-factor.json
```
 Por último ejecutaremos el comando:

```
kafka-reassign-partitions --bootstrap-server kafka1:19092 --reassignment-json-file    increase-replication-factor.json --execute
```

## Producer / Consumer API
### Console Producer
### Java Producer / Consumer
### Python Producer / Consumer
## Streams API
### Java Streams
### Python Streams
## Kafka Connect
## KSQL
