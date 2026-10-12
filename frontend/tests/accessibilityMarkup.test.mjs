import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import { test } from 'node:test';

const login = await readFile(new URL('../src/pages/LoginPage.tsx', import.meta.url), 'utf8');
const register = await readFile(new URL('../src/pages/RegisterPage.tsx', import.meta.url), 'utf8');
const platformCard = await readFile(new URL('../src/components/dashboard/PlatformCard.tsx', import.meta.url), 'utf8');
const platformAccountModal = await readFile(new URL('../src/components/platform/PlatformAccountModal.tsx', import.meta.url), 'utf8');
const codingProfiles = await readFile(new URL('../src/pages/CodingProfilesPage.tsx', import.meta.url), 'utf8');
const appNavbar = await readFile(new URL('../src/components/layout/AppNavbar.tsx', import.meta.url), 'utf8');
const componentStyles = await readFile(new URL('../src/styles/components.css', import.meta.url), 'utf8');

function assertFieldErrorAssociation(source, fieldId, errorId) {
  const input = new RegExp(`<input[\\s\\S]*?id="${fieldId}"[\\s\\S]*?aria-invalid=\\{Boolean\\(fieldErrors\\.${fieldId.replace('register-', '').replace('login-', '')}\\)\\}[\\s\\S]*?aria-describedby=\\{fieldErrors\\.${fieldId.replace('register-', '').replace('login-', '')} \\? '${errorId}' : undefined\\}`);
  assert.match(source, input, `${fieldId} should expose invalid state and describe its error`);
  assert.match(source, new RegExp(`<span id="${errorId}" className="form-error" role="alert">`));
}

test('login and registration validation errors are linked to fields and announced', () => {
  assertFieldErrorAssociation(login, 'email', 'login-email-error');
  assertFieldErrorAssociation(login, 'password', 'login-password-error');
  assertFieldErrorAssociation(register, 'name', 'register-name-error');
  assertFieldErrorAssociation(register, 'email', 'register-email-error');
  assertFieldErrorAssociation(register, 'password', 'register-password-error');
  assert.match(login, /className="alert alert-error" role="alert"/);
  assert.match(register, /className="alert alert-error" role="alert"/);
});

test('dashboard profile link has an accessible name and sync failures are announced as alerts', () => {
  assert.match(platformCard, /aria-label=\{`View \$\{platform\.platform\} profile \(opens in a new tab\)`\}/);
  assert.match(platformCard, /role=\{feedback\.success \? 'status' : 'alert'\}/);
});

test('platform account fields and actions have accessible names and linked validation feedback', () => {
  assert.match(platformAccountModal, /htmlFor="platform-account-platform"/);
  assert.match(platformAccountModal, /id="platform-account-platform"[\s\S]*?aria-invalid=\{Boolean\(fieldErrors\.platform\)\}[\s\S]*?aria-describedby=\{fieldErrors\.platform \? 'platform-account-platform-error' : undefined\}/);
  assert.match(platformAccountModal, /htmlFor="platform-account-username"/);
  assert.match(platformAccountModal, /id="platform-account-username"[\s\S]*?aria-invalid=\{Boolean\(fieldErrors\.username\)\}[\s\S]*?aria-describedby=\{fieldErrors\.username \? 'platform-account-username-error' : undefined\}/);
  assert.match(platformAccountModal, /htmlFor="platform-account-profile-url"/);
  assert.match(platformAccountModal, /id="platform-account-profile-url"[\s\S]*?aria-invalid=\{Boolean\(fieldErrors\.profileUrl\)\}[\s\S]*?aria-describedby=\{fieldErrors\.profileUrl \? 'platform-account-profile-url-error' : undefined\}/);
  assert.match(platformAccountModal, /className="alert alert-error" role="alert"/);
  assert.match(codingProfiles, /aria-label=\{`Edit \$\{acc\.platform\} account`\}/);
  assert.match(codingProfiles, /aria-label=\{`Disconnect \$\{acc\.platform\} account`\}/);
});

test('buttons and form fields retain visible keyboard focus indicators', () => {
  assert.match(componentStyles, /\.btn:focus-visible,\s*\.form-input:focus-visible\s*\{\s*outline: 2px solid var\(--color-border-focus\);\s*outline-offset: 2px;/);
});

test('profile menu trigger has a descriptive accessible name when the username is visually hidden', () => {
  assert.match(appNavbar, /className="profile-menu-trigger"[\s\S]*?aria-label=\{`\$\{profileOpen \? 'Close' : 'Open'\} profile menu for \$\{user\?\.name \|\| 'account'\}`\}/);
});
