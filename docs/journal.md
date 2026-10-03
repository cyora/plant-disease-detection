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

## Session 3 — Model training (Phase 2)
- Trained 3 models on Kaggle (GPU T4, TF 2.20), total run 75 min
- Results on validation set:
  - Custom CNN: 84.04% (0.4M params, 15.5 min, 4.9 MB)
  - MobileNetV2: 96.37% (2.31M params, 21.1 min, 22.3 MB)
  - EfficientNetB0: 97.24% (4.1M params, 27.9 min, 29.5 MB)
- Transfer learning beats the CNN from scratch by +12 points
- Custom CNN validation accuracy was unstable (55% → 84% → 65%), early stopping kept epoch 3
- Fine-tuning gave a clear jump for both pretrained models
- Final model choice postponed to Phase 3 (test set + inference speed)  