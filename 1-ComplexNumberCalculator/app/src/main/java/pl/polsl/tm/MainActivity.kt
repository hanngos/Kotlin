package pl.polsl.tm

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.AttributeSet
import android.view.View
import android.widget.*


const val EXTRA_RE = "pl.polsl.tm.EXTRA_RE"
const val EXTRA_IM = "pl.polsl.tm.EXTRA_IM"

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val reA = findViewById<EditText>(R.id.ReA)
        val reB = findViewById<EditText>(R.id.ReB)
        val imA = findViewById<EditText>(R.id.ImA)
        val imB = findViewById<EditText>(R.id.ImB)

        fun isOk(): Boolean {
            var ok = true

            if (reA.text.toString().isEmpty()) {
                reA.error = "No input!"
                ok = false
            }
            if (reB.text.toString().isEmpty()) {
                reB.error = "No input!"
                ok = false
            }
            if (imA.text.toString().isEmpty()) {
                imA.error = "No input!"
                ok = false
            }
            if (imB.text.toString().isEmpty()) {
                imB.error = "No input!"
                ok = false
            }
            return ok
        }

        fun add() {
            if (isOk()) {
                val reAVal = (reA.text.toString()).toDouble()
                val reBVal = (reB.text.toString()).toDouble()
                val imAVal = (imA.text.toString()).toDouble()
                val imBVal = (imB.text.toString()).toDouble()

                val re = reAVal + reBVal
                val im = imAVal + imBVal

                var text = ((String.format("%.12f", re)).replace(',', '.')).toDouble().toString()
                if (im >= 0) {
                    text += "+"
                }
                text += ((String.format("%.12f", im)).replace(',', '.')).toDouble().toString() + "i"

                Toast.makeText(applicationContext, text, Toast.LENGTH_SHORT).show()
                openGauss(re, im)
            }
        }

        fun sub() {
            if (isOk()) {
                val reAVal = (reA.text.toString()).toDouble()
                val reBVal = (reB.text.toString()).toDouble()
                val imAVal = (imA.text.toString()).toDouble()
                val imBVal = (imB.text.toString()).toDouble()
                val re = reAVal - reBVal
                val im = imAVal - imBVal

                val text: String
                text = if (im >= 0) {
                    ((String.format("%.12f", re)).replace(',', '.')).toDouble().toString() + "+" + ((String.format("%.12f", im)).replace(',', '.')).toDouble().toString() + "i"
                } else {
                    ((String.format("%.12f", re)).replace(',', '.')).toDouble().toString() + ((String.format("%.12f", im)).replace(',', '.')).toDouble().toString() + "i"
                }

                Toast.makeText(applicationContext, text, Toast.LENGTH_SHORT).show()
                openGauss(re, im)
            }
        }

        val plus = findViewById<Button>(R.id.plus)
        val minus = findViewById<Button>(R.id.minus)

        plus.setOnClickListener { add() }
        minus.setOnClickListener { sub() }

        val spinnerItems = resources.getStringArray(R.array.spinnerItems)
        val spinner = findViewById<MySpinner>(R.id.spinner)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, spinnerItems)
        spinner.adapter = adapter
        spinner.isSelected = false

        var initSpinner = true

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
            ) {
                if (initSpinner) {
                    initSpinner = false
                } else {
                    val text: String = parent?.getItemAtPosition(position).toString()
                    if (text == "+") {
                        add()
                    } else if (text == "-") {
                        sub()
                    }
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun openGauss(re: Double, im: Double) {
        val intent = Intent(this, Gauss::class.java).apply {
            putExtra(EXTRA_RE, re.toString())
            putExtra(EXTRA_IM, im.toString())
        }
        startActivity(intent)
    }
}

class MySpinner : android.support.v7.widget.AppCompatSpinner {
    constructor(context: Context?) : super(context) {}
    constructor(context: Context?, attS: AttributeSet?) : super(context, attS) {}
    constructor(context: Context?, attS: AttributeSet?, defStyle: Int) : super(context, attS, defStyle) {}

    override fun setSelection(position: Int) {
        val theSame = position == selectedItemPosition
        super.setSelection(position)
        if (theSame) {
            onItemSelectedListener!!.onItemSelected(this, selectedView, position, selectedItemId)
        }
    }
}
