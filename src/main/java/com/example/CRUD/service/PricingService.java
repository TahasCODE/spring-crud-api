package com.example.CRUD.service;

import com.example.CRUD.DTO.*;
import com.example.CRUD.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

@Service
public class PricingService {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private record Tier(BigDecimal minSubtotal, BigDecimal percent) {}

    // volume discount, highest tier first
    private static final List<Tier> TIERS = List.of(
            new Tier(new BigDecimal("50000"), new BigDecimal("10")),
            new Tier(new BigDecimal("10000"), new BigDecimal("5")));

    private static final Map<String, BigDecimal> COUPONS = Map.of(
            "WELCOME5", new BigDecimal("5"),
            "SAVE15", new BigDecimal("15"));

    private final BigDecimal taxRatePercent;

    public PricingService(@Value("${app.pricing.tax-rate-percent:18}") BigDecimal taxRatePercent) {
        this.taxRatePercent = taxRatePercent;
    }

    public QuoteResponse quote(QuoteRequest request) {
        List<QuoteLine> lines = request.items().stream()
                .map(i -> new QuoteLine(i.name(), i.quantity(), i.unitPrice(),
                        i.unitPrice().multiply(BigDecimal.valueOf(i.quantity())).setScale(2, RoundingMode.HALF_UP)))
                .toList();
        BigDecimal subtotal = lines.stream().map(QuoteLine::lineTotal).reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal volumePercent = TIERS.stream()
                .filter(t -> subtotal.compareTo(t.minSubtotal()) >= 0)
                .map(Tier::percent).findFirst().orElse(BigDecimal.ZERO);

        BigDecimal couponPercent = BigDecimal.ZERO;
        String code = request.couponCode();
        if (code != null && !code.isBlank()) {
            couponPercent = COUPONS.get(code.trim().toUpperCase());
            if (couponPercent == null) {
                throw new BadRequestException("Unknown coupon code");
            }
        }

        // discounts never stack: the customer gets whichever is better
        boolean couponWins = couponPercent.compareTo(volumePercent) > 0;
        BigDecimal percent = couponWins ? couponPercent : volumePercent;
        String type = percent.signum() == 0 ? "NONE" : couponWins ? "COUPON" : "VOLUME";

        BigDecimal discount = subtotal.multiply(percent).divide(HUNDRED, 2, RoundingMode.HALF_UP);
        BigDecimal taxable = subtotal.subtract(discount);                       // discount comes BEFORE tax
        BigDecimal tax = taxable.multiply(taxRatePercent).divide(HUNDRED, 2, RoundingMode.HALF_UP);
        BigDecimal total = taxable.add(tax);

        return new QuoteResponse(lines, subtotal, type, percent, discount, taxable, taxRatePercent, tax, total);
    }
}