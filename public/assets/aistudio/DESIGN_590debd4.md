---
name: Emerald Mint Light
colors:
  surface: '#faf8ff'
  surface-dim: '#d2d9f4'
  surface-bright: '#faf8ff'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f2f3ff'
  surface-container: '#eaedff'
  surface-container-high: '#e2e7ff'
  surface-container-highest: '#dae2fd'
  on-surface: '#131b2e'
  on-surface-variant: '#3c4a42'
  inverse-surface: '#283044'
  inverse-on-surface: '#eef0ff'
  outline: '#6c7a71'
  outline-variant: '#bbcabf'
  surface-tint: '#006c49'
  primary: '#006c49'
  on-primary: '#ffffff'
  primary-container: '#10b981'
  on-primary-container: '#00422b'
  inverse-primary: '#4edea3'
  secondary: '#006c4a'
  on-secondary: '#ffffff'
  secondary-container: '#82f5c1'
  on-secondary-container: '#00714e'
  tertiary: '#006c4b'
  on-tertiary: '#ffffff'
  tertiary-container: '#00b982'
  on-tertiary-container: '#00422c'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#6ffbbe'
  primary-fixed-dim: '#4edea3'
  on-primary-fixed: '#002113'
  on-primary-fixed-variant: '#005236'
  secondary-fixed: '#85f8c4'
  secondary-fixed-dim: '#68dba9'
  on-secondary-fixed: '#002114'
  on-secondary-fixed-variant: '#005137'
  tertiary-fixed: '#68fcbf'
  tertiary-fixed-dim: '#45dfa4'
  on-tertiary-fixed: '#002114'
  on-tertiary-fixed-variant: '#005137'
  background: '#faf8ff'
  on-background: '#131b2e'
  surface-variant: '#dae2fd'
typography:
  display-lg:
    fontFamily: Inter
    fontSize: 40px
    fontWeight: '700'
    lineHeight: 48px
    letterSpacing: -0.02em
  display-lg-mobile:
    fontFamily: Inter
    fontSize: 32px
    fontWeight: '700'
    lineHeight: 38px
    letterSpacing: -0.015em
  headline-lg:
    fontFamily: Inter
    fontSize: 28px
    fontWeight: '600'
    lineHeight: 36px
    letterSpacing: -0.015em
  headline-md:
    fontFamily: Inter
    fontSize: 22px
    fontWeight: '600'
    lineHeight: 28px
    letterSpacing: -0.01em
  headline-sm:
    fontFamily: Inter
    fontSize: 18px
    fontWeight: '600'
    lineHeight: 24px
  body-lg:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  body-md:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  body-sm:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '400'
    lineHeight: 16px
  label-lg:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '600'
    lineHeight: 20px
    letterSpacing: 0.01em
  label-md:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '600'
    lineHeight: 16px
    letterSpacing: 0.02em
  label-sm:
    fontFamily: Inter
    fontSize: 11px
    fontWeight: '500'
    lineHeight: 14px
    letterSpacing: 0.04em
  currency-display:
    fontFamily: Inter
    fontSize: 36px
    fontWeight: '700'
    lineHeight: 44px
    letterSpacing: -0.02em
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  gutter: 1.5rem
  gutter-mobile: 1rem
  margin: 2rem
  margin-mobile: 1rem
  space-xs: 0.25rem
  space-sm: 0.5rem
  space-md: 1rem
  space-lg: 1.5rem
  space-xl: 2rem
---

## Brand & Style

This design system translates a secure, vault-grade personal wealth and expense management application into an airy, high-clarity daylight experience. Designed for financially conscious individuals, founders, and professionals who manage real-time liquidity, budgeting, and recurring allocations, the UI evokes feelings of precision, fiscal serenity, and uncompromised transparency.

The aesthetic fuses crisp modern minimalism with soft tactile glass accents:
- **Atmosphere:** Clean, clinical, yet warm and reassuring. The visual noise is suppressed so critical monetary figures, trends, and status indicators command effortless attention.
- **Tonal Structure:** Crisp off-white foundations (`#F8FAFC`) layered with pristine white cards (`#FFFFFF`), whisper-soft border delineations, and radiant mint-tinted callouts.
- **Interaction Feel:** Precise, responsive micro-interactions with gentle emerald glow transitions rather than heavy physical states, maintaining parity with its dark-mode counterpart while optimizing for daytime ambient light and high legibility.

