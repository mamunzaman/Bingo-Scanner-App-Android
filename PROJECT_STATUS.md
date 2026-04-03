# Project status

**Last update:** 2026-04-02 (ImportTicketImageOcr grid-only rollback)

## Completed features

- **Import ticket scan UI count**: `finalUiGridRowMajor` normalizes OCR row-major to 25 cells in `setScanResult`; `displayedCount` / quality card / History photo import summary use non-zero count on that grid only; debug log `ImportTicketFinalUi` at VM handoff.
- **`ImportTicketImageOcr`**: removed LOS/serial strip experiments, `LeftStripMetaOcr`, regex meta merge, and dedicated left-strip ML Kit panel OCR; grid pipeline unchanged (consensus, highlight, deskew, weak header path uses grid crop only). Single `ImportTicketOcr` log line: `finalRowMajor`, `displayedCount`, `distinctDisplayedCount`.

## In progress

- (none)

## Pending tasks

- Rescan tickets; confirm `ImportTicketOcr` / `ImportTicketFinalUi` logs match visible grid.
- Optional OCR threshold tuning.
