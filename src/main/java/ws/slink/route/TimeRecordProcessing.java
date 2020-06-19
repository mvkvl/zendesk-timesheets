package ws.slink.route;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
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

    @Bean(name = "influxClient")
    private InfluxDB getInfluxClient(
        @Value("${influxdb.port}")  int port,
        @Value("${influxdb.host}")  String host,
        @Value("${influxdb.proto}") String proto,
        @Value("${influxdb.user}")  String user,
        @Value("${influxdb.pass}")  String pass
    ) {
        return InfluxDBFactory.connect(proto + "://" + host + ":" + port, user, pass);
    }

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
            .log(LoggingLevel.WARN, log.getName(), "SAVE TO INFLUX    : ${body}")
            .to("influxdb://influxClient?databaseName=" + influxDb + "&retentionPolicy=autogen&batch=true")
            .log(LoggingLevel.INFO, log.getName(), "INFLUX SAVE RESULT: ${body}")
        ;
    }
}



//            .choice()
//            .when(simple("${headers.ReportName} == \"report1\""))
//            .bean(R1DocumentToPointConverter.class) // convert mongo document to influx point
//            .when(simple("${headers.ReportName} == \"report2\""))
//            .bean(R2DocumentToPointConverter.class) // convert mongo document to influx point
//            .otherwise()
//            .log(LoggingLevel.INFO, log.getName(), "UNSUPPORTED REPORT TYPE REQUESTED: ${headers.ReportName}")
//            .stop()
//            .end()
//            .aggregate(constant(true), new PointAggregationStrategy())
//            .completionTimeout(10000)
//            .completionSize(100)
//            .forceCompletionOnStop()
//            .bean(PointBatchBean.class, "build")
//            .bean(PointBatchBean.class, "print")
//                .log(LoggingLevel.INFO, log.getName(), "AGGREGATOR: ${headers.CamelAggregatedSize} - ${headers.CamelAggregatedCompletedBy}")
//            .to("influxdb://influxClient?databaseName=" + influxDb + "&retentionPolicy=autogen&batch=true")
//            .log(LoggingLevel.INFO, log.getName(), "INFLUX SAVE RESULT: ${body}")
//            .end()

