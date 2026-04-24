# Tài liệu Yêu cầu

## Giới thiệu

Ứng dụng mobile scanner (tương tự CamScanner) cho phép người dùng chụp ảnh tài liệu, tự động phát hiện biên, chỉnh sửa góc, áp dụng bộ lọc và xuất file PDF hoặc ảnh — hoàn toàn offline, không phụ thuộc server trong quá trình sử dụng thông thường.

Ứng dụng được xây dựng trên nền tảng cross-platform (Flutter) hoặc native iOS/Android, tích hợp sẵn TFLite model để hỗ trợ phát hiện biên tài liệu. Toàn bộ xử lý ảnh và lưu trữ diễn ra trên thiết bị.

---

## Bảng thuật ngữ

- **App**: Ứng dụng mobile scanner là đối tượng chính của tài liệu này.
- **Camera_Module**: Module quản lý camera, preview, chụp ảnh, flash và focus.
- **Detection_Module**: Module phát hiện biên tài liệu, trả về 4 điểm góc.
- **Edit_Module**: Module chỉnh sửa ảnh gồm crop, warp, xoay, filter, độ sáng/tương phản.
- **Document_Module**: Module quản lý tài liệu gồm danh sách trang, sắp xếp, thêm/xóa.
- **Export_Module**: Module xuất file PDF/ảnh và chia sẻ qua share sheet.
- **Storage_Module**: Module lưu trữ cục bộ metadata và file ảnh/PDF.
- **Settings_Module**: Module cài đặt thông số cơ bản của ứng dụng.
- **Tài_liệu**: Một document gồm một hoặc nhiều trang ảnh đã xử lý.
- **Trang**: Một ảnh đơn lẻ thuộc một Tài_liệu.
- **Perspective_Transform**: Phép biến đổi phối cảnh để làm phẳng tài liệu từ 4 điểm góc.
- **TFLite_Model**: Model TensorFlow Lite được nhúng sẵn vào app để hỗ trợ phát hiện biên.
- **Contour**: Đường viền ngoài cùng của tờ giấy được phát hiện qua xử lý ảnh.
- **Filter**: Bộ lọc màu áp dụng lên ảnh (Original, Đen trắng, Màu sắc, Tối).
- **Share_Sheet**: Giao diện chia sẻ file gốc của hệ điều hành (iOS/Android).

---

## Yêu cầu

### Yêu cầu 1: Chụp ảnh tài liệu bằng camera

**User Story:** Là người dùng cá nhân, tôi muốn mở camera và chụp ảnh tài liệu trực tiếp trong app, để tôi có thể scan tài liệu nhanh mà không cần thiết bị ngoài.

#### Tiêu chí chấp nhận

1. THE App SHALL mở Camera_Module ngay khi người dùng nhấn nút "Scan mới" từ màn hình Home.
2. WHILE Camera_Module đang hoạt động, THE Camera_Module SHALL hiển thị khung hướng dẫn chụp (guide frame) trên màn hình preview.
3. WHILE Camera_Module đang hoạt động, THE Camera_Module SHALL duy trì chế độ auto-focus liên tục.
4. WHEN người dùng nhấn nút chụp, THE Camera_Module SHALL chụp ảnh và chuyển sang màn hình phát hiện biên trong vòng 1 giây.
5. WHEN người dùng nhấn nút Flash, THE Camera_Module SHALL chuyển đổi trạng thái flash giữa bật và tắt.
6. WHEN người dùng nhấn "Thêm trang", THE Camera_Module SHALL cho phép chụp thêm ảnh mới và gắn vào Tài_liệu hiện tại.
7. IF Camera_Module không thể truy cập camera do thiếu quyền, THEN THE App SHALL hiển thị thông báo yêu cầu cấp quyền camera và hướng dẫn vào Settings.

---

### Yêu cầu 2: Nhập ảnh từ thư viện

**User Story:** Là người dùng, tôi muốn chọn ảnh có sẵn từ thư viện ảnh của thiết bị, để tôi có thể xử lý tài liệu đã chụp trước đó.

#### Tiêu chí chấp nhận

1. THE App SHALL cung cấp nút "Import ảnh" trên màn hình Home.
2. WHEN người dùng nhấn "Import ảnh", THE App SHALL mở giao diện chọn ảnh từ thư viện của hệ điều hành.
3. WHEN người dùng chọn một hoặc nhiều ảnh, THE App SHALL nhận danh sách ảnh đó và chuyển sang màn hình phát hiện biên cho từng ảnh theo thứ tự.
4. IF người dùng không cấp quyền truy cập thư viện ảnh, THEN THE App SHALL hiển thị thông báo yêu cầu cấp quyền và hướng dẫn vào Settings.

