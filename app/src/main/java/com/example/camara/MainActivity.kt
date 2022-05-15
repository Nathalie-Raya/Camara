package com.example.camara

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.camara.databinding.ActivityMainBinding
import java.io.File
import java.lang.Exception
import java.lang.System.currentTimeMillis
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var outPutDirectory: File
    private var imageCapture: ImageCapture? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        outPutDirectory = getOutputDirectory()
        requestPermissions()

        binding.btnTomarFoto.setOnClickListener {
            takePhoto()
        }


    }

    private fun startCamera(){
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder()
                .build()
                .also {
                        mPreview ->
                    mPreview.setSurfaceProvider (
                        binding.viewFinder.surfaceProvider
                    )
                }
            imageCapture = ImageCapture.Builder().build()
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,cameraSelector,preview,imageCapture
                )
            }catch (e: Exception){
                Log.d(Constans.TAG,"Error al inicializar camara")
            }

        },ContextCompat.getMainExecutor(this))
    }

    private fun getOutputDirectory():File{
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
                mFile -> File(mFile,"ejemplocamara").apply {
            mkdirs()
        }
        }
        return if(mediaDir!=null && mediaDir.exists())
            mediaDir else filesDir
    }

    private fun requestPermissions(){
        if (allPermissionGranted()){
            startCamera()
        }else{
            ActivityCompat.requestPermissions(this,Constans.REQUIRED_PERMISSIONS,Constans.REQUEST_CODE_PERMISSIONS)
        }
    }

    private fun allPermissionGranted() =
        Constans.REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(baseContext,it) == PackageManager.PERMISSION_GRANTED

        }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode==Constans.REQUEST_CODE_PERMISSIONS){
            if (allPermissionGranted()){
                startCamera()
            }else{
                finish()
            }
        }
    }

    private fun takePhoto(){
        val imageCapture = imageCapture?:return
        val  photoFile = File(
            outPutDirectory,
            SimpleDateFormat(Constans.FILE_NAME_FORMAT, Locale.getDefault()).format(System.currentTimeMillis())+".jpg")
        val outputOption = ImageCapture
            .OutputFileOptions
            .Builder(photoFile)
            .build()
        //Toast.makeText(this@MainActivity,"foto guardada",Toast.LENGTH_SHORT).show()
        imageCapture.takePicture(
            outputOption,ContextCompat.getMainExecutor(this@MainActivity),
            object: ImageCapture.OnImageSavedCallback{
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    val msg = "Foto guardada"
                    Toast.makeText(this@MainActivity,"foto guardada",Toast.LENGTH_SHORT).show()

                    Log.i(Constans.TAG,"Foto *******: $msg,$savedUri")
                }
                override fun onError(exception: ImageCaptureException) {
                    Log.e(Constans.TAG,"Error:****  ${exception.message.toString()}",exception)
                }

            }
        )

    }

}