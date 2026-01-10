package com.amaravathi.tradeidentity.domain.formData;


import com.amaravathi.tradeidentity.api.admin.dto.RoleResponseResponseDto;
import com.amaravathi.tradeidentity.api.formData.dto.CountryResponseDto;
import com.amaravathi.tradeidentity.api.formData.dto.ProductTypeResponseDto;
import com.amaravathi.tradeidentity.domain.user.UserServiceUtil;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.amaravathi.tradeidentity.domain.user.UserServiceUtil.mapCountryEntityToCountryDto;

@Service
public class FormDataService {

    private final CountryRepository countryRepository;

    private final ProductTypeRepository productTypeRepository;

    public FormDataService(CountryRepository countryRepository,
                           ProductTypeRepository productTypeRepository){
        this.countryRepository = countryRepository;
        this.productTypeRepository = productTypeRepository;
    }


    public List<CountryResponseDto> getAllCountries() {
      List<CountryResponseDto> countryDtos =
              Optional.ofNullable(countryRepository.findAll())
                      .orElse(List.of())
                      .stream()
                      .filter(Objects::nonNull)
                      .map(UserServiceUtil::mapCountryEntityToCountryDto)
                      .filter(Objects::nonNull)
                      .toList();
        return countryDtos;
    }

    public List<ProductTypeResponseDto> getAllProductTypes() {
        List<ProductTypeResponseDto> productTypeResponseDtos =
                Optional.ofNullable(productTypeRepository.findAll())
                        .orElse(List.of())
                        .stream()
                        .filter(Objects::nonNull)
                        .map(UserServiceUtil::mapProductTypeEntityToProductTypeDto)
                        .filter(Objects::nonNull)
                        .toList();
        return productTypeResponseDtos;
    }
}
