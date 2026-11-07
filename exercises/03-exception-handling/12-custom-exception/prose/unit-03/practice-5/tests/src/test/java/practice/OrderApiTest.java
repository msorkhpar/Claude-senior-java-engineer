package practice;

import org.junit.jupiter.api.Test;

import practice.OrderApi.ApiError;
import practice.OrderApi.ErrorCode;
import practice.OrderApi.OrderException;

import static org.assertj.core.api.Assertions.assertThat;

class OrderApiTest {

    @Test
    void eachCodeGetsItsDocumentedStatus() {
        assertThat(OrderApi.toResponse(new OrderException(ErrorCode.OUT_OF_STOCK, "sku KB-204 out of stock")))
                .extracting(ApiError::status, ApiError::code).containsExactly(409, "OUT_OF_STOCK");
        assertThat(OrderApi.toResponse(new OrderException(ErrorCode.COUPON_EXPIRED, "coupon SPRING24 expired")))
                .extracting(ApiError::status, ApiError::code).containsExactly(422, "COUPON_EXPIRED");
        assertThat(OrderApi.toResponse(new OrderException(ErrorCode.ORDER_LIMIT_REACHED, "limit of 5 orders reached")))
                .extracting(ApiError::status, ApiError::code).containsExactly(429, "ORDER_LIMIT_REACHED");
    }

    @Test
    void rewordingAMessageDoesNotChangeTheResponse() {
        ApiError before = OrderApi.toResponse(new OrderException(ErrorCode.OUT_OF_STOCK, "sku KB-204 out of stock"));
        ApiError after = OrderApi.toResponse(new OrderException(ErrorCode.OUT_OF_STOCK,
                "0 units of KB-204 left in warehouse wh-north"));

        assertThat(after).isEqualTo(before);
    }

    @Test
    void theResponseCarriesNothingFromTheInternalMessage() {
        OrderException e = new OrderException(ErrorCode.ORDER_LIMIT_REACHED,
                "customer 88123 placed 6 orders since 2024-05-02T00:00Z, limit 5 (risk rule R-17)");

        assertThat(OrderApi.toResponse(e).detail())
                .doesNotContain("88123", "R-17")
                .isEqualTo("You've reached today's order limit.");
    }
}