---

### Yêu cầu 3: Phát hiện biên tài liệu tự động

**User Story:** Là người dùng, tôi muốn app tự động nhận diện 4 góc của tờ giấy trong ảnh, để tôi không phải chỉnh tay mỗi lần scan.

#### Tiêu chí chấp nhận

1. WHEN ảnh được đưa vào Detection_Module, THE Detection_Module SHALL thực hiện pipeline: resize tạm → grayscale → blur nhẹ → edge detection → tìm Contour ngoài cùng → trả về 4 điểm góc theo thứ tự top-left, top-right, bottom-right, bottom-left.
2. WHEN Detection_Module phát hiện thành công, THE Detection_Module SHALL trả về đúng 4 điểm góc hợp lệ trong vòng 2 giây.
3. WHERE TFLite_Model được nhúng vào app, THE Detection_Module SHALL sử dụng TFLite_Model để hỗ trợ cải thiện độ chính xác phát hiện biên bên cạnh Contour-based detect.
4. WHILE Detection_Module đang xử lý, THE App SHALL hiển thị trạng thái loading để không block giao diện người dùng.
5. IF Detection_Module không tìm được Contour hợp lệ (diện tích quá nhỏ hoặc không đủ 4 góc), THEN THE Detection_Module SHALL fallback về chế độ cho phép người dùng chỉnh 4 góc thủ công.
6. WHEN 4 điểm góc được xác định, THE App SHALL hiển thị 4 điểm kéo thả trên ảnh để người dùng xem và điều chỉnh trước khi xác nhận.

---

### Yêu cầu 4: Chỉnh sửa 4 góc thủ công

**User Story:** Là người dùng, tôi muốn kéo thả 4 điểm góc để hiệu chỉnh vùng tài liệu, để tôi có thể sửa khi app nhận diện sai.

#### Tiêu chí chấp nhận

1. THE Edit_Module SHALL hiển thị 4 điểm kéo thả (drag handle) tại các góc tài liệu đã phát hiện trên ảnh preview.
2. WHEN người dùng kéo một điểm góc, THE Edit_Module SHALL cập nhật vị trí điểm đó theo thời gian thực và vẽ lại vùng chọn.
3. WHEN người dùng nhấn nút "Zoom góc", THE Edit_Module SHALL phóng to vùng xung quanh góc đang chỉnh để tăng độ chính xác.
4. WHEN người dùng nhấn "Reset", THE Edit_Module SHALL khôi phục 4 điểm về kết quả phát hiện tự động ban đầu.
5. WHEN người dùng nhấn "Detect lại", THE Detection_Module SHALL chạy lại quá trình phát hiện biên trên ảnh gốc.
6. WHEN người dùng xác nhận 4 góc, THE Edit_Module SHALL thực hiện Perspective_Transform để làm phẳng tài liệu.

---

### Yêu cầu 5: Xử lý và chỉnh sửa ảnh

**User Story:** Là người dùng, tôi muốn chỉnh sửa ảnh sau khi làm phẳng (xoay, crop, filter, sáng/tối), để tôi có được ảnh scan đẹp và rõ ràng.

#### Tiêu chí chấp nhận

1. WHEN Perspective_Transform hoàn tất, THE Edit_Module SHALL áp dụng pipeline: sharpen nhẹ → denoise cơ bản → hiển thị ảnh kết quả cho người dùng xem trước.
2. THE Edit_Module SHALL cung cấp các Filter: Original, Đen trắng (scan), Màu sắc (tăng cường), Tối (tài liệu tối).
3. WHEN người dùng chọn một Filter, THE Edit_Module SHALL áp dụng Filter đó và cập nhật preview trong vòng 500ms.
4. THE Edit_Module SHALL cung cấp thanh trượt điều chỉnh Brightness trong khoảng -100 đến +100.
5. THE Edit_Module SHALL cung cấp thanh trượt điều chỉnh Contrast trong khoảng -100 đến +100.
6. WHEN người dùng nhấn nút xoay, THE Edit_Module SHALL xoay ảnh 90 độ theo chiều kim đồng hồ.
7. WHEN người dùng nhấn "Retake", THE App SHALL quay lại Camera_Module để chụp lại ảnh đó.
8. WHILE Edit_Module đang xử lý ảnh full quality, THE App SHALL render preview ở độ phân giải thấp hơn để không block giao diện, và chỉ render full quality khi người dùng lưu hoặc xuất.

