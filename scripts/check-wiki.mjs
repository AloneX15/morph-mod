import fs from 'node:fs';
import path from 'node:path';
import assert from 'node:assert/strict';

const root = path.resolve(process.argv[2] ?? 'docs/wiki');
const pages = fs.readdirSync(root).filter(name => name.endsWith('.md'));
const slugs = new Set(pages.map(name => name.slice(0, -3)));
let links = 0, jsonExamples = 0;
for (const name of pages) {
  const body = fs.readFileSync(path.join(root, name), 'utf8');
  assert(!body.includes('\uFFFD'), `${name}: replacement character`);
  assert(!body.includes('§'), `${name}: unexpanded backtick marker`);
  assert(!/TODO|TBD|PLACEHOLDER/.test(body), `${name}: unfinished content`);
  assert(body.includes('TakumiStudios'), `${name}: missing attribution`);
  assert.equal((body.match(/^~~~/gm) ?? []).length % 2, 0, `${name}: unclosed code fence`);
  for (const match of body.matchAll(/!?\[[^\]]*\]\(([^)]+)\)/g)) {
    links++;
    const target = match[1];
    if (/^https?:\/\//.test(target)) continue;
    if (target.includes('/')) assert(fs.existsSync(path.join(root, target)), `${name}: missing file ${target}`);
    else assert(slugs.has(target.replace(/\.md$/, '')), `${name}: missing page ${target}`);
  }
  for (const match of body.matchAll(/~~~json\s*\n([\s\S]*?)\n~~~/g)) {
    JSON.parse(match[1]); jsonExamples++;
  }
  if (/^(ES|EN)-/.test(name) && name !== 'ES-Index.md' && name !== 'EN-Index.md') {
    const other = name.startsWith('ES-') ? name.replace(/^ES-/, 'EN-') : name.replace(/^EN-/, 'ES-');
    assert(pages.includes(other), `${name}: missing translation`);
    assert(body.includes(`](${other})`), `${name}: missing translation link`);
    const translation = fs.readFileSync(path.join(root, other), 'utf8');
    assert.equal((body.match(/^## /gm) ?? []).length, (translation.match(/^## /gm) ?? []).length, `${name}: section parity`);
    const json = text => [...text.matchAll(/~~~json\s*\n([\s\S]*?)\n~~~/g)].map(m => JSON.parse(m[1]));
    assert.deepEqual(json(body), json(translation), `${name}: JSON translation parity`);
  }
}
const otter = JSON.parse(fs.readFileSync(path.join(root, 'examples/otter/character.json'), 'utf8'));
const explorer = JSON.parse(fs.readFileSync(path.join(root, 'examples/explorador/character.json'), 'utf8'));
for (const def of [otter, explorer]) {
  const geo = JSON.parse(fs.readFileSync(path.join(root, 'examples/otter', def.model), 'utf8'));
  const anim = JSON.parse(fs.readFileSync(path.join(root, 'examples/otter', def.animations), 'utf8')).animations;
  const bones = new Set(geo['minecraft:geometry'][0].bones.map(b => b.name));
  for (const clip of Object.values(def.bindings ?? {})) assert(clip in anim, `missing bound clip ${clip}`);
  for (const anchor of Object.values(def.anchors ?? {})) assert(bones.has(anchor.bone), `missing anchor ${anchor.bone}`);
  for (const emote of Object.values(def.emotes ?? {})) {
    assert(emote.animation in anim, `missing emote clip ${emote.animation}`);
    for (const bone of emote.bones ?? []) assert(bones.has(bone), `missing mask bone ${bone}`);
  }
}
assert.equal(pages.length, 61);
assert.equal(slugs.size, 61);
console.log(JSON.stringify({ pages: pages.length, bilingualTopics: 28, links, jsonExamples, templates: 2, status: 'passed' }));
