package com.mobiversa.ezy2pay.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.SharedPreferences;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputConnection;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import com.mobiversa.ezy2pay.R;
import com.mobiversa.ezy2pay.network.ApiService;
import com.mobiversa.ezy2pay.network.response.EzyMotoRecPayment;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyInputMethodService extends InputMethodService
        implements KeyboardView.OnKeyboardActionListener {

    RelativeLayout invoice_rel, amount_rel;
    AppCompatTextView amount_edt, text_home;
    AppCompatEditText invoice_home;
    AppCompatImageView send_img;
    AppCompatImageView copy_img;
    Boolean isAmount = true;
    Boolean isCopied = false;
    Boolean isSended = false;
    Boolean isCaps = false;

    public int counter;
    public int cpyCounter;
    Keyboard qwerty_keyboard;
    Keyboard number_keyboard;
    KeyboardView keyboardView;

    @Override
    public View onCreateInputView() {
        // get the KeyboardView and add our Keyboard layout to it
        View view = getLayoutInflater().inflate(R.layout.keyboard_view, null);
        keyboardView = (KeyboardView) view.findViewById(R.id.number_keyboard_view);

        text_home = (AppCompatTextView) view.findViewById(R.id.text_home);
        amount_edt = (AppCompatTextView) view.findViewById(R.id.edt_amount);
        invoice_home = (AppCompatEditText) view.findViewById(R.id.invoice_home);
        send_img = (AppCompatImageView) view.findViewById(R.id.send_img);
        copy_img = (AppCompatImageView) view.findViewById(R.id.copy_img);
        invoice_rel = (RelativeLayout) view.findViewById(R.id.invoice_rel);
        amount_rel = (RelativeLayout) view.findViewById(R.id.amount_rel);
        qwerty_keyboard = new Keyboard(this, R.xml.qwety_pad);
        number_keyboard = new Keyboard(this, R.xml.number_pad);
        keyboardView.setKeyboard(number_keyboard);
        keyboardView.setOnKeyboardActionListener(this);

        /*text_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isAmount = true;

                amount_rel.setBackground(getResources().getDrawable(R.drawable.rect_bor_key));
                invoice_rel.setBackground(getResources().getDrawable(R.drawable.rect_bor));
            }
        });*/

        amount_edt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isAmount = true;
                keyboardView.setKeyboard(number_keyboard);
                amount_rel.setBackground(getResources().getDrawable(R.drawable.rect_bor_key));
                invoice_rel.setBackground(getResources().getDrawable(R.drawable.rect_bor));
            }
        });


        amount_rel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isAmount = true;
                keyboardView.setKeyboard(number_keyboard);
                amount_rel.setBackground(getResources().getDrawable(R.drawable.rect_bor_key));
                invoice_rel.setBackground(getResources().getDrawable(R.drawable.rect_bor));
            }
        });


        invoice_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isAmount = false;
                keyboardView.setKeyboard(qwerty_keyboard);
                invoice_rel.setBackground(getResources().getDrawable(R.drawable.rect_bor_key));
                amount_rel.setBackground(getResources().getDrawable(R.drawable.rect_bor));
            }
        });

        invoice_rel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isAmount = false;
                keyboardView.setKeyboard(qwerty_keyboard);
                invoice_rel.setBackground(getResources().getDrawable(R.drawable.rect_bor_key));
                amount_rel.setBackground(getResources().getDrawable(R.drawable.rect_bor));
            }
        });

        send_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!isSended) {
                    verifyData(true);
                } else {
                    Toast.makeText(getApplicationContext(), "Link already Copied", Toast.LENGTH_SHORT).show();
                }

            }
        });

        copy_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isCopied) {
                    verifyData(false);
                } else {
                    Toast.makeText(getApplicationContext(), "Link Already Sent", Toast.LENGTH_SHORT).show();
                }
            }
        });
        return view;
    }

    @Override
    public void onPress(int primaryCode) {
    }

    @Override
    public void onRelease(int primaryCode) {
    }

    @Override
    public void onKey(int primaryCode, int[] keyCodes) {
        InputConnection ic = getCurrentInputConnection();
        if (ic == null) return;
        switch (primaryCode) {
            case Keyboard.KEYCODE_DELETE:
                if (isAmount) {
                    String amount = amount_edt.getText().toString().replace(".", "");
                    if (amount.length() == 1) {
                        amount = "0";
                    } else {
                        amount = amount.substring(0, amount.length() - 1);
                    }
                    deleteData(amount);
                } else {
                    String display = invoice_home.getText().toString();

                    if (!TextUtils.isEmpty(display)) {
                        display = display.substring(0, display.length() - 1);

                        invoice_home.setText(display);
                    }
                }
                break;
            case Keyboard.KEYCODE_DONE:
                Log.e("DONE", "Success");
//                ic.commitText(amount_edt.getText().toString(), 1);
                break;
            case Keyboard.KEYCODE_SHIFT :
                isCaps = !isCaps;
                qwerty_keyboard.setShifted(isCaps);
                keyboardView.invalidateAllKeys();
                break;
            default:
                char code = (char) primaryCode;
                Log.e("CODE", String.valueOf(code));
                if (isAmount) {
                    if (isStringInt(String.valueOf(code)))
                    pressData(String.valueOf(code));
                }
                else {
                    String datas = invoice_home.getText().toString();
                    if (isCaps)
                    invoice_home.setText(datas + String.valueOf(code).toUpperCase());
                    else
                    invoice_home.setText(datas + String.valueOf(code));
//                    ic.commitText(String.valueOf(code), 1);
                }
        }
    }

    private void editText(String data, AppCompatEditText edit_txt) {

    }

    @Override
    public void onText(CharSequence text) {

    }

    @Override
    public void swipeLeft() {

    }

    @Override
    public void swipeRight() {

    }

    @Override
    public void swipeDown() {

    }

    @Override
    public void swipeUp() {

    }

    public boolean isStringInt(String s)
    {
        try
        {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException ex)
        {
            return false;
        }
    }

    public void pressData(String pressedVal) {
        String amount = amount_edt.getText().toString().replace(".", "");

        if (amount.length() < 12) {
            amount += pressedVal;
        }

        Long totalAmount = java.lang.Long.parseLong(amount);
        String costDisplay = "0.00";

        if (totalAmount < 10) {
            costDisplay = "0.0" + totalAmount;
        } else if (totalAmount > 10 && totalAmount < 100) {
            costDisplay = "0." + totalAmount;
        } else {
            costDisplay = totalAmount.toString();
            costDisplay = costDisplay.substring(
                    0,
                    costDisplay.length() - 2
            ) + "." + costDisplay.substring(costDisplay.length() - 2);
        }

        amount_edt.setText(costDisplay);
        isCopied = false;
        isSended = false;

    }


    void deleteData(String amount) {
        Long totalAmount = java.lang.Long.parseLong(amount);
        String costDisplay = "0.00";

        if (totalAmount < 10) {
            costDisplay = "0.0" + totalAmount;
        } else if (totalAmount >= 9 && totalAmount < 100) {
            costDisplay = "0." + totalAmount;
        } else {
            costDisplay = totalAmount.toString();
            costDisplay = costDisplay.substring(
                    0,
                    costDisplay.length() - 2
            ) + "." + costDisplay.substring(costDisplay.length() - 2);
        }

        amount_edt.setText(costDisplay);
    }

    public void verifyData(Boolean isSend) {

        Double totalPrice = Double.valueOf(amount_edt.getText().toString());

        if (totalPrice > 0.10) {
            SharedPreferences customPrefs = PreferenceHelper.INSTANCE.customPrefs(getApplicationContext(), "REMEMBER");
            String value_encrypted = Encryptor.Encrypt.encrypt(Constants.mobiApi.substring(0, 16),
                    Constants.mobiApi.substring(16, 32), getAmount(amount_edt.getText().toString()));
            Log.v("--encrypt--", "" + value_encrypted);
            String hex_to_asci_name = Encryptor.Encrypt.encodeHexString(value_encrypted);

            HashMap<String, String> data = new HashMap();
            HashMap<String, String> header = new HashMap();

            if (customPrefs.getString("UserName", "").isEmpty()){
                Toast.makeText(getApplicationContext(), "Please Login into Mobi Application", Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(getApplicationContext(), "Loading", Toast.LENGTH_SHORT).show();

            data.put(Fields.customerName, customPrefs.getString("UserName", ""));
            data.put(Fields.username, customPrefs.getString("UserName", ""));
            data.put(Fields.encAmount, hex_to_asci_name);
            data.put(Fields.InvoiceId, invoice_home.getText().toString());
            data.put(Fields.callback, "");
            data.put(Fields.Service, Fields.NEW_EXTERNAL_TXN_LINK_REQ);

            header.put(Fields.loginId, "");
            header.put(Fields.mobiApiKey, Constants.mobiApi);
            jsonMotoLink(header, data, isSend);
        } else {
            Toast.makeText(getApplicationContext(), "Enter Amount more than 10 cents", Toast.LENGTH_SHORT).show();
        }
    }

    public void jsonMotoLink(HashMap<String, String> header, HashMap<String, String> data, Boolean isSend) {

        ApiService apiResponse = ApiService.RetrofitClient.serviceRequest();
        apiResponse.getEzyMotoExternalPayment(header, data).enqueue(new Callback<EzyMotoRecPayment>() {
            @Override
            public void onResponse(Call<EzyMotoRecPayment> call, Response<EzyMotoRecPayment> response) {

                if (response.isSuccessful()) {
                    if (response.body().getResponseCode().equals("0000")) {
//                        String opt = response.body().getResponseData().getInvoiceId() + "\n" + response.body().getResponseData().getOpt();
                        String opt = response.body().getResponseData().getInvoiceId().replace("u003", "=");
                        if (isSend) {
                            getCurrentInputConnection().commitText(opt, 1);

                            isSended = true;
                        } else {
                            ClipboardManager board = (ClipboardManager) getSystemService(getApplicationContext().CLIPBOARD_SERVICE);
                            ClipData clip = ClipData.newPlainText("Mobi", opt);
                            board.setPrimaryClip(clip);
                            Toast.makeText(getApplicationContext(), "Copied to Clip board", Toast.LENGTH_SHORT).show();

                            isCopied = true;
//                            amount_edt.setText("0.00");
//                            invoice_home.setText("");
                        }

                        amount_edt.setText("0.00");
                        invoice_home.setText("");
                    } else {
                        Toast.makeText(getApplicationContext(), response.body().getResponseDescription(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<EzyMotoRecPayment> call, Throwable t) {

            }
        });
    }

    String getAmount(String amount) {

//        String ams = amount.replace(".", "")
        String ams = amount.replace(".", "");
        String amount_to_send = String.format("%012d", Long.valueOf(ams));

        return amount_to_send;
    }

}