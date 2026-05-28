import logging

import google.generativeai as genai

from app.config import settings

logger = logging.getLogger(__name__)

_PROMPT_TEMPLATE = """
You are a civic analyst summarising resident-reported issues for a local government dashboard.

Given the following list of reported civic issues in an area, write ONE concise summary sentence (max 30 words) that captures the main themes.
Write in third person. Do not list individual issues — synthesise them.

Issues:
{issues}

Summary:
""".strip()


class GeminiService:
    def __init__(self) -> None:
        if not settings.gemini_api_key:
            raise ValueError("GEMINI_API_KEY is not set")
        genai.configure(api_key=settings.gemini_api_key)
        self._model = genai.GenerativeModel(settings.gemini_model)

    def generate_area_summary(self, issues: list[str]) -> str:
        if not issues:
            raise ValueError("issues list must not be empty")

        bullet_list = "\n".join(f"- {issue}" for issue in issues)
        prompt = _PROMPT_TEMPLATE.format(issues=bullet_list)

        try:
            response = self._model.generate_content(prompt)
            return response.text.strip()
        except Exception as exc:
            logger.error("Gemini API call failed: %s", exc)
            raise


def build_gemini_service() -> GeminiService:
    """Return a GeminiService, or None if the API key is missing (allows the app to start without it)."""
    try:
        return GeminiService()
    except ValueError as exc:
        logger.warning("GeminiService not initialised: %s", exc)
        return None
