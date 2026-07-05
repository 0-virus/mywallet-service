package com.ragvirus.policy.application.normalization;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import org.springframework.stereotype.Component;

@Component
public class ApplicationStatusCalculator {

    private static final int CLOSING_SOON_DAYS = 7;

    public ApplicationStatus calculate(LocalDate startDate, LocalDate dueDate, boolean alwaysOpen) {
        LocalDate today = LocalDate.now();
        if (alwaysOpen) {
            return new ApplicationStatus("always_open", null);
        }
        if (startDate == null && dueDate == null) {
            return new ApplicationStatus("unknown", null);
        }
        if (startDate != null && today.isBefore(startDate)) {
            return new ApplicationStatus("scheduled", (int) ChronoUnit.DAYS.between(today, startDate));
        }
        if (dueDate != null && today.isAfter(dueDate)) {
            return new ApplicationStatus("closed", (int) ChronoUnit.DAYS.between(today, dueDate));
        }
        if (dueDate != null) {
            int dday = (int) ChronoUnit.DAYS.between(today, dueDate);
            if (dday <= CLOSING_SOON_DAYS) {
                return new ApplicationStatus("closing_soon", dday);
            }
            return new ApplicationStatus("open", dday);
        }
        return new ApplicationStatus("open", null);
    }
}
