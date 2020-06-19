package ws.slink;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.FluentProducerTemplate;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import org.zendesk.client.v2.model.Audit;
import org.zendesk.client.v2.model.Ticket;
import org.zendesk.client.v2.model.User;
import org.zendesk.client.v2.model.events.ChangeEvent;
import org.zendesk.client.v2.model.events.Event;
import ws.slink.config.AppConfig;
import ws.slink.model.TimeRecord;
import ws.slink.tools.TimeUtils;
import ws.slink.zendesk.ZendeskFacade;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.TimeZone;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@DependsOn({"commandLineArguments"})
public class ZenTimeRunner implements CommandLineRunner, ApplicationContextAware {

    private final @NonNull AppConfig appConfig;
    private final @NonNull ZendeskFacade zendeskFacade;
    private final @NonNull CamelContext camelContext;

    private ConfigurableApplicationContext applicationContext;
    private FluentProducerTemplate producerTemplate;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = (ConfigurableApplicationContext) applicationContext;
    }

    @PostConstruct
    private void init() {
        producerTemplate = camelContext.createFluentProducerTemplate();
    }

    private String ticketsQuery(String offset) {
        if (StringUtils.isBlank(offset) || offset.equals("*") || offset.equals("all")) {
            return "type:ticket";
        } else {
            Instant startTime = TimeUtils.offset(Instant.now(), "-10d");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            return "type:ticket updated>" + sdf.format(new Date(startTime.toEpochMilli()));
        }
    }
    private boolean eventFilter(Event e) {
        if (e instanceof ChangeEvent) {
            ChangeEvent ce = (ChangeEvent)e;
            if (ce.getFieldName().equalsIgnoreCase(appConfig.timeField())) // "360043408493"
                return true;
        }
        return false;
    }
    private TimeRecord createTimeRecord(Ticket ticket, Audit audit, ChangeEvent change) {
        Optional<User> user = zendeskFacade.getUser(audit.getAuthorId());
        if (user.isPresent()) {
            return new TimeRecord()
                .date(audit.getCreatedAt().toInstant())
                .employee(user.get().getName())
                .ticketId(ticket.getId())
                .seconds(Long.valueOf(change.getValueObject().toString()))
            ;
        } else {
            log.warn("could not get user from zendesk");
            return null;
        }
    }
    private void processTimeRecord(TimeRecord timeRecord) {
        if (!appConfig.dryRun()) {
            producerTemplate
                .withBody(timeRecord)
                .to("seda:saveToInflux")
                .send()
            ;
        } else {
            System.out.println(timeRecord);
        }
    }

    @Override
    public void run(String... args) {
        int exitCode = 0;

        if (!checkConfiguration()) {
            printUsage();
        } else {
            zendeskFacade.searchTickets(ticketsQuery(appConfig.offsetStr()))
                .forEach(t -> {
                    zendeskFacade.getAudits(t)
                        .stream()
                        .forEach(a -> {
                            a.getEvents()
                                .parallelStream()
                                .filter(this::eventFilter)
                                .map( e -> (ChangeEvent)e)
                                .map(ce -> createTimeRecord(t, a, ce))
                                .filter(Objects::nonNull)
                                .forEach(this::processTimeRecord)
                            ;
                        });
                    });
        }

        // close up
        applicationContext.close();
        System.exit(exitCode);
    }

    private boolean checkConfiguration() {
        return !( StringUtils.isNotBlank(appConfig.url()) && (StringUtils.isBlank(appConfig.user()) || StringUtils.isBlank(appConfig.token()))
               || StringUtils.isNotBlank(appConfig.user()) && (StringUtils.isBlank(appConfig.url()) || StringUtils.isBlank(appConfig.token()))
               || StringUtils.isNotBlank(appConfig.token()) && (StringUtils.isBlank(appConfig.user()) || StringUtils.isBlank(appConfig.url()))
               )
        ;
    }
    public void printUsage() {
        System.out.println("Usage: ");
        System.out.println("  java -jar zendesk-reporter.jar [--dry-run] [--all | --since=<offsetStr>] --url=<zendesk url> --user=<login> --token=<token>");
        System.out.println("\t--url\t\t\tZendesk server URL (e.g. http://test.zendesk.com)");
        System.out.println("\t--user\t\t\tZendesk user with publish rights");
        System.out.println("\t--token\t\t\tZendesk access token");
        System.out.println("\t--dry-run\t\tPerform dry-run - don't write anything into influx database");
        System.out.println("\t--all\t\t\tProcess all tickets from zendesk");
        System.out.println("\t--since\tProcess tickets updated after given offset");
        System.out.println();
        System.out.println("   Offset string format: [-]<number>{s | m | h | d | w | M | y}");
        System.out.println("       [-] offset from current date into the past (without offset into the future)");
        System.out.println();
        System.out.println("       s - seconds");
        System.out.println("       m - minutes");
        System.out.println("       h - hours");
        System.out.println("       d - days");
        System.out.println("       w - weeks");
        System.out.println("       M - months");
        System.out.println("       y - years");
        System.out.println();
        System.exit(1);
    }
}
