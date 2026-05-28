import logging

import redis

from app.config import settings

logger = logging.getLogger(__name__)

_KEY_PREFIX = "area_summary"


class RedisService:
    def __init__(self) -> None:
        self._client = redis.Redis(
            host=settings.redis_host,
            port=settings.redis_port,
            decode_responses=True,
        )
        self._client.ping()

    def _key(self, area: str) -> str:
        return f"{_KEY_PREFIX}:{area}"

    def get_summary(self, area: str) -> str | None:
        try:
            return self._client.get(self._key(area))
        except redis.RedisError as exc:
            logger.warning("Redis get failed for area '%s': %s", area, exc)
            return None

    def set_summary(self, area: str, summary: str) -> None:
        try:
            self._client.setex(self._key(area), settings.redis_ttl_seconds, summary)
        except redis.RedisError as exc:
            logger.warning("Redis set failed for area '%s': %s", area, exc)


def build_redis_service() -> RedisService | None:
    try:
        return RedisService()
    except Exception as exc:
        logger.warning("RedisService not initialised: %s", exc)
        return None
