import json
import os

tag  = os.environ.get("TAG", "")
body = os.environ.get("BODY", "")

with open("/tmp/release.json", "r", encoding="utf-8") as f:
    release = json.load(f)

assets  = release.get("assets", [])
apk_url = next(
    (a["browser_download_url"] for a in assets if a["name"].endswith(".apk")),
    ""
)

data = {
    "title":   f"Ghost IDE {tag}",
    "massges": body,
    "version": tag,
    "link":    apk_url,
    "appname": "Ghost IDE"
}

with open("update.json", "w", encoding="utf-8") as f:
    json.dump(data, f, ensure_ascii=False, indent=2)

print(json.dumps(data, ensure_ascii=False, indent=2))