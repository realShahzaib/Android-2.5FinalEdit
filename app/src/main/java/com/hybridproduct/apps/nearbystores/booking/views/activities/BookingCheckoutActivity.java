package com.hybridproduct.apps.nearbystores.booking.views.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.hybridproduct.apps.nearbystores.AppController;
import com.hybridproduct.apps.nearbystores.R;
import com.hybridproduct.apps.nearbystores.appconfig.AppContext;
import com.hybridproduct.apps.nearbystores.appconfig.Constances;
import com.hybridproduct.apps.nearbystores.booking.controllers.ProductsController;
import com.hybridproduct.apps.nearbystores.booking.controllers.services.GenericNotifyEvent;
import com.hybridproduct.apps.nearbystores.booking.modals.CF;
import com.hybridproduct.apps.nearbystores.booking.modals.Cart;
import com.hybridproduct.apps.nearbystores.booking.modals.Option;
import com.hybridproduct.apps.nearbystores.booking.modals.Variant;
import com.hybridproduct.apps.nearbystores.booking.views.fragments.checkout.BookingInfoFragment;
import com.hybridproduct.apps.nearbystores.booking.views.fragments.checkout.ConfirmationFragment;
import com.hybridproduct.apps.nearbystores.classes.Store;
import com.hybridproduct.apps.nearbystores.controllers.SettingsController;
import com.hybridproduct.apps.nearbystores.controllers.sessions.SessionsController;
import com.hybridproduct.apps.nearbystores.controllers.stores.StoreController;
import com.hybridproduct.apps.nearbystores.network.VolleySingleton;
import com.hybridproduct.apps.nearbystores.network.api_request.SimpleRequest;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.hybridproduct.apps.nearbystores.appconfig.AppConfig.APP_DEBUG;

public class BookingCheckoutActivity extends AppCompatActivity {


    //checkout navigation fields
    private enum State {ORDER, CONFIRMATION}

    State[] array_state = new State[]{State.ORDER, State.CONFIRMATION};
    private View line_first;
    private ImageView image_shipping, image_confirm;
    private TextView tv_shipping, tv_confirm;
    private int idx_state = 0;


    //init static params
    public static HashMap<String, String> orderFields;
    public static int order_id = -1;
    public static int module_id = -1;

    public static List<Cart> mCart;
    public static Store mStore;


    public static int PAYMENT_CALLBACK_CODE = 2020;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.booking_checkout_activity);
        initToolbar();

        initComponent();

        buttonRtlSupp();

        handleIntentAction();

        // display the first fragment as a default page
        displayFragment(State.ORDER);

    }


    private void buttonRtlSupp() {
        //rtl
        if (AppController.isRTL()) {
            ((ImageView) findViewById(R.id.arrow_next)).setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_arrow_forward_white_18dp, null));
            ((ImageView) findViewById(R.id.arrow_previous)).setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_arrow_forward_white_18dp, null));
        } else {
            ((ImageView) findViewById(R.id.arrow_next)).setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_arrow_back, null));
            ((ImageView) findViewById(R.id.arrow_previous)).setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_arrow_back, null));
        }

        ((ImageView) findViewById(R.id.arrow_next)).setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        ((ImageView) findViewById(R.id.arrow_previous)).setColorFilter(getResources().getColor(R.color.black), PorterDuff.Mode.SRC_ATOP);

    }

    private void handleIntentAction() {
        //checkout from offer
        if (getIntent().hasExtra("module_id") && getIntent().hasExtra("module")) {

            module_id = getIntent().getIntExtra("module_id", 0);
            mStore = StoreController.getStore(module_id);
            Cart storedCart = ProductsController.findServiceByStoreId(module_id);
            mCart = Arrays.asList(storedCart);
        }

    }


    private void initComponent() {
        line_first = findViewById(R.id.line_first);
        image_shipping = findViewById(R.id.image_shipping);
        image_confirm = findViewById(R.id.image_confirm);

        tv_shipping = findViewById(R.id.tv_shipping);
        tv_confirm = findViewById(R.id.tv_confirm);

        image_confirm.setColorFilter(getResources().getColor(R.color.grey_20), PorterDuff.Mode.SRC_ATOP);

        (findViewById(R.id.lyt_next)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                //check for required field
                if (array_state[idx_state] == State.ORDER)
                    if (checkRequiredFields(mStore)) {
                        //display error message and cancel the operation
                        Toast.makeText(BookingCheckoutActivity.this, getString(R.string.complet_required_fileds), Toast.LENGTH_LONG).show();
                        return;
                    }

                //check content format
                if (!checkRegexFormatField(mStore)) {
                    //display error message and cancel the operation
                    return;
                }

                //Submit order action
                if (array_state[idx_state] == State.CONFIRMATION) {
                    submitOrderAPI();
                    if (idx_state == array_state.length - 1) {
                        return;

                    }
                }

                //navigate to the next fragment
                idx_state++;
                displayFragment(array_state[idx_state]);

                //change btton status after click
                buttonStatusChange();

            }
        });

        (

                findViewById(R.id.lyt_previous)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (idx_state < 1) return;
                idx_state--;
                displayFragment(array_state[idx_state]);

                buttonStatusChange();

            }
        });

    }

    private void showSuccessPage() {

        findViewById(R.id.layout_content).setVisibility(View.GONE);
        findViewById(R.id.layout_done).setVisibility(View.VISIBLE);
        findViewById(R.id.lyt_done).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // run event to update  the order list
                EventBus.getDefault().postSticky(new GenericNotifyEvent("order_updated"));
                finish();
            }
        });

        //update color
        findViewById(R.id.lyt_done).setBackgroundColor(getResources().getColor(R.color.green));


    }

    private void buttonStatusChange() {


        if (idx_state == array_state.length - 1) {

            if (!SettingsController.isModuleEnabled(Constances.ModulesConfig.ORDER_PAYMENT_MODULE)) {
                ((TextView) findViewById(R.id.btn_next)).setText(getString(R.string.confirm_order));
            } else {
                ((TextView) findViewById(R.id.btn_next)).setText(getString(R.string.confirm_payment));
                //findViewById(R.id.lyt_previous).setVisibility(View.GONE);
            }

            (findViewById(R.id.arrow_next)).setVisibility(View.GONE);
        } else {
            ((TextView) findViewById(R.id.btn_next)).setText(getString(R.string.next));
            (findViewById(R.id.arrow_next)).setVisibility(View.VISIBLE);
            //(findViewById(R.id.lyt_previous)).setVisibility(View.VISIBLE);

        }

    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_close_white_24dp);
        toolbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    private void displayFragment(State state) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Bundle bundle = new Bundle();
        bundle.putInt("module_id", module_id);
        bundle.putString("module", Constances.ModulesConfig.SERVICE_MODULE);

        Fragment fragment = null;


        refreshStepTitle();

        if (state.name().equalsIgnoreCase(State.ORDER.name())) {
            fragment = new BookingInfoFragment();
            fragment.setArguments(bundle);
            tv_shipping.setTextColor(getResources().getColor(R.color.colorPrimary));
            image_shipping.clearColorFilter();
            line_first.setBackgroundColor(getResources().getColor(R.color.grey_20));
        } else if (state.name().equalsIgnoreCase(State.CONFIRMATION.name())) {
            fragment = new ConfirmationFragment();
            fragment.setArguments(bundle);

            tv_shipping.setTextColor(getResources().getColor(R.color.colorPrimary));
            tv_confirm.setTextColor(getResources().getColor(R.color.colorPrimary));
            line_first.setBackgroundColor(getResources().getColor(R.color.colorPrimary));

            image_confirm.clearColorFilter();
        }

        if (fragment == null) return;
        fragmentTransaction.replace(R.id.frame_content, fragment);
        fragmentTransaction.commit();
    }

    private void refreshStepTitle() {
        tv_shipping.setTextColor(getResources().getColor(R.color.grey_40));
        tv_confirm.setTextColor(getResources().getColor(R.color.grey_40));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        orderFields = null;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        } else {
            Toast.makeText(getApplicationContext(), item.getTitle(), Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean checkRequiredFields(final Store mItemOrderble) {

        Boolean result = false;
        if (mItemOrderble != null) {
            for (CF mCF : mItemOrderble.getCf()) {
                if (mCF.getRequired() == 1) {
                    if (orderFields != null && !orderFields.containsKey(mCF.getLabel())
                            || (orderFields.containsKey(mCF.getLabel())
                            && orderFields.get(mCF.getLabel()).trim().length() == 0)) {
                        result = true;
                        break;
                    }
                }

            }
        }
        return result;
    }


    private boolean checkRegexFormatField(final Store mItemOrderble) {

        Boolean result = true;
        if (mItemOrderble != null) {
            for (CF mCF : mItemOrderble.getCf()) {
                if (mCF.getType() != null) {
                    String[] arrayType = mCF.getType().split("\\.");

                    //check if location field is good
                    if (arrayType.length > 0 && arrayType[1].equals("location")) {
                        if (orderFields != null && orderFields.containsKey(mCF.getLabel())) {
                            String[] locationFormat = orderFields.get(mCF.getLabel()).split(";");
                            if (locationFormat.length != 3) {
                                Toast.makeText(this, getString(R.string.location_format_not_correct), Toast.LENGTH_SHORT).show();
                                result = false;
                                break;
                            } else {
                                if (locationFormat[0].length() == 0) {
                                    Toast.makeText(this, getString(R.string.location_address_not_correct), Toast.LENGTH_SHORT).show();
                                    result = false;
                                    break;
                                }
                                //check if float
                                try {
                                    Float.parseFloat(locationFormat[1]);
                                } catch (NumberFormatException e) {
                                    Toast.makeText(this, getString(R.string.location_format_not_correct), Toast.LENGTH_SHORT).show();
                                    result = false;
                                    break;
                                }

                                //check if float
                                try {
                                    Float.parseFloat(locationFormat[2]);
                                } catch (NumberFormatException e) {
                                    Toast.makeText(this, getString(R.string.location_format_not_correct), Toast.LENGTH_SHORT).show();
                                    result = false;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        return result;
    }


    private void submitOrderAPI() {

        RequestQueue queue = VolleySingleton.getInstance(this).getRequestQueue();
        queue = VolleySingleton.getInstance(this).getRequestQueue();
        Gson gson = new Gson();

        final Map<String, String> params = new HashMap<String, String>();

        if (SessionsController.isLogged()) {
            params.put("user_id", SessionsController.getSession().getUser().getId() + "");
            params.put("user_token", SessionsController.getSession().getUser().getToken());
        }


        params.put("store_id", String.valueOf(module_id));
        params.put("req_cf_id", String.valueOf(mStore.getCf_id()));


        if (orderFields != null && !orderFields.isEmpty()) {
            String json = gson.toJson(orderFields); // convert hashmaps to json format
            params.put("req_cf_data", json);
        }

        try {
            JSONArray carts = new JSONArray();

            for (Cart c : mCart) {

                if (c.getVariants() != null && c.getVariants().size() > 0) {
                    List<Variant> variants = c.getVariants();
                    for (Variant var : variants) {
                        JSONObject cart = new JSONObject();
                        cart.put("module", c.getModule());
                        cart.put("qty", String.valueOf(c.getQte()));
                        cart.put("amount", c.getAmount() > 0 ? c.getAmount() : 0);

                        //dynamic
                        cart.put("module_id", String.valueOf(var.getGroup_id()));

                        List<Option> options = var.getOptions();
                        JSONObject optJson = new JSONObject();
                        for (Option opt : options) {
                            optJson.put(opt.getLabel(), String.valueOf(opt.getLabel()));

                        }
                        cart.put("options", optJson.toString());
                        carts.put(cart);

                    }


                }
            }
            params.put("cart", carts.toString());


        } catch (JSONException e) {
            e.printStackTrace();
        }


        SimpleRequest request = new SimpleRequest(Request.Method.POST,
                Constances.API.API_BOOKING_CREATE, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {

                    if (AppContext.DEBUG)
                        Log.e("order_api_output", response);

                    JSONObject jso = new JSONObject(response);
                    int success = jso.getInt("success");
                    if (success == 1) {
                        order_id = jso.getInt("result");
                        showSuccessPage();

                        //Save custom field in shared pref
                        if (orderFields != null && !orderFields.isEmpty()) {

                            int userId = SessionsController.getSession().getUser().getId();
                            int cfId = mStore.getCf_id();
                            final SharedPreferences sharedPref = AppController.getInstance()
                                    .getSharedPreferences("savedCF_" + cfId + "_" + userId, Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putInt("user_id", userId);
                            editor.putInt("req_cf_id", cfId);
                            editor.putString("cf", gson.toJson(orderFields));
                            editor.commit();
                        }

                    } else {
                        Toast.makeText(BookingCheckoutActivity.this, getString(R.string.error_try_later), Toast.LENGTH_LONG).show();

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (APP_DEBUG) Log.e("ERROR", error.toString());
            }
        }) {

            @Override
            protected Map<String, String> getParams() {

                if (AppContext.DEBUG)
                    Log.e("order_api_input", params.toString());

                return params;
            }

        };

        request.setRetryPolicy(new DefaultRetryPolicy(SimpleRequest.TIME_OUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(request);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PAYMENT_CALLBACK_CODE) {
            if (resultCode == AutocompleteActivity.RESULT_OK) {

                showSuccessPage();
            } else {
                Toast.makeText(this, getString(R.string.payment_error), Toast.LENGTH_LONG).show();

            }
        }
    }
}

