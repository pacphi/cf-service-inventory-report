package io.pivotal.cfapp.service;

import java.util.List;

import io.pivotal.cfapp.domain.OrganizationCount;
import io.pivotal.cfapp.domain.ServiceCount;
import io.pivotal.cfapp.domain.ServiceDetail;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ServiceInfoService {

	Mono<ServiceDetail> save(ServiceDetail entity);
	Flux<ServiceDetail> findAll();
	Mono<Void> deleteAll();
	List<ServiceCount> countServicesByType();
	List<OrganizationCount> countServicesByOrganization();
}
