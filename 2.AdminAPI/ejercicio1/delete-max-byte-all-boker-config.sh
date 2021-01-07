
kafka-configs --bootstrap-server kafka1:19092 --entity-type brokers --entity-default --alter --delete-config message.max.bytes