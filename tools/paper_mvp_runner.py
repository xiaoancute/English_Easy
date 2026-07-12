#!/usr/bin/env python3
"""
Paper MVP stability runner.

Runs the current system prompt against the benchmark set, records raw outputs,
and writes a markdown report plus a JSON artifact for later review.
"""

from __future__ import annotations

import argparse
import dataclasses
import datetime as dt
import json
import os
import sys
import textwrap
import urllib.error
import urllib.parse
import urllib.request
from pathlib import Path
from typing import Any, Optional


ROOT = Path(__file__).resolve().parents[1]
PROMPT_PATH = ROOT / "docs/system_prompt_v3.md"
KOTLIN_PROMPT_PATH = ROOT / "app/src/main/java/io/github/xiaoancute/englisheasy/data/prompt/SystemPrompt.kt"
DEFAULT_OUTPUT_DIR = ROOT / "docs/paper_runs"
DEFAULT_ENV_FILE = ROOT / "paper_mvp.env"
MINIMAL_REGRESSION = {
    "run",
    "spring",
    "bank",
    "get",
    "break the ice",
    "red apple",
}

PROVIDER_BASE_URLS = {
    "openai": "https://api.openai.com/v1",
    "openai-compatible": "https://api.openai.com/v1",
    "gemini": "https://generativelanguage.googleapis.com/v1beta",
    "anthropic": "https://api.anthropic.com",
}


@dataclasses.dataclass(frozen=True)
class Sample:
    word: str
    expected_entry_type: str
    expected_branch_mode: str
    note: str
    expected_branch_type: Optional[str] = None


@dataclasses.dataclass(frozen=True)
class ProviderRunConfig:
    provider: str
    api_key: str
    base_url: str
    model: str


SAMPLES: list[Sample] = [
    Sample("run", "WORD", "NONE", "不能误拆语义簇"),
    Sample("spring", "WORD", "NONE", "四个中文译法要统一"),
    Sample("frame", "WORD", "NONE", "`框 / 陷害 / 构建问题` 要统一"),
    Sample("address", "WORD", "NONE", "`地址 / 演讲 / 处理` 要统一"),
    Sample("available", "WORD", "NONE", "物、人、时间要统一"),
    Sample("charge", "WORD", "NONE", "收费 / 指控 / 冲锋 / 充电"),
    Sample("issue", "WORD", "NONE", "问题 / 发布 / 期刊号"),
    Sample("draw", "WORD", "NONE", "画 / 拉 / 吸引 / 平局"),
    Sample("bank", "WORD", "BRANCH", "河岸 vs 银行不能强行统一", "HOMONYM"),
    Sample("light", "WORD", "BRANCH", "光 vs 轻不能强行统一", "HOMONYM"),
    Sample("bear", "WORD", "BRANCH", "熊 vs 承受要分清", "HOMONYM"),
    Sample("get", "WORD", "BRANCH", "获取 vs 到达/变成", "SEMANTIC_CLUSTER"),
    Sample("take", "WORD", "BRANCH", "携带 / 接受 / 花费等", "SEMANTIC_CLUSTER"),
    Sample("set", "WORD", "BRANCH", "放置 / 设定 / 固定", "SEMANTIC_CLUSTER"),
    Sample("make", "WORD", "BRANCH", "制造 / 使变成", "SEMANTIC_CLUSTER"),
    Sample("break the ice", "FIXED_PHRASE", "NONE", "不能逐词解释成破冰"),
    Sample("kick the bucket", "FIXED_PHRASE", "NONE", "不能逐词解释"),
    Sample("take a break", "FIXED_PHRASE", "NONE", "解释整体动作画面"),
    Sample("on the same page", "FIXED_PHRASE", "NONE", "共同理解状态"),
    Sample("out of the blue", "FIXED_PHRASE", "NONE", "突然出现"),
    Sample("red apple", "FREE_COMBINATION", "NONE", "不能胡编深层含义"),
    Sample("cold water", "FREE_COMBINATION", "NONE", "不能当习语处理"),
    Sample("new phone", "FREE_COMBINATION", "NONE", "自由组合"),
    Sample("hold", "WORD", "NONE", "尽量找统一画面"),
    Sample("stand", "WORD", "NONE", "站立 / 忍受 / 立场"),
    Sample("turn", "WORD", "NONE", "转动 / 轮到 / 变成"),
    Sample("pick", "WORD", "NONE", "选择 / 摘取 / 挑剔"),
    Sample("carry", "WORD", "NONE", "携带 / 支撑 / 承载意义"),
    Sample("subtle", "WORD", "NONE", "微妙 / 不易察觉 / 不露痕迹"),
    Sample("compromise", "WORD", "NONE", "妥协 / 损害 / 被攻破"),
]


