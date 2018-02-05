package io.mtc.app.mtcwallet.test;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import io.mtc.app.mtcwallet.R;

public class TestMainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_main);
        findViewById(R.id.btn_get_balance).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_get_balance: {

                break;
            }
        }
    }
}
