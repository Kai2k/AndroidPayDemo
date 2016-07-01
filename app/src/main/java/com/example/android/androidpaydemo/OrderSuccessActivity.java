package com.example.android.androidpaydemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.wallet.FullWallet;
import com.google.android.gms.wallet.PaymentMethodToken;
import com.google.android.gms.wallet.WalletConstants;

public class OrderSuccessActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_success);
        final FullWallet mFullWallet = getIntent().getParcelableExtra(WalletConstants
                .EXTRA_FULL_WALLET);
        final PaymentMethodToken paymentMethodToken = mFullWallet.getPaymentMethodToken();
        final TextView txtPaymentMethodToken = (TextView) findViewById(R.id
                .txt_payment_method_token);
        if (txtPaymentMethodToken != null) {
            txtPaymentMethodToken.setText(paymentMethodToken.getToken());
        }
        final Button continueButton = (Button) findViewById(R.id.btn_continue);
        if (continueButton != null) {
            continueButton.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {
        final Intent intent = new Intent(OrderSuccessActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        OrderSuccessActivity.this.startActivity(intent);
    }
}
