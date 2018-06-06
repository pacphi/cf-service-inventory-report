package io.pivotal.cfapp.repository;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.unwind;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.stereotype.Component;

import io.pivotal.cfapp.domain.ServiceDetail;
import io.pivotal.cfapp.domain.ServiceCount;
import io.pivotal.cfapp.domain.OrganizationCount;

@Component
public class ServiceDetailAggregator {

    private ReactiveMongoTemplate reactiveMongoTemplate;

    @Autowired
    public ServiceDetailAggregator(ReactiveMongoTemplate reactiveMongoTemplate) {
        this.reactiveMongoTemplate = reactiveMongoTemplate;
    }
    
    public List<ServiceCount> countServicesByType() {
        Aggregation agg = newAggregation(
            project("service"),
            unwind("service"),
            group("service").count().as("total"),
            project("total").and("service").previousOperation(),
            sort(Sort.Direction.DESC, "total")
        );
        return reactiveMongoTemplate
                .aggregate(agg, ServiceDetail.class, ServiceCount.class)
                    .toStream()
                        .collect(Collectors.toList());
    }
    
    public List<OrganizationCount> countServicesByOrganization() {
        Aggregation agg = newAggregation(
            project("organization"),
            unwind("organization"),
            group("organization").count().as("total"),
            project("total").and("organization").previousOperation(),
            sort(Sort.Direction.DESC, "total")
        );
        return reactiveMongoTemplate
                .aggregate(agg, ServiceDetail.class, OrganizationCount.class)
                    .toStream()
                        .collect(Collectors.toList());
    }
    
}
