package ws.slink.bean;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.influxdb.InfluxDB;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.zendesk.client.v2.model.Ticket;
import ws.slink.config.AppConfig;
import ws.slink.model.TimeRecord;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PointRemover {

    private final @NonNull InfluxDB influxDB;
    private final @NonNull AppConfig appConfig;

    public void remove(List<Ticket> timeRecords) {
        String where = timeRecords
            .stream()
            .map(t -> t.getId())
            .distinct()
            .map(id -> "ticketId='" + id + "'")
            .collect(Collectors.joining(" OR "));
        log.info("DROP POINTS WHERE {}", where);
        Query query = new Query("DROP SERIES FROM \"time-log\" WHERE " + where + ";"
                              , appConfig.influxDb()
                              , false);
        QueryResult result = influxDB.query(query);
        if (null != result.getError())
            log.warn("ERROR: {}", result.getError());
    }

}
