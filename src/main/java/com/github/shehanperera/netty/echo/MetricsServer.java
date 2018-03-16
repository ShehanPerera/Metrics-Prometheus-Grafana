/*
 * Copyright 2018 Shehan Perera
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
package com.github.shehanperera.netty.echo;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.Counter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.dropwizard.DropwizardExports;

public class MetricsServer {

    private static MetricsServer metricServer;
    private final MetricRegistry metricRegistry = new MetricRegistry();
    private Histogram responseSize;
    private Timer responsesTime;
    private Counter totalJobs;
    private Counter successJobs;
    private Histogram requestSize;
    private JmxReporter JmxReporter;
    private ConsoleReporter ConsoleReporter;

    private MetricsServer() {

        responsesTime = this.metricRegistry.timer("Time To Response");
        responseSize = this.metricRegistry.histogram("Response Size");
        requestSize = this.metricRegistry.histogram("Request Size");
        totalJobs = this.metricRegistry.counter("Total Jobs");
        successJobs=this.metricRegistry.counter("Success Jobs");
    }

    public static MetricsServer getInstance() {

        if (metricServer == null) {
            metricServer = new MetricsServer();
        }
        return metricServer;
    }

    public Timer getResponsesTime() {

        return responsesTime;
    }

    public Histogram getResponseSize() {

        return responseSize;
    }

    public Histogram getRequestSize() {

        return requestSize;
    }

    public Counter getSuccessJobs() {

        return successJobs;
    }

    public Counter getTotalJobs() {

        return totalJobs;
    }

    public void startReport() {
        /*
        * This for Console reporter
        * Period apply to 2 for make easy view of output
        */

/*       ConsoleReporter = ConsoleReporter.forRegistry(metricRegistry)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build();
        ConsoleReporter.start(1, TimeUnit.SECONDS);
*/

/*
  *This the reporter for Prometheus
*/
        CollectorRegistry.defaultRegistry.register(new DropwizardExports(metricRegistry));

        // Expose Prometheus metrics.
        PrometheusServer prometheusServer = new PrometheusServer(CollectorRegistry.defaultRegistry, 9092);
        prometheusServer.start();

       /*
          * This for JMX reporter
      */
        JmxReporter = JmxReporter.forRegistry(metricRegistry).build();
        JmxReporter.start();
    }

}