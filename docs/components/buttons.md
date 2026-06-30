# Buttons

The button family. All three components share theme resolution and render the most native control per platform. They take the same variant, size, color, and menu types, so an app can move between them without relearning the API.

### NativeButton

A text button with optional leading and trailing icons, an optional loading state, and an optional pull-down menu.

**Android:** a Material 3 button — `Button` (Primary, Destructive), `FilledTonalButton` (Secondary), `TextButton` (Tertiary), or `OutlinedButton` (Outline). A menu renders as an anchored `DropdownMenu`.
**iOS:** a `UIButton` via `UIKitView`, with a custom `[leading | label | trailing]` content stack and a centered spinner. A menu uses `UIMenu` with `showsMenuAsPrimaryAction`.

**Use it when**
- You need a standard labeled action button.
- You want a pull-down menu trigger (set `menu`); a chevron is appended automatically.

**Avoid it when**
- The control is icon-only — use `NativeIconButton`.
- You need one main action plus a related menu — use `NativeSplitButton` (an iOS menu button suppresses the tap action, so `onClick` may not fire).

**Parameters**

| Parameter | Type | Default | Description |
|---|---|---|---|
| `text` | `String` | — | Button label. |
| `onClick` | `() -> Unit` | — | Tap handler. With a `menu`, also fires on Android tap; may not fire on iOS, where presenting the menu suppresses the tap action. |
| `modifier` | `Modifier` | `Modifier` | Layout modifier. |
| `variant` | `NativeButtonVariant` | `NativeButtonVariant.Primary` | Visual variant: `Primary`, `Secondary`, `Tertiary`, `Outline`, `Destructive`. |
| `size` | `NativeButtonSize` | `NativeButtonSize.Medium` | `Small`, `Medium`, or `Large`. Drives height, padding, and text style from theme tokens. |
| `shape` | `NativeButtonShape` | `NativeButtonShape.Rounded` | `Rounded` (theme corner radius) or `Pill` (capsule, radius = height / 2). |
| `enabled` | `Boolean` | `true` | When false the button is not interactive and uses themed disabled tones. |
| `loading` | `Boolean` | `false` | Shows a centered spinner; label and icons are hidden while loading. |
| `fullWidth` | `Boolean` | `false` | Stretches the button to fill the available width. |
| `leadingIcon` | `NativeIcon?` | `null` | Icon before the label. May be set together with `trailingIcon`. |
| `trailingIcon` | `NativeIcon?` | `null` | Icon after the label. May be set together with `leadingIcon`. |
| `menu` | `NativeMenu?` | `null` | When non-null, tapping opens a pull-down menu and a chevron is appended. |
| `contentPadding` | `PaddingValues?` | `null` | Overrides the size-derived content padding. |
| `cornerRadius` | `Dp?` | `null` | Explicit corner radius. Wins over `shape`. |
| `colorsOverride` | `NativeButtonColors?` | `null` | Overrides the variant's resolved colors. |
| `textStyleOverride` | `TextStyle?` | `null` | Merged over the size-derived base text style. |
| `touch` | `NativeInteropTouch` | `NativeInteropTouch.Cooperative` | iOS scroll-vs-tap interop strategy. No-op on Android. |
| `contentDescription` | `String?` | `null` | Accessibility label override. |
| `testTag` | `String?` | `null` | Maps to `testTag` (Android) / `accessibilityIdentifier` (iOS). |

**Example**

```kotlin
NativeButton("Save", onClick = { save() })

NativeButton(
    text = "Export",
    onClick = { },
    variant = NativeButtonVariant.Outline,
    trailingIcon = NativeIcon(Icons.Default.Download, sfSymbolName = "square.and.arrow.down"),
)
```

**Notes**
- `leadingIcon` and `trailingIcon` render in their true positions; both may be set at once.
- Disabled tones are derived from the theme in shared code, not hardcoded per renderer.
- On iOS the visual height stays compact (Small = 36pt) but the hit area is expanded to a HIG-safe 44pt minimum. Android reserves a 48dp target via Material's minimum interactive size.
- Text-bearing native controls honor Dynamic Type on iOS (`adjustsFontForContentSizeCategory`).
- `touch` is a documented no-op on Android, where the Material button is not embedded via UIKit interop.

### NativeIconButton

An icon-only button — a square or circular tap target with no label. Circular by default.

**Android:** a Material 3 icon button — `FilledIconButton`, `FilledTonalIconButton`, `OutlinedIconButton`, or the standard `IconButton`, keyed by variant.
**iOS:** a square `UIButton` via `UIKitView`.

**Use it when**
- A glyph alone is clear (toolbar actions, an overflow "…" button).
- You need a compact menu trigger; set `menu` and the menu opens on tap with no extra chevron.

**Avoid it when**
- The action needs a visible label — use `NativeButton`.

**Parameters**

| Parameter | Type | Default | Description |
|---|---|---|---|
| `icon` | `NativeIcon` | — | The glyph to display. |
| `onClick` | `() -> Unit` | — | Tap handler. |
| `contentDescription` | `String` | — | Required. The accessible name; an icon-only control has no visible label. Maps to `accessibilityLabel` (iOS) / merged `contentDescription` (Android). |
| `modifier` | `Modifier` | `Modifier` | Layout modifier. |
| `variant` | `NativeButtonVariant` | `NativeButtonVariant.Tertiary` | Visual variant. |
| `size` | `NativeButtonSize` | `NativeButtonSize.Medium` | `Small`, `Medium`, or `Large`. Drives the side length. |
| `enabled` | `Boolean` | `true` | When false the button is not interactive and uses themed disabled tones. |
| `loading` | `Boolean` | `false` | Shows a centered spinner; the icon is hidden while loading. |
| `menu` | `NativeMenu?` | `null` | When non-null, tapping opens a pull-down menu. No chevron is added. |
| `cornerRadius` | `Dp?` | `null` | Explicit corner radius. Default is circular (radius = side / 2). |
| `colorsOverride` | `NativeButtonColors?` | `null` | Overrides the variant's resolved colors. |
| `touch` | `NativeInteropTouch` | `NativeInteropTouch.Cooperative` | iOS scroll-vs-tap interop strategy. No-op on Android. |
| `testTag` | `String?` | `null` | Maps to `testTag` (Android) / `accessibilityIdentifier` (iOS). |

