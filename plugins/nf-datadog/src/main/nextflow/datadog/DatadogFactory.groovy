package nextflow.datadog;

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j;
import nextflow.Session;
import nextflow.trace.TraceObserver;
import nextflow.trace.TraceObserverFactory;

import java.util.ArrayList;
import java.util.Collection;

@CompileStatic
@Slf4j
class DatadogFactory implements TraceObserverFactory {

    @Override
    Collection<TraceObserver> create(Session session) {
        final result = new ArrayList()
        result.add( new DatadogObserver() )
        return result
    }
}
