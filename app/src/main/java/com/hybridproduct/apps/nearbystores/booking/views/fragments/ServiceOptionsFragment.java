package com.hybridproduct.apps.nearbystores.booking.views.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.hybridproduct.apps.nearbystores.R;
import com.hybridproduct.apps.nearbystores.appconfig.Constances;
import com.hybridproduct.apps.nearbystores.booking.controllers.CartController;
import com.hybridproduct.apps.nearbystores.booking.modals.Cart;
import com.hybridproduct.apps.nearbystores.booking.modals.Option;
import com.hybridproduct.apps.nearbystores.booking.modals.Variant;
import com.hybridproduct.apps.nearbystores.booking.views.activities.BookingCheckoutActivity;
import com.hybridproduct.apps.nearbystores.classes.Store;
import com.hybridproduct.apps.nearbystores.controllers.sessions.SessionsController;
import com.hybridproduct.apps.nearbystores.controllers.stores.StoreController;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmList;


public class ServiceOptionsFragment extends Fragment {

    @BindView(R.id.frame_content)
    LinearLayout frame_content;

    @BindView(R.id.layout_custom_order)
    LinearLayout layout_custom_order;
    @BindView(R.id.btn_custom_order)
    AppCompatButton btnCustomOrder;
    // custom quantity fields
    private Context mContext;
    private List<Variant> mProduct;
    private int store_id;
    private Store mStore;
    private float customPrice = -1;

    private RealmList<Variant> selectedOptions;
    private Cart mcart;


    @OnClick(R.id.btn_custom_order)
    public void submit(View view) {


        //fill cart detail
        mcart.setModule_id(store_id);
        mcart.setModule(Constances.ModulesConfig.SERVICE_MODULE);
        mcart.setAmount(customPrice);
        mcart.setQte(1);
        mcart.setServices(mStore.getVariants());
        mcart.setVariants(selectedOptions);
        if (SessionsController.isLogged())
            mcart.setUser_id(SessionsController.getSession().getUser().getId());


        //delete all from carts
        CartController.removeAll();
        //save cart in the database
        CartController.addServiceToCart(mcart);

        //redirect to cart activity
        Intent intent = new Intent(new Intent(getActivity(), BookingCheckoutActivity.class));
        intent.putExtra("module_id", store_id);
        intent.putExtra("module", Constances.ModulesConfig.SERVICE_MODULE);

        startActivity(intent);
        getActivity().finish();

    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //init custom params
        selectedOptions = new RealmList<>();
        mcart = new Cart();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_variant, container, false);
        mContext = root.getContext();
        ButterKnife.bind(this, root);


        Bundle args = getArguments();
        if (args != null) {
            store_id = args.getInt(Constances.ModulesConfig.STORE_MODULE);
            mStore = StoreController.getStore(store_id);

            if (mStore != null) {

                mProduct = mStore.getVariants();

                generateGroupView(mContext, mProduct);

            }
        }


