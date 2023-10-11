package com.example.smart_iron

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.InputType
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AlertDialog

class MainActivity : AppCompatActivity() {
    private lateinit var materialSpinner: Spinner
    private lateinit var temperatureTextView: TextView
    private lateinit var waterTextView: TextView
    private lateinit var electricityTextView: TextView
    private lateinit var hotTemperatureTextView: TextView
    private lateinit var usedWaterTextView: TextView
    private lateinit var ironButton: Button
    private lateinit var resetButton: Button
    private lateinit var ironingTimerTextView: TextView
    private lateinit var clothesCountTextView: TextView
    private lateinit var pourWaterButton: Button
    private lateinit var pourWaterTextView: TextView

    private val materialTemperatures = mapOf(
        "Pamuk" to 100,
        "Poliester" to 80,
        "Svila" to 70
    )

    private var remainingWater = 400
    private var usedWater = 0
    private var usedElectricity = 0.00
    private var hotTemperature = 0
    private var isIroning = false
    private lateinit var handler: Handler
    private lateinit var ironingRunnable: Runnable
    private var ironingStartTime = 0L
    private var elapsedTimeInSeconds = 0
    private var clothesCount = 0
    private var lastMaterialSelectionTime = 0L
    private var selectedMaterial = ""
    private var isFirstIroning = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val backButton = findViewById<ImageView>(R.id.backButton)

        backButton.setOnClickListener(View.OnClickListener {
            // Handle the click event here, for example, to navigate back onBackPressed()
            val intent = Intent(this, WelcomeActivity::class.java)
            startActivity(intent)
        })

        materialSpinner = findViewById(R.id.materialSpinner)
        temperatureTextView = findViewById(R.id.temperatureTextView)
        waterTextView = findViewById(R.id.waterTextView)
        electricityTextView = findViewById(R.id.electricityTextView)
        hotTemperatureTextView = findViewById(R.id.hotTemperatureTextView)
        usedWaterTextView = findViewById(R.id.usedWaterTextView)
        ironButton = findViewById(R.id.ironButton)
        resetButton = findViewById(R.id.resetButton)
        ironingTimerTextView = findViewById(R.id.ironingTimerTextView)
        clothesCountTextView = findViewById(R.id.clothesCountTextView)
        pourWaterButton = findViewById(R.id.pourWaterButton)
        pourWaterTextView = findViewById(R.id.pourWaterTextView)

        // Postavite početne vrijednosti
        temperatureTextView.text = "Temperatura: 0°C"
        waterTextView.text = "Preostala voda: 400 ml"
        electricityTextView.text = "Potrošena struja: 0 kWh"
        hotTemperatureTextView.text = "Trenutna temperatura vrelog dijela: 0°C"
        usedWaterTextView.text = "Potrošena voda: 0 ml"
        ironingTimerTextView.text = "Vrijeme peglanja: 0 sekundi"
        clothesCountTextView.text = "Broj ispeglanih komada: $clothesCount"
        materialSpinner.isEnabled = false  // Postavite Spinner na onemogućen način početno
        pourWaterTextView.visibility = View.GONE
        pourWaterButton.visibility = View.GONE

