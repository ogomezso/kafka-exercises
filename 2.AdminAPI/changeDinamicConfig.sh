kafka-configs --bootstrap-server kafka1:19092 --entity-name 1 brokers --entity-default --alter --add-config message.max.bytes=512

kafka-configs --bootstrap-server kafka1:19092 --entity-name 1 brokers --describe --all

kafka-configs --bootstrap-server kafka1:19092 --entity-type brokers --entity-default --alter --add-config message.max.bytes=512

kafka-configs --bootstrap-server kafka1:19092 --entity-type brokers --describe --all

kafka-configs --bootstrap-server kafka1:19092 --entity-type brokers --entity-default --alter --delete-config message.max.bytes

kafka-configs --bootstrap-server kafka1:19092 --entity-type brokers --describe --all
kafka-configs --bootstrap-server kafka1:19092 --entity-name 1 brokers --describe --all
kafka-configs --bootstrap-server kafka1:19092 --entity-name 2 brokers --describe --all
kafka-configs --bootstrap-server kafka1:19092 --entity-name 3 brokers --describe --all