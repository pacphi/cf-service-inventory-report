package io.pivotal.cfapp.domain;

import lombok.Data;

@Data
public class ServiceCount {
    
    private String service;
    private long total;
    
    public String toCsv() {
        return String.join(",", service, String.valueOf(total));
    }
    
    public static String headers() {
        return String.join(",", "service", "total");
    }
}
