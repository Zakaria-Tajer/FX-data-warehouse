package com.data.warehouse.dto;

import com.opencsv.bean.CsvBindByName;
import lombok.Data;

import java.math.BigDecimal;


@Data
public class DealsDto {

    @CsvBindByName(column = "dealId", required = true)
    private String dealId;

    @CsvBindByName(column = "fromCurrency", required = true)
    private String fromCurrency;

    @CsvBindByName(column = "toCurrency", required = true)
    private String toCurrency;

    @CsvBindByName(column = "timestamp", required = true)
    private String timestamp;

    @CsvBindByName(column = "amount", required = true)
    private BigDecimal amount;
}
