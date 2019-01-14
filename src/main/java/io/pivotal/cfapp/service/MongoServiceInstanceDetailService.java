package io.pivotal.cfapp.service;

import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import io.pivotal.cfapp.domain.OrganizationCount;
import io.pivotal.cfapp.domain.ServiceCount;
import io.pivotal.cfapp.domain.ServiceInstanceDetail;
import io.pivotal.cfapp.repository.MongoServiceInstanceDetailAggregator;
import io.pivotal.cfapp.repository.MongoServiceInstanceDetailRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Profile("mongo")
@Service
public class MongoServiceInstanceDetailService implements ServiceInstanceDetailService {

	private MongoServiceInstanceDetailRepository repo;
	private MongoServiceInstanceDetailAggregator aggregator;

	public MongoServiceInstanceDetailService(
			MongoServiceInstanceDetailRepository repo,
			MongoServiceInstanceDetailAggregator aggregator) {
		this.repo = repo;
		this.aggregator = aggregator;
	}

	@Override
	public Mono<ServiceInstanceDetail> save(ServiceInstanceDetail entity) {
		return repo.save(entity);
	}

	@Override
	public Flux<ServiceInstanceDetail> findAll() {
		return repo.findAll();
	}

	@Override
	public Mono<Void> deleteAll() {
		return repo.deleteAll();
	}

	@Override
	public List<ServiceCount> countServicesByType() {
		return aggregator.countServicesByType();
	}

	@Override
	public List<OrganizationCount> countServicesByOrganization() {
		return aggregator.countServicesByOrganization();
	}

}
