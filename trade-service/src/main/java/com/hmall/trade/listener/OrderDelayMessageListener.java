package com.hmall.trade.listener;

import com.hmall.api.client.PayClient;
import com.hmall.api.dto.PayOrderDTO;
import com.hmall.trade.constants.MQConstants;
import com.hmall.trade.domain.po.Order;
import com.hmall.trade.service.IOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderDelayMessageListener {

    private final IOrderService orderService;

    private final PayClient payClient;
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MQConstants.DELAY_QRDER_QUEUE_NAME),
            exchange = @Exchange(name = MQConstants.DELAY_EXCHANGE_NAME, delayed = "true"),
            key = MQConstants.DELAY_QRDER_KEY
    ))
    public void listenOrderDelayMessage(Long orderId){
        // 查询订单 数据库
        Order order = orderService.getById(orderId);
        // 检查订单状态
        if(order == null || order.getStatus() != 1){
            return;
        }
        // 查询流水状态
        PayOrderDTO payOrder = payClient.queryPayOrderByBizOrderNo(orderId);
        // 已支付
        if(payOrder != null && payOrder.getStatus() == 3){
            orderService.markOrderPaySuccess(orderId);
        }else{// 未支付
            orderService.cancelOrder(orderId);
        }

    }
}