def read_system_prompt() -> str:
    if KOTLIN_PROMPT_PATH.exists():
        kotlin_source = KOTLIN_PROMPT_PATH.read_text(encoding="utf-8")
        start_marker = 'val SYSTEM_PROMPT_V3 = """'
        end_marker = '""".trimIndent()'
        start = kotlin_source.find(start_marker)
        end = kotlin_source.find(end_marker, start + len(start_marker))
        if start != -1 and end != -1:
            return kotlin_source[start + len(start_marker) : end].strip()

    if PROMPT_PATH.exists():
        return PROMPT_PATH.read_text(encoding="utf-8")

    raise FileNotFoundError(
        f"Missing prompt files: {KOTLIN_PROMPT_PATH} and {PROMPT_PATH}"
    )


def normalize_entry(value: str) -> str:
    return " ".join(value.strip().lower().split())


def load_env_file(path: Path) -> None:
    if not path.exists():
        return
    for line in path.read_text(encoding="utf-8").splitlines():
        stripped = line.strip()
        if not stripped or stripped.startswith("#") or "=" not in stripped:
            continue
        key, value = stripped.split("=", 1)
        key = key.strip()
        value = value.strip().strip('"').strip("'")
        if key and key not in os.environ:
            os.environ[key] = value


def getenv_first(keys: list[str]) -> str:
    for key in keys:
        value = os.environ.get(key, "").strip()
        if value:
            return value
    return ""


def normalize_provider(value: str) -> str:
    provider = value.strip().lower()
    if provider in ("openai_compatible", "openai-compatible", "compatible"):
        return "openai-compatible"
    if provider in ("openai", "gemini", "anthropic"):
        return provider
    raise ValueError(
        "ENG_EASY_PROVIDER must be one of: openai, openai-compatible, gemini, anthropic"
    )


def provider_default_base_url(provider: str) -> str:
    return PROVIDER_BASE_URLS[provider]


def load_provider_config(*, suffix: str = "") -> Optional[ProviderRunConfig]:
    provider_key = f"ENG_EASY_PROVIDER{suffix}"
    api_key_key = f"ENG_EASY_API_KEY{suffix}"
    base_url_key = f"ENG_EASY_BASE_URL{suffix}"
    model_key = f"ENG_EASY_MODEL{suffix}"

    if suffix and not any(
        os.environ.get(key, "").strip()
        for key in (provider_key, api_key_key, base_url_key, model_key)
    ):
        return None

    provider = normalize_provider(os.environ.get(provider_key, "").strip() or "openai")
    provider_upper = provider.replace("-", "_").upper()

    api_key = getenv_first(
        [
            api_key_key,
            f"ENG_EASY_{provider_upper}_API_KEY",
            "ENG_EASY_API_KEY",
        ]
    )
    model = getenv_first([model_key, "ENG_EASY_MODEL"])
    base_url = getenv_first(
        [
            base_url_key,
            f"ENG_EASY_{provider_upper}_BASE_URL",
            "ENG_EASY_BASE_URL",
        ]
    ) or provider_default_base_url(provider)

    if not api_key or not model:
        raise ValueError(
            f"Missing config for provider {provider}: set {api_key_key} and {model_key} "
            "or provider-specific key aliases."
        )
    return ProviderRunConfig(
        provider=provider,
        api_key=api_key,
        base_url=base_url,
        model=model,
    )


def select_samples(
    *,
    sample_names: list[str],
    minimal_regression: bool,
    limit: Optional[int],
) -> list[Sample]:
    selected = SAMPLES
    if minimal_regression:
        selected = [sample for sample in selected if normalize_entry(sample.word) in MINIMAL_REGRESSION]
    if sample_names:
        wanted = {normalize_entry(name) for name in sample_names}
        known = {normalize_entry(sample.word) for sample in SAMPLES}
        unknown = sorted(wanted - known)
        if unknown:
            raise ValueError(f"Unknown sample(s): {', '.join(unknown)}")
        selected = [sample for sample in selected if normalize_entry(sample.word) in wanted]
    if limit is not None:
        selected = selected[:limit]
    return selected


