package io.pivotal.cfapp.mail;

import java.io.IOException;

import javax.activation.DataSource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.util.ByteArrayDataSource;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import io.pivotal.cfapp.config.ServiceSettings;
import io.pivotal.cfapp.config.MailSettings;

public class JavaMailNotifier extends ServiceNotifier {

    private final JavaMailSender javaMailSender;
    
    public JavaMailNotifier(
            ServiceSettings serviceSettings, MailSettings mailSettings, 
            JavaMailSender javaMailSender) {
        super(serviceSettings, mailSettings);
        this.javaMailSender = javaMailSender;
    }

    protected void sendMail(String to, String subject, String body, String detailAttachment, String summaryAttachment) throws MessagingException, IOException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setFrom(mailSettings.getFrom());
        helper.setSubject(subject);
        helper.setTo(to);
        helper.setText(body, true);
        DataSource detail = new ByteArrayDataSource(detailAttachment, "text/csv");
        DataSource summary = new ByteArrayDataSource(summaryAttachment, "text/csv");
        helper.addAttachment("service-inventory-detail.csv", detail);
        helper.addAttachment("service-inventory-summary.csv", summary);
        javaMailSender.send(message);
    }
}