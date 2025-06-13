package com.research.oproom

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.hardware.usb.*
import android.os.*
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updateLayoutParams
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import com.hoho.android.usbserial.util.SerialInputOutputManager
import java.lang.Exception
import java.text.DateFormat
import java.text.DecimalFormat
import java.util.*
import kotlin.concurrent.thread
//import mraa.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.IllegalStateException
import java.lang.NullPointerException
import kotlin.collections.ArrayList
import kotlin.concurrent.timer
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity(), View.OnClickListener, TimerSetDialog.OnInputListener, UVSetDialog.OnInputListener, ConfigDialog.OnInputListener, ValveSetDialog.OnInputListener, PasswordDialog.OnInputListener {
//    lateinit var handler : Handler
    lateinit var uiHandler: Handler
    lateinit var opHourSet: TextView
    lateinit var opMinSet: TextView
    lateinit var anHourSet: TextView
    lateinit var anMinSet: TextView
    lateinit var opTimerSetting: TimerSetting
    lateinit var anTimerSetting: TimerSetting
    lateinit var light1: LightSwitch
    lateinit var light2: LightSwitch
    lateinit var lightUV: LightSwitch
    lateinit var clock2: realTime
    lateinit var autoButton: ImageButton
    lateinit var manButton: ImageButton
    lateinit var tempSetting: NumericSetting
    lateinit var humSetting: NumericSetting
    lateinit var coolerSetting: NumericSetting
    lateinit var humUnSetting: NumericSetting
    lateinit var heaterSetting: NumericSetting
    lateinit var sysButton: DualStateButton
    lateinit var npButton: DualStateButton
    lateinit var applyButton: Button
    lateinit var valveButton: TextView
    lateinit var valveText: TextView
    lateinit var valveBack: ImageView
    lateinit var fanButton: DualStateButton
    lateinit var coolerButton: DualStateButton
    lateinit var heaterButton: DualStateButton
    lateinit var tempSensor: SensorView
    lateinit var humiSensor: SensorView
    lateinit var presSensor: SensorView
    lateinit var filterView: TextView
    lateinit var prImage: ImageView
    lateinit var present: Present
    lateinit var heaterAlarm: module_alarm
    lateinit var ventAlarm: module_alarm

    val tempHi = 7
    val tempSetHi = 9
    val tempTolHi = 11
    val heatTolHi = 13
    val humHi = 15
    val humSetHi = 17
    val humTolHi = 19
    val valveHi = 21

    val strTempSt = "tempSetting"
    val strCoolerSt = "coolerSetting"
    val strHeaterSt = "heaterSetting"
    val strHumSt = "humSetting"
    val strHumTolSt = "humUnSetting"

    val humRequest = ubyteArrayOf(0x01u, 0x03u, 0x00u, 0x01u, 0x00u, 0x02u, 0x34u, 0x0bu).toByteArray()
    val presRequest = ubyteArrayOf(0x02u, 0x03u, 0x10u, 0x12u, 0x00u, 0x04u, 0x60u, 0xfdu).toByteArray()
    val filterRequest = ubyteArrayOf(0x02u, 0x03u, 0x10u, 0x13u, 0x00u, 0x01u, 0x20u, 0xfcu).toByteArray()
    val lion1Request = ubyteArrayOf(0x02u, 0x06u, 0x10u, 0x11u, 0x00u, 0x01u, 0x9cu, 0xfbu).toByteArray()
    val liof1Request = ubyteArrayOf(0x02u, 0x06u, 0x10u, 0x11u, 0x00u, 0x00u, 0x5du, 0x3bu).toByteArray()
    val lion2Request = ubyteArrayOf(0x02u, 0x06u, 0x10u, 0x0au, 0x00u, 0x01u, 0x6cu, 0xfbu).toByteArray()
    val liof2Request = ubyteArrayOf(0x02u, 0x06u, 0x10u, 0x0au, 0x00u, 0x00u, 0xadu, 0x3bu).toByteArray()
    val uvonRequest = ubyteArrayOf(0x02u, 0x06u, 0x10u, 0x0bu, 0x00u, 0x01u, 0x3du, 0x3bu).toByteArray()
    val uvofRequest = ubyteArrayOf(0x02u, 0x06u, 0x10u, 0x0bu, 0x00u, 0x00u, 0xfcu, 0xfbu).toByteArray()
    val modeAutoRequest = ubyteArrayOf(0x02u, 0x06u, 0x10u, 0x00u, 0x00u, 0x01u, 0x4cu, 0xf9u).toByteArray()
    val modeManRequest = ubyteArrayOf(0x02u, 0x06u, 0x10u, 0x00u, 0x00u, 0x00u, 0x8du, 0x39u).toByteArray()
    val runRequest = ubyteArrayOf(0x02u, 0x06u, 0x10u, 0x01u, 0x00u, 0x01u, 0x1du, 0x39u).toByteArray()
    val stopRequest = ubyteArrayOf(0x02u, 0x06u, 0x10u, 0x01u, 0x00u, 0x00u, 0xdcu, 0xf9u).toByteArray()
    val npOnRequest = ubyteArrayOf(0x02u, 0x06u, 0x10u, 0x0du, 0x00u, 0x01u, 0xddu, 0x3au).toByteArray()
    val npOffRequest = ubyteArrayOf(0x02u, 0x06u, 0x10u, 0x0du, 0x00u, 0x00u, 0x1cu, 0xfau).toByteArray()
    val fanOnRequest = ubyteArrayOf(0x02u, 0x06u, 0x10u, 0x10u, 0x00u, 0x01u, 0x4cu, 0x3du).toByteArray()
    val fanOffRequest = ubyteArrayOf(0x02u, 0x06u, 0x10u, 0x10u, 0x00u, 0x00u, 0x8cu, 0xfcu).toByteArray()
    val coolOnRequest = ubyteArrayOf(0x02u, 0x06u, 0x10u, 0x0eu, 0x00u, 0x01u, 0x2du, 0x3au).toByteArray()
    val coolOffRequest = ubyteArrayOf(0x02u, 0x06u, 0x10u, 0x0eu, 0x00u, 0x00u, 0xecu, 0xfau).toByteArray()
    val heatOnRequest = ubyteArrayOf(0x02u, 0x06u, 0x10u, 0x0fu, 0x00u, 0x01u, 0x7cu, 0xfau).toByteArray()
    val heetOffRequest = ubyteArrayOf(0x02u, 0x06u, 0x10u, 0x0fu, 0x00u, 0x00u, 0xbdu, 0x3au).toByteArray()

    var tempForwardRequest = ubyteArrayOf(0x02u, 0x06u, 0x10u, 0x02u, 0x00u, 0x00u, 0xbdu, 0x3au).toByteArray()
    var humForwardRequest = ubyteArrayOf(0x02u, 0x06u, 0x10u, 0x06u, 0x00u, 0x00u, 0xbdu, 0x3au).toByteArray()
    var autoSettingsRequest = ubyteArrayOf(0x02u, 0x10u, 0x10u, 0x02u, 0x00u, 0x08u, 0x10u, 0x01u, 0x0au, 0x00u, 0xe6u, 0x00u, 0x05u, 0x00u, 0x14u, 0x02u, 0x5eu, 0x02u, 0x8au, 0x00u, 0x32u, 0x00u, 0x00u, 0x05u, 0x18u).toByteArray()


    var dummyRequest = ubyteArrayOf(0x02u, 0x03u, 0x10u, 0x12u, 0x00u, 0x01u, 0x20u, 0xfcu).toByteArray()
    val crcByte = CrcByte()
    val handlerThread = HandlerThread("TLA boss")
    lateinit var threadHandler: Handler

    //    var port: UsbSerialPort? = null

    lateinit var port: UsbSerialPort

    var timerList: ArrayList<Timer> = arrayListOf()
    var timerListUV: ArrayList<Timer> = arrayListOf()
    var timeSettingBuffer: ArrayList<TimerSetting> = arrayListOf()
    var timerNumber = 0
    var uvTime = 10
    var valveValue = 0
    var filterValue:UInt = 0u
    var page = 0
    var autoMode = true
    var transmitting = false
    lateinit var opTimer: TimerText
    lateinit var anTimer: TimerText
    lateinit var sp: SharedPreferences
    lateinit var editor: Editor
//    val sensorUpdater = Timer()
    var updaterBuffer = arrayListOf<Timer>()
//    var mUsbDevice: UsbDevice? = null
//    var comDeviceConnection: UsbDeviceConnection? = null
//    var comDeviceEndpoint: UsbEndpoint? = null
    var packageBuffer = arrayListOf<Byte>()

//    val gpio = Gpio(5)
    var restarted = true
    val delayOff = Timer()

    val sampleSize: UInt = 30u
    var sampleList = arrayListOf<UInt>()
    var sampleSum: UInt = 0u

    lateinit var retrofit: Retrofit
    private lateinit var oproomApi: OproomApi
    private lateinit var post: Post
    var updateToServer = Timer()

    var filterTimer = Timer()
    var filterState = true
    var filterCount = 0
    lateinit var filterText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        uiHandler = Handler(Looper.getMainLooper())
        handlerThread.start()
        threadHandler = Handler(handlerThread.looper)
//        gpio.dir(Dir.DIR_OUT)
//        gpio.outputMode(OutputMode.MODE_OUT_PUSH_PULL)
//        gpio.write(1)
        restarted = true

        opHourSet = findViewById(R.id.textView_opHourSet)
        opMinSet = findViewById(R.id.textView_opMinSet)
        anHourSet = findViewById(R.id.textView_anHourSet)
        anMinSet = findViewById(R.id.textView_anMinSet)
        opHourSet.setOnClickListener(this)
        opMinSet.setOnClickListener(this)
        anHourSet.setOnClickListener(this)
        anMinSet.setOnClickListener(this)
        opTimerSetting = TimerSetting(0,0, opHourSet, opMinSet)
        anTimerSetting = TimerSetting(0,0, anHourSet, anMinSet)
        sysButton = DualStateButton(false, findViewById(R.id.buttonSys), R.drawable.sys_on, R.drawable.sys_off)
        light1 = LightSwitch(findViewById(R.id.buttonLight1), false)
        light2 = LightSwitch(findViewById(R.id.buttonLight2), false)
        lightUV = object : LightSwitch(findViewById(R.id.buttonLightUV), false){
            override fun setIcon(){
                if (state)
                    ref.setImageResource(R.drawable.uv_on)
                else
                    ref.setImageResource(R.drawable.uv_off)
            }
        }
        tempSensor = SensorView(0u, " \u2103", true, findViewById(R.id.textView_sensorTemp))
        humiSensor = SensorView(0u, " %RH", true, findViewById(R.id.textView_sensorHum))
        presSensor = object : SensorView(0u, " Pa", true, findViewById(R.id.textView_sensorPres)){
            override fun update(){
                uiHandler.post {
                    if (showUnit)
//                        ref.text = (value.toFloat() * 100 / 2048).roundToInt().toString().padStart(2, '0') + unit
                        ref.text = (value.toFloat() * 100 / 8192).roundToInt().toString().padStart(2, '0') + unit
                    else
//                        ref.text = (value.toFloat() * 100 / 2048).roundToInt().toString().padStart(2, '0')
                        ref.text = (value.toFloat() * 100 / 8192).roundToInt().toString().padStart(2, '0')
                }
            }
        }
        heaterAlarm = module_alarm(false, findViewById(R.id.alarm_heater), R.drawable.heater_ok, R.drawable.heater_error)
        ventAlarm = module_alarm(false, findViewById(R.id.alarm_vent), R.drawable.vent_ok, R.drawable.vent_error)

        val inflater = layoutInflater
        val operatingViewer = inflater.inflate(R.layout.activity_setting, null)
        opTimer = TimerText(
            0, 0, 0, 0,
            operatingViewer.findViewById(R.id.textView_opHour),
            operatingViewer.findViewById(R.id.textView_opMin),
            operatingViewer.findViewById(R.id.textView_opSec),
            false, true)

        anTimer = TimerText(
            0, 0, 0, 0,
            operatingViewer.findViewById(R.id.textView_anHour),
            operatingViewer.findViewById(R.id.textView_anMin),
            operatingViewer.findViewById(R.id.textView_anSec),
            false, true)
        clock2 = realTime("2021","5180", operatingViewer.findViewById(R.id.textView_date), operatingViewer.findViewById(R.id.textView_time))
        clock2.updateText()
        clock2.start()
        prImage = operatingViewer.findViewById(R.id.imageView_info)
        present = Present(operatingViewer.findViewById(R.id.button_nextP), operatingViewer.findViewById(R.id.button_backP), operatingViewer.findViewById(R.id.imageView_info), operatingViewer.findViewById(R.id.button_slideShow))

        val settingViewer = inflater.inflate(R.layout.activity_settings_real, null)
        autoButton = settingViewer.findViewById(R.id.buttonAuto)
        manButton = settingViewer.findViewById(R.id.buttonMan)
        npButton = DualStateButton(false, settingViewer.findViewById(R.id.buttonNp), R.drawable.np_enable, R.drawable.np_disable)
        applyButton = settingViewer.findViewById(R.id.buttonApply)
        fanButton = DualStateButton(false, settingViewer.findViewById(R.id.imageButton_fan), R.drawable.fan_on, R.drawable.fan_off)
        coolerButton = DualStateButton(false, settingViewer.findViewById(R.id.imageButton_cooler), R.drawable.cooler_on, R.drawable.cooler_off)
        heaterButton = DualStateButton(false, settingViewer.findViewById(R.id.imageButton_heater), R.drawable.heater_on, R.drawable.heater_off)
        tempSetting = NumericSetting(
            23f,
            16f,
            30f,
            0.5f,
            settingViewer.findViewById(R.id.imageButton_tempDown),
            settingViewer.findViewById(R.id.textView_tempValue),
            settingViewer.findViewById(R.id.imageButton_tempUp),
            settingViewer.findViewById(R.id.textView_temp),
            settingViewer.findViewById(R.id.imageView_temp))

        humSetting = NumericSetting(
            65f,
            50f,
            80f,
            1f,
            settingViewer.findViewById(R.id.imageButton_humDown),
            settingViewer.findViewById(R.id.textView_humValue),
            settingViewer.findViewById(R.id.imageButton_humUp),
            settingViewer.findViewById(R.id.textView_hum),
            settingViewer.findViewById(R.id.imageView_hum))

        coolerSetting = NumericSetting(
            0.5f,
            0f,
            1f,
            0.1f,
            settingViewer.findViewById(R.id.imageButton_coolerDown),
            settingViewer.findViewById(R.id.textView_coolerValue),
            settingViewer.findViewById(R.id.imageButton_coolerUp),
            settingViewer.findViewById(R.id.textView_cooler),
            settingViewer.findViewById(R.id.imageView_cooler))

        humUnSetting = NumericSetting(
            5f,
            0f,
            10f,
            1f,
            settingViewer.findViewById(R.id.imageButton_humUnDown),
            settingViewer.findViewById(R.id.textView_humUnValue),
            settingViewer.findViewById(R.id.imageButton_humUnUp),
            settingViewer.findViewById(R.id.textView_humUn),
            settingViewer.findViewById(R.id.imageView_humUn))

        heaterSetting = NumericSetting(
            1f,
            0f,
            3f,
            0.2f,
            settingViewer.findViewById(R.id.imageButton_heaterDown),
            settingViewer.findViewById(R.id.textView_heaterValue),
            settingViewer.findViewById(R.id.imageButton_heaterUp),
            settingViewer.findViewById(R.id.textView_heater),
            settingViewer.findViewById(R.id.imageView_heater))

        sp = getSharedPreferences("Config", MODE_PRIVATE)
        editor = sp.edit()
        opTimerSetting.hr = sp.getInt("opHr", 0)
        opTimerSetting.min = sp.getInt("opMin", 0)
        opTimer.countUp = sp.getBoolean("opUp", true)
        anTimerSetting.hr = sp.getInt("anHr", 0)
        anTimerSetting.min = sp.getInt("anMin", 0)
        anTimer.countUp = sp.getBoolean("anUp", true)

        filterCount = sp.getInt("filterCount", 259200)
        filterState = sp.getBoolean("filterState", false)
        stateUpdate(0)
        opTimer.hr = opTimerSetting.hr
        opTimer.min = opTimerSetting.min
        anTimer.hr = anTimerSetting.hr
        anTimer.min = anTimerSetting.min
        filterTimer.scheduleAtFixedRate(object : TimerTask(){
            override fun run() {
                if(filterCount >= 256200){
                    if(filterState) {
                        killFilter()
                    }
                }else{
                    filterCount ++
                    editor.putInt("filterCount", filterCount)
                    editor.apply()
//                    Log.d("Filter", filterCount.toString())
                }
            }
        }, 0, 60000)

        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_BARS_BY_SWIPE
        supportActionBar?.hide()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        /*
        val mUsbManager = getSystemService(USB_SERVICE) as UsbManager
        val lUsbManager = mUsbManager
        val dList: HashMap<String, UsbDevice> = lUsbManager.deviceList
        //val textView = findViewById<TextView>(R.id.textView)
        dList.forEach{
                (key, value) ->
            //val name = key.toString()
            val mVendorID = value.vendorId.toString()
            val mProductInfo = value.productName.toString()
//            Log.d("Db", mProductInfo)
            if(mVendorID.contains("1027")){
                mUsbDevice = value
                Log.d("Db", "Communication device detected")
                Log.d("Db", "InterfaceCount: " + mUsbDevice?.interfaceCount)

            }
        }
        comDeviceConnection = mUsbManager.openDevice(mUsbDevice)
        val comDeviceInterface = mUsbDevice?.getInterface(0)
        comDeviceEndpoint = comDeviceInterface?.getEndpoint(0)
        Log.d("Db", "Endpoint type: " + comDeviceEndpoint?.type.toString())
        Log.d("Db", "Interface protocol: " + comDeviceInterface?.interfaceProtocol.toString())
        //val comRequest = comDeviceConnection.requestWait()
        //Log.d("Db", comRequest.endpoint.toString())
        */

        // Find all available drivers from attached devices.
        //val manager = getSystemService<Any>(Context.USB_SERVICE) as UsbManager
        val manager = getSystemService(USB_SERVICE) as UsbManager
        val availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager)
        if (availableDrivers.isEmpty()) {
            return
        }
        // Open a connection to the first available driver.
        val driver = availableDrivers[0]
        val connection = manager.openDevice(driver.device)

        Log.d("Db", "VendorID: " + driver.device.productName)
        Log.d("Db", "VendorID: " + driver.device.vendorId.toString())

        port = driver.ports[0] // Most devices have just one port (port 0)
        port.open(connection)
        port.setParameters(9600, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE)
        val humListener = object : SerialInputOutputManager.Listener{
            override fun onNewData(data: ByteArray) {
//                Log.d("TLA", data.toUByteArray().contentToString() )
                for (i in data) {
                    packageBuffer.add(i)
                }
//                Log.d("315", packageBuffer.toString())
//                while (packageBuffer.size > 0 && packageBuffer[0].compareTo(1) == 0 && packageBuffer[0].compareTo(2) == 0){
//                    packageBuffer.removeAt(0)
//                }
                if (packageBuffer.size >= 5) {
                    if (packageBuffer[0].compareTo(0x01) == 0 && packageBuffer[1].compareTo(0x03) == 0) {
                        val packageSize = packageBuffer[2].toInt() + 5
                        if (packageBuffer.size >= packageSize) {
                            /*
                            humiSensor.value = packageBuffer[3].toUByte() * 16u * 16u + packageBuffer[4].toUByte()
//                            humiSensor.value = updateAvrHum(humiSensor.value)
                            tempSensor.value = packageBuffer[5].toUByte() * 16u * 16u + packageBuffer[6].toUByte()
//                            Log.d("Temp", tempSensor.value.toFloat().toString())
                            tempForwardRequest[4] = packageBuffer[5]
                            tempForwardRequest[5] = packageBuffer[6]
                            humForwardRequest[4] = packageBuffer[3]
                            humForwardRequest[5] = packageBuffer[4]
                            autoSettingsRequest[tempHi] = packageBuffer[5]
                            autoSettingsRequest[tempHi + 1] = packageBuffer[6]
                            autoSettingsRequest[humHi] = packageBuffer[3]
                            autoSettingsRequest[humHi + 1] = packageBuffer[4]
                             */

                            //-----Use for LEFOO-----
                            humiSensor.value = packageBuffer[5].toUByte() * 16u * 16u + packageBuffer[6].toUByte()
//                            humiSensor.value = updateAvrHum(humiSensor.value)
                            tempSensor.value = packageBuffer[3].toUByte() * 16u * 16u + packageBuffer[4].toUByte()
//                            Log.d("Temp", tempSensor.value.toFloat().toString())
                            tempForwardRequest[4] = packageBuffer[3]
                            tempForwardRequest[5] = packageBuffer[4]
                            humForwardRequest[4] = packageBuffer[5]
                            humForwardRequest[5] = packageBuffer[6]
                            autoSettingsRequest[tempHi] = packageBuffer[3]
                            autoSettingsRequest[tempHi + 1] = packageBuffer[4]
                            autoSettingsRequest[humHi] = packageBuffer[5]
                            autoSettingsRequest[humHi + 1] = packageBuffer[6]
                            //-----Use for LEFOO-----

                            if (page == 0 || page == 2) {
                                humiSensor.update()
                                tempSensor.update()
                            }
//                            for (i in 0 until packageSize)
//                                packageBuffer.removeAt(0)
                            transmitting = false
//                            Log.d("341", "Transmit complete")
                        }
                    } else if ((packageBuffer[0].compareTo(0x02) == 0) && (packageBuffer[1].compareTo(0x03) == 0)) {
                        val packageSize = packageBuffer[2].toInt() + 5
                        if (packageBuffer.size >= packageSize) {
                            presSensor.value =
                                packageBuffer[3].toUByte() * 16u * 16u + packageBuffer[4].toUByte()
                            filterValue = (packageBuffer[5].toUByte().toUInt() * 16u * 16u + packageBuffer[6].toUByte().toUInt())
                            Log.d("442", packageBuffer.toString())
//                            Log.d("443", presSensor.value.toString() + filterValue.toString())
                            if (page == 0 || page == 2) {
                                presSensor.update()
                                if (page == 0){
                                    if (packageBuffer[8].compareTo(0x01) == 0)
                                        heaterAlarm.update(true)
                                    else
                                        heaterAlarm.update(false)
                                    if (packageBuffer[10].compareTo(0x01) == 0)
                                        ventAlarm.update(true)
                                    else
                                        ventAlarm.update(false)
                                }
                            }else{
                                updateFilterPres()
                            }
//                            for (i in 0 until (packageBuffer[2] + 5))
//                                packageBuffer.removeAt(0)
                            transmitting = false
//                            Log.d("452", "Transmit complete")
                        }
                    } else if (packageBuffer[0].compareTo(0x02) == 0 && packageBuffer.size == dummyRequest.size) {
                        val packageSize = packageBuffer[2].toInt() + 5
                        var match = true
                        for (i in dummyRequest.indices){
                            if(packageBuffer[i] != dummyRequest[i])
                                match = false
                        }
                        if(match) {
//                            for (i in 0 until packageSize)
//                                packageBuffer.removeAt(0)
                            transmitting = false
//                            Log.d("465", "Transmit complete")
//                            Log.d("466", "Repeated")
                        }
                    } else {
                        errorCodeScan()
                    }
                }
            }
            override fun onRunError(e: Exception?) {

            }
        }
        val usbIoManager = SerialInputOutputManager(port, humListener)
        usbIoManager.setThreadPriority(1)
        usbIoManager.start()

