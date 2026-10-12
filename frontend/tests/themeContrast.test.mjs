import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import { test } from 'node:test';

const css = await readFile(new URL('../src/styles/index.css', import.meta.url), 'utf8');

function variables(block) {
  return Object.fromEntries([...block.matchAll(/--([\w-]+):\s*(#[0-9a-fA-F]{6})\s*;/g)]
    .map(([, name, value]) => [name, value.slice(1)]));
}

function luminance(hex) {
  const channels = [0, 2, 4].map((start) => Number.parseInt(hex.slice(start, start + 2), 16) / 255);
  const linear = channels.map((value) => value <= 0.04045 ? value / 12.92 : ((value + 0.055) / 1.055) ** 2.4);
  return 0.2126 * linear[0] + 0.7152 * linear[1] + 0.0722 * linear[2];
}

function contrastRatio(first, second) {
  const a = luminance(first);
  const b = luminance(second);
  return (Math.max(a, b) + 0.05) / (Math.min(a, b) + 0.05);
}

test('muted text meets WCAG AA contrast on the application surfaces in both themes', () => {
  const darkBlock = css.match(/:root\s*\{([\s\S]*?)\n\}/)?.[1];
  const lightBlock = css.match(/:root\[data-theme='light'\]\s*\{([\s\S]*?)\n\}/)?.[1];
  assert.ok(darkBlock, 'dark theme token block exists');
  assert.ok(lightBlock, 'light theme token block exists');

  for (const [theme, block] of [['dark', darkBlock], ['light', lightBlock]]) {
    const values = { ...variables(darkBlock), ...variables(block) };
    for (const surface of ['color-bg-base', 'color-bg-card', 'color-bg-elevated', 'color-bg-subtle', 'color-bg-input']) {
      assert.ok(values[surface], `${theme} theme defines ${surface}`);
      assert.ok(
        contrastRatio(values['color-text-muted'], values[surface]) >= 4.5,
        `${theme} muted text should reach 4.5:1 on ${surface}`,
      );
    }
  }
});
