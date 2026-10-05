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

## Session 4 — Evaluation (Phase 3)
- Evaluated the 3 models on the untouched test set (8,146 images):
  - Custom CNN: accuracy 82.94%, macro F1 0.802, top-3 95.63%
  - MobileNetV2: accuracy 96.48%, macro F1 0.958, top-3 99.66%
  - EfficientNetB0: accuracy 97.67%, macro F1 0.970, top-3 99.79%
- Final model: EfficientNetB0 (best on every metric). Test accuracy close to validation (97.24%), so no overfitting to the validation set
- Weakest classes: Potato healthy (F1 0.84, only 23 test images, fragile metric),
  Corn Cercospora (0.88), Tomato Early blight (0.88), Tomato Target Spot (0.89)
- Main confusions: diseases of the same plant with similar symptoms
  (Early blight vs Target Spot, Cercospora vs Northern Leaf Blight).
  Tomato vs Potato Late blight = same pathogen (Phytophthora infestans)
- Grad-CAM: heat mostly on lesions. Corn Cercospora: heat on the image edge, not the lesions (to discuss as a limitation)
- Confidence threshold chosen on the validation set: 0.80
  - Validation: 94.83% coverage, 99.11% accuracy on answered photos
  - Test: 94.97% coverage, 99.35% accuracy on answered photos
  - Reason: above 0.80, most rejected photos were correct (diminishing returns), and field photos will be less confident
- Below the threshold the app will show "uncertain" + the top 3
- Fix: os.walk was very slow on Kaggle; now it skips class folders
- Started the report: Chapter 4 drafted (sections 4.1 to 4.8)

   ## Session 5 — FastAPI ML service (Phase 4)
   - Built the FastAPI service: /health, /classes, /predict, /predict/heatmap
   - Model loaded once at startup; preprocessing identical to training
   - Threshold 0.80 configurable via environment variable
   - Benchmark on 76 test images (2 per class), local CPU:
     - Accuracy 96.05% (73/76); 6 uncertain; 97.14% on answered (68/70)
     - Inference: median 186 ms, p95 405 ms
     - Grad-CAM: median 431 ms, p95 1149 ms
   - Total ~0.6 s per request, under the 3 s requirement

      - Benchmark errors (same confusions as on the test set):
     - Corn Cercospora -> Northern Leaf Blight (86%, passes the threshold)
     - Corn Northern Leaf Blight -> Cercospora (86%, passes the threshold)
     - Tomato Early blight -> Target Spot (51%, caught by the threshold)
   - Second run: inference median 186 ms (p95 209), Grad-CAM median 420 ms (p95 498)
   - Error handling: non-image file returns 400; a photo of text returns uncertain (25%)

   