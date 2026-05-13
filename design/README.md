# Design Documentation — CamScanner App

Thư mục này chứa toàn bộ tài liệu thiết kế cho CamScanner App.
Tất cả giá trị UI đã được extract từ Android implementation thực tế.

## Cấu trúc

```
design/
├── tokens/         ← Design tokens (nguồn chuẩn cho mọi giá trị UI)
│   ├── base.tokens.json    ← Source of truth — màu, spacing, radius, typography
│   └── ios.tokens.md       ← Swift code sẵn sàng dùng cho iOS
├── screens/        ← Screen specs (layout, measurements, states, behavior)
├── components/     ← Component specs (props, states, measurements, iOS code)
├── flows/          ← Flow specs (user journeys, Android/iOS parity)
├── screenshots/    ← Screenshots để QA visual (Android vs iOS)
└── android-current-ui-mapping.md  ← Trạng thái Android + Android→iOS mapping
```

## Thứ tự ưu tiên Source of Truth

```
1. design/tokens/base.tokens.json   (cao nhất)
2. design/screens/*.md
3. design/components/*.md
4. Code Android hiện tại            (thấp nhất)
```

## Bắt đầu từ đâu?

- **Implement iOS** → Đọc `tokens/ios.tokens.md` + `screens/{screen}.md` + `android-current-ui-mapping.md`
- **Update UI Android** → Đọc `screens/{screen}.md` + `components/*.md`
- **Kiểm tra behavior** → Đọc `flows/*.md`
- **QA visual** → Chụp screenshot → đặt vào `screenshots/android/` → so sánh với iOS
