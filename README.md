# SS13 HW03 - Half-Open và Time-Based Circuit Breaker

**Sinh viên:** Trương Hà Cẩm Linh

**Lớp:** IT214

**Mã sinh viên:** PTIT056

## Cấu hình SLA

Circuit Breaker `shippingClient` được cấu hình:

```yaml
resilience4j:
  circuitbreaker:
    instances:
      shippingClient:
        sliding-window-type: TIME_BASED
        sliding-window-size: 30
        minimum-number-of-calls: 5
        failure-rate-threshold: 1
        wait-duration-in-open-state: 20s
        permitted-number-of-calls-in-half-open-state: 3
        automatic-transition-from-open-to-half-open-enabled: true
```

- Cửa sổ thống kê: 30 giây gần nhất.
- Khi `OPEN`, ngừng gọi Shipping Service đúng 20 giây.
- Sau 20 giây, tự chuyển sang `HALF_OPEN`, không cần request mồi.
- Chỉ 3 request được đi qua để thăm dò; các request còn lại nhận `CallNotPermittedException`.
- `failure-rate-threshold: 1` bảo đảm chỉ khi cả 3 request thăm dò thành công thì circuit mới đóng. Một lỗi trong 3 request tương đương 33,3%, lớn hơn ngưỡng 1%, nên circuit mở lại.

## Vòng đời

```text
CLOSED -- lỗi vượt ngưỡng --> OPEN
OPEN   -- tự chờ 20 giây --> HALF_OPEN
HALF_OPEN -- 3/3 thành công --> CLOSED
HALF_OPEN -- có request lỗi --> OPEN
```

`TIME_BASED` nghĩa là Resilience4j thống kê các lời gọi xảy ra trong 30 giây gần nhất. Nó khác `COUNT_BASED`, vốn giữ một số lượng request gần nhất.

## Tự đánh giá

1. Ép circuit sang `OPEN`, chờ đủ 20 giây: circuit tự chuyển sang `HALF_OPEN` nhờ `automatic-transition-from-open-to-half-open-enabled: true`.
2. Bắn đồng thời 10 request trong `HALF_OPEN`: chỉ 3 request lấy được quyền gọi, 7 request còn lại bị từ chối.
3. Nếu cả 3 thành công: circuit về `CLOSED`.
4. Nếu có ít nhất một request lỗi: circuit quay lại `OPEN` trong 20 giây.

## Kiểm thử

```bash
./gradlew clean build
```

Test tự động xác nhận cấu hình 30 giây, thời gian chờ 20 giây, tự chuyển trạng thái và giới hạn chính xác 3/10 request trong `HALF_OPEN`.

Theo dõi trạng thái:

```text
GET http://localhost:8080/actuator/circuitbreakers
GET http://localhost:8080/actuator/circuitbreakerevents
```
