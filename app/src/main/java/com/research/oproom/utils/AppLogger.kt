package com.research.oproom.utils

import android.content.Context
import android.util.Log
import androidx.core.content.ContextCompat
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Tiện ích ghi nhật ký (Log) chuyên dụng cho hệ thống phòng mổ trên Tinker Board.
 * - Ghi log sự kiện ứng dụng có cấu trúc vào `log_YYYY-MM-DD.txt`.
 * - Tự động bắt 100% toàn bộ log từ Terminal/Logcat hệ thống (kể cả thư viện bên ngoài) vào `terminal_YYYY-MM-DD.txt`.
 * - Tự động bắt và lưu lại toàn bộ vết sập ứng dụng (Fatal Crash / StackTrace) khi có lỗi ngoài ý muốn.
 * - Tự động xóa log cũ sau 15 ngày để bảo vệ bộ nhớ Flash eMMC.
 * - Hỗ trợ trích xuất toàn bộ file log sang USB cắm ngoài khi nhận mật khẩu 'viewlog'.
 */
object AppLogger {

    private const val DEFAULT_TAG = "OpRoom"
    private const val RETENTION_DAYS = 15 // Tự động xóa log sự kiện & terminal cũ hơn 15 ngày
    private const val SENSOR_RETENTION_DAYS = 2 // Tự động xóa log thông số cảm biến cũ hơn 2 ngày

    private var logDirectory: File? = null
    private val fileExecutor = Executors.newSingleThreadExecutor()
    private var logcatProcess: Process? = null
    private var lastCleanCheckTime: Long = 0L

    private val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
    private val fileDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    /**
     * Mô hình dữ liệu thông số cảm biến phòng mổ phục vụ lưu vết vào file riêng.
     */
    data class SensorSnapshot(
        val tempCelsius: Float,
        val humPercent: Float,
        val pressurePa: Int,
        val filterDpPa: Int,
        val filterGood: Boolean,
        val filterCount: Int,
        val heaterAlarm: Boolean,
        val ventAlarm: Boolean,
        val serialConnected: Boolean,
        val hasIncident: Boolean,
        val incidentDescription: String = ""
    )

    /**
     * Kết quả xuất log sang USB.
     */
    data class ExportResult(
        val success: Boolean,
        val message: String,
        val exportedFilesCount: Int = 0,
        val targetPath: String = ""
    )

    /**
     * Khởi tạo bộ ghi log. Gọi hàm này trong onCreate của MainActivity.
     * @param captureTerminal Nếu true, sẽ tự động bắt toàn bộ mọi dòng log ở Terminal/Logcat vào file riêng.
     */
    fun init(context: Context, captureTerminal: Boolean = true) {
        // Lưu tại /sdcard/Android/data/com.research.oproom/files/logs/
        val dir = context.getExternalFilesDir("logs") ?: File(context.filesDir, "logs")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        logDirectory = dir

        // 1. Tự động bắt mọi lỗi Crash ngoài ý muốn (Uncaught Exceptions)
        setupCrashHandler()

        // 2. Tự động dọn dẹp các file log cũ hơn 15 ngày
        cleanOldLogs()

        // 3. Khởi động tiến trình bắt toàn bộ Terminal / Logcat nếu được yêu cầu
        if (captureTerminal) {
            startTerminalCapture()
        }

        i(DEFAULT_TAG, "==================================================")
        i(DEFAULT_TAG, "Bảng điều khiển phòng mổ Sìn Hồ khởi động")
        i(DEFAULT_TAG, "Thư mục lưu log: ${dir.absolutePath}")
        i(DEFAULT_TAG, "==================================================")
    }

    // --- Các hàm ghi log theo cấp độ ---

    fun d(tag: String = DEFAULT_TAG, message: String) {
        Log.d(tag, message)
        writeLogToFile("DEBUG", tag, message)
    }

    fun i(tag: String = DEFAULT_TAG, message: String) {
        Log.i(tag, message)
        writeLogToFile("INFO ", tag, message)
    }

