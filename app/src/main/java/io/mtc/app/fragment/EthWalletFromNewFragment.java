package io.mtc.app.fragment;


import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import io.mtc.app.R;

public class EthWalletFromNewFragment extends Fragment implements View.OnClickListener {

    private EditText editPassword;
    private EditText editPasswordConfirm;
    private Button btnGenerate;

    public EthWalletFromNewFragment() {

    }

    public static EthWalletFromNewFragment newInstance() {
        EthWalletFromNewFragment fragment = new EthWalletFromNewFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_eth_wallet_gen_new, container, false);
        editPassword = (EditText) rootView.findViewById(R.id.edit_password);
        editPasswordConfirm = (EditText) rootView.findViewById(R.id.edit_password_confirm);
        btnGenerate = (Button) rootView.findViewById(R.id.btn_generate);
        btnGenerate.setOnClickListener(this);
        return rootView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_generate: {
                btnGenerate.setEnabled(false);
                String password = editPassword.getText().toString();
                String passwordConfirm = editPasswordConfirm.getText().toString();
                if (password.length() < 9) {
                    showSnack("密码长度不能小于9个字符!");
                    btnGenerate.setEnabled(true);
                    return;
                }

                if (!password.equals(passwordConfirm)) {
                    showSnack("两次输入密码不同!");
                    btnGenerate.setEnabled(true);
                    return;
                }

            }
        }
    }

    private void showSnack(String s) {
        Snackbar mySnackbar = Snackbar.make(getView(), s, Snackbar.LENGTH_SHORT);
        mySnackbar.show();
    }





}
