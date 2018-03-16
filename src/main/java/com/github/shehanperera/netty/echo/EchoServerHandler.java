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

import com.codahale.metrics.Timer;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;

import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * Handler for Netty-echo-server
 */
@ChannelHandler.Sharable
public class EchoServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private final MetricsServer metricServer = MetricsServer.getInstance();

    private int msgSize;
    private Timer.Context context;

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {

        ctx.flush();
    }
    /**
     * Message Handler and sending response
     * @param msg This is the request form client
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg)
            throws Exception {

        try {

            msgSize = msg.content().readableBytes();
            // Number of all income jobs
            metricServer.getTotalJobs().inc();
            // Length of request message calculate by histogram
            metricServer.getRequestSize().update(msgSize);
            // Timer start
            context = metricServer.getResponsesTime().time();
            // Create response
            FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, msg.content().copy());
            response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, msgSize);
            //Length of response
            metricServer.getResponseSize().update(response.content().readableBytes());

            if (ctx != null) {
                //output
                ctx.write(response);
                //Number of success jobs.
                metricServer.getSuccessJobs().inc();
            }

        } finally {
            //Stop timer
            context.stop();
        }
    }
}
