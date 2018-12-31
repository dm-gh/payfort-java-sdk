package com.payfort.sdk.request.converter;

import com.payfort.sdk.parameter.FortParameter;


/**
 * Interface which give you ability to override implementation of json serialization and parsing
 */
public interface RequestConverter {
    /**
     * @param parameter
     * @return request which contain all parameter values
     */
    String serialize(FortParameter parameter);

    /**
     * @param rawContent
     * @return fort parameter in raw content
     */
    FortParameter parse(String rawContent);
}
