# Flow: Delete Document

> **Status**: Implemented on Android ✅

## Goal
User xóa một tài liệu khỏi danh sách.

## Entry Points
- HomeScreen → DocumentCard → More menu (⋮) → "Xóa"

## Steps

```
1. HomeScreen
   └─ Tap ⋮ trên DocumentCard → DropdownMenu hiện ra
   
2. DropdownMenu
   └─ Tap "Xóa" (text màu error #FF5252)
   
3. AlertDialog — Confirm
   ├─ Icon: Delete, màu error
   ├─ Title: "Xóa tài liệu?"
   ├─ Text: "Tài liệu và tất cả trang sẽ bị xóa vĩnh viễn."
   ├─ Tap "Hủy" → đóng dialog, không xóa
   └─ Tap "Xóa" (bg error) → xóa
   
4. Xóa thành công
   └─ Document biến mất khỏi list (Room Flow tự update)
```

## Success State
- Document không còn trong danh sách
- Nếu list trống → EmptyState hiển thị

## Error States
- Xóa thất bại: Snackbar với message lỗi

## Offline Behavior
- Hoạt động offline hoàn toàn (Room local)

## Android / iOS Parity

| Element | Android | iOS |
|---|---|---|
| More menu | `DropdownMenu` | `Menu` button |
| Confirm dialog | `AlertDialog` | `.alert` modifier |
| Confirm button color | `MaterialTheme.colorScheme.error` | `Color.statusError` |

## Acceptance Criteria
- [ ] More menu hiển thị đúng 2 options: Hợp nhất + Xóa
- [ ] "Xóa" text màu error
- [ ] Dialog hiển thị đúng title, text, buttons
- [ ] Confirm → document biến mất ngay lập tức
- [ ] Hủy → không có gì thay đổi