---

### Yêu cầu 6: Quản lý tài liệu

**User Story:** Là người dùng, tôi muốn tổ chức nhiều trang thành một tài liệu và quản lý danh sách tài liệu đã lưu, để tôi dễ dàng tìm lại và chỉnh sửa sau.

#### Tiêu chí chấp nhận

1. THE Document_Module SHALL cho phép tạo Tài_liệu từ một hoặc nhiều Trang.
2. WHEN người dùng tạo Tài_liệu mới, THE Document_Module SHALL gán tên mặc định theo định dạng "Tài liệu [ngày giờ tạo]".
3. WHEN người dùng đổi tên Tài_liệu, THE Document_Module SHALL lưu tên mới ngay lập tức vào Storage_Module.
4. THE Document_Module SHALL cho phép người dùng sắp xếp lại thứ tự Trang bằng thao tác kéo thả.
5. THE Document_Module SHALL cho phép người dùng thêm Trang mới vào Tài_liệu hiện có.
6. THE Document_Module SHALL cho phép người dùng xóa một Trang khỏi Tài_liệu.
7. THE Document_Module SHALL cho phép người dùng hợp nhất hai Tài_liệu thành một.
8. THE App SHALL hiển thị danh sách Tài_liệu đã lưu trên màn hình Home, sắp xếp theo ngày chỉnh sửa gần nhất.
9. IF người dùng xóa một Tài_liệu, THEN THE Document_Module SHALL hiển thị hộp thoại xác nhận trước khi xóa vĩnh viễn.

---

### Yêu cầu 7: Xuất file và chia sẻ

**User Story:** Là người dùng, tôi muốn xuất tài liệu ra file PDF hoặc ảnh và chia sẻ qua các ứng dụng khác, để tôi có thể gửi hoặc lưu trữ tài liệu theo nhu cầu.

#### Tiêu chí chấp nhận

1. THE Export_Module SHALL xuất Tài_liệu thành file PDF với mỗi Trang là một trang trong PDF.
2. THE Export_Module SHALL xuất từng Trang thành file ảnh định dạng JPG hoặc PNG theo lựa chọn của người dùng.
3. WHEN người dùng nhấn "Chia sẻ", THE Export_Module SHALL mở Share_Sheet của hệ điều hành với file đã xuất.
4. WHEN người dùng chọn "Lưu vào Files", THE Export_Module SHALL lưu file vào thư mục Files/Documents của thiết bị.
5. WHEN người dùng chọn "Lưu vào Photos", THE Export_Module SHALL lưu ảnh vào thư viện ảnh của thiết bị.
6. IF quá trình xuất file thất bại, THEN THE Export_Module SHALL hiển thị thông báo lỗi mô tả nguyên nhân và cho phép thử lại.

---

### Yêu cầu 8: Lưu trữ cục bộ

**User Story:** Là người dùng, tôi muốn tất cả dữ liệu được lưu trên thiết bị mà không cần kết nối mạng, để tôi có thể dùng app hoàn toàn offline.

#### Tiêu chí chấp nhận

1. THE Storage_Module SHALL lưu ảnh gốc, ảnh đã xử lý và file PDF vào bộ nhớ cục bộ của thiết bị.
2. THE Storage_Module SHALL lưu metadata của mỗi Tài_liệu gồm: tên tài liệu, ngày tạo, danh sách Trang, Filter đã chọn cho từng Trang.
3. THE Storage_Module SHALL sử dụng SQLite (hoặc tương đương theo nền tảng: Core Data trên iOS, Room trên Android) để lưu metadata.
4. WHILE App đang chạy offline hoàn toàn, THE Storage_Module SHALL đảm bảo toàn bộ chức năng đọc/ghi dữ liệu hoạt động bình thường.
5. THE Storage_Module SHALL giảm dung lượng bộ nhớ bằng cách lưu ảnh preview ở độ phân giải thấp hơn ảnh gốc.
6. IF bộ nhớ thiết bị không đủ để lưu file, THEN THE Storage_Module SHALL thông báo cho người dùng và hủy thao tác lưu mà không làm hỏng dữ liệu hiện có.

