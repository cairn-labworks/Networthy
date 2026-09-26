# Networthy — marketing screenshots

Store / landing-page promo screenshots in a **dark & colorful** style
(inspired by the framing of [Streak](https://github.com/InlitX/streak)).
Each poster is a 1080×1920 composite (rendered at 2× → 2160×3840) with a badge,
headline, subtitle, a device mockup of the app, floating feature callouts and a
bottom brand/feature strip.

| File | Screen | Headline |
|------|--------|----------|
| `screenshots/01-networth.png`   | Home            | Your wealth. One glance. |
| `screenshots/02-statistics.png` | Statistics      | Numbers, not guesswork. |
| `screenshots/03-currency.png`   | Home (expanded) | Every currency. One total. |
| `screenshots/04-private.png`    | Lock screen     | Private by design. |
| `screenshots/05-portfolios.png` | Portfolios      | Many portfolios. One you. |
| `screenshots/06-backup.png`     | Export          | Your data. Your keys. |

## Regenerating

The posters are generated from a single self-contained HTML file and rendered
with headless Chrome/Edge (no build step or network needed).

```powershell
# generator: marketing/generate.html  (append ?p=<0-5> to render one poster)
$chrome = "C:\Program Files\Google\Chrome\Application\chrome.exe"
$src    = "marketing\generate.html"
0..5 | ForEach-Object {
  & $chrome --headless=new --disable-gpu --hide-scrollbars `
    --force-device-scale-factor=2 --window-size=1080,1920 `
    --screenshot="screenshots\poster-$_.png" "file:///$($src -replace '\\','/')?p=$_"
}
```

Edit headlines, captions, callouts and the app-screen mockups in the `POSTERS`
and screen-builder functions inside the generator, then re-run.

> Note: these depict the proposed dark + colorful theme. Once that theme ships in
> the app, replace these mockups with real on-device captures for full accuracy.
