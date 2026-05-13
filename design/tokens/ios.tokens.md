# iOS Token Mapping — CamScanner App

> Source of truth: `design/tokens/base.tokens.json`
> 1 token unit = 1 iOS pt (same as Android dp, same as Figma unit)
> Status: Ready to implement — all values extracted from Android production code

---

## Colors → `Theme/Colors.swift`

```swift
import SwiftUI

extension Color {
    // ── Backgrounds ──────────────────────────────────────────────────────────
    static let backgroundPrimary   = Color(hex: "#0D1F1E")  // Main screen bg
    static let backgroundSecondary = Color(hex: "#1A2E2D")  // Card / surface bg
    static let backgroundElevated  = Color(hex: "#1F3533")  // Dialog, bottom sheet
    static let backgroundBottomNav = Color(hex: "#111E1D")  // Bottom nav bar

    // ── Teal accent ───────────────────────────────────────────────────────────
    static let tealPrimary        = Color(hex: "#2DD4BF")   // Primary action, FAB, active
    static let tealPrimaryDark    = Color(hex: "#1BA89A")   // Pressed state
    static let tealContainer      = Color(hex: "#1A3D3A")   // Chip bg, subtle teal surface
    static let tealContainerLight = Color(hex: "#234E4A")   // Slightly lighter container

    // ── Text ──────────────────────────────────────────────────────────────────
    static let textPrimary   = Color(hex: "#FFFFFF")
    static let textSecondary = Color(hex: "#8A9E9D")
    static let textDisabled  = Color(hex: "#4A5E5D")
    static let textOnPrimary = Color(hex: "#0D1F1E")  // Text on teal bg

    // ── Borders ───────────────────────────────────────────────────────────────
    static let borderSubtle = Color(hex: "#243534")
    static let borderMedium = Color(hex: "#2E4543")

    // ── Status ────────────────────────────────────────────────────────────────
    static let statusError   = Color(hex: "#FF5252")
    static let statusSuccess = Color(hex: "#4CAF50")
    static let statusWarning = Color(hex: "#FFB74D")

    // ── Icons ─────────────────────────────────────────────────────────────────
    static let iconDefault   = Color(hex: "#8A9E9D")
    static let iconActive    = Color(hex: "#2DD4BF")
    static let iconOnPrimary = Color(hex: "#0D1F1E")

    // ── Camera ────────────────────────────────────────────────────────────────
    static let cameraOverlay     = Color.black.opacity(0.4)
    static let cameraGuideBorder = Color(hex: "#2196F3")
    static let cameraGuideCorner = Color.white
    static let cameraHandle      = Color(hex: "#2DD4BF")
}

// Hex initializer
extension Color {
    init(hex: String) {
        let hex = hex.trimmingCharacters(in: CharacterSet.alphanumerics.inverted)
        var int: UInt64 = 0
        Scanner(string: hex).scanHexInt64(&int)
        let a, r, g, b: UInt64
        switch hex.count {
        case 3: (a, r, g, b) = (255, (int >> 8) * 17, (int >> 4 & 0xF) * 17, (int & 0xF) * 17)
        case 6: (a, r, g, b) = (255, int >> 16, int >> 8 & 0xFF, int & 0xFF)
        case 8: (a, r, g, b) = (int >> 24, int >> 16 & 0xFF, int >> 8 & 0xFF, int & 0xFF)
        default: (a, r, g, b) = (1, 1, 1, 0)
        }
        self.init(.sRGB, red: Double(r)/255, green: Double(g)/255, blue: Double(b)/255, opacity: Double(a)/255)
    }
}
```

---

## Typography → `Theme/Typography.swift`

```swift
import SwiftUI

enum AppFont {
    // Screen title — "CamScanner" centered
    static let headlineLarge  = Font.system(size: 22, weight: .semibold)
    // Top bar title — "Thư viện", "Xuất tài liệu"
    static let titleLarge     = Font.system(size: 18, weight: .semibold)
    // Card title, document name
    static let titleMedium    = Font.system(size: 16, weight: .medium)
    // Section header — "Thao tác nhanh", "Tài liệu gần đây"
    static let titleSmall     = Font.system(size: 14, weight: .medium)
    // Body text
    static let bodyLarge      = Font.system(size: 16, weight: .regular)
    static let bodyMedium     = Font.system(size: 14, weight: .regular)
    static let bodySmall      = Font.system(size: 12, weight: .regular)
    // Button labels
    static let labelLarge     = Font.system(size: 15, weight: .semibold)
    static let labelMedium    = Font.system(size: 13, weight: .medium)
    // Metadata, timestamps
    static let labelSmall     = Font.system(size: 11, weight: .regular)
}
```

---

## Spacing → `Theme/Spacing.swift`

