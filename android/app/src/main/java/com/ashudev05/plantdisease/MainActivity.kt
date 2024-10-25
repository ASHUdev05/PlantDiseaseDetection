package com.ashudev05.plantdisease

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ashudev05.plantdisease.ui.theme.PlantDiseaseTheme
import java.io.IOException

class MainActivity : ComponentActivity() {
    private lateinit var plantClassifier: PlantClassifier
    private var isClassifierInitialized by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        plantClassifier = PlantClassifier(this)

        // Initialize classifier asynchronously
        plantClassifier.initialize().addOnSuccessListener {
            isClassifierInitialized = true
            Log.d("MainActivity", "PlantClassifier initialized")
        }.addOnFailureListener { exception ->
            Log.e("MainActivity", "Error initializing PlantClassifier", exception)
        }

        setContent {
            PlantDiseaseTheme {
                var predictionResult by remember { mutableStateOf<String?>(null) }
                var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
                var isLoading by remember { mutableStateOf(false) }
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    if (predictionResult == null) {
                        HomeScreen(
                            modifier = Modifier.padding(innerPadding),
                            selectedImageUri = selectedImageUri,
                            onImagePicked = { uri ->
                                selectedImageUri = uri
                            },
                            onPredictClicked = {
                                if (isClassifierInitialized && selectedImageUri != null) {
                                    isLoading = true
                                    classifyImage(selectedImageUri!!) { result ->
                                        predictionResult = result
                                        isLoading = false
                                    }
                                } else {
                                    Log.e("MainActivity", "Classifier not initialized or no image selected")
                                }
                            },
                            isLoading = isLoading
                        )
                    } else {
                        ResultScreen(
                            predictionResult = predictionResult!!,
                            onBack = { predictionResult = null }
                        )
                    }
                }
            }
        }
    }

    private fun classifyImage(uri: Uri, onResult: (String) -> Unit) {
        // Load the image and classify asynchronously
        try {
            val inputStream = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (bitmap != null) {
                plantClassifier.classifyAsync(bitmap).addOnSuccessListener { result ->
                    onResult(result)
                }.addOnFailureListener { e ->
                    Log.e("MainActivity", "Error classifying image", e)
                }
            } else {
                Log.e("MainActivity", "Failed to decode bitmap from URI")
            }
        } catch (e: IOException) {
            Log.e("MainActivity", "Error loading image", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        plantClassifier.close() // Close the classifier when the activity is destroyed
    }
}

@Composable
fun HomeScreen(
    modifier: Modifier,
    selectedImageUri: Uri?,
    onImagePicked: (Uri) -> Unit,
    onPredictClicked: () -> Unit,
    isLoading: Boolean
) {
    val pickMedia = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            Log.d("PhotoPicker", "Selected URI: $uri")
            onImagePicked(uri)
        } else {
            Log.d("PhotoPicker", "No media selected")
        }
    }

    val context = LocalContext.current

    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Header
        Text(
            text = "Plant Disease Classifier",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp)
        )

        // Card for image selection
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (selectedImageUri != null) {
                    val bitmap = BitmapFactory.decodeStream(
                        context.contentResolver.openInputStream(selectedImageUri)
                    )
                    Image(
                        bitmap = bitmap!!.asImageBitmap(),
                        contentDescription = "Selected Image",
                        modifier = Modifier
                            .size(256.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.ic_camera), // Add a camera icon in drawable
                        contentDescription = "Select Image",
                        modifier = Modifier
                            .size(64.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Button(
                        onClick = {
                            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        modifier = Modifier.padding(8.dp),
                        elevation = ButtonDefaults.buttonElevation(8.dp)
                    ) {
                        Text("Pick an image")
                    }
                    Button(
                        onClick = onPredictClicked,
                        modifier = Modifier.padding(8.dp),
                        elevation = ButtonDefaults.buttonElevation(8.dp)
                    ) {
                        Text("Predict")
                    }
                }
                if (isLoading) {
                    Spacer(modifier = Modifier.height(16.dp))
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    PlantDiseaseTheme {
        HomeScreen(
            modifier = Modifier.fillMaxSize(),
            selectedImageUri = null,
            onImagePicked = {},
            onPredictClicked = {},
            isLoading = false
        )
    }
}