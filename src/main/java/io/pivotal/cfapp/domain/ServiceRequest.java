package io.pivotal.cfapp.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor(access=AccessLevel.PACKAGE)
@NoArgsConstructor(access=AccessLevel.PACKAGE)
public class ServiceRequest {

    private String organization;
    private String space;
    private String serviceName;
    
    public static ServiceRequestBuilder from(ServiceRequest request) {
        return ServiceRequest
                .builder()
                    .organization(request.getOrganization())
                    .space(request.getSpace())
                    .serviceName(request.getServiceName());
    }
}
