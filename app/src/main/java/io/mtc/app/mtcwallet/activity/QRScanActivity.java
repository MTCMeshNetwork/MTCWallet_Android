package io.mtc.app.mtcwallet.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;

import java.util.ArrayList;

import io.mtc.app.mtcwallet.R;
import me.dm7.barcodescanner.zxing.ZXingScannerView;


public class QRScanActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    public static final int REQUEST_CODE = 100;
    public static final int REQUEST_CAMERA_PERMISSION = 106;

    private ZXingScannerView mScannerView;
    private FrameLayout barCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrscan);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextView title = (TextView) findViewById(R.id.toolbar_title);

        barCode = (FrameLayout) findViewById(R.id.barcode);
        // BarcodeCapture barcodeCapture = (BarcodeCapture) getSupportFragmentManager().findFragmentById(R.id.barcode);
        // barcodeCapture.setRetrieval(this);

        if (hasPermission(this))
            initQRScan(barCode);
        else
            askForPermissionRead(this);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public void initQRScan(FrameLayout frame) {
        mScannerView = new ZXingScannerView(this);
        frame.addView(mScannerView);
        mScannerView.setResultHandler(this);
        ArrayList<BarcodeFormat> supported = new ArrayList<BarcodeFormat>();
        supported.add(BarcodeFormat.QR_CODE);
        mScannerView.setFormats(supported);
        mScannerView.startCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mScannerView != null)
            mScannerView.stopCamera();
    }

    public boolean hasPermission(Context c) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (c.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                return true;
            }
        } else {
            return true;
        }
        return false;
    }

    public static void askForPermissionRead(Activity c) {
        if (Build.VERSION.SDK_INT < 23) return;
        ActivityCompat.requestPermissions(c, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initQRScan(barCode);
                } else {
                    Toast.makeText(this, "Please grant camera permission in order to read QR codes", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    @Override
    public void handleResult(Result result) {
        if (result == null) return;
        String address = result.getText();
        Intent data = new Intent();
        data.putExtra("ADDRESS", address);
        setResult(RESULT_OK, data);
        finish();
    }

}