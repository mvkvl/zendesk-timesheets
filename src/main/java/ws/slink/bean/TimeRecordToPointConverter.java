package ws.slink.bean;

import org.influxdb.dto.Point;
import org.springframework.stereotype.Component;
import ws.slink.model.TimeRecord;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class TimeRecordToPointConverter {

    public List<Point> timeRecordsToPoint(List<TimeRecord> timeRecords) {
        return timeRecords
            .stream()
            .map(this::timeRecordToPoint)
            .collect(Collectors.toList())
        ;
    }

    private Point timeRecordToPoint(TimeRecord timeRecord) {
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