        val materialArray = resources.getStringArray(R.array.materials)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, materialArray)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        materialSpinner.adapter = adapter

        materialSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedMaterial = materialSpinner.selectedItem.toString()
                val temperature = materialTemperatures[selectedMaterial]
                temperatureTextView.text = "Temperatura: ${temperature}°C"
                lastMaterialSelectionTime = System.currentTimeMillis()

                // Povećajte broj ispeglane odjeće svaki put kad se odabere materijal
                if (!isFirstIroning) {
                    clothesCount++
                    clothesCountTextView.text = "Broj ispeglanih komada: $clothesCount"
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Ne radi ništa
            }
        }

        pourWaterButton.setOnClickListener {
            showWaterInputDialog()
        }

        handler = Handler()
        ironingRunnable = object : Runnable {
            override fun run() {
                if (isIroning) {
                    elapsedTimeInSeconds++
                    // Ažurirajte brojač vremena svake sekunde
                    val timeText = "Vrijeme peglanja: $elapsedTimeInSeconds sekundi"
                    ironingTimerTextView.text = timeText

                    // Provjerite je li prošlo više od 10 sekundi od zadnjeg odabira materijala
                    if (System.currentTimeMillis() - lastMaterialSelectionTime >= 10000) {
                        // Samo ako je prošlo više od 10 sekundi, povećajte broj ispeglanih komada
                        clothesCount++
                        clothesCountTextView.text = "Broj ispeglanih komada: $clothesCount"
                        lastMaterialSelectionTime = System.currentTimeMillis()
                    }

                    if (elapsedTimeInSeconds % 10 == 0) {
                        // Ažurirajte ostale vrijednosti svakih 10 sekundi
                        updateIronValues()
                    }

                    handler.postDelayed(this, 1000) // 1000 ms = 1 sekunda
                }
            }
        }

        ironButton.setOnClickListener {
            if (isIroning) {
                isIroning = false
                materialSpinner.isEnabled = false
                ironButton.text = "Peglaj"
                handler.removeCallbacks(ironingRunnable)
            } else {
                isIroning = true
                materialSpinner.isEnabled = true
                ironButton.text = "Ugasi peglu"
                ironingStartTime = System.currentTimeMillis()

                if (isFirstIroning) {
                    isFirstIroning = false
                }

                handler.post(ironingRunnable)
            }
        }

        resetButton.setOnClickListener {
            resetIronValues()
        }
    }

    private fun calculateHotTemperature(materialTemperature: Int, usedWater: Int): Int {
        return materialTemperature + usedWater / 10
    }

    private fun updateWaterAndElectricityViews() {
        waterTextView.text = "Preostala voda: $remainingWater ml"
        electricityTextView.text = String.format("Potrošena struja: %.2f kWh", usedElectricity)
        hotTemperatureTextView.text = "Trenutna temperatura vrelog dijela: $hotTemperature°C"
        usedWaterTextView.text = "Potrošena voda: $usedWater ml"
    }

    private fun updateIronValues() {
        val materialTemperature = materialTemperatures[selectedMaterial] ?: 0
        val waterToAdd = 100 // Količina vode potrebna svakih 10 sekundi
        usedWater += waterToAdd
        usedElectricity += 0.05
        remainingWater -= waterToAdd // Ovdje se oduzima točno onoliko koliko je potrošeno

        if (remainingWater < 0) {
            remainingWater = 0 // Osigurajte da preostala voda nikada ne padne ispod 0
        }

        hotTemperature = calculateHotTemperature(materialTemperature, usedWater)
        updateWaterAndElectricityViews()

        if (remainingWater <= 0) {
            showWaterInputDialog()
            isIroning = false // Isključite peglu kad nestane vode
            ironButton.text = "Peglaj"
        }
    }

    private fun resetIronValues() {
        remainingWater = 400
        usedWater = 0
        usedElectricity = 0.00
        hotTemperature = 0
        isIroning = false
        ironButton.text = "Peglaj"
        elapsedTimeInSeconds = 0
        handler.removeCallbacks(ironingRunnable)
        updateWaterAndElectricityViews()
        ironingTimerTextView.text = "Vrijeme peglanja: 0 sekundi"
        clothesCount = 0
        clothesCountTextView.text = "Broj ispeglanih komada: $clothesCount"
        ironButton.isEnabled = true
        pourWaterButton.visibility = View.GONE
        materialSpinner.isEnabled = false
        pourWaterTextView.visibility = View.GONE
    }

    private fun showWaterInputDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Unesite količinu vode (max. 400 ml)!")

        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_NUMBER
        builder.setView(input)

        builder.setPositiveButton("OK") { _, _ ->
            val inputText = input.text.toString()
            if (inputText.isNotEmpty()) {
                val waterToAdd = inputText.toInt()
                if (waterToAdd > 0 && waterToAdd <= 400) {
                    remainingWater += waterToAdd
                    updateWaterAndElectricityViews()
                    pourWaterButton.visibility = View.GONE // Sakrij gumb "Ulij" nakon što je voda dodana
                    ironButton.isEnabled = true // Omogući ponovno peglanje nakon što korisnik unese vodu
                    pourWaterTextView.visibility = View.GONE
                } else {
                    showWaterInputDialog() // Ponovno prikažite dijaloški okvir ako je unos neispravan
                }
            }
        }

        builder.setNegativeButton("Odustani") { dialog, _ ->
            dialog.cancel()
            pourWaterButton.visibility = View.VISIBLE // Prikaži gumb "Ulij" kad korisnik odustane
            pourWaterTextView.visibility = View.VISIBLE
        }

        builder.setOnCancelListener {
            pourWaterButton.visibility = View.VISIBLE // Prikaži gumb "Ulij" kad korisnik zatvori dijaloški okvir
            pourWaterTextView.visibility = View.VISIBLE
        }

        val dialog = builder.create()
        dialog.setCancelable(false) // Ovo sprječava zatvaranje dijaloškog okvira kad se klikne izvan njega
        dialog.show()
        ironButton.isEnabled = false // Onemogući gumb za peglanje dok je dijaloški okvir otvoren
        materialSpinner.isEnabled = false // Onemogući Spinner dok se voda ne unese
    }
}