def extract_json_text(raw: str) -> Optional[str]:
    start = raw.find("{")
    end = raw.rfind("}")
    if start == -1 or end == -1 or end <= start:
        return None
    return raw[start : end + 1]


def request_json(
    *,
    url: str,
    payload: dict[str, Any],
    headers: dict[str, str],
    timeout_seconds: int,
) -> dict[str, Any]:
    data = json.dumps(payload, ensure_ascii=False).encode("utf-8")
    request = urllib.request.Request(
        url,
        data=data,
        headers=headers,
        method="POST",
    )
    try:
        with urllib.request.urlopen(request, timeout=timeout_seconds) as response:
            body = response.read().decode("utf-8")
    except urllib.error.HTTPError as exc:
        body = exc.read().decode("utf-8", errors="replace")
        raise RuntimeError(f"HTTP {exc.code}: {body}") from exc
    except urllib.error.URLError as exc:
        raise RuntimeError(f"Network error: {exc.reason}") from exc
    return json.loads(body)


def call_openai_compatible(
    *,
    cfg: ProviderRunConfig,
    system_prompt: str,
    user_prompt: str,
    temperature: float,
    timeout_seconds: int,
) -> str:
    response = request_json(
        url=f"{cfg.base_url.rstrip('/')}/chat/completions",
        payload={
            "model": cfg.model,
            "messages": [
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": user_prompt},
            ],
            "response_format": {"type": "json_object"},
            "temperature": temperature,
        },
        headers={
            "Authorization": f"Bearer {cfg.api_key}",
            "Content-Type": "application/json",
        },
        timeout_seconds=timeout_seconds,
    )
    return response["choices"][0]["message"]["content"]


def call_gemini(
    *,
    cfg: ProviderRunConfig,
    system_prompt: str,
    user_prompt: str,
    temperature: float,
    timeout_seconds: int,
) -> str:
    model_path = cfg.model if cfg.model.startswith("models/") else f"models/{cfg.model}"
    model_path = urllib.parse.quote(model_path, safe="/")
    url = f"{cfg.base_url.rstrip('/')}/{model_path}:generateContent"
    url = f"{url}?key={urllib.parse.quote(cfg.api_key)}"
    response = request_json(
        url=url,
        payload={
            "systemInstruction": {
                "parts": [{"text": system_prompt}],
            },
            "contents": [
                {
                    "role": "user",
                    "parts": [{"text": user_prompt}],
                }
            ],
            "generationConfig": {
                "temperature": temperature,
                "responseMimeType": "application/json",
            },
        },
        headers={"Content-Type": "application/json"},
        timeout_seconds=timeout_seconds,
    )
    parts = response["candidates"][0]["content"].get("parts", [])
    return "".join(part.get("text", "") for part in parts)


def call_anthropic(
    *,
    cfg: ProviderRunConfig,
    system_prompt: str,
    user_prompt: str,
    temperature: float,
    timeout_seconds: int,
    max_tokens: int,
) -> str:
    response = request_json(
        url=f"{cfg.base_url.rstrip('/')}/v1/messages",
        payload={
            "model": cfg.model,
            "max_tokens": max_tokens,
            "temperature": temperature,
            "system": system_prompt,
            "messages": [
                {
                    "role": "user",
                    "content": user_prompt,
                }
            ],
        },
        headers={
            "x-api-key": cfg.api_key,
            "anthropic-version": "2023-06-01",
            "Content-Type": "application/json",
        },
        timeout_seconds=timeout_seconds,
    )
    blocks = response.get("content", [])
    return "".join(block.get("text", "") for block in blocks if block.get("type") == "text")


def call_model(
    *,
    cfg: ProviderRunConfig,
    system_prompt: str,
    user_prompt: str,
    temperature: float,
    timeout_seconds: int,
    max_tokens: int,
) -> str:
    if cfg.provider in ("openai", "openai-compatible"):
        return call_openai_compatible(
            cfg=cfg,
            system_prompt=system_prompt,
            user_prompt=user_prompt,
            temperature=temperature,
            timeout_seconds=timeout_seconds,
        )
    if cfg.provider == "gemini":
        return call_gemini(
            cfg=cfg,
            system_prompt=system_prompt,
            user_prompt=user_prompt,
            temperature=temperature,
            timeout_seconds=timeout_seconds,
        )
    if cfg.provider == "anthropic":
        return call_anthropic(
            cfg=cfg,
            system_prompt=system_prompt,
            user_prompt=user_prompt,
            temperature=temperature,
            timeout_seconds=timeout_seconds,
            max_tokens=max_tokens,
        )
    raise ValueError(f"Unsupported provider: {cfg.provider}")


