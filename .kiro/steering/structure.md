# Project Structure Rules — CamScanner App

## Tổng quan cấu trúc

```
project-root/
├── app/src/main/java/com/example/camscanner/
│   ├── data/               ← Data layer
│   ├── di/                 ← Dependency injection
│   ├── domain/             ← Domain layer (models, usecases, repositories)
│   ├── presentation/       ← UI layer (screens, viewmodels, components)
│   └── util/               ← Shared utilities
├── design/                 ← Design documentation & tokens
│   ├── tokens/             ← Design tokens (JSON + platform mappings)
│   ├── screens/            ← Screen specs
│   ├── components/         ← Component specs
│   ├── flows/              ← Flow specs
│   ├── figma/              ← Figma exports & references
│   ├── screenshots/        ← Reference screenshots
│   └── assets/             ← Exported assets from Figma
└── .kiro/
    ├── steering/           ← Project-wide rules (luôn được load)
    └── specs/              ← Feature specs (requirements, design, tasks)
```

## UI Files

### Screens
- Mỗi màn hình nằm trong thư mục riêng: `presentation/{screen-name}/`
- Mỗi màn hình có 2 file: `{Screen}Screen.kt` và `{Screen}ViewModel.kt`
- Screen Composable nhận state và callbacks — không inject ViewModel trực tiếp
- Route Composable (trong NavGraph) inject ViewModel và truyền xuống Screen

```
presentation/
├── home/
│   ├── HomeScreen.kt
│   └── HomeViewModel.kt
├── camera/
│   ├── CameraScreen.kt
│   └── CameraViewModel.kt
├── detection/
│   ├── DetectionScreen.kt
│   └── DetectionViewModel.kt
├── edit/
│   ├── EditScreen.kt
│   └── EditViewModel.kt
├── document/
│   ├── DocumentScreen.kt
│   └── DocumentViewModel.kt
├── export/
│   ├── ExportScreen.kt
│   └── ExportViewModel.kt
├── settings/
│   ├── SettingsScreen.kt
│   └── SettingsViewModel.kt
├── components/             ← Reusable UI components
│   ├── AppTopBar.kt
│   ├── DocumentCard.kt
│   ├── EmptyState.kt
│   └── ...
└── navigation/
    └── NavGraph.kt
```

### Components
- Component dùng ở nhiều màn hình → đặt vào `presentation/components/`
- Component chỉ dùng trong 1 màn hình → có thể đặt trong file Screen hoặc file riêng cùng thư mục
- Mỗi component phải có `@Preview` annotation để xem trước trong IDE

### Theme / Tokens
- Theme Compose: `presentation/theme/` (nếu chưa có, tạo mới)
  - `Color.kt` — màu sắc từ tokens
  - `Typography.kt` — typography từ tokens
  - `Shape.kt` — radius từ tokens
  - `Theme.kt` — MaterialTheme wrapper
- Giá trị trong theme phải map từ `design/tokens/base.tokens.json`
- Không hardcode giá trị trong Composable — dùng `MaterialTheme.colorScheme`, `MaterialTheme.typography`, `MaterialTheme.shapes`

## Design Documentation

### Tokens
```
design/tokens/
├── base.tokens.json        ← Source of truth cho mọi giá trị UI
├── android.tokens.md       ← Hướng dẫn map token sang Android/Compose
└── ios.tokens.md           ← Hướng dẫn map token sang iOS (tương lai)
```

### Screens
```
design/screens/
├── README.md               ← Index và hướng dẫn đọc screen spec
├── home.md
├── scan-camera.md
├── crop-document.md
├── preview-document.md
├── document-detail.md
├── export-share.md
└── settings.md
```

### Components
```
design/components/
├── README.md               ← Index và hướng dẫn đọc component spec
├── app-top-bar.md
├── primary-button.md
├── secondary-button.md
├── icon-button.md
├── document-card.md
├── empty-state.md
├── loading-state.md
├── error-state.md
├── bottom-action-bar.md
├── scan-capture-button.md
├── page-thumbnail.md
├── filter-chip.md
└── permission-view.md
```

### Flows
```
design/flows/
├── README.md               ← Index và hướng dẫn đọc flow spec
├── scan-to-pdf.md
├── import-image-to-pdf.md
├── share-document.md
├── delete-document.md
└── rename-document.md
```

## Specs

```
.kiro/specs/
└── {feature-name}/
    ├── requirements.md     ← User stories + acceptance criteria
    ├── design.md           ← Technical design
    └── tasks.md            ← Implementation task list
```

### Khi thêm feature mới

1. Tạo thư mục `.kiro/specs/{feature-name}/`
2. Tạo `requirements.md` với user stories và acceptance criteria
3. Tạo `design.md` với technical design (scope, components, state model, navigation)
4. Tạo `tasks.md` với task list chia theo nhóm:
   - inspect current project
   - map existing files
   - create/update tokens
   - create/update components
   - update screen
   - verify behavior
   - run build
   - visual QA
5. Nếu feature có UI mới → tạo screen spec trong `design/screens/`
6. Nếu feature có component mới → tạo component spec trong `design/components/`
7. Nếu feature có flow mới → tạo flow spec trong `design/flows/`

### Naming convention

- Feature name: `kebab-case` (ví dụ: `user-authentication`, `export-pdf`)
- Screen spec file: `kebab-case.md` (ví dụ: `home.md`, `scan-camera.md`)
- Component spec file: `kebab-case.md` (ví dụ: `primary-button.md`)
- Kotlin class: `PascalCase` (ví dụ: `HomeScreen`, `PrimaryButton`)
- Token key: `dot.notation` (ví dụ: `color.background.primary`)

## Figma & Assets

```
design/figma/
├── README.md               ← Link Figma, hướng dẫn export
└── assets/                 ← Assets exported từ Figma

design/screenshots/
└── README.md               ← Screenshots tham chiếu cho QA

design/assets/
└── README.md               ← Hướng dẫn quản lý assets
```

- Android assets → `app/src/main/res/drawable/` hoặc vector assets
- Không dùng remote image URL cho UI core
