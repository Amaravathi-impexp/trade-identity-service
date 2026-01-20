package com.amaravathi.tradeidentity.api.formData;

import com.amaravathi.tradeidentity.api.formData.dto.CountryResponseDto;
import com.amaravathi.tradeidentity.api.formData.dto.ProductTypeResponseDto;
import com.amaravathi.tradeidentity.domain.formData.FormDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/trade-identity/v1/formData")
@Slf4j
@Validated
@RequiredArgsConstructor
public class FormDataController {

    private final FormDataService formDataService;

    @GetMapping("/countries")
    public ResponseEntity<List<CountryResponseDto>> getAllCountries() {
        log.info("GET /formData/countries");
        return ResponseEntity.status(HttpStatus.OK).body(formDataService.getAllCountries());
    }

    @GetMapping("/productTypes")
    public ResponseEntity<List<ProductTypeResponseDto>> getAllProductTypes() {
        log.info("GET /formData/productTypes");
        return ResponseEntity.status(HttpStatus.OK).body(formDataService.getAllProductTypes());
    }
}
