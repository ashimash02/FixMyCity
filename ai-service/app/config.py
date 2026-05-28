from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    app_name: str = "civic-ai-service"
    app_version: str = "0.1.0"
    debug: bool = False

    # Kafka — to be used by kafka_consumer.py
    kafka_bootstrap_servers: str = "localhost:9092"
    kafka_topic_issues: str = "issue-summaries"
    kafka_consumer_group: str = "ai-service-group"

    # Redis — to be used by redis_service.py
    redis_host: str = "localhost"
    redis_port: int = 6379
    redis_ttl_seconds: int = 3600

    # Gemini
    gemini_api_key: str = ""
    gemini_model: str = "gemini-2.0-flash-lite"

    model_config = SettingsConfigDict(env_file=".env", extra="ignore")


settings = Settings()
