"""ML service: receives a leaf photo, returns the predicted disease and a Grad-CAM heatmap.

Run from the ml-api folder:
    uvicorn app.main:app --port 8000
Interactive documentation: http://127.0.0.1:8000/docs
"""
from contextlib import asynccontextmanager

import tensorflow as tf
from fastapi import FastAPI, File, HTTPException, Query, UploadFile
from fastapi.responses import Response

from . import config
from .model import PlantDiseaseModel
from .schemas import HealthResponse, PredictionResponse

ml = {}


@asynccontextmanager
async def lifespan(app: FastAPI):
    # The model is loaded ONCE at startup, not at every request
    ml["model"] = PlantDiseaseModel()
    print(f"Model '{config.MODEL_NAME}' loaded in {ml['model'].load_seconds:.1f} s "
          f"({len(ml['model'].class_names)} classes, threshold {config.CONFIDENCE_THRESHOLD})")
    yield
    ml.clear()




app = FastAPI(
    title="Plant Disease Detection - ML service",
    description="Predicts the disease of a plant leaf from a photo "
                "(EfficientNetB0 fine-tuned on PlantVillage, 38 classes).",
    version="1.0.0",
    lifespan=lifespan,
)

from fastapi.responses import RedirectResponse

@app.get("/", include_in_schema=False)
def root():
    return RedirectResponse(url="/docs")

def read_image(file: UploadFile):
    """Reads and validates the uploaded file, returns the preprocessed image tensor."""
    image_bytes = file.file.read()
    if not image_bytes:
        raise HTTPException(status_code=400, detail="The uploaded file is empty.")
    if len(image_bytes) > config.MAX_UPLOAD_BYTES:
        raise HTTPException(status_code=413, detail="The image is larger than the allowed size.")
    try:
        return ml["model"].preprocess(image_bytes)
    except (tf.errors.InvalidArgumentError, ValueError):
        raise HTTPException(status_code=400, detail="Could not read the image. Use a JPG or PNG file.")


@app.get("/health", response_model=HealthResponse)
def health():
    m = ml["model"]
    return HealthResponse(
        status="ok",
        model=config.MODEL_NAME,
        num_classes=len(m.class_names),
        threshold=config.CONFIDENCE_THRESHOLD,
        tensorflow_version=tf.__version__,
    )


@app.get("/classes", response_model=list[str])
def classes():
    return ml["model"].class_names


@app.post("/predict", response_model=PredictionResponse)
def predict(
    file: UploadFile = File(..., description="Leaf photo (JPG or PNG)"),
    gradcam: bool = Query(True, description="Also compute the Grad-CAM heatmap"),
):
    m = ml["model"]
    img = read_image(file)
    result = m.predict(img)
    if gradcam:
        png, result["gradcam_ms"] = m.gradcam(img, result["class_index"])
        result["heatmap_base64"] = m.to_base64(png)
    return PredictionResponse(**result)


@app.post("/predict/heatmap", response_class=Response,
          responses={200: {"content": {"image/png": {}}}})
def predict_heatmap(file: UploadFile = File(..., description="Leaf photo (JPG or PNG)")):
    """Returns only the Grad-CAM overlay as a PNG image (handy to check it in the browser)."""
    m = ml["model"]
    img = read_image(file)
    result = m.predict(img)
    png, _ = m.gradcam(img, result["class_index"])
    return Response(content=png, media_type="image/png")
