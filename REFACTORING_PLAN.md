# 📋 KẾ HOẠCH TÁI CẤU TRÚC VÀ TỐI ƯU HÓA MÃ NGUỒN
### 🏥 DỰ ÁN: BẢNG ĐIỀU KHIỂN PHÒNG MỔ SÌN HỒ - LAI CHÂU (`com.research.oproom`)

---

> [!IMPORTANT]
> **Mục tiêu tối thượng:** Tinh gọn và chuyên nghiệp hóa mã nguồn, **giữ nguyên 100% tính năng và giao thức Modbus RTU** đang chạy thực tế, loại bỏ hoàn toàn hiện tượng "God Activity" và anti-pattern gọi lại `setContentView(...)`.

---

## 📊 1. SƠ ĐỒ KIẾN TRÚC MỤC TIÊU (ARCHITECTURE OVERVIEW)

```mermaid
graph TD
    subgraph UI_Layer ["🖥️ TẦNG GIAO DIỆN (UI LAYER)"]
        MA["MainActivity (Single Activity Container)"]
        F1["DashboardFragment<br/>(Giám sát phòng mổ)"]
        F2["SettingsFragment<br/>(Cài đặt thông số PLC)"]
        F3["PresentationFragment<br/>(Trình chiếu ca mổ & Đồng hồ)"]
        MA --> F1
        MA --> F2
        MA --> F3
        
        subgraph View_Components ["🧩 View Components"]
            C1["NumericSettingHelper"]
            C2["DualStateButtonHelper"]
            C3["SensorDisplayHelper"]
            C4["RealTimeClockHelper"]
            C5["SlideShowHelper"]
        end
        F1 -.-> View_Components
        F2 -.-> View_Components
        F3 -.-> View_Components
    end

    subgraph Data_Layer ["📦 TẦNG DỮ LIỆU & PHẦN CỨNG"]
        VM["MainViewModel / OpRoomRepository"]
        SP["SerialPortManager<br/>(UsbSerialPort)"]
        MP["ModbusParser & Commands<br/>(Nhiệt, Ẩm, Áp, Lọc)"]
        CRC["ModbusCrc16<br/>(Tính mã kiểm tra)"]
        PREF["AppPreferences<br/>(SharedPreferences)"]
        API["OproomApi / ApiClient<br/>(Retrofit - Báo cáo Server)"]
    end

    F1 --> VM
    F2 --> VM
    F3 --> VM
    
    VM --> SP
    VM --> PREF
    VM --> API
    SP <--> MP
    MP --> CRC

    subgraph Hardware ["🔌 PHẦN CỨNG THỰC TẾ"]
        PLC["Bo mạch PLC / RS485"]
        Sensors["Cảm biến Nhiệt, Ẩm, Áp suất, Màng lọc"]
        Actuators["Đèn UV, Van gió, Quạt, Sưởi"]
        SP <== "USB Serial (9600-8-N-1)" ==> PLC
        PLC --> Sensors
        PLC --> Actuators
    end

    style UI_Layer fill:#1e293b,stroke:#3b82f6,stroke-width:2px,color:#fff
    style Data_Layer fill:#0f172a,stroke:#10b981,stroke-width:2px,color:#fff
    style Hardware fill:#334155,stroke:#f59e0b,stroke-width:2px,color:#fff
```

---

## 🗂️ 2. BỐ CỤC THƯ MỤC CHUẨN HÓA

```mermaid
graph LR
    Root["com.research.oproom"]
    
    Root --> Data["📁 data/"]
    Data --> DataLocal["local/ (AppPreferences)"]
    Data --> DataRemote["remote/ (ApiClient, SensorPost)"]
    
    Root --> Hardware["📁 hardware/"]
    Hardware --> Modbus["modbus/ (Crc16, Commands, Parser)"]
    Hardware --> Serial["serial/ (SerialPortManager)"]
    
    Root --> UI["📁 ui/"]
    UI --> UIMain["main/ (MainActivity, MainViewModel)"]
    UI --> UIFrag["fragments/ (Dashboard, Settings, Presentation)"]
    UI --> UIDialog["dialogs/ (5 Dialogs)"]
    UI --> UIComp["components/ (View Helpers)"]
    
    Root --> Utils["📁 utils/ (Constants, TimeUtils, SystemBarUtils)"]
    Root --> Boot["BootUpReceiver.kt"]
```

