# Design System Rules — CamScanner App

## Thứ tự ưu tiên Source of Truth

Khi có xung đột giữa các nguồn, ưu tiên theo thứ tự sau:

```
1. Figma (cao nhất)
2. design/tokens/base.tokens.json
3. design/screens/*.md
4. design/components/*.md
5. Code hiện tại (thấp nhất)
```

Nếu Figma chưa có giá trị → dùng token. Nếu token chưa có → dùng screen/component spec. Không tự bịa giá trị.

---

## Colors

### Quy tắc
- Không hardcode hex color trong Composable.
- Mọi màu phải đến từ `MaterialTheme.colorScheme` hoặc token đã định nghĩa.
- Dùng semantic color name, không dùng tên màu vật lý (không dùng `blue500`, dùng `action.primary`).

### Naming convention — Semantic Colors

```
color.background.primary      ← Nền chính của app (toàn màn hình)
color.background.secondary    ← Nền phụ (bottom sheet, card nền)
color.surface.card            ← Nền của card/item
color.surface.overlay         ← Overlay mờ (dialog backdrop)

color.text.primary            ← Text chính (tiêu đề, nội dung)
color.text.secondary          ← Text phụ (subtitle, hint)
color.text.disabled           ← Text bị disabled
color.text.onPrimary          ← Text trên nền action.primary

color.action.primary          ← Màu chính cho button, FAB, accent
color.action.primaryText      ← Text trên action.primary
color.action.secondary        ← Màu phụ cho secondary button
color.action.secondaryText    ← Text trên action.secondary
color.action.destructive      ← Màu cho hành động nguy hiểm (xóa)

color.border.default          ← Viền mặc định
color.border.focused          ← Viền khi focused
color.border.disabled         ← Viền khi disabled

color.status.error            ← Lỗi
color.status.success          ← Thành công
color.status.warning          ← Cảnh báo
color.status.info             ← Thông tin

color.icon.default            ← Icon mặc định
color.icon.active             ← Icon đang active
color.icon.disabled           ← Icon disabled
color.icon.onPrimary          ← Icon trên nền action.primary

color.camera.overlay          ← Overlay trên camera preview
color.camera.guide            ← Màu khung hướng dẫn chụp
color.camera.handle           ← Màu drag handle góc tài liệu
```

---

## Typography

### Quy tắc
- Không hardcode font size, font weight trong Composable.
- Dùng `MaterialTheme.typography.*` hoặc token đã định nghĩa.
- Android dùng `sp` cho font size.
- Line height tính theo `sp` hoặc tỷ lệ (1.2–1.5× font size).

### Naming convention — Typography Tokens

```
typography.titleLarge         ← Tiêu đề màn hình lớn (Home title)
typography.titleMedium        ← Tiêu đề section, dialog
typography.titleSmall         ← Tiêu đề nhỏ, label section

typography.bodyLarge          ← Nội dung chính, dài
typography.bodyMedium         ← Nội dung thông thường
typography.bodySmall          ← Nội dung phụ, mô tả

typography.labelLarge         ← Label button lớn
typography.labelMedium        ← Label button thường, chip
typography.labelSmall         ← Label nhỏ, badge

typography.caption            ← Chú thích, metadata (ngày tháng, kích thước file)
typography.overline           ← Label trên (category, section header nhỏ)
```

### Thuộc tính mỗi token typography
```json
{
  "fontSize": "16sp",
  "fontWeight": "SemiBold",
  "lineHeight": "24sp",
  "letterSpacing": "0sp"
}
```

---

## Spacing

### Quy tắc
- Chỉ dùng các giá trị trong spacing scale — không dùng giá trị tùy tiện.
- Android dùng `dp`.
- Spacing scale được phép dùng:

```
spacing.0   = 0dp
spacing.1   = 2dp
spacing.2   = 4dp
spacing.3   = 8dp
spacing.4   = 12dp
spacing.5   = 16dp
spacing.6   = 20dp
spacing.7   = 24dp
spacing.8   = 32dp
spacing.9   = 40dp
spacing.10  = 48dp
spacing.11  = 56dp
spacing.12  = 64dp
spacing.14  = 80dp
spacing.16  = 96dp
```

