#!/usr/bin/env python

import json
import logging
import random

from kafka import KafkaProducer

log = logging.getLogger(__name__)

producer = KafkaProducer(
    bootstrap_servers=['127.0.0.1:9092', '127.0.0.1:9093', '127.0.0.1:9094'],
    value_serializer=lambda m: json.dumps(m).encode('utf-8'))


def on_send_success(record_metadata):
  print(record_metadata.topic)
  print(record_metadata.partition)
  print(record_metadata.offset)


def on_send_error(ex):
  log.error('I am an Error', exc_info=ex)
  # handle exception


for i in range(1000):
  key = str(random.randint(0, 9))
  producer.send('simple-topic', key=key.encode('utf-8'),
                value={'msg': str(i)}).add_callback(
      on_send_success).add_errback(on_send_error)

producer.flush()
