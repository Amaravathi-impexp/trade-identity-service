package com.amaravathi.tradeidentity.domain.formData;

import com.amaravathi.tradeidentity.api.formData.dto.CountryResponseDto;
import com.amaravathi.tradeidentity.api.formData.dto.ProductTypeResponseDto;
import com.amaravathi.tradeidentity.common.TradeIdentityException;
import com.amaravathi.tradeidentity.domain.user.UserServiceUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class FormDataService {

    private final CountryRepository countryRepository;
    private final ProductTypeRepository productTypeRepository;

    @Transactional(readOnly = true)
    public List<CountryResponseDto> getAllCountries() {
        log.info("Fetching all countries");

        try {
            List<Country> countries = countryRepository.findAll();
            log.debug("Found {} countries", countries.size());

            if (countries.isEmpty()) return List.of();

            return countries.stream()
                    .filter(Objects::nonNull)
                    .map(UserServiceUtil::mapCountryEntityToCountryDto)
                    .filter(Objects::nonNull)
                    .toList();

        } catch (DataAccessException dae) {
            log.error("Database error while fetching countries", dae);
            throw new TradeIdentityException("Database error while fetching countries", dae);
        }
    }

    @Transactional(readOnly = true)
    public List<ProductTypeResponseDto> getAllProductTypes() {
        log.info("Fetching all product types");

        try {
            List<ProductType> types = productTypeRepository.findAll();
            log.debug("Found {} product types", types.size());

            if (types.isEmpty()) return List.of();

            return types.stream()
                    .filter(Objects::nonNull)
                    .map(UserServiceUtil::mapProductTypeEntityToProductTypeDto)
                    .filter(Objects::nonNull)
                    .toList();

        } catch (DataAccessException dae) {
            log.error("Database error while fetching product types", dae);
            throw new TradeIdentityException("Database error while fetching product types", dae);
        }
    }
}