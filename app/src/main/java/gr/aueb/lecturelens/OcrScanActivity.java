package gr.aueb.lecturelens;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
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
    private ExecutorService cameraExecutor;
    private TextRecognizer textRecognizer;
    private boolean isIdDetected = false;

    private String extractedName = "";
    private String extractedAM = "";

    private ImageCapture imageCapture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocr_scan);

        viewFinder = findViewById(R.id.viewFinder);
        cameraExecutor = Executors.newSingleThreadExecutor();

        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        findViewById(R.id.btnCancelScan).setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });

        View btnCapture = findViewById(R.id.btnCapture);
        btnCapture.setOnClickListener(v -> {
        });

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

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(viewFinder.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);

            } catch (Exception e) {
                Log.e("OcrScanActivity", "Camera initialization failed", e);
            }
        }, ContextCompat.getMainExecutor(this));

        findViewById(R.id.btnCapture).setOnClickListener(v -> takePhotoAndProcess());
    }


    private void takePhotoAndProcess() {
        if (imageCapture == null) return;

        imageCapture.takePicture(ContextCompat.getMainExecutor(this), new ImageCapture.OnImageCapturedCallback() {
            @Override
            public void onCaptureSuccess(@NonNull ImageProxy image) {
                @OptIn(markerClass = androidx.camera.core.ExperimentalGetImage.class)
                android.media.Image mediaImage = image.getImage();

                if (mediaImage != null) {
                    InputImage inputImage = InputImage.fromMediaImage(mediaImage, image.getImageInfo().getRotationDegrees());

                    textRecognizer.process(inputImage)
                            .addOnSuccessListener(visionText -> {
                                parseExtractedText(visionText.getText());
                                image.close();
                            })
                            .addOnFailureListener(e -> {
                                image.close();
                                Toast.makeText(OcrScanActivity.this, "Failed to read text. Try again.", Toast.LENGTH_SHORT).show();
                            });
                } else {
                    image.close();
                }
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Log.e("OcrScanActivity", "Photo capture failed", exception);
            }
        });
    }
    private void parseExtractedText(String rawText) {
        if (rawText == null || rawText.isEmpty()) return;

        String cleanText = rawText.toUpperCase()
                .replaceAll("[ΌΟ]", "O")
                .replaceAll("[ΆΑ]", "A")
                .replaceAll("[ΈΕ]", "E")
                .replaceAll("[ΉΗ]", "H")
                .replaceAll("[ΊΙ]", "I")
                .replaceAll("[ΎΥ]", "Y")
                .replaceAll("[ΏΩ]", "Ω");

        if (extractedName.isEmpty()) {
            if (cleanText.contains("ΑΚΑΔΗΜΑΪΚΗ") || cleanText.contains("ΤΑΥΤΟΤΗΤΑ") || cleanText.contains("ACADEMIC ID")) {

                String[] lines = rawText.split("\n");
                for (int i = 0; i < lines.length; i++) {
                    String lineUpper = lines[i].toUpperCase();

                    if (lineUpper.contains("UNDERGRADUATE") || lineUpper.contains("STUDENT") || lineUpper.contains("ΦΟΙΤΗΤΗΣ")) {
                        if (i >= 1) {
                            String structuralLine = lines[i - 1].trim();

                            if (structuralLine.toUpperCase().matches("^[A-Z\\s]+$")) {
                                extractedName = structuralLine;
                                runOnUiThread(() -> {
                                    TextView tvInstructions = findViewById(R.id.tvInstructions);
                                    if (tvInstructions != null) {
                                        tvInstructions.setText("Front Scanned! Flip card vertically to scan the Back.");
                                    }
                                    Toast.makeText(this, "English Name Verified: " + extractedName, Toast.LENGTH_SHORT).show();
                                });
                                break;
                            }
                        }
                    }
                }
            }

            if (extractedName.isEmpty()) {
                runOnUiThread(() -> Toast.makeText(this, "English Name not found. Retake front photo.", Toast.LENGTH_SHORT).show());
            }
            return;
        }

        if (extractedAM.isEmpty()) {
            boolean containsBackIdentifiers = cleanText.contains("A.M.") ||
                    cleanText.contains("Α.Μ.") ||
                    cleanText.contains("ΕΓΓΡΑΦΗΣ") ||
                    cleanText.contains("ΣΧΟΛΗΣ") ||
                    cleanText.contains("/");

            if (containsBackIdentifiers) {
                Pattern amPattern = Pattern.compile("/\\s*(\\d{6,8})");
                Matcher matcher = amPattern.matcher(cleanText);

                if (matcher.find()) {
                    extractedAM = matcher.group(1);
                    isIdDetected = true;

                    runOnUiThread(() -> {
                        Toast.makeText(this, "A.M. Extracted: " + extractedAM, Toast.LENGTH_LONG).show();

                        Intent returnIntent = new Intent();
                        returnIntent.putExtra("EXTRACTED_NAME", extractedName);
                        returnIntent.putExtra("EXTRACTED_AM", extractedAM);
                        setResult(RESULT_OK, returnIntent);
                        finish();
                    });
                    return;
                }
            }

            runOnUiThread(() -> Toast.makeText(this, "A.M. not found. Clear shadows and retake back photo.", Toast.LENGTH_SHORT).show());
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