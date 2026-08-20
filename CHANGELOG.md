<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# k6 Companion Changelog

## [Unreleased]

## [0.1.2]

### Fixed

- Tool window no longer shows the generic platform icon in the sidebar —
  the real Gap Hunter Labs mark is now declared via `icon=` on
  `<toolWindow>`.

## [0.1.1]

### Fixed

- Tool window content ("Open k6 --summary-export JSON..." button and
  output area) was rendering flush against the tool window's own
  border, with no margin — fixed with an 8px empty border on the root
  panel.

## [0.1.0]

### Added

- k6 Run Configuration with path-macro expansion for both the script and
  executable paths.
- Process output rendered through the platform's native `ConsoleView`.
- "k6 Summary" tool window: parses a local `--summary-export` JSON file
  and lists each threshold's pass/fail result.

[Unreleased]: https://github.com/GapHunterLabs/k6-companion/compare/0.1.1...HEAD
[0.1.1]: https://github.com/GapHunterLabs/k6-companion/compare/0.1.0...0.1.1
[0.1.0]: https://github.com/GapHunterLabs/k6-companion/commits/0.1.0