//        var loggingIntercepter = HttpLoggingInterceptor()
//        loggingIntercepter.setLevel(HttpLoggingInterceptor.Level.BODY)
//        var okHttpClient = OkHttpClient.Builder().addInterceptor(loggingIntercepter).build()
        retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.101.38:80/")
            .addConverterFactory(GsonConverterFactory.create())
//            .client(okHttpClient)
            .build()

        oproomApi = retrofit.create(OproomApi::class.java)
//        post = Post(1, 32.5f, 50.1f)

    }

    override fun onResume() {
//        gpio.write(1)
        sendState(light1.state, lion1Request, liof1Request)
        Log.d("Reset", "1")
        sendState(light2.state, lion2Request, liof2Request)
        Log.d("Reset", "2")
        sendState(lightUV.state, uvonRequest, uvofRequest)
        Log.d("Reset", "3")
        sendState(sysButton.stateOn, runRequest, stopRequest)
        Log.d("Reset", "4")
        sendState(autoMode, modeAutoRequest, modeManRequest)
        Log.d("Reset", "5")
        sendState(fanButton.stateOn, fanOnRequest, fanOffRequest)
        Log.d("Reset", "6")
        sendState(coolerButton.stateOn, coolOnRequest, coolOffRequest)
        Log.d("Reset", "7")
        sendState(heaterButton.stateOn, heatOnRequest, heetOffRequest)
        Log.d("Reset", "8")
        loadInitParam()
        parameterUpdate()
        threadHandler.post(SendRequestRunnable(autoSettingsRequest, false))
        Log.d("Status report", "Ready")

        updaterBuffer.clear()
        updaterBuffer.add(Timer())
        updaterBuffer[0].scheduleAtFixedRate(object : TimerTask(){
            override fun run() {
                threadHandler.post(SendRequestRunnable(humRequest, true))
                threadHandler.post(object : Runnable{
                    override fun run() {
                        crcByte.update(tempForwardRequest.copyOfRange(0,6))
                        tempForwardRequest[6] = crcByte.low.toByte()
                        tempForwardRequest[7] = crcByte.high.toByte()
                        crcByte.update(humForwardRequest.copyOfRange(0,6))
                        humForwardRequest[6] = crcByte.low.toByte()
                        humForwardRequest[7] = crcByte.high.toByte()
//                        Log.d("TLA", tempForwardRequest.contentToString())
//                        Log.d("TLA", humForwardRequest.contentToString())

                    }
                })
                threadHandler.post(SendRequestRunnable(presRequest, true))
                threadHandler.post(object : SendRequestRunnable(tempForwardRequest, true){
                    override fun run() {
                        dummyRequest = tempForwardRequest
                        super.run()
                    }
                })
                threadHandler.post(object : SendRequestRunnable(humForwardRequest, true){
                    override fun run() {
                        dummyRequest = humForwardRequest
                        super.run()
                    }
                })
            }
        }, 1500, 1000)
        /*
        updateToServer.scheduleAtFixedRate(object : TimerTask(){
            override fun run() {
                updatePost()
            }
        }, 2000, 5000)

         */
        super.onResume()
        Log.d("Status report", "On Resume")
    }
    override fun onPause() {
        super.onPause()
        updaterBuffer[0].cancel()
        delayOff.schedule(object : TimerTask(){
            override fun run() {
//                gpio.write(0)
            }
        }, 1000)
        Log.d("Status report", "On Pause")
    }

    override fun onStop() {
        super.onStop()
        updateToServer.cancel()
        filterTimer.cancel()
        delayOff.schedule(object : TimerTask(){
            override fun run() {
//                gpio.write(0)
            }
        }, 1000)
        Log.d("Status report", "On Stop")
    }
    override fun onDestroy() {
        super.onDestroy()
        Log.d("Status report", "Gud bye")
        clock2.stop()
//        sensorUpdater.cancel()
        handlerThread.quit()
        try {
            delayOff.cancel()
        }catch (e: IllegalStateException){
            Log.e("Exception", "Timer already cancelled")
        }
    }

    fun hideThatShit(v: View){
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }
    fun opTimeStart(countUp : Boolean){
        opTimer.runnin = true
        timerList.add(Timer())

        timerList[opTimer.pos].scheduleAtFixedRate(object : TimerTask() {
            val up = countUp
            override fun run() {
                if(this.up)
                    opTimer.tickUp()
                else
                    opTimer.tickDown()
                if (page == 2) {
                    try {
                        uiHandler.post {
                            opTimer.updateText()
                            //opTimeUpdate()
                        }
                    }catch (e : NullPointerException){
                        Log.d("Ect", "Just saved your ass opTimer")
                    }
                }
            }
        }, 0, 250)

        timerNumber = timerList.size
    }
    fun anTimeStart(countUp: Boolean){
        anTimer.runnin = true
        timerList.add(Timer())
        timerList[anTimer.pos].scheduleAtFixedRate(object : TimerTask(){
            val up = countUp
            override fun run() {
                if(this.up)
                    anTimer.tickUp()
                else
                    anTimer.tickDown()
                if(page == 2) {
                    try {
                        uiHandler.post {
                            //anTimeUpdate()
                            anTimer.updateText()
                        }
                    }catch (e: NullPointerException){
                        Log.d("Ect", "Just saved your ass anTimer")
                    }
                }
            }
        }, 0, 250)
        timerNumber = timerList.size
    }
    fun timerKill(pos: Int){
        if (timerList.size != 0) {
            Log.d("DB", "Remove at " + pos.toString() + " of " + timerList.size.toString())
            timerList.get(pos).cancel()
            timerList.removeAt(pos)
            timerNumber = timerList.size
            opTimer.pos = 0
            anTimer.pos = 0
        }
    }
    fun timerUvClear(){
        while(timerListUV.size > 0){
            timerListUV[0].cancel()
            timerListUV.removeAt(0)
        }
    }
    fun updateFilterPres(){
        runOnUiThread{
            filterView.text = (filterValue.toFloat() * 100 / 2048).roundToInt().toString().padStart(2, '0') + " Pa"
        }
    }
    fun stateUpdate(page: Int){
        when(page) {
            0->{
                opHourSet = findViewById(R.id.textView_opHourSet)
                opMinSet = findViewById(R.id.textView_opMinSet)
                anHourSet = findViewById(R.id.textView_anHourSet)
                anMinSet = findViewById(R.id.textView_anMinSet)
                opTimerSetting.updateRef(opHourSet, opMinSet)
                anTimerSetting.updateRef(anHourSet, anMinSet)
                opTimerSetting.updateData()
                anTimerSetting.updateData()
                light1.update(findViewById(R.id.buttonLight1))
                light2.update(findViewById(R.id.buttonLight2))
                lightUV.update(findViewById(R.id.buttonLightUV))
                filterText = findViewById(R.id.textViewFilter)
                runOnUiThread {
                    if(filterState)
                        filterText.text = "Đạt"
                    else
                        filterText.text = "Kém"
                }
                if(opTimer.countUp){
                    findViewById<ImageButton>(R.id.imageButton_opUp).setImageResource(R.drawable.up_active)
                    findViewById<ImageButton>(R.id.imageButton_opDown).setImageResource(R.drawable.down_inactive)
                }else
                {
                    findViewById<ImageButton>(R.id.imageButton_opUp).setImageResource(R.drawable.up_inactive)
                    findViewById<ImageButton>(R.id.imageButton_opDown).setImageResource(R.drawable.down_active)
                }
                if(anTimer.countUp){
                    findViewById<ImageButton>(R.id.imageButton_anUp).setImageResource(R.drawable.up_active)
                    findViewById<ImageButton>(R.id.imageButton_anDown).setImageResource(R.drawable.down_inactive)
                }else
                {
                    findViewById<ImageButton>(R.id.imageButton_anUp).setImageResource(R.drawable.up_inactive)
                    findViewById<ImageButton>(R.id.imageButton_anDown).setImageResource(R.drawable.down_active)
                }
                tempSensor.updateRef(findViewById(R.id.textView_sensorTemp))
                humiSensor.updateRef(findViewById(R.id.textView_sensorHum))
                presSensor.updateRef(findViewById(R.id.textView_sensorPres))
                sysButton.updateRef(findViewById(R.id.buttonSys))
                heaterAlarm.updateRef(findViewById(R.id.alarm_heater))
                ventAlarm.updateRef(findViewById(R.id.alarm_vent))
                tempSensor.update()
                humiSensor.update()
                presSensor.update()
                sysButton.update()
                heaterAlarm.update(heaterAlarm.state)
                ventAlarm.update(ventAlarm.state)
                tempSensor.showUnit = false
                humiSensor.showUnit = false
                presSensor.showUnit = false

            }
            1->{
                modeUpdateRef()
                modeUpdate()
                showAdvance(false)
                humSetting.emerge()
                tempSetting.updateValue()
                humSetting.updateValue()
                coolerSetting.updateValue()
                humUnSetting.updateValue()
                heaterSetting.updateValue()
                npButton.updateRef(findViewById(R.id.buttonNp))
                npButton.update()
                fanButton.update()
                coolerButton.update()
                heaterButton.update()
                updateFilterPres()
                runOnUiThread{
                    valveButton.text = valveValue.toString()
                }
            }
            2-> {
                tempSensor.updateRef(findViewById(R.id.textView_sensorTempMini))
                humiSensor.updateRef(findViewById(R.id.textView_sensorHumMini))
                presSensor.updateRef(findViewById(R.id.textView_sensorPresMini))
                //presSensor.updateRef(findViewById(R.id.textView_sensorPres))
                prImage = findViewById(R.id.imageView_info)
                present.updateRef(findViewById(R.id.button_nextP), findViewById(R.id.button_backP), findViewById(R.id.imageView_info), findViewById(R.id.button_slideShow))
                tempSensor.update()
                humiSensor.update()
                presSensor.update()
                //presSensor.update()
                val opButton = findViewById<ImageButton>(R.id.imageButton_startOp)
                if (opTimer.runnin)
                    opButton.setImageResource(R.drawable.pause_count)
                else {
                    opButton.setImageResource(R.drawable.start_count)
                    opTimer.hr = opTimerSetting.hr
                    opTimer.min = opTimerSetting.min
                }
                val anButton = findViewById<ImageButton>(R.id.imageButton_startAn)
                if (anTimer.runnin)
                    anButton.setImageResource(R.drawable.pause_count)
                else{
                    anButton.setImageResource(R.drawable.start_count)
                    anTimer.hr = anTimerSetting.hr
                    anTimer.min = anTimerSetting.min
                }
                //opTimeUpdate()
                //anTimeUpdate()
                opTimer.updateRef(findViewById(R.id.textView_opHour), findViewById(R.id.textView_opMin), findViewById(R.id.textView_opSec))
                opTimer.updateText()
                anTimer.updateRef(findViewById(R.id.textView_anHour), findViewById(R.id.textView_anMin), findViewById(R.id.textView_anSec))
                anTimer.updateText()
                light1.update(findViewById(R.id.miniLight1))
                light2.update(findViewById(R.id.miniLight2))
                clock2.updateRef(findViewById(R.id.textView_date), findViewById(R.id.textView_time))
                clock2.updateText()
                tempSensor.showUnit = false
                humiSensor.showUnit = false
                presSensor.showUnit = false
            }
        }
    }
    fun valveSetter(){
        val valveSetDialog = ValveSetDialog(valveValue)
        valveSetDialog.show(supportFragmentManager, "TLA")
    }
    fun timeSetter(targetSetting : TimerSetting){
        timeSettingBuffer.add(targetSetting)
        val timerSetDialog = TimerSetDialog(targetSetting.hr, targetSetting.min)
        timerSetDialog.show(supportFragmentManager, "TLA")
    }
    fun resetTimerVal(target: TimerText, roleModel: TimerSetting){
        target.hr = roleModel.hr
        target.min = roleModel.min
        target.sec = 0
        target.runnin = false
    }
    fun modeUpdate(){
        if(autoMode) {
            autoButton.setImageResource(R.drawable.auto_enable)
            manButton.setImageResource(R.drawable.man_disable)
            autoEmerge()
        }else{
            autoButton.setImageResource(R.drawable.auto_disable)
            manButton.setImageResource(R.drawable.man_enable)
            autoSubmerge()
        }
    }
    fun modeUpdateRef(){
        if (page == 1) {
            autoButton = findViewById(R.id.buttonAuto)
            manButton = findViewById(R.id.buttonMan)
            applyButton = findViewById(R.id.buttonApply)
            valveButton = findViewById(R.id.textView_valveValue)
            valveText = findViewById(R.id.textView_valveText)
            valveBack = findViewById(R.id.imageView_valve)
            fanButton.updateRef(findViewById(R.id.imageButton_fan))
            coolerButton.updateRef(findViewById(R.id.imageButton_cooler))
            heaterButton.updateRef(findViewById(R.id.imageButton_heater))
            filterView = findViewById(R.id.textView_fdpValue)
            tempSetting.updateRef(
                findViewById(R.id.imageButton_tempDown),
                findViewById(R.id.textView_tempValue),
                findViewById(R.id.imageButton_tempUp),
                findViewById(R.id.textView_temp),
                findViewById(R.id.imageView_temp)
            )
            humSetting.updateRef(
                findViewById(R.id.imageButton_humDown),
                findViewById(R.id.textView_humValue),
                findViewById(R.id.imageButton_humUp),
                findViewById(R.id.textView_hum),
                findViewById(R.id.imageView_hum)
            )
            coolerSetting.updateRef(
                findViewById(R.id.imageButton_coolerDown),
                findViewById(R.id.textView_coolerValue),
                findViewById(R.id.imageButton_coolerUp),
                findViewById(R.id.textView_cooler),
                findViewById(R.id.imageView_cooler)
            )
            humUnSetting.updateRef(
                findViewById(R.id.imageButton_humUnDown),
                findViewById(R.id.textView_humUnValue),
                findViewById(R.id.imageButton_humUnUp),
                findViewById(R.id.textView_humUn),
                findViewById(R.id.imageView_humUn)
            )
            heaterSetting.updateRef(
                findViewById(R.id.imageButton_heaterDown),
                findViewById(R.id.textView_heaterValue),
                findViewById(R.id.imageButton_heaterUp),
                findViewById(R.id.textView_heater),
                findViewById(R.id.imageView_heater)
            )
        }
    }
    fun autoSubmerge(){
        tempSetting.submerge()
        humSetting.submerge()

        coolerSetting.submerge()
        humUnSetting.submerge()
        heaterSetting.submerge()
        valveText.visibility = View.INVISIBLE
        valveButton.visibility = View.INVISIBLE
        valveBack.visibility = View.INVISIBLE


        applyButton.visibility = View.GONE
//        showAdvance(false)
        fanButton.emerge()
        coolerButton.emerge()
        heaterButton.emerge()
    }
    fun autoEmerge(){
        tempSetting.emerge()
        humSetting.emerge()

        coolerSetting.emerge()
        humUnSetting.emerge()
        heaterSetting.emerge()
        valveText.visibility = View.VISIBLE
        valveButton.visibility = View.VISIBLE
        valveBack.visibility = View.VISIBLE

//        showAdvance(false)
        applyButton.visibility = View.VISIBLE
        fanButton.submerge()
        coolerButton.submerge()
        heaterButton.submerge()
    }
    fun showAdvance(show: Boolean){
        if (show){
            /*
            coolerSetting.emerge()
            humUnSetting.emerge()
            heaterSetting.emerge()
            valveText.visibility = View.VISIBLE
            valveButton.visibility = View.VISIBLE
            valveBack.visibility = View.VISIBLE

             */
            autoEmerge()
            manButton.visibility = View.VISIBLE
        }else{
            coolerSetting.submerge()
            humUnSetting.submerge()
            heaterSetting.submerge()
            valveText.visibility = View.GONE
            valveButton.visibility = View.GONE
            valveBack.visibility = View.GONE
            manButton.visibility = View.GONE
        }
    }
    fun errorCodeScan(){
//        Log.d("707", "Scan error code")
        crcByte.update(packageBuffer.toByteArray().copyOfRange(0, 3))
        if(packageBuffer[3] == crcByte.low.toByte() && packageBuffer[4] == crcByte.high.toByte()){
            Log.d("TEC", packageBuffer.toByteArray().toUByteArray().contentToString())
//            for(i in 0..4){
//                packageBuffer.removeAt(0)
//            }
        }
    }
    fun sendState(state: Boolean, on: ByteArray, off: ByteArray){
        if(state) {
            threadHandler.post(SendRequestRunnable(on, false))
        }
        else {
            threadHandler.post(SendRequestRunnable(off, false))
        }
        Thread.sleep(30)
    }
    fun killFilter(){
        filterCount = 256200
        filterState = false
        editor.putBoolean("filterState", filterState)
        editor.putInt("filterCount", filterCount)
        editor.apply()
        runOnUiThread {
            filterText.text = "Kém"
        }
    }
    fun parameterUpdate(){
        val tempSet = (tempSetting.value * 10).toInt()
        val tempTol = (coolerSetting.value * 10).toInt()
        val heatTol = (heaterSetting.value * 10).toInt()
        val humSet = (humSetting.value * 10).toInt()
        val humTol = (humUnSetting.value * 10).toInt()
        saveInitParam()
        autoSettingsRequest[tempSetHi] = tempSet.and(0xff00).rotateRight(8).toByte()
        autoSettingsRequest[tempSetHi+1] = tempSet.and(0xff).toByte()
        autoSettingsRequest[tempTolHi] = tempTol.and(0xff00).rotateRight(8).toByte()
        autoSettingsRequest[tempTolHi+1] = tempTol.and(0xff).toByte()
        autoSettingsRequest[heatTolHi] = heatTol.and(0xff00).rotateRight(8).toByte()
        autoSettingsRequest[heatTolHi+1] = heatTol.and(0xff).toByte()
        autoSettingsRequest[humSetHi] = humSet.and(0xff00).rotateRight(8).toByte()
        autoSettingsRequest[humSetHi+1] = humSet.and(0xff).toByte()
        autoSettingsRequest[humTolHi] = humTol.and(0xff00).rotateRight(8).toByte()
        autoSettingsRequest[humTolHi+1] = humTol.and(0xff).toByte()
        autoSettingsRequest[valveHi] = valveValue.and(0xff00).rotateRight(8).toByte()
        autoSettingsRequest[valveHi+1] = valveValue.and(0xff).toByte()
        crcByte.update(autoSettingsRequest.copyOfRange(0, 23))
        autoSettingsRequest[autoSettingsRequest.size-2] = crcByte.low.toByte()
        autoSettingsRequest[autoSettingsRequest.size-1] = crcByte.high.toByte()
    }
    fun saveInitParam(){
        editor.putFloat(strTempSt, tempSetting.value)
        editor.putFloat(strCoolerSt, coolerSetting.value)
        editor.putFloat(strHeaterSt, heaterSetting.value)
        editor.putFloat(strHumSt, humSetting.value)
        editor.putFloat(strHumTolSt, humUnSetting.value)
        editor.putInt("valve", valveValue)
        editor.apply()
    }
    fun loadInitParam(){
        tempSetting.value = sp.getFloat(strTempSt, 23F)
        coolerSetting.value = sp.getFloat(strCoolerSt, 0.5F)
        heaterSetting.value = sp.getFloat(strHeaterSt, 1F)
        humSetting.value = sp.getFloat(strHumSt, 65F)
        humUnSetting.value = sp.getFloat(strHumTolSt, 5F)
        valveValue = sp.getInt("valve", 0)
    }
    fun updateAvrHum(hum: UInt): UInt {
        sampleList.add(hum)
        sampleSum += hum
        return if(sampleList.size < sampleSize.toInt()){
            hum
        }else{
            var avr = sampleSum / sampleSize
            sampleSum -= sampleList[0]
            sampleList.removeAt(0)
            avr
        }
    }
    fun updatePost(){
        post = Post(1, tempSensor.value.toFloat()/10f, humiSensor.value.toFloat()/10f)
        var call = oproomApi.patchPost( post)
        call.enqueue(object : Callback<Post> {
            override fun onResponse(call: Call<Post>, response: Response<Post>) {
//                Log.d("Code", response.code().toString())
            }
            override fun onFailure(call: Call<Post>, t: Throwable) {
//                Log.d("Error", t.message.toString())
            }
        })
    }
    fun setLocal(activity: Activity, langCode: String){
        val locale = Locale(langCode)
        val resources = activity.resources
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    override fun onClick(p0: View?) {
        when(p0?.id){
            R.id.buttonLight1->{
                light1.switch()
                sendState(light1.state, lion1Request, liof1Request)
                updatePost()
            }
            R.id.buttonLight2-> {
                light2.switch()
                sendState(light2.state, lion2Request, liof2Request)
            }
            R.id.miniLight1->{
                light1.switch()
                sendState(light1.state, lion1Request, liof1Request)

            }
            R.id.miniLight2->{
                light2.switch()
                sendState(light2.state, lion2Request, liof2Request)

            }
            R.id.buttonLightUV->{
                lightUV.switch()
                if(!lightUV.state){
                    timerUvClear()
                }else{
                    timerListUV.add(Timer())
                    timerListUV[0].schedule(object : TimerTask(){
                        override fun run() {
                            timerUvClear()
                            lightUV.switch()
                            sendState(lightUV.state, uvonRequest, uvofRequest)
                        }
                    }, (uvTime.toLong() + 1) * 60000)
                    threadHandler.post(object : SendRequestRunnable(dummyRequest, false){
                        override fun run() {
                            dummyRequest = ubyteArrayOf(0x02u, 0x06u, 0x10u, 0x0cu, 0x00u, 0x00u, 0xfcu, 0xfbu).toByteArray()
                            dummyRequest[5] = uvTime.toByte()
                            crcByte.update(dummyRequest.copyOfRange(0, 6))
                            dummyRequest[6] = crcByte.low.toByte()
                            dummyRequest[7] = crcByte.high.toByte()
                            this.request = dummyRequest
                            super.run()
                        }
                    })
                }
                sendState(lightUV.state, uvonRequest, uvofRequest)

            }
            R.id.textView_opHourSet->{
                if(!opTimer.countUp)
                    timeSetter(opTimerSetting)
            }
            R.id.textView_opMinSet->{
                if(!opTimer.countUp)
                    timeSetter(opTimerSetting)
            }
            R.id.textView_anHourSet->{
                if(!anTimer.countUp)
                    timeSetter(anTimerSetting)
            }
            R.id.textView_anMinSet->{
                if(!anTimer.countUp)
                    timeSetter(anTimerSetting)
            }
            R.id.button_go->{
//                val intent = Intent(this, SettingActivity::class.java)
//                startActivity(intent)
                page = 2
                setContentView(R.layout.activity_setting)
                stateUpdate(2)
            }
            R.id.button->{
                page = 0
                setContentView(R.layout.activity_main)
                uiHandler = Handler(Looper.getMainLooper())
                stateUpdate(0)
            }
            R.id.button_setting->{
                setContentView(R.layout.activity_settings_real)
                page = 1
                stateUpdate(1)
                uiHandler = Handler(Looper.getMainLooper())
            }
            R.id.button2->{
                page = 0
                setContentView(R.layout.activity_main)
                uiHandler = Handler(Looper.getMainLooper())
                stateUpdate(0)
            }
            R.id.imageButton_startOp->{
                val button = findViewById<ImageButton>(R.id.imageButton_startOp)
                if (!opTimer.runnin){
                    opTimer.pos = timerNumber
                    opTimeStart(opTimer.countUp)
                    button.setImageResource(R.drawable.pause_count)
                }else{
                    timerKill(opTimer.pos)
                    opTimer.runnin = false
                    button.setImageResource(R.drawable.start_count)
                }
            }
            R.id.imageButton_startAn->{
                val button = findViewById<ImageButton>(R.id.imageButton_startAn)
                if (!anTimer.runnin){
                    anTimer.pos = timerNumber
                    anTimeStart(anTimer.countUp)
                    button.setImageResource(R.drawable.pause_count)
                }else{
                    timerKill(anTimer.pos)
                    anTimer.runnin = false
                    button.setImageResource(R.drawable.start_count)
                }
            }
            R.id.buttonReset->{
                while (timerList.size > 0){
                    timerKill(0)
                }
                resetTimerVal(opTimer, opTimerSetting)
//                opTimer.hr = opTimerSetting.hr
//                opTimer.min = opTimerSetting.min
//                opTimer.sec = 0
//                opTimer.runnin = false
                val opButton = findViewById<ImageButton>(R.id.imageButton_startOp)
                opButton.setImageResource(R.drawable.start_count)
                opTimer.updateText()
                resetTimerVal(anTimer, anTimerSetting)
//                anTimer.hr = anTimerSetting.hr
//                anTimer.min = anTimerSetting.min
//                anTimer.sec = 0
//                anTimer.runnin = false
                val anButton = findViewById<ImageButton>(R.id.imageButton_startAn)
                anButton.setImageResource(R.drawable.start_count)
                //anTimeUpdate()
                anTimer.updateText()
            }
            R.id.imageButton_opUp->{
                opTimer.countUp = true
                opTimerSetting.hr = 0
                opTimerSetting.min = 0
                stateUpdate(0)
                editor.putBoolean("opUp", opTimer.countUp)
                editor.putInt("opHr", opTimerSetting.hr)
                editor.putInt("opMin", opTimerSetting.min)
                editor.apply()
            }
            R.id.imageButton_opDown->{
                opTimer.countUp = false
                stateUpdate(0)
                editor.putBoolean("opUp", opTimer.countUp)
            }
            R.id.imageButton_anUp->{
                anTimer.countUp = true
                anTimerSetting.hr = 0
                anTimerSetting.min = 0
                stateUpdate(0)
                editor.putBoolean("anUp", anTimer.countUp)
                editor.putInt("anHr", anTimerSetting.hr)
                editor.putInt("anMin", anTimerSetting.min)
                editor.apply()
            }
            R.id.imageButton_anDown->{
                anTimer.countUp = false
                stateUpdate(0)
                editor.putBoolean("anUp", anTimer.countUp)
            }
            R.id.buttonAuto->{
                if (!autoMode){
                    autoMode = true
                    modeUpdate()
                    threadHandler.post(SendRequestRunnable(modeAutoRequest, false))
                }
            }
            R.id.buttonMan->{
                if (autoMode){
                    autoMode = false
                    modeUpdate()
                    threadHandler.post(SendRequestRunnable(modeManRequest, false))
                }
            }
            R.id.buttonSys->{
                sysButton.switch()
                Log.d("959", sysButton.stateOn.toString())
                sendState(sysButton.stateOn, runRequest, stopRequest)
            }
            R.id.buttonSysOld->{
//                sysButton.switch()
//                sendState(sysButton.stateOn, runRequest, stopRequest)
            }
            R.id.buttonNp->{
                npButton.switch()
                sendState(npButton.stateOn, npOnRequest, npOffRequest)

            }
            R.id.buttonApply->{
                //send settings to PLC
                parameterUpdate()
                threadHandler.post {
                    port.write(autoSettingsRequest, 800)
                }
                Log.d("955", autoSettingsRequest.toUByteArray().contentToString())
            }
            R.id.textView_valveValue->{
                valveSetter()
            }

            R.id.imageButton_fan->{
                fanButton.switch()
                sendState(fanButton.stateOn, fanOnRequest, fanOffRequest)
            }
            R.id.imageButton_cooler->{
                coolerButton.switch()
                sendState(coolerButton.stateOn, coolOnRequest, coolOffRequest)
            }
            R.id.imageButton_heater->{
                heaterButton.switch()
                sendState(heaterButton.stateOn, heatOnRequest, heetOffRequest)
            }
            R.id.imageButton_tempDown->{
                tempSetting.down()
            }
            R.id.imageButton_tempUp->{
                tempSetting.up()
            }
            R.id.imageButton_humDown->{
                humSetting.down()
            }
            R.id.imageButton_humUp->{
                humSetting.up()
            }
            R.id.imageButton_coolerDown->{
                coolerSetting.down()
            }
            R.id.imageButton_coolerUp->{
                coolerSetting.up()
            }
            R.id.imageButton_humUnDown->{
                humUnSetting.down()
            }
            R.id.imageButton_humUnUp->{
                humUnSetting.up()
            }
            R.id.imageButton_heaterDown->{
                heaterSetting.down()
            }
            R.id.imageButton_heaterUp->{
                heaterSetting.up()
            }
            R.id.button_uvSetting->{
                val uvSetDialog = UVSetDialog(uvTime)
                uvSetDialog.show(supportFragmentManager, "TLA")
            }
            R.id.buttonSave->{
                val configDialog = ConfigDialog(true)
                configDialog.show(supportFragmentManager, "TLA")
            }
            R.id.buttonLoad->{
                val configDialog = ConfigDialog(false)
                configDialog.show(supportFragmentManager, "TLA")
            }
            R.id.buttonFilter->{

            }
            R.id.textView_fdp->{

            }
            R.id.button_info->{
                present.emerge()
            }
            R.id.imageView_info->{
                present.submerge()
            }
            R.id.header2->{
                val passwordDialog = PasswordDialog()
                passwordDialog.show(supportFragmentManager, "TLA")
            }
        }
    }
    override fun sendInput(inputHr: Int, inputMin: Int) {
        timeSettingBuffer[0].updateData(inputHr, inputMin)
        timeSettingBuffer.removeAt(0)
        if(!opTimer.runnin){
            opTimer.hr = opTimerSetting.hr
            opTimer.min = opTimerSetting.min
            opTimer.sec = 0
        }
        if(!anTimer.runnin){
            anTimer.hr = anTimerSetting.hr
            anTimer.min = anTimerSetting.min
            anTimer.sec = 0
        }
        editor.putInt("opHr", opTimerSetting.hr)
        editor.putInt("opMin", opTimerSetting.min)
        editor.putInt("anHr", anTimerSetting.hr)
        editor.putInt("anMin", anTimerSetting.min)
        editor.apply()
    }

    override fun sendUVTime(uvTime: Int) {
        this.uvTime = uvTime
    }

    override fun sendValve(valve: Int) {
        this.valveValue = valve
        runOnUiThread{
            valveButton.text = valve.toString()
        }
    }

    override fun sendPassword(code: String) {
        if (code == "advst") {
            showAdvance(true)
//            val rsButton = findViewById<Button>(R.id.buttonFilter)
//            rsButton.visibility = View.VISIBLE
        }else if(code == "retlifteser"){
            filterState = true
            filterCount = 0
            editor.putBoolean("filterState", true)
            editor.putInt("filterCount", 0)
            editor.apply()
        }else if(code == "kft") {
            killFilter()
        }else if(code == "lvi")
        {
//            setLocal( MainActivity.getInstance(), "en")
        }else{
            showAdvance(false)
        }
        Log.d("enter", code)
    }

    override fun saveOrLoad(config: Int, save: Boolean) {
        if(save){
            editor.putInt("opHr$config", opTimerSetting.hr)
            editor.putInt("opMin$config", opTimerSetting.min)
            editor.putBoolean("opUp$config", opTimer.countUp)
            editor.putInt("anHr$config", anTimerSetting.hr)
            editor.putInt("anMin$config", anTimerSetting.min)
            editor.putBoolean("anUp$config", anTimer.countUp)
            editor.apply()
        }else{
            opTimerSetting.hr = sp.getInt("opHr$config", 0)
            opTimerSetting.min = sp.getInt("opMin$config", 0)
            opTimer.countUp = sp.getBoolean("opUp$config", true)
            anTimerSetting.hr = sp.getInt("anHr$config", 0)
            anTimerSetting.min = sp.getInt("anMin$config", 0)
            anTimer.countUp = sp.getBoolean("anUp$config", true)
            resetTimerVal(opTimer, opTimerSetting)
            resetTimerVal(anTimer, anTimerSetting)
            stateUpdate(0)
        }
    }

    open inner class TimerText(public var hr: Int,
                               public var min: Int,
                               public var sec: Int,
                               public var pos: Int
                               , public var hrRef: TextView
                               , public var minRef: TextView
                               , public var secRef: TextView
                               , public var runnin: Boolean
                               , public var countUp: Boolean){
        //Use on page 2
        private var tick = 0
        public fun tickUp(){
            if(tick >= 3){
                tick = 0
                this.sec++
            }else{
                tick++
            }
            if (this.sec >= 60){
                this.sec = 0
                this.min++
            }
            if (this.min >= 60){
                this.min = 0
                this.hr++
            }
        }
        public fun tickDown(){
            if((this.sec + this.min + this.hr) == 0)
                lam_lai_cuoc_doi()
            else {
                if (tick >= 3) {
                    if (this.sec <= 0) {
                        if (this.min <= 0) {
                            this.hr--
                            this.min = 59
                        } else
                            this.min--
                        this.sec = 59
                    } else
                        this.sec--
                    tick = 0
                } else
                    tick++
            }
        }
        private fun lam_lai_cuoc_doi(){
            timerKill(this.pos)
            this.runnin = false
            if(page == 2) {
                try {
                    uiHandler.post {
                        stateUpdate(2)
                    }
                }catch (e: NullPointerException){
                    Log.d("Ect", "Just saved your ass from reset your life")
                }
            }
            this.tick = 0
        }
        public fun updateRef(hrRef: TextView, minRef: TextView, secRef: TextView){
            this.hrRef = hrRef
            this.minRef = minRef
            this.secRef = secRef
        }
        public fun updateText(){
            this.secRef.text = this.sec.toString().padStart(2, '0')
            this.minRef.text = this.min.toString().padStart(2, '0')
            if(tick % 4 < 2)
                this.hrRef.text = this.hr.toString().padStart(2, '0') + ':'
            else
                this.hrRef.text = this.hr.toString().padStart(2, '0') + ' '
        }
    }
    inner class TimerSetting(var hr: Int,
                       var min: Int,
                       var hrText: TextView,
                       var minText: TextView){
        //use on page 0
        fun updateRef(hrTextNew: TextView, minTextNew: TextView){
            hrText = hrTextNew
            minText = minTextNew
        }
        fun updateData(hr: Int, min: Int){
            this.hr = hr
            hrText.text = this.hr.toString().padStart(2, '0')
            this.min = min
            minText.text = this.min.toString().padStart(2, '0')
        }
        fun updateData(){
            hrText.text = this.hr.toString().padStart(2, '0')
            minText.text = this.min.toString().padStart(2, '0')
        }
    }
    open inner class LightSwitch(public var ref: ImageButton, public var state: Boolean){
        fun switch(){
            if (state) {
                state = !state
                setOff()
            } else {
                state = !state
                setOn()
            }
        }
        fun update(ref: ImageButton){
            this.ref = ref
            if (state) {
                setOn()
            }
            else {
                setOff()
            }
        }
        private fun setOn(){
            uiHandler.post {
                setIcon()
                if (page == 0)
                    ref.updateLayoutParams<ConstraintLayout.LayoutParams> { verticalBias = 0.937f }
                else
                    ref.updateLayoutParams<ConstraintLayout.LayoutParams> { verticalBias = 0.32f }
            }
        }
        private fun setOff(){
            uiHandler.post {
                setIcon()
                if (page == 0)
                    ref.updateLayoutParams<ConstraintLayout.LayoutParams> { verticalBias = 0.95f }
                else
                    ref.updateLayoutParams<ConstraintLayout.LayoutParams> { verticalBias = 0.33f }
            }
        }
        protected open fun setIcon(){
            if (state)
                ref.setImageResource(R.drawable.light_on)
            else
                ref.setImageResource(R.drawable.light_off)
        }
    }
    inner class realTime(public var date: String, public var time: String, private var dateRef: TextView, private var timeRef: TextView){
        //private val calendar = Calendar.getInstance()
        private val timer = Timer()
        private var running = false
        public fun updateRef(dateRef: TextView, timeRef: TextView){
            this.dateRef = dateRef
            this.timeRef = timeRef
        }
        public fun updateText(){
            val calendar = Calendar.getInstance()
            this.date = DateFormat.getDateInstance().format(calendar.time)
            this.time = DateFormat.getTimeInstance().format(calendar.time)
            uiHandler.post {
                this.timeRef.text = this.time.padStart(8, '0')
                this.dateRef.text = this.date
            }
            //Log.d("Time", "Update")
        }
        public fun start(){
            if(!running){
                running = true
                timer.scheduleAtFixedRate(object: TimerTask(){
                    override fun run() {
                        if(page == 2)
                            updateText()
                    }
                }, 0, 500)
            }
        }
        public fun stop(){
            timer.cancel()
        }
    }
    inner class NumericSetting(iValue: Float,               //initial value
                               private val bValue: Float,   //min value
                               private val tValue: Float,   //max value
                               public var uncertain: Float,
                               private var dRef: ImageButton,
                               private var vRef: TextView,
                               private var uRef: ImageButton,
                               private var text: TextView,
                               private var image: ImageView){
        public var value = iValue
        public fun updateValue(){
            val df = DecimalFormat("0.0")
            vRef.text = df.format(this.value)
        }
        public fun up(){
            if(this.value < this.tValue) {
                this.value += this.uncertain
                updateValue()
            }
        }
        public fun down(){
            if (this.value > (this.bValue + this.uncertain)) {
                this.value -= uncertain
                updateValue()
            }
        }
        public fun updateRef(dRef: ImageButton, vRef: TextView, uRef: ImageButton, text: TextView, image: ImageView){
            this.dRef = dRef
            this.vRef = vRef
            this.uRef = uRef
            this.text = text
            this.image = image
        }
        public fun submerge(){
            this.dRef.visibility = View.INVISIBLE
            this.vRef.visibility = View.INVISIBLE
            this.uRef.visibility = View.INVISIBLE
            this.text.visibility = View.INVISIBLE
            this.image.visibility = View.INVISIBLE
        }
        public fun emerge(){
            this.dRef.visibility = View.VISIBLE
            this.vRef.visibility = View.VISIBLE
            this.uRef.visibility = View.VISIBLE
            this.text.visibility = View.VISIBLE
            this.image.visibility = View.VISIBLE
        }
    }
    inner class DualStateButton(
        public var stateOn: Boolean,
        private var ref: ImageButton,
        private val on: Int,
        private val off: Int
    ){
        public fun switch(){
            this.stateOn = !this.stateOn
            update()
        }
        public fun update(){
            if(this.stateOn)
                ref.setImageResource(on)
            else
                ref.setImageResource(off)
        }
        public fun updateRef(ref: ImageButton){
            this.ref = ref
        }
        public fun submerge(){
            this.ref.visibility = View.INVISIBLE
        }
        public fun emerge(){
            this.ref.visibility = View.VISIBLE
        }
    }
    open inner class SensorView(
        public var value: UInt,
        public var unit: String,
        public var showUnit: Boolean,
        protected var ref: TextView
    ){
        public open fun update(){
            uiHandler.post {
                if (showUnit)
                    ref.text = (value.toFloat() / 10).toString().padStart(4, '0') + unit
                else
                    ref.text = (value.toFloat() / 10).toString().padStart(4, '0')
            }
        }
        public fun updateRef(ref: TextView){
            this.ref = ref
        }
    }
    class CrcByte(){
        private var crc: UShort = 0xffffu
        public var high: UByte = 0xffu
        public var low: UByte = 0xffu
        public fun update(pack: ByteArray){
            crc = 0xffffu
            for(bite in pack){
                crc = crc.xor(bite.toUByte().toUShort())
                for (i in 0..7){
                    crc = if(crc.and(0x0001u).toInt() != 0){
                        crc.and(0xfffeu).rotateRight(1).xor(0xa001u)
                    } else {
                        crc.and(0xfffeu).rotateRight(1)
                    }
                }
            }
            this.high = crc.and(0xff00u).rotateRight(8).toUByte()
            this.low = crc.and(0x00ffu).toUByte()
        }
    }
    open inner class SendRequestRunnable(public var request: ByteArray, public var waitRespond: Boolean): Runnable{
        private val timeOut = request.size * 5
        override fun run() {
//            Log.d("send nuke", request.contentToString())
            while(packageBuffer.size > 0){
                packageBuffer.removeAt(0)
            }
            crcByte.update(request.copyOfRange(0, request.size - 2))
            request[request.size - 2] = crcByte.low.toByte()
            request[request.size - 1] = crcByte.high.toByte()
            port.write(request, timeOut)
//            Log.d("TLA", request.contentToString())
            if(waitRespond){
                transmitting = true
                val waitTime = if(request[1].compareTo(3) == 0 || request[1].compareTo(16) == 0){
                    ((request[4].toInt() * 16 + request[5].toInt()) * 2 + 5) * 5
                } else {
                    (request[3] * 2 + 5) * 5
                }
                var wait = 0
                Thread.sleep(waitTime.toLong())
                while (transmitting){
//                    Log.d("TLA", "waiting " + request[0].toString())
                    Thread.sleep(20)
                    wait++
//                    Log.d("wait", wait.toString())
                    if(wait > 6) {
                        transmitting = false
//                        Log.d("1614", "force quit " + request.contentToString())
                    }
                }
            }else{
                // in case receive error code
                Thread.sleep(100)
            }
        }
    }
    inner class module_alarm(var state: Boolean, var ref: ImageView, private val ok: Int, private val error: Int){
        public fun update(state: Boolean){
            this.state = if(state){
                runOnUiThread {
                    ref.setImageResource(error)
                }
                true
            } else{
                runOnUiThread{
                    ref.setImageResource(ok)
                }
                false
            }
        }
        public fun updateRef(ref: ImageView){
            this.ref = ref
        }
    }
    inner class Present(var next: Button, var back: Button, var picView: ImageView, var run: Button){
        var pic = 0
        private var picList = arrayListOf(R.drawable.pic0)
        private var timerBuffer = arrayListOf<Timer>()
        public fun emerge(){
            pic = picList.size
            next.visibility = View.VISIBLE
            back.visibility = View.VISIBLE
            run.visibility = View.VISIBLE
            picView.visibility = View.VISIBLE
            nextPic()
        }
        public fun submerge(){
            next.visibility = View.GONE
            back.visibility = View.GONE
            run.visibility = View.GONE
            picView.visibility = View.GONE
        }
        private fun nextPic(){
            pic++
            if (pic >= picList.size)
                pic = 0
            runOnUiThread {
                picView.setImageResource(picList[pic])
            }
        }
        private fun prevPic(){
            if (pic == 0)
                pic = picList.size
            pic--
            runOnUiThread{
                picView.setImageResource(picList[pic])
            }
        }
        private fun stop(): Boolean{
            return if(timerBuffer.size > 0) {
                this.timerBuffer[0].cancel()
                this.timerBuffer.removeAt(0)
                false
            }else
                true
        }
        private fun slideShow(period: Long){
            if (stop()){
                this.timerBuffer.add( Timer())
                this.timerBuffer[0].scheduleAtFixedRate(object : TimerTask() {
                    override fun run() {
                        nextPic()
                    }
                }, 1000, period)
            }
        }
        public fun updateRef(next: Button, back: Button, picView: ImageView, run: Button){
            this.next = next
            this.back = back
            this.picView = picView
            this.run = run
            this.next.setOnClickListener {
                this.nextPic()
//                stop()
            }
            this.back.setOnClickListener {
                this.prevPic()
//                stop()
            }
            this.run.setOnClickListener{ this.slideShow(30000) }
        }
    }
}