package com.example.uefchangemaker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.AppCompatTextView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private AppCompatSpinner spinner;
    private AppCompatTextView txtVND, txtTo, txtCurrency;

    private List<CurrencyData> data;
    private ArrayAdapter<CurrencyData> adapter;
    private DecimalFormat decimalFormat;
    private int numberDecimalPlaces;
    private boolean isRoundUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "onCreate: ");

        spinner = findViewById(R.id.spinner_to);
        txtVND = findViewById(R.id.edt_vnd);
        txtTo = findViewById(R.id.txt_to);
        txtCurrency = findViewById(R.id.edt_currency);

        findViewById(R.id.btn_settings).setOnClickListener(this);
        findViewById(R.id.btn_one).setOnClickListener(this);
        findViewById(R.id.btn_two).setOnClickListener(this);
        findViewById(R.id.btn_three).setOnClickListener(this);
        findViewById(R.id.btn_four).setOnClickListener(this);
        findViewById(R.id.btn_five).setOnClickListener(this);
        findViewById(R.id.btn_six).setOnClickListener(this);
        findViewById(R.id.btn_seven).setOnClickListener(this);
        findViewById(R.id.btn_eight).setOnClickListener(this);
        findViewById(R.id.btn_nine).setOnClickListener(this);
        findViewById(R.id.btn_zero).setOnClickListener(this);
        findViewById(R.id.btn_clear).setOnClickListener(this);

        data = new ArrayList<>();

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemSelected: " + position);
                if (position >= 0 && position < data.size()) {
                    txtTo.setText(data.get(position).getName() + ":");
                    updateCurrency();

                    SharedPreferences sharedPref = getSharedPreferences("uefchangemaker", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putInt("index", position);
                    editor.apply();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.d(TAG, "onNothingSelected: ");
            }
        });

        loadData();
        loadUI();
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        SharedPreferences sharedPref = getSharedPreferences("uefchangemaker", Context.MODE_PRIVATE);
        if (sharedPref.getBoolean("has_changed", false)) {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean("has_changed", false);
            editor.apply();

            loadData();
            loadUI();
            updateCurrency();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        Log.d(TAG, "onSaveInstanceState: ");
        outState.putString("text_vnd", txtVND.getText().toString());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        txtVND.setText(savedInstanceState.getString("text_vnd", "0"));
        Log.d(TAG, "onRestoreInstanceState: ");
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_clear) {
            txtVND.setText("0");
            txtCurrency.setText("0");
        } else if (v.getId() == R.id.btn_zero) {
            putNumber(0);
        } else if (v.getId() == R.id.btn_one) {
            putNumber(1);
        } else if (v.getId() == R.id.btn_two) {
            putNumber(2);
        } else if (v.getId() == R.id.btn_three) {
            putNumber(3);
        } else if (v.getId() == R.id.btn_four) {
            putNumber(4);
        } else if (v.getId() == R.id.btn_five) {
            putNumber(5);
        } else if (v.getId() == R.id.btn_six) {
            putNumber(6);
        } else if (v.getId() == R.id.btn_seven) {
            putNumber(7);
        } else if (v.getId() == R.id.btn_eight) {
            putNumber(8);
        } else if (v.getId() == R.id.btn_nine) {
            putNumber(9);
        } else if (v.getId() == R.id.btn_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        }
    }

    private void loadData() {
        SharedPreferences sharedPref = getSharedPreferences("uefchangemaker", Context.MODE_PRIVATE);
        String dataStr = sharedPref.getString("data", "");
        data.clear();
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

        numberDecimalPlaces = sharedPref.getInt("number_decimal_places", 7);
        isRoundUp = sharedPref.getBoolean("round_up", false);
        String format = "#.";
        if (numberDecimalPlaces == 0) {
            format = "#";
        } else {
            for (int i = 0; i < numberDecimalPlaces; i++) {
                format += "#";
            }
        }
        decimalFormat = new DecimalFormat(format);
        Log.d(TAG, "loadData: " + numberDecimalPlaces + ", " + format);
    }

    private void loadUI() {
        adapter = new ArrayAdapter<>(this, R.layout.item_spinner, data);
        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        spinner.setAdapter(adapter);

        SharedPreferences sharedPref = getSharedPreferences("uefchangemaker", Context.MODE_PRIVATE);
        int index = sharedPref.getInt("index", 0);
        if (index >= 0 && index < data.size()) {
            spinner.setSelection(index);
        }
    }

    private void putNumber(int num) {
        Log.d(TAG, "putNumber: " + num);
        String vndStr = txtVND.getText().toString() + num;
        try {
            long vnd = Long.parseLong(vndStr);
            txtVND.setText(String.valueOf(vnd));

            BigDecimal vndBD = new BigDecimal(String.valueOf(vnd));
            BigDecimal value = new BigDecimal(String.valueOf(data.get(spinner.getSelectedItemPosition()).getRate()));
            BigDecimal result;
            if (isRoundUp) {
                result = vndBD.multiply(value).setScale(numberDecimalPlaces, RoundingMode.HALF_UP);
            } else {
                result = vndBD.multiply(value).setScale(numberDecimalPlaces, RoundingMode.DOWN);
            }
            Log.d(TAG, "putNumber: " + numberDecimalPlaces + ", " + result.toString());

            txtCurrency.setText(decimalFormat.format(result.doubleValue()));
        } catch (NumberFormatException ignored) {}
    }

    private void updateCurrency() {
        Log.d(TAG, "updateCurrency: ");
        try {
            long vnd = Long.parseLong(txtVND.getText().toString());
            if (vnd == 0) {
                return;
            }

            BigDecimal vndBD = new BigDecimal(String.valueOf(vnd));
            BigDecimal rate = new BigDecimal(String.valueOf(data.get(spinner.getSelectedItemPosition()).getRate()));
            BigDecimal result;
            if (isRoundUp) {
                result = vndBD.multiply(rate).setScale(numberDecimalPlaces, RoundingMode.HALF_UP);
            } else {
                result = vndBD.multiply(rate).setScale(numberDecimalPlaces, RoundingMode.DOWN);
            }
            Log.d(TAG, "updateCurrency: " + numberDecimalPlaces + ", " + result.toString());

            txtCurrency.setText(decimalFormat.format(result.doubleValue()));
        } catch (NumberFormatException ignored) {}
    }
}