```swift
import CoreGraphics

enum Spacing {
    static let s0:  CGFloat = 0
    static let s1:  CGFloat = 2
    static let s2:  CGFloat = 4
    static let s3:  CGFloat = 8
    static let s4:  CGFloat = 10
    static let s5:  CGFloat = 12
    static let s6:  CGFloat = 16   // screenPadding, cardPadding
    static let s7:  CGFloat = 20
    static let s8:  CGFloat = 24   // sectionGap
    static let s9:  CGFloat = 32
    static let s10: CGFloat = 40
    static let s11: CGFloat = 48   // touchTarget
    static let s12: CGFloat = 52   // buttonHeight
    static let s13: CGFloat = 56   // topBarHeight, bottomBarHeight
    static let s14: CGFloat = 64
    static let s15: CGFloat = 72
    static let s16: CGFloat = 80   // shutterOuter

    // Semantic aliases
    static let screenPadding:   CGFloat = 16
    static let cardPadding:     CGFloat = 12
    static let sectionGap:      CGFloat = 24
    static let itemGap:         CGFloat = 10
    static let iconTextGap:     CGFloat = 4
    static let buttonHeight:    CGFloat = 52
    static let topBarHeight:    CGFloat = 56
    static let bottomBarHeight: CGFloat = 56
    static let touchTarget:     CGFloat = 48
    static let shutterOuter:    CGFloat = 80
    static let shutterInner:    CGFloat = 64
}
```

---

## Radius → `Theme/Shapes.swift`

```swift
import CoreGraphics

enum Radius {
    static let none:        CGFloat = 0
    static let xs:          CGFloat = 4
    static let sm:          CGFloat = 8    // thumbnail
    static let md:          CGFloat = 12   // card, button, quick action
    static let lg:          CGFloat = 16   // dialog
    static let xl:          CGFloat = 24   // bottom sheet
    static let full:        CGFloat = 9999 // chip, pill

    // Semantic aliases
    static let card:        CGFloat = 12
    static let button:      CGFloat = 12
    static let chip:        CGFloat = 9999
    static let dialog:      CGFloat = 16
    static let bottomSheet: CGFloat = 24
    static let thumbnail:   CGFloat = 8
    static let quickAction: CGFloat = 12
    static let formatCard:  CGFloat = 12
    static let cornerZoom:  CGFloat = 12
}
```

---

## Component Sizes

```swift
enum ComponentSize {
    // Document card thumbnail
    static let docThumbnailWidth:  CGFloat = 52
    static let docThumbnailHeight: CGFloat = 70

    // Recent document card (horizontal scroll)
    static let recentCardWidth:    CGFloat = 100
    static let recentThumbnailH:   CGFloat = 80

    // Quick action button icon box
    static let quickActionBox:     CGFloat = 56

    // Page item thumbnail (DocumentScreen)
    static let pageThumbnailWidth: CGFloat = 56
    static let pageThumbnailHeight:CGFloat = 76

    // Format card (ExportScreen)
    static let formatCardWidth:    CGFloat = 90

    // Corner zoom overlay (DetectionScreen)
    static let cornerZoomSize:     CGFloat = 120

    // Permission view icon
    static let permissionIconSize: CGFloat = 72

    // Empty state icon
    static let emptyIconSize:      CGFloat = 72

    // Shutter button
    static let shutterOuter:       CGFloat = 80
    static let shutterInner:       CGFloat = 64

    // FAB
    static let fabSize:            CGFloat = 56

    // Action buttons in PageItem
    static let pageActionButton:   CGFloat = 36
    static let dragHandle:         CGFloat = 36
}
```

---

## MaterialTheme → SwiftUI Mapping

| Android `MaterialTheme.colorScheme.*` | iOS `Color.*` |
|---|---|
| `background` | `Color.backgroundPrimary` |
| `onBackground` | `Color.textPrimary` |
| `surface` | `Color.backgroundSecondary` |
| `onSurface` | `Color.textPrimary` |
| `surfaceVariant` | `Color.backgroundElevated` |
| `onSurfaceVariant` | `Color.textSecondary` |
| `primary` | `Color.tealPrimary` |
| `onPrimary` | `Color.textOnPrimary` |
| `primaryContainer` | `Color.tealContainer` |
| `onPrimaryContainer` | `Color.tealPrimary` |
| `error` | `Color.statusError` |
| `outline` | `Color.borderSubtle` |
| `outlineVariant` | `Color.borderMedium` |
| `scrim` | `Color.black.opacity(0.8)` |

| Android `MaterialTheme.shapes.*` | iOS `Radius.*` |
|---|---|
| `extraSmall` (4dp) | `Radius.xs` |
| `small` (8dp) | `Radius.sm` |
| `medium` (12dp) | `Radius.md` |
| `large` (16dp) | `Radius.lg` |
| `extraLarge` (24dp) | `Radius.xl` |
