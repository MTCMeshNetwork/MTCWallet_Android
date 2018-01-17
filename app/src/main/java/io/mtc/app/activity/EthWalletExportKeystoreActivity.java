package io.mtc.app.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import io.mtc.app.R;
import io.mtc.app.utils.qr.AddressEncoder;
import io.mtc.app.utils.qr.Contents;
import io.mtc.app.utils.qr.QREncoder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;

import org.web3j.crypto.WalletFile;
import org.web3j.protocol.ObjectMapperFactory;

import java.io.File;
import java.io.IOException;

public class EthWalletExportKeystoreActivity extends AppCompatActivity {

    public final static String KEY_WALLET_ADDRESS = "key_wallet_address";

    private EditText edit_keystore;
    private ImageView iv_qrcode;
    private String address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eth_wallet_export_keystore);

        edit_keystore = (EditText) findViewById(R.id.edit_keystore);

        address = getIntent().getStringExtra(KEY_WALLET_ADDRESS);
        if (TextUtils.isEmpty(address)) {
            Toast.makeText(getApplication(), "请传入钱包地址!", Toast.LENGTH_SHORT).show();
            finish();
        }

        if (address.startsWith("0x")) {
            address = address.substring(2, address.length());
        }

        File srcFile = new File(getFilesDir(), address);
        ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
        try {
            WalletFile walletFile = (WalletFile)objectMapper.readValue(srcFile, WalletFile.class);
            edit_keystore.setText(objectMapper.writeValueAsString(walletFile));
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getApplication(), "无法读取钱包数据!", Toast.LENGTH_SHORT).show();
            finish();
        }

        findViewById(R.id.iv_copy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("", edit_keystore.getText().toString());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(EthWalletExportKeystoreActivity.this, "复制KEYSTORE到剪贴板！", Toast.LENGTH_SHORT).show();
            }
        });

        iv_qrcode = (ImageView) findViewById(R.id.iv_qrcode);
        final float scale = getResources().getDisplayMetrics().density;
        int qrCodeDimention = (int) (310 * scale + 0.5f);

        QREncoder qrCodeEncoder = new QREncoder(AddressEncoder.encodeERC(new AddressEncoder(edit_keystore.getText().toString())), null,
                Contents.Type.TEXT, BarcodeFormat.QR_CODE.toString(), qrCodeDimention);

        try {
            Bitmap bitmap = qrCodeEncoder.encodeAsBitmap();
            iv_qrcode.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }

        findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_TEXT, edit_keystore.getText().toString());
                startActivity(Intent.createChooser(i, "Share via"));
            }
        });

    }

    public static void startActivity(Context context, String address) {
        Intent intent = new Intent(context, EthWalletExportKeystoreActivity.class);
        intent.putExtra(KEY_WALLET_ADDRESS, address);
        context.startActivity(intent);
    }

}
