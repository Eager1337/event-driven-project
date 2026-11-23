#!/usr/bin/env python3
"""Package all deliverables without credentials, caches or intermediate build output."""
from pathlib import Path
import shutil
import zipfile
import hashlib
import urllib.request

root = Path(__file__).resolve().parent.parent
(root/'dist').mkdir(exist_ok=True)
shutil.copy2(root/'target/marketflow.jar', root/'dist/marketflow.jar')
sources = root/'dist/dependency-sources'
sources.mkdir(exist_ok=True)
for group, artifact, version in [('com/mysql', 'mysql-connector-j', '26.10.0'), ('com/google/protobuf', 'protobuf-java', '4.36.1')]:
    name = f'{artifact}-{version}-sources.jar'
    dest = sources/name
    if not dest.exists():
        urllib.request.urlretrieve(f'https://repo.maven.apache.org/maven2/{group}/{artifact}/{version}/{name}', dest)
results = root/'docs/test-results'
results.mkdir(exist_ok=True)
for path in (root/'target/surefire-reports').glob('*'):
    if path.suffix in ['.xml', '.txt']:
        shutil.copy2(path, results/path.name)
jar = root/'dist/marketflow.jar'
(root/'dist/SHA256SUMS.txt').write_text(hashlib.sha256(jar.read_bytes()).hexdigest()+'  marketflow.jar\n', encoding='utf-8')
archive = root.parent/'marketflow-complete-project.zip'
skip_dirs = {'.git', 'target', '__pycache__', '.idea', '.vscode'}
skip_files = {'.env', 'config.properties', 'task.md', 'source.tar.gz.b64', 'PRESENTATION-STUDY-GUIDE.pdf'}
# The untracked older study-guide PDF is preserved locally, not shipped as current.
with zipfile.ZipFile(archive, 'w', zipfile.ZIP_DEFLATED) as out:
    for path in sorted(root.rglob('*')):
        rel = path.relative_to(root)
        if path.is_file() and not any(part in skip_dirs for part in rel.parts) and path.name not in skip_files and path.suffix not in {'.log', '.zip'}:
            out.write(path, Path('event-driven-project')/rel)
print(archive)