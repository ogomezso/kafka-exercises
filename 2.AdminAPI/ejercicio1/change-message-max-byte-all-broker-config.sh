
kafka-configs --bootstrap-server kafka1:19092 --entity-type brokers --entity-default --alter --add-config message.max.bytes=512

