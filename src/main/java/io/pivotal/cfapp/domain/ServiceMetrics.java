package io.pivotal.cfapp.domain;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

public class ServiceMetrics {

    private static LocalDate NOW = LocalDate.now();
    
    private List<ServiceDetail> detail;
    
    public ServiceMetrics(List<ServiceDetail> detail) {
        this.detail = detail;
    }
    
    public static String updatedHeaders() {
        return "last updated,services total";
    }
    
    public Integer totalServices() {
        return detail.size();
    }
    
    public Integer updatedInLastDay() {
        return detail
                .stream()
                    .filter(
                        i -> i.getLastUpdated() != null && 
                        ChronoUnit.DAYS.between(i.getLastUpdated().toLocalDate(), NOW) <= 1
                    )
                    .collect(Collectors.toList())
                    .size();
    }
    
    public Integer updatedInLastWeek() {
        return detail
                .stream()
                    .filter(
                        i -> i.getLastUpdated() != null &&
                        ChronoUnit.WEEKS.between(i.getLastUpdated().toLocalDate(), NOW) <= 1 &&
                        ChronoUnit.DAYS.between(i.getLastUpdated().toLocalDate(), NOW) > 1
                    )
                    .collect(Collectors.toList())
                    .size();
    }
    
    public Integer updatedInLastMonth() {
        return detail
                .stream()
                    .filter(
                    		i -> i.getLastUpdated() != null &&
                        ChronoUnit.MONTHS.between(i.getLastUpdated().toLocalDate(), NOW) <= 1 &&
                        ChronoUnit.WEEKS.between(i.getLastUpdated().toLocalDate(), NOW) > 1
                    )
                    .collect(Collectors.toList())
                    .size();
    }
    
    public Integer updatedInLastThreeMonths() {
        return detail
                .stream()
                    .filter(
                    		i -> i.getLastUpdated() != null &&
                        ChronoUnit.MONTHS.between(i.getLastUpdated().toLocalDate(), NOW) <= 3 &&
                        ChronoUnit.MONTHS.between(i.getLastUpdated().toLocalDate(), NOW) > 1
                    )
                    .collect(Collectors.toList())
                    .size();
    }
    
    public Integer updatedInLastSixMonths() {
        return detail
                .stream()
                    .filter(
                    		i -> i.getLastUpdated() != null &&
                        ChronoUnit.MONTHS.between(i.getLastUpdated().toLocalDate(), NOW) <= 6 &&
                        ChronoUnit.MONTHS.between(i.getLastUpdated().toLocalDate(), NOW) > 3
                    )
                    .collect(Collectors.toList())
                    .size();
    }
    
    public Integer updatedInLastYear() {
        return detail
                .stream()
                    .filter(
                    		i -> i.getLastUpdated() != null &&
                        ChronoUnit.YEARS.between(i.getLastUpdated().toLocalDate(), NOW) <= 1 &&
                        ChronoUnit.MONTHS.between(i.getLastUpdated().toLocalDate(), NOW) > 6
                    )
                    .collect(Collectors.toList())
                    .size();
    }
    
    public Integer updatedBeyondOneYear() {
        return detail
                .stream()
                    .filter(
                    		i -> i.getLastUpdated() != null &&
                        ChronoUnit.YEARS.between(i.getLastUpdated().toLocalDate(), NOW) > 1
                    )
                    .collect(Collectors.toList())
                    .size();
    }
}
