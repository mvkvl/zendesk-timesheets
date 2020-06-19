package ws.slink.model;

import lombok.Data;
import lombok.ToString;

import java.time.Instant;

@Data
@ToString
public class TimeRecord {

    Long    ticketId;
    String  employee;
    Instant date;
    Long     seconds;

    public TimeRecord ticketId(Long value) {
        this.ticketId = value;
        return this;
    }
    public TimeRecord employee(String value) {
        this.employee = value;
        return this;
    }
    public TimeRecord date(Instant value) {
        this.date = value;
        return this;
    }
    public TimeRecord seconds(Long value) {
        this.seconds = value;
        return this;
    }

}
