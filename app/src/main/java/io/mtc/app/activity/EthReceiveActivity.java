package io.mtc.app.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import io.mtc.app.R;
import io.mtc.app.utils.qr.AddressEncoder;
import io.mtc.app.utils.qr.Contents;
import io.mtc.app.utils.qr.QREncoder;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;

public class EthReceiveActivity extends AppCompatActivity {

    public final static String ADDRESS = "ADDRESS";

    private String ethaddress = "";

    public static void startActivity(Context context, String wallet_address) {
        Intent intent = new Intent(context, EthReceiveActivity.class);
        intent.putExtra(ADDRESS, wallet_address);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eth_receive);

        ethaddress = getIntent().getStringExtra(ADDRESS);

        TextView tv_address = (TextView) findViewById(R.id.tv_address);
        tv_address.setText(ethaddress);

        Button clipboard = (Button) findViewById(R.id.copytoclip);
        clipboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_TEXT, ethaddress);
                startActivity(Intent.createChooser(i, "Share via"));
            }
        });

        final float scale = getResources().getDisplayMetrics().density;
        int qrCodeDimention = (int) (310 * scale + 0.5f);

        ImageView qrcode = (ImageView) findViewById(R.id.qrcode);

        qrcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("", ethaddress);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(EthReceiveActivity.this, "复制钱包地址到剪贴板！", Toast.LENGTH_SHORT).show();
            }
        });

        /*
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        QREncoder qrCodeEncoder = new QREncoder(prefs.getBoolean("qr_encoding_erc", true) ? AddressEncoder.encodeERC(new AddressEncoder(ethaddress)) : ethaddress, null,
                Contents.Type.TEXT, BarcodeFormat.QR_CODE.toString(), qrCodeDimention);
        */

        QREncoder qrCodeEncoder = new QREncoder(AddressEncoder.encodeERC(new AddressEncoder(ethaddress)), null,
                Contents.Type.TEXT, BarcodeFormat.QR_CODE.toString(), qrCodeDimention);

        try {
            Bitmap bitmap = qrCodeEncoder.encodeAsBitmap();
            qrcode.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }
}
