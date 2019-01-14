package io.pivotal.cfapp.controller;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.pivotal.cfapp.config.ServiceSettings;
import io.pivotal.cfapp.report.CsvReport;
import io.pivotal.cfapp.service.ServiceInstanceDetailService;
import io.pivotal.cfapp.task.ServiceInstanceDetailRetrievedEvent;
import reactor.core.publisher.Mono;

@RestController
public class ServiceInstanceDetailController {

	private ServiceInstanceDetailService service;
	private CsvReport report;

	@Autowired
	public ServiceInstanceDetailController(
			ServiceSettings appSettings,
			ServiceInstanceDetailService service) {
		this.report = new CsvReport(appSettings);
		this.service = service;
	}

	@GetMapping(value = { "/report" }, produces = MediaType.TEXT_PLAIN_VALUE )
	public Mono<ResponseEntity<String>> generateReport() {
		return service
				.findAll()
				.collectList()
		        .map(r -> new ServiceInstanceDetailRetrievedEvent(this)
							.detail(r)
							.serviceCounts(service.countServicesByType())
							.organizationCounts(service.countServicesByOrganization())
				)
				.delayElement(Duration.ofMillis(500))
		        .map(event -> ResponseEntity.ok(
		        	String.join(
		        			"\n\n",
		        			report.generatePreamble(),
		        			report.generateDetail(event),
		        			report.generateSummary(event))));
	}

}
