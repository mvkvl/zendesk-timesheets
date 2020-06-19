package ws.slink.config;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InfluxConfig {

    @Bean(name = "influxClient")
    public InfluxDB getInfluxClient(
            @Value("${influxdb.port}")  int port,
            @Value("${influxdb.host}")  String host,
            @Value("${influxdb.proto}") String proto,
            @Value("${influxdb.user}")  String user,
            @Value("${influxdb.pass}")  String pass
    ) {
        return InfluxDBFactory.connect(proto + "://" + host + ":" + port, user, pass);
    }

}
