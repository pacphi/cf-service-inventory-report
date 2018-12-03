package io.pivotal.cfapp.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceCount {

    private String service;
    private String plan;
    private long total;

    // Assumes null values for service are instances of user_provided_service; likewise we'll set plan to default
    public String toCsv() {
        return String.join(",", service != null ? service: "user_provided_service", plan != null ? plan : "default" , String.valueOf(total));
    }

    public static String headers() {
        return String.join(",", "service", "plan", "total");
    }
}
