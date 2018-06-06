package io.pivotal.cfapp.task;

import java.util.List;

import org.springframework.context.ApplicationEvent;

import io.pivotal.cfapp.domain.ServiceDetail;
import io.pivotal.cfapp.domain.ServiceCount;
import io.pivotal.cfapp.domain.OrganizationCount;

public class ServiceInfoRetrievedEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    private final List<ServiceDetail> detail;
    private final List<ServiceCount> serviceCounts;
    private final List<OrganizationCount> organizationCounts;
    
    public ServiceInfoRetrievedEvent(
            Object source, 
            List<ServiceDetail> detail, 
            List<ServiceCount> serviceCounts,
            List<OrganizationCount> organizationCounts
            ) {
        super(source);
        this.detail = detail;
        this.serviceCounts = serviceCounts;
        this.organizationCounts = organizationCounts;
    }
    
    public List<ServiceDetail> getDetail() {
        return detail;
    }
    
    public List<ServiceCount> getServiceCounts() {
        return serviceCounts;
    }
    
    public List<OrganizationCount> getOrganizationCounts() {
        return organizationCounts;
    }
    
}
