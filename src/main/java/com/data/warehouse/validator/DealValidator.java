package com.data.warehouse.validator;


import com.data.warehouse.config.MessageResolver;
import com.data.warehouse.models.Deal;
import com.data.warehouse.utils.CurrencyUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Component
@Slf4j
@RequiredArgsConstructor
public class DealValidator {


    private final MessageResolver messageSourceResolver;
    private final CurrencyUtil currencyUtil;


    public String validate(Deal deal) {
        if (StringUtils.isBlank(deal.getDealId())) {
            return messageSourceResolver.get("error.dealid.required");
        }

        if (StringUtils.isBlank(deal.getFromCurrency()) || !currencyUtil.isValid(deal.getFromCurrency())) {
            log.warn("Invalid fromCurrency: {}", deal.getFromCurrency());
            return messageSourceResolver.get("error.fromcurrency.invalid");
        }

        if (StringUtils.isBlank(deal.getToCurrency()) || !currencyUtil.isValid(deal.getToCurrency())) {
            log.warn("Invalid toCurrency: {}", deal.getToCurrency());
            return messageSourceResolver.get("error.tocurrency.invalid");
        }

        if (deal.getTimestamp() == null || deal.getTimestamp().isAfter(LocalDateTime.now())) {
            log.warn("Invalid or future timestamp: {}", deal.getTimestamp());
            return messageSourceResolver.get("error.timestamp.invalid");
        }

        if (deal.getAmount() == null || deal.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Amount must be a positive number: {}", deal.getAmount());
            return messageSourceResolver.get("error.amount.invalid");
        }

        return null;
    }

}
