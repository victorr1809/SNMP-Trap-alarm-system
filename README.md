# SNMP Trap alamr system
> Hệ thống xử lý bản tin SNMP Trap phát sinh từ thiết bị mạng, lưu trữ, trực quan hoá, gửi cảnh báo tới người dùng

## Giới thiệu dự án
- SNMP Trap là bản tin thiết bị mạng chủ động gửi tới cho máy manager khi có sự cố xảy ra, qua giao thức SNMP
- Nhà mạng sử dụng dữ liệu này để xác định được thông tin của thiết bị đang gặp lỗi và đưa ra phương hướng xử lý
- Xây dựng luồng xử lý dữ liệu bất đồng bộ

## Cấu trúc dự án
```text
project-root/
│── src/
│   ├── ...
│── data/
│── docs/
│── docker/
│── README.md
│── requirements.txt
│── docker-compose.yml
