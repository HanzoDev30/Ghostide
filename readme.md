# Ghost IDE 👻


<p align="center">
  <img src="assets/banner.png" alt="Ghost IDE Banner" width="100%">
</p>

<p align="center">
  Fast IDE for Mobile Developers
</p>


[![GitHub Release](https://img.shields.io/github/v/release/HanzoDev30/Ghostide?style=for-the-badge&logo=github&logoColor=white&color=FF6B6B)](https://github.com/HanzoDev30/Ghostide/releases)
[![Downloads](https://img.shields.io/github/downloads/HanzoDev30/Ghostide/latest/total?style=for-the-badge&logo=github&logoColor=white&color=4ECDC4)](https://github.com/HanzoDev30/Ghostide/releases/latest)
[![License](https://img.shields.io/github/license/HanzoDev30/Ghostide?style=for-the-badge&logo=opensourceinitiative&logoColor=white&color=A78BFA)](LICENSE)
[![Telegram](https://img.shields.io/badge/Telegram-ghost__web__ide?style=for-the-badge&logo=telegram&logoColor=white&color=4A90E2)](https://t.me/ghost_web_ide)


## 📖 Overview

Ghost IDE is an advanced mobile development environment built for Android developers, web developers, and scripting workflows. Unlike many mobile editors, Ghost IDE provides real tooling and compiler integrations directly inside the application.


> [!WARNING]
> Ghost IDE requires **Android 11 or higher** to run.
> Android versions below Android 11 are **not supported**.


<table border="0" cellspacing="5" cellpadding="5">
  <tr>
    <td align="center" width="130">
      <img src="assets/bestfilemanager.jpg" width="100" height="130"><br>
      <b>File Manager</b><br>
      <small>Storage folders.</small>
    </td>
    <td align="center" width="130">
      <img src="assets/Installed plugins.jpg" width="100" height="130"><br>
      <b>Installed Plugins</b><br>
      <small>Active extensions.</small>
    </td>
    <td align="center" width="130">
      <img src="assets/reallsp.jpg" width="100" height="130"><br>
      <b>Code Editor</b><br>
      <small>Python env.</small>
    </td>
    <td align="center" width="130">
      <img src="assets/store.jpg" width="100" height="130"><br>
      <b>Plugin Store</b><br>
      <small>Extensions.</small>
    </td>
  </tr>
  <tr>
    <td align="center" width="130">
      <img src="assets/terminal debian.jpg" width="100" height="130"><br>
      <b>Terminal 1</b><br>
      <small>Debian OS info.</small>
    </td>
    <td align="center" width="130">
      <img src="assets/terminalcoderun.jpg" width="100" height="130"><br>
      <b>Terminal 2</b><br>
      <small>Code output.</small>
    </td>
    <td align="center" width="130">
      <img src="assets/theme editor.jpg" width="100" height="130"><br>
      <b>Theme Editor</b><br>
      <small>UI colors.</small>
    </td>
  </tr>
</table>

**Core Focus Areas:**
- ⚡ High-performance editing
- 🧩 Rich language support
- 🔧 Integrated compilers and runtime tools
- 🎨 Deep customization
- ⌨️ Physical keyboard workflows
- 💻 Modern editor experience

---

## ✨ Features

### 🖊️ Editor
- [x] Blazing-fast code editor engine
- [x] Syntax highlighting
- [x] Auto-save functionality
- [x] Multi-tab support
- [x] Code formatting(Auto format code by lsp service)
- [x] Snippets support
- [ ] Physical keyboard shortcuts
- [x] Large file handling(Page)
- [x] Custom themes
- [x] Background customization
- [x] Lsp

### 🛠️ Development Tools
- Python execution 
- PHP execution 
- Kotlin compiler
- Java helper tools 
- Sass / SCSS / Less compilers 
- TypeScript / TSX / JSX support 
- JavaFX compiler
- Git integration
- HTML preview support 
- FTP
- SFTP
- SMB

# Lsp 

- how in install lsp? [click](https://github.com/HanzoDev30/GhostIde/blob/main/Lsp.md)
- نحوه نصب زبان سرور فارسی [click](https://github.com/HanzoDev30/GhostIde/blob/main/Lspfa.md)

## Terminal 

- Note
  - If for some reason Debian packages are not installed, try this code.

```shell

rm -f /etc/resolv.conf
echo "nameserver 8.8.8.8" > /etc/resolv.conf

```

### 🌐 Supported Languages

- [x] html
- [x] css
- [x] cpp
- [x] js
- [x] python
- [x] json
- [x] markdown
- [x] sass scss
- [x] java
- [x] c
- [x] xml
- [x] kotlin
- [x] typescript
- [x] toml
- [x] gradle
- [x] yaml
- [x] lua
- [x] dart
- [x] charp
- [x] sql
- [x] go
- [x] php
- [x] tsx & jsx
- [x] rust
- [x] shell
- [x] ini
- [x] ruby
- [ ] javacc
- [x] vue
- [ ] cmake
- [x] antlr
- [x] swift 
- [x] scala
- [x] perl
- [x] julia
- [x] r
- [x] elixir
- [x] haskell
- [x] nim
- [x] solidity
- [x] asm

---

### Preview

- [x] HtmlPreview
- [x] ImagePreview
- [x] LinkPreview


## 🎨 Theme Engine

The editor supports deep UI customization, including:
- [x] Syntax colors
- [x] Backgrounds and tab styles
- [x] Autocomplete appearance
- [x] Bracket-level coloring
- [x] Selection styles

### Make your own theme

- 📘 English guide — [click here to learn how to create a theme](https://github.com/HanzoDev30/GhostIde/blob/main/ThemeMakerEn.md)
- 📙 راهنمای فارسی — برای ساخت تم [اینجا کلیک کنید](https://github.com/HanzoDev30/GhostIde/blob/main/ThemeMakerFa.md)

> Create a theme quickly: copy any `.gth` file, rename it, open it in the File Manager and choose **Edit**. The Theme Editor's 4 tabs (Activity · Editor · Widget · M3Color) let you pick every color visually.

---
## 🚀 Why Ghost IDE?

- [x] Lightweight and fast
- [x] Built natively for Android
- [x] Real compiler integrations
- [x] Plugin-ready architecture
- [x] Material Design interface
- [x] Fully open source
- [x] Optimized typing experience
- [x] Powerful customization system

---
## Code Runer🔥🔥🔥

- [x] Clang Family (c, cpp, h, hpp, cc)
- [x] Python (package-aware, runs via `python3 -m`)
- [x] PHP (php)
- [x] Go (go run)
- [x] Node.js (js)
- [x] TypeScript (ts-node)
- [x] Lua (lua5.4)
- [x] Java (javac + main detection)
- [x] Java + Gradle Wrapper (gradlew build)
- [x] Java + Maven (mvn package)
- [x] Java + Gradle (gradle build)
- [x] Java + Android classpath (android.jar + libs + gradle cache)
- [x] Kotlin (.kt → kotlinc -include-runtime → jar)
- [x] Kotlin Script (.kts → kotlinc -script)
- [x] Sass / SCSS (sass → css output)
- [x] Custom Runners (`ext:command` with `{file}`, `{dir}`, `{file_name}`, `{base}`)
- [x] Auto-install missing toolchains (apt)
- [x] ANDROID_HOME / ANDROID_SDK_ROOT export
- [x] Preference-gated runners (master switch + per-language keys)
- [x] Terminal Activity mode
- [x] Terminal Bottom Sheet mode (fresh tab every run)
- [x] Raw shell command support (`runShell`)
- [x] `isSupported(path)` extension check
- [x] `bindof(path, asBottomSheet)` entry point

### Note

- Installing packages may take a while, it varies on different mobiles. The system we use is Debian.
- To make it easier to work, it is better to learn to work with the Debian operating system so that you do not encounter problems because the best option for Android is Debian.

- [x] c
- [x] cpp
- [x] python
- [x] php
- [x] lua 
- [x] nodejs
- [x] typesctipt
- [x] go lang


## Plugin 

- how in install Plugin? [click](./Plugin.md)
- نحوه پیاده سازی پلاگین [click](/Pluginfa.md)
