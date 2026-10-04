"""Configuration of the ML service.

Every value can be overridden with an environment variable, so the model,
the threshold or the paths can change without touching the code.
"""
import os
from pathlib import Path

# ml-api/app/config.py -> parents[2] is the project root (plant-disease-detection/)
PROJECT_ROOT = Path(__file__).resolve().parents[2]

MODEL_PATH = Path(os.getenv("MODEL_PATH", PROJECT_ROOT / "models" / "efficientnetb0.keras"))
CLASS_NAMES_PATH = Path(os.getenv("CLASS_NAMES_PATH", PROJECT_ROOT / "models" / "class_names.json"))
MODEL_NAME = os.getenv("MODEL_NAME", "efficientnetb0")

# Last convolutional layer used by Grad-CAM ("out_relu" for MobileNetV2)
LAST_CONV_LAYER = os.getenv("LAST_CONV_LAYER", "top_activation")

# Below this confidence the result is flagged as uncertain (chosen in Phase 3)
CONFIDENCE_THRESHOLD = float(os.getenv("CONFIDENCE_THRESHOLD", "0.80"))

IMG_SIZE = 224
MAX_UPLOAD_BYTES = int(os.getenv("MAX_UPLOAD_MB", "5")) * 1024 * 1024