### Semantic spacing aliases
```
spacing.screenPadding         = spacing.5  (16dp) — padding ngang màn hình
spacing.cardPadding           = spacing.5  (16dp) — padding trong card
spacing.sectionGap            = spacing.7  (24dp) — khoảng cách giữa sections
spacing.itemGap               = spacing.3  (8dp)  — khoảng cách giữa items
spacing.iconTextGap           = spacing.2  (4dp)  — khoảng cách icon và text
spacing.buttonHeight          = spacing.11 (56dp) — chiều cao button chính
spacing.topBarHeight          = spacing.11 (56dp) — chiều cao top bar
spacing.bottomBarHeight       = spacing.11 (56dp) — chiều cao bottom bar
spacing.touchTarget           = spacing.11 (56dp) — minimum touch target
```

---

## Border Radius

### Naming convention — Radius Tokens

```
radius.none     = 0dp
radius.xs       = 4dp
radius.sm       = 8dp
radius.md       = 12dp
radius.lg       = 16dp
radius.xl       = 24dp
radius.full     = 9999dp  (pill / circle)

radius.card     = radius.md  (12dp)
radius.button   = radius.md  (12dp)
radius.chip     = radius.full
radius.dialog   = radius.lg  (16dp)
radius.bottomSheet = radius.xl (24dp, chỉ top corners)
radius.thumbnail = radius.sm  (8dp)
```

---

## Shadows / Elevation

### Quy tắc
- Dùng elevation để tạo visual hierarchy, không dùng custom shadow.
- Android Compose dùng `elevation` parameter trong Surface/Card.
- Giữ đúng visual hierarchy — element nổi hơn có elevation cao hơn.

### Naming convention — Shadow Tokens

```
shadow.none     = 0dp elevation
shadow.xs       = 1dp elevation   ← Subtle separation
shadow.sm       = 2dp elevation   ← Card, list item
shadow.md       = 4dp elevation   ← Bottom bar, top bar
shadow.lg       = 8dp elevation   ← FAB, floating button
shadow.xl       = 16dp elevation  ← Dialog, bottom sheet
```

---

## Icons

### Quy tắc
- Dùng vector drawable (SVG/VectorDrawable) — không dùng raster PNG cho icon UI.
- Ưu tiên Material Icons (`androidx.compose.material:material-icons-extended`) trước khi tạo custom icon.
- Custom icon export từ Figma dưới dạng SVG → convert sang VectorDrawable.
- Icon size phải đến từ token, không hardcode.

### Icon size tokens
```
icon.xs   = 16dp
icon.sm   = 20dp
icon.md   = 24dp   ← Default
icon.lg   = 32dp
icon.xl   = 48dp
```

---

## Touch Targets

- Minimum touch target: **48dp × 48dp** (Android accessibility guideline).
- Nếu icon nhỏ hơn 48dp, thêm padding để đạt minimum touch target.
- Không giảm touch target dưới 44dp trong bất kỳ trường hợp nào.

---

## Dark Mode

- TODO: Xác nhận từ Figma xem app có dark mode không.
- Nếu có: định nghĩa color token riêng cho dark theme.
- Nếu không: chỉ cần light theme, nhưng vẫn dùng semantic color name để dễ thêm sau.

---

## Quy tắc chung

1. **Không hardcode** bất kỳ giá trị UI nào (màu, size, spacing, radius) trực tiếp trong Composable.
2. **Mọi giá trị UI** phải đến từ token (`base.tokens.json`) hoặc screen/component spec.
3. **TODO marker** ở những chỗ chưa có giá trị từ Figma — không tự bịa.
4. Khi Figma cung cấp giá trị mới → cập nhật `base.tokens.json` trước, sau đó cập nhật theme Compose.
5. Token là **single source of truth** — không có giá trị UI nào tồn tại ở 2 nơi khác nhau.
