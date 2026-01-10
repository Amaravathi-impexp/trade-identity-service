package com.amaravathi.tradeidentity.api.formData;

import com.amaravathi.tradeidentity.api.admin.dto.RoleResponseResponseDto;
import com.amaravathi.tradeidentity.api.formData.dto.CountryResponseDto;
import com.amaravathi.tradeidentity.api.formData.dto.ProductTypeResponseDto;
import com.amaravathi.tradeidentity.domain.formData.FormDataService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/trade-identity/v1/formData")
public class FormDataController {

    private final FormDataService formDataService;

    public FormDataController(FormDataService formDataService) {
        this.formDataService = formDataService;
    }
    @GetMapping("/countries")
    public ResponseEntity<List<CountryResponseDto>> getAllCountries() {
        return ResponseEntity.status(HttpStatus.OK).body(formDataService.getAllCountries());
    }

    @GetMapping("/productTypes")
    public ResponseEntity<List<ProductTypeResponseDto>> getAllProductTypes() {
        return ResponseEntity.status(HttpStatus.OK).body(formDataService.getAllProductTypes());
    }
}
