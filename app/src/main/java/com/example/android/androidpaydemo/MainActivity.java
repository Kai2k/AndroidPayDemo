package com.example.android.androidpaydemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.BooleanResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wallet.Cart;
import com.google.android.gms.wallet.MaskedWallet;
import com.google.android.gms.wallet.MaskedWalletRequest;
import com.google.android.gms.wallet.PaymentMethodTokenizationParameters;
import com.google.android.gms.wallet.PaymentMethodTokenizationType;
import com.google.android.gms.wallet.Wallet;
import com.google.android.gms.wallet.WalletConstants;
import com.google.android.gms.wallet.fragment.SupportWalletFragment;
import com.google.android.gms.wallet.fragment.WalletFragmentInitParams;
import com.google.android.gms.wallet.fragment.WalletFragmentMode;
import com.google.android.gms.wallet.fragment.WalletFragmentOptions;
import com.google.android.gms.wallet.fragment.WalletFragmentStyle;

public class MainActivity extends AppCompatActivity implements GoogleApiClient
        .OnConnectionFailedListener {

    private static final int REQUEST_CODE_MASKED_WALLET = 10033;
    private static final String TAG = "MainActivity";
    static final int WALLET_ENVIRONMENT = WalletConstants.ENVIRONMENT_TEST;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final GoogleApiClient googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wallet.API, new Wallet
                        .WalletOptions.Builder()
                        .setEnvironment(WALLET_ENVIRONMENT)
                        .build())
                .enableAutoManage(this, this)
                .build();

        Wallet.Payments.isReadyToPay(googleApiClient).setResultCallback(new ResultCallback<BooleanResult>() {
            @Override
            public void onResult(@NonNull BooleanResult booleanResult) {

                if (booleanResult.getStatus().isSuccess()) {
                    if (booleanResult.getValue()) {
                        createAndAddWalletFragment();
                    } else {
                        View androidPayLayout = findViewById(R.id.layout_android_pay);
                        if (androidPayLayout != null) {
                            androidPayLayout.setVisibility(View.GONE);
                        }
                    }
                } else {
                    Log.e(TAG, "isReadyToPay:" + booleanResult.getStatus());
                }
            }
        });
    }

    private void createAndAddWalletFragment() {
        final WalletFragmentStyle walletFragmentStyle = new WalletFragmentStyle()
                .setBuyButtonText(WalletFragmentStyle.BuyButtonText.BUY_WITH)
                .setBuyButtonAppearance(WalletFragmentStyle.BuyButtonAppearance.ANDROID_PAY_DARK)
                .setBuyButtonWidth(WalletFragmentStyle.Dimension.MATCH_PARENT);

        final WalletFragmentOptions walletFragmentOptions = WalletFragmentOptions.newBuilder()
                .setEnvironment(WALLET_ENVIRONMENT)
                .setFragmentStyle(walletFragmentStyle)
                .setTheme(WalletConstants.THEME_LIGHT)
                .setMode(WalletFragmentMode.BUY_BUTTON)
                .build();

        final SupportWalletFragment walletFragment = SupportWalletFragment.newInstance
                (walletFragmentOptions);

        final MaskedWalletRequest maskedWalletRequest = createMaskedWalletRequest
                (getPaymentMethodTokenizationParameters(getString(R.string.public_key)));

        final WalletFragmentInitParams.Builder startParamsBuilder = WalletFragmentInitParams
                .newBuilder()
                .setMaskedWalletRequest(maskedWalletRequest)
                .setMaskedWalletRequestCode(REQUEST_CODE_MASKED_WALLET)
                .setAccountName(getString(R.string.user_name));

        walletFragment.initialize(startParamsBuilder.build());

        getSupportFragmentManager().beginTransaction().replace(R.id.placeholder_button_fragment,
                walletFragment).commit();
    }

    private MaskedWalletRequest createMaskedWalletRequest(PaymentMethodTokenizationParameters
                                                                  parameters) {
        return MaskedWalletRequest.newBuilder()
                .setMerchantName(getString(R.string.store_name))
                .setPhoneNumberRequired(true)
                .setShippingAddressRequired(true)
                .setCurrencyCode(getString(R.string.store_currency))
                .setEstimatedTotalPrice(getString(R.string.total_price))
                .setCart(Cart.newBuilder()
                        .setCurrencyCode(getString(R.string.store_currency))
                        .setTotalPrice(getString(R.string.product_price))
                        .setLineItems(new ShoppingCart(this).getLineItems())
                        .build())
                .setPaymentMethodTokenizationParameters(parameters)
                .build();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        int errorCode;
        if (data != null) {
            errorCode = data.getIntExtra(WalletConstants.EXTRA_ERROR_CODE, -1);
        } else {
            Log.d(TAG, "onActivityResult - data was null");
            return;
        }

        switch (requestCode) {
            case REQUEST_CODE_MASKED_WALLET:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        final MaskedWallet maskedWallet = data.getParcelableExtra(WalletConstants
                                .EXTRA_MASKED_WALLET);
                        launchConfirmationPage(maskedWallet);
                        break;
                    case Activity.RESULT_CANCELED:
                        break;
                    default:
                        handleError(errorCode);
                        break;
                }
                break;
            case WalletConstants.RESULT_ERROR:
                handleError(errorCode);
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    private void launchConfirmationPage(MaskedWallet maskedWallet) {
        final Intent intent = new Intent(this, ConfirmationActivity.class);
        intent.putExtra(WalletConstants.EXTRA_MASKED_WALLET, maskedWallet);
        startActivity(intent);
    }

    private void handleError(int errorCode) {
        Toast.makeText(this, "Error code: " + errorCode, Toast.LENGTH_LONG).show();
    }

    private PaymentMethodTokenizationParameters getPaymentMethodTokenizationParameters(String publicKey) {
        return PaymentMethodTokenizationParameters.newBuilder()
                .setPaymentMethodTokenizationType(PaymentMethodTokenizationType.NETWORK_TOKEN)
                .addParameter("publicKey",publicKey)
                .build();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
