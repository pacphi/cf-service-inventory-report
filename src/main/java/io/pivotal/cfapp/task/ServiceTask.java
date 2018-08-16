package io.pivotal.cfapp.task;

import java.time.Instant;
import java.time.ZoneId;

import org.apache.commons.lang3.StringUtils;
import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.cloudfoundry.operations.services.GetServiceInstanceRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import io.pivotal.cfapp.domain.ServiceDetail;
import io.pivotal.cfapp.domain.ServiceRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public abstract class ServiceTask implements ApplicationRunner {
    
    private DefaultCloudFoundryOperations opsClient;
    
    @Autowired
    public ServiceTask(DefaultCloudFoundryOperations opsClient) {
        this.opsClient = opsClient;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
    	runTask();
    }
    
    protected abstract void runTask();

    protected Flux<ServiceRequest> getOrganizations() {
        return DefaultCloudFoundryOperations.builder()
            .from(opsClient)
            .build()
                .organizations()
                    .list()
                    .map(os -> ServiceRequest.builder().organization(os.getName()).build())
                    .log();
    }
    
    protected Flux<ServiceRequest> getSpaces(ServiceRequest request) {
        return DefaultCloudFoundryOperations.builder()
            .from(opsClient)
            .organization(request.getOrganization())
            .build()
                .spaces()
                    .list()
                    .map(s -> ServiceRequest.from(request).space(s.getName()).build())
                    .log();
    }
    
    protected Flux<ServiceRequest> getServiceSummary(ServiceRequest request) {
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
    
    protected Mono<ServiceDetail> getServiceDetail(ServiceRequest request) {
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
                                    .description(sd.getDescription())
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
