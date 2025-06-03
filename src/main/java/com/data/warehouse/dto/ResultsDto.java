package com.data.warehouse.dto;


import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ResultsDto {

    private int saved;
    private int duplicates;
    private int invalid;
    private List<String> errors;
}
