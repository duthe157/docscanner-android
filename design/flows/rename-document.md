# Flow: Rename Document

> **Status**: Implemented on Android ✅

## Goal
User đổi tên một tài liệu.

## Entry Points
- DocumentScreen → TopAppBar → Rename icon (✏️)

## Steps

```
1. DocumentScreen
   └─ Tap rename icon (DriveFileRenameOutline) trong TopAppBar
   
2. RenameDialog
   ├─ Icon: DriveFileRenameOutline
   ├─ Title: "Đổi tên tài liệu"
   ├─ OutlinedTextField với tên hiện tại pre-filled
   ├─ Tap "Hủy" → đóng dialog
   └─ Tap "Lưu" (disabled nếu blank) → lưu tên mới
   
3. Lưu thành công
   └─ TopAppBar title cập nhật ngay lập tức
```

## Success State
- TopAppBar hiển thị tên mới
- Room database cập nhật

## Error States
- Tên blank → "Lưu" button disabled
- Lưu thất bại → Snackbar

## Offline Behavior
- Hoạt động offline hoàn toàn

## Android / iOS Parity

| Element | Android | iOS |
|---|---|---|
| Rename icon | `DriveFileRenameOutline` | `pencil` (SF Symbol) |
| Dialog | `AlertDialog` với `OutlinedTextField` | `.alert` với `TextField` hoặc custom sheet |
| Pre-fill | `mutableStateOf(currentName)` | `@State var name = currentName` |

## Acceptance Criteria
- [ ] Rename icon visible trong TopAppBar
- [ ] Dialog mở với tên hiện tại pre-filled
- [ ] "Lưu" disabled khi text blank
- [ ] Lưu → title cập nhật ngay
- [ ] Hủy → không thay đổi
