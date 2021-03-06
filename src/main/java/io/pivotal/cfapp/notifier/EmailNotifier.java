package io.pivotal.cfapp.notifier;

import java.io.IOException;

import javax.mail.MessagingException;

import org.springframework.context.ApplicationListener;

import io.pivotal.cfapp.config.MailSettings;
import io.pivotal.cfapp.config.ServiceSettings;
import io.pivotal.cfapp.report.CsvReport;
import io.pivotal.cfapp.task.ServiceInstanceDetailRetrievedEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class EmailNotifier implements ApplicationListener<ServiceInstanceDetailRetrievedEvent> {
    
    protected final MailSettings mailSettings;
    protected final CsvReport report;
    
    public EmailNotifier(ServiceSettings serviceSettings, MailSettings mailSettings) {
    	this.report = new CsvReport(serviceSettings);
        this.mailSettings = mailSettings;
    }

    protected abstract void sendMail(String to, String subject, String body, String detailAttachment, String summaryAttachment) throws MessagingException, IOException;

    @Override
    public void onApplicationEvent(ServiceInstanceDetailRetrievedEvent event) {
    	String body = report.generatePreamble();
        String detailAttachment = report.generateDetail(event);
        String summaryAttachment = report.generateSummary(event);
        mailSettings.getRecipients().forEach(r -> {
            try {
                sendMail(r, mailSettings.getSubject(), body, detailAttachment, summaryAttachment);
            } catch (MessagingException | IOException e) {
                log.error("Could not send email!", e);
            }
        });
        
    }

}