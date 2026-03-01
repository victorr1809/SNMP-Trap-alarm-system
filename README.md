# SNMP Trap alarm system
> Hệ thống xử lý bản tin SNMP Trap phát sinh từ thiết bị mạng viễn thông, lưu trữ, trực quan hoá, gửi cảnh báo tới người dùng

## Overview
Trap là bản tin được thiết bị mạng chủ động gửi tới máy manager khi có sự cố xảy ra, qua giao thức SNMP. Nhà mạng sử dụng dữ liệu này để xác định được thông tin của thiết bị đang gặp lỗi từ đó đưa ra phương hướng xử lý.

Trap không phải dòng dữ liệu ổn định mà nó có yếu tố bùng phát (Trap storm) khi hệ thống gặp lỗi nghiêm trọng. Dự án này xây dựng một pipeline xử lý bất đồng bộ, sử dụng **Kafka** làm vùng đệm, các **hàng đợi nội bộ** để giảm tải và **batch processing** khi lưu trữ vào database.

## Project Structure
```text
alarm-system/
│── pom.xml                   # Maven config
│── src/
│   ├── main/java/com/
│   │   ├── consumer/
│   │   │   ├── kafkaConsumer/    # Kafka consumer
│   │   │   ├── model/            # model data
│   │   │   ├── test/             # test DB utils
│   │   │   ├── trap/             # Handling and Saving
│   │   │   └── util/             # DB utils
│   │   ├── producer/
│   │   │   ├── app/              # Application entry point
│   │   │   ├── common/           # Common constants & config
│   │   │   ├── kafkaProducer/    # Kafka producer
│   │   │   ├── model/            # Data models
│   │   │   ├── trap/             # Trap receiver
│   │   │   └── util/             # DB utils
│   └── test/java/            
│── target/                   # Build output
│── config/                   # config files
│── docker-compose.yml        # Docker compose file
│── README.md
```
## System Architecture
<img width="2400" height="1350" alt="system architecture" src="https://github.com/user-attachments/assets/a1f8584f-b6bf-45af-b39b-62394e1e383c" />

### 1. Thu thập Trap
- Trap Receiver được viết bằng Java, lắng nghe Trap gửi về qua UDP socket
- Dữ liệu Trap được parse, làm giàu dữ liệu rồi gửi lên Kafka
  
### 2. Xử lý và lưu trữ
- Consumer đọc dữ liệu từ Kafka -> phân loại bản tin theo network (3G, 4G, Core) -> đẩy vào 3 hàng đợi riêng biệt
- 3 luồng song song lấy dữ liệu từ 3 hàng đợi và gọi procedure để lưu vào PostgreSQL theo logic sau:
<img width="1043" height="234" alt="Screenshot 2026-02-04 at 19 51 35" src="https://github.com/user-attachments/assets/f0be0b2e-b247-498d-9a8b-4641c0ac8845" />
<img width="1052" height="304" alt="Screenshot 2026-02-04 at 18 43 32" src="https://github.com/user-attachments/assets/2c5b5b95-e026-4f3c-8503-ad7afb3cdd95" />

### 3. Trực quan hoá và gửi cảnh báo:
- Sử dụng Grafana để vẽ dashboard
- Dùng Grafana Alerting thiết lập luật cảnh báo và gửi tới Discord khi thoả mãn điều kiện

### ERD
<img width="828" height="556" alt="ERD" src="https://github.com/user-attachments/assets/94bae5f6-eb97-427d-bea0-e19de8221586" />

### Dashboard (Grafana)
<img width="2864" height="2028" alt="dash" src="https://github.com/user-attachments/assets/37472ef6-80ba-46b1-bfa6-b703952cd281" />

### Grafana Alert Rules
<img width="1398" height="434" alt="Alert rules" src="https://github.com/user-attachments/assets/6819b68b-daef-43a7-8da0-4d52dd079e8c" />

### Cảnh báo được gửi tới Discord
<img width="1201" height="628" alt="send_to_discord" src="https://github.com/user-attachments/assets/88bf9513-bb28-49f2-b2e9-aa68b5d38ddb" />

## Key features 🎯
**1. Khả năng chịu tải và xử lý song song:** Sử dụng hàng đợi nội bộ, đặt ở những điểm dễ bị nghẽn (giữa TrapReceiver và Kafka, giữa Consumer và PostgreSQL) giúp hệ thống chịu tải tốt hơn

**2. Batch Processing:** Vì Update là thao tác chạy tốn thời gian hơn Insert nên Update sẽ được gom tới khi đủ 100 câu mới thực thi --> tối ưu hơn.

**3. Gửi cảnh báo:** Thiết lập các luật cảnh báo cho 1 vài mã lỗi nghiêm trọng (cáp quang, nguồn điện, mất kết nối,...) khi thoả mãn điều kiện sẽ gửi cảnh báo tới user.

## Setup 💻
### 🛠️ Yêu cầu hệ thống
Trước khi chạy, đảm bảo máy bạn đáp ứng những yêu cầu sau:
* **Java:** Version 17 hoặc cao hơn
* **Maven (Quản trị dự án):** Maven 3.8+.
* **Message Broker:** Apache Kafka & Zookeeper.
* **Database:** PostgreSQL 14+.
* **Visualization:** Grafana

> **Cài đặt Kafka, Kafka UI, Zookeeper, Grafana:** cài trên **Docker**
```text
docker-compose up -d
```
> **Cài đặt Maven**
```text
brew install maven      # For MacOS
```
### 💾 Chuẩn bị
- Tạo topic alarm-snmp trên Kafka
- Tạo bảng alarm.alarm_all trong PostgreSQL 
- Tạo procedure insert_alarm_all và update_alarm_all trong PostgreSQL
### 🚀 Cách thức chạy
Mở 3 terminal khác nhau để chạy các thành phần của hệ thống
> Compile project bằng maven để tải các dependencies về máy:
```text
mvn clean compile
```
> Khởi động TrapReceiver lắng nghe TRAP gửi qua port 1234 UDP SOCKET và đẩy lên Kafka
```text
mvn exec:java -Dexec.mainClass=com.producer.trap.TrapReceiver
```   
> Khởi động TrapSender gửi Trap tới port 1234 (localhost) UDP SOCKET
```text
mvn exec:java -Dexec.mainClass=com.producer.trap.TrapSender
```
> Khởi động TrapObs để poll dữ liệu từ Kafka và lưu DB
```text
mvn exec:java -Dexec.mainClass=com.consumer.trap.TrapObs
```

## Data source
- Trap data: dữ liệu Trap phát sinh từ thiết bị mạng của nhà mạng Mobifone, đây là bộ dữ liệu test được sử dụng để kiểm thử hệ thống
- Mapping data: dữ liệu các trạm phát sóng của Mobifone để thực hiện mapping
- Gửi và nhận: Trap được gửi qua UDP Socket
- Thời gian: 06/12/2025 - 07/12/2025

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)
![Kafka](https://img.shields.io/badge/Apache%20Kafka-000?style=for-the-badge&logo=apachekafka)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white)
![Grafana](https://img.shields.io/badge/Grafana-F46800?style=for-the-badge&logo=grafana&logoColor=white)

