package io.mtc.app.mtcwallet.dialog;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import io.mtc.app.mtcwallet.R;

public class SimpleConfirmDialogFragment extends DialogFragment implements View.OnClickListener {

    private static final String ARG_TITLE = "arg_title";
    private static final String ARG_MESSAGE = "arg_message";

    private String mTitle;
    private String mMessage;
    private DialogFragmentCallback callback;

    public SimpleConfirmDialogFragment() {

    }

    public static SimpleConfirmDialogFragment newInstance(String title, String message) {
        SimpleConfirmDialogFragment fragment = new SimpleConfirmDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_MESSAGE, message);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mTitle = getArguments().getString(ARG_TITLE);
            mMessage = getArguments().getString(ARG_MESSAGE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_simple_confirm_dialog, container, false);
        TextView tvTitle = (TextView) rootView.findViewById(R.id.tv_title);
        TextView tvMessage = (TextView) rootView.findViewById(R.id.tv_message);
        tvTitle.setText(mTitle);
        tvMessage.setText(mMessage);
        rootView.findViewById(R.id.btn_ok).setOnClickListener(this);
        rootView.findViewById(R.id.btn_cancel).setOnClickListener(this);
        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (getTargetFragment() != null &&  getTargetFragment() instanceof DialogFragmentCallback) {
            callback = (DialogFragmentCallback) getTargetFragment();
        } else if (getActivity() instanceof DialogFragmentCallback) {
            callback = (DialogFragmentCallback) getActivity();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callback = null;
    }

    @Override
    public void onClick(View v) {
        if (callback != null) {
            callback.onDialogFragmentClick(this, v);
        }
        dismiss();
    }
}
