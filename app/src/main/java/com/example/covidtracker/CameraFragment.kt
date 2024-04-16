import android.Manifest
import android.animation.ObjectAnimator
import android.content.Context.CAMERA_SERVICE
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.covidtracker.R
import java.io.File


class CameraFragment : Fragment() {

    private lateinit var imageReader: ImageReader
    //private var capturedImageUri: Uri? = null

    private lateinit var textureView: TextureView
    private lateinit var cameraDevice: CameraDevice
    private lateinit var captureSession: CameraCaptureSession
    private lateinit var captureRequestBuilder: CaptureRequest.Builder

    private val handler = Handler(Looper.getMainLooper())

    private lateinit var captureButton: Button
    private lateinit var shareButton: Button

    private val cameraStateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            cameraDevice = camera
            createCaptureSession()
        }

        override fun onDisconnected(camera: CameraDevice) {
            camera.close()
        }

        override fun onError(camera: CameraDevice, error: Int) {
            camera.close()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_camera, container, false)

        captureButton = view.findViewById(R.id.btnCapture)
        shareButton = view.findViewById(R.id.btnShare)

        // Adăugați animația de tip ObjectAnimator pe butonul de capture
        val objectAnimator = ObjectAnimator.ofFloat(captureButton, "rotation", 0f, 360f)
        objectAnimator.duration = 1000

        textureView = view.findViewById(R.id.textureView)
        view.findViewById<Button>(R.id.btnCapture).setOnClickListener {
            objectAnimator.start()
            captureImage()
        }

        view.findViewById<Button>(R.id.btnShare).setOnClickListener {
        }
        /*
        view.findViewById<Button>(R.id.btnShare).setOnClickListener {
            shareImage()
        }*/
        return view
    }

    override fun onResume() {
        super.onResume()
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_CODE
            )
        } else {
            // Check if textureView has valid dimensions
            if (textureView.width == 0 || textureView.height == 0) {
                textureView.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
                    openCamera()
                }
            } else {
                openCamera()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        closeCamera()
    }

    private fun openCamera() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val cameraManager = requireContext().getSystemService(CAMERA_SERVICE) as CameraManager
            val cameraId = cameraManager.cameraIdList[0] // Use the first available camera

            // Check if textureView has valid dimensions
            if (textureView.width == 0 || textureView.height == 0) {
                return
            }

            // Create ImageReader with valid dimensions
            imageReader = ImageReader.newInstance(
                textureView.width,
                textureView.height,
                ImageFormat.JPEG,
                1
            )

            cameraManager.openCamera(cameraId, cameraStateCallback, null)
        } else {
            // Request camera permission
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_CODE
            )
        }
    }

    private fun closeCamera() {
        captureSession.close()
        cameraDevice.close()
    }

    private fun createCaptureSession() {
        val surfaceTexture = textureView.surfaceTexture
        if (surfaceTexture != null) {
            // Set default buffer size
            surfaceTexture.setDefaultBufferSize(textureView.width, textureView.height)

            // Create surfaces
            val surface = Surface(surfaceTexture)
            val imageReaderSurface = imageReader.surface

            // Set up capture request
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            captureRequestBuilder.addTarget(surface)
            captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO)

            val surfaces = listOf(surface, imageReaderSurface)

            cameraDevice.createCaptureSession(
                surfaces,
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(session: CameraCaptureSession) {
                        captureSession = session
                        session.setRepeatingRequest(
                            captureRequestBuilder.build(),
                            null,
                            null
                        )
                    }

                    override fun onConfigureFailed(session: CameraCaptureSession) {
                        // Handle configuration failure
                    }
                },
                null
            )
        }
    }
    /*
    private fun saveImageToGallery(imageFile: File): Uri? {
        val directory = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        if (directory != null && !directory.exists()) {
            directory.mkdirs()
        }
        val directoryPath = directory?.absolutePath
        Log.d("CameraFragment", "Directory Path: $directoryPath")
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "image.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_DCIM}/com.example.covidtracker")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }

        val contentResolver = requireActivity().contentResolver
        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        uri?.let {
            val outputStream = contentResolver.openOutputStream(uri)
            outputStream?.use { stream ->
                val inputStream = FileInputStream(imageFile)
                try {
                    inputStream.copyTo(stream)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            contentValues.clear()
            contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
            try {
                contentResolver.update(uri, contentValues, null, null)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return uri
    }

    private fun shareImage() {
        val imageUri = capturedImageUri
        if (imageUri != null) {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/jpeg"
                putExtra(Intent.EXTRA_STREAM, imageUri)
            }
            startActivity(Intent.createChooser(shareIntent, "Share Image"))
        } else {
            // Nu există o imagine capturată sau salvată în galerie
            Toast.makeText(requireContext(), "No image available", Toast.LENGTH_SHORT).show()
        }
    }*/

    private fun captureImage() {
        val imageFile = File(requireContext().externalMediaDirs.first(), "image.jpg")
        val surfaces = mutableListOf<Surface>()
        surfaces.add(imageReader.surface)
        surfaces.add(Surface(textureView.surfaceTexture))
        //val imageUri = saveImageToGallery(imageFile)
        //capturedImageUri = imageUri

        val captureRequestBuilder =
            cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
        captureRequestBuilder.addTarget(imageReader.surface)
        captureRequestBuilder.set(
            CaptureRequest.CONTROL_MODE,
            CaptureRequest.CONTROL_MODE_AUTO
        )

        val captureCallback = object : CameraCaptureSession.CaptureCallback() {
            override fun onCaptureCompleted(
                session: CameraCaptureSession,
                request: CaptureRequest,
                result: TotalCaptureResult
            ) {
                super.onCaptureCompleted(session, request, result)
                // Image capture completed
                // You can handle the captured image here
            }
        }

        if (textureView.isAvailable && imageReader.surface.isValid && ::captureSession.isInitialized) {
            captureSession.stopRepeating()

            // Delay capturing to ensure the surfaces are ready
            handler.postDelayed({
                try {
                    captureSession.capture(
                        captureRequestBuilder.build(),
                        captureCallback,
                        handler
                    )
                } catch (e: CameraAccessException) {
                    // Handle camera access exception
                }
            }, 500) // Adjust the delay as needed
        }

        captureSession.stopRepeating()
        captureSession.capture(
            captureRequestBuilder.build(),
            captureCallback,
            handler
        )
    }

    companion object {
        private const val CAMERA_PERMISSION_CODE = 1
    }
}
