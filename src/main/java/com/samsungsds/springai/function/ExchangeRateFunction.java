package com.samsungsds.springai.function;

import com.samsungsds.springai.model.ExchangeRateRequest;
import com.samsungsds.springai.model.ExchangeRateResponse;
import org.springframework.web.client.RestTemplate;
import java.util.function.Function;


public class ExchangeRateFunction implements Function<ExchangeRateRequest, String> {
    private static final String NAVER_EXCHANGE_API_URL =
            "https://m.stock.naver.com/front-api/marketIndex/productDetail?category=exchange&reutersCode=FX_USDKRW";
    private final RestTemplate restTemplate;

    public ExchangeRateFunction() {
        this.restTemplate = new RestTemplate();
    }

    @Override
    public String apply(ExchangeRateRequest request) {
        try {
            ExchangeRateResponse response = restTemplate.getForObject(NAVER_EXCHANGE_API_URL, ExchangeRateResponse.class);

            if (response == null || !response.isSuccess()) {
                return "Failed to fetch exchange rate data";
            }

            ExchangeRateResponse.ExchangeResult result = response.result();
            String direction = result.fluctuationsType().text();
            String fluctuation = result.fluctuations();
            String rate = result.closePrice();
            String time = result.localTradedAt().replace("T", " ").substring(0, 16);

            return String.format(
                    "현재 달러/원 환율: %s원 (%s %s원)\n거래 시각: %s",
                    rate,
                    direction,
                    fluctuation,
                    time
            );
        } catch (Exception e) {
            return "Exchange rate data fetch failed: " + e.getMessage();
        }
    }
}
