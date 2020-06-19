package ws.slink.bean;

import org.apache.camel.Exchange;
import org.apache.camel.processor.aggregate.AggregationStrategy;
import org.influxdb.dto.Point;
import org.springframework.stereotype.Component;
import ws.slink.model.TimeRecord;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class TimeRecordAggregationStrategy implements AggregationStrategy {

    @Override
    public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {

        if (oldExchange == null) {
            // we start a new correlation group, so complete all previous groups
            newExchange.setProperty(Exchange.AGGREGATION_COMPLETE_ALL_GROUPS, true);
            return newExchange;
        }

        Object oldObject = oldExchange.getIn().getBody();
        Object newObject = newExchange.getIn().getBody();

        if (oldObject instanceof TimeRecord && newObject instanceof TimeRecord) {
            oldExchange.getIn().setBody(new ArrayList(Arrays.asList((TimeRecord)oldObject, (TimeRecord)newObject)));
        } else if (oldObject instanceof List) {
            ((List) oldObject).add((TimeRecord) newObject);
            oldExchange.getIn().setBody(oldObject);
        }

        return oldExchange;
    }
}
