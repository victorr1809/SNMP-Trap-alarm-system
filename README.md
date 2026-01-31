# SNMP Trap alamr system
> Hệ thống xử lý bản tin SNMP Trap phát sinh từ thiết bị mạng, lưu trữ, trực quan hoá, gửi cảnh báo tới người dùng

## Giới thiệu dự án
- SNMP Trap là bản tin thiết bị mạng chủ động gửi tới cho máy manager khi có sự cố xảy ra, qua giao thức SNMP
- Nhà mạng sử dụng dữ liệu này để xác định được thông tin của thiết bị đang gặp lỗi và đưa ra phương hướng xử lý
- Xây dựng luồng tiếp nhận, xử lý dữ Trap, lưu trữ lâu dài, trực quan bằng Grafana

## Cấu trúc dự án
```text
alarm-system/
│── pom.xml
│── src/
│   ├── main/java/com/
        ├── manh/
            ├── kafka
            ├── test
            ├── trap
            ├── util
        ├── victor/
            ├── alarm
            ├── app
            ├── common
            ├── kafka
            ├── model
            ├── trap
            ├── util
    ├── test/java/com
├── target   
│── README.md
│── docker-compose.yml
## Kiến trúc hệ thống

## Luồng poll data và lưu trữ
<img width="1200" height="1400" alt="Luồng lưu trữ data lấy từ kafka" src="https://github.com/user-attachments/assets/e66da851-5054-4fb0-8b55-646c6a6749db" />

