package io.pivotal.cfapp.notifier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import io.pivotal.cfapp.config.ServiceSettings;
import io.pivotal.cfapp.report.CsvReport;
import io.pivotal.cfapp.task.ServiceInstanceDetailRetrievedEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ConsoleNotifier implements ApplicationListener<ServiceInstanceDetailRetrievedEvent> {

	private final CsvReport report;
	
    @Autowired
    public ConsoleNotifier(ServiceSettings appSettings) {
        this.report = new CsvReport(appSettings);
    }

	@Override
	public void onApplicationEvent(ServiceInstanceDetailRetrievedEvent event) {
		log.info(String.join("\n\n", report.generatePreamble(), report.generateDetail(event), report.generateSummary(event)));
	}
  
}