def parse_card(raw_text: str) -> tuple[Optional[dict[str, Any]], str]:
    json_text = extract_json_text(raw_text)
    if json_text is None:
        return None, "response does not contain a JSON object"
    try:
        return json.loads(json_text), ""
    except json.JSONDecodeError as exc:
        return None, f"invalid JSON: {exc}"


def branch_type_ok(parsed: dict[str, Any], sample: Sample) -> bool:
    if sample.expected_branch_type is None:
        return True
    branches = parsed.get("branches")
    if not isinstance(branches, list) or not branches:
        return False
    return all(branch.get("type") == sample.expected_branch_type for branch in branches)


def card_shape_ok(parsed: dict[str, Any], sample: Sample) -> bool:
    content_fields = [
        parsed.get("coreConcept"),
        parsed.get("chineseApproximation"),
        parsed.get("scenarios"),
        parsed.get("misconceptions"),
    ]
    has_branches = parsed.get("branches") not in (None, [])
    if sample.expected_branch_mode == "BRANCH":
        return has_branches and all(value is None for value in content_fields)
    return (
        not has_branches
        and isinstance(parsed.get("coreConcept"), dict)
        and isinstance(parsed.get("chineseApproximation"), str)
        and isinstance(parsed.get("scenarios"), list)
        and isinstance(parsed.get("misconceptions"), list)
    )


def run_once(
    *,
    system_prompt: str,
    cfg: ProviderRunConfig,
    sample: Sample,
    temperature: float,
    timeout_seconds: int,
    max_tokens: int,
) -> dict[str, Any]:
    normalized_word = normalize_entry(sample.word)
    raw_text = call_model(
        cfg=cfg,
        system_prompt=system_prompt,
        user_prompt=normalized_word,
        temperature=temperature,
        timeout_seconds=timeout_seconds,
        max_tokens=max_tokens,
    )
    parsed, parse_error = parse_card(raw_text)
    entry_type_ok = False
    branch_ok = False
    branch_kind_ok = False
    shape_ok = False
    prompt_version_ok = False
    if parsed is not None:
        entry_type_ok = parsed.get("entryType") == sample.expected_entry_type
        has_branches = parsed.get("branches") not in (None, [])
        branch_ok = (sample.expected_branch_mode == "BRANCH" and has_branches) or (
            sample.expected_branch_mode == "NONE" and not has_branches
        )
        branch_kind_ok = branch_type_ok(parsed, sample)
        shape_ok = card_shape_ok(parsed, sample)
        prompt_version_ok = parsed.get("promptVersion") == 3
    return {
        "provider": cfg.provider,
        "model": cfg.model,
        "raw": raw_text,
        "parsed": parsed,
        "parse_error": parse_error,
        "json_ok": parsed is not None,
        "entry_type_ok": entry_type_ok,
        "branch_ok": branch_ok,
        "branch_type_ok": branch_kind_ok,
        "shape_ok": shape_ok,
        "prompt_version_ok": prompt_version_ok,
    }


def failed_run(cfg: ProviderRunConfig, error: Exception) -> dict[str, Any]:
    return {
        "provider": cfg.provider,
        "model": cfg.model,
        "raw": "",
        "parsed": None,
        "parse_error": str(error),
        "json_ok": False,
        "entry_type_ok": False,
        "branch_ok": False,
        "branch_type_ok": False,
        "shape_ok": False,
        "prompt_version_ok": False,
    }


