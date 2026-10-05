# LeafScan: plant leaf disease detection

LeafScan identifies plant leaf diseases from a photo. It recognizes 38 classes (26 diseases and 12 healthy leaf types across 14 crops), shows which part of the leaf led to its answer with a Grad-CAM heatmap, and flags uncertain results instead of guessing.

Second-year engineering project, TekUP University.

## Results

The final model is EfficientNetB0, fine-tuned on the PlantVillage dataset (54,305 images).

| Model | Test accuracy | Macro F1 | Top-3 accuracy |
|---|---|---|---|
| Custom CNN (baseline) | 82.94% | 0.802 | 95.63% |
| MobileNetV2 | 96.48% | 0.958 | 99.66% |
| **EfficientNetB0** | **97.67%** | **0.970** | **99.79%** |

Below a confidence of 0.80, the result is flagged as uncertain. With this threshold, the application answers 94.97% of test photos with 99.35% accuracy. A prediction takes about 0.2 s on a laptop CPU, and about 0.6 s with the heatmap.

## Architecture

```
Angular (4200)  ->  Spring Boot (8080)  ->  FastAPI + TensorFlow (8000)
                          |
                       MySQL 8.4 (3306)  +  images on disk
```

| Folder | Content |
|---|---|
| `notebooks/` | Data exploration, training and evaluation (run on Kaggle, GPU T4) |
| `ml-api/` | FastAPI inference service: prediction and Grad-CAM |
| `backend/` | Spring Boot API: scans, history, statistics, disease library |
| `frontend/` | Angular interface |
| `models/` | Trained model and class names (the model file is not in Git) |
| `docs/` | Journal, figures, evaluation results |

## Prerequisites

| Tool | Version used |
|---|---|
| Anaconda (Python) | Python 3.11 |
| Java | JDK 25 (Temurin) |
| MySQL | 8.4 LTS |
| Node.js | 24 LTS |
| Angular CLI | 22 |

## Installation

### 1. Python environment

```
conda create -n plantdisease python=3.11 -y
conda activate plantdisease
pip install -r ml-api/requirements.txt
```

### 2. Get the model

The trained model (`efficientnetb0.keras`, about 30 MB) is too large for the repository. Put it in `models/`, next to `class_names.json`. It is produced by `notebooks/02-model-training.ipynb` (Kaggle output, `models/` folder).

Download it from the [v1.0 release](https://github.com/cyora/plant-disease-detection/releases/tag/v1.0) of this repository.

### 3. Database

In MySQL Workbench, as root:

```sql
CREATE DATABASE plant_disease CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'plantapp'@'localhost' IDENTIFIED BY 'plantapp_local';
GRANT ALL PRIVILEGES ON plant_disease.* TO 'plantapp'@'localhost';
FLUSH PRIVILEGES;
```

The tables and the 38 disease records are created automatically at the first start of the backend.

### 4. Interface dependencies

```
cd frontend
npm install
```

## Running

**Windows, one click:** double-click `start-all.bat` at the root. It opens one window per service and then the browser at http://localhost:4200. Close the three windows to stop.

**Manually**, in three terminals:

```
# 1. ML service
conda activate plantdisease
cd ml-api
uvicorn app.main:app --port 8000

# 2. Backend
cd backend
mvnw.cmd spring-boot:run

# 3. Interface
cd frontend
npm start
```

| Address | What |
|---|---|
| http://localhost:4200 | The application |
| http://localhost:8000/docs | ML service documentation (Swagger) |
| http://localhost:8080/api/stats | Example backend endpoint |

## Main API endpoints (backend)

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/scans` | Analyze a photo (multipart field `file`) |
| GET | `/api/scans?page=&size=` | History |
| GET | `/api/scans/{id}` | One result |
| GET | `/api/scans/{id}/image`, `/heatmap` | Photo and heatmap |
| DELETE | `/api/scans/{id}` | Delete a scan |
| GET | `/api/diseases?plant=` | Disease library |
| GET | `/api/stats` | Dashboard statistics |

## Limitations

- PlantVillage photos show one leaf on a plain background; accuracy on field photos is expected to be lower.
- The threshold reduces wrong answers on unrelated images but does not detect them reliably.
- Treatment information is general guidance. Check with a local agricultural advisor before applying any treatment.

## Dataset

Hughes, D. P., & Salathé, M. (2015). An open access repository of images on plant health to enable the development of mobile disease diagnostics. arXiv:1511.08060.
