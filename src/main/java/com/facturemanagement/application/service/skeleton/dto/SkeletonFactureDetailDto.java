package com.facturemanagement.application.service.skeleton.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import com.facturemanagement.application.service.skeleton.model.SkeletonFactureType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkeletonFactureDetailDto {
    private Long id;
    private Long factCode;
    private String entId;
    private Long thId;
    private Set<SkeletonProductDto> products;
    private String totalValue;
    private String totalPay;
    private String pendingValue;
    private LocalDate expirationDate;
    private Long accountingAccount;
    private SkeletonFactureType factureType;
    private LocalDateTime createdAt;
}
