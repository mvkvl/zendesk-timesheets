package ws.slink.bean;

import org.influxdb.dto.Point;
import org.springframework.stereotype.Component;
import ws.slink.model.TimeRecord;

import java.util.concurrent.TimeUnit;

@Component
public class TimeRecordToPointConverter {

    public Point documentToPoint(TimeRecord timeRecord) {
        return Point
            .measurement("time-log")
            .addField("seconds", timeRecord.getSeconds())
            .tag("ticketId", timeRecord.getTicketId().toString())
            .tag("employee", timeRecord.getEmployee())
            .tag("priority", timeRecord.getPriority())
            .tag("type", timeRecord.getType())
            .tag("topic", timeRecord.getTopic())
            .tag("app", timeRecord.getApp())
            .time(timeRecord.getDate().toEpochMilli(), TimeUnit.MILLISECONDS)
            .build()
        ;
    }

}
