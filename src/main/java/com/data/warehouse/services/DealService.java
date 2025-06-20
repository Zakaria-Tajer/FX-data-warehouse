package com.data.warehouse.services;

import com.data.warehouse.config.MessageResolver;
import com.data.warehouse.dto.DealsDto;
import com.data.warehouse.dto.ResultsDto;
import com.data.warehouse.models.Deal;
import com.data.warehouse.repository.DealRepository;
import com.data.warehouse.validator.DealValidator;
import com.opencsv.CSVParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.springframework.web.multipart.MultipartFile;


import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;


@Service
@RequiredArgsConstructor
@Slf4j
public class DealService {


    private final DealRepository dealRepository;
    private final DealValidator validator;
    private final MessageResolver messageSourceResolver;


    public ResultsDto importCsv(MultipartFile file) throws IOException {


        int successCount = 0, duplicateCount = 0, invalidCount = 0;
        List<String> errorMessages = new ArrayList<>();

        if (file.isEmpty()) {
            log.warn(messageSourceResolver.get("error.csv.empty"));
            throw new IllegalArgumentException(messageSourceResolver.get("error.csv.empty"));
        }


        String contentType = file.getContentType();
        String fileName = file.getOriginalFilename();

        if (contentType == null || (!contentType.equals("text/csv") && !contentType.equals("application/vnd.ms-excel"))
                || (fileName != null && !fileName.toLowerCase().endsWith(".csv"))) {
            log.warn(messageSourceResolver.get("error.csv.only"));
            throw new IllegalArgumentException(messageSourceResolver.get("error.csv.only"));
        }

        try (Reader reader = new InputStreamReader(file.getInputStream())) {
            CsvToBean<DealsDto> csvToBean = new CsvToBeanBuilder<DealsDto>(reader)
                    .withType(DealsDto.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();


            List<DealsDto> dtoList;

            try {
                dtoList = csvToBean.parse();
            } catch (RuntimeException e) {
                log.error("Error parsing CSV file", e);
                throw new IllegalArgumentException(messageSourceResolver.get("error.csv.invalid.form") + e.getMessage(), e);
            }

            Set<String> appearedID = new HashSet<>();
            for (DealsDto dto : dtoList) {
                String reason = validator.validate(Deal.toEntity(dto));

                if (!appearedID.add(dto.getDealId())) {
                    errorMessages.add("Duplicate deal in file [" + dto.getDealId() + "] ignored.");
                    duplicateCount++;
                    continue;
                }

                if (reason != null) {
                    errorMessages.add("Invalid deal [" + dto.getDealId() + "]: " + reason);
                    invalidCount++;
                    continue;
                }

                if (dealRepository.existsByDealId(dto.getDealId())) {
                    errorMessages.add("Duplicate deal [" + dto.getDealId() + "] ignored.");
                    duplicateCount++;
                    continue;
                }

                Deal deal = Deal.toEntity(dto);
                dealRepository.save(deal);
                successCount++;
            }

            log.info("CSV import completed: {} saved, {} duplicates, {} invalid.",
                    successCount, duplicateCount, invalidCount);
            return new ResultsDto(successCount, duplicateCount, invalidCount, errorMessages);


        }
    }


}
