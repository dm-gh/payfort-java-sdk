package com.payfort.sdk.types;

import java.util.stream.Stream;

/**
 * Class represent Fort Currency
 */
public enum FortCurrency {
    AED("AED", 2), BHD("BHD", 3), KWD("KWD", 3),
    USD("USD", 2), JOD("JOD", 3), OMR("OMR", 3),
    LYD("LYD", 3), IQD("IQD", 3), TND("TND", 3);

    private final String currencyCode;
    private final int decimalPoints;

    FortCurrency(String currencyCode, int decimalPoints) {
        this.currencyCode = currencyCode;
        this.decimalPoints = decimalPoints;
    }

    /**
     * @param code
     * @return Fort Currency with same code
     */
    public static FortCurrency getByCode(String code) {
        return Stream.of(values()).filter(c -> c.currencyCode.equals(code)).findFirst().get();
    }

    /**
     * @return number of decimal places
     */
    public int decimalPoints() {
        return decimalPoints;
    }

    /**
     * @return currency code
     */
    public String currencyCode() {
        return currencyCode;
    }

    /**
     * @param amount
     * @return return amount * currency decimal point
     */
    public double fortAmount(double amount) {
        return amount * Math.pow(10.0, decimalPoints);
    }


    /**
     * @param amount
     * @return return amount / currency decimal point
     */
    public double castFortAmount(double amount) {
        return amount / (Math.pow(10.0, decimalPoints));
    }
}
