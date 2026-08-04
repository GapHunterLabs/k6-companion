# k6 Companion

A Run Configuration for [k6](https://k6.io) load test scripts, plus a
tool window for k6's `--summary-export` JSON.

## Why it exists

**k6** (JetBrains Marketplace id 16141), 81,065 downloads, freemium with a
real paid license tier, vendor Mikhail Bolotov. Real, verbatim reviewer
complaints (5 independent reviewers over ~2 years):

- *"it doesn't respect Macros (despite allowing macros to be
  used)... configuration errors saying the file does not exist"*
  (2024-05-28)
- *"Does not work with WSL at all... caused severe performance
  issues... right click a file/directory... would freeze phpstorm for a
  few seconds"* (2023-10-02)
- *"the terminal output for the 'animated progress bar' has been an
  issue for 2 years... makes the plugin nearly unusable"* (2023-09-20)
- *"there's been no work on this plugin for 2 years, it may truly be
  dead"* / *"it only supports debugging javascript"* despite advertising
  TypeScript debugging support

## Why built this way

- **Path macros are always expanded before touching the filesystem.**
  `K6CommandLineBuilder` applies `PathMacroManager.expandPath` to both
  the script path and the k6 executable path before building the
  `GeneralCommandLine` -- the platform does not do this automatically for
  a plain string field on a custom Run Configuration, which is exactly
  how the cited "configuration errors saying the file does not exist"
  bug happens.
- **The platform's own `ConsoleView`, via `CommandLineState`**, not a
  hand-rolled terminal emulator -- the direct fix for the "animated
  progress bar... issue for 2 years" complaint. Rendering process output
  is the platform's job, not this plugin's.
- **No WSL support in v1** -- not promising what the competitor doesn't
  reliably deliver either; the only UI entry points are the standard Run
  Configuration flow (already async-safe) and a tool window button that
  reads a file off the EDT, so there's no synchronous file-system probe
  on a context-menu click for a WSL-related freeze to hide in.
- **The `--summary-export` tool window is read-only and entirely
  local** -- the user runs k6 themselves with `--summary-export`, and
  this plugin just parses the resulting JSON (a small, hand-rolled
  reader scoped to k6's actual summary shape) and lists each
  threshold's pass/fail result.

## Usage

**Run Configuration:** Run > Edit Configurations > + > k6. Set the
script path (path macros like `$PROJECT_DIR$` are supported) and, if
needed, extra CLI arguments and a custom k6 executable path.

**Summary tool window:** run `k6 run --summary-export summary.json
script.js` yourself, then open the "k6 Summary" tool window and click
"Open k6 --summary-export JSON..." to see each threshold's result.

## Enterprise / Team Licensing

Need enterprise features, custom rules, or team licensing? Contact us at
**kennyj.diazm@gmail.com**.

## Development

```
./gradlew test           # unit tests
./gradlew buildPlugin    # generates build/distributions/*.zip
./gradlew verifyPlugin   # checks compatibility against real IDEs
```

## License

Apache-2.0. See `LICENSE`.
