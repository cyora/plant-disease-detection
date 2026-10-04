# ML service (FastAPI)

Serves the EfficientNetB0 model trained in `notebooks/02-model-training.ipynb`.

## Run

```
conda activate plantdisease
cd ml-api
uvicorn app.main:app --port 8000
```

Swagger documentation: http://127.0.0.1:8000/docs

## Endpoints

| Method | Path | Description |
|---|---|---|
| GET | `/health` | Service status, model name, threshold |
| GET | `/classes` | The 38 class names |
| POST | `/predict` | Photo → top class, top 3, uncertain flag, Grad-CAM (base64) |
| POST | `/predict/heatmap` | Photo → Grad-CAM overlay as a PNG image |

## Configuration (environment variables)

| Variable | Default |
|---|---|
| `MODEL_PATH` | `../models/efficientnetb0.keras` |
| `CLASS_NAMES_PATH` | `../models/class_names.json` |
| `CONFIDENCE_THRESHOLD` | `0.80` |
| `LAST_CONV_LAYER` | `top_activation` |
| `MAX_UPLOAD_MB` | `5` |

## Benchmark

Put test images in `sample_images/` (name format `Class___Name--file.JPG`), then:

```
python -m scripts.benchmark
```
