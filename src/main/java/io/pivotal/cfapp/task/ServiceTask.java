package io.pivotal.cfapp.task;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.cloudfoundry.client.v2.servicebindings.ListServiceBindingsRequest;
import org.cloudfoundry.client.v3.applications.GetApplicationRequest;
import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.cloudfoundry.operations.services.GetServiceInstanceRequest;
import org.cloudfoundry.reactor.client.ReactorCloudFoundryClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import io.pivotal.cfapp.domain.ServiceDetail;
import io.pivotal.cfapp.domain.ServiceRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public abstract class ServiceTask implements ApplicationRunner {
    
    private DefaultCloudFoundryOperations opsClient;
    private ReactorCloudFoundryClient cloudFoundryClient;
    
    @Autowired
    public ServiceTask(
    		DefaultCloudFoundryOperations opsClient,
    		ReactorCloudFoundryClient cloudFoundryClient
    		) {
        this.opsClient = opsClient;
        this.cloudFoundryClient = cloudFoundryClient;
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
                    .map(os -> ServiceRequest.builder().organization(os.getName()).build());
    }
    
    protected Flux<ServiceRequest> getSpaces(ServiceRequest request) {
        return DefaultCloudFoundryOperations.builder()
            .from(opsClient)
            .organization(request.getOrganization())
            .build()
                .spaces()
                    .list()
                    .map(s -> ServiceRequest.from(request).space(s.getName()).build());
    }
    
    protected Flux<ServiceRequest> getServiceSummary(ServiceRequest request) {
        return DefaultCloudFoundryOperations.builder()
            .from(opsClient)
            .organization(request.getOrganization())
            .space(request.getSpace())
            .build()
                .services()
                    .listInstances()
                    .map(ss -> ServiceRequest.from(request)
                    							.id(ss.getId())
                    							.serviceName(ss.getName())
                    							.build());
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
                                   .type(sd.getType() != null ? sd.getType().name().toLowerCase(): "")
                                   .applications(toTruncatedString(request.getApplicationNames()))
                                   .lastOperation(sd.getLastOperation())
                                   .lastUpdated(StringUtils.isNotBlank(sd.getUpdatedAt()) ? Instant.parse(sd.getUpdatedAt())
                                               .atZone(ZoneId.systemDefault())
                                               .toLocalDateTime() : null)
                                   .dashboardUrl(sd.getDashboardUrl())
                                   .requestedState(StringUtils.isNotBlank(sd.getUpdatedAt()) ? sd.getStatus().toLowerCase(): "")
                                   .build());
    }
    
    protected Mono<ServiceRequest> getServiceBoundApplicationIds(ServiceRequest request) {
    	return cloudFoundryClient
			.serviceBindingsV2()
			.list(ListServiceBindingsRequest.builder().serviceInstanceId(request.getId()).build())
			.flux()
			.flatMap(serviceBindingResponse -> Flux.fromIterable(serviceBindingResponse.getResources()))
			.map(resource -> resource.getEntity())
			.map(entity -> entity.getApplicationId())
    		.collectList()
    		.map(i -> ServiceRequest.from(request).applicationIds(i).build());
    }
    
    protected Mono<ServiceRequest> getServiceBoundApplicationNames(ServiceRequest request) {
    	return Flux
    		.fromIterable(request.getApplicationIds())
    		.flatMap(appId ->
    			cloudFoundryClient
    				.applicationsV3()
    					.get(GetApplicationRequest.builder().applicationId(appId).build())
    					.map(response -> response.getName()))
    					.collectList()
    					.map(n -> ServiceRequest.from(request).applicationNames(n).build());
    }
    
    private String toTruncatedString(List<String> urls) {
    	String rawData = String.join(",", urls);
    	return rawData.length() <= 1000 ? rawData : rawData.substring(0, 1000);  
    }
    
}