    fun w(tag: String = DEFAULT_TAG, message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Log.w(tag, message, throwable)
        } else {
            Log.w(tag, message)
        }
        writeLogToFile("WARN ", tag, message, throwable)
    }

    fun e(tag: String = DEFAULT_TAG, message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Log.e(tag, message, throwable)
        } else {
            Log.e(tag, message)
        }
        writeLogToFile("ERROR", tag, message, throwable)
    }

    /**
     * Ghi dòng log vào file sự kiện của ngày hiện tại (log_YYYY-MM-DD.txt).
     */
    private fun writeLogToFile(level: String, tag: String, message: String, throwable: Throwable? = null) {
        val dir = logDirectory ?: return

        fileExecutor.execute {
            try {
                val now = Date()
                val timestamp = timeFormat.format(now)
                val fileName = "log_${fileDateFormat.format(now)}.txt"
                val logFile = File(dir, fileName)

                val logLine = StringBuilder()
                    .append("[$timestamp] ")
                    .append("[$level] ")
                    .append("[${tag.padEnd(10)}] ")
                    .append(message)
                    .toString()

                PrintWriter(FileWriter(logFile, true)).use { writer ->
                    writer.println(logLine)
                    throwable?.printStackTrace(writer)
                }
            } catch (e: Exception) {
                Log.e(DEFAULT_TAG, "Không thể ghi log ra file: ${e.message}")
            }
        }
    }

    /**
     * Ghi nhận thông số cảm biến vào file riêng `sensor_YYYY-MM-DD.txt`.
     * - Chu kỳ bình thường: 5 giây / 1 lần.
     * - Khi có sự cố: lưu liên tục (1 giây / 1 lần).
     * - Tự động xóa file cũ sau 2 ngày lưu trữ.
     */
    fun logSensorReading(snapshot: SensorSnapshot) {
        val dir = logDirectory ?: return

        fileExecutor.execute {
            try {
                val now = Date()
                val timestamp = timeFormat.format(now)
                val fileName = "sensor_${fileDateFormat.format(now)}.txt"
                val sensorFile = File(dir, fileName)

                val isNewFile = !sensorFile.exists() || sensorFile.length() == 0L

                PrintWriter(FileWriter(sensorFile, true)).use { writer ->
                    if (isNewFile) {
                        writer.println("# ==========================================================================================")
                        writer.println("# NHẬT KÝ THÔNG SỐ CẢM BIẾN PHÒNG MỔ SÌN HỒ - LAI CHÂU")
                        writer.println("# Ngày bắt đầu: ${fileDateFormat.format(now)} | Bình thường: 5s/lần | Khi có sự cố: Lưu liên tục (1s/lần)")
                        writer.println("# Cơ chế lưu trữ: Tự động xóa sau 2 ngày")
                        writer.println("# ==========================================================================================")
                    }

                    val stateLabel = if (snapshot.hasIncident) {
                        "[SỰ CỐ: ${snapshot.incidentDescription}]"
                    } else {
                        "[BÌNH THƯỜNG]"
                    }

                    val line = String.format(
                        Locale.US,
                        "[%s] %-35s | Nhiệt độ: %5.1f°C | Độ ẩm: %5.1f%% | Áp suất phòng: %3d Pa | Chênh áp lọc: %3d Pa | Màng lọc: %s (%dh) | Sấy: %s | Quạt: %s | Modbus: %s",
                        timestamp,
                        stateLabel,
                        snapshot.tempCelsius,
                        snapshot.humPercent,
                        snapshot.pressurePa,
                        snapshot.filterDpPa,
                        if (snapshot.filterGood) "Đạt" else "KÉM",
                        snapshot.filterCount,
                        if (snapshot.heaterAlarm) "BÁO ĐỘNG" else "OK",
                        if (snapshot.ventAlarm) "BÁO ĐỘNG" else "OK",
                        if (snapshot.serialConnected) "Đã nối" else "MẤT KẾT NỐI"
                    )
                    writer.println(line)
                }

                // Tự động kiểm tra dọn dẹp mỗi 1 giờ
                val currentTimeMs = System.currentTimeMillis()
                if (currentTimeMs - lastCleanCheckTime > 3_600_000L) {
                    lastCleanCheckTime = currentTimeMs
                    cleanOldLogs()
                }
            } catch (e: Exception) {
                Log.e(DEFAULT_TAG, "Không thể ghi sensor log ra file: ${e.message}")
            }
        }
    }

    /**
     * Tự động khởi chạy tiến trình logcat ngầm của Android để bắt 100% mọi log xuất hiện ở Terminal
     * (bao gồm log của USB Serial, Retrofit, Kotlin coroutines, System.out) lưu vào file `terminal_YYYY-MM-DD.txt`.
     */
    fun startTerminalCapture() {
        fileExecutor.execute {
            try {
                val dir = logDirectory ?: return@execute
                val todayStr = fileDateFormat.format(Date())
                val terminalFile = File(dir, "terminal_$todayStr.txt")

                // Dừng tiến trình cũ nếu đang chạy
                logcatProcess?.destroy()

                // Chạy lệnh logcat của Android ghi thẳng vào file
                // -v time: kèm mốc thời gian chi tiết
                // -f: đường dẫn file đích
                val command = arrayOf("logcat", "-v", "time", "-f", terminalFile.absolutePath, "*:D")
                logcatProcess = Runtime.getRuntime().exec(command)
                Log.i(DEFAULT_TAG, "Đã kích hoạt bắt toàn bộ Terminal vào: ${terminalFile.name}")
            } catch (e: Exception) {
                Log.e(DEFAULT_TAG, "Không thể bắt logcat terminal: ${e.message}")
            }
        }
    }

    /**
     * Tự động bắt mọi lỗi sập ứng dụng (Crash / Exception) ghi vào file log trước khi app thoát.
     */
    private fun setupCrashHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            e("CRASH_FATAL", "ỨNG DỤNG BỊ DỪNG ĐỘT NGỘT trên luồng '${thread.name}': ${throwable.message}", throwable)
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }

    /**
     * Xuất toàn bộ file log sang ổ đĩa USB Flash cắm vào cổng Tinker Board.
     */
    fun exportLogsToUsb(context: Context): ExportResult {
        val dir = logDirectory ?: return ExportResult(false, "Thư mục log không tồn tại hoặc chưa khởi tạo.")
        val logFiles = dir.listFiles { file ->
            file.isFile && (file.name.startsWith("log_") || file.name.startsWith("terminal_") || file.name.startsWith("sensor_"))
        }

        if (logFiles.isNullOrEmpty()) {
            return ExportResult(false, "Không tìm thấy file log nào để xuất.")
        }

        val usbRoot = findUsbMountPoint(context)
            ?: return ExportResult(
                false,
                "Không tìm thấy ổ đĩa USB nào được cắm vào Tinker Board!\n\nVui lòng cắm USB vào cổng của thiết bị và thử lại."
            )

        return try {
            val targetDir = File(usbRoot, "OpRoom_SinHo_Logs")
            if (!targetDir.exists()) {
                targetDir.mkdirs()
            }

            var count = 0
            for (file in logFiles) {
                val destFile = File(targetDir, file.name)
                file.copyTo(destFile, overwrite = true)
                count++
            }

            val msg = "Đã xuất thành công $count file log sang USB!\n\nThư mục lưu trữ trên USB:\n${targetDir.absolutePath}"
            i("USB_EXPORT", msg)
            ExportResult(true, msg, count, targetDir.absolutePath)
        } catch (e: Exception) {
            val err = "Lỗi khi sao chép file log sang USB: ${e.message}"
            e("USB_EXPORT", err, e)
            ExportResult(false, err)
        }
    }

    /**
     * Nén tất cả các file log hiện có thành một file .zip duy nhất trong thư mục cache.
     */
    fun zipAllLogs(context: Context): File? {
        val dir = logDirectory ?: return null
        val logFiles = dir.listFiles { f ->
            f.isFile && (f.name.startsWith("log_") || f.name.startsWith("terminal_") || f.name.startsWith("sensor_"))
        }
        if (logFiles.isNullOrEmpty()) return null

        val cacheDir = context.cacheDir
        val zipName = "OpRoom_Logs_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.zip"
        val zipFile = File(cacheDir, zipName)

        return try {
            ZipOutputStream(FileOutputStream(zipFile)).use { out ->
                for (file in logFiles) {
                    val entry = ZipEntry(file.name)
                    out.putNextEntry(entry)
                    file.inputStream().use { input ->
                        input.copyTo(out)
                    }
                    out.closeEntry()
                }
            }
            zipFile
        } catch (e: Exception) {
            e(DEFAULT_TAG, "Lỗi nén file log: ${e.message}", e)
            null
        }
    }

    /**
     * Gửi toàn bộ file log (dưới dạng file nén .zip) tới máy tính theo địa chỉ IP do người dùng nhập.
     * Hỗ trợ format: "192.168.1.100" hoặc "192.168.1.100:8080"
     */
    fun sendLogsToIp(context: Context, rawIp: String): ExportResult {
        val zipFile = zipAllLogs(context)
            ?: return ExportResult(false, "Không có file log nào để gửi hoặc lỗi khi nén log.")

        val cleanInput = rawIp.trim().replace("http://", "").replace("https://", "")
        val host: String
        val port: Int
        if (cleanInput.contains(":")) {
            val parts = cleanInput.split(":")
            host = parts[0]
            port = parts[1].toIntOrNull() ?: 8080
        } else {
            host = cleanInput
            port = 8080
        }

        if (host.isEmpty()) {
            return ExportResult(false, "Địa chỉ IP không hợp lệ.")
        }

        val url = "http://$host:$port/upload"
        i("LOG_SENDER", "Bắt đầu gửi file log (${zipFile.length() / 1024} KB) tới máy tính: $url")

        return try {
            val client = OkHttpClient.Builder()
                .connectTimeout(8, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()

            val mediaType = "application/zip".toMediaTypeOrNull()
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "file",
                    zipFile.name,
                    zipFile.asRequestBody(mediaType)
                )
                .build()

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val msg = "Đã gửi thành công toàn bộ file log tới máy tính ($host:$port)!\n\nFile: ${zipFile.name}"
                    i("LOG_SENDER", msg)
                    zipFile.delete()
                    ExportResult(true, msg, 1, url)
                } else {
                    val errMsg = "Máy tính từ chối nhận file (Mã lỗi HTTP ${response.code})."
                    w("LOG_SENDER", errMsg)
                    ExportResult(false, errMsg)
                }
            }
        } catch (e: Exception) {
            val errMsg = "Không thể kết nối tới máy tính $host:$port!\n\nChi tiết lỗi: ${e.message}\n(Vui lòng kiểm tra lại IP và đảm bảo máy tính đã chạy phần mềm nhận file)"
            e("LOG_SENDER", errMsg, e)
            ExportResult(false, errMsg)
        }
    }

    /**
     * Dò tìm phân vùng ổ đĩa USB cắm ngoài trên Tinker Board (Rockchip Android).
     */
    private fun findUsbMountPoint(context: Context): File? {
        // Cách 1: Quét qua API chuẩn của Android (ContextCompat.getExternalFilesDirs)
        try {
            val externalDirs = ContextCompat.getExternalFilesDirs(context, null)
            for (f in externalDirs) {
                if (f != null) {
                    val path = f.absolutePath
                    if (!path.contains("emulated")) {
                        // Thử lấy thư mục gốc của USB (thường là /storage/XXXX-XXXX)
                        val match = Regex("/storage/[^/]+").find(path)
                        if (match != null) {
                            val root = File(match.value)
                            if (root.exists() && isWritableDirectory(root)) return root
                        }
                        if (isWritableDirectory(f)) return f
                    }
                }
            }
        } catch (_: Exception) {}

        // Cách 2: Quét trực tiếp thư mục /storage/ trên Tinker Board
        try {
            val storage = File("/storage")
            if (storage.exists() && storage.isDirectory) {
                val candidates = storage.listFiles { f ->
                    f.isDirectory && f.name != "emulated" && f.name != "self" && f.name != "knox-emulated"
                }
                candidates?.forEach { candidate ->
                    if (isWritableDirectory(candidate)) return candidate
                }
            }
        } catch (_: Exception) {}

        // Cách 3: Quét các đường dẫn mount USB đặc thù của chip Rockchip Android
        val mntPaths = arrayOf("/mnt/media_rw", "/mnt/usb_storage", "/mnt/usbhost", "/storage/usbdisk", "/mnt/udisk")
        for (mntPath in mntPaths) {
            try {
                val dir = File(mntPath)
                if (dir.exists() && dir.isDirectory) {
                    dir.listFiles()?.forEach { candidate ->
                        if (candidate.isDirectory && isWritableDirectory(candidate)) {
                            return candidate
                        }
                    }
                }
            } catch (_: Exception) {}
        }

        return null
    }

    private fun isWritableDirectory(dir: File): Boolean {
        return try {
            val testFile = File(dir, ".test_usb_probe_${System.currentTimeMillis()}")
            if (testFile.createNewFile()) {
                testFile.delete()
                true
            } else {
                dir.canWrite()
            }
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Đọc các dòng log mới nhất của ngày hôm nay (phục vụ hiển thị trên màn hình).
     */
    fun getTodayLogs(maxLines: Int = 200): String {
        val dir = logDirectory ?: return "Chưa khởi tạo Logger"
        val todayFile = File(dir, "log_${fileDateFormat.format(Date())}.txt")
        if (!todayFile.exists()) return "Chưa có log trong ngày hôm nay."

        return try {
            val lines = todayFile.readLines()
            if (lines.size <= maxLines) {
                lines.joinToString("\n")
            } else {
                lines.takeLast(maxLines).joinToString("\n")
            }
        } catch (e: Exception) {
            "Lỗi khi đọc file log: ${e.message}"
        }
    }

    /**
     * Lấy danh sách tất cả file log (cả sự kiện lẫn terminal) đang có trong máy.
     */
    fun getAllLogFiles(): List<File> {
        val dir = logDirectory ?: return emptyList()
        return dir.listFiles { file -> 
            file.isFile && (file.name.startsWith("log_") || file.name.startsWith("terminal_") || file.name.startsWith("sensor_")) 
        }?.sortedByDescending { it.name } ?: emptyList()
    }

    /**
     * Tự động xóa các file log cũ để bảo vệ bộ nhớ Flash eMMC.
     * - File sự kiện (`log_`) và terminal (`terminal_`): xóa sau defaultRetentionDays ngày (15 ngày).
     * - File thông số cảm biến (`sensor_`): xóa sau sensorRetentionDays ngày (2 ngày).
     */
    fun cleanOldLogs(defaultRetentionDays: Int = RETENTION_DAYS, sensorRetentionDays: Int = SENSOR_RETENTION_DAYS) {
        fileExecutor.execute {
            try {
                val dir = logDirectory ?: return@execute
                val now = System.currentTimeMillis()
                val generalCutoff = now - (defaultRetentionDays.toLong() * 24 * 60 * 60 * 1000)
                val sensorCutoff = now - (sensorRetentionDays.toLong() * 24 * 60 * 60 * 1000)

                dir.listFiles()?.forEach { file ->
                    if (file.isFile) {
                        when {
                            file.name.startsWith("sensor_") && file.lastModified() < sensorCutoff -> {
                                file.delete()
                                Log.d(DEFAULT_TAG, "Đã xóa file sensor log cũ (> $sensorRetentionDays ngày): ${file.name}")
                            }
                            (file.name.startsWith("log_") || file.name.startsWith("terminal_")) && file.lastModified() < generalCutoff -> {
                                file.delete()
                                Log.d(DEFAULT_TAG, "Đã xóa file log cũ (> $defaultRetentionDays ngày): ${file.name}")
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(DEFAULT_TAG, "Lỗi dọn dẹp log cũ: ${e.message}")
            }
        }
    }

    /**
     * Xóa sạch toàn bộ file log khi cần reset thiết bị.
     */
    fun clearAllLogs() {
        fileExecutor.execute {
            try {
                logcatProcess?.destroy()
                logDirectory?.listFiles()?.forEach { if (it.isFile) it.delete() }
            } catch (e: Exception) {
                Log.e(DEFAULT_TAG, "Lỗi xóa log: ${e.message}")
            }
        }
    }
}