## Colors

The palette grounds high-vibrancy botanical mint and emerald accents in structured slate neutrals. High contrast ratios ensure financial telemetry is immediately readable under direct glare.

### Core Architecture
- **Primary (`#10B981`):** The signature emerald anchor, utilized for primary actions, positive inflow movements, and core interactive states.
- **Secondary (`#059669`):** Deep emerald for hovered actions, dense chart strokes, and high-emphasis focal items.
- **Tertiary Accent (`#34D399`):** Mint highlight used for gentle data visualization gradients and secondary chips.
- **Base Canvas:** `#F8FAFC` provides a calm backdrop that eliminates optical glare associated with raw `#FFFFFF`.
- **Card Surfaces:** `#FFFFFF` on primary containers; `#F1F5F9` on secondary nested groupings and table headers.

### Financial Semantics
- **Income / Inflow:** Positive capital movement is marked with `#10B981` (stroke/text) backed by `#ECFDF5` badge fills.
- **Expense / Outflow:** Debits, deductions, and warnings use `#EF4444` / `#DC2626` text and iconography paired with `#FEF2F2` tinted backgrounds.
- **Transfers & Neutral Reallocations:** Neutral movements utilize `#3B82F6` (fill text) backed by `#EFF6FF`.
- **Text Tiers:** Primary headers and figures use `#0F172A`, body and secondary labels utilize `#64748B`, while hints, empty states, and inactive captions sit at `#94A3B8`.

## Typography

The type system is powered exclusively by `Inter` to deliver systematic clarity, tabular figure alignment, and frictionless scanning of numerical data.

- **Tabular Figures:** Always apply `font-feature-settings: 'tnum' 1, 'cv05' 1` to financial amounts, currency tables, percentages, and metrics to prevent horizontal layout jank during live data updates.
- **Hierarchy Rules:** 
  - `display-lg` and `currency-display` are reserved for net worth balances and aggregate category views.
  - `headline-*` defines module views, modal dialog titles, and card headers.
  - `label-*` handles metric delta chips, table headers (uppercase, medium tracking), and button states.
- **Contrast Disciplines:** Never drop below `#64748B` for analytical data labels. Subtle secondary text relies on size and weight shifts rather than low-contrast alpha washes.

## Layout & Spacing

A 12-column responsive fluid grid governs desktop and tablet representations, consolidating down to a single-column, bottom-anchored layout on mobile viewports.

### Breakpoints & Canvas Margins
- **Desktop (≥ 1280px):** 12 columns, `1.5rem` gutters, max canvas width `1440px` with centered auto margins (`margin: 2rem`).
- **Tablet (768px – 1279px):** 8 columns, `1.25rem` gutters, responsive canvas with `1.5rem` outer margin padding.
- **Mobile (< 768px):** 4 columns (or single stack), `1rem` gutters, `1rem` outer horizontal margins.

### Spatial Rhythm
- Interior card padding standardizes on `space-lg` (`1.5rem`) on desktop, tightening to `space-md` (`1rem`) on small viewports.
- Spacing between related metric values and label pairings must strictly use `space-xs` (`0.25rem`) or `space-sm` (`0.5rem`).
- Major analytical sections (e.g., Spending Breakdown versus Transaction Feeds) separate by `space-xl` (`2rem`).

## Elevation & Depth

In this light theme, depth is produced through hairline structural boundaries combined with soft, natural ambient shadowing.

### Tiers & Elevation Tokens
- **Base Canvas:** Flat `#F8FAFC`. Zero elevation, non-interactive foundation.
- **Card Surfaces (Resting):** Pure `#FFFFFF` resting on a compound ambient shadow:
  - `box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.06), 0 1px 2px -1px rgba(0, 0, 0, 0.04);`
  - Paired with a hairline border: `1px solid #E2E8F0` (or `rgba(15, 23, 42, 0.08)`).
