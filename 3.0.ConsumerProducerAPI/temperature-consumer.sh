kafka-console-consumer --bootstrap-server kafka1:19092 --topic console-exam --property print.key=true --from-beginning  --consumer-property group.id=meter-consumer