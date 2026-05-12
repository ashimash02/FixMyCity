"""
Redis service — caches AI-generated area summaries.

TODO:
- Initialize redis.Redis client using settings.redis_host / redis_port
- Implement get_summary(area: str) -> str | None
- Implement set_summary(area: str, summary: str) with TTL from settings.redis_ttl_seconds
"""


class RedisService:
    def __init__(self):
        # TODO: initialize redis.Redis client
        pass

    def get_summary(self, area: str) -> str | None:
        # TODO: return cached summary or None
        raise NotImplementedError

    def set_summary(self, area: str, summary: str) -> None:
        # TODO: store summary with TTL
        raise NotImplementedError


redis_service = RedisService()
