package io.pivotal.cfapp.service;

import java.util.List;

import io.pivotal.cfapp.domain.OrganizationCount;
import io.pivotal.cfapp.domain.ServiceCount;
import io.pivotal.cfapp.domain.ServiceInstanceDetail;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ServiceInstanceDetailService {

	Mono<ServiceInstanceDetail> save(ServiceInstanceDetail entity);
	Flux<ServiceInstanceDetail> findAll();
	Mono<Void> deleteAll();
	List<ServiceCount> countServicesByType();
	List<OrganizationCount> countServicesByOrganization();
}
