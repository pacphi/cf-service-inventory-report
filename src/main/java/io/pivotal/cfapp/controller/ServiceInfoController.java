package io.pivotal.cfapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.pivotal.cfapp.config.ServiceSettings;
import io.pivotal.cfapp.report.CsvReport;
import io.pivotal.cfapp.service.ServiceInfoService;
import io.pivotal.cfapp.task.ServiceInfoRetrievedEvent;
import reactor.core.publisher.Mono;

@RestController
public class ServiceInfoController {

	private ServiceInfoService service;
	private CsvReport report;
	
	@Autowired
	public ServiceInfoController(
			ServiceSettings appSettings,
			ServiceInfoService service) {
		this.report = new CsvReport(appSettings);
		this.service = service;
	}
	
	@GetMapping(value = { "/report" }, produces = MediaType.TEXT_PLAIN_VALUE )
	public Mono<String> generateReport() {
		return service
				.findAll()
				.collectList()
		        .map(r ->
	                new ServiceInfoRetrievedEvent(
                        this, 
                        r, 
                        service.countServicesByType(),
                        service.countServicesByOrganization()
	                )
		        )
		        .map(event -> 
		        	String.join(
		        			"\n\n", 
		        			report.generatePreamble(), 
		        			report.generateDetail(event), 
		        			report.generateSummary(event)));
	}

}
