package io.pivotal.cfapp.repository;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.unwind;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.stereotype.Component;

import io.pivotal.cfapp.domain.OrganizationCount;
import io.pivotal.cfapp.domain.ServiceCount;
import io.pivotal.cfapp.domain.ServiceDetail;

@Profile("mongo")
@Component
public class MongoServiceDetailAggregator implements ServiceDetailAggregator {

    private ReactiveMongoTemplate reactiveMongoTemplate;

    @Autowired
    public MongoServiceDetailAggregator(ReactiveMongoTemplate reactiveMongoTemplate) {
        this.reactiveMongoTemplate = reactiveMongoTemplate;
    }

    // FIXME Aggregate is possibly broken
    public List<ServiceCount> countServicesByType() {
        List<ServiceCount> result = new ArrayList<>();
        Aggregation agg = newAggregation(
            project("service", "plan"),
            unwind("service", "plan"),
            group("service", "plan").count().as("total"),
            sort(Sort.Direction.DESC, "total")
        );
        reactiveMongoTemplate
                .aggregate(agg, ServiceDetail.class, ServiceCount.class)
                .subscribe(result::add);
        return result;
    }

    public List<OrganizationCount> countServicesByOrganization() {
        List<OrganizationCount> result = new ArrayList<>();
        Aggregation agg = newAggregation(
            project("organization"),
            unwind("organization"),
            group("organization").count().as("total"),
            project("total").and("organization").previousOperation(),
            sort(Sort.Direction.DESC, "total")
        );
        reactiveMongoTemplate
                .aggregate(agg, ServiceDetail.class, OrganizationCount.class)
                .subscribe(result::add);
        return result;
        
    }

}
