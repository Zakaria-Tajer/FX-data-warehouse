package com.data.warehouse.models;


import com.data.warehouse.dto.DealsDto;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

import java.time.LocalDateTime;

@Entity
@Table(name = "deals", uniqueConstraints = @UniqueConstraint(columnNames = "dealId"))
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Deal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String dealId;

    @Column(nullable = false, length = 3)
    private String fromCurrency;

    @Column(nullable = false, length = 3)
    private String toCurrency;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false)
    private BigDecimal amount;


    public static Deal toEntity(DealsDto dto) {
        return new Deal(
                null,
                dto.getDealId(),
                dto.getFromCurrency().toUpperCase(),
                dto.getToCurrency().toUpperCase(),
                LocalDateTime.parse(dto.getTimestamp()),
                dto.getAmount()
        );
    }
}


