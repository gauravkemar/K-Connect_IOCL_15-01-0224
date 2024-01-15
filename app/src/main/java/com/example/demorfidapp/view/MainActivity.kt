package com.example.demorfidapp.view

import android.app.AlertDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.content.res.AppCompatResources
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.demorfidapp.R
import com.example.demorfidapp.databinding.ActivityMainBinding
import com.example.demorfidapp.databinding.SecurityDialogueBinding
import com.example.demorfidapp.helper.Constants
import com.example.demorfidapp.helper.RFIDHandler
import com.example.demorfidapp.helper.Resource
import com.example.demorfidapp.helper.SessionManager
import com.example.demorfidapp.model.PostRFIDReadRequest
import com.example.demorfidapp.repository.RFIDReaderRepository
import com.kemarport.kdmsmahindra.viewmodel.PostRFIDDataViewModel
import com.kemarport.kdmsmahindra.viewmodel.PostRFIDDataViewModelFactory
import com.zebra.rfid.api3.TagData
import es.dmoral.toasty.Toasty
import java.util.Calendar
import java.util.Date


class MainActivity : AppCompatActivity(), RFIDHandler.ResponseHandlerInterface {
    lateinit var binding: ActivityMainBinding
    private var antennaPower: String? = ""
    private var baseUrl: String = ""
    private lateinit var session: SessionManager
    private lateinit var viewModel: PostRFIDDataViewModel

    //rfid
    var TAG = "MainActivity"
    var rfidHandler: RFIDHandler? = null
    private fun initReader(antennaPower: Int) {
        rfidHandler = RFIDHandler()
        rfidHandler!!.init(this, antennaPower)
    }

    var isRFIDInit = false

    private lateinit var progress: ProgressDialog

    ////scanner
    private lateinit var deatails: HashMap<String, String?>

    private var selectedGateSpinnerString: String? = ""
    var resumeFlag = false
    lateinit var spinnerItems: MutableList<String>

    lateinit var TagDataSet: ArrayList<String>

    lateinit var securityDialogueBinding: SecurityDialogueBinding
    var securityDialog: Dialog? = null
    private var mediaPlayer: MediaPlayer? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        securityDialogueBinding = SecurityDialogueBinding.inflate(getLayoutInflater());
        mediaPlayer = MediaPlayer.create(this, R.raw.scanner_sound)
        securityDialog = Dialog(this)
        checkAndShowPopup()
        progress = ProgressDialog(this)
        progress.setMessage("Please Wait...")
        TagDataSet =  ArrayList()
        Toasty.Config.getInstance()
            .setGravity(Gravity.CENTER)
            .apply()

        setSpinner()
        session = SessionManager(this)
        deatails = session.getUserDetails()
        try {
            if (antennaPower != null) {
                antennaPower = deatails[Constants.SET_ANTENNA_POWER]
                println("Antennapower $antennaPower")
            }
            if (baseUrl != null ) {
                baseUrl = deatails[Constants.SET_BASE_URL].toString()
                println("Antbase $baseUrl")
            }
        } catch (e: Exception) {
            Toasty.warning(this, e.message.toString(), Toasty.LENGTH_SHORT)
                .show()
        }

