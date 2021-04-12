package pl.polsl.iwsk

import android.content.Context
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import java.lang.Integer.toHexString

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val button = findViewById<Button>(R.id.button)
        val textview = findViewById<TextView>(R.id.textview)

        button.setOnClickListener {
            val manager = getSystemService(Context.USB_SERVICE) as UsbManager
            val deviceList: HashMap<String, UsbDevice> = manager.deviceList
            var text = "Urządzenia:\n"
            deviceList.values.forEach {device ->
                text += (device.deviceName + "\t" + device.productName + "\t" + toHexString(device.vendorId).padStart(4,'0') + ":" + toHexString(device.productId).padStart(4, '0') + "\n")
            }
            textview.text = text
        }
    }
}