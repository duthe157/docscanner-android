# Preview Document Screen (Edit Screen)

> **Status**: Implemented on Android ✅
> **Source**: `presentation/edit/EditScreen.kt`

## Purpose
Xem preview ảnh đã crop, chọn filter, điều chỉnh brightness/contrast, xoay, và lưu trang vào tài liệu.

## Layout Baseline
- Full screen với TopAppBar và bottom controls panel
- Background: `#0D1F1E`

## Structure
```
┌─────────────────────────────────┐
│  Status Bar                     │
├─────────────────────────────────┤
│  TopAppBar                      │
│  [←]  "Chỉnh sửa"              │
├─────────────────────────────────┤
│                                 │
│  Image Preview (weight 1f)      │
│  bg: surfaceVariant 40%         │
│  ContentScale.Fit, padding 8dp  │
│                                 │
│  [Loading/Saving overlay]       │
│                                 │
├─────────────────────────────────┤
│  Controls Panel (Surface 3dp)   │
│  ┌─────────────────────────┐    │
│  │ [Gốc][B&W][Màu][Tối]   │    │  ← FilterChips
│  │ Sáng      ────●──  +30  │    │  ← Slider
│  │ Tương phản ──────●  +10 │    │  ← Slider
│  │ [Xoay 90°]  [Lưu trang] │    │  ← Action row
│  └─────────────────────────┘    │
│  Navigation Bar                 │
└─────────────────────────────────┘
```

## Exact Measurements

| Element | Giá trị | Token |
|---|---|---|
| TopAppBar bg | theme default | — |
| TopAppBar title | "Chỉnh sửa" hoặc "Chỉnh sửa trang" | — |
| Preview area bg | `surfaceVariant` opacity 40% | — |
| Preview image padding | 8dp | `spacing.3` |
| Loading overlay bg | `scrim` opacity 35% | — |
| Loading text | "Đang xử lý..." / "Đang lưu..." | — |
| Controls panel elevation | 3dp | — |
| Filter chips row padding H | 12dp | — |
| Filter chips row padding V | 8dp | — |
| Filter chip gap | 8dp | `spacing.3` |
| Filter chip weight | equal (weight 1f each) | — |
| Filter chip font | 13sp Medium | `typography.labelMedium` |
| Slider row padding H | 16dp | `spacing.screenPadding` |
| Slider label width | 72dp | — |
| Slider label font | 11sp Regular | `typography.labelSmall` |
| Slider label color | onSurface opacity 70% | — |
| Slider value width | 32dp | — |
| Slider value font | 11sp Regular | `typography.labelSmall` |
| Action row padding H | 12dp | — |
| Action row padding V | 4dp | — |
| Action row gap | 10dp | `spacing.4` |
| Rotate button | OutlinedButton, weight 1f, radius 10dp | — |
| Rotate icon size | 18dp | `icon.md` |
| Save button | FilledButton, weight 1.4f, radius 10dp | — |
| Save button bg | primary `#2DD4BF` | `color.action.primary` |
| Save icon size | 18dp | `icon.md` |
| Save font | SemiBold | — |
| Controls bottom padding | 8dp | — |

## Filter Labels

| FilterType | Label |
|---|---|
| ORIGINAL | "Gốc" |
| BW | "B&W" |
| COLOR | "Màu" |
| DARK | "Tối" |

## Slider Ranges

| Slider | Range | Default |
|---|---|---|
| Brightness (Sáng) | -100 to +100 | 0 |
| Contrast (Tương phản) | -100 to +100 | 0 (EditViewModel default: 10) |

## States

### loading (processing)
- Preview area: ảnh mờ hoặc placeholder
- Overlay: spinner + "Đang xử lý..."
- Controls disabled

### content
- Preview hiển thị ảnh đã process
- Tất cả controls active

### saving
- Overlay: spinner + "Đang lưu..."
- Tất cả controls disabled

### error
- Text màu error ở dưới action row

## Behavior

- **Chọn filter** → `viewModel.applyFilter()` → re-render preview
- **Kéo slider** → `viewModel.setBrightness/setContrast()` → re-render preview
- **Xoay 90°** → `viewModel.rotate()` → re-render preview
- **Lưu trang** → `viewModel.saveToDocument()` → navigate to DocumentScreen
- **Back (←)** → `onRetake()` → về DetectionScreen

## Navigation

**Entry:** DetectionScreen (Xác nhận)
**Exit:** → DocumentScreen (Lưu trang)
**Back:** → DetectionScreen

## iOS Notes

- `Surface` với `tonalElevation` → `RoundedRectangle` với shadow
- `FilterChip` → custom `Toggle`-style button
- `Slider` → SwiftUI `Slider`
- `OutlinedButton` → `Button` với `.bordered` style
- `FilledButton` → `Button` với `.borderedProminent` style
