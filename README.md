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
         * [Java Transactional Producer/Consumer (Exactly Once Semantics)](#java-transactional-producerconsumer-exactly-once-semantics)
            * [TX Word Count](#tx-word-count)
      * [Streams API](#streams-api)
         * [KStream vs KTable](#kstream-vs-ktable)
         * [Agregando Información de un Stream](#agregando-informaci\xC3\xB3n-de-un-stream)
         * [Joins](#joins)
      * [Kafka Connect](#kafka-connect)
         * [Plain Text Connector Example](#plain-text-connector-example)
         * [Mongo Connector Example](#mongo-connector-example)

<!-- Added by: ogomez, at: mié 27 ene 2021 22:35:44 CET -->

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
kafka-topics --bootstrap-server kafka1:19092 --create --topic my-topic --partitions 1 --replication-factor 1 --config max.message.bytes=64000 --config flush.messages=1
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

¡El consumidor consume todos los mensajes!.
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
Necesitamos crear un productor para que un operador desde consola introduzca las lecturas de n (15??) medidores de temperatura de una sala.

Cada lectura la recibira un dispositivo distinto simulado a través de un consumidor de consola independentiente, queremos que cada consumidor solo reciba la medición correspondiente a su medidor teniendo en cuenta que es muy importante preservar el orden de las mediciones tomadas.
````

### Java/Python Producer / Consumer


**Nota:** Si eliges Python como lenguaje, necesitarás instalar el módulo kafka

```
pip install kafka-python
```
Observemos la configuración de la clase SimpleProducer. ¿Qué pasa en nuestro cluster si la ejecutamos "directamente"?

```
Usa el comando kafka-topics para ver que ha pasado con nuestro simple-topic
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
Es tiempo ahora de volver a nuestro medidor de temperatura. Esta vez simularemos cada dispositivo en una clase productora equivalente.

De nuevo necesitamos que los eventos de cada medidor sean atendidos por un solo consumidor y en el orden establecido.
````
### Java Transactional Producer/Consumer (Exactly Once Semantics) 

En la carpeta tx del punto 3.1 encontraremos un ejemplo de un productor consumidor transaccional, es decir que asegura una semantica Exactly Once!.

Hay que tener en cuenta que esta semántica solo esta garantizada out of the box en operaciones dentro del cluster es decir cuando consumimos un topic para producir en otro. 

De ese modo debemos asegurar la idempotencia de los mensajes que producimos, asegurar la entrega de los mismos (minimo de ACKS de cada réplica), además en la parte consumidora tendremos que asegurar la transacción en la producción a nuestro topic de salida (recordamos que produciremos en otro topic) poniendo el isolation level en READ_COMMITED, es decir solo consumiremos aquellos mensajes que vengan marcados como comiteados por el productor.

Para los casos en los que no escribamos en otro topic tendra que ser la lógica de consumo la que asegure la transaccionalidad en su parte.

**¡De este modo como veremos más adelante las APIs construidas por encima de producer consumer asegura esta semántica by the face!.**

#### TX Word Count

En este ejemplo TxMessageProducer produce dos textos, marcados con un id, es decir nos aseguramos la idempotencia en la producción (ver comentarios en las clases de configuración).

Más tarde TxWordCount consumirá los textos separando y contando las palabras de cada mensaje para producir idempotentemente la cuenta de cada palabra en un topic de salida. Para ello:

* Iniciaremos un productor en modo transaccional antes de empezar a consumir.
* En cada poll iniciaremos una nueva transacción
* Ejecutaremos nuestra lógica de consumo para luego mandar todos los commit de los offset consumidos en este poll con el consumer group asegurandonos de ese modo que tanto productor como consumidor han marcado el mensaje como procesado.

## Streams API

Toda la documentación oficial del API de Streams [aquí](https://kafka.apache.org/documentation/streams/)

Especial atención a los [conceptos basicos](https://kafka.apache.org/27/documentation/streams/core-concepts)

También digno de mencionar como todas las semanticas de entrega estan sooportadas por infraestructura mediante la propiedad de configuración **processing.guarantee**


### KStream vs KTable

Para el primer ejemplo buscaremos los básicos de Stream KTable y KStream, la mejor explicación grafica la podemos encontrar [aquí](https://www.confluent.io/blog/kafka-streams-tables-part-1-event-streaming/)

Podemos ver el stream como una foto en un momento dado del estado de un topic/partición, esta foto se está siendo constantemente actualizada (si procesamos en tiempo real) o bien en micro batches en una ventana de tiempo, como vemos en los gráficos de la docu oficial cada momento del stream representa un mensaje en la historia del topic. Por contra en la tabla podremos de un solo momento (en un solo offset) obtener la informacion agregada del estado de nuestro topic.

En nuestro primer ejemplo **KafkaStreamsWordCount** vemos como el simple concepto de Ktable simplifica y hace mucho más eficiente nuestro código.

### Agregando Información de un Stream

Si nos fijamos en nuestro ejemplo **KafkaStreams** mediante un sencillo método (función lambda) aggregate podemos ir agregando, valga la redundancia, información que va llegando a nuestro topic.

Podemos simplificar más aún estos calculus gracias a la abstracción **KGroupedStream** (ejemplos **KafkaStreamGroupedKey**, **KafkaStreamsAggregate**) que conseguiremos applicando **groupBy* a nuestro stream, sobre la que podemos aplicar funciones reduce, aggregate, flatmap, etc.

¿Qué diferencia vemos entre nuestros dos ejemplos?

<details>
  <summary><b>Pista</b></summary>

¿Alguna diferencia en como suma?, ¿Qué pasa si dejamos algún tiempo sin consumir?
</details>

<details>
  <summary><b>Solución</b></summary>

Efectivamente el windowedBy descartará por defecto todos los mensajes que salgan de nuestra ventana de tiempo. Por tanto vemos como en el ejemplo de aggregate sumara todas las entradas de las keys producidas dentro de la ventana. Mientras que en la GroupedKey sumara todo lo que entre en el topic.

Más info sobre el "windowing" [aquí](https://kafka.apache.org/27/documentation/streams/core-concepts#streams_concepts_windowing)

</details>

### Joins

También podemos "cruzar los rayos" para ello usaremos la sintaxis de join de las que nos provee el DSL de Streams. Esto nos permitirá agregar información de dos o más topics en cualquier abstracción del [dsl](https://kafka.apache.org/27/documentation/streams/developer-guide/dsl-api.html#id11) de streams.

Información detalla y amigable de todas las posibilidades de Join [aquí](https://www.confluent.io/blog/crossing-streams-joins-apache-kafka/)

Otra cosa a tener en cuenta es que alguna de las operaciones sobre los streams son stateful, esto quiere decir que los procesadores guardaran información en un storage intermedio (normalmente disco local) del estado de las task ejecutadas, de modo que puedan recuperar y proseguir las operaciones donde las dejaron en caso de tener que reiniciarse.

Además esto nos provee de la interesante posibilidad de hacer queries interactivas sobre un stream, funcionalidad sobre la que se construye KSQL.


En nuestros ejemplos de **Movies** utilizaremos joins de streams sirviéndonos tanto de KTables  como de [GlobalKTables](https://kafka.apache.org/27/documentation/streams/developer-guide/dsl-api.html#streams_concepts_globalktable).

Utilizaremos  estos ejemplos para ver como de una manera sencilla podemos implementar nuestros propios [serializadores y serdes](https://kafka.apache.org/10/documentation/streams/developer-guide/datatypes), que no es más que la abstracción que agrupa en una sola clase el serializador y deserializdor. Para ello solo tendremos que exteneder e implementar algunos métodos, para dar la logica de mapeo desde el tipo de entrada a nuestro tipo de salida. Puedes ver un ejemplo en el paquete **movies.serializers**, y un ejemplo generico de serializacion POJO <-> JSON en el paquete **streamutils**

## Kafka Connect

Connect es un herramienta que nos permite ingestar desde y hacia sistemas de persistencia externos (incluidos topics de kafka) usando workers (maquinas tanto en modo stand alone como distribuido) donde tendremos instalado el core de Connect (normalmente una instalación común de kafka nos valdría) usando para ello una serie de plugins (connectors).

Como cualquier otra API construida "on top" of producer/consumer utiliza la propia infraestrucutura de Kafka para asegurarnos la persistencia, semánticas de entrega (es capaz de asegurar semanticas exactly once, dependiendo del conector).

Solo necesitaremos arrancarlo pasandole un configuración, podemos ver un ejemplo tanto en la config de nuestro contenedor **connect** como en el fichero de ejemplo dentro de la carpeta **5.KafkaConnect/plaintext**.

Cosas a tener en cuenta en esta configuración:

* Los Serializadores son los usados por defecto en caso de no especificar ninguno en el connector
* Configuramos varios topics internos que connect usara para mantener el estado de sus tareas
* Configuramos la ruta donde connect en **arranque**, es decir no admite cambios en caliente leera los plugins disponibles para nuestro worker.
* Group Id que nos permite federar workers

Mas info sobre como levantar connect e instalar plugin [aquí](https://docs.confluent.io/platform/current/connect/userguide.html)

Además connect nos provee de un [API Rest](https://docs.confluent.io/platform/current/connect/references/restapi.html) para poder interactuar de manera amigable.

Además existe un [hub](https://www.confluent.io/hub/) donde podremos buscar y descagar los connectors oficiales y no oficiales que necesitemos.

### Plain Text Connector Example

En la carpeta **5.KafkaConnect/plaintext** podemos ver un ejemplo de conector que procesa un archivo de texto plano volcándolo en un topic.

Para ello solo tenemos que mediante el api rest de Connect que arrancaremos un plain text connector:

````
curl -d @"connect-file-source.json" -H "Content-Type: application/json" -X POST http://localhost:8083/connectors
````

como vemos en el json de configuracion que le pasamos:

````
{
  "name": "local-file-source",
  "config": {
    "connector.class": "FileStreamSource",
    "tasks.max": 1,
    "file": "/home/appuser/plain-txt.txt",
    "topic": "file.content"
  }
}
````

Lo único que tenemos que decirle es que arrancaremos un nuevo proceso con:
* La clase FileStreamSource (connector integrado en la instalación por defecto de Kafka)
* El número maximo de task (en este caso task de stream en paralelo) que queremos
* Y la ruta donde estará el fichero a procesar. 
  OJO ruta **dentro del contenedor de connect, por lo que tendremos que usar el comando (dentro de la carpeta del ejemplo):
  ````
  docker cp plain-txt.txt connect:/home/appuser
  ````

¡Después de correr el proceso inspecciona el topic file.content para ver que ha pasado!


###  Mongo Connector Example

Para el ejemplo de Mongo usaremos un replica set de mongo (no he conseguido que funcione con un stand alone) que ya hemos levantado en nuestro compose.

Antes de emepzar el ejemplo necesitaremos poner nuestra BBDD en orden.

Para ello interactuaremos directamente con el contenedor (a falta de un cliente de mongo, si lo tenéis sentíos libres de usarlo)

````
docker-compose -f zk-simple-kafka-multiple.yml exec mongo1 /usr/bin/mongo
````
Iniciamos el replica set

````
rs.initiate({_id : 'rs0',
            members: [
                      { _id : 0, host : "mongo1:27017" },
                      { _id : 1, host : "mongo2:27017" },
                      { _id : 2, host : "mongo3:27017" }
            ]} )
````

y creamos nuestra coleccion:

````
db.createCollection( "movies", {
   validator: { $jsonSchema: {
      bsonType: "object",
      required: [ "id" ],
      properties: {
         id: {
            bsonType: "string",
            description: "id"
         },
         title: {
            bsonType : "string",
            description: "title"
         },
         releaseYear: {
            bsonType : "string",
            description: "release year"
         }
      }
   } }
} )
````
Para el ejercicio usaremos el mismo modelo que ya usamos en movies.

Hacemos el post para crear el nuevo conector:

````
curl -d @"connect-mongo-source.json" -H "Content-Type: application/json" -X POST http://localhost:8083/connectors
````

echemosle un ojo a la configuración:

````
{
  "name": "mongo-source",
  "config": {
    "connector.class": "com.mongodb.kafka.connect.MongoSourceConnector",
    "tasks.max": 1,
    "database": "test",
    "collection": "movies",
    "connection.uri": "mongodb://mongo1:27017,mongo2:27017,mongo3:27017",
    "key.converter":"org.apache.kafka.connect.storage.StringConverter",
    "value.converter":"org.apache.kafka.connect.storage.StringConverter",
    "topic.prefix": "",
    "poll.max.batch.size": "1000",
    "poll.await.time.ms": "5000",
    "pipeline":"[]",
    "batch.size": 0,
    "change.stream.full.document": "updateLookup"
  }
}
````

Esta vez le estamos pasando:

* El nombre del conector y el número maximo de task
* Los serializadores (converters) que usaremos en el proceso, sobreescribiendo los que pasamos por defecto en arranque.
* Configuración específica del [connector](https://docs.mongodb.com/kafka-connector/current/kafka-source)
* La clase que usaremos, como podemos ver esta clase no pertenece a los paquetes base de kafka, por hemos tenido que copiar
  el jar que la contiene, que descargamos del hub, en un volumen al que el contendeor de connect tenga acceso, ojo este jar debe estar ahi antes de arrancar. 

Insertaremos algunos documentos y veremos como se comporta....

puedes decirme....

¿En que topic esta escribiendo?

<details>
  <summary><b>Solución</b></summary>

como vemos en la docu este conector creara un nuevo topic test.movies (db . collecion ) al que agregaria un prefijo si asi lo configuramos,

fijate en el describe del topic...

</details>
¿Qué formato tiene la salida? ¿Por qué?
<details>
  <summary><b>Solución</b></summary>

El converter a string elimina todos los metadatos de Mongo (que vienen por cabeceras) por tanto obtendremos un string limpio con el esquema json de nuestra coleccion
</details>

¿Qué pasa si inserto nuevos documentos en la colección?

<details>
  <summary><b>Solución</b></summary>

el proceso esta escuchando a todos los updates de nuestra colección por tanto procesara como mensaje esta/s nueva/s entrada.

</details>
Y, ¿Si paro el conector y lo vuelvo a arrancar?
<details>
  <summary><b>Solución</b></summary>

Si no hemos borrado los topics internos del conector el proceso deberia volver arracarse y volver a empezar el proceso en el punto que lo dejo.

</details>
## KSQL
