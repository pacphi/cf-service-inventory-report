package io.pivotal.cfapp.task;

import java.util.List;

import org.springframework.context.ApplicationEvent;

import io.pivotal.cfapp.domain.ServiceDetail;
import io.pivotal.cfapp.domain.ServiceCount;
import io.pivotal.cfapp.domain.OrganizationCount;

public class ServiceInfoRetrievedEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    private List<ServiceDetail> detail;
    private List<ServiceCount> serviceCounts;
    private List<OrganizationCount> organizationCounts;

    public ServiceInfoRetrievedEvent(Object source) {
        super(source);
    }

    public ServiceInfoRetrievedEvent detail(List<ServiceDetail> detail) {
        this.detail = detail;
        return this;
    }

    public ServiceInfoRetrievedEvent serviceCounts(List<ServiceCount> serviceCounts) {
        this.serviceCounts = serviceCounts;
        return this;
    }

    public ServiceInfoRetrievedEvent organizationCounts(List<OrganizationCount> organizationCounts) {
        this.organizationCounts = organizationCounts;
        return this;
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
