import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import { test } from 'node:test';
import ts from 'typescript';

const routeMetadata = await readFile(new URL('../src/components/seo/RouteMetadata.tsx', import.meta.url), 'utf8');
const seoEventSource = await readFile(new URL('../src/components/seo/publicProfileSeo.ts', import.meta.url), 'utf8');
const profileEdit = await readFile(new URL('../src/components/profile/ProfileEditModal.tsx', import.meta.url), 'utf8');
const vercel = JSON.parse(await readFile(new URL('../vercel.json', import.meta.url), 'utf8'));
const robots = await readFile(new URL('../public/robots.txt', import.meta.url), 'utf8');

test('Vercel proxies the public sitemap to the Render backend before the SPA fallback', () => {
  const sitemapRewriteIndex = vercel.rewrites.findIndex((route) => route.source === '/sitemap.xml');
  const spaRewriteIndex = vercel.rewrites.findIndex((route) => route.destination === '/index.html');
  assert.ok(sitemapRewriteIndex >= 0);
  assert.ok(spaRewriteIndex > sitemapRewriteIndex);
  assert.equal(vercel.rewrites[sitemapRewriteIndex].destination, 'https://kodbtw-backend.onrender.com/api/public/sitemap.xml');
  assert.match(robots, /^Allow: \/$/m);
  assert.match(robots, /^Sitemap: https:\/\/kodbtw\.vercel\.app\/sitemap\.xml$/m);
});

test('profile routes default to noindex until the API confirms owner opt-in', () => {
  assert.match(routeMetadata, /profileMatch \? 'noindex,follow' : 'index,follow'/);
  assert.match(routeMetadata, /detail\.indexable \? 'index,follow' : 'noindex,follow'/);
  assert.match(routeMetadata, /\/u\/\$\{encodeURIComponent\(detail\.username\)\}/);
  assert.match(profileEdit, /Allow search engines to index my public profile/);
  assert.match(profileEdit, /indexable,\s*$/m);
});

test('public SEO metadata event preserves the member opt-in as a boolean', async () => {
  const { outputText, diagnostics } = ts.transpileModule(seoEventSource, {
    compilerOptions: { module: ts.ModuleKind.ESNext, target: ts.ScriptTarget.ES2022 },
    reportDiagnostics: true,
  });
  assert.deepEqual(diagnostics.filter((item) => item.category === ts.DiagnosticCategory.Error), []);
  const { publishPublicProfileSeo, PUBLIC_PROFILE_SEO_EVENT } = await import(
    `data:text/javascript;base64,${Buffer.from(outputText).toString('base64')}`
  );
  let received;
  globalThis.window = {
    dispatchEvent(event) {
      received = event;
      return true;
    },
  };
  try {
    publishPublicProfileSeo({ username: 'public_dev', displayName: 'Public Dev', indexable: false });
    assert.equal(received.type, PUBLIC_PROFILE_SEO_EVENT);
    assert.deepEqual(received.detail, { username: 'public_dev', displayName: 'Public Dev', indexable: false });
  } finally {
    delete globalThis.window;
  }
});
