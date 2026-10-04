"""Loads the trained model once and exposes prediction and Grad-CAM."""
import base64
import json
import time

import cv2
import numpy as np
import tensorflow as tf
from tensorflow import keras

from . import config


def split_class_name(class_name: str) -> dict:
    """'Corn_(maize)___Common_rust_' -> plant 'Corn (maize)', disease 'Common rust'."""
    plant, disease = class_name.split("___")
    return {
        "class_name": class_name,
        "plant": plant.replace("_", " ").strip(),
        "disease": disease.replace("_", " ").strip(),
        "healthy": disease == "healthy",
    }


class PlantDiseaseModel:
    def __init__(self):
        start = time.perf_counter()
        self.model = keras.models.load_model(config.MODEL_PATH, compile=False)
        with open(config.CLASS_NAMES_PATH, encoding="utf-8") as f:
            self.class_names = json.load(f)
        self._prepare_gradcam()
        # Warm-up: the first call is always slower, so we do it at startup
        self.model(tf.zeros((1, config.IMG_SIZE, config.IMG_SIZE, 3)), training=False)
        self.load_seconds = time.perf_counter() - start

    def _prepare_gradcam(self):
        """Builds once the sub-model that returns the last conv feature maps."""
        last = config.LAST_CONV_LAYER
        self.base = next(
            layer for layer in self.model.layers
            if isinstance(layer, keras.Model) and any(s.name == last for s in layer.layers)
        )
        self.base_multi = keras.Model(
            self.base.inputs, [self.base.get_layer(last).output, self.base.output]
        )
        layers_list = [l for l in self.model.layers if not isinstance(l, keras.layers.InputLayer)]
        i = layers_list.index(self.base)
        self.pre_layers = layers_list[:i]        # augmentation (inactive at inference)
        self.post_layers = layers_list[i + 1:]   # pooling, dropout, dense

    @staticmethod
    def preprocess(image_bytes: bytes) -> tf.Tensor:
        """Same steps as the training pipeline: decode as RGB, resize to 224x224, keep values 0-255.
        Rescaling is done inside the model itself."""
        img = tf.io.decode_image(image_bytes, channels=3, expand_animations=False)
        return tf.image.resize(img, (config.IMG_SIZE, config.IMG_SIZE))

    def predict(self, img: tf.Tensor) -> dict:
        start = time.perf_counter()
        probs = self.model(img[None, ...], training=False).numpy()[0]
        inference_ms = (time.perf_counter() - start) * 1000

        top3_idx = np.argsort(probs)[::-1][:3]
        top3 = [
            {**split_class_name(self.class_names[i]), "confidence": round(float(probs[i]), 4)}
            for i in top3_idx
        ]
        return {
            "top_class": top3[0],
            "class_index": int(top3_idx[0]),
            "uncertain": top3[0]["confidence"] < config.CONFIDENCE_THRESHOLD,
            "threshold": config.CONFIDENCE_THRESHOLD,
            "top3": top3,
            "inference_ms": round(inference_ms, 1),
        }

    def gradcam(self, img: tf.Tensor, class_index: int) -> tuple[bytes, float]:
        """Returns the Grad-CAM overlay as PNG bytes, and the time it took in ms."""
        start = time.perf_counter()
        with tf.GradientTape() as tape:
            x = tf.cast(img[None, ...], tf.float32)
            for layer in self.pre_layers:
                x = layer(x, training=False)
            conv_out, x = self.base_multi(x, training=False)
            tape.watch(conv_out)
            for layer in self.post_layers:
                x = layer(x, training=False)
            score = x[:, class_index]

        grads = tape.gradient(score, conv_out)
        weights = tf.reduce_mean(grads, axis=(1, 2))                  # importance of each feature map
        cam = tf.nn.relu(tf.reduce_sum(conv_out[0] * weights[0], axis=-1)).numpy()
        cam = cv2.resize(cam / (cam.max() + 1e-8), (config.IMG_SIZE, config.IMG_SIZE))

        heat = cv2.applyColorMap(np.uint8(255 * cam), cv2.COLORMAP_JET)          # BGR
        photo = cv2.cvtColor(img.numpy().astype("uint8"), cv2.COLOR_RGB2BGR)
        overlay = np.uint8(0.6 * photo + 0.4 * heat)
        _, png = cv2.imencode(".png", overlay)

        gradcam_ms = (time.perf_counter() - start) * 1000
        return png.tobytes(), round(gradcam_ms, 1)

    @staticmethod
    def to_base64(png_bytes: bytes) -> str:
        return base64.b64encode(png_bytes).decode("ascii")
