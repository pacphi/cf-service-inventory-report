package io.pivotal.cfapp.service;

import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import io.pivotal.cfapp.domain.OrganizationCount;
import io.pivotal.cfapp.domain.ServiceCount;
import io.pivotal.cfapp.domain.ServiceDetail;
import io.pivotal.cfapp.repository.JdbcServiceDetailAggregator;
import io.pivotal.cfapp.repository.JdbcServiceInfoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Profile("jdbc")
@Service
public class JdbcServiceInfoService implements ServiceInfoService {

	private JdbcServiceInfoRepository repo;
	private JdbcServiceDetailAggregator aggregator;

	public JdbcServiceInfoService(
			JdbcServiceInfoRepository repo,
			JdbcServiceDetailAggregator aggregator) {
		this.repo = repo;
		this.aggregator = aggregator;
	}
	
	@Override
	public Mono<ServiceDetail> save(ServiceDetail entity) {
		return repo.save(entity);
	}

	@Override
	public Flux<ServiceDetail> findAll() {
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