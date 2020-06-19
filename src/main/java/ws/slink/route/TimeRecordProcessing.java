package ws.slink.route;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ws.slink.bean.PointAggregationStrategy;
import ws.slink.bean.PointBatchBean;
import ws.slink.bean.TimeRecordToPointConverter;
import ws.slink.config.AppConfig;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TimeRecordProcessing extends RouteBuilder {

    private final @NonNull AppConfig appConfig;

    @Value("${influxdb.db}") String influxDb;

    @Override
    public void configure() throws Exception {
        onException()
            .log(LoggingLevel.WARN, log.getName(), "${exception.getMessage()}")
            // .log(LoggingLevel.WARN, log.getName(), "${exception.printStackTrace()}")
            .handled(true)
        ;
        from("seda:saveToInflux")
            .log(LoggingLevel.INFO, log.getName(), "${body}")
            .bean(TimeRecordToPointConverter.class)
            .aggregate(constant(true), new PointAggregationStrategy())
            .completionTimeout(appConfig.batchTime())
            .completionSize(appConfig.batchSize())
            .forceCompletionOnStop()
            .bean(PointBatchBean.class, "build")
//            .bean(PointBatchBean.class, "print")
            .log(LoggingLevel.INFO, log.getName(), "SAVE TO INFLUX    : ${body}")
            .to("influxdb://influxClient?databaseName=" + influxDb + "&retentionPolicy=autogen&batch=true")
            .log(LoggingLevel.INFO, log.getName(), "INFLUX SAVE RESULT: ${body}")
        ;
    }
}
