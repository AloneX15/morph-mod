import fs from 'node:fs';
import path from 'node:path';
import assert from 'node:assert/strict';

const mod = JSON.parse(fs.readFileSync('src/main/resources/fabric.mod.json', 'utf8'));
assert.deepEqual(mod.authors, ['TakumiStudios'], 'authors must be exactly TakumiStudios');
for (const key of ['homepage', 'sources', 'issues']) assert.match(mod.contact[key], /^https:\/\//);
assert.ok(mod.description && mod.license && fs.existsSync(`src/main/resources/${mod.icon}`));
assert.equal(mod.depends.java, '>=25');
assert.equal(mod.depends.minecraft, '${minecraft}');
assert.equal(mod.depends.fabricloader, '>=${loader}');
for (const name of ['LICENSE', 'CHANGELOG.md', 'SECURITY.md', 'MIXINS.md', 'docs/registro-de-errores.md']) assert.ok(fs.existsSync(name), `Missing ${name}`);
assert.match(fs.readFileSync('README.md', 'utf8'), /Creado por \*\*TakumiStudios\*\*\./);
const props = fs.readFileSync('stonecutter.properties.toml', 'utf8');
const version = props.match(/^mod\.version\s*=\s*"([^"]+)"/m)?.[1];
assert.match(version, /^\d+\.\d+\.\d+(?:-[\w.-]+)?$/);
assert.ok(fs.readFileSync('CHANGELOG.md', 'utf8').includes(`## ${version}`), 'Missing changelog version');
assert.match(props, /mod\.group = "com\.takumistudios\.morphmod"/);
const en = JSON.parse(fs.readFileSync('src/main/resources/assets/morphmod/lang/en_us.json', 'utf8'));
const es = JSON.parse(fs.readFileSync('src/main/resources/assets/morphmod/lang/es_es.json', 'utf8'));
assert.deepEqual(Object.keys(en).sort(), Object.keys(es).sort(), 'Translation keys differ');
const walk = dir => fs.readdirSync(dir, {withFileTypes: true}).flatMap(entry => {
  const file = path.join(dir, entry.name);
  return entry.isDirectory() ? walk(file) : [file];
});
for (const file of walk('src').filter(file => file.endsWith('.java'))) {
  const source = fs.readFileSync(file, 'utf8');
  assert.match(source, /^package com\.takumistudios\.morphmod[.;]/m, `Wrong package in ${file}`);
  assert.doesNotMatch(source, /System\.(out|err)\b|\.printStackTrace\s*\(/, `Console logging in ${file}`);
  assert.doesNotMatch(source, /catch\s*\([^)]*\)\s*\{\s*\}/, `Empty catch in ${file}`);
  for (const [, key] of source.matchAll(/Component\.translatable\("([^"]+)"/g)) {
    if (key.endsWith('.')) continue; // Dynamic enum suffix; checked below.
    assert.ok(key in en, `Missing translation ${key} in ${file}`);
  }
}
const passiveSource = fs.readFileSync('src/main/java/com/takumistudios/morphmod/morph/Passive.java', 'utf8');
for (const [, name] of passiveSource.matchAll(/^\s*([A-Z_]+)[,;]/gm)) assert.ok(`passive.morphmod.${name.toLowerCase()}` in en);
assert.doesNotMatch(fs.readFileSync('.github/workflows/build.yml', 'utf8') + fs.readFileSync('.github/workflows/release.yml', 'utf8'), /__[A-Z_]+__/);
console.log(`TakumiStudios standards passed: Morph ${version}, ${Object.keys(en).length} translation keys.`);
