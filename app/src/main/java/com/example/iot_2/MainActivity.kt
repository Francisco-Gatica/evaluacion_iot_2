package com.example.iot_2

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class MainActivity : AppCompatActivity() {

    private lateinit var tvStatusMessage: TextView
    private lateinit var tvSensorMQ2: TextView
    private lateinit var tvSensorKY026: TextView
    private var tvFanStatus: TextView? = null
    private var btnLogout: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvStatusMessage = findViewById(R.id.tvStatusMessage)
        tvSensorMQ2 = findViewById(R.id.tvSensorMQ2)
        tvSensorKY026 = findViewById(R.id.tvSensorKY026)
        tvFanStatus = findViewById(R.id.tvFanStatus)
        btnLogout = findViewById(R.id.btnLogout)

        btnLogout?.setOnClickListener {
            cerrarSesion()
        }

        // Configurar botones de simulación manual
        configurarPanelSimulacion()

        // Estado inicial: Monitoreo Normal (sin alertas fantasma)
        updateSensorDashboard("NORMAL")
    }

    private fun configurarPanelSimulacion() {
        val btnSimNormal = findViewById<Button>(R.id.btnSimNormal)
        val btnSimGas = findViewById<Button>(R.id.btnSimGas)
        val btnSimHumo = findViewById<Button>(R.id.btnSimHumo)
        val btnSimFuego = findViewById<Button>(R.id.btnSimFuego)

        btnSimNormal?.setOnClickListener {
            updateSensorDashboard("NORMAL")
            Toast.makeText(this, "Estado: Monitoreo Normal", Toast.LENGTH_SHORT).show()
        }

        btnSimGas?.setOnClickListener {
            updateSensorDashboard("GAS")
            Toast.makeText(this, "Simulación: Fuga de Gas (Ventilador activado)", Toast.LENGTH_SHORT).show()
        }

        btnSimHumo?.setOnClickListener {
            updateSensorDashboard("HUMO")
            Toast.makeText(this, "Simulación: Acumulación de Humo (Ventilador activado)", Toast.LENGTH_SHORT).show()
        }

        btnSimFuego?.setOnClickListener {
            updateSensorDashboard("FUEGO")
            Toast.makeText(this, "Simulación: Detección de Fuego (Ventilador apagado)", Toast.LENGTH_SHORT).show()
        }
    }

    private fun cerrarSesion() {
        val masterKey = MasterKey.Builder(this)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        val sharedPreferences = EncryptedSharedPreferences.create(
            this,
            "secure_iot_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )

        sharedPreferences.edit { putBoolean("isLoggedIn", false) }

        Toast.makeText(this, "Sesión cerrada correctamente", Toast.LENGTH_SHORT).show()

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    @SuppressLint("SetTextI18n")
    fun updateSensorDashboard(threatType: String) {
        // La lógica visual replica el comportamiento del actuador semáforo (LED RGB)
        when (threatType) {
            "GAS" -> {
                tvStatusMessage.text = "ALERTA: Fuga de gas detectada"
                tvStatusMessage.setTextColor(ContextCompat.getColor(this, R.color.status_gas))
                tvSensorMQ2.text = "MQ-2: ALTO (Gas Combustible)"
                tvSensorMQ2.setTextColor(ContextCompat.getColor(this, R.color.status_gas))
                tvSensorKY026.text = "KY-026: NORMAL"
                tvSensorKY026.setTextColor(ContextCompat.getColor(this, R.color.status_normal))
                triggerVentilationActuator()
            }
            "HUMO" -> {
                tvStatusMessage.text = "ALERTA: Acumulación de humo"
                tvStatusMessage.setTextColor(ContextCompat.getColor(this, R.color.status_smoke))
                tvSensorMQ2.text = "MQ-2: ALTO (Partículas de humo)"
                tvSensorMQ2.setTextColor(ContextCompat.getColor(this, R.color.status_smoke))
                tvSensorKY026.text = "KY-026: NORMAL"
                tvSensorKY026.setTextColor(ContextCompat.getColor(this, R.color.status_normal))
                triggerVentilationActuator()
            }
            "FUEGO" -> {
                tvStatusMessage.text = "PELIGRO CRÍTICO: Fuego directo detectado"
                tvStatusMessage.setTextColor(ContextCompat.getColor(this, R.color.status_fire))
                tvSensorMQ2.text = "MQ-2: VARIABLE"
                tvSensorMQ2.setTextColor(ContextCompat.getColor(this, R.color.status_fire))
                tvSensorKY026.text = "KY-026: ALTO (Radiación IR)"
                tvSensorKY026.setTextColor(ContextCompat.getColor(this, R.color.status_fire))
                tvFanStatus?.text = "APAGADO"
                tvFanStatus?.setTextColor(ContextCompat.getColor(this, R.color.status_off))
                // No se activa ventilación para no avivar el fuego
            }
            else -> {
                tvStatusMessage.text = "Monitoreo Normal"
                tvStatusMessage.setTextColor(ContextCompat.getColor(this, R.color.status_normal))
                tvSensorMQ2.text = "NORMAL"
                tvSensorMQ2.setTextColor(ContextCompat.getColor(this, R.color.status_normal))
                tvSensorKY026.text = "NORMAL"
                tvSensorKY026.setTextColor(ContextCompat.getColor(this, R.color.status_normal))
                tvFanStatus?.text = "APAGADO"
                tvFanStatus?.setTextColor(ContextCompat.getColor(this, R.color.status_off))
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun triggerVentilationActuator() {
        // Función para enviar comando HTTP/MQTT al microcontrolador para activar el Ventilador Modelo 4010
        // como medida de mitigación pasiva
        tvFanStatus?.text = "ENCENDIDO"
        tvFanStatus?.setTextColor(ContextCompat.getColor(this, R.color.status_gas))
    }
}