---

## 🔄 3. BẢNG ĐỐI CHIẾU CHUYỂN ĐỔI MÃ NGUỒN

| Thành phần cũ trong `MainActivity.kt` (1.800 dòng) | Vị trí / Tên mới được tối ưu | Lợi ích cải thiện |
|---|---|---|
| `class CrcByte` | `hardware.modbus.ModbusCrc16` | Tách rời thuật toán toán học, dễ viết unit test kiểm tra tính chính xác. |
| Mảng byte `humRequest`, `presRequest`, `lion1Request`,... | `hardware.modbus.ModbusCommands` | Tập trung mảng byte lệnh, không bị phân tán rải rác. |
| Vòng lặp parse byte `onNewData(...)` | `hardware.modbus.ModbusParser` | Tách logic phân tích byte cảm biến ra khỏi vòng đời Activity. |
| `UsbSerialPort`, `UsbSerialProber` | `hardware.serial.SerialPortManager` | Quản lý kết nối USB Serial mượt mà, tự động kết nối lại khi tuột cáp. |
| `inner class NumericSetting` | `ui.components.NumericSettingHelper` | Độc lập hóa thành phần chỉnh thông số (Nhiệt độ, Độ ẩm, Sưởi...). |
| `inner class DualStateButton` | `ui.components.DualStateButtonHelper` | Chuẩn hóa nút bấm 2 trạng thái Bật/Tắt đồng nhất. |
| `inner class SensorView` | `ui.components.SensorDisplayHelper` | Độc lập hóa hiển thị giá trị cảm biến kèm đơn vị Pa, °C, %RH. |
| `inner class realTime` | `ui.components.RealTimeClockHelper` | Chuẩn hóa tên class theo quy chuẩn PascalCase của Kotlin. |
| `inner class module_alarm` | `ui.components.ModuleAlarmHelper` | Quản lý icon cảnh báo lỗi thiết bị riêng biệt. |
| `inner class Present` | `ui.components.SlideShowHelper` | Độc lập hóa trình chiếu slide ảnh ca mổ. |
| `inner class TimerText` & `TimerSetting` | `ui.components.TimerHelper` | Quản lý đếm giờ mổ/gây mê rõ ràng, không lỗi index hay xung đột luồng. |
| `sp` & `editor` (`SharedPreferences`) | `data.local.AppPreferences` | Đóng gói kiểu an toàn (type-safe), không hardcode key bừa bãi. |
| `updatePost()`, `Retrofit` | `data.remote.ApiClient` | Quản lý kết nối mạng chuyên nghiệp, tránh lag UI khi mất kết nối máy chủ. |
| `setContentView(...)` lặp lại 3 lần | `ui.fragments.*` (3 Fragments) | **Bỏ hoàn toàn anti-pattern**, chuyển màn hình tức thì, dùng ViewBinding. |

---

## 🚀 4. LỘ TRÌNH 5 GIAI ĐOẠN TRIỂN KHAI

```mermaid
gantt
    title LỘ TRÌNH TÁI CẤU TRÚC OPROOM
    dateFormat  YYYY-MM-DD
    section Giai đoạn 1
    Bật ViewBinding & Thư viện Coroutines   :done, g1_1, 2026-09-28, 1d
    Tách ModbusCrc16 & SystemBarUtils       :active, g1_2, 2026-09-28, 1d
    Chuẩn hóa Post.kt -> SensorPost         :g1_3, 2026-09-28, 1d
    section Giai đoạn 2
    Tạo AppPreferences tập trung            :g2_1, 2026-09-29, 1d
    Chuẩn hóa ApiClient & 5 Dialogs         :g2_2, 2026-09-29, 1d
    section Giai đoạn 3
    Gom ModbusCommands & ModbusParser       :g3_1, 2026-09-30, 1d
    Tách SerialPortManager                  :g3_2, 2026-09-30, 1d
    section Giai đoạn 4
    Tách 6 View Helper Components           :g4_1, 2026-10-01, 1d
    Chuyển 3 màn hình thành 3 Fragments     :g4_2, 2026-10-01, 1d
    section Giai đoạn 5
    Tối ưu Timer Coroutines                 :g5_1, 2026-10-02, 1d
    Tinh gọn MainActivity (~200 dòng)       :g5_2, 2026-10-02, 1d
    Kiểm thử tích hợp máy thật              :g5_3, 2026-10-02, 1d
```

