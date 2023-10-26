package com.example.uefchangemaker;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.SwitchCompat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = SettingsActivity.class.getSimpleName();

    private AppCompatSpinner spinner;
    private AppCompatEditText edtNumber;
    private SwitchCompat switchRoundUp;
    private AppCompatTextView txtCurrency;

    private DecimalFormat decimalFormat;
    private List<CurrencyData> data;
    private ArrayAdapter<CurrencyData> adapter;
    private int numberDecimalPlaces;

    private final TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            edtNumber.removeTextChangedListener(textWatcher);

            SharedPreferences sharedPref = getSharedPreferences("uefchangemaker", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();

            try {
                int num = Integer.parseInt(s.toString());
                s.clear();
                s.append(String.valueOf(num));
                editor.putInt("number_decimal_places", num);
                String format = "#.";
                if (num == 0) {
                    format = "#";
                } else {
                    for (int i = 0; i < num; i++) {
                        format += "#";
                    }
                }
                decimalFormat = new DecimalFormat(format);
                numberDecimalPlaces = num;
            } catch (NumberFormatException e) {
                s.clear();
                s.append('0');
                editor.putInt("number_decimal_places", 0);
                decimalFormat = new DecimalFormat("#");
                numberDecimalPlaces = 0;
            }

            editor.putBoolean("has_changed", true);
            editor.apply();

            edtNumber.addTextChangedListener(textWatcher);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        data = new ArrayList<>();

        spinner = findViewById(R.id.spinner_currency);
        edtNumber = findViewById(R.id.edt_number);
        switchRoundUp = findViewById(R.id.switch_round_up);
        txtCurrency = findViewById(R.id.txt_currency);

        findViewById(R.id.btn_back).setOnClickListener(this);
        findViewById(R.id.btn_add).setOnClickListener(this);
        findViewById(R.id.btn_edit).setOnClickListener(this);
        findViewById(R.id.btn_remove).setOnClickListener(this);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemSelected: " + position);
                if (position >= 0 && position < data.size()) {
                    BigDecimal rate = new BigDecimal(String.valueOf(data.get(position).getRate()));
                    if (switchRoundUp.isChecked()) {
                        rate = rate.setScale(numberDecimalPlaces, RoundingMode.HALF_UP);
                    } else {
                        rate = rate.setScale(numberDecimalPlaces, RoundingMode.DOWN);
                    }
                    txtCurrency.setText(decimalFormat.format(rate.doubleValue()));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.d(TAG, "onNothingSelected: ");
            }
        });
        edtNumber.addTextChangedListener(textWatcher);
        switchRoundUp.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences sharedPref = getSharedPreferences("uefchangemaker", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean("round_up", isChecked);
            editor.putBoolean("has_changed", true);
            editor.apply();
        });

        loadData();
        loadUI();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_back) {
            finish();
        } else if (v.getId() == R.id.btn_add) {
            addCurrencyData();
        } else if (v.getId() == R.id.btn_edit) {
            editCurrencyData();
        } else if (v.getId() == R.id.btn_remove) {
            removeCurrencyData();
        }
    }

    private void loadData() {
        SharedPreferences sharedPref = getSharedPreferences("uefchangemaker", Context.MODE_PRIVATE);
        String dataStr = sharedPref.getString("data", "");
        if (dataStr.isEmpty()) {
            data.add(new CurrencyData("USD", 0.000041));
            data.add(new CurrencyData("won", 0.055));
            data.add(new CurrencyData("Yen", 0.0061));
            data.add(new CurrencyData("VND", 1));
        } else {
            String[] keys = dataStr.split("\\|");
            for (String key : keys) {
                data.add(new CurrencyData(key, Double.parseDouble(sharedPref.getString(key, "0"))));
            }
        }
    }

    private void loadUI() {
        adapter = new ArrayAdapter<>(this, R.layout.item_spinner, data);
        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        adapter.setNotifyOnChange(true);
        spinner.setAdapter(adapter);

        SharedPreferences sharedPref = getSharedPreferences("uefchangemaker", Context.MODE_PRIVATE);
        numberDecimalPlaces = sharedPref.getInt("number_decimal_places", 7);
        edtNumber.setText(String.valueOf(numberDecimalPlaces));
        switchRoundUp.setChecked(sharedPref.getBoolean("round_up", false));
        String format = "#.";
        if (numberDecimalPlaces == 0) {
            format = "#";
        } else {
            for (int i = 0; i < numberDecimalPlaces; i++) {
                format += "#";
            }
        }
        decimalFormat = new DecimalFormat(format);
    }

    private void addCurrencyData() {
        Log.d(TAG, "addCurrencyData: ");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Currency Data");

        View viewInflated = getLayoutInflater().inflate(R.layout.dialog_currency_data, null);

        TextInputEditText currency = viewInflated.findViewById(R.id.edt_currency);
        TextInputEditText rate = viewInflated.findViewById(R.id.edt_rate);

        builder.setView(viewInflated);

        builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
        });
        builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.cancel());

        final AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            if (currency.getText().length() == 0 || rate.getText().length() == 0) {
                Toast.makeText(SettingsActivity.this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            String currencyStr = currency.getText().toString();
            for (CurrencyData currencyData : data) {
                if (currencyData.getName().equalsIgnoreCase(currencyStr)) {
                    Toast.makeText(SettingsActivity.this, "Currency already exists", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            double rateF;
            try {
                rateF = Double.parseDouble(rate.getText().toString());
            } catch (NumberFormatException e) {
                Toast.makeText(SettingsActivity.this, "Invalid exchange rate", Toast.LENGTH_SHORT).show();
                return;
            }

            Log.d(TAG, "addCurrencyData: " + currency.getText() + ", " + rate.getText());
            Log.d(TAG, "addCurrencyData: " + currencyStr + ", " + rateF);
            data.add(new CurrencyData(currencyStr, rateF));
            adapter.notifyDataSetChanged();

            saveData();

            dialog.dismiss();
        });
    }

    private void editCurrencyData() {
        Log.d(TAG, "editCurrencyData: ");

        int position = spinner.getSelectedItemPosition();
        if (position == -1) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Currency Data");

        View viewInflated = getLayoutInflater().inflate(R.layout.dialog_currency_data, null);

        TextInputEditText currency = viewInflated.findViewById(R.id.edt_currency);
        currency.setText(data.get(position).getName());
        currency.setClickable(false);
        currency.setFocusable(false);
        currency.setFocusableInTouchMode(false);

        TextInputEditText rate = viewInflated.findViewById(R.id.edt_rate);
        BigDecimal decimal = new BigDecimal(String.valueOf(data.get(position).getRate()));
        rate.setText(String.format("%s", decimal));

        builder.setView(viewInflated);

        builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
        });
        builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.cancel());

        final AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            if (rate.getText().length() == 0) {
                Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            String currencyStr = currency.getText().toString();
            double rateF;
            try {
                rateF = Double.parseDouble(rate.getText().toString());
            } catch (NumberFormatException e) {
                Toast.makeText(SettingsActivity.this, "Invalid exchange rate", Toast.LENGTH_SHORT).show();
                return;
            }

            for (CurrencyData currencyData : data) {
                if (currencyData.getName().equalsIgnoreCase(currencyStr)) {
                    currencyData.setRate(rateF);
                }
            }
            adapter.notifyDataSetChanged();

            BigDecimal rateD = new BigDecimal(String.valueOf(rateF));
            if (switchRoundUp.isChecked()) {
                rateD = rateD.setScale(numberDecimalPlaces, RoundingMode.HALF_UP);
            } else {
                rateD = rateD.setScale(numberDecimalPlaces, RoundingMode.DOWN);
            }
            txtCurrency.setText(decimalFormat.format(rateD.doubleValue()));

            saveData();

            dialog.dismiss();
        });
    }

    private void removeCurrencyData() {
        Log.d(TAG, "removeCurrencyData: ");

        int selection = spinner.getSelectedItemPosition();
        if (selection == -1) {
            return;
        }

        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    data.remove(selection);
                    adapter.notifyDataSetChanged();
                    saveData();
                    dialog.dismiss();
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    dialog.cancel();
                    break;
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure to delete?")
                .setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }

    private void saveData() {
        SharedPreferences sharedPref = getSharedPreferences("uefchangemaker", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        if (data.isEmpty()) {
            editor.putString("data", "");
        } else {
            String dataStr = data.get(0).getName();
            for (int i = 1; i < data.size(); i++) {
                dataStr += ("|" + data.get(i).getName());
            }
            editor.putString("data", dataStr);
        }

        for (CurrencyData currencyData : data) {
            editor.putString(currencyData.getName(), new BigDecimal(String.valueOf(currencyData.getRate())).toString());
        }

        editor.apply();
    }
}
