package com.example.android.androidpaydemo;

import android.content.Context;

import com.google.android.gms.wallet.LineItem;

import java.util.ArrayList;
import java.util.List;

class ShoppingCart {

    private final Context context;

    ShoppingCart(Context context) {
        this.context = context;
    }

    List<LineItem> getLineItems() {
        final List<LineItem> lineItems = new ArrayList<>();
        lineItems.add(getShinobiCuddlyToyLineItem());
        return lineItems;
    }

    private LineItem getShinobiCuddlyToyLineItem() {
        return LineItem.newBuilder()
                .setCurrencyCode(context.getString(R.string.store_currency))
                .setDescription(context.getString(R.string.product_description))
                .setQuantity(context.getString(R.string.product_quantity))
                .setUnitPrice(context.getString(R.string.product_price))
                .setTotalPrice(context.getString(R.string.total_price))
                .build();
    }
}
