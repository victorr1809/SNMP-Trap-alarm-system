## Bảng: alarm_all ✅
Bảng `alarm_all` lưu trữ toàn bộ thông tin cảnh báo từ hệ thống mạng, bao gồm thông tin thiết bị, mức độ nghiêm trọng, thời gian xảy ra và vị trí địa lý của sự cố.

| Tên cột | Kiểu dữ liệu | Mô tả |
|--------|-------------|------|
| id | BIGINT (PK) | Khóa chính của bảng, định danh duy nhất cho mỗi bản ghi cảnh báo |
| nbi_alarm_id | VARCHAR | ID của alarm trong hệ thống NBI |
| ne | VARCHAR | Tên phần tử mạng (Network Element) phát sinh cảnh báo |
| nbi_alarm_type | VARCHAR | Loại cảnh báo |
| nbi_perceived_severity | VARCHAR | Mức độ nghiêm trọng của cảnh báo (Critical, Major, Minor, Warning) |
| nbi_specific_problem | TEXT | Mô tả chi tiết vấn đề gây ra cảnh báo |
| nbi_additional_text | TEXT | Thông tin bổ sung liên quan đến cảnh báo |
| nbi_object_instance | VARCHAR | Mã định danh của thiết bị gây ra cảnh báo |
| cell_id | VARCHAR | ID của cell mạng nơi xảy ra cảnh báo |
| site | VARCHAR | Tên hoặc mã trạm |
| ne_type | VARCHAR | Loại thiết bị mạng |
| ip_address | VARCHAR | Địa chỉ IP của thiết bị mạng |
| nbi_alarm_time | TIMESTAMP | Thời điểm cảnh báo được ghi nhận |
| nbi_clear_time | TIMESTAMP | Thời điểm cảnh báo được xử lý |
| last_updated_time | TIMESTAMP | Thời điểm bản ghi được cập nhật gần nhất |
| status | VARCHAR | Trạng thái của alarm (Active, Clear) |
| record_type | VARCHAR | Loại bản ghi (Start, End) |
| region | VARCHAR | Khu vực xảy ra cảnh báo |
| province | VARCHAR | Tỉnh/thành phố nơi xảy ra cảnh báo |
| district | VARCHAR | Quận/huyện nơi xảy ra cảnh báo |
