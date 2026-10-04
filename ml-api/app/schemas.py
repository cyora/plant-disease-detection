"""Response formats of the API (they also generate the Swagger documentation)."""
from pydantic import BaseModel


class ClassPrediction(BaseModel):
    class_name: str      # exactly as in class_names.json, e.g. "Tomato___Late_blight"
    plant: str           # e.g. "Tomato"
    disease: str         # e.g. "Late blight"
    healthy: bool
    confidence: float    # between 0 and 1


class PredictionResponse(BaseModel):
    top_class: ClassPrediction
    uncertain: bool                    # True when confidence < threshold
    threshold: float
    top3: list[ClassPrediction]
    heatmap_base64: str | None = None  # Grad-CAM overlay, PNG encoded in base64
    inference_ms: float
    gradcam_ms: float | None = None


class HealthResponse(BaseModel):
    status: str
    model: str
    num_classes: int
    threshold: float
    tensorflow_version: str
