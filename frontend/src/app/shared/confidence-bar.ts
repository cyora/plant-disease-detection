import { Component, computed, input } from '@angular/core';

/**
 * Horizontal bar showing the model's confidence, with a marker at the threshold.
 * Green above the threshold, ochre below (uncertain).
 */
@Component({
  selector: 'app-confidence-bar',
  template: `
    <div class="meter"
         role="meter"
         aria-valuemin="0"
         aria-valuemax="100"
         [attr.aria-valuenow]="pct()"
         [attr.aria-label]="'Confidence ' + pct() + ' percent'">
      <div class="labels">
        <span class="label">Confidence</span>
        <span class="value" [class.low]="low()">{{ pct() }}%</span>
      </div>
      <div class="track">
        <div class="fill" [class.low]="low()" [style.width.%]="pct()"></div>
        <div class="threshold" [style.left.%]="thresholdPct()">
          <span>{{ thresholdPct() }}% needed</span>
        </div>
      </div>
    </div>
  `,
  styles: `
    .meter { margin: 1.5rem 0 0.5rem; max-width: 560px; }
    .labels { display: flex; justify-content: space-between; align-items: baseline; margin-bottom: 1.6rem; }
    .label { color: var(--muted); font-weight: 700; }
    .value { font-family: var(--font-head); font-weight: 700; font-size: 1.75rem; color: var(--healthy); }
    .value.low { color: var(--ochre); }
    .track { position: relative; height: 14px; background: var(--leaf-200); border-radius: 7px; }
    .fill { height: 100%; border-radius: 7px; background: var(--healthy); transition: width 0.6s ease-out; }
    .fill.low { background: var(--ochre); }
    .threshold { position: absolute; top: -8px; bottom: -8px; width: 2px; background: var(--leaf-900); }
    .threshold span {
      position: absolute; bottom: calc(100% + 4px); left: 50%; transform: translateX(-50%);
      white-space: nowrap; font-size: 0.8rem; color: var(--leaf-900); font-weight: 700;
    }
  `,
})
export class ConfidenceBar {
  /** Between 0 and 1 */
  readonly value = input.required<number>();
  /** Between 0 and 1 */
  readonly threshold = input.required<number>();

  readonly pct = computed(() => Math.round(this.value() * 1000) / 10);
  readonly thresholdPct = computed(() => Math.round(this.threshold() * 100));
  readonly low = computed(() => this.value() < this.threshold());
}
