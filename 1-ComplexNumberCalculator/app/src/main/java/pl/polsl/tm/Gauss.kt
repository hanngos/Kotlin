package pl.polsl.tm

import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import kotlin.math.abs


class Gauss : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gauss)

        val num1 = intent.getStringExtra(EXTRA_RE)
        val num2 = intent.getStringExtra(EXTRA_IM)

        val re = num1?.toDouble()?: 0.0
        val im = num2?.toDouble()?: 0.0

        val graph = findViewById<GraphView>(R.id.graph)
        val series: LineGraphSeries<DataPoint> = LineGraphSeries<DataPoint>()
        series.appendData(DataPoint(re, im), false, 100)
        graph.addSeries(series)
        series.color = Color.MAGENTA
        series.isDrawDataPoints = true

        graph.viewport.isXAxisBoundsManual = true
        graph.viewport.isYAxisBoundsManual = true
        graph.viewport.setMaxX(3 * abs(if(re == 0.0) { 1.0} else {re}))
        graph.viewport.setMinX(-3 * abs(if(re == 0.0) { 1.0} else {re}))
        graph.viewport.setMinY(-3 * abs(if(im == 0.0) { 1.0} else {im}))
        graph.viewport.setMaxY(3 *abs(if(im == 0.0) { 1.0} else {im}))

    }

}