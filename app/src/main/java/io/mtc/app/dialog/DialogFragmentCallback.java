package io.mtc.app.dialog;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;

/**
 * Created by admin on 2017/8/2.
 */

public interface DialogFragmentCallback {
    void onDialogFragmentDismiss(DialogFragment dialogFragment);
    void onDialogFragmentCancel(DialogFragment dialogFragment);
    void onDialogFragmentClick(DialogFragment dialogFragment, View view);
    void onDialogFragmentResult(DialogFragment dialogFragment, Bundle bundle);
}
