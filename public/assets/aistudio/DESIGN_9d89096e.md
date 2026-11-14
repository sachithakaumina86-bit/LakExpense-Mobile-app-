---
name: Emerald Vault
colors:
  surface: '#0c1321'
  surface-dim: '#0c1321'
  surface-bright: '#323949'
  surface-container-lowest: '#070e1c'
  surface-container-low: '#151b2a'
  surface-container: '#19202e'
  surface-container-high: '#232a39'
  surface-container-highest: '#2e3544'
  on-surface: '#dce2f6'
  on-surface-variant: '#bbcabf'
  inverse-surface: '#dce2f6'
  inverse-on-surface: '#2a3040'
  outline: '#86948a'
  outline-variant: '#3c4a42'
  surface-tint: '#4edea3'
  primary: '#4edea3'
  on-primary: '#003824'
  primary-container: '#10b981'
  on-primary-container: '#00422b'
  inverse-primary: '#006c49'
  secondary: '#62dcad'
  on-secondary: '#003827'
  secondary-container: '#18a479'
  on-secondary-container: '#003121'
  tertiary: '#45dfa4'
  on-tertiary: '#003825'
  tertiary-container: '#00b982'
  on-tertiary-container: '#00422c'
  error: '#ffb4ab'
  on-error: '#690005'
  error-container: '#93000a'
  on-error-container: '#ffdad6'
  primary-fixed: '#6ffbbe'
  primary-fixed-dim: '#4edea3'
  on-primary-fixed: '#002113'
  on-primary-fixed-variant: '#005236'
  secondary-fixed: '#80f9c8'
  secondary-fixed-dim: '#62dcad'
  on-secondary-fixed: '#002115'
  on-secondary-fixed-variant: '#00513a'
  tertiary-fixed: '#68fcbf'
  tertiary-fixed-dim: '#45dfa4'
  on-tertiary-fixed: '#002114'
  on-tertiary-fixed-variant: '#005137'
  background: '#0c1321'
  on-background: '#dce2f6'
  surface-variant: '#2e3544'
typography:
  display-lg:
    fontFamily: Inter
    fontSize: 36px
    fontWeight: '700'
    lineHeight: 44px
    letterSpacing: -0.03em
  headline-lg:
    fontFamily: Inter
    fontSize: 28px
    fontWeight: '700'
    lineHeight: 36px
    letterSpacing: -0.02em
  headline-md:
    fontFamily: Inter
    fontSize: 22px
    fontWeight: '600'
    lineHeight: 28px
    letterSpacing: -0.015em
  headline-sm:
    fontFamily: Inter
    fontSize: 18px
    fontWeight: '600'
    lineHeight: 24px
    letterSpacing: -0.01em
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
    fontWeight: '500'
    lineHeight: 16px
    letterSpacing: 0.02em
  label-sm:
    fontFamily: Inter
    fontSize: 10px
    fontWeight: '600'
    lineHeight: 14px
    letterSpacing: 0.04em
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  gutter: 1rem
  margin: 1rem
  space-xs: 0.25rem
  space-sm: 0.5rem
  space-md: 1rem
  space-lg: 1.5rem
  space-xl: 2rem
---

## Brand & Style
The design system establishes a premium, authoritative, and deeply focused personal finance environment tailored for Android users in Sri Lanka. It captures the psychological state of control, precision, and financial sovereignty under the philosophy of "Every Rupee. Under Control." 

The design movement combines **Modern High-Contrast Dark Fintech** with **Subtle Luminescence**. Rather than a stark monochrome dark mode, it pairs layered deep navy tones with rich emerald gradients, precision line work, and tactile glowing borders that signify financial growth, safety, and security.

### Core Visual Principles
- **Financial Mastery & Clarity:** Dense numerical data is structured for instant readability, prioritizing monetary balances, cash-flow momentum, and localized currency formatting (`Rs.` / `LKR`).
- **Tactile Luminescence:** Vital metrics, status indicators, and active states emit a soft, controlled emerald glow (`#10B981` / `#6EE7B7`) against deep void surfaces.
- **Offline-First Sovereignty:** The interface conveys instant responsiveness, privacy, and permanence with steady, grounded layouts devoid of transient web artifacts.

## Colors
The palette leverages deep midnight navy foundations punctuated by vivid emerald and mint highlights. Every semantic color is tuned for maximum contrast against deep backdrops.

### Foundation & Surfaces
- **Canvas Base (`#0B1220`):** The primary canvas representing depth, privacy, and minimal battery consumption on OLED displays.
- **Card Surface (`#151F30`):** Primary elevated plane for modules, summary balance tiles, and interactive sections.
- **Secondary Surface (`#1D293B`):** Nested surface containers, chips, input tracks, and table rows.
- **Subtle Border Stroke (`rgba(110, 231, 183, 0.12)`): Subtle emerald-tinted containment borders.

### Accents & Semantics
- **Brand Emerald (`#10B981`):** Key interactive buttons, brand markers, charts, and primary actions.
- **Mint Accent (`#6EE7B7`):** Focus rings, active icons, selected tab indicators, and subtle luminescence highlights.
- **Income Mint (`#34D399`):** Direct visual indicator for positive transactions, inflows, savings goals, and cash surpluses.
- **Expense Red (`#F87171`):** High-clarity semantic indicator for outbound transactions, over-budget states, and alert conditions.

### Text Contrast
- **Primary Text (`#FFFFFF`):** High-emphasis financial figures, primary headlines, active button labels.
- **Secondary Text (`#94A3B8`):** Low-emphasis descriptions, date headers, secondary balance labels, and metadata.

