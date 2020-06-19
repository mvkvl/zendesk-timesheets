package ws.slink.bean;

import org.influxdb.InfluxDB;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class PointBatchBean {

    @Value("${influxdb.db}")
    String influxDb;

    public BatchPoints build(Collection<Point> points) {
        BatchPoints.Builder bpb =
            BatchPoints
                .database(influxDb)
                .precision(TimeUnit.MILLISECONDS)
                .consistency(InfluxDB.ConsistencyLevel.ANY)
        ;
        points.stream().forEach(bpb::point);
        return bpb.build();
    }

    public String print(Collection<Point> points) {
        return points.stream().map(Point::toString).collect(Collectors.joining(", "));
    }

    public String print(BatchPoints points) {
        return points.getPoints().size() + " : " + points.toString();
    }

}
