package com.payfort.sdk;

import com.payfort.sdk.exception.FortException;
import com.payfort.sdk.parameter.FortKey;
import com.payfort.sdk.parameter.FortParameter;
import com.payfort.sdk.signture.SignatureGenerator;
import com.payfort.sdk.types.FortCurrency;
import com.payfort.sdk.types.FortEnvironment;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.UUID;

import static com.payfort.sdk.FortKeys.SIGNATURE;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Entry point for all fort operation, it required fort configuration instance
 * to start using it
 */
public class FortSDK {

    private final FortAccount fortAccount;
    private final FortEnvironment environment;
    private final SignatureGenerator signatureGenerator;
    private final FortRequestConfiguration fortRequestConfiguration;

    public FortSDK(@Nonnull FortAccount fortAccount, @Nonnull FortEnvironment environment, @Nonnull FortRequestConfiguration fortRequestConfiguration) {
        this.fortAccount = fortAccount;
        this.signatureGenerator = new SignatureGenerator(fortAccount.getHashingFunction(), UTF_8);
        this.environment = environment;
        this.fortRequestConfiguration = fortRequestConfiguration;
    }

    public FortSDK(@Nonnull FortAccount fortAccount, @Nonnull FortEnvironment environment) {
        this.fortAccount = fortAccount;
        this.signatureGenerator = new SignatureGenerator(fortAccount.getHashingFunction(), UTF_8);
        this.environment = environment;
        this.fortRequestConfiguration = new FortRequestConfiguration();
    }

    /**
     * Call fort service using parameter, you can register handler to handle
     * response these method will add default merchant identifier if missing these
     * method will add default language if missing these method will add default
     * access code if missing these method will add default signature if missing
     *
     * @param parameter
     * @param handler
     */
    public void callApi(FortParameter parameter, FortResponseHandler handler) {
        try {
            FortParameter fortParameterResponse = callApi(parameter);
            handler.handleResponse(fortParameterResponse);
        } catch (RuntimeException ex) {
            handler.handleFailure(parameter, new FortException(ex));
        }
    }

    /**
     * Call fort service using parameter, you can register handler to handle
     * response these method will add default merchant identifier if missing these
     * method will add default language if missing these method will add default
     * access code if missing these method will add default signature if missing
     *
     * @param parameter
     * @return FortParameter which represent response from source
     */
    public FortParameter callApi(FortParameter parameter) {
        addBasicParameters(parameter);
        addSigntureIfMissing(parameter);

        return toFortParameter(fortRequestConfiguration.getGateway(environment)
                .send(fortRequestConfiguration.getRequestConverter().serialize(parameter)));
    }

    /**
     * Convert normal text to fort parameter using request converter
     *
     * @param contentBody
     * @return
     */
    private FortParameter toFortParameter(String contentBody) {
        return fortRequestConfiguration.getRequestConverter().parse(contentBody);
    }

    /**
     * Convert Fort parameter to normal text request converter
     *
     * @param fortParameter
     * @return
     */
    public String toRawString(FortParameter fortParameter) {
        return fortRequestConfiguration.getRequestConverter().serialize(fortParameter);
    }

    /**
     * Calculate signature using all parameter and request sha phrase
     *
     * @param parameters
     * @return
     */
    public String calculateRequestSignature(FortParameter parameters) {
        return signatureGenerator.generate(this.fortAccount.getShaRequestPhrase(), parameters);
    }

    /**
     * Calculate signature using all parameter and response sha phrase
     *
     * @param parameters
     * @return
     */
    public String calculateResponseSignature(FortParameter parameters) {
        return signatureGenerator.generate(this.fortAccount.getShaResponsePhrase(), parameters);
    }

    /**
     * validate the FORT response signature.
     *
     * @param parameters
     * @return
     */
    public boolean validateResponseSignature(FortParameter parameters) {
        FortParameter parameterWithoutSig = new FortParameter();
        parameters.iterator(
                (k, v) -> {
                    if (!SIGNATURE.equals(k)) {
                        parameterWithoutSig.add(k, v);
                    }
                }
        );
        String signature = calculateResponseSignature(parameterWithoutSig);
        return signature.equalsIgnoreCase((String) parameters.get(SIGNATURE));
    }

    /**
     * @param currencyCode
     * @return decimal point using currency code
     */
    public int currencyDecimalPoints(String currencyCode) {
        return FortCurrency.getByCode(currencyCode).decimalPoints();
    }

    /**
     * @param currencyCode
     * @return Fort Currency using currency code
     */
    public FortCurrency payfortCurrency(String currencyCode) {
        return FortCurrency.getByCode(currencyCode);
    }

    /**
     * @param amount
     * @param currencyCode
     * @return calculate amount with decimal point required conversion
     */
    public double convertFortAmount(double amount, FortCurrency currencyCode) {
        return currencyCode.fortAmount(amount);
    }

    /**
     * @param amount
     * @param currencyCode
     * @return
     */
    public double castAmountFromFort(double amount, FortCurrency currencyCode) {
        return currencyCode.castFortAmount(amount);
    }

    /**
     * @return almost random merchant reference
     */
    public String generateRandomMerchantReference() {
        String uuid = UUID.randomUUID().toString();
        if (uuid.length() > 39) {
            return uuid.substring(0, 39);
        } else {
            return uuid;
        }
    }

    /**
     * @param parameter
     * @return convert map to fort parameter
     */
    public static FortParameter toFortParameter(Map<String, String> parameter) {
        FortParameter fortParameter = new FortParameter();
        for (Map.Entry<String, String> entry : parameter.entrySet()) {
            fortParameter.add(new FortKey(entry.getKey()), entry.getValue());
        }
        return fortParameter;
    }

    private void addSigntureIfMissing(FortParameter parameter) {
        if (parameter.get(SIGNATURE) == null) {
            parameter.add(SIGNATURE, calculateRequestSignature(parameter));
        }
    }

    private FortParameter addBasicParameters(FortParameter parameter) {
        parameter.add(FortKeys.ACCESS_CODE, this.fortAccount.getAccessCode());
        parameter.add(FortKeys.MERCHANT_IDENTIFIER, this.fortAccount.getMerchantIdentifier());
        if (useDefualtLanguageFromRequestConfigs(parameter)) {
            parameter.add(FortKeys.LANGUAGE, this.fortRequestConfiguration.getLanguage());
        }
        return parameter;
    }

    private boolean useDefualtLanguageFromRequestConfigs(FortParameter parameter) {
        return parameter.get(FortKeys.LANGUAGE) == null;
    }
}
