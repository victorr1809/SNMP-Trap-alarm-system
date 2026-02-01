# SNMP Trap alarm system
> Hệ thống xử lý bản tin SNMP Trap phát sinh từ thiết bị mạng, lưu trữ, trực quan hoá, gửi cảnh báo tới người dùng

## Giới thiệu dự án
Trap là bản tin thiết bị mạng chủ động gửi tới máy manager khi có sự cố xảy ra, qua giao thức SNMP. Nhà mạng sử dụng dữ liệu này để xác định được thông tin của thiết bị đang gặp lỗi từ đó đưa ra phương hướng xử lý.

Dự án này xây dựng một hệ thống giám sát cho các thiết bị mạng dựa trên Trap. Các bản tin Trap được thu thập qua giao thức UDP, xử lý bất đồng bộ, lưu trữ và trực quan hóa để phục vụ theo dõi và cảnh báo.

Hệ thống được thiết kế để xử lý bất đồng bộ, sử dụng hàng đợi, luồng song song để tránh nghẽn khi lưu lượng Trap tăng cao.

## Cấu trúc dự án
```text
alarm-system/
│── pom.xml                   # Maven config
│── src/
│   ├── main/java/com/
│   │   ├── manh/
│   │   │   ├── kafka/        # Kafka consumer
│   │   │   ├── trap/         # Handling and Saving
│   │   │   ├── util/         # DB utils
│   │   │   └── test/         # test code
│   │   ├── victor/
│   │   │   ├── app/          # Application entry point
│   │   │   ├── alarm/        # Alarm processing logic
│   │   │   ├── kafka/        # Kafka producer
│   │   │   ├── trap/         # Trap receiver
│   │   │   ├── model/        # Data models
│   │   │   ├── common/       # Common constants & config
│   │   │   └── util/         # DB utils
│   └── test/java/            
│── target/                   # Build output
│── config/                   # config files
│── docker-compose.yml        # Docker compose file
│── README.md
```
## Kiến trúc hệ thống
<img width="1198" height="390" alt="Screenshot 2026-01-31 at 22 51 34" src="https://github.com/user-attachments/assets/84b41cfc-9c94-49d4-8199-bf479618d730" />

### 1. Data collection
- Trap Receiver được viết bằng Java, lắng nghe Trap gửi về qua UDP socket
- Dữ liệu Trap được xử lý qua rồi gửi lên Kafka
  
### 2. Xử lý dữ liệu bất đồng bộ

### 3. Lưu trữ dữ liệu song song

### 4. Gửi cảnh báo tới người dùng

