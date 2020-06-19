package ws.slink.model;

import lombok.Data;
import lombok.ToString;

import java.time.Instant;

@Data
@ToString
public class TimeRecord {

    Long    ticketId;
    String  employee;
    String  priority;
    Long     seconds;
    String     topic;
    String      type;
    String       app;
    Instant     date;

    public TimeRecord ticketId(Long value) {
        this.ticketId = value;
        return this;
    }
    public TimeRecord employee(String value) {
        this.employee = value;
        return this;
    }
    public TimeRecord priority(String value) {
        this.priority = value;
        return this;
    }
    public TimeRecord seconds(Long value) {
        this.seconds = value;
        return this;
    }
    public TimeRecord topic(String value) {
        this.topic = value;
        return this;
    }
    public TimeRecord date(Instant value) {
        this.date = value;
        return this;
    }
    public TimeRecord type(String value) {
        this.type = value;
        return this;
    }
    public TimeRecord app(String value) {
        this.app = value;
        return this;
    }

}
