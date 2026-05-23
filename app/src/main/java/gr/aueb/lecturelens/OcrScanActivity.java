package gr.aueb.lecturelens;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OcrScanActivity extends AppCompatActivity {

    private static final int CAMERA_PERM_CODE = 101;
    private PreviewView viewFinder;
    private ExecutorService cameraExecutor; // Simplified type reference
    private TextRecognizer textRecognizer;
    private boolean isIdDetected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocr_scan);

        viewFinder = findViewById(R.id.viewFinder);
        cameraExecutor = Executors.newSingleThreadExecutor();

        // Initialize the local text processor engine
        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCameraPipeline();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERM_CODE);
        }
    }
    @androidx.annotation.OptIn(markerClass = androidx.camera.core.ExperimentalGetImage.class)
    private void startCameraPipeline() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                // 1. Setup Live View Preview Screen
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(viewFinder.getSurfaceProvider());

                // 2. Setup Real-time Frame Processing Analyzer
                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                // Explicitly Opt-In to access experimental Proxy Graphics buffers safely
                        imageAnalysis.setAnalyzer(cameraExecutor, imageProxy -> {
                    android.media.Image mediaImage = imageProxy.getImage();
                    if (mediaImage != null && !isIdDetected) {
                        InputImage image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());

                        textRecognizer.process(image)
                                .addOnSuccessListener(visionText -> {
                                    parseExtractedText(visionText.getText());
                                })
                                .addOnCompleteListener(task -> imageProxy.close());
                    } else {
                        imageProxy.close();
                    }
                });

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);

            } catch (Exception e) {
                Log.e("OcrScanActivity", "Camera initialization failed", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void parseExtractedText(String rawText) {
        if (rawText == null || rawText.isEmpty()) return;

        // Clean up accents and force uppercase
        String cleanText = rawText.toUpperCase()
                .replaceAll("[ΌΟ]", "O")
                .replaceAll("[ΆΑ]", "A")
                .replaceAll("[ΈΕ]", "E");

        // Look for common identity keywords printed on all Greek student passes
        if (cleanText.contains("ΔΗΜΑΙΚΗ") || cleanText.contains("ΤΑΥΤΟΤΗΤΑ") || cleanText.contains("ΙΔΡΥΜΑ")) {

            // Regex seeking isolated 6-7 digits representing standard A.M. records
            Pattern amPattern = Pattern.compile("\\b\\d{6,7}\\b");
            Matcher matcher = amPattern.matcher(cleanText);

            if (matcher.find()) {
                isIdDetected = true; // Freeze frame collection loops
                String studentAM = matcher.group();

                runOnUiThread(() -> {
                    Toast.makeText(this, "ID Verified! A.M.: " + studentAM, Toast.LENGTH_LONG).show();

                    // Return the data directly back to your Registration page field inputs
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("EXTRACTED_AM", studentAM);
                    setResult(RESULT_OK, returnIntent);
                    finish();
                });
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERM_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCameraPipeline();
        } else {
            Toast.makeText(this, "Camera permission required to scan student identity.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }
}