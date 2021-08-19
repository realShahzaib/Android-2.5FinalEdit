package com.hybridproduct.apps.nearbystores.utils;

import com.hybridproduct.apps.nearbystores.classes.Currency;

import java.text.DecimalFormat;

/**
 * Created by Droideve on 1/26/2018.
 */

public class OfferUtils {


    public static String parseCurrencyFormat(float price, Currency cData) {

        DecimalFormat decim = new DecimalFormat("0.00");

        String ps = decim.format(price);

        if (cData != null) {
            switch (cData.getFormat()) {
                case 1:
                    return cData.getSymbol() + ps;
                case 2:
                    return ps + cData.getSymbol();
                case 3:
                    return cData.getSymbol() + " " + ps;
                case 4:
                    return ps + " " + cData.getSymbol();
                case 5:
                    return String.valueOf(ps);
                case 6:
                    return cData.getSymbol() + ps + " " + cData.getCode();
                case 7:
                    return cData.getSymbol() + ps;
                case 8:
                    return ps + cData.getCode();
            }

        }

        return String.valueOf(price);


    }

}
