package io.pivotal.cfapp.report;

import java.time.LocalDateTime;

import io.pivotal.cfapp.config.ServiceSettings;
import io.pivotal.cfapp.domain.OrganizationCount;
import io.pivotal.cfapp.domain.ServiceCount;
import io.pivotal.cfapp.domain.ServiceInstanceDetail;
import io.pivotal.cfapp.domain.ServiceMetrics;
import io.pivotal.cfapp.task.ServiceInstanceDetailRetrievedEvent;

public class CsvReport {

	private ServiceSettings settings;

	public CsvReport(ServiceSettings settings) {
		this.settings = settings;
	}

    public String generatePreamble() {
    	StringBuffer preamble = new StringBuffer();
        preamble.append("Please find attached service instance inventory detail and summary reports from ");
        preamble.append(settings.getApiHost());
        preamble.append(" generated ");
        preamble.append(LocalDateTime.now());
        preamble.append(".");
        return preamble.toString();
    }

    public String generateDetail(ServiceInstanceDetailRetrievedEvent event) {
    	StringBuffer detail = new StringBuffer();
        detail.append("\n");
        detail.append(ServiceInstanceDetail.headers());
        detail.append("\n");
        event.getDetail()
                .forEach(a -> { 
                    detail.append(a.toCsv());
                    detail.append("\n");
                });
        return detail.toString();
    }

    public String generateSummary(ServiceInstanceDetailRetrievedEvent event) {
    	ServiceMetrics metrics = new ServiceMetrics(event.getDetail());
        StringBuffer summary = new StringBuffer();

        summary.append("\n");
        summary.append(OrganizationCount.headers());
        summary.append("\n");
        event.getOrganizationCounts().forEach(r -> {
            summary.append(r.toCsv());
            summary.append("\n");
        });

        summary.append("\n");
        summary.append(ServiceCount.headers());
        summary.append("\n");
        event.getServiceCounts().forEach(r -> {
            summary.append(r.toCsv());
            summary.append("\n");
        });

        summary.append("\n");
        summary.append(ServiceMetrics.updatedHeaders() + "\n");
        summary.append("<= 1 day," + metrics.updatedInLastDay() + "\n");
        summary.append("> 1 day <= 1 week," + metrics.updatedInLastWeek() + "\n");
        summary.append("> 1 week <= 1 month," + metrics.updatedInLastMonth() + "\n");
        summary.append("> 1 month <= 3 months," + metrics.updatedInLastThreeMonths() + "\n");
        summary.append("> 3 months <= 6 months," + metrics.updatedInLastSixMonths() + "\n");
        summary.append("> 6 months <= 1 year," + metrics.updatedInLastYear() + "\n");
        summary.append("> 1 year," + metrics.updatedBeyondOneYear() + "\n");

        summary.append("\n");
        summary.append("Total services: " + metrics.totalServices());
        return summary.toString();
    }
}