## Typography
Typographic scale and hierarchy are structured around rapid scanning of numbers and currencies. **Inter** provides tabular numerical alignment (via OpenType features `tnum` and `cv05`), ensuring transaction values stack cleanly without jumping baselines.

### Currency & Numerics Rules
- **Prefix Consistency:** Currency format follows `Rs. 124,500.00` or `LKR 124,500.00`. The currency symbol sits at 75% opacity or a reduced font weight (`500`) relative to the primary integer value (`700`).
- **Negative Values:** Expenses explicitly prefix `- Rs.` in `#F87171`. Inflows prefix `+ Rs.` in `#34D399`.
- **Display Weights:** Hero net worth cards utilize `display-lg` with tight tracking (`-0.03em`) for a solid, institutional fintech feel.

## Layout & Spacing
Built strictly upon an **8pt spacing system** (with 4pt half-steps for micro-components like icon padding and badges), conforming directly to Android mobile interface conventions.

### Mobile Screen Structure
- **Edge Margin:** `16px` (`margin: 1rem`) screen gutter to ensure thumb reachability and prevent clipping on edge-to-edge curved Android panels.
- **Android Status Bar:** Clear top spacer (`24px` to `48px` dynamic safe-area-inset) rendering light icons against `#0B1220`.
- **Card Padding:** Internal card padding is consistently `16px` (`space-md`) for regular cards and `24px` (`space-lg`) for primary account overview cards.
- **Vertical Flow Rhythm:** Sections adhere to `24px` (`space-lg`) spacing intervals. Transaction list elements maintain `12px` to `16px` separation for comfortable tap targets (minimum 48x48dp interactive boundaries).

## Elevation & Depth
Elevation is achieved using tonal stacking combined with subtle ambient luminescence rather than heavy drop shadows.

### Elevation Hierarchy
- **Level 0 (Canvas):** `#0B1220` - Root viewport background.
- **Level 1 (Cards & Modules):** `#151F30` - Crisp boundary defined by a `1px` border of `rgba(110, 231, 183, 0.08)`.
- **Level 2 (Active Sheets & Modals):** `#1D293B` - Shadow `0 12px 32px -4px rgba(0, 0, 0, 0.6)`.
- **Luminescent Accent (Hero Cards & Floating Action):** A soft, localized radial glow: `box-shadow: 0 0 20px -2px rgba(16, 185, 129, 0.25), 0 8px 16px -4px rgba(0, 0, 0, 0.5)`. This creates a distinctive neon-lit edge without causing visual noise.

## Shapes
The shape philosophy bridges modern Android Material Design with sleek, automotive-inspired fintech softness. 

- **Primary Cards & Containers:** Standardized on `rounded-lg` (`16px` / `1rem`) to provide generous, soft touch containers.
- **Hero Balance Modules:** Utilize `rounded-xl` (`24px` / `1.5rem`) creating a focal pebble-like statement piece at the top of the dashboard.
- **Buttons & Search Inputs:** `12px` to `16px` radius.
- **Badges & Transaction Category Chips:** Fully pill-shaped (`9999px`) to visually differentiate quick metadata tags from structural interactive cards.

## Components

### Buttons
- **Primary Action (Add Transaction, Save):** Full-bleed or pill-rounded surface filled with `#10B981`, bold `#0B1220` or `#FFFFFF` text, subtle top inner-glow border, and an ambient emerald drop glow (`0 4px 14px rgba(16, 185, 129, 0.35)`).
- **Secondary / Ghost Action:** Bordered container `#151F30` with `1px` solid `rgba(110, 231, 183, 0.2)`, text in `#6EE7B7`.

### Cards & Account Tiles
- **Balance Card:** Dark `#151F30` background with a subtle linear gradient angle (`135deg, rgba(21, 31, 48, 1) 0%, rgba(29, 41, 59, 0.6) 100%`), encased in a crisp `1px` border of `rgba(110, 231, 183, 0.15)`.
- **Split Insight Tiles:** Dual income (`#34D399`) and expense (`#F87171`) mini-cards nested inside `#151F30` with soft rounded corners (`12px`) and muted 10% opacity semantic background tints.

### Chips & Filters
- **Filter Chips (Weekly, Monthly, Custom Range):** `#1D293B` background, `10px` horizontal padding, secondary text `#94A3B8`.
- **Selected State:** Border turns `#10B981` with a `15%` emerald background tint and white text (`#FFFFFF`).

### Lists & Transactions
- **List Items:** Flat structure with clean dividers (`1px` solid `rgba(255, 255, 255, 0.04)`).
- **Category Icons:** Rounded squircle (`12px`) with `#1D293B` container and emerald or ruby accent icons.
- **Amount Label:** Right-aligned tabular bold font with explicit sign prefixes (`+ Rs.` or `- Rs.`).

### Input Fields & Keypads
- **Form Inputs:** `#151F30` surface with `1px` border of `#1D293B`. On active focus, transitions to `1px solid #10B981` accompanied by a soft glow ring (`rgba(16, 185, 129, 0.2)`).
- **Numpad (Quick Entry):** High-touch 64dp key targets with haptic feedback styling, zero borders, and `#FFFFFF` numbers against transparent hit states.

### Bottom Navigation Bar
- **Android Bottom Nav:** Docked to screen bottom with fixed height (`68dp` + system navigation insets). Background `#0B1220` with a top boundary line of `rgba(255, 255, 255, 0.06)`.
- **FAB (Quick Expense Log):** Center elevated floating action button using Brand Emerald (`#10B981`) with a prominent plus icon, slightly overlapping the navigation threshold with an emerald ambient halo.