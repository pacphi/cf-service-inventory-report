package io.pivotal.cfapp.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import io.pivotal.cfapp.domain.ServiceDetail;
 
public interface ReactiveServiceInfoRepository extends ReactiveCrudRepository<ServiceDetail, String> {
}