def format_run_block(run: dict[str, Any], index: int) -> str:
    parsed = run["parsed"]
    parsed_text = json.dumps(parsed, ensure_ascii=False, indent=2) if parsed is not None else ""
    raw_text = run["raw"].strip()
    return textwrap.dedent(
        f"""\
        #### Run {index}

        Provider：{run["provider"]}
        模型：{run["model"]}

        原始输出：

        ````json
        {raw_text}
        ````

        解析结果：

        ````json
        {parsed_text}
        ````

        判定：

        - JSON 合法：{'是' if run['json_ok'] else '否'}
        - entryType 正确：{'是' if run['entry_type_ok'] else '否'}
        - 是否误拆分支：{'否' if run['branch_ok'] else '是'}
        - 分支类型正确：{'是' if run['branch_type_ok'] else '否'}
        - 卡片形态正确：{'是' if run['shape_ok'] else '否'}
        - promptVersion 正确：{'是' if run['prompt_version_ok'] else '否'}
        - 核心画面是否命中：待人工填写
        - 是否像中文释义换皮：待人工填写
        - 是否帮助迁移造句：待人工填写
        - 失败归因：{run['parse_error'] or '待人工填写'}
        - 备注：\n"""
    )


def build_report(
    *,
    runs_by_sample: list[tuple[Sample, list[dict[str, Any]]]],
    prompt_version: int,
    primary_cfg: ProviderRunConfig,
    alt_cfg: Optional[ProviderRunConfig],
    temperature: float,
) -> str:
    today = dt.datetime.now().strftime("%Y-%m-%d %H:%M")
    lines = [
        f"# Paper MVP Stability Run",
        "",
        f"- 生成时间：{today}",
        f"- Prompt 版本：v{prompt_version}",
        f"- 主 Provider：{primary_cfg.provider}",
        f"- 主模型：{primary_cfg.model}",
        f"- 对照 Provider：{alt_cfg.provider if alt_cfg else '无'}",
        f"- 对照模型：{alt_cfg.model if alt_cfg else '无'}",
        f"- 温度：{temperature}",
        f"- 样本数：{len(runs_by_sample)}",
        "",
    ]

    total = 0
    json_ok = 0
    entry_ok = 0
    branch_ok = 0
    branch_type_ok_count = 0
    shape_ok_count = 0
    prompt_version_ok_count = 0

    for sample, runs in runs_by_sample:
        lines.append(f"## {sample.word}")
        lines.append("")
        lines.append(f"- 预期：{sample.expected_entry_type} / {sample.expected_branch_mode}")
        lines.append(f"- 备注：{sample.note}")
        lines.append("")

        for idx, run in enumerate(runs, 1):
            total += 1
            json_ok += int(run["json_ok"])
            entry_ok += int(run["entry_type_ok"])
            branch_ok += int(run["branch_ok"])
            branch_type_ok_count += int(run["branch_type_ok"])
            shape_ok_count += int(run["shape_ok"])
            prompt_version_ok_count += int(run["prompt_version_ok"])
            lines.append(format_run_block(run, idx))

        lines.append("#### 稳定性结论")
        lines.append("")
        lines.append("- 核心画面是否一致：待人工填写")
        lines.append("- 是否需要 v4 修正：待人工填写")
        lines.append("- 顿悟感 1-5：待人工填写")
        lines.append("- 可迁移 1-5：待人工填写")
        lines.append("- 下一步：待人工填写")
        lines.append("")

    lines.extend(
        [
            "## 自动汇总",
            "",
            f"- JSON 合法率：{json_ok}/{total} = {json_ok / total:.1%}" if total else "- JSON 合法率：无数据",
            f"- entryType 正确率：{entry_ok}/{total} = {entry_ok / total:.1%}" if total else "- entryType 正确率：无数据",
            f"- 分支判断正确率：{branch_ok}/{total} = {branch_ok / total:.1%}" if total else "- 分支判断正确率：无数据",
            f"- 分支类型正确率：{branch_type_ok_count}/{total} = {branch_type_ok_count / total:.1%}" if total else "- 分支类型正确率：无数据",
            f"- 卡片形态正确率：{shape_ok_count}/{total} = {shape_ok_count / total:.1%}" if total else "- 卡片形态正确率：无数据",
            f"- promptVersion 正确率：{prompt_version_ok_count}/{total} = {prompt_version_ok_count / total:.1%}" if total else "- promptVersion 正确率：无数据",
            "",
        ]
    )
    return "\n".join(lines)


