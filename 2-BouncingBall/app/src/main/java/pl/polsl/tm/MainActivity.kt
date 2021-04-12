package pl.polsl.tm

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Resources
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaPlayer
import android.opengl.GLSurfaceView
import android.opengl.GLU
import android.os.Bundle
import android.util.Log
import android.view.View
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.microedition.khronos.opengles.GL10
import kotlin.math.*

class MainActivity : Activity() {

    private var surface: MyGLSurfaceView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        window.decorView.apply {
            systemUiVisibility =
            View.SYSTEM_UI_FLAG_FULLSCREEN
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }

        surface = MyGLSurfaceView(applicationContext)
        setContentView(surface)
    }

    override fun onResume() {
        super.onResume()
        surface?.onResume()
    }

    override fun onPause() {
        super.onPause()
        surface?.onPause()
    }

}

class MyGLSurfaceView(context: Context) : GLSurfaceView(context), SensorEventListener {

    private var sensorManager: SensorManager? = null
    private var sensor: Sensor? = null
    private var mediaPlayer: MediaPlayer = MediaPlayer.create(context, R.raw.pilka)
    private var renderer: MyRenderer? = MyRenderer(mediaPlayer)

    init {
        setRenderer(renderer)
        sensorManager = this.context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val listOfSensors: List<Sensor> = sensorManager!!.getSensorList(Sensor.TYPE_ACCELEROMETER)
        if (listOfSensors.isNotEmpty()) {
            sensor = listOfSensors[0]
            sensorManager!!.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    class MyRenderer(private val mediaPlayer: MediaPlayer) : Renderer {
        private var t = 0.00f
        private var zt = 0.00f
        private val height = Resources.getSystem().displayMetrics.heightPixels
        private val width = Resources.getSystem().displayMetrics.widthPixels

        fun setT(float: Float) {
            zt += round(float *10000) / 10000000
        }

        override fun onDrawFrame(p0: GL10?) {

            val numberOfVertices = 150
            val x = 0.0f
            val y = 0.0f
            val z = -1.0f
            val k: Float = height.toFloat() / width
            val radius = 0.2f / k //szerokość ekranu to 2.0f, czyli 10% to będzie 0.2f, ale przez to, że nie jest to kwadrat należy jeszcze podzielić przez k
            val doublePi: Float = 2.0f * PI.toFloat()

            val vertices = FloatArray(numberOfVertices * 3)
            vertices[0] = x
            vertices[1] = y
            vertices[2] = z

            val alpha = doublePi / (numberOfVertices - 2)

            for (i in 1 until numberOfVertices) {
                vertices[i * 3] = x + (radius * k * cos(i * alpha))
                vertices[(i * 3) + 1] = y + (radius * sin(i * alpha))
                vertices[(i * 3) + 2] = z
            }
            val buffer = ByteBuffer.allocateDirect(numberOfVertices * 3 * 4)
            buffer.order(ByteOrder.nativeOrder())
            val bufferOfVertices = buffer.asFloatBuffer()
            bufferOfVertices.put(vertices)
            bufferOfVertices.position(0)
            p0?.glClear(GL10.GL_COLOR_BUFFER_BIT or GL10.GL_DEPTH_BUFFER_BIT)
            p0?.glColor4f(0.0f, 0.0f, 1.0f, 1.0f)
            p0?.glLoadIdentity()
            t += zt
            if ((t + radius * k) >= 1.00f) {
                if (abs(zt)<0.01f) zt = 0.0f
                else mediaPlayer.start()
                zt *= -0.7f
                t = (1.0f - radius * k)
            } else if ((t - radius * k) <= -1.0f) {
                if (abs(zt)<0.01f) zt = 0.0f
                else mediaPlayer.start()
                zt *= -0.7f
                t = (-1.0f + radius * k)
            }
            p0?.glTranslatef(t, 0f, 0f)
            p0?.glEnableClientState(GL10.GL_VERTEX_ARRAY)
            p0?.glVertexPointer(3, GL10.GL_FLOAT, 0, bufferOfVertices)
            p0?.glDrawArrays(GL10.GL_TRIANGLE_FAN, 0, numberOfVertices)
            p0?.glDisableClientState(GL10.GL_VERTEX_ARRAY)
        }

        override fun onSurfaceChanged(p0: GL10?, width: Int, height: Int) {
            p0?.glViewport(0, 0, width, height)
            p0?.glMatrixMode(GL10.GL_PROJECTION)
            p0?.glLoadIdentity()
            GLU.gluPerspective(p0, 45.0f, 0.5f, -1.0f, -10.0f)
            p0?.glClearColor(0.0f, 1.0f, 0.0f, 1.0f)
        }

        override fun onSurfaceCreated(p0: GL10?, config: javax.microedition.khronos.egl.EGLConfig?) {}
    }

    override fun onSensorChanged(p0: SensorEvent?) {
        if (p0 != null) {
            renderer?.setT(p0.values[1])
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {}

    override fun onResume() {
        sensorManager!!.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        sensorManager!!.unregisterListener(this)
    }

}
