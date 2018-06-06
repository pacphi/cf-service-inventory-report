package io.pivotal.cfapp.task;

import java.time.Instant;
import java.time.ZoneId;

import org.apache.commons.lang3.StringUtils;
import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.cloudfoundry.operations.services.GetServiceInstanceRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import io.pivotal.cfapp.domain.ServiceDetail;
import io.pivotal.cfapp.domain.ServiceRequest;
import io.pivotal.cfapp.repository.ServiceDetailAggregator;
import io.pivotal.cfapp.repository.ReactiveServiceInfoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class ServiceTask implements ApplicationRunner {
    
    private DefaultCloudFoundryOperations opsClient;
    private ApplicationEventPublisher applicationEventPublisher;
    private ReactiveServiceInfoRepository reactiveServiceInfoRepository;
    private ServiceDetailAggregator serviceDetailAggregator;
    
    @Autowired
    public ServiceTask(
            DefaultCloudFoundryOperations opsClient,
            ApplicationEventPublisher applicationEventPublisher,
            ReactiveServiceInfoRepository reactiveServiceInfoRepository,
            ServiceDetailAggregator serviceDetailAggregator
            ) {
        this.opsClient = opsClient;
        this.applicationEventPublisher = applicationEventPublisher;
        this.reactiveServiceInfoRepository = reactiveServiceInfoRepository;
        this.serviceDetailAggregator = serviceDetailAggregator;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
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

    private Flux<ServiceRequest> getOrganizations() {
        return DefaultCloudFoundryOperations.builder()
            .from(opsClient)
            .build()
                .organizations()
                    .list()
                    .map(os -> ServiceRequest.builder().organization(os.getName()).build())
                    .log();
    }
    
    private Flux<ServiceRequest> getSpaces(ServiceRequest request) {
        return DefaultCloudFoundryOperations.builder()
            .from(opsClient)
            .organization(request.getOrganization())
            .build()
                .spaces()
                    .list()
                    .map(s -> ServiceRequest.from(request).space(s.getName()).build())
                    .log();
    }
    
    private Flux<ServiceRequest> getServiceSummary(ServiceRequest request) {
        return DefaultCloudFoundryOperations.builder()
            .from(opsClient)
            .organization(request.getOrganization())
            .space(request.getSpace())
            .build()
                .services()
                    .listInstances()
                    .map(ss -> ServiceRequest.from(request).serviceName(ss.getName()).build())
                    .log();
    }
    
    private Mono<ServiceDetail> getServiceDetail(ServiceRequest request) {
         return DefaultCloudFoundryOperations.builder()
            .from(opsClient)
            .organization(request.getOrganization())
            .space(request.getSpace())
            .build()
                .services()
                    .getInstance(GetServiceInstanceRequest.builder().name(request.getServiceName()).build())
                    .onErrorResume(e -> Mono.empty())
                    .map(sd -> ServiceDetail
                                .builder()
                                    .organization(request.getOrganization())
                                    .space(request.getSpace())
                                    .name(request.getServiceName())
                                    .service(sd.getService())
                                    .plan(sd.getPlan())
                                    .type(sd.getType() !=null ? sd.getType().name().toLowerCase(): "")
                                    .lastOperation(sd.getLastOperation())
                                    .lastUpdated(StringUtils.isNotBlank(sd.getUpdatedAt()) ? Instant.parse(sd.getUpdatedAt())
                                                .atZone(ZoneId.systemDefault())
                                                .toLocalDateTime() : null)
                                    .dashboardUrl(sd.getDashboardUrl())
                                    .requestedState(StringUtils.isNotBlank(sd.getUpdatedAt()) ? sd.getStatus().toLowerCase(): "")
                                    .build())
                    .log();
    }
}
