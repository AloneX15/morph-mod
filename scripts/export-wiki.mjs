import fs from 'node:fs';
import path from 'node:path';

// The checked-in source works as ordinary GitHub Markdown. Export converts page
// links to Wiki routes and binary/example links to the Wiki's raw Git endpoint.
const source = path.resolve('docs/wiki');
const destination = path.resolve('build/wiki-export');
const assets = 'https://raw.githubusercontent.com/wiki/AloneX15/morph-mod/';
fs.mkdirSync(destination, {recursive:true});
for (const entry of fs.readdirSync(source, {withFileTypes:true})) {
  const input = path.join(source, entry.name);
  const output = path.join(destination, entry.name);
  if (entry.isDirectory()) fs.cpSync(input, output, {recursive:true});
  else if (entry.name.endsWith('.md')) {
    const body = fs.readFileSync(input, 'utf8')
      .replace(/\]\(((?:ES|EN)-[^)/]+|Home)\.md\)/g, ']($1)')
      .replace(/\]\(((?:images|examples)\/[^)]+)\)/g, `](${assets}$1)`);
    fs.writeFileSync(output, body);
  }
}
console.log('Wiki exported to build/wiki-export. Create the first Home page on GitHub, then copy these files into a clone of morph-mod.wiki.git, commit and push its default branch.');