---

### Yêu cầu 9: Tích hợp TFLite Model

**User Story:** Là developer, tôi muốn nhúng TFLite model có sẵn vào app để hỗ trợ phát hiện biên tài liệu, để tôi tận dụng được model đã train mà không cần server.

#### Tiêu chí chấp nhận

1. THE App SHALL đóng gói TFLite_Model vào bundle ứng dụng tại đường dẫn cố định trong assets.
2. WHEN App khởi động, THE Detection_Module SHALL tải TFLite_Model vào bộ nhớ một lần duy nhất.
3. WHEN Detection_Module nhận ảnh đầu vào, THE Detection_Module SHALL tiền xử lý ảnh (resize về kích thước đầu vào của model, normalize) trước khi chạy inference.
4. WHEN TFLite_Model trả về kết quả, THE Detection_Module SHALL hậu xử lý output để lấy 4 điểm góc tài liệu.
5. IF TFLite_Model không tải được hoặc inference thất bại, THEN THE Detection_Module SHALL fallback về Contour-based detect mà không crash app.
6. THE Detection_Module SHALL chạy inference TFLite trên background thread để không block giao diện người dùng.

---

### Yêu cầu 10: Hiệu năng và trải nghiệm người dùng

**User Story:** Là người dùng, tôi muốn app phản hồi nhanh và không bị giật lag khi xử lý ảnh, để tôi có trải nghiệm mượt mà khi sử dụng hàng ngày.

#### Tiêu chí chấp nhận

1. THE App SHALL xử lý ảnh trên background thread và không block UI thread trong suốt quá trình xử lý.
2. WHEN người dùng mở màn hình Home, THE App SHALL hiển thị danh sách Tài_liệu trong vòng 1 giây.
3. WHEN người dùng mở một Tài_liệu, THE App SHALL hiển thị preview các Trang trong vòng 1 giây bằng cách dùng ảnh preview độ phân giải thấp.
4. THE App SHALL cache ảnh preview để tránh render lại khi người dùng cuộn danh sách.
5. WHILE App đang xử lý ảnh full quality để lưu hoặc xuất, THE App SHALL hiển thị thanh tiến trình để người dùng biết trạng thái.
6. THE App SHALL giải phóng bộ nhớ ảnh lớn ngay sau khi không còn cần thiết để tránh tràn bộ nhớ.

---

### Yêu cầu 11: Cài đặt ứng dụng

**User Story:** Là người dùng, tôi muốn điều chỉnh một số thông số cơ bản của app, để tôi có thể tùy chỉnh theo thói quen sử dụng.

#### Tiêu chí chấp nhận

1. THE Settings_Module SHALL cho phép người dùng chọn định dạng xuất mặc định (PDF hoặc JPG/PNG).
2. THE Settings_Module SHALL cho phép người dùng chọn Filter mặc định áp dụng khi scan.
3. THE Settings_Module SHALL hiển thị dung lượng bộ nhớ đang được app sử dụng.
4. THE Settings_Module SHALL cho phép người dùng xóa toàn bộ ảnh gốc đã lưu để giải phóng dung lượng, trong khi vẫn giữ ảnh đã xử lý.


---

### Yêu cầu 12: Thu thập dữ liệu training (Planned)

**User Story:** Là developer, tôi muốn thu thập ảnh scan và tọa độ góc được người dùng chỉnh sửa để cải thiện model theo thời gian, mà không ảnh hưởng đến trải nghiệm người dùng.

#### Tiêu chí chấp nhận

1. THE App SHALL xin phép người dùng (opt-in) trước khi thu thập bất kỳ dữ liệu nào.
2. WHEN người dùng xác nhận 4 góc sau khi điều chỉnh, THE App SHALL so sánh tọa độ auto-detect và tọa độ user-corrected để tính `correction_delta`.
3. IF `correction_delta` > ngưỡng (50px), THEN THE App SHALL queue một record training data gồm: ảnh gốc, auto_corners, user_corners, detection_method, metadata.
4. THE App SHALL upload training data lên Google Drive bất đồng bộ (background thread) sau khi user confirm, không làm chậm luồng scan.
5. IF upload thất bại (offline hoặc lỗi), THE App SHALL giữ data trong local queue và retry khi có kết nối.
6. THE App SHALL không lưu ảnh chứa thông tin nhạy cảm nếu người dùng không đồng ý.

**Status**: PLANNED — chưa implement.
