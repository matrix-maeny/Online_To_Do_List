package com.matrix_maeny.onlinetodolist;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.appcompat.widget.AppCompatButton;

import com.matrix_maeny.onlinetodolist.databinding.WorkDialogBinding;

public class WorkDialog extends AppCompatDialogFragment {

    private WorkDialogBinding binding;
    private String workName = null;
    private WorkDialogListener listener;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        ContextThemeWrapper wrapper = new ContextThemeWrapper(requireContext(), androidx.appcompat.R.style.Theme_AppCompat_Dialog_Alert);
        AlertDialog.Builder builder = new AlertDialog.Builder(wrapper);

        binding = WorkDialogBinding.bind(requireActivity().getLayoutInflater().inflate(R.layout.work_dialog, null));
        builder.setView(binding.getRoot());

        listener = (WorkDialogListener) requireContext();

        binding.dlgSaveBtn.setOnClickListener(v -> {
            if (checkWork()) {
                // save work
                listener.saveWork(workName);
                dismiss();
            }
        });


        return builder.create();
    }

    private boolean checkWork(){
        try {
            workName = binding.dlgTaskNameEt.getText().toString().trim();
            if(!workName.equals("")) return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        Toast.makeText(requireContext(), "Please enter task", Toast.LENGTH_SHORT).show();
        return false;
    }

    public interface WorkDialogListener{
        void saveWork(String workName);
    }
}
