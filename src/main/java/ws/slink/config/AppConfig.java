package ws.slink.config;

import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import ws.slink.tools.TimeUtils;


/**
 * accepts following configuration parameters from any spring-supported configuration source
 * (properties file, command line arguments, environment variables):
 *
 *   a2z.dir     - input directory for processing
 *   a2z.profile - configuration file suffix
 *   a2z.url     - zendesk server URL
 *   a2z.user    - zendesk server user
 *   a2z.token   - zendesk server password
 *
 */

@Data
@Component
@ConfigurationProperties(prefix = "a2z")
@Accessors(fluent = true)
public class AppConfig {

    @Value("${report.priority-field}")
    private String priorityField;

    @Value("${report.time-field}")
    private String timeField;

    @Value("${report.topic-field}")
    private String topicField;

    @Value("${report.app-field}")
    private String appField;

    @Value("${report.offset}")
    private String offsetStr;

    @Value("${influxdb.batch-size}")
    private int batchSize;

    @Value("${influxdb.batch-time}")
    private int batchTime;

    private String  url;
    private String  user;
    private String  token;
    private boolean dryRun = false;

    public void print() {
        System.out.println("zendesk url    : " + url);
        System.out.println("zendesk user   : " + user);
        System.out.println("zendesk token  : " + token);
        System.out.println("dry run        : " + dryRun);
        System.out.println("app field      : " + appField);
        System.out.println("topic field    : " + topicField);
        System.out.println("priority field : " + priorityField);
        System.out.println("time field     : " + timeField);
        System.out.println("time offset    : " + offsetStr);
        System.out.println("time since     : " + TimeUtils.timeString(TimeUtils.offset(offsetStr)));
    }

}
