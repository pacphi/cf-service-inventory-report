package io.pivotal.cfapp.task.jdbc;

import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.pivotal.cfapp.repository.ServiceDetailAggregator;
import io.pivotal.cfapp.repository.jdbc.JdbcServiceInfoRepository;
import io.pivotal.cfapp.task.ServiceInfoRetrievedEvent;
import io.pivotal.cfapp.task.ServiceTask;

@Profile("jdbc")
@Component
public class JdbcServiceTask extends ServiceTask {
    
    private ApplicationEventPublisher applicationEventPublisher;
    private JdbcServiceInfoRepository reactiveServiceInfoRepository;
    private ServiceDetailAggregator serviceDetailAggregator;
    
    @Autowired
    public JdbcServiceTask(
            DefaultCloudFoundryOperations opsClient,
            ApplicationEventPublisher applicationEventPublisher,
            JdbcServiceInfoRepository reactiveServiceInfoRepository,
            ServiceDetailAggregator serviceDetailAggregator
            ) {
        super(opsClient);
        this.applicationEventPublisher = applicationEventPublisher;
        this.reactiveServiceInfoRepository = reactiveServiceInfoRepository;
        this.serviceDetailAggregator = serviceDetailAggregator;
    }

    @Override
    @Scheduled(cron = "${cron}")
    protected void runTask() {
        reactiveServiceInfoRepository
            .deleteAll()
            .thenMany(getOrganizations())
            .flatMap(spaceRequest -> getSpaces(spaceRequest))
            .flatMap(serviceSummaryRequest -> getServiceSummary(serviceSummaryRequest))
            .flatMap(serviceDetailRequest -> getServiceDetail(serviceDetailRequest))
            .flatMap(reactiveServiceInfoRepository::save)
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
