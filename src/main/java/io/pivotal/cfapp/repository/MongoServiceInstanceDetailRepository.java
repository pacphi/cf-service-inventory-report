package io.pivotal.cfapp.repository;

import org.springframework.context.annotation.Profile;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import io.pivotal.cfapp.domain.ServiceInstanceDetail;

@Profile("mongo")
public interface MongoServiceInstanceDetailRepository extends ReactiveCrudRepository<ServiceInstanceDetail, String> {
}