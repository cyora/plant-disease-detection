# Project journal

## Session 1 — Setup
- Created GitHub repo and project structure
- Created conda environment `plantdisease` (Python 3.11, TensorFlow 2.21)
- Chose original PlantVillage dataset (color) instead of the pre-augmented version to avoid train/validation leakage
- Will train on Kaggle Notebooks (free GPU)

from data exploration : 
- Total images: 54305
Classes: 38 | Plants: 14

- Imbalance ratio: 36.2
- image size (256, 256), I'll resize them to  224×224, the input size MobileNetV2 expects

- Train: 38013 | Validation: 8146 | Test: 8146