package com.example.myapplication;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.*;
import androidx.camera.core.*;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.*;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Fragment for scanning QR codes using the device camera.
 *
 * <p>The QRScannerFragment uses CameraX and ML Kit's barcode scanner to capture QR codes. The
 * scanned QR codes are processed, and if they contain a valid event URL, the user is directed
 * to the EventDetailsActivity with the event ID from the QR code.</p>
 */
public class QRScannerFragment extends Fragment {

    private static final String TAG = "QRScannerFragment";
    private PreviewView previewView;
    private ImageButton flashToggleButton;
    private boolean isFlashOn = false;

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private ExecutorService cameraExecutor;
    private BarcodeScanner barcodeScanner;
    private Camera camera;

    /**
     * Inflates the fragment layout and initializes the camera and barcode scanner.
     *
     * @param inflater The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container The parent view that this fragment's UI should be attached to.
     * @param savedInstanceState The fragment's previously saved state, if any.
     * @return The View for the fragment's UI.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_qr_scanner, container, false);

        // UI initialization
        previewView = view.findViewById(R.id.previewView);
        flashToggleButton = view.findViewById(R.id.flash_toggle_button);

        cameraExecutor = Executors.newSingleThreadExecutor();

        // Configure ML Kit's Barcode Scanner for QR codes
        BarcodeScannerOptions options =
                new BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                        .build();
        barcodeScanner = BarcodeScanning.getClient(options);

        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());

        // Start the camera and bind use cases
        startCamera();

        // Set up flash toggle button
        flashToggleButton.setOnClickListener(v -> toggleFlash());

        return view;
    }

    /**
     * Starts the camera and binds the use cases for preview and analysis.
     */
    private void startCamera() {
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Error starting camera", e);
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    /**
     * Binds the preview and analysis use cases to the camera lifecycle.
     *
     * @param cameraProvider The ProcessCameraProvider managing camera resources.
     */
    private void bindCameraUseCases(ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        imageAnalysis.setAnalyzer(cameraExecutor, this::processImageProxy);

        CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
        cameraProvider.unbindAll();

        try {
            camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalysis);
        } catch (Exception e) {
            Log.w(TAG, "Back camera not available, trying front camera.", e);
            try {
                cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;
                camera = cameraProvider.bindToLifecycle(
                        this, cameraSelector, preview, imageAnalysis);
            } catch (Exception ex) {
                Log.e(TAG, "No available camera found.", ex);
                Toast.makeText(getContext(), "No available camera found on this device.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Processes each frame of the camera feed to scan for QR codes.
     *
     * @param imageProxy The image frame from the camera feed.
     */
    @OptIn(markerClass = ExperimentalGetImage.class)
    private void processImageProxy(ImageProxy imageProxy) {
        Image mediaImage = imageProxy.getImage();
        if (mediaImage != null) {
            InputImage image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());

            barcodeScanner.process(image)
                    .addOnSuccessListener(barcodes -> {
                        for (Barcode barcode : barcodes) {
                            String rawValue = barcode.getRawValue();
                            if (rawValue != null) {
                                Log.d(TAG, "Scanned QR Code: " + rawValue);
                                handleScannedData(rawValue);
                                imageProxy.close();
                                return;
                            }
                        }
                        imageProxy.close();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Barcode scanning failed", e);
                        imageProxy.close();
                    });
        }
    }

    /**
     * Handles the scanned QR code data, opening the EventDetailsActivity if valid.
     *
     * @param data The raw data from the QR code.
     */
    private void handleScannedData(String data) {
        // Assuming the data is in the format: eventapp://event/<eventId>
        if (data.startsWith("eventapp://event/")) {
            String eventId = data.substring(data.lastIndexOf('/') + 1);

            Intent intent = new Intent(getActivity(), EventDetailsActivity.class);
            intent.putExtra("eventId", eventId);
            startActivity(intent);
        } else {
            Toast.makeText(getContext(), "Invalid QR code", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Toggles the camera's flashlight if available.
     */
    private void toggleFlash() {
        if (camera == null) {
            Toast.makeText(getContext(), "Camera not initialized", Toast.LENGTH_SHORT).show();
            return;
        }

        if (camera.getCameraInfo().hasFlashUnit()) {
            camera.getCameraControl().enableTorch(!isFlashOn);
            isFlashOn = !isFlashOn;

            if (isFlashOn) {
                flashToggleButton.setImageResource(R.drawable.ic_flash_on);
            } else {
                flashToggleButton.setImageResource(R.drawable.ic_flash_off);
            }
        } else {
            Toast.makeText(getContext(), "Flash not available on this device", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Releases resources used by the camera and barcode scanner when the fragment is destroyed.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
        barcodeScanner.close();
    }
}
