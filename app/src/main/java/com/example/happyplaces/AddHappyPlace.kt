package com.example.happyplaces

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.view.View
import android.widget.Toast
import com.example.happyplaces.databinding.ActivityAddHappyPlaceBinding
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class AddHappyPlace : AppCompatActivity(), View.OnClickListener {

 private var binding:ActivityAddHappyPlaceBinding?=null

    private var cal = Calendar.getInstance()
    private lateinit var dateSetListener:DatePickerDialog.OnDateSetListener
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityAddHappyPlaceBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        dateSetListener=DatePickerDialog.OnDateSetListener{
            view, year, month, dayOfMonth ->
            cal.set(Calendar.YEAR,year)
            cal.set(Calendar.MONTH,month)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateView()
        }
        binding?.etDate?.setOnClickListener(this)
        binding?.tvAddImage?.setOnClickListener(this)
    }
    override fun onClick(v: View?) {
       when(v!!.id){
           R.id.et_date ->{
               DatePickerDialog(this@AddHappyPlace,
                   dateSetListener,
                   cal.get(Calendar.YEAR),
                   cal.get(Calendar.MONTH),
                   cal.get(Calendar.DAY_OF_MONTH)).show()
           }
           R.id.tv_add_image ->{
               val pictureDialog = AlertDialog.Builder(this)
               pictureDialog.setTitle("Select Action")
               val pictureDialogItems = arrayOf("Select photo from Gallery", "Capture photo from camera")
               pictureDialog.setItems(pictureDialogItems){
                   _, which ->
                   when(which){
                       0 -> choosePhotoFromGallery()
                       1 -> takePhotoFromCamera()
                   }
               }
               pictureDialog.show()
           }

       }
    }




    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK)
        {
            if (resultCode== GALLERY){
                if (data!=null){
                    val contentUri = data.data
                    try {
                        val selectedImageBitmap= MediaStore.Images.Media.getBitmap(this.contentResolver, contentUri)
                        binding?.ivPlaceImage?.setImageBitmap(selectedImageBitmap)
                    }catch (e:IOException){
                        e.printStackTrace()
                        Toast.makeText(this@AddHappyPlace,"Something went wrong", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            else if (resultCode == CAMERA){
                val thumbnail:Bitmap= data!!.extras!!.get("data") as Bitmap
                binding?.ivPlaceImage?.setImageBitmap(thumbnail)
            }
        }
    }
    private fun takePhotoFromCamera(){
        Dexter.withContext(this).withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
        ).withListener(object: MultiplePermissionsListener {
            override  fun  onPermissionsChecked(report: MultiplePermissionsReport?)
            {
                if (report!!.areAllPermissionsGranted()){
                    val galleryIntent=Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(galleryIntent, CAMERA)

                }
            }
            override fun  onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>?, token: PermissionToken?)
            {
                showRationalDialogForPermissions()
            }
        }).onSameThread().check()
    }

    private fun choosePhotoFromGallery() {
       Dexter.withContext(this).withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,
       Manifest.permission.WRITE_EXTERNAL_STORAGE
       ).withListener(object: MultiplePermissionsListener {
           @SuppressLint("SuspiciousIndentation")
           override  fun  onPermissionsChecked(report: MultiplePermissionsReport)
           {
               if (report.areAllPermissionsGranted()){
                val galleryIntent=Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                   startActivityForResult(galleryIntent, GALLERY)
               }
          }
           override fun  onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>, token: PermissionToken)
           {
               showRationalDialogForPermissions()
           }
       }).onSameThread().check()

    }
    private fun showRationalDialogForPermissions(){
        androidx.appcompat.app.AlertDialog.Builder(this).setMessage("It looks like you have turned off permission required" +
        "for this feature. it can be enabled under the "+ "Application Settings"
        )
            .setPositiveButton("Go To Settings"){_,_->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                }catch (e:ActivityNotFoundException){
                    e.printStackTrace()
                }
            }
            .setNegativeButton("Cancel"){
                dialog,_->
                dialog.dismiss()
            }.show()

    }

    private fun updateDateView(){
        val myFormat= "dd.MM.yyyy"
        val sdf= SimpleDateFormat(myFormat, Locale.getDefault())
        binding?.etDate?.setText(sdf.format(cal.time).toString())
    }
    companion object{
        private const val GALLERY =1
        private const val CAMERA =2

    }
}