        //viewmodel
        val kdmsRepository = RFIDReaderRepository()
        val viewModelProviderFactory = PostRFIDDataViewModelFactory(application, kdmsRepository)
        viewModel =
            ViewModelProvider(this, viewModelProviderFactory)[PostRFIDDataViewModel::class.java]
        viewModel.postRFIDDataMutable.observe(this) { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    //binding.edScanRfid.setText("")
                    clear()
                    setSpinner()
                    response.data?.let { resultResponse ->
                        Toasty.success(this, resultResponse, Toasty.LENGTH_SHORT)
                            .show()
                    }
                }
                is Resource.Error -> {
                    hideProgressBar()
                    setSpinner()
                    response.message?.let { resultResponse ->
                        Toasty.error(this, resultResponse, Toasty.LENGTH_SHORT).show()
                    }
                }
                is Resource.Loading -> {
                    showProgressBar()
                }
                else -> {}
            }
        }
        defaulReaderOn(antennaPower)
        binding.btnVehicleMapping.setOnClickListener {
            submit()
        }
        binding.settings.setOnClickListener {
            setSecurityDialog()
        }
        binding.btnClear.setOnClickListener {
            clear()
        }
    }

    private fun setSecurityDialog() {
        securityDialog!!.setContentView(securityDialogueBinding.root)
        securityDialog!!.getWindow()?.setBackgroundDrawable(
            AppCompatResources.getDrawable(
                this@MainActivity,
                android.R.color.transparent
            )
        )
        securityDialog!!.getWindow()?.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        securityDialog!!.setCancelable(true)
        securityDialogueBinding.btnSubmit.setOnClickListener {
            checkSecurity()
        }

        securityDialogueBinding.closeDialogueTopButton.setOnClickListener {
            securityDialog!!.dismiss()
        }
        securityDialog!!.show()
    }

    private fun checkSecurity() {
        val edPassword = securityDialogueBinding.edScanRfid.text.toString().trim()
        if (edPassword.equals("0000")) {
            adminPage()
        } else {
            Toasty.error(this, "Password Wrong!!", Toasty.LENGTH_SHORT).show()
        }

    }

    private fun setSpinner() {
        val languages = resources.getStringArray(R.array.selectgate)
        spinnerItems = mutableListOf(*languages)
        selectedGateSpinnerString = languages.get(0)
        val spinner = findViewById<Spinner>(R.id.spinnerApp)
        if (spinner != null) {

            val adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item, spinnerItems
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            spinner.adapter = adapter

            spinner.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View, position: Int, id: Long
                ) {
                    selectedGateSpinnerString = spinnerItems[position]
                }
                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
        }
    }

    private fun adminPage() {
        startActivity(Intent(this@MainActivity, AdminActivity::class.java))
        securityDialogueBinding.edScanRfid.setText("")
        securityDialog!!.dismiss()
    }

    private fun defaulReaderOn(antennaPower: String?) {
        if (Build.MANUFACTURER.contains("Zebra Technologies") || Build.MANUFACTURER.contains("Motorola Solutions")) {
            isRFIDInit = true
            Thread.sleep(1000)
            initReader(antennaPower?.toInt() ?: 130)
        }
    }

    private fun clear() {
        // binding.edScanRfid.setText("")
        TagDataSet?.clear()
        binding.tvRfid.setError(null)
        binding.autoCompleteTextViewRfid.setHint("Scan RFID")
        binding.autoCompleteTextViewRfid.setText("")
    }
    //viewmodel
    fun submit() {
        try {
            val edRFID = binding.autoCompleteTextViewRfid.text.toString().trim()
            if(baseUrl!="null"){
                if (edRFID.isNotEmpty() && !selectedGateSpinnerString.equals("Select Gate") ) {
                    baseUrl?.let {
                        viewModel.submitRFIDDetails(it, PostRFIDReadRequest(selectedGateSpinnerString, edRFID))
                    }
                }
                else {
                    Toasty.error(
                        this@MainActivity,
                        "Please fill the details",
                        Toasty.LENGTH_SHORT
                    ).show()
                }
            }
            else{
                Toasty.warning(
                    this@MainActivity,
                    "Please set the url!!",
                    Toasty.LENGTH_SHORT
                ).show()
            }


        } catch (e: Exception) {
            Toasty.warning(
                this@MainActivity,
                e.printStackTrace().toString(),
                Toasty.LENGTH_SHORT
            ).show()
        }
    }

    private fun showProgressBar() {
        progress.show()
    }

    private fun hideProgressBar() {
        progress.cancel()
    }

    override fun onStart() {
        super.onStart()
    }

    ///Emdk scanner
    override fun onResume() {
        super.onResume()
        checkAndShowPopup()
        if (resumeFlag) {
            resumeFlag = false
            if (Build.MANUFACTURER.contains("Zebra Technologies") || Build.MANUFACTURER.contains("Motorola Solutions")) {
            }
        }
    }

    override fun handleTagdata(tagData: Array<TagData>) {
        val sb = StringBuilder()
        sb.append(tagData[0].tagID)
           runOnUiThread {
               var tagDataFromScan = tagData[0].tagID
               mediaPlayer?.start()
            if (tagDataFromScan.startsWith("E200")) {
                   //binding.tvBarcode.setText(tagDataFromScan)
                   Log.e(TAG, "RFID Data : $tagDataFromScan")
                   binding.autoCompleteTextViewRfid.setText(tagData[0].tagID.toString())
                   stopInventory()

                   if (!TagDataSet?.contains(tagDataFromScan)!!)
                       TagDataSet.add(tagDataFromScan)
                   val adapter1: ArrayAdapter<String> = ArrayAdapter<String>(
                       this,
                       R.layout.dropdown_menu_popup_item,
                       TagDataSet
                   )
                   runOnUiThread {
                       if (TagDataSet.size == 1) {
                           binding.autoCompleteTextViewRfid.setText(
                               adapter1.getItem(0).toString(),
                               false
                           )
                       } else {
                           binding.autoCompleteTextViewRfid.setText("")
                           binding.tvRfid.setError("Select the RFID value from dropdown")
                       }
                       binding.autoCompleteTextViewRfid.setAdapter<ArrayAdapter<String>>(adapter1)
                   }
               }
           }
      /*  try {
                runOnUiThread {
                    val detectedTag = tagData[0].tagID
                    if (detectedTag != null) {
                        rfidHandler?.stopInventory()
                        if (!TagDataSet?.contains(detectedTag)!!)
                            TagDataSet.add(detectedTag)
                        val adapter1: ArrayAdapter<String> = ArrayAdapter<String>(
                            this,
                            R.layout.dropdown_menu_popup_item,
                            TagDataSet
                        )
                        //runOnUiThread {
                            if (TagDataSet.size == 1) {
                                binding.autoCompleteTextViewRfid.setText(
                                    adapter1.getItem(0).toString(),
                                    false
                                )
                            } else {
                                binding.autoCompleteTextViewRfid.setText("")
                                binding.tvRfid.setError("Select the RFID value from dropdown")
                            }
                            binding.autoCompleteTextViewRfid.setAdapter<ArrayAdapter<String>>(adapter1)
                       // }
                    }
                }

            } catch (e: InvalidUsageException) {
                e.printStackTrace()
            } catch (e: OperationFailureException) {
                e.printStackTrace()
            }*/

    }

 /*   private fun checkAndShowPopup() {
        val currentYear = getCurrentYear()
        if (currentYear == 2024) {
            // Show the non-cancelable popup
            showNonCancelablePopup()
        }
    }*/
    private fun getCurrentYear(): Int {
        val calendar = Calendar.getInstance()
        return calendar.get(Calendar.YEAR)
    }
    private fun getCurrentDate(): Date {
        return Calendar.getInstance().time
    }

    private fun checkAndShowPopup() {
        val currentDate = getCurrentDate()
        val calendar = Calendar.getInstance()
        calendar.time = currentDate

        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

        // Check if it's the year 2024 and on or before March 1
        if (currentYear == 2024 && currentMonth == Calendar.MARCH && currentDay >= 1) {
            // Show the non-cancelable popup
            showNonCancelablePopup()
        }
    }




    override fun onDestroy() {
        super.onDestroy()
        binding.unbind()
        if (isRFIDInit) {
            rfidHandler!!.onDestroy()
        }
        if (mediaPlayer != null) {
            mediaPlayer?.release()
        }

    }

    override fun onPostResume() {
        super.onPostResume()
        if (isRFIDInit) {
            val status = rfidHandler!!.onResume()
            Toast.makeText(this@MainActivity, status, Toast.LENGTH_SHORT).show()
        }

    }

    override fun onPause() {
        super.onPause()
        if (isRFIDInit) {
            rfidHandler!!.onPause()
        }
        resumeFlag = true
    }

    fun performInventory() {
        rfidHandler!!.performInventory()
    }

    fun stopInventory() {
        rfidHandler!!.stopInventory()
    }

    override fun handleTriggerPress(pressed: Boolean) {
        if (pressed) {
            performInventory()
        } else stopInventory()

    }
    private fun showNonCancelablePopup() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Alert")
        builder.setMessage("Please Update Application!!.")
        // Set the dialog as non-cancelable
        builder.setCancelable(false)
        val alertDialog = builder.create()
        // Show the dialog
        alertDialog.show()
    }

}
