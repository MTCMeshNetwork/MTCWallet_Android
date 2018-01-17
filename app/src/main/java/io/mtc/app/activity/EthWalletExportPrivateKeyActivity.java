package io.mtc.app.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import io.mtc.app.R;
import io.mtc.app.dialog.DialogFragmentCallback;
import io.mtc.app.dialog.ProgressDialogFragment;
import io.mtc.app.dialog.WalletOperConfirmDialogFragment;
import io.mtc.app.utils.qr.AddressEncoder;
import io.mtc.app.utils.qr.Contents;
import io.mtc.app.utils.qr.QREncoder;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;

import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.WalletUtils;
import org.web3j.utils.Numeric;

import java.io.File;
import java.io.IOException;

public class EthWalletExportPrivateKeyActivity extends AppCompatActivity implements DialogFragmentCallback {

    public final static String TAG_OPERATION_CONFIRM_DIALOG = "tag_operation_confirm_dialog";
    public final static String TAG_PROGRESS_DIALOG = "tag_progress_dialog";

    public final static String KEY_WALLET_ADDRESS = "key_wallet_address";

    private EditText edit_private_key;
    private String address;
    private ImageView iv_qrcode;

    private final Handler backgroundHandler;
    private final HandlerThread backgroundHandlerThread;

    public EthWalletExportPrivateKeyActivity() {
        backgroundHandlerThread = new HandlerThread("EthWalletExportPrivateKeyActivityHandlerThread", Process.THREAD_PRIORITY_FOREGROUND);
        backgroundHandlerThread.start();
        backgroundHandler = new Handler(backgroundHandlerThread.getLooper());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eth_wallet_export_private_key);

        edit_private_key = (EditText) findViewById(R.id.edit_private_key);
        iv_qrcode = (ImageView) findViewById(R.id.iv_qrcode);

        address = getIntent().getStringExtra(KEY_WALLET_ADDRESS);
        if (TextUtils.isEmpty(address)) {
            Toast.makeText(getApplication(), "请传入钱包地址!", Toast.LENGTH_SHORT).show();
            finish();
        }

        if (address.startsWith("0x")) {
            address = address.substring(2, address.length());
        }

        findViewById(R.id.iv_copy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("", edit_private_key.getText().toString());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(EthWalletExportPrivateKeyActivity.this, "复制私钥到剪贴板！", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_TEXT, edit_private_key.getText().toString());
                startActivity(Intent.createChooser(i, "Share via"));
            }
        });

        WalletOperConfirmDialogFragment.newInstance().show(getSupportFragmentManager(), TAG_OPERATION_CONFIRM_DIALOG);
    }

    private void decode(final String address, final String password) {
        final File srcDir = getFilesDir();
        backgroundHandler.post(new Runnable() {
            @Override
            public void run() {
                File srcFile = new File(srcDir, address);
                //ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
                try {
                    Credentials credentials = WalletUtils.loadCredentials(password, srcFile);
                    onDecodeSuccess(credentials);
                    //WalletFile walletFile = (WalletFile)objectMapper.readValue(srcFile, WalletFile.class);
                } catch (IOException e) {
                    e.printStackTrace();
                    onDecodeError("读取钱包数据失败!");
                } catch (CipherException e) {
                    e.printStackTrace();
                    onDecodeError("解码钱包私钥数据失败!");
                }
            }
        });
    }

    private void onDecodeError(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(EthWalletExportPrivateKeyActivity.this, message, Toast.LENGTH_SHORT).show();
                closeProgressDialog();
                finish();
            }
        });
    }

    private void onDecodeSuccess(final Credentials credentials) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(EthWalletExportPrivateKeyActivity.this, "获取私钥成功!", Toast.LENGTH_SHORT).show();
                closeProgressDialog();
                initPrivateKeyView(credentials);
            }
        });
    }

    private void showProgressDialog() {
        closeProgressDialog();
        ProgressDialogFragment progressDialogFragment = ProgressDialogFragment.newInstance("正在解码私钥,请等待...");
        progressDialogFragment.setCancelable(false);
        progressDialogFragment.show(getSupportFragmentManager(), TAG_PROGRESS_DIALOG);
    }

    private void closeProgressDialog() {
        ProgressDialogFragment progressDialogFragment = (ProgressDialogFragment)getSupportFragmentManager().findFragmentByTag(TAG_PROGRESS_DIALOG);
        if (progressDialogFragment != null)
            progressDialogFragment.dismiss();
    }

    private void initPrivateKeyView(Credentials credentials) {

        ECKeyPair ecKeyPair = credentials.getEcKeyPair();
        System.out.println(Numeric.toHexStringWithPrefixZeroPadded(ecKeyPair.getPrivateKey(), 64));
        System.out.println(Numeric.toHexStringWithPrefixZeroPadded(ecKeyPair.getPublicKey(), 128));

        edit_private_key.setText(Numeric.toHexStringWithPrefixZeroPadded(ecKeyPair.getPrivateKey(), 64));

        final float scale = getResources().getDisplayMetrics().density;
        int qrCodeDimention = (int) (310 * scale + 0.5f);

        QREncoder qrCodeEncoder = new QREncoder(AddressEncoder.encodeERC(new AddressEncoder(edit_private_key.getText().toString())), null,
                Contents.Type.TEXT, BarcodeFormat.QR_CODE.toString(), qrCodeDimention);

        try {
            Bitmap bitmap = qrCodeEncoder.encodeAsBitmap();
            iv_qrcode.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    public static void startActivity(Context context, String address) {
        Intent intent = new Intent(context, EthWalletExportPrivateKeyActivity.class);
        intent.putExtra(KEY_WALLET_ADDRESS, address);
        context.startActivity(intent);
    }


    @Override
    public void onDialogFragmentDismiss(DialogFragment dialogFragment) {

    }

    @Override
    public void onDialogFragmentCancel(DialogFragment dialogFragment) {
        finish();
    }

    @Override
    public void onDialogFragmentClick(DialogFragment dialogFragment, View view) {

    }

    @Override
    public void onDialogFragmentResult(DialogFragment dialogFragment, Bundle bundle) {
        if (TAG_OPERATION_CONFIRM_DIALOG.equals(dialogFragment.getTag())) {
            String password = bundle.getString(WalletOperConfirmDialogFragment.KEY_PASSWORD);
            showProgressDialog();
            decode(address, password);
        }
    }
}
