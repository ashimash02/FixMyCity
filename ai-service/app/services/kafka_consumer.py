"""
Kafka consumer — listens for civic issue events published by the Spring Boot backend.

TODO:
- Initialize confluent_kafka Consumer using settings.kafka_bootstrap_servers
- Subscribe to settings.kafka_topic_issues
- On each message: extract area + issues, call gemini_service, store result in redis_service
- Run as a background thread started from main.py lifespan
"""


class IssueKafkaConsumer:
    def __init__(self):
        # TODO: initialize confluent_kafka.Consumer
        pass

    def start(self):
        # TODO: poll loop in background thread
        raise NotImplementedError

    def stop(self):
        # TODO: graceful shutdown
        raise NotImplementedError


kafka_consumer = IssueKafkaConsumer()
