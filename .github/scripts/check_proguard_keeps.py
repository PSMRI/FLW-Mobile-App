#!/usr/bin/env python3
"""Fail a PR that adds an unjustified or over-broad ProGuard/R8 -keep rule (FLW-1216).

Only lines ADDED by the PR are checked; the existing broad rules are FLW-1211's job.

Rules for every added `-keep*` line in a *.pro file:
  1. It needs a `#` comment directly above it (one comment may cover a group of
     keep lines added together).
  2. It must not keep a wildcard package/class wholesale, e.g.
     `-keep class org.piramalswasthya.sakhi.** { *; }` or `-keepnames class **`.
     If one is truly needed, put `keep-broad-ok: <reason>` in the comment above.

Usage: check_proguard_keeps.py <base-ref>     (e.g. origin/main)
"""
import re
import subprocess
import sys

WILDCARD_CLASS = re.compile(r"(\*\*|(^|[\s.])\*)(\s|\{|$)")
ALL_MEMBERS = re.compile(r"\{\s*\*\s*;\s*\}")
HUNK = re.compile(r"^@@ -\d+(?:,\d+)? \+(\d+)(?:,(\d+))? @@")
BROAD_OK = "keep-broad-ok:"


def added_lines(base):
    """Return {path: set(line numbers added in the new file)} for *.pro files."""
    diff = subprocess.run(
        ["git", "diff", "-U0", "--no-color", f"{base}...HEAD", "--", "*.pro"],
        check=True, capture_output=True, text=True,
    ).stdout
    result, path = {}, None
    for line in diff.splitlines():
        if line.startswith("+++ "):
            path = None if line == "+++ /dev/null" else line[6:]  # strip "+++ b/"
        elif path and (m := HUNK.match(line)):
            start, count = int(m.group(1)), int(m.group(2) or 1)
            result.setdefault(path, set()).update(range(start, start + count))
    return result


def class_spec(rule):
    """`-keep class a.b.** { *; }` -> `a.b.**` (text between the class keyword and `{`)."""
    head = rule.split("{", 1)[0]
    m = re.search(r"\b(class|interface|enum)\s+(.+)$", head.strip())
    return m.group(2).split()[0] if m else ""


def is_broad(rule):
    if not WILDCARD_CLASS.search(class_spec(rule)):
        return False
    # A wildcard class is fine when only specific members are kept.
    return "{" not in rule or bool(ALL_MEMBERS.search(rule))


def comment_above(lines, added, idx):
    """Comment directly above lines[idx], skipping other lines added in the same group."""
    i = idx - 1
    while i >= 0:
        text = lines[i].strip()
        if text.startswith("#"):
            return text
        if not text or (i + 1) not in added:
            return None
        i -= 1
    return None


def main():
    if len(sys.argv) != 2:
        print(__doc__)
        return 2
    errors = 0
    for path, added in added_lines(sys.argv[1]).items():
        with open(path, encoding="utf-8") as f:
            lines = f.read().splitlines()
        for n in sorted(added):
            rule = lines[n - 1].strip()
            if not rule.startswith("-keep"):
                continue
            comment = comment_above(lines, added, n - 1)
            if comment is None:
                print(f"::error file={path},line={n}::New keep rule has no comment above it "
                      f"explaining why it is needed: {rule}")
                errors += 1
            elif is_broad(rule) and BROAD_OK not in comment:
                print(f"::error file={path},line={n}::Keep rule is too broad (wildcard class with all "
                      f"members kept), it blocks R8 shrinking and the Play DEX metric. Narrow it, or "
                      f"add '{BROAD_OK} <reason>' to the comment above: {rule}")
                errors += 1
    if errors:
        print(f"{errors} ProGuard keep rule problem(s). See docs/PLAY_VITALS_MONITORING.md section 5.")
        return 1
    print("ProGuard keep rules OK.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