- **Interactive Hovered State:**
  - `box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.07), 0 2px 4px -2px rgba(0, 0, 0, 0.05);`
  - Subtle border highlight transition: `border-color: rgba(16, 185, 129, 0.4);`
- **Floating Modals & Flyout Drawers:**
  - `box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.08), 0 8px 10px -6px rgba(0, 0, 0, 0.04);`
  - Background: `#FFFFFF` with frosted backdrop blur `backdrop-filter: blur(12px);` on the overlay shield (`rgba(15, 23, 42, 0.4)`).

## Shapes

The geometric identity balances modern precision with soft, accessible radii that soften analytical dashboards:

- **Cards & Primary Modules:** Configured with `20px` corner radii (`rounded-2xl` styling) to preserve the signature aesthetic of the dark-mode vault.
- **Form Controls & Inputs:** Built with `12px` radii (`rounded-xl`), creating nested visual cohesion inside parent containers.
- **Buttons, Badges, & Filter Chips:** Rounded pill forms (`rounded-full`) for chips and selection pills; `12px` for primary/secondary action triggers.
- **Nested Ratio Consistency:** Child elements within a `20px` card container must use inner radii of `8px` to `12px` to prevent optical corner pinching.

## Components

### Buttons
- **Primary:** Background `#10B981`, text `#FFFFFF`, font weight `600`, radius `12px`. Hover state shifts to `#059669` with an emerald focus ring (`0 0 0 3px rgba(16, 185, 129, 0.25)`).
- **Secondary:** Background `#ECFDF5`, text `#059669`, border `1px solid rgba(16, 185, 129, 0.2)`. Hover background shifts to `#D1FAE5`.
- **Tertiary / Ghost:** Background transparent, text `#0F172A`. Hover background `#F1F5F9`.

### Chips & Semantic Badges
- **Inflow Chip:** Text `#059669`, background `#ECFDF5`, border `1px solid rgba(16, 185, 129, 0.2)`. Uses `+` prefix notation.
- **Outflow Chip:** Text `#DC2626`, background `#FEF2F2`, border `1px solid rgba(239, 68, 68, 0.2)`. Uses `-` prefix notation.
- **Transfer Chip:** Text `#2563EB`, background `#EFF6FF`, border `1px solid rgba(59, 130, 246, 0.2)`.

### Transaction Lists
- **Row Styling:** `#FFFFFF` background, separated by `1px solid #F1F5F9`. Hover state shifts row surface to `#F8FAFC`.
- **Icon Containers:** `40px` circular or `12px` rounded squares with tinted backgrounds matching the category nature (`#ECFDF5` for deposits, `#FEF2F2` for expenses, `#F1F5F9` for general operations).

### Checkboxes & Radio Controls
- **Unchecked:** Border `1.5px solid #CBD5E1`, background `#FFFFFF`.
- **Checked:** Background `#10B981`, border `1.5px solid #10B981`, checkmark glyph in `#FFFFFF`.

### Form Fields & Inputs
- **Base State:** Background `#FFFFFF`, border `1px solid #E2E8F0`, text `#0F172A`, placeholder `#94A3B8`, radius `12px`.
- **Focus State:** Border `#10B981`, outline none, shadow ring `0 0 0 3px rgba(16, 185, 129, 0.15)`.

### Cards & Metric Panels
- **Container Structure:** Surface `#FFFFFF`, border `1px solid #E2E8F0`, radius `20px`, internal padding `1.5rem`.
- **Header Structure:** Flex layout housing metric label (`label-md`, `#64748B`) paired with a contextual action or icon. Large amount formatted using `currency-display` (`#0F172A`).

### Charting & Visualization
- **Line & Area Curves:** Primary stroke `#10B981` (stroke-width: 2.5px), fill uses a vertical linear gradient from `rgba(16, 185, 129, 0.2)` transitioning to `rgba(16, 185, 129, 0.0)`.
- **Grid Lines:** Horizontal and vertical grid tick lines rendered in `#E2E8F0` (dashed or hairline solid).
- **Tooltip Flyouts:** Floating `#FFFFFF` container with `box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.08)`, border `1px solid #E2E8F0`, radius `12px`.