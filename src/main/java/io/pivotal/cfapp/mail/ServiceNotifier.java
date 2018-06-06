package io.pivotal.cfapp.mail;

import java.io.IOException;
import java.time.LocalDateTime;

import javax.mail.MessagingException;

import org.springframework.context.ApplicationListener;

import io.pivotal.cfapp.config.ServiceSettings;
import io.pivotal.cfapp.config.MailSettings;
import io.pivotal.cfapp.domain.ServiceDetail;
import io.pivotal.cfapp.domain.ServiceMetrics;
import io.pivotal.cfapp.domain.ServiceCount;
import io.pivotal.cfapp.domain.OrganizationCount;
import io.pivotal.cfapp.task.ServiceInfoRetrievedEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class ServiceNotifier implements ApplicationListener<ServiceInfoRetrievedEvent> {
    
    protected final ServiceSettings serviceSettings;
    protected final MailSettings mailSettings;
    
    public ServiceNotifier(ServiceSettings serviceSettings, MailSettings mailSettings) {
        this.serviceSettings = serviceSettings;
        this.mailSettings = mailSettings;
    }

    protected abstract void sendMail(String to, String subject, String body, String detailAttachment, String summaryAttachment) throws MessagingException, IOException;

    @Override
    public void onApplicationEvent(ServiceInfoRetrievedEvent event) {
        String body = applyBody();
        String detailAttachment = applyDetailAttachment(event);
        String summaryAttachment = applySummaryAttachment(event);
        log.info(detailAttachment);
        log.info(summaryAttachment);
        mailSettings.getRecipients().forEach(r -> {
            try {
                sendMail(r, mailSettings.getSubject(), body, detailAttachment, summaryAttachment);
            } catch (MessagingException | IOException e) {
                log.error("Could not send email!", e);
            }
        });
        
    }

    private String applyBody() {
        StringBuffer body = new StringBuffer();
        body.append("Please find attached service inventory detail and summary reports from ");
        body.append(serviceSettings.getApiHost());
        body.append(" generated ");
        body.append(LocalDateTime.now());
        body.append(".");
        return body.toString();
    }
    
    private String applyDetailAttachment(ServiceInfoRetrievedEvent event) {
        StringBuffer attachment = new StringBuffer();
        attachment.append("\n");
        attachment.append(ServiceDetail.headers());
        attachment.append("\n");
        event.getDetail()
                .forEach(a -> { 
                    attachment.append(a.toCsv());
                    attachment.append("\n");
                });
        return attachment.toString();
    }
    
    private String applySummaryAttachment(ServiceInfoRetrievedEvent event) {
        ServiceMetrics metrics = new ServiceMetrics(event.getDetail());
        StringBuffer attachment = new StringBuffer();
        
        attachment.append("\n");
        attachment.append(OrganizationCount.headers());
        attachment.append("\n");
        event.getOrganizationCounts().forEach(r -> {
            attachment.append(r.toCsv());
            attachment.append("\n");
        });
        
        
        attachment.append("\n");
        attachment.append(ServiceCount.headers());
        attachment.append("\n");
        event.getServiceCounts().forEach(r -> {
            attachment.append(r.toCsv());
            attachment.append("\n");
        });
    
        attachment.append("\n");
        attachment.append(ServiceMetrics.updatedHeaders() + "\n");
        attachment.append("<= 1 day," + metrics.updatedInLastDay() + "\n");
        attachment.append("> 1 day <= 1 week," + metrics.updatedInLastWeek() + "\n");
        attachment.append("> 1 week <= 1 month," + metrics.updatedInLastMonth() + "\n");
        attachment.append("> 1 month <= 3 months," + metrics.updatedInLastThreeMonths() + "\n");
        attachment.append("> 3 months <= 6 months," + metrics.updatedInLastSixMonths() + "\n");
        attachment.append("> 6 months <= 1 year," + metrics.updatedInLastYear() + "\n");
        attachment.append("> 1 year," + metrics.updatedBeyondOneYear() + "\n");
        
        attachment.append("\n");
        attachment.append("Total services: " + metrics.totalServices());
        return attachment.toString();
    }
}