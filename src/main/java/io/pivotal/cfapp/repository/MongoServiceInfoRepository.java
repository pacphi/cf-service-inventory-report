package io.pivotal.cfapp.repository;

import org.springframework.context.annotation.Profile;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import io.pivotal.cfapp.domain.ServiceDetail;

@Profile("mongo")
public interface MongoServiceInfoRepository extends ReactiveCrudRepository<ServiceDetail, String> {
}