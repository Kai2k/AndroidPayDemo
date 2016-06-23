package com.example.android.androidpaydemo;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.BooleanResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wallet.Cart;
import com.google.android.gms.wallet.LineItem;
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

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements
        View.OnClickListener,
        GoogleApiClient.OnConnectionFailedListener{

    private static final String TAG = "MainActivity";
    private static final int REQUEST_CODE_MASKED_WALLET = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wallet.API, new Wallet.WalletOptions.Builder()
                        .setEnvironment(WalletConstants.ENVIRONMENT_TEST)
                        .build())
                .enableAutoManage(this, this)
                .build();

        Wallet.Payments.isReadyToPay(googleApiClient).setResultCallback(
                new ResultCallback<BooleanResult>() {
                    @Override
                    public void onResult(@NonNull BooleanResult booleanResult) {

                        if (booleanResult.getStatus().isSuccess()) {
                            if (booleanResult.getValue()) {
                                Log.d(TAG, "isReadyToPay:true");
                                createAndAddWalletFragment();
                            } else {
                                Log.d(TAG, "isReadyToPay:false:" + booleanResult.getStatus());
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
        WalletFragmentStyle walletFragmentStyle = new WalletFragmentStyle()
                .setBuyButtonText(WalletFragmentStyle.BuyButtonText.BUY_WITH)
                .setBuyButtonAppearance(WalletFragmentStyle.BuyButtonAppearance.ANDROID_PAY_DARK)
                .setBuyButtonWidth(WalletFragmentStyle.Dimension.MATCH_PARENT);

        WalletFragmentOptions walletFragmentOptions = WalletFragmentOptions.newBuilder()
                .setEnvironment(WalletConstants.ENVIRONMENT_TEST)
                .setFragmentStyle(walletFragmentStyle)
                .setTheme(WalletConstants.THEME_LIGHT)
                .setMode(WalletFragmentMode.BUY_BUTTON)
                .build();
        SupportWalletFragment walletFragment = SupportWalletFragment.newInstance
                (walletFragmentOptions);

        MaskedWalletRequest maskedWalletRequest = createMaskedWalletRequest
                    (getPaymentMethodTokenizationParameters(getString(R.string.public_key)));

        WalletFragmentInitParams.Builder startParamsBuilder = WalletFragmentInitParams.newBuilder()
                .setMaskedWalletRequest(maskedWalletRequest)
                .setMaskedWalletRequestCode(REQUEST_CODE_MASKED_WALLET)
                .setAccountName(getString(R.string.user_name));
        walletFragment.initialize(startParamsBuilder.build());

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.placeholder_button_fragment, walletFragment)
                .commit();
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private MaskedWalletRequest createMaskedWalletRequest(PaymentMethodTokenizationParameters parameters) {
        return MaskedWalletRequest.newBuilder()
                .setMerchantName(getString(R.string.store_name))
                .setPhoneNumberRequired(true)
                .setShippingAddressRequired(true)
                .setCurrencyCode(getString(R.string.store_currency))
                .setEstimatedTotalPrice(getString(R.string.product_price))
                .setCart(Cart.newBuilder()
                        .setCurrencyCode(getString(R.string.store_currency))
                        .setTotalPrice(getString(R.string.product_price))
                        .setLineItems(getLineItems())
                        .build())
                .setPaymentMethodTokenizationParameters(parameters)
                .build();
    }

    private List<LineItem> getLineItems() {
        List<LineItem> lineItems = new ArrayList<>();
        lineItems.add(getShinobiCuddlyToyLineItem());
        return lineItems;
    }

    private LineItem getShinobiCuddlyToyLineItem() {
        return LineItem.newBuilder()
                .setCurrencyCode(getString(R.string.store_currency))
                .setDescription(getString(R.string.product_description))
                .setQuantity(getString(R.string.product_quantity))
                .setUnitPrice(getString(R.string.product_price))
                .setTotalPrice(getString(R.string.product_price))
                .build();
    }

    private PaymentMethodTokenizationParameters getPaymentMethodTokenizationParameters(String
                                                                                          publicKey) {
        return PaymentMethodTokenizationParameters.newBuilder().setPaymentMethodTokenizationType
                (PaymentMethodTokenizationType.NETWORK_TOKEN).addParameter("publicKey", publicKey).build();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
}
