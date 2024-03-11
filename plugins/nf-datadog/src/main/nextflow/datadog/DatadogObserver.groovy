/*
 * Copyright 2021, Seqera Labs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nextflow.datadog

import com.datadog.api.client.ApiClient
import com.datadog.api.client.v2.api.LogsApi
import com.datadog.api.client.v2.api.MetricsApi
import com.datadog.api.client.v2.model.HTTPLogItem
import com.datadog.api.client.v2.model.MetricPayload
import com.datadog.api.client.v2.model.MetricPoint
import com.datadog.api.client.v2.model.MetricSeries
import groovy.json.JsonSlurper
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import jakarta.annotation.Nullable
import nextflow.Session
import nextflow.processor.TaskHandler
import nextflow.processor.TaskProcessor
import nextflow.trace.ResourcesAggregator
import nextflow.trace.TraceObserver
import nextflow.trace.TraceRecord
import nextflow.util.Threads

import java.nio.file.Path

/**
 * Example workflow events observer
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@Slf4j
@CompileStatic
class DatadogObserver implements TraceObserver {

    private Session session

    /**
     * Workflow identifier, will be taken from the Session() object later
     */
    private String runId
    private String runName
    private ResourcesAggregator aggregator

    private ApiClient datadogClient
    LogsApi logsApi
    MetricsApi metricsApi
    DatadogConfig config

    @Override
    void onFlowCreate(Session session) {

        this.config = buildConfig(session.config)

        this.datadogClient =  buildApiClient(config.apiKey,
                null,
                config.site)
        this.logsApi = new LogsApi(datadogClient)
        this.metricsApi = new MetricsApi(datadogClient)
        this.session = session
        this.aggregator = new ResourcesAggregator(session)
        this.runName = session.getRunName()
        this.runId = session.getUniqueId()
    }

    @Override
    void onProcessComplete(TaskHandler handler, TraceRecord trace) {
        final task = handler.task

        def lines = task.isSuccess() ?
                task.dumpStdout(config.getMaxLines()) :
                task.dumpStderr(config.getMaxLines())
        sendLogs(lines, config.getService())
    }

    private sendLogs(List<String> logs, String taskName){
        Threads.start {
            this.logsApi.submitLogAsync(logs.collect {
                new HTTPLogItem(it)
                        .service(config.getService())
                        .ddtags("runName:${this.runName}, task: ${taskName}, runId: ${this.runId}")
            })
        }
    }

    private ApiClient buildApiClient(String apiKey, @Nullable String appKey, @Nullable String site) {
        ApiClient client = ApiClient.defaultApiClient
        if (site != null) {
            HashMap<String, String> serverVariables = new HashMap<String, String>();
            serverVariables.put("site", site);
            client.setServerVariables(serverVariables);
        }
        // Configure API key authorization
        HashMap<String, String> secrets = new HashMap<String, String>();
        if (apiKey != null) {
            secrets.put("apiKeyAuth", apiKey);
        }
        if (appKey != null) {
            secrets.put("appKeyAuth", appKey);
        }
        client.configureApiKeys(secrets);
        return client
    }

    def DatadogConfig buildConfig(Map map) {
        String apiKey = session.config.navigate("datadog.apiKey") as String
        if(!apiKey){
            log.error("Missing datadog apikey configuration. Please add a proper entry in the nextflow config")
            throw new RuntimeException("Missing datadog apiKey configuration")
        }
        String site = session.config.navigate("datadog.site") as String
        if(!site){
            log.error("Missing datadog site configuration. Please add a proper entry in the nextflow config")
            throw new RuntimeException("Missing datadog site configuration")
        }
        Integer maxLines = session.config.navigate("datadog.maxLines") as Integer ?: 50
        String service = session.config.navigate("datadog.service") as String ?: "nextflow"
        return new DatadogConfig(apiKey: apiKey, site:site, maxLines: maxLines, service: service)
    }
}
