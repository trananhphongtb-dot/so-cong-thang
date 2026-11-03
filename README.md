# Sổ Công Tháng

App Android offline, không cần đăng nhập, không cần mạng.

Cách dùng đúng theo thiết kế đã duyệt:
- Bấm Dự án để thêm việc theo số (số, tên việc, màu).
- Màn hình chính là lịch tháng. Bấm vào 1 ngày thì hiện bảng chọn việc.
- Tích số nào thì số đó hiện ngay lên ô ngày đó, 1 ngày tích được nhiều việc.
- Ngày tương lai chỉ cần bấm Hẹn trước, chưa cần chọn việc.
- Bấm Đã xong ngày này thì ngày đó không nhắc nữa.
- Dưới lịch là thống kê tháng: số ngày đã tích, tổng lượt việc, từng số được bao nhiêu ngày, và danh sách ngày chưa tích / chưa xong để tích bù.
- Nhắc lúc 20h30 nếu ngày có hẹn hoặc đã tích mà chưa bấm Đã xong, khi máy đã cho phép thông báo.

Bản 0.2.0: giao diện mới thoáng hơn, lịch là tab riêng, thống kê sang tab riêng, thẻ bo tròn lớn có bóng đổ kiểu 3D, nhắc 20h30 bằng AlarmManager (không dùng WorkManager nữa để nhẹ hơn), build debug có thu gọn R8 để giảm dung lượng.
Build: GitHub Actions, chỉ APK debug trước, chưa làm AAB.
Gói: vn.phong.socongthang, version 0.2.0.
