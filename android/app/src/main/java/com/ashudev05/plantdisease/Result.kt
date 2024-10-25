package com.ashudev05.plantdisease

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ashudev05.plantdisease.ui.theme.PlantDiseaseTheme

@Composable
fun ResultScreen(
    predictionResult: String,
    onBack: () -> Unit
) {
    val resultData = predictionResult.split("\n").map { it.split(": ") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Prediction Result",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Attribute",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Start
                    )
                    Text(
                        text = "Value",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.End
                    )
                }
                HorizontalDivider(thickness = 1.dp, color = Color.Gray)
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    items(resultData) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = item[0],
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Start
                            )
                            Text(
                                text = item[1],
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.End
                            )
                        }
                        HorizontalDivider(thickness = 0.5.dp, color = Color.Gray)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onBack,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Back to Home")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ResultScreenPreview() {
    PlantDiseaseTheme {
        ResultScreen(predictionResult = "Prediction Result: Tomato Early Blight\nConfidence: 95.00", onBack = {})
    }
}