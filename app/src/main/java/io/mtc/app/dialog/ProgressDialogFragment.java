package io.mtc.app.dialog;


import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import io.mtc.app.R;

public class ProgressDialogFragment extends DialogFragment {

    public final static String KEY_MESSAGE = "key_message";

    public static ProgressDialogFragment newInstance(String message) {
        Bundle args = new Bundle();
        args.putString(KEY_MESSAGE, message);
        ProgressDialogFragment fragment = new ProgressDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private DialogFragmentCallback callback;

    private TextView tv_msg;

    public ProgressDialogFragment() {

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (getTargetFragment() != null && getTargetFragment() instanceof DialogFragmentCallback) {
            callback = (DialogFragmentCallback)getTargetFragment();
        } else if (getActivity() != null && getActivity() instanceof DialogFragmentCallback) {
            callback = (DialogFragmentCallback)getActivity();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callback = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_progress_dialog, container, false);
        tv_msg = (TextView)rootView.findViewById(R.id.tv_message);

        if (getArguments() != null) {
            tv_msg.setText(getArguments().getString(KEY_MESSAGE));
        }

        return rootView;
    }

    public void setMessage(String msg) {
        Bundle bundle = getArguments();
        if (bundle != null) {
            bundle.putString(KEY_MESSAGE, msg);
        }

        if (this.isResumed()) {
            tv_msg.setText(msg);
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        if (callback != null) {
            callback.onDialogFragmentCancel(this);
        }
    }


}
