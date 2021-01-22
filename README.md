Indice
======

<!--ts-->
   * [Kafka - de 0 a PRO - Práctica Guiada](#kafka---de-0-a-pro---pr\xC3\xA1ctica-guiada)
      * [Prerrequisitos](#prerrequisitos)
      * [Organización del Repositorio](#organizaci\xC3\xB3n-del-repositorio)
      * [Arrancando el Clúster](#arrancando-el-cl\xC3\xBAster)
      * [Admin API](#admin-api)
         * [Settings Básicos](#settings-b\xC3\xA1sicos)
         * [Ejercicio 1 - Administración de Configuración básica del clúster desde línea de comandos](#ejercicio-1---administraci\xC3\xB3n-de-configuraci\xC3\xB3n-b\xC3\xA1sica-del-cl\xC3\xBAster-desde-l\xC3\xADnea-de-comandos)
         * [Creación y Administración de un Topic](#creaci\xC3\xB3n-y-administraci\xC3\xB3n-de-un-topic)
         * [Ejercicio 2 - Administración de Topics](#ejercicio-2---administraci\xC3\xB3n-de-topics)
      * [Producer / Consumer API](#producer--consumer-api)
         * [Console Producer](#console-producer)
         * [Ejercicio1 - Console Producer / Consumer](#ejercicio1---console-producer--consumer)
         * [Java/Python Producer / Consumer](#javapython-producer--consumer)
         * [Ejercicio2 - Console Producer / Consumer](#ejercicio2---console-producer--consumer)
      * [Streams API](#streams-api)
         * [Stream Basics](#stream-basics)
      * [Kafka Connect](#kafka-connect)
      * [KSQL](#ksql)

<!-- Added by: ogomez, at: vie 22 ene 2021 14:40:55 CET -->

<!--te-->
Kafka - de 0 a PRO - Práctica Guiada
====================================

## Prerrequisitos

* Docker Instalado: Para facilitar la práctica y el manejo montaremos nuestro propio "cluster" de
  Kafka en contenedores docker.

  [Instala Docker](https://docs.docker.com/get-docker/)
* JDK 11+ Instalado
* Maven Instalado

**Nota:** Para la instalación de SDKs mi recomendación es usar [SDKman](https://sdkman.io/)

## Organización del Repositorio

El repositorio estará organizado en carpetas, por temática (API), dentro de la cual encontraréis una
con el ejercicio propuesto a resolver y otra con el ejercicio resuelto.

## Arrancando el Clúster

Abre la carpeta _**1.Environment**_ y ejecuta:

```
docker-compose -f zk-simple-kafka-multiple.yml up -d
```

## Admin API

En este apartado veremos como setear algunas de las propiedades basicas de Kafka.

Para ver el listado de todas las configuraciones posibles:

[Kafka Broker-Level Config](http://kafka.apache.org/10/documentation.html#brokerconfigs)

[Kafka Topic-Level Configs](http://kafka.apache.org/10/documentation.html#topicconfigs)

### Settings Básicos

Utilizaremos el comando kafka-configs que nos da la instalación de kafka para comprobar el estado de
algunos settings básicos de nuestro clúster, para ello deberemos ejecutar dicho comando dentro de
cualquiera de nuestros broker.

Por tanto lo primero que necesitaremos será habilitar una consola interactiva dentro del contenedor
de uno de nuestros broker para lo que ejecutamos:

```
docker exec -it kafka-broker-1 /bin/bash
```

Una vez dentro ejecutaremos el comando **kafka-configs** para listar la configuración de brokers
activa en este momento:

```
kafka-configs --bootstrap-server kafka1:19092 --entity-type brokers --describe --all
```

### Ejercicio 1 - Administración de Configuración básica del clúster desde línea de comandos

````
1. Utiliza el comando **kafka-configs** para setear la propiedad _message.max.bytes_ a _512_ en el broker 1

2. Utiliza el comando **kafka-configs** para comprobar el efecto de tu acción.

3. Utiliza el comando **kafka-configs** para setear la propiedad _message.max.bytes_ a _512_ en todos los brokers

4. Revierte la propiedad al valor por defecto para todos los broker.

5. ¿Qué pasa si usa configuración solo existe en un broker e intentamos borrarla de todos a la vez?, ¿Testealo con los scripts anteriores?
````

### Creación y Administración de un Topic

Utilizaremos el comando **kafka-topics** para crear y administrar topics dentro de nuestro cluster:

Para monitorizar lo que está pasando en nuestro cluster, abriremos el log de cada broker en una
consola aparte ejecutando:

````
docker logs -f kafka-broker-<id>
````

Dentro del contenedor (recuerda docker exec...) de cual quiera de nuestros broker ejecutaremos:

````
kafka-topics --bootstrap-server kafka1:19092 --create --topic my-topic --partitions 1 \
      --replication-factor 1 --config max.message.bytes=64000 --config flush.messages=1
````

Vamos a modificar el numero de particiones y replicas de nuestro topic y observemos lo que pasa:

Para el número de particiones:

````
kafka-topics --bootstrap-server kafka1:19092 --alter --topic my-topic --partitions 2
````

El incremento de réplicas más "tricky", necesitaremos reasignar la réplica de cada partición a
mano (algo a evitar tanto como sea posible).

Primero necesitamos saber cual es la configuración actual del topic:

```
kafka-topics --bootstrap-server kafka1:19092 --topic my-topic --describe
```

También necesitaremos un fichero JSON que describa esta reasignación,
increase-replication-factor.json:

```JSON
{
  "version": 1,
  "partitions": [
    {
      "topic": "my-topic",
      "partition": 0,
      "replicas": [
        1,
        3,
        2
      ]
    },
    {
      "topic": "my-topic",
      "partition": 1,
      "replicas": [
        2,
        3,
        1
      ]
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

### Ejercicio 2 - Administración de Topics

````
1. Crea un topic con 1 particion, factor de replica 1, y que sincronice tras 5 mensajes

2. Cambia el número de particiones a 3 y reasigna la replicación de manera óptima.

3. Cambia la configuración de sincronizacón para que esta se haga tras cada mensaje.

4. Experimenta matando y levantando brokers, ¿Crees que tu asignación del factor de replica fue adecuada?
````

## Producer / Consumer API

### Console Producer

Primero crea un topic **console-example** con 3 particiones y factor de réplica 3.

Produciremos varios mensajes en el topic mediante el comando kafka-console-producer y observaremos el comportamiento:

El mensaje a producir será uno simple que solo conntedra un id como body:

```JSON
1,{"id": "1"}
```

```
kafka-console-producer --bootstrap-server kafka1:19092 --topic console-example --property "parse.key=true" --property "key.separator=,"
```

Ahora crearemos un consumidor de consola:

```
kafka-console-consumer --bootstrap-server kafka1:19092 --topic console-example --property print.key=true --from-beginning
```
¿Qué pasa cuando este arranca?:

<details>
  <summary><b>Solución</b></summary>

¡El consumidorR consume todos los mensajes!.
</details>

¿Que pasara si añadimos otro consumidor?

<details>
  <summary><b>Solución</b></summary>

¡Tenemos dos consumidores consumiendo exactamente los mismos mensajes!.
</details>



Ahora introduciremos dos consumidores formando un grupo de consumo:

```
kafka-console-consumer --bootstrap-server kafka1:19092 --topic console-example --property print.key=true --from-beginning  --consumer-property group.id=console-group-1
```

Observad el rebalanceo y particionado que se produce mediante la partition key elegida. ¿Qué casos de uso encontramos para esta funcionalidad?.

### Ejercicio1 - Console Producer / Consumer
````
Necesitamos crear un productor para que un operador desde consola introduzca las lecturas de 4 medidores de temperatura de una sala.

Cada lectura la recibira un dispositivo distinto simulado a través de un consumidor de consola independentiente, queremos que cada consumidor solo reciba la medición correspondiente a su medidor teniendo en cuenta que es muy importante preservar el orden de las mediciones tomadas.
````

### Java/Python Producer / Consumer


**Nota:** Si eliges Python como lenguaje, necesitarás instalar el módulo kafka

```
pip install kafka-python
```
Observemos la configuración de la clase SimpleProducer. ¿Qué pasa en nuestro cluster si la ejecutamos "directamente"?

```
Usa el comando kafka-topics para ver que ha pasado con nuestro simple-producer-topic
```

Es momento ahora de crear nuestro primer consumidor. ¿Sabrías decir que pasará cuando arranquemos nuestro SimpleConsumer1?

¿Que pasará si arrancamos SimpleConsumer2 y SimpleConsumer3?

<details>
  <summary><b>Solución</b></summary>

Estarán preparados para consumir, pero no consumirán nada, ya que todos los mensajes han sido consumidos por en el arranque anterior y nuestros nuevos procesos pertenecen al mismo grupo de consumo.
</details>

¿Y si corremos de nuevo SimpleProducer?, ¿Habrá algún cambio en la manera de consumir?

<details>
  <summary><b>Solución</b></summary>

Los nuevos mensajes empezarán a ser consumidos por el proceso perteneciente al grupo que tenga la partición a la que corresponda el mensaje asignada. 
</details>

### Ejercicio2 - Console Producer / Consumer

````
Es tiempo ahora de volver a nuestro medidor de temperatura. Esta vez simularemos cada dispositivo con su Clase productora equivalente.

De nuevo necesitamos que los eventos de cada medidor sean atendidos por un solo consumidor y en el orden establecido.
````

## Streams API

Toda la documentación oficial del API de Streams [aquí](https://kafka.apache.org/documentation/streams/)

### Stream Basics

Para el primer ejemplo buscaremos los básicos de Stream KTable y KStream, la mejor explicación grafica la podemos encontrar [aquí](https://www.confluent.io/blog/kafka-streams-tables-part-1-event-streaming/)


https://www.confluent.io/blog/crossing-streams-joins-apache-kafka/

## Kafka Connect

docker-compose -f zk-simple-kafka-multiple.yml exec mongo1 /usr/bin/mongo

https://docs.confluent.io/home/connect/install.html
https://turkogluc.com/apache-kafka-connect-introduction/


https://www.confluent.io/hub/

https://www.confluent.io/blog/hello-world-kafka-connect-kafka-streams/

## KSQL