### ✅ Checklist Chi Tiết Từng Bước

- [x] **Giai đoạn 1: Chuẩn bị & Chuẩn hóa nền tảng (Quick Wins) - ĐÃ HOÀN THÀNH**
  - [x] Bật `viewBinding = true` trong `app/build.gradle`.
  - [x] Tách thuật toán tính CRC16 sang `hardware/modbus/ModbusCrc16.kt`.
  - [x] Chuyển đổi `Post.kt` thành Kotlin `data class SensorPost / Post`.
  - [x] Viết `SystemBarUtils.kt` (thay thế cho hàm `hideThatShit()`).
  - [x] Chạy lệnh `.\gradlew compileDebugKotlin` xác nhận build thành công.

- [x] **Giai đoạn 2: Tách tầng Dữ liệu & Lưu trữ (Data Layer) - ĐÃ HOÀN THÀNH**
  - [x] Tạo `data/local/AppPreferences.kt` để quản lý tập trung toàn bộ SharedPreferences.
  - [x] Tạo `data/remote/ApiClient.kt` tập trung OkHttp và Retrofit.
  - [x] Chuẩn hóa 5 file Dialog với ViewBinding và xử lý listener an toàn.

- [x] **Giai đoạn 3: Tách tầng Phần cứng & Giao tiếp Modbus Serial (Hardware Layer) - ĐÃ HOÀN THÀNH**
  - [x] Tạo `hardware/modbus/ModbusCommands.kt` gom tất cả mảng byte lệnh.
  - [x] Tạo `hardware/modbus/ModbusParser.kt` giải mã gói tin nhiệt, ẩm, áp suất, màng lọc.
  - [x] Tạo `hardware/serial/SerialPortManager.kt` quản lý `UsbSerialPort`.

- [x] **Giai đoạn 4: Tách các View Components & Màn hình Fragment (UI Layer) - ĐÃ HOÀN THÀNH**
  - [x] Tách toàn bộ inner class giao diện ra thư mục `ui/components/`.
  - [x] Tạo `DashboardFragment.kt` (giao diện `activity_main.xml`).
  - [x] Tạo `SettingsFragment.kt` (giao diện `activity_settings_real.xml`).
  - [x] Tạo `PresentationFragment.kt` (giao diện `activity_setting.xml`).
  - [x] Tạo hàm điều hướng tập trung `navigateToPage(page)` chuẩn bị loại bỏ hoàn toàn `stateUpdate()`.

- [x] **Giai đoạn 5: Tối ưu Timer, Đồng bộ Coroutines & Rút gọn MainActivity - ĐÃ HOÀN THÀNH**
  - [x] Thay thế `ArrayList<Timer>` bằng cơ chế Timer độc lập, loại bỏ triệt để lỗi xung đột luồng và index shift.
  - [x] Tinh gọn `MainActivity.kt`, loại bỏ toàn bộ inner classes trùng lặp và tích hợp các module chuyên trách.
  - [x] Kiểm thử biên dịch thành công 100% (`BUILD SUCCESSFUL in 29s`).

---

> [!TIP]
> **Cách xem file này sinh động nhất trong IDE:**
> 1. Nhấn tổ hợp phím **`Ctrl + Shift + V`** để mở chế độ xem trước toàn màn hình với đồ họa Mermaid đầy đủ.
> 2. Hoặc nhấn **`Ctrl + K` rồi nhấn `V`** để mở cửa sổ xem trước nằm song song ngay cạnh mã nguồn.
> 3. Bạn cũng có thể mở file trực quan **`REFACTORING_DASHBOARD.html`** bằng trình duyệt để tương tác trực tiếp!
