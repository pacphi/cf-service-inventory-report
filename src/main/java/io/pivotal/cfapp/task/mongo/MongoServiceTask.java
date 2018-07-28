package io.pivotal.cfapp.task.mongo;

import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import io.pivotal.cfapp.repository.ServiceDetailAggregator;
import io.pivotal.cfapp.repository.mongo.MongoServiceInfoRepository;
import io.pivotal.cfapp.task.ServiceInfoRetrievedEvent;
import io.pivotal.cfapp.task.ServiceTask;

@Profile("mongo")
@Component
public class MongoServiceTask extends ServiceTask {
    
    private ApplicationEventPublisher applicationEventPublisher;
    private MongoServiceInfoRepository reactiveServiceInfoRepository;
    private ServiceDetailAggregator serviceDetailAggregator;
    
    @Autowired
    public MongoServiceTask(
            DefaultCloudFoundryOperations opsClient,
            ApplicationEventPublisher applicationEventPublisher,
            MongoServiceInfoRepository reactiveServiceInfoRepository,
            ServiceDetailAggregator serviceDetailAggregator
            ) {
        super(opsClient);
        this.applicationEventPublisher = applicationEventPublisher;
        this.reactiveServiceInfoRepository = reactiveServiceInfoRepository;
        this.serviceDetailAggregator = serviceDetailAggregator;
    }

    @Override
    protected void runTask() {
        reactiveServiceInfoRepository
            .deleteAll()
            .thenMany(getOrganizations())
            .flatMap(spaceRequest -> getSpaces(spaceRequest))
            .flatMap(serviceSummaryRequest -> getServiceSummary(serviceSummaryRequest))
            .flatMap(serviceDetailRequest -> getServiceDetail(serviceDetailRequest))
            .flatMap(reactiveServiceInfoRepository::save)
            .thenMany(reactiveServiceInfoRepository.findAll())
            .collectList()
            .subscribe(r -> 
                applicationEventPublisher.publishEvent(
                    new ServiceInfoRetrievedEvent(
                            this, 
                            r, 
                            serviceDetailAggregator.countServicesByType(),
                            serviceDetailAggregator.countServicesByOrganization()
                    )
                )
            );
    }
}