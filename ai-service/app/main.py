from contextlib import asynccontextmanager

from fastapi import Depends, FastAPI, HTTPException
from pydantic import BaseModel, field_validator

from app.config import settings
from app.services.gemini_service import GeminiService, build_gemini_service


_gemini: GeminiService | None = None


@asynccontextmanager
async def lifespan(app: FastAPI):
    global _gemini
    _gemini = build_gemini_service()
    yield


app = FastAPI(
    title=settings.app_name,
    version=settings.app_version,
    docs_url="/docs",
    redoc_url="/redoc",
    lifespan=lifespan,
)


def get_gemini() -> GeminiService:
    if _gemini is None:
        raise HTTPException(
            status_code=503,
            detail="AI service unavailable: GEMINI_API_KEY not configured",
        )
    return _gemini


class SummariseRequest(BaseModel):
    area: str
    issues: list[str]

    @field_validator("area")
    @classmethod
    def area_not_blank(cls, v: str) -> str:
        v = v.strip()
        if not v:
            raise ValueError("area must not be blank")
        return v

    @field_validator("issues")
    @classmethod
    def issues_not_empty(cls, v: list[str]) -> list[str]:
        if not v:
            raise ValueError("issues must contain at least one entry")
        if len(v) > 50:
            raise ValueError("issues must contain at most 50 entries")
        v = [item.strip() for item in v]
        if any(len(item) == 0 for item in v):
            raise ValueError("issue entries must not be blank")
        return v


class SummariseResponse(BaseModel):
    area: str
    summary: str
    issue_count: int


@app.get("/health")
def health():
    return {
        "status": "healthy",
        "service": settings.app_name,
        "version": settings.app_version,
        "gemini_configured": _gemini is not None,
    }


@app.post("/summarise", response_model=SummariseResponse)
def summarise(
    body: SummariseRequest,
    gemini: GeminiService = Depends(get_gemini),
):
    """Call Gemini and return a summary. No caching — the backend owns that."""
    try:
        summary = gemini.generate_area_summary(body.area, body.issues)
    except Exception as exc:
        raise HTTPException(status_code=502, detail=f"Gemini error: {exc}") from exc

    return SummariseResponse(
        area=body.area,
        summary=summary,
        issue_count=len(body.issues),
    )
