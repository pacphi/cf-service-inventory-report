package io.pivotal.cfapp.task;

import java.util.List;

import org.springframework.context.ApplicationEvent;

import io.pivotal.cfapp.domain.ServiceInstanceDetail;
import io.pivotal.cfapp.domain.ServiceCount;
import io.pivotal.cfapp.domain.OrganizationCount;

public class ServiceInstanceDetailRetrievedEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    private List<ServiceInstanceDetail> detail;
    private List<ServiceCount> serviceCounts;
    private List<OrganizationCount> organizationCounts;

    public ServiceInstanceDetailRetrievedEvent(Object source) {
        super(source);
    }

    public ServiceInstanceDetailRetrievedEvent detail(List<ServiceInstanceDetail> detail) {
        this.detail = detail;
        return this;
    }

    public ServiceInstanceDetailRetrievedEvent serviceCounts(List<ServiceCount> serviceCounts) {
        this.serviceCounts = serviceCounts;
        return this;
    }

    public ServiceInstanceDetailRetrievedEvent organizationCounts(List<OrganizationCount> organizationCounts) {
        this.organizationCounts = organizationCounts;
        return this;
    }

    public List<ServiceInstanceDetail> getDetail() {
        return detail;
    }

    public List<ServiceCount> getServiceCounts() {
        return serviceCounts;
    }

    public List<OrganizationCount> getOrganizationCounts() {
        return organizationCounts;
    }

}
