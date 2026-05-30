#!/usr/bin/env python3
"""
Generate a compact review pack from a Paper MVP runner JSON output.

The pack is for manual quality review when no real learners are available yet.
It extracts selected cards and leaves scoring fields beside each run.
"""

from __future__ import annotations

import argparse
import glob
import json
from pathlib import Path
from typing import Any


ROOT = Path(__file__).resolve().parents[1]
DEFAULT_RUNS_DIR = ROOT / "paper_runs"
DEFAULT_REVIEW_WORDS = [
    "run",
    "get",
    "break the ice",
    "kick the bucket",
    "compromise",
    "red apple",
]


def latest_runner_json() -> Path:
    candidates = sorted(glob.glob(str(DEFAULT_RUNS_DIR / "paper-mvp-*.json")))
    if not candidates:
        raise FileNotFoundError(f"No runner JSON found under {DEFAULT_RUNS_DIR}")
    return Path(candidates[-1])


def card_summary(card: dict[str, Any]) -> str:
    branches = card.get("branches")
    if branches:
        lines = []
        for branch in branches:
            branch_card = branch.get("card") or {}
            core = branch_card.get("coreConcept") or {}
            lines.append(
                f"- {branch.get('type')}: {branch_card.get('word')} -> "
                f"{core.get('picture')} (like {core.get('anchorWord')})"
            )
        return "\n".join(lines)

    core = card.get("coreConcept") or {}
    scenarios = card.get("scenarios") or []
    lines = [
        f"- 类型：{card.get('entryType')}",
        f"- 核心画面：{core.get('picture')} (like {core.get('anchorWord')})",
        f"- 中文逼近：{card.get('chineseApproximation')}",
    ]
    if scenarios:
        lines.append("- 场景：")
        for scenario in scenarios[:2]:
            lines.append(
                f"  - {scenario.get('englishExample')} -> "
                f"{scenario.get('pictureExplanation')}"
            )
    return "\n".join(lines)


def build_review_pack(data: list[dict[str, Any]], words: list[str]) -> str:
    by_word = {item["word"]: item for item in data}
    missing = [word for word in words if word not in by_word]
    if missing:
        raise ValueError(f"Missing words in runner output: {', '.join(missing)}")

    lines = [
        "# Paper MVP Manual Review Pack",
        "",
        "评分说明：",
        "",
        "- 顿悟感：1=没帮助，3=能理解但普通，5=明显打通概念",
        "- 可迁移：1=不能造句，3=能模仿例句，5=能换场景自然造句",
        "- 判定：通过 / 需微调 / 失败",
        "",
    ]

    for word in words:
        item = by_word[word]
        lines.append(f"## {word}")
        lines.append("")
        for index, run in enumerate(item["runs"], 1):
            parsed = run.get("parsed") or {}
            lines.append(f"### Run {index}")
            lines.append("")
            lines.append(card_summary(parsed))
            lines.append("")
            lines.append("| 项目 | 分数 / 结论 | 备注 |")
            lines.append("|---|---|---|")
            lines.append("| 核心画面命中 |  |  |")
            lines.append("| 顿悟感 1-5 |  |  |")
            lines.append("| 可迁移 1-5 |  |  |")
            lines.append("| 是否像释义换皮 |  |  |")
            lines.append("| 判定 |  |  |")
            lines.append("")
        lines.append("### 三轮稳定性结论")
        lines.append("")
        lines.append("- 是否稳定：")
        lines.append("- 是否需要 v4：")
        lines.append("- 结论：")
        lines.append("")

    return "\n".join(lines)


def main() -> int:
    parser = argparse.ArgumentParser(description="Generate a manual review pack.")
    parser.add_argument("--input", type=Path, default=None, help="Runner JSON path. Defaults to latest paper_runs JSON.")
    parser.add_argument("--output", type=Path, default=None, help="Output markdown path.")
    parser.add_argument("--word", action="append", default=[], help="Word/phrase to include. Can repeat.")
    args = parser.parse_args()

    input_path = args.input or latest_runner_json()
    data = json.loads(input_path.read_text(encoding="utf-8"))
    words = args.word or DEFAULT_REVIEW_WORDS
    output = build_review_pack(data, words)

    output_path = args.output or input_path.with_name(input_path.stem + "-review-pack.md")
    output_path.write_text(output, encoding="utf-8")
    print(output_path)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