def main() -> int:
    parser = argparse.ArgumentParser(description="Run the Paper MVP benchmark set.")
    parser.add_argument("--output-dir", default=str(DEFAULT_OUTPUT_DIR), help="Directory for generated reports.")
    parser.add_argument("--env-file", default=str(DEFAULT_ENV_FILE), help="Optional env file with ENG_EASY_* values.")
    parser.add_argument("--sample", action="append", default=[], help="Run one named sample. Can be used multiple times.")
    parser.add_argument("--minimal-regression", action="store_true", help="Run the six-sample prompt regression set.")
    parser.add_argument("--limit", type=int, help="Run only the first N selected samples.")
    parser.add_argument("--runs", type=int, default=3, help="Runs per sample. Default: 3.")
    parser.add_argument("--temperature", type=float, default=0.7)
    parser.add_argument("--timeout", type=int, default=90)
    parser.add_argument("--max-tokens", type=int, default=4096, help="Max output tokens for Anthropic.")
    parser.add_argument("--dry-run", action="store_true", help="Print selected samples and exit without calling the API.")
    parser.add_argument("--repair-on-fail", action="store_true", help="Retry once if a run fails to parse.")
    args = parser.parse_args()

    if args.runs <= 0:
        print("--runs must be greater than 0", file=sys.stderr)
        return 1
    if args.limit is not None and args.limit <= 0:
        print("--limit must be greater than 0", file=sys.stderr)
        return 1

    try:
        selected_samples = select_samples(
            sample_names=args.sample,
            minimal_regression=args.minimal_regression,
            limit=args.limit,
        )
    except ValueError as exc:
        print(exc, file=sys.stderr)
        return 1

    if args.dry_run:
        for sample in selected_samples:
            print(f"{sample.word}\t{sample.expected_entry_type}\t{sample.expected_branch_mode}")
        return 0

    load_env_file(Path(args.env_file))

    try:
        primary_cfg = load_provider_config()
        alt_cfg = load_provider_config(suffix="_ALT")
    except ValueError as exc:
        print(str(exc), file=sys.stderr)
        return 1

    system_prompt = read_system_prompt()
    output_dir = Path(args.output_dir)
    output_dir.mkdir(parents=True, exist_ok=True)

    timestamp = dt.datetime.now().strftime("%Y%m%d-%H%M%S")
    runs_by_sample: list[tuple[Sample, list[dict[str, Any]]]] = []
    report_payload: list[dict[str, Any]] = []

    for sample in selected_samples:
        sample_runs = []
        configs: list[ProviderRunConfig] = [primary_cfg] * args.runs
        if alt_cfg and args.runs >= 2:
            configs = [primary_cfg] * (args.runs - 1) + [alt_cfg]

        for run_index, cfg in enumerate(configs, 1):
            print(
                f"[{sample.word}] run {run_index}/{len(configs)} "
                f"with {cfg.provider}/{cfg.model}",
                file=sys.stderr,
            )
            try:
                run = run_once(
                    system_prompt=system_prompt,
                    cfg=cfg,
                    sample=sample,
                    temperature=args.temperature,
                    timeout_seconds=args.timeout,
                    max_tokens=args.max_tokens,
                )
            except Exception as exc:
                run = failed_run(cfg, exc)
            if args.repair_on_fail and not run["json_ok"]:
                first_attempt = run
                try:
                    retry = run_once(
                        system_prompt=system_prompt,
                        cfg=cfg,
                        sample=sample,
                        temperature=args.temperature,
                        timeout_seconds=args.timeout,
                        max_tokens=args.max_tokens,
                    )
                except Exception as exc:
                    retry = failed_run(cfg, exc)
                retry["first_attempt"] = first_attempt
                run = retry
            sample_runs.append(run)

        runs_by_sample.append((sample, sample_runs))
        report_payload.append(
            {
                "word": sample.word,
                "expected_entry_type": sample.expected_entry_type,
                "expected_branch_mode": sample.expected_branch_mode,
                "runs": sample_runs,
            }
        )

    report = build_report(
        runs_by_sample=runs_by_sample,
        prompt_version=3,
        primary_cfg=primary_cfg,
        alt_cfg=alt_cfg,
        temperature=args.temperature,
    )

    report_path = output_dir / f"paper-mvp-{timestamp}.md"
    json_path = output_dir / f"paper-mvp-{timestamp}.json"
    report_path.write_text(report, encoding="utf-8")
    json_path.write_text(
        json.dumps(report_payload, ensure_ascii=False, indent=2),
        encoding="utf-8",
    )

    print(report_path)
    print(json_path)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
