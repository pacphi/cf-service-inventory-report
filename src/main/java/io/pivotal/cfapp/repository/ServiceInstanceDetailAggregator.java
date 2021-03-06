package io.pivotal.cfapp.repository;

import java.util.List;

import io.pivotal.cfapp.domain.OrganizationCount;
import io.pivotal.cfapp.domain.ServiceCount;

public interface ServiceInstanceDetailAggregator {

    public List<ServiceCount> countServicesByType();
    public List<OrganizationCount> countServicesByOrganization();

}
