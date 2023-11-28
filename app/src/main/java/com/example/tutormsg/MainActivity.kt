package com.example.tutormsg

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.telephony.SmsManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Switch
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tutormsg.databinding.ActivityMainBinding
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.InputStream

class MainActivity : AppCompatActivity() {
    private lateinit var bind : ActivityMainBinding
    private lateinit var excelFileUri: String
    private lateinit var adapter: ExcelDataAdapter
    private lateinit var data: MutableList<List<String>>
    private  lateinit var phoneNo : MutableList<String>
    private  lateinit var msgToPhone : MutableList<String>
    private val SEND_SMS_PERMISSION_REQUEST_CODE = 1
    private lateinit var toggle : ActionBarDrawerToggle

    private val getContent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data: Intent? = result.data
                data?.data?.let { excelFileUri ->
                    readExcelData(excelFileUri)
                }
            } else {
                Toast.makeText(this, "File picking canceled", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bind.root)
        val name=intent.getStringExtra("name")
        bind.tvWel.text="Welcome, $name"

        toggle = ActionBarDrawerToggle(this,bind.drawerLayout,R.string.nav_open,R.string.nav_close)
        bind.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        bind.navView.setNavigationItemSelectedListener {
            when(it.itemId){
                R.id.nav_logout -> {
                    val sharedPreferences = getSharedPreferences("tutor", MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    editor.putBoolean("IS_SIGNED_IN", false)
                    editor.apply()
                    val intent = Intent(this,SignInActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                R.id.nav_account -> {
                    Toast.makeText(this,"Work in Progress",Toast.LENGTH_SHORT).show()
                }
            }
            true
        }

        bind.btnUpload.setOnClickListener {
            pickExcelFile()
        }
        bind.sendMsg.setOnClickListener {
            Toast.makeText(this,"Upload Excel sheet...then try",Toast.LENGTH_SHORT).show()
        }
        adapter = ExcelDataAdapter()
        bind.rvExcel.layoutManager = LinearLayoutManager(this)
        bind.rvExcel.adapter = adapter
    }

    private fun pickExcelFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        getContent.launch(intent)
    }

    private fun readExcelData(excelFileUri: Uri) {
        val inputStream: InputStream = contentResolver.openInputStream(excelFileUri)!!
        // Load the Excel workbook
        val workbook = WorkbookFactory.create(inputStream)
        // Assuming you have only one sheet in your Excel file, otherwise, you can loop through sheets
        val sheet = workbook.getSheetAt(0)
        data = mutableListOf<List<String>>()
        for (row in sheet) {
            val rowData = mutableListOf<String>()
            for (cell in row) {
                if (cell.cellType == CellType.NUMERIC) {
                    rowData.add(cell.numericCellValue.toLong().toString())
                } else {
                    rowData.add(cell.toString())
                }
            }
            if(rowData.isNotEmpty())
                data.add(rowData)
        }
        val no=createIndexMap(data)
        Log.d("hari", no["phone"].toString())
        var totmark = no.keys.count { it.startsWith("mark") } + 1
        var countMark = 1
        var msg =""
        phoneNo = mutableListOf<String>()
        msgToPhone = mutableListOf<String>()
        var flag =true
        for((i,_) in data.withIndex()) {
            if(i==0){
                continue
            }
            msg += "Message from " + intent.getStringExtra("name") + "\nYour ward " + data[i][no["name"]!!] + " with register number " + data[i][no["regNo"]!!] + " has scored " + "\n"
            while (totmark != countMark) {
                val mark = data[0][no["mark$countMark"]!!] + " -> " + data[i][no["mark$countMark"]!!] + "\n"
                msg += mark
                countMark++
            }
            if(no.containsKey("phone")) {
                Log.d("hari",no.toString())
                phoneNo.add(data[i][no["phone"]!!])
                flag = true
                bind.sendMsg.isEnabled = true
            }else{
                flag=false
                break
            }
            msgToPhone.add(msg)
            totmark=no.keys.count { it.startsWith("mark") } + 1
            countMark=1
            msg=""
        }
        if(flag==true) {
            bind.sendMsg.setOnClickListener {
                if (ContextCompat.checkSelfPermission(
                        this,
                        android.Manifest.permission.SEND_SMS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(android.Manifest.permission.SEND_SMS),
                        SEND_SMS_PERMISSION_REQUEST_CODE
                    )
                } else {
                    for ((i, _) in phoneNo.withIndex()) {
                        sendSms(phoneNo[i], msgToPhone[i])
                    }
                }
            }
        }else{
            bind.sendMsg.isEnabled = false
        }
        if(flag==true) {
            adapter.setData(data)
            bind.tvData.text = "Selected Excel Data:"
            bind.rvExcel.setBackgroundColor(ContextCompat.getColor(this, R.color.sandal))
            inputStream.close()
        }else{
            bind.tvData.text = "Excel Data Not in format as (Name,Reg,Phone,Mark)"
            inputStream.close()
        }
    }

    private fun findPhoneIndex(data: MutableList<List<String>>): Int {
        val firstSublist = data.firstOrNull() ?: return -1
        // Find the index of an element containing "phone" or "mobile" as a substring in the first sublist
        return firstSublist.indexOfFirst { it.contains("phone", ignoreCase = true) || it.contains("mobile", ignoreCase = true) }
    }

    fun createIndexMap(data: MutableList<List<String>>): Map<String, Int> {
        val indexMap = mutableMapOf<String, Int>()
        if (data.size > 1) {
            val headerRow = data[1]
            var markCounter = 1
            for ((index, value) in headerRow.withIndex()) {
                val category = when {
                    value.matches(Regex("\\d{1,3}")) -> "mark${markCounter++}" // 1 to 3 digits: mark1, mark2, mark3
                    value.matches(Regex("\\d{10}")) -> "phone"        // 10 digits: phone number
                    value.matches(Regex("[a-zA-Z]+")) -> "name"       // String: name
                    value.matches(Regex("[a-zA-Z\\d]+")) -> "regNo"   // Mix of numbers and letters: reg no
                    else -> "unknown"                                 // Unknown category
                }
                indexMap[category] = index
            }
        }
        return indexMap
    }

     private fun sendSms(phoneNumber: String, message: String) {
        try {
            val smsManager:SmsManager
            if (Build.VERSION.SDK_INT>=23) {
                //if SDK is greater that or equal to 23 then
                //this is how we will initialize the SmsManager
                smsManager = this.getSystemService(SmsManager::class.java)
            }
            else{
                //if user's SDK is less than 23 then
                //SmsManager will be initialized like this
                smsManager = SmsManager.getDefault()
            }
            // on below line we are sending text message.
            smsManager.sendTextMessage(phoneNumber, null, message, null, null)
            // on below line we are displaying a toast message for message send,
            Toast.makeText(applicationContext, "Message Sent", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            // on catch block we are displaying toast message for error.
            Toast.makeText(applicationContext, "not send.."+e.message.toString(), Toast.LENGTH_LONG).show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(toggle.onOptionsItemSelected(item)){
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}