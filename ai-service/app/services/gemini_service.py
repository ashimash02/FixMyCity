import logging

import google.generativeai as genai

from app.config import settings

logger = logging.getLogger(__name__)

# Sentinel area name sent by the backend when the user has no location selected.
# Kept in sync with AreaSummaryServiceImpl.TRENDING_AREA_LABEL on the Java side.
_TRENDING_AREA = "All locations"

_AREA_PROMPT = """
You are a civic analyst writing for the FixMyCity locality dashboard.

The following civic issues were reported by residents in {area}. Write ONE concise
paragraph (max 4 sentences) summarising what residents in this area are concerned about.

Guidelines:
- Always write the summary regardless of how many issues are listed — even one issue is enough
- Third person, factual, no hype or marketing language
- Synthesise the themes — do NOT list issues individually
- Mention {area} naturally in the paragraph
- Never ask for more issues or say the list is incomplete — work with what is given

Issues:
{issues}

Summary:
""".strip()

_TRENDING_PROMPT = """
You are a civic analyst writing for the FixMyCity trending dashboard.

The following are the top-voted civic issues across the city right now. Write ONE
concise paragraph (max 4 sentences) capturing what's drawing the most resident attention.

Guidelines:
- Always write the summary regardless of how many issues are listed — even one issue is enough
- Third person, factual, no hype
- Synthesise the themes — do NOT list issues individually
- Frame as a citywide trend, not tied to one neighbourhood
- Never ask for more issues or say the list is incomplete — work with what is given

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
        logger.info("GeminiService initialised with model=%s", settings.gemini_model)

    def generate_area_summary(self, area: str, issues: list[str]) -> str:
        if not issues:
            raise ValueError("issues list must not be empty")

        bullet_list = "\n".join(f"- {issue}" for issue in issues)
        template = _TRENDING_PROMPT if area == _TRENDING_AREA else _AREA_PROMPT
        prompt = template.format(area=area, issues=bullet_list)

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