**Example**

```kotlin
NativeIconButton(
    icon = NativeIcon(Icons.Default.Add, sfSymbolName = "plus"),
    onClick = { add() },
    contentDescription = "Add",
)
```

**Notes**
- `contentDescription` is required, not optional.
- The host is clamped to a 44pt minimum on iOS (44 is its natural size).
- `touch` is a documented no-op on Android.

### NativeSplitButton

A primary action segment plus a chevron segment that opens a menu. Tapping the label fires `onPrimaryClick`; tapping the chevron presents the menu.

**Android:** two Material buttons sharing variant colors, with a hairline divider and per-side rounded corners, plus an anchored `DropdownMenu`.
**iOS:** two `UIButton`s with a divider; each segment rounds only its outer corners via `layer.maskedCorners`. The chevron presents a `UIMenu`.

**Use it when**
- A button has one main action plus related secondary actions.
- You want an action and a menu on one control (unlike a menu-bearing `NativeButton`, the primary tap always fires here).

**Avoid it when**
- There is no primary action — use `NativeButton` with a `menu`.

**Parameters**

| Parameter | Type | Default | Description |
|---|---|---|---|
| `text` | `String` | — | Label on the primary segment. |
| `onPrimaryClick` | `() -> Unit` | — | Fires when the label segment is tapped. |
| `menu` | `NativeMenu` | — | The menu opened by the chevron segment. Required. |
| `modifier` | `Modifier` | `Modifier` | Layout modifier. |
| `variant` | `NativeButtonVariant` | `NativeButtonVariant.Primary` | Visual variant shared by both segments. |
| `size` | `NativeButtonSize` | `NativeButtonSize.Medium` | `Small`, `Medium`, or `Large`. |
| `enabled` | `Boolean` | `true` | When false neither segment is interactive; uses themed disabled tones. |
| `loading` | `Boolean` | `false` | Shows a centered spinner; label and icon are hidden while loading. |
| `leadingIcon` | `NativeIcon?` | `null` | Icon before the label on the primary segment. |
| `cornerRadius` | `Dp?` | `null` | Explicit outer corner radius. Defaults to the theme corner radius. |
| `colorsOverride` | `NativeButtonColors?` | `null` | Overrides the variant's resolved colors. |
| `textStyleOverride` | `TextStyle?` | `null` | Merged over the size-derived base text style. |
| `touch` | `NativeInteropTouch` | `NativeInteropTouch.Cooperative` | iOS scroll-vs-tap interop strategy. No-op on Android. |
| `contentDescription` | `String?` | `null` | Accessibility label override. |
| `testTag` | `String?` | `null` | Maps to `testTag` (Android) / `accessibilityIdentifier` (iOS). |

**Example**

```kotlin
NativeSplitButton(
    text = "Save",
    onPrimaryClick = { save() },
    menu = NativeMenu(
        items = listOf(
            NativeMenuItem("Save as draft", onSelect = { saveDraft() }),
            NativeMenuItem("Save a copy", onSelect = { saveCopy() }),
        ),
    ),
)
```

**Notes**
- There is no single native split control on either platform; the component is composed from two segments sharing the variant colors with a hairline divider and a single rounded outer outline.
- Outline-variant split buttons show a doubled hairline at the seam.
- The host is clamped to a 44pt minimum on iOS.
- `touch` is a documented no-op on Android.

### NativeMenu

The pull-down menu attached to a button. The same `NativeMenu` drives all three button components and renders natively per platform — a `UIMenu` shown via `UIButton.showsMenuAsPrimaryAction` on iOS, an anchored Material 3 `DropdownMenu` on Android.

**`NativeMenu` fields**

| Field | Type | Default | Description |
|---|---|---|---|
| `items` | `List<NativeMenuItem>` | — | The menu rows, in order. |
| `title` | `String?` | `null` | Optional header at the top of the menu (iOS `UIMenu.title`; ignored by Android Material). |

**`NativeMenuItem` fields**

| Field | Type | Default | Description |
|---|---|---|---|
| `title` | `String` | — | Row label. |
| `onSelect` | `() -> Unit` | — | Fires when the row is selected. |
| `icon` | `NativeIcon?` | `null` | Optional leading glyph (Android uses `androidImageVector`; iOS uses `sfSymbolName`). |
| `role` | `NativeMenuItemRole` | `NativeMenuItemRole.Normal` | `Destructive` renders the row red with the native destructive style. |
| `enabled` | `Boolean` | `true` | When false the row is shown dimmed and is not selectable. |
| `selected` | `Boolean` | `false` | Marks the row as the current choice with a native checkmark. Use for single- or multi-select menus. |

**Notes**
- `NativeMenu` and `NativeMenuItem` are `@Immutable` data classes.
- A destructive item maps to `UIMenuElementAttributes.destructive` (iOS) and an error-tinted `DropdownMenuItem` (Android).
- A selected item maps to `UIMenuElementState.on` (iOS) and a trailing check (Android).
