package com.example.gestureinteraction;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gestureinteraction.databinding.ActivityInputPanelBinding;

public class InputPanel extends GestureActivity {
    private ActivityInputPanelBinding binding;
    private EditText mEditTextInput;
    private ImageButton mImageButtonReturn;
    private TextView mTextViewHeader;
    private ClipboardManager mClipboard;
    private String inputContent = "";
    private String hint;
    private View v;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityInputPanelBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        v = this.findViewById(android.R.id.content);
        mEditTextInput = binding.editTextInput;
        mImageButtonReturn = binding.imageButtonReturn;
        mTextViewHeader = binding.textViewHeader;

        mClipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

        Bundle extras = getIntent().getExtras();
        String inputType = null;
        if (extras != null) {
            inputContent = extras.getString("text_input");
            inputType = extras.getString("type_input");
            mTextViewHeader.setText(inputType);
            mEditTextInput.setText(inputContent);
        }
        mImageButtonReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.putExtra("text_input", inputContent);
                intent.putExtra("action", "Submit");
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        if (inputType.equals("Search")){
            setPopupWindow(v, inputType, R.drawable.ic_baseline_search);
            hint = "Input keyword with gesture...";
        } else if (inputType.equals("Input")){
            setPopupWindow(v, inputType, R.drawable.ic_baseline_keyboard);
            hint = "Input message with gesture...";
        } else if (inputType.equals("Add Chat")){
            hint = "Input contact name with gesture...";
            setPopupWindow(v, inputType, R.drawable.ic_baseline_add);
        }
        mEditTextInput.setHint(hint);
        //inputContent = "B";
        updateUI("Null");
    }

    @Override
    public void updateUI(String result) {
        switch (result){
            case "Draw Letter A":
                inputContent += "A";
                break;
            case "Draw Letter B":
                inputContent += "B";
                break;
            case "Draw Letter C":
                inputContent += "C";
                break;
            case "Draw Letter D":
                inputContent += "D";
                break;
            case "Draw Letter E":
                inputContent += "E";
                break;
            case "Finger Rubbing":
                inputContent = (inputContent == "") ? "" : (inputContent.substring(0, inputContent.length() - 1));
                break;
            case "Hand Sweeping":
                Toast.makeText(getApplicationContext(), "Clear", Toast.LENGTH_SHORT).show();
                inputContent = "";
                break;
            case "Hand Squeezing":
                Toast.makeText(getApplicationContext(), "Copy", Toast.LENGTH_SHORT).show();
                ClipData clipData = ClipData.newPlainText("message", inputContent);
                mClipboard.setPrimaryClip(clipData);
                break;
            case "Finger Pinching":
                Toast.makeText(getApplicationContext(), "Paste", Toast.LENGTH_SHORT).show();
                String pasteData = "";
                if (mClipboard.hasPrimaryClip()) {
                    ClipData.Item item = mClipboard.getPrimaryClip().getItemAt(0);
                    pasteData = item.getText().toString();
                }
                if(pasteData != ""){
                    inputContent += " " + pasteData;
                }
                break;
            case "Finger Snapping":
                mImageButtonReturn.callOnClick();
                break;
            case "Hand Rotation":
                Intent intent = new Intent();
                intent.putExtra("text_input", inputContent);
                intent.putExtra("action", "None");
                setResult(RESULT_OK, intent);
                finish();
                break;
        }
        if(inputContent != ""){
            mEditTextInput.setText("");
            mEditTextInput.append(inputContent);
        } else {
            mEditTextInput.setText("");
            mEditTextInput.setHint(hint);
        }
    }
}