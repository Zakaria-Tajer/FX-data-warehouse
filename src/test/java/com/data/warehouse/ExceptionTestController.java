package com.data.warehouse;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class ExceptionTestController {

    @GetMapping("/illegal")
    public String throwIllegalArg() {
        throw new IllegalArgumentException("Invalid parameter");
    }

    @GetMapping("/general")
    public String throwGenericException() {
        throw new RuntimeException("Unexpected error");
    }
}
