package com.example.analogwatch

import android.R.layout.simple_spinner_item
import android.graphics.Color
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.example.analogwatch.databinding.ActivityCustomWatchBinding

class CustomWatchActivity : AppCompatActivity() {

    private lateinit var adapterViewElementsName: ArrayAdapter<String>
    private lateinit var adapterOfColors: ArrayAdapter<String>

    private lateinit var binding: ActivityCustomWatchBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomWatchBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        initSpinners()

        initButton()
    }

    private fun initButton() {
        binding.buttonChangeColor.setOnClickListener {
            val id = binding.spinnerOfElements.selectedItemPosition
            val color = when (binding.spinnerOfColor.selectedItemPosition) {
                0 -> Color.BLACK
                1 -> Color.WHITE
                2 -> Color.RED
                3 -> Color.GREEN
                4 -> Color.BLUE
                else -> Color.WHITE
            }
            when (id) {
                0 -> binding.analogWatchView.changeWatchCaseColor(color)
                1 -> binding.analogWatchView.changeWatchBodyColor(color)
                2 -> binding.analogWatchView.changeTimeNumbersColor(color)
                3 -> binding.analogWatchView.changeDelimitersColor(color)
                4 -> binding.analogWatchView.changeHourArrowColor(color)
                5 -> binding.analogWatchView.changeMinArrowColor(color)
                6 -> binding.analogWatchView.changeSecArrowColor(color)
            }
        }
    }

    private fun initSpinners() {
        adapterViewElementsName =
            spinnerAdapterMake(resources.getStringArray(R.array.listViewElementsName))
        adapterOfColors = spinnerAdapterMake(resources.getStringArray(R.array.listOfColors))
        binding.spinnerOfElements.adapter = adapterViewElementsName
        binding.spinnerOfColor.adapter = adapterOfColors
    }

    private fun spinnerAdapterMake(list: Array<String>): ArrayAdapter<String> {
        return ArrayAdapter<String>(
            this,
            simple_spinner_item,
            list
        )

    }

}