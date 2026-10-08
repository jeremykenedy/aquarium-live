#!/usr/bin/env python3
"""Checks the documentation: local links and anchors, the README contents list
(every ## section), the banners, the license line, and that every GitHub
Action is pinned to a commit."""

import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent


def without_code(text):
    return re.sub(r"```.*?```", "", text, flags=re.S)


def anchors(path, levels="1,6"):
    found = set()
    for heading in re.findall(r"^#{" + levels + r"} (.+)$", without_code(path.read_text()), flags=re.M):
        found.add(re.sub(r"[^\w\- ]", "", heading.strip().lower()).replace(" ", "-"))
    return found


def check_links(problems):
    documents = [ROOT / "README.md", ROOT / "CHANGELOG.md", *sorted((ROOT / "docs").glob("*.md"))]
    checked = 0
    for path in documents:
        text = without_code(path.read_text())
        targets = re.findall(r"\]\(([^\s)]+)\)", text)
        targets += re.findall(r'(?:src|href|srcset)="([^"]+)"', text)
        for target in targets:
            if target.startswith(("https:", "http:", "mailto:")):
                continue
            name, _, fragment = target.partition("#")
            linked = (path.parent / name).resolve() if name else path
            checked += 1
            if not linked.is_file() or ROOT not in linked.parents:
                problems.append(f"{path.relative_to(ROOT)}: missing {target}")
            elif fragment and linked.suffix == ".md" and fragment not in anchors(linked):
                problems.append(f"{path.relative_to(ROOT)}: missing anchor {target}")
    return len(documents), checked


def check_readme(problems):
    readme = (ROOT / "README.md").read_text()
    for heading in anchors(ROOT / "README.md", "2") - {"table-of-contents"}:
        if f"](#{heading})" not in readme:
            problems.append(f"README.md: the table of contents is missing #{heading}")
    if not readme.rstrip().endswith("licensed under the [MIT license](LICENSE)."):
        problems.append("README.md: must end with the MIT license line")


def check_banners(problems):
    for mode in ("light", "dark"):
        path = ROOT / "art" / f"banner-{mode}.svg"
        if 'viewBox="0 0 800 200"' not in path.read_text():
            problems.append(f"{path.relative_to(ROOT)}: viewBox must be 0 0 800 200")
        if "No tracking or analytics" not in path.read_text():
            problems.append(f"{path.relative_to(ROOT)}: must say there is no tracking or analytics")


def check_action_pins(problems):
    for workflow in sorted((ROOT / ".github" / "workflows").glob("*.yml")):
        for reference in re.findall(r"uses:\s*([^\s#]+)", workflow.read_text()):
            if not re.fullmatch(r"[\w.-]+/[\w./-]+@[0-9a-f]{40}", reference):
                problems.append(f"{workflow.relative_to(ROOT)}: {reference} is not pinned to a commit")


def main():
    problems = []
    documents, links = check_links(problems)
    check_readme(problems)
    check_banners(problems)
    check_action_pins(problems)
    for problem in problems:
        print(problem)
    if problems:
        sys.exit(1)
    print(f"Documentation checks passed: {documents} documents, {links} local links, contents, banners, license, action pins")


if __name__ == "__main__":
    main()
