package com.research.oproom.hardware.serial

import android.content.Context
import android.hardware.usb.UsbManager
import android.util.Log
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import com.hoho.android.usbserial.util.SerialInputOutputManager
import java.io.IOException
import java.util.concurrent.Executors

/**
 * Quản lý kết nối, cấu hình và truyền nhận dữ liệu qua cổng USB Serial RS485.
 */
class SerialPortManager(private val context: Context) {

    interface SerialDataListener {
        fun onNewData(data: ByteArray)
        fun onRunError(e: Exception)
        fun onConnectionStatusChanged(connected: Boolean, deviceName: String?)
    }

    var listener: SerialDataListener? = null
    private var usbSerialPort: UsbSerialPort? = null
    private var ioManager: SerialInputOutputManager? = null
    private val executor = Executors.newSingleThreadExecutor()

    val isConnected: Boolean
        get() = usbSerialPort?.isOpen == true

    /**
     * Tìm kiếm và mở kết nối đến thiết bị USB Serial đầu tiên tìm thấy.
     * @return true nếu kết nối thành công, false nếu không tìm thấy thiết bị hoặc mở thất bại.
     */
    fun connect(baudRate: Int = 9600): Boolean {
        val usbManager = context.getSystemService(Context.USB_SERVICE) as? UsbManager ?: return false
        val availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(usbManager)
        if (availableDrivers.isEmpty()) {
            Log.w(TAG, "Không tìm thấy thiết bị USB Serial nào cắm vào máy.")
            listener?.onConnectionStatusChanged(false, null)
            return false
        }

        val driver = availableDrivers[0]
        val connection = usbManager.openDevice(driver.device)
        if (connection == null) {
            Log.e(TAG, "Không thể mở kết nối USB (thiếu quyền USB permission hoặc lỗi cổng).")
            listener?.onConnectionStatusChanged(false, driver.device.productName)
            return false
        }

        return try {
            val port = driver.ports[0]
            port.open(connection)
            port.setParameters(baudRate, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE)

            usbSerialPort = port

            ioManager = SerialInputOutputManager(port, object : SerialInputOutputManager.Listener {
                override fun onNewData(data: ByteArray) {
                    listener?.onNewData(data)
                }

                override fun onRunError(e: Exception) {
                    Log.e(TAG, "Lỗi luồng đọc Serial: ${e.message}")
                    listener?.onRunError(e)
                    listener?.onConnectionStatusChanged(false, driver.device.productName)
                }
            }).apply {
                setThreadPriority(1)
                start()
            }

            Log.i(TAG, "Kết nối USB Serial thành công: ${driver.device.productName} @ $baudRate bps")
            listener?.onConnectionStatusChanged(true, driver.device.productName)
            true
        } catch (e: IOException) {
            Log.e(TAG, "Lỗi cấu hình cổng USB Serial: ${e.message}")
            disconnect()
            false
        }
    }

    /**
     * Gửi mảng byte dữ liệu qua cổng nối tiếp.
     * @param data Dữ liệu cần gửi.
     * @param timeout Thời gian chờ tối đa (ms).
     */
    fun write(data: ByteArray, timeout: Int = 800): Boolean {
        val port = usbSerialPort ?: return false
        if (!port.isOpen) return false

        return try {
            port.write(data, timeout)
            true
        } catch (e: IOException) {
            Log.e(TAG, "Lỗi khi ghi dữ liệu ra Serial: ${e.message}")
            listener?.onRunError(e)
            false
        }
    }

    /**
     * Gửi dữ liệu bất đồng bộ trên luồng riêng để tránh block UI Thread.
     */
    fun writeAsync(data: ByteArray, timeout: Int = 800, onComplete: ((Boolean) -> Unit)? = null) {
        executor.execute {
            val result = write(data, timeout)
            onComplete?.invoke(result)
        }
    }

    /**
     * Đóng cổng nối tiếp và giải phóng tài nguyên.
     */
    fun disconnect() {
        try {
            ioManager?.listener = null
            ioManager?.stop()
            ioManager = null
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi khi dừng Serial IO Manager: ${e.message}")
        }

        try {
            usbSerialPort?.close()
            usbSerialPort = null
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi khi đóng cổng UsbSerialPort: ${e.message}")
        }

        listener?.onConnectionStatusChanged(false, null)
    }

    companion object {
        private const val TAG = "SerialPortManager"
    }
}
