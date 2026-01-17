package com.amaravathi.tradeidentity.domain.user;

import com.amaravathi.tradeidentity.api.auth.dto.SignUpRequestDto;
import com.amaravathi.tradeidentity.api.formData.dto.CountryResponseDto;
import com.amaravathi.tradeidentity.api.formData.dto.ProductTypeResponseDto;
import com.amaravathi.tradeidentity.api.admin.dto.RoleResponseResponseDto;
import com.amaravathi.tradeidentity.domain.formData.Country;
import com.amaravathi.tradeidentity.domain.formData.ProductType;
import com.amaravathi.tradeidentity.domain.role.Role;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

public class UserServiceUtil {

    public static CountryResponseDto mapCountryEntityToCountryDto(Country country){

        return Optional.ofNullable(country)
                        .map(country1 -> CountryResponseDto.builder()
                                .id(country1.getId())
                                .name(country1.getName())
                                .currency(country1.getCurrency())
                                .build()
                        )
                        .orElse(null);
    }

    public static ProductTypeResponseDto mapProductTypeEntityToProductTypeDto(ProductType productType){

        return Optional.ofNullable(productType)
                .map(productType1 -> ProductTypeResponseDto.builder()
                        .id(productType1.getId())
                        .category(productType1.getCategory())
                        .code(productType1.getCode())
                        .name(productType1.getName())
                        .hsCode(productType1.getHsCode())
                        .build()
                )
                .orElse(null);

    }

    public static List<RoleResponseResponseDto> mapRoleEntityToRoleResponseDto(List<Role> roles) {
        return roles.stream()
                .map(role1 -> RoleResponseResponseDto.builder()
                        .id(role1.getId())
                        .code(role1.getCode())
                        .name(role1.getName())
                        .description(role1.getDescription())
                        .build()
                )
                .toList();
    }

    public static AppUser convertRequestDtoToUserEntity(SignUpRequestDto req, PasswordEncoder passwordEncoder) {
        AppUser user = new AppUser();
        user.setEmail(req.getEmail());
        user.setFullName(req.getFullName());
        user.setPhone(req.getPhone());
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        user.setStatus(UserStatus.ENROLLED);
        user.setResidenceCountry(req.getResidenceCountry());
        user.setCity(req.getCity());
        user.setPreferredLanguage(req.getPreferredLanguage());
        user.setOccupation(req.getOccupation());
        user.setInterest(req.getInterest());
        user.setPreviousTradingExposure(req.getPreviousTradingExposure());
        user.setTermsAccepted(req.isTermsAccepted());
        user.setCommunicationConsent(req.isCommunicationConsent());
        return user;
    }
}
