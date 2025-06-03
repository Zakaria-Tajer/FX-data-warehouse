package com.data.warehouse.controller;


import com.data.warehouse.dto.ResultsDto;
import com.data.warehouse.services.DealService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;



@RestController
@RequestMapping("/api/import")
@RequiredArgsConstructor
public class DealController {


    private final DealService dealService;

    @PostMapping
    public ResponseEntity<ResultsDto> importDeals(@RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(dealService.importCsv(file));

    }
}







