package com.protechone.dto.dashboard;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RecentTransactionResponse(
        String type,       // SALE, PURCHASE, EXPENSE
        String reference,
        String partyName,
        BigDecimal amount,
        String status,
        LocalDateTime date
) {}
