package nextflow.datadog

import groovy.transform.CompileStatic
import groovy.transform.PackageScope


/**
 * This class allows model an specific configuration, extracting values from a map and converting
 *
 *
 * @author : swampie <matteo.fiandesio@gmail.com>
 *
 */
@PackageScope
@CompileStatic
class DatadogConfig {

    final private String apiKey
    final private String site
    final private Integer maxLines
    final private String service

    DatadogConfig(Map map){
        def config = map ?: Collections.emptyMap()
        apiKey = config.apiKey as String
        site = config.site as String
        maxLines = config.maxLines as Integer
        service =config.service as String
    }

    String getApiKey(){
        return apiKey
    }

    String getSite(){
        return site
    }

    Integer getMaxLines(){
        return maxLines
    }

    String getService() {
        return service
    }

}
