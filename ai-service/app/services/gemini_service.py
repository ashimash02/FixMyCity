"""
Gemini AI service — generates summaries for civic issues grouped by area.

TODO:
- Initialize Gemini client using settings.gemini_api_key
- Implement generate_area_summary(area: str, issues: list[str]) -> str
- Handle API errors and rate limits
"""


class GeminiService:
    def __init__(self):
        # TODO: initialize google.generativeai client
        pass

    def generate_area_summary(self, area: str, issues: list[str]) -> str:
        # TODO: build prompt from issues and call Gemini API
        raise NotImplementedError


gemini_service = GeminiService()
