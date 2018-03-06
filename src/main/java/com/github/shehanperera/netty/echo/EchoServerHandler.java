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
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;
/**
 * Handler for Netty-echo-server
 */
@ChannelHandler.Sharable
public class EchoServerHandler extends ChannelInboundHandlerAdapter {

    private Timer.Context context;
    /**
     *ChannelRead in used to get the request and send the response back
     * @param msg this is the request msg from client
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {

        MetricsServer metricsServer = MetricsServer.getInstance();
        context = metricsServer.getResponsesTime().time();
        metricsServer.getTotallJobs().inc();
        ByteBuf response = (ByteBuf) msg;
        metricsServer.getResponseSize().update(response.readableBytes());
        System.out.println("Server received: " + response.toString(CharsetUtil.UTF_8));
        ctx.write(response);
        context.stop();
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {

        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {

        cause.printStackTrace();
        ctx.close();
    }
}