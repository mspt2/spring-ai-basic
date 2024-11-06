package com.samsungsds.springai.model;

public record ExchangeRateResponse(
        boolean isSuccess,
        String detailCode,
        String message,
        ExchangeResult result
) {
    public record ExchangeResult(
            String reutersCode,
            String symbolCode,
            String name,
            String description,
            String localTradedAt,
            String closePrice,
            String fluctuations,
            String fluctuationsRatio,
            FluctuationType fluctuationsType,
            String marketStatus,
            String unit,
            String calcPrice
    ) {}

    public record FluctuationType(
            String code,
            String text,
            String name
    ) {}
}