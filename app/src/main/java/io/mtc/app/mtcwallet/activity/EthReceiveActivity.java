package io.mtc.app.mtcwallet.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import io.mtc.app.mtcwallet.R;
import io.mtc.app.mtcwallet.database.EthWalletInfo;
import io.mtc.app.mtcwallet.database.EthWalletInfoDB;
import io.mtc.app.mtcwallet.utils.MTCWalletUtils;
import io.mtc.app.mtcwallet.utils.qr.AddressEncoder;
import io.mtc.app.mtcwallet.utils.qr.Contents;
import io.mtc.app.mtcwallet.utils.qr.QREncoder;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;

public class EthReceiveActivity extends AppCompatActivity implements View.OnClickListener {

    public final static String ADDRESS = "ADDRESS";

    private Toolbar toolbar;
    private String ethaddress = "";
    private String ethaddress_prefix = "";

    public static void startActivity(Context context, String wallet_address) {
        Intent intent = new Intent(context, EthReceiveActivity.class);
        intent.putExtra(ADDRESS, wallet_address);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eth_receive);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ethaddress = getIntent().getStringExtra(ADDRESS);
        ethaddress_prefix = MTCWalletUtils.getPrefixAddress(ethaddress);

        EthWalletInfoDB ethWalletInfoDB = new EthWalletInfoDB(this);
        EthWalletInfo ethWalletInfo = ethWalletInfoDB.getWalletInfo(ethaddress);
        TextView tv_name = (TextView) findViewById(R.id.tv_wallet_name);
        if (ethWalletInfo != null) {
            tv_name.setText(ethWalletInfo.name);
        } else {
            tv_name.setText("");
        }

        TextView tv_address = (TextView) findViewById(R.id.tv_address);
        tv_address.setText(ethaddress_prefix);

        Button clipboard = (Button) findViewById(R.id.copytoclip);
        clipboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_TEXT, ethaddress_prefix);
                startActivity(Intent.createChooser(i, "Share via"));
            }
        });

        final float scale = getResources().getDisplayMetrics().density;
        int qrCodeDimention = (int) (256 * scale + 0.5f);

        findViewById(R.id.iv_address_copy).setOnClickListener(this);

        ImageView qrcode = (ImageView) findViewById(R.id.qrcode);

        qrcode.setOnClickListener(this);

        /*
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        QREncoder qrCodeEncoder = new QREncoder(prefs.getBoolean("qr_encoding_erc", true) ? AddressEncoder.encodeERC(new AddressEncoder(ethaddress)) : ethaddress, null,
                Contents.Type.TEXT, BarcodeFormat.QR_CODE.toString(), qrCodeDimention);
        */

        QREncoder qrCodeEncoder = new QREncoder(AddressEncoder.encodeERC(new AddressEncoder(ethaddress_prefix)), null,
                Contents.Type.TEXT, BarcodeFormat.QR_CODE.toString(), qrCodeDimention);

        try {
            Bitmap bitmap = qrCodeEncoder.encodeAsBitmap();
            qrcode.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_address_copy:
            case R.id.qrcode: {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("", ethaddress_prefix);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(EthReceiveActivity.this, "复制钱包地址到剪贴板！", Toast.LENGTH_SHORT).show();
                break;
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
