package com.example.demorfidapp.view

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import com.example.demorfidapp.R
import com.example.demorfidapp.databinding.ActivityAdminBinding
import com.example.demorfidapp.helper.Constants
import com.example.demorfidapp.helper.SessionManager
import com.example.demorfidapp.helper.Utils

class AdminActivity : AppCompatActivity() {
    lateinit var binding: ActivityAdminBinding
    private lateinit var session: SessionManager
    private var baseUrl: String? = ""
    private var antennaPower: String ?=""
    private lateinit var deatails: HashMap<String, String?>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_admin)
        setSupportActionBar(binding.adminToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        session = SessionManager(this)
        deatails = session.getUserDetails()

        try{
            //
            baseUrl = deatails[Constants.SET_BASE_URL]
                if(baseUrl!=null)
                {
                    //baseUrl=baseUrl!!.replace("/Service/api/", "")
                    //baseUrl=baseUrl!!.replace("/api/", "")
                    binding.edBaseUrl.setText(baseUrl)
                }
                else
                {
                    binding.edBaseUrl.setHint("Please set URL With Http/Https://")
                    binding.edBaseUrl.setText("")
                }
            //
            antennaPower = deatails[Constants.SET_ANTENNA_POWER]
            if(antennaPower!=null)
            {
                if(antennaPower!=null)
                {
                    binding.edAntennaPower.setText(antennaPower)
                }
                else
                {
                    binding.edBaseUrl.setHint("130")
                    binding.edBaseUrl.setText("")
                }
                println("Antennapower $antennaPower")
            }

        }
        catch (e:Exception)
        {}
        binding.setBaseUrl.setOnClickListener {
            checkinputUrl()
        }

        binding.setAntennaPower.setOnClickListener {
            checkinputAntennaPower()
        }

    }

    private fun checkinputUrl() {
        val url: String = binding.edBaseUrl.getText().toString().trim()
        if (url.isEmpty() || url.equals("")) {
            binding.edBaseUrl.setError("Please enter ip address")
        } else {
            Utils.setSharedPrefs(this@AdminActivity, Constants.SET_BASE_URL, "$url")
            showCustomDialogFinish(
                this@AdminActivity,
                "Base Url Updated!!"
            )
        }
    }

    private fun checkinputAntennaPower() {
        val url: String = binding.edAntennaPower.getText().toString().trim()
        if (url.isEmpty() || url.equals("")) {
            binding.edBaseUrl.setError("Please enter ip address")
        } else {
            Utils.setSharedPrefs(this@AdminActivity, Constants.SET_ANTENNA_POWER, url )
            showCustomDialogFinish(
                this@AdminActivity,
                "Antenna Power Updated!!"
            )
        }
    }

    fun showCustomDialogFinish(context: Context?, message: String?) {
        val builder = AlertDialog.Builder(context)
        builder.setMessage(message)
            .setCancelable(false)
            .setPositiveButton("OK") { dialogInterface, i ->
                val intent = Intent(applicationContext, MainActivity::class.java)
                startActivity(intent)
                finishAffinity()
            }
            .show()
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }
}