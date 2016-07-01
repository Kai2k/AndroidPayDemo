package com.example.android.androidpaydemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wallet.Cart;
import com.google.android.gms.wallet.FullWallet;
import com.google.android.gms.wallet.FullWalletRequest;
import com.google.android.gms.wallet.LineItem;
import com.google.android.gms.wallet.MaskedWallet;
import com.google.android.gms.wallet.Wallet;
import com.google.android.gms.wallet.WalletConstants;
import com.google.android.gms.wallet.fragment.SupportWalletFragment;
import com.google.android.gms.wallet.fragment.WalletFragmentInitParams;
import com.google.android.gms.wallet.fragment.WalletFragmentMode;
import com.google.android.gms.wallet.fragment.WalletFragmentOptions;
import com.google.android.gms.wallet.fragment.WalletFragmentStyle;

import java.util.ArrayList;
import java.util.List;

public class ConfirmationActivity extends AppCompatActivity implements View.OnClickListener,
        GoogleApiClient.OnConnectionFailedListener {
    private MaskedWallet maskedWallet;
    private GoogleApiClient googleApiClient;
    private SupportWalletFragment walletFragment;
    private static final int REQUEST_CODE_CHANGE_MASKED_WALLET = 1002;
    private static final int REQUEST_CODE_RESOLVE_LOAD_FULL_WALLET = 1004;
    private static final String TAG = "ConfirmationActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        maskedWallet = getIntent().getParcelableExtra(WalletConstants.EXTRA_MASKED_WALLET);
        setContentView(R.layout.activity_confirm);

        Button confirmOrder = (Button) findViewById(R.id.btn_confirm_order);
        if (confirmOrder != null) {
            confirmOrder.setOnClickListener(this);
        }

        googleApiClient = new GoogleApiClient.Builder(this).enableAutoManage(this, this)
                .setAccountName(getString(R.string.user_name))
                .addApi(Wallet.API, new Wallet
                        .WalletOptions.Builder()
                        .setEnvironment(MainActivity.WALLET_ENVIRONMENT)
                        .setTheme(WalletConstants.THEME_LIGHT)
                        .build())
                .build();

        createAndAddWalletFragment();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        int errorCode;
        if (data != null) {
            errorCode = data.getIntExtra(WalletConstants.EXTRA_ERROR_CODE, -1);
        } else {
            Log.d(TAG, "onActivityResult - data was null");
            super.onActivityResult(requestCode, resultCode, null);
            return;
        }

        switch (requestCode) {
            case REQUEST_CODE_CHANGE_MASKED_WALLET:
                if (resultCode == Activity.RESULT_OK && data.hasExtra(WalletConstants
                        .EXTRA_MASKED_WALLET)) {
                    maskedWallet = data.getParcelableExtra(WalletConstants.EXTRA_MASKED_WALLET);
                }
                break;
            case REQUEST_CODE_RESOLVE_LOAD_FULL_WALLET:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        if (data.hasExtra(WalletConstants.EXTRA_FULL_WALLET)) {
                            FullWallet fullWallet = data.getParcelableExtra(WalletConstants
                                    .EXTRA_FULL_WALLET);
                            fetchTransactionStatus(fullWallet);
                        } else if (data.hasExtra(WalletConstants
                                .EXTRA_MASKED_WALLET)) {
                            maskedWallet = data.getParcelableExtra(WalletConstants
                                    .EXTRA_MASKED_WALLET);
                            getIntent().putExtra(WalletConstants.EXTRA_MASKED_WALLET, maskedWallet);
                            startActivity(getIntent());
                        }
                        break;
                    case Activity.RESULT_CANCELED:
                        break;
                    default:
                        handleError(errorCode);
                        break;
                }
                break;
            case WalletConstants.RESULT_ERROR:
                errorCode = data.getIntExtra(WalletConstants.EXTRA_ERROR_CODE, 0);
                handleError(errorCode);
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    private void createAndAddWalletFragment() {
        WalletFragmentStyle walletFragmentStyle = new WalletFragmentStyle()
                .setMaskedWalletDetailsTextAppearance(R.style
                        .WalletFragmentDetailsTextAppearance)
                .setMaskedWalletDetailsHeaderTextAppearance(R.style
                        .WalletFragmentDetailsHeaderTextAppearance)
                .setMaskedWalletDetailsBackgroundColor(ContextCompat.getColor
                        (getApplicationContext(), R.color.shinobi_white));

        WalletFragmentOptions walletFragmentOptions = WalletFragmentOptions.newBuilder()
                .setEnvironment(MainActivity.WALLET_ENVIRONMENT)
                .setFragmentStyle(walletFragmentStyle)
                .setTheme(WalletConstants.THEME_LIGHT)
                .setMode(WalletFragmentMode.SELECTION_DETAILS)
                .build();

        walletFragment = SupportWalletFragment.newInstance(walletFragmentOptions);

        WalletFragmentInitParams.Builder startParamsBuilder =
                WalletFragmentInitParams.newBuilder()
                        .setMaskedWallet(maskedWallet)
                        .setMaskedWalletRequestCode(REQUEST_CODE_CHANGE_MASKED_WALLET)
                        .setAccountName(getString(R.string.user_name));

        walletFragment.initialize(startParamsBuilder.build());

        // add Wallet fragment to the UI
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.dynamic_wallet_masked_wallet_fragment, walletFragment)
                .commit();
    }

    private void confirmOrder() {
        FullWalletRequest fullWalletRequest = FullWalletRequest.newBuilder()
                .setGoogleTransactionId(maskedWallet.getGoogleTransactionId())
                .setCart(Cart.newBuilder()
                        .setCurrencyCode(getString(R.string.store_currency))
                        .setTotalPrice(getString(R.string.total_price))
                        .setLineItems(new ShoppingCart(this).getLineItems())
                        .build())
                .build();
        Wallet.Payments.loadFullWallet(googleApiClient, fullWalletRequest,
                REQUEST_CODE_RESOLVE_LOAD_FULL_WALLET);
    }

    @Override
    public void onClick(View view) {
        confirmOrder();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void fetchTransactionStatus(FullWallet fullWallet) {
        Intent intent = new Intent(this, OrderSuccessActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(WalletConstants.EXTRA_FULL_WALLET, fullWallet);
        startActivity(intent);
    }

    private void handleError(int errorCode) {
        Toast.makeText(this, "Error code: " + errorCode, Toast.LENGTH_LONG).show();
    }
}
