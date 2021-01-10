#!/usr/bin/env python

import json

from kafka import KafkaConsumer

consumer = KafkaConsumer('simple-producer-topic',
                         group_id='py-group',
                         bootstrap_servers=['127.0.0.1:9092', '127.0.0.1:9093',
                                            '127.0.0.1:9094'],
                         auto_offset_reset='earliest')

consumer.subscribe(['simple-producer-topic'])

for message in consumer:
  # message value and key are raw bytes -- decode if necessary!
  # e.g., for unicode: `message.value.decode('utf-8')`
  print("%s:%d:%d: key=%s value=%s" % (message.topic, message.partition,
                                       message.offset,
                                       message.key.decode('utf-8'),
                                       message.value.decode('utf8')))
