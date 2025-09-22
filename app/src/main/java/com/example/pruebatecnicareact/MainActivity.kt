package com.example.pruebatecnicareact

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.pruebatecnicareact.databinding.ActivityMainBinding
import com.regula.facesdk.FaceSDK
import com.regula.facesdk.configuration.FaceCaptureConfiguration
import com.regula.facesdk.configuration.InitializationConfiguration
import com.regula.facesdk.detection.request.OutputImageCrop
import com.regula.facesdk.detection.request.OutputImageParams
import com.regula.facesdk.enums.ImageType
import com.regula.facesdk.enums.OutputImageCropAspectRatio
import com.regula.facesdk.model.MatchFacesImage
import com.regula.facesdk.model.results.FaceCaptureResponse
import com.regula.facesdk.model.results.matchfaces.MatchFacesResponse
import com.regula.facesdk.model.results.matchfaces.MatchFacesSimilarityThresholdSplit
import com.regula.facesdk.request.MatchFacesRequest
import java.io.IOException
import kotlin.math.max

enum class ActionPicker {
    CAMERA, REGULA, GALLERY
}

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var imageType1: ImageType
    private lateinit var imageType2: ImageType
    private lateinit var faceBitmaps: ArrayList<Bitmap>
    private var currentImageView: ImageView? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val license = getLicense(this)
        initFaceSdk(license)

        //Bot'on de comparaciión de imágenes
        binding.buttonMatch.setOnClickListener {
            if (binding.imageView1.drawable != null && binding.imageView2.drawable != null) {
                binding.detectionContainer.visibility = View.VISIBLE
                binding.textViewSimilarity.text = "Procesando…"

                matchFaces(getImageBitmap(binding.imageView1), getImageBitmap(binding.imageView2))
                binding.buttonMatch.isEnabled = false
                binding.buttonClear.isEnabled = false
            } else {
                Toast.makeText(
                    this,
                    "Es necesario que estén las 2 imágenes",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        //Reseteamos las imágenes
        binding.buttonClear.setOnClickListener {
            faceBitmaps = ArrayList()
            binding.imageView1.setImageDrawable(null)
            binding.imageView2.setImageDrawable(null)
            binding.textViewSimilarity.text = "Similitud:"
            binding.imageViewerContainer.visibility = View.GONE
            binding.detectionContainer.visibility = View.GONE
        }

        binding.buttonSee.setOnClickListener {
            binding.imageViewerContainer.visibility = View.VISIBLE
            setImage(binding.imageViewCrop, faceBitmaps[0])
        }

        var num = 0
        binding.buttonNext.setOnClickListener {
            num++
            if (num >= faceBitmaps.size)
                num = 0
            setImage(binding.imageViewCrop, faceBitmaps[num])
        }

        //Inicializaciónb
        imageType1 = ImageType.PRINTED
        imageType2 = ImageType.PRINTED

        //Al pulsar botón de abrir la cámara para la primera imágen
        binding.buttonCamera1.setOnClickListener {
            buttonAction(binding.imageView1, ActionPicker.CAMERA)
        }

        //Al pulsar botón de la galería para la primera imágen
        binding.buttonGallery1.setOnClickListener {

            buttonAction(binding.imageView1, ActionPicker.GALLERY)
        }

        //Al pulsar botón de capturar con Regula para la primera imágen
        binding.regulaCapture1.setOnClickListener {

            buttonAction(binding.imageView1, ActionPicker.REGULA)
        }

        //Al pulsar botón de abrir la cámara para la segunda imágen
        binding.buttonCamera2.setOnClickListener {

            buttonAction(binding.imageView2, ActionPicker.CAMERA)
        }

        //Al pulsar botón de la galería para la segunda imágen
        binding.buttonGallery2.setOnClickListener {

            buttonAction(binding.imageView2, ActionPicker.GALLERY)
        }

        //Al pulsar botón de capturar con Regula para la segunda imágen
        binding.regulaCapture2.setOnClickListener {

            buttonAction(binding.imageView2, ActionPicker.REGULA)
        }

    }

    //Inicialización de FaceSDK
    private fun initFaceSdk(license: ByteArray?) {

        binding.mainlayout.visibility = View.GONE
        license?.let {
            val initConfig: InitializationConfiguration =
                InitializationConfiguration.Builder(license).setLicenseUpdate(true).build()
            FaceSDK.Instance().initialize(this, initConfig) { status, e ->
                binding.progressLayout.visibility = View.INVISIBLE
                if (!status) {
                    Log.d("MainActivity", "FaceSDK error: " + e?.message)
                    Toast.makeText(
                        this,
                        "Error en la inicialización: " + if (e != null) e.message else "",
                        Toast.LENGTH_LONG
                    ).show()
                }
                binding.mainlayout.visibility = View.VISIBLE
                Log.d("MainActivity", "Inicilización FaceSDK completada ")
            }
        } ?: return
    }

    //Función para determinar si debemos abrir la cámara, la galería o la cámara de Regula
    private fun buttonAction(imageView: ImageView?, action: ActionPicker) {

        when (action) {
            ActionPicker.CAMERA -> {
                currentImageView = imageView

                openDefaultCamera()
            }

            ActionPicker.REGULA -> {

                startFaceCaptureActivity(imageView)
            }

            ActionPicker.GALLERY -> {
                currentImageView = imageView
                when (imageView) {
                    binding.imageView1 -> {

                        openGallery(PICK_IMAGE_1)
                    }

                    binding.imageView2 -> {

                        openGallery(PICK_IMAGE_2)
                    }
                }

            }
        }
    }

    //Comprobamos si tenemos el perimso de la cámara
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                launchCamera()
            } else {
                Toast.makeText(
                    this,
                    "No hay permisos de cámara",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    //Abrimos la cámara si tenemos el permiso
    private fun openDefaultCamera() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                launchCamera()
            }

            else -> {
                requestPermissionLauncher.launch(
                    Manifest.permission.CAMERA
                )
            }
        }
    }

    //Lanzamos launcher de la cámara
    private fun launchCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        activityResultLauncherCamera.launch(cameraIntent)
    }


    //Recoge el Bitmap del ImageView
    private fun getImageBitmap(imageView: ImageView?): Bitmap {
        imageView?.invalidate()
        val drawable = imageView?.drawable as BitmapDrawable

        return drawable.bitmap
    }

    //Función para abrir la galería
    private fun openGallery(id: Int) {
        val pickPhoto = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        pickPhoto.setType("image/*")
        pickPhoto.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        pickPhoto.putExtra("PICK_ACTION", id)
        activityResultLauncherGallery.launch(pickPhoto)
    }

    //Función para inicializar la capturadora de Regula
    private fun startFaceCaptureActivity(imageView: ImageView?) {
        val configuration = FaceCaptureConfiguration.Builder().setCameraSwitchEnabled(true).build()

        FaceSDK.Instance().presentFaceCaptureActivity(
            this,
            configuration
        ) { faceCaptureResponse: FaceCaptureResponse? ->
            if (faceCaptureResponse?.image != null) {
                imageView!!.setImageBitmap(faceCaptureResponse.image!!.bitmap)

                when (imageView) {
                    binding.imageView1 -> {

                        imageType1 = ImageType.LIVE
                    }

                    binding.imageView2 -> {

                        imageType2 = ImageType.LIVE
                    }
                }
            }
        }
    }

    //Función para comparar las imágenes
    private fun matchFaces(first: Bitmap, second: Bitmap) {
        //Coge las 2 imágenes insertadas e inicia la comparación
        val firstImage = MatchFacesImage(first, imageType1, false)
        val secondImage = MatchFacesImage(second, imageType2, false)
        val matchFacesRequest = MatchFacesRequest(arrayListOf(firstImage, secondImage))

        //Recorta la cara y pone un fondo blanco
        val crop = OutputImageCrop(
            OutputImageCropAspectRatio.OUTPUT_IMAGE_CROP_ASPECT_RATIO_3X4
        )
        val outputImageParams = OutputImageParams(crop, Color.WHITE)
        matchFacesRequest.outputImageParams = outputImageParams


        FaceSDK.Instance()
            .matchFaces(this, matchFacesRequest) { matchFacesResponse: MatchFacesResponse ->
                val split = MatchFacesSimilarityThresholdSplit(matchFacesResponse.results, 0.75)
                val similarity = if (split.matchedFaces.size > 0) {
                    split.matchedFaces[0].similarity
                } else if (split.unmatchedFaces.size > 0) {
                    split.unmatchedFaces[0].similarity
                } else {
                    null
                }
                //Pone la similitud entre las dos imágenes
                val text = similarity?.let {
                    "Similitud: " + String.format("%.2f", it * 100) + "%"
                } ?: matchFacesResponse.exception?.let {
                    "Similitud: " + it.message
                } ?: "Similitud: "

                binding.textViewSimilarity.text = text

                faceBitmaps = ArrayList()

                //Inserta las imagenes detectadas en el array
                for (matchFaces in matchFacesResponse.detections) {
                    for (face in matchFaces.faces)
                        face.crop?.let {
                            faceBitmaps.add(it)
                        }
                }

                val l = faceBitmaps.size
                if (l > 0) {
                    binding.buttonSee.text = "Detecciones ($l)"
                    binding.buttonSee.visibility = View.VISIBLE
                } else {
                    binding.buttonSee.visibility = View.GONE
                }

                binding.buttonMatch.isEnabled = true
                binding.buttonClear.isEnabled = true
            }
    }

    companion object {
        private const val PICK_IMAGE_1 = 1
        private const val PICK_IMAGE_2 = 2
    }

    //Función para leer la licencia de Regula
    fun getLicense(context: Context?): ByteArray? {
        if (context == null) return null
        val licInput = context.resources.openRawResource(R.raw.regula)
        val available: Int = try {
            licInput.available()
        } catch (e: IOException) {
            return null
        }
        val license = ByteArray(available)
        try {
            licInput.read(license)
        } catch (e: IOException) {
            return null
        }
        return license
    }

    //Redimensionamos la imágen para que la altura no supere maxSize
    fun transform(source: Bitmap?, maxSize: Int): Bitmap? {
        var result: Bitmap? = null

        if (source != null) {
            var width = source.width
            var height = source.height

            if (max(width, height) <= maxSize)
                return source

            //Calculamos proporcion
            val bitmapRatio = width.toFloat() / height.toFloat()

            if (bitmapRatio > 1) {
                width = maxSize;
                height = (width / bitmapRatio).toInt()
            } else {
                height = maxSize;
                width = (height * bitmapRatio).toInt()
            }

            result = Bitmap.createScaledBitmap(source, width, height, true)
            source.recycle()
        }

        return result
    }

    //Función para mostrar las imágenes recortadas
    private fun setImage(imageView: ImageView, image: Bitmap?) {
        image?.let {
            imageView.setImageBitmap(it)
        }
    }

    //Launcher para mostrar resultados de la cámara
    var activityResultLauncherCamera: ActivityResultLauncher<Intent> =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
            object : ActivityResultCallback<ActivityResult> {
                override fun onActivityResult(result: ActivityResult) {
                    if (result.resultCode == RESULT_OK) {


                        val photo =
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) result.data?.getParcelableExtra(
                                "data",
                                Bitmap::class.java
                            )
                            else result.data!!.extras!!.getParcelable("data")
                        if (photo is Bitmap)
                            currentImageView?.setImageBitmap(photo)
                    }

                }
            })

    //Launcher para mostrar resultados de la galería
    var activityResultLauncherGallery: ActivityResultLauncher<Intent> =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
            object : ActivityResultCallback<ActivityResult> {
                override fun onActivityResult(result: ActivityResult) {
                    val data = result.data

                    if (result.resultCode == RESULT_OK) {

                        var imageUri: Uri? = null
                        imageUri = data?.data
                        processImageFromGallery(imageUri)
                    } else {
                        return
                    }

                }
            })

    //Función para procesar la imágen recibida del result
    private fun processImageFromGallery(imageUri: Uri?) {
        binding.textViewSimilarity.text = "Similitud:"

        imageUri?.let {
            val bitmap = contentResolver?.openInputStream(it).use { data ->
                BitmapFactory.decodeStream(data)
            }
            val resizedBitmap = transform(bitmap, 1080)
            currentImageView?.setImageBitmap(resizedBitmap)
        }
    }
}