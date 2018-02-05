package io.mtc.app.mtcwallet.activity;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import io.mtc.app.mtcwallet.R;
import io.mtc.app.mtcwallet.database.EthWalletInfo;
import io.mtc.app.mtcwallet.dialog.DialogFragmentCallback;
import io.mtc.app.mtcwallet.dialog.EthWalletExportDialogFragment;
import io.mtc.app.mtcwallet.utils.qr.AddressEncoder;
import io.mtc.app.mtcwallet.utils.qr.Contents;
import io.mtc.app.mtcwallet.utils.qr.QREncoder;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;

public class EthWalletDetailsActivity extends AppCompatActivity implements View.OnClickListener, DialogFragmentCallback {

    public final static String REQUEST_TAG_EXPORT_DIALOG = "request_tag_export_dialog";

    public final static String KEY_WALLET_INFO = "key_wallet_info";

    private Toolbar toolbar;
    private TextView tv_name, tv_address, tv_password_hit;
    private ImageView iv_copy, iv_qrcode;

    private EthWalletInfo ethWalletInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eth_wallet_details);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ethWalletInfo = getIntent().getParcelableExtra(KEY_WALLET_INFO);

        tv_name = (TextView) findViewById(R.id.tv_name);
        tv_address = (TextView) findViewById(R.id.tv_address);
        tv_password_hit = (TextView) findViewById(R.id.tv_password_hit);

        iv_copy = (ImageView) findViewById(R.id.iv_copy);
        iv_qrcode = (ImageView) findViewById(R.id.iv_qrcode);

        tv_name.setText(ethWalletInfo.name);
        tv_address.setText("0x" + ethWalletInfo.address);
        tv_password_hit.setText(ethWalletInfo.password_hint);

        iv_copy.setOnClickListener(this);
        findViewById(R.id.btn_export).setOnClickListener(this);

        final float scale = getResources().getDisplayMetrics().density;
        int qrCodeDimention = (int) (256 * scale + 0.5f);
        QREncoder qrCodeEncoder = new QREncoder(AddressEncoder.encodeERC(new AddressEncoder("0x" + ethWalletInfo.address)), null,
                Contents.Type.TEXT, BarcodeFormat.QR_CODE.toString(), qrCodeDimention);

        try {
            Bitmap bitmap = qrCodeEncoder.encodeAsBitmap();
            iv_qrcode.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case android.R.id.home: {
                finish();
                break;
            }

            case R.id.iv_copy: {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("", ethWalletInfo.address);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(this, "复制钱包地址到剪贴板！", Toast.LENGTH_SHORT).show();
                break;
            }

            case R.id.btn_export: {
                EthWalletExportDialogFragment.newInstance().show(getSupportFragmentManager(), REQUEST_TAG_EXPORT_DIALOG);
                break;
            }

        }
    }

    @Override
    public void onDialogFragmentDismiss(DialogFragment dialogFragment) {

    }

    @Override
    public void onDialogFragmentCancel(DialogFragment dialogFragment) {

    }

    @Override
    public void onDialogFragmentClick(DialogFragment dialogFragment, View view) {
        switch (view.getId()) {
            case R.id.tv_keystore: {
                EthWalletExportKeystoreActivity.startActivity(this, ethWalletInfo.address);
                break;
            }

            case R.id.tv_private_key: {
                EthWalletExportPrivateKeyActivity.startActivity(this, ethWalletInfo.address);
                break;
            }
        }
    }

    @Override
    public void onDialogFragmentResult(DialogFragment dialogFragment, Bundle bundle) {

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

    public static void startActivity(Context context, EthWalletInfo ethWalletInfo) {
        Intent intent = new Intent(context, EthWalletDetailsActivity.class);
        intent.putExtra(KEY_WALLET_INFO, ethWalletInfo);
        context.startActivity(intent);
    }

    public static void startActivityForResult(Activity activity, EthWalletInfo ethWalletInfo, int requestCode) {
        Intent intent = new Intent(activity, EthWalletDetailsActivity.class);
        intent.putExtra(KEY_WALLET_INFO, ethWalletInfo);
        activity.startActivityForResult(intent, requestCode);
    }

}
