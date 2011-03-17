package org.cloumon.manager.alarm;

import java.security.Security;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.net.ssl.SSLSocketFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloumon.common.util.CollectionUtils;
import org.cloumon.manager.model.Alarm;
import org.cloumon.thrift.HostInfo;
import org.cloumon.thrift.MonitorItem;

import com.gruter.common.conf.GruterConf;

public class MailAlarmSender implements AlarmSender {
  public static final Log LOG = LogFactory.getLog(MailAlarmSender.class);
  private Properties props = System.getProperties();
  private Session session;
  private String from;
  
  private void init(final GruterConf conf) {
    String smtpHost = conf.get("alarm.mail.smtp.host");   //smtp.gmail.com
    String smtpPort = conf.get("alarm.mail.smtp.port", "465");   //465
    final String smtpUserId = conf.get("alarm.mail.smtp.userId");
    final String smtpUserPasswd = conf.get("alarm.mail.smtp.userPasswd");
    
    props.setProperty("mail.smtp.host", smtpHost);
    props.setProperty("mail.smtp.socketFactory.class", SSLSocketFactory.class.getCanonicalName());
    props.setProperty("mail.smtp.socketFactory.fallback", "false");
    props.setProperty("mail.smtp.port", smtpPort);
    props.setProperty("mail.smtp.socketFactory.port", smtpPort);
    props.put("mail.smtp.auth", "true");
    
    Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
    session = Session.getDefaultInstance(props, new Authenticator() {
      @Override
      protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(smtpUserId, smtpUserPasswd);
      }
    });
    
    from = conf.get("alarm.mail.from");
  }

  private void sendMail(GruterConf conf, String subject, String body, List<String> targets) {
    if(session == null) {
      init(conf);
    }
    try {
      Message message = new MimeMessage(session);
      message.setFrom(new InternetAddress(from));
      message.setSentDate(new Date());
      message.setSubject(subject);
      
      message.setContent(body, "text/html;charset=utf-8");
       
      for(String eachMail: targets) {
        LOG.info("send mail to " + eachMail);
        message.addRecipient(MimeMessage.RecipientType.TO, new InternetAddress(eachMail));
      }
      Transport.send(message);
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
    }
  }
  
  @Override
  public void sendAgentFailAlarm(GruterConf conf, HostInfo hostInfo) {
    if(!hostInfo.isAlarmOn()) {
      return;
    }
    List<String> target = Arrays.asList(hostInfo.getHostAlarm().split(","));

    String subject = "Monitor Notification Message:" + hostInfo.getHostName() + " agent shutdowned.";
    StringBuilder mailText = new StringBuilder();
    mailText.append("- Host: ").append(hostInfo.getHostName()).append("<br/>");
    sendMail(conf, subject, mailText.toString(), target);
  }
  
  @Override
  public void sendAlarm(GruterConf conf, Alarm alarm, HostInfo hostInfo, MonitorItem monitorItem, String alarmExpr, byte[] monitorData) {
    String subject = "Monitor Notification Message:" + hostInfo.getHostName() + ":" + monitorItem.getItemName() + ":" + new String(monitorData);
    StringBuilder mailText = new StringBuilder();
    
    mailText.append("- Host: ").append(hostInfo.getHostName()).append("<br/>");
    mailText.append("- Monitor Item: ").append(monitorItem.getItemName()).append("<br/>");
    mailText.append("- Monitor Data: ").append(new String(monitorData)).append("<br/>");
    mailText.append("- Alarm Expression: ").append(alarmExpr).append("<br/>");
    mailText.append("- Description: ").append(monitorItem.getDescription()).append("<br/>");
    mailText.append("- Params: ").append(monitorItem.getParams()).append("<br/>");
    mailText.append("- Mail: ").append(CollectionUtils.toString(alarm.getAlarmTargets())).append("<br/>");
    
    sendMail(conf, subject, mailText.toString(), alarm.getAlarmTargets());
  }
}