        return root;

    }

    @SuppressLint("StringFormatInvalid")
    private void generateGroupView(Context context, List<Variant> variants) {

        if (variants != null && variants.size() > 0) {


            //global fields
            LinearLayout.LayoutParams lp_match_wrap = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            LinearLayout.LayoutParams lp_wrap_wrap = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);


            for (Variant variant : variants) {

                //fill the selected variant
                Variant variant1 = new Variant();
                variant1.setGroup_id(variant.getGroup_id());

                //group linear layout
                LinearLayout group_wrapper = new LinearLayout(context);
                group_wrapper.setOrientation(LinearLayout.VERTICAL);
                group_wrapper.setPaddingRelative((int) getResources().getDimension(R.dimen.spacing_middle), 0, (int) getResources().getDimension(R.dimen.spacing_middle), (int) getResources().getDimension(R.dimen.spacing_middle));
                LinearLayout.LayoutParams grpLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                grpLayoutParams.setMargins(0, 0, 0, 0);
                group_wrapper.setLayoutParams(grpLayoutParams);

                //group title txt
                TextView group_label = new TextView(context);
                group_label.setText(variant.getGroup_label());
                group_label.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                group_label.setTypeface(group_label.getTypeface(), Typeface.BOLD);
                group_label.setTextColor(ContextCompat.getColorStateList(context, R.color.defaultColorText));
                group_label.setTextSize(20);


                lp_match_wrap.setMargins(0, 0, 0, (int) getResources().getDimension(R.dimen.spacing_medium));

                group_label.setLayoutParams(lp_match_wrap);

                //add group title to the layou
                group_wrapper.addView(group_label);


                if (variant.getOptions() != null && variant.getOptions().size() > 0) {

                    if (variant.getType() != null && variant.getType().equalsIgnoreCase(Variant.ONE_OPTION)) {

                        Variant tempVariantOO = Realm.getDefaultInstance().copyFromRealm(variant);


                        /********* ONE_OPTION   *********/

                        for (Option option : variant.getOptions()) {

                            LinearLayout service_view_group = new LinearLayout(mContext);
                            service_view_group.setOrientation(LinearLayout.VERTICAL);
                            service_view_group.setLayoutParams(lp_match_wrap);

                            //choice  with price
                            LinearLayout linearLayout_376 = new LinearLayout(mContext);
                            linearLayout_376.setOrientation(LinearLayout.HORIZONTAL);
                            linearLayout_376.setLayoutParams(lp_match_wrap);


                            //radio
                            RadioButton radioBtn = new RadioButton(mContext);
                            LinearLayout.LayoutParams lp_rb = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                            lp_rb.weight = 1;
                            radioBtn.setLayoutParams(lp_rb);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                                radioBtn.setButtonTintList(ContextCompat.getColorStateList(context, R.color.colorPrimary));

                            //dynamic content
                            radioBtn.setText(option.getLabel());
                            radioBtn.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
                            radioBtn.setTag(option.getId());
                            radioBtn.setId(option.getId());

                            //todo: action click listener
                            radioBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {


                                    for (int i = 0; i < variant.getOptions().size(); i++) {
                                        if (view.getId() == variant.getOptions().get(i).getId()) {
                                            RealmList<Option> tempOption = new RealmList<>();
                                            tempOption.add(variant.getOptions().get(i));
                                            tempVariantOO.setOptions(tempOption);


                                            if (tempVariantOO.getOptions() != null && tempVariantOO.getOptions().size() > 0) {
                                                selectedOptions.remove(tempVariantOO);
                                                selectedOptions.add(tempVariantOO);
                                            }

                                            ((RadioButton) getView().findViewWithTag(variant.getOptions().get(i).getId())).setChecked(true);
                                        } else {
                                            ((RadioButton) getView().findViewWithTag(variant.getOptions().get(i).getId())).setChecked(false);
                                        }
                                    }


                                }
                            });

                            linearLayout_376.addView(radioBtn);

                            service_view_group.addView(linearLayout_376);


                            //item description
                            if (option.getDescription() != null && !option.getDescription().trim().equals("")) {
                                //Linear layout checkbox
                                LinearLayout.LayoutParams description_ll_M_M = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                description_ll_M_M.leftMargin = (int) getResources().getDimension(R.dimen.spacing_xlarge);
                                //choice 1 linearlayout
                                LinearLayout description_ll = new LinearLayout(mContext);
                                description_ll.setOrientation(LinearLayout.HORIZONTAL);
                                description_ll.setLayoutParams(description_ll_M_M);

                                //choice 1 price
                                TextView description_txt = new TextView(mContext);
                                description_txt.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                                description_txt.setTextColor(getResources().getColor(R.color.grey_60));
                                description_txt.setTypeface(description_txt.getTypeface(), Typeface.ITALIC);
                                description_txt.setLayoutParams(lp_wrap_wrap);
                                description_ll.addView(description_txt);

                                //dynamic content
                                description_txt.setText(option.getDescription());

                                service_view_group.addView(description_ll);
                            }

                            group_wrapper.addView(service_view_group);


                        }

                    } else if (variant.getType() != null && variant.getType().equalsIgnoreCase(Variant.MULTI_OPTIONS)) {
                        /********* MULTI_OPTIONS   *********/

                        Variant variantMO = Realm.getDefaultInstance().copyFromRealm(variant);
                        variantMO.getOptions().clear();


                        for (Option option : variant.getOptions()) {

                            LinearLayout service_view_group_mo = new LinearLayout(mContext);
                            service_view_group_mo.setOrientation(LinearLayout.VERTICAL);
                            service_view_group_mo.setLayoutParams(lp_match_wrap);

                            //Linear layout checkbox
                            LinearLayout.LayoutParams checkBox_params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                            //choice 1 linearlayout
                            LinearLayout linearLayout_ch_1 = new LinearLayout(mContext);
                            linearLayout_ch_1.setOrientation(LinearLayout.HORIZONTAL);
                            linearLayout_ch_1.setLayoutParams(lp_match_wrap);


                            //choice 1 checkbox
                            CheckBox checkBox = new CheckBox(mContext);
                            checkBox_params.weight = 1;

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                                checkBox.setButtonTintList(ContextCompat.getColorStateList(context, R.color.colorPrimary));


                            //dynamic content
                            checkBox.setText(option.getLabel());
                            checkBox.setTag(option.getId());
                            checkBox.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
                            checkBox.setId(option.getId());


                            //click listener
                            checkBox.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                        variantMO.getOptions().stream()
                                                .filter(option1 -> option1.getId() == option.getId())
                                                .findFirst()
                                                .map(p -> {
                                                    variantMO.getOptions().remove(p);
                                                    return p;
                                                });
                                    }


                                    if (((CheckBox) view).isChecked()) {

                                        variantMO.getOptions().add(option);
                                        //calculate the amount
                                        if (option.getValue() > 0)
                                            customPrice = (float) (customPrice + option.getValue());

                                    } else {


                                        //calculate the amount
                                        if (option.getValue() > 0)
                                            customPrice = (float) (customPrice - option.getValue());
                                    }

                                    if (variantMO.getOptions() != null && variantMO.getOptions().size() > 0) {
                                        selectedOptions.remove(variantMO);
                                        selectedOptions.add(variantMO);
                                    }

                                }
                            });


                            checkBox.setLayoutParams(checkBox_params);

                            linearLayout_ch_1.addView(checkBox);

                            //choice 1 price
                            TextView checkBox_price = new TextView(mContext);
                            checkBox_price.setText(String.format(getContext().getString(R.string.variant_additional_cost), option.getParsed_value()));
                            checkBox_price.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
                            checkBox_price.setTextColor(getResources().getColor(R.color.colorPrimary));
                            checkBox_price.setTypeface(checkBox_price.getTypeface(), Typeface.BOLD);
                            checkBox_price.setLayoutParams(lp_wrap_wrap);
                            checkBox_price.setVisibility(View.GONE);
                            linearLayout_ch_1.addView(checkBox_price);
                            service_view_group_mo.addView(linearLayout_ch_1);


                            if (option.getDescription() != null && !option.getDescription().trim().equals("")) {
                                //Linear layout checkbox
                                LinearLayout.LayoutParams description_ll_M_M = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                description_ll_M_M.leftMargin = (int) getResources().getDimension(R.dimen.spacing_xlarge);
                                //choice 1 linearlayout
                                LinearLayout description_ll = new LinearLayout(mContext);
                                description_ll.setOrientation(LinearLayout.HORIZONTAL);
                                description_ll.setLayoutParams(description_ll_M_M);

                                //choice 1 price
                                TextView description_txt = new TextView(mContext);
                                description_txt.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                                description_txt.setTextColor(getResources().getColor(R.color.grey_60));
                                description_txt.setTypeface(description_txt.getTypeface(), Typeface.ITALIC);
                                description_txt.setLayoutParams(lp_wrap_wrap);
                                description_ll.addView(description_txt);


                                //dynamic content
                                description_txt.setText(option.getDescription());

                                service_view_group_mo.addView(description_ll);

                            }

                            group_wrapper.addView(service_view_group_mo);

                        }


                    }
                }

                frame_content.addView(group_wrapper);
            }


        } else {
            Toast.makeText(context, getString(R.string.no_service_found_for_this_store), Toast.LENGTH_LONG).show();

            (new Handler()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    getActivity().finish();
                }
            }, 1000);

        }

    }


}
