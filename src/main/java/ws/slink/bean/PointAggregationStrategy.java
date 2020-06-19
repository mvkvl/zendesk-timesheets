package ws.slink.bean;

import org.apache.camel.Exchange;
import org.apache.camel.processor.aggregate.AggregationStrategy;
import org.influxdb.dto.Point;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class PointAggregationStrategy implements AggregationStrategy {

    @Override
    public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {

        if (oldExchange == null) {
            // we start a new correlation group, so complete all previous groups
            newExchange.setProperty(Exchange.AGGREGATION_COMPLETE_ALL_GROUPS, true);
            return newExchange;
        }

        Object oldObject = oldExchange.getIn().getBody();
        Object newObject = newExchange.getIn().getBody();

        if (oldObject instanceof Point && newObject instanceof Point) {
            oldExchange.getIn().setBody(new ArrayList(Arrays.asList((Point)oldObject, (Point)newObject)));
        } else if (oldObject instanceof List) {
            ((List) oldObject).add((Point) newObject);
            oldExchange.getIn().setBody(oldObject);
        }

        return oldExchange;
    }
}
