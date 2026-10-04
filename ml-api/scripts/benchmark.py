"""Measures accuracy and CPU latency of the model on the sample images.

Image file names must start with their class, then "--", e.g.
    Tomato___Late_blight--0a1b2c.JPG

Run from the ml-api folder:
    python -m scripts.benchmark
"""
import statistics
import sys
from pathlib import Path

from app.model import PlantDiseaseModel

SAMPLE_DIR = Path(__file__).resolve().parents[1] / "sample_images"


def p95(values):
    return sorted(values)[int(0.95 * (len(values) - 1))]


def main():
    files = sorted(p for p in SAMPLE_DIR.iterdir() if p.suffix.lower() in {".jpg", ".jpeg", ".png"})
    if not files:
        sys.exit(f"No images found in {SAMPLE_DIR}")

    m = PlantDiseaseModel()
    print(f"Model loaded in {m.load_seconds:.1f} s - {len(files)} images\n")

    # Untimed warm-up of Grad-CAM (its first call is slower)
    first = m.preprocess(files[0].read_bytes())
    m.gradcam(first, m.predict(first)["class_index"])

    inference, gradcam, correct, uncertain, correct_kept = [], [], 0, 0, 0
    for path in files:
        true_class = path.name.split("--")[0]
        img = m.preprocess(path.read_bytes())
        result = m.predict(img)
        _, cam_ms = m.gradcam(img, result["class_index"])

        inference.append(result["inference_ms"])
        gradcam.append(cam_ms)
        is_correct = result["top_class"]["class_name"] == true_class
        correct += is_correct
        if result["uncertain"]:
            uncertain += 1
        else:
            correct_kept += is_correct

    n, kept = len(files), len(files) - uncertain
    print(f"Accuracy (all images):        {correct / n:.2%}  ({correct}/{n})")
    print(f"Uncertain (below threshold):  {uncertain}/{n}")
    if kept:
        print(f"Accuracy on answered images:  {correct_kept / kept:.2%}  ({correct_kept}/{kept})")
    print()
    print(f"Inference time  - mean {statistics.mean(inference):.0f} ms | "
          f"median {statistics.median(inference):.0f} ms | p95 {p95(inference):.0f} ms")
    print(f"Grad-CAM time   - mean {statistics.mean(gradcam):.0f} ms | "
          f"median {statistics.median(gradcam):.0f} ms | p95 {p95(gradcam):.0f} ms")


if __name__ == "__main__":
    main()
