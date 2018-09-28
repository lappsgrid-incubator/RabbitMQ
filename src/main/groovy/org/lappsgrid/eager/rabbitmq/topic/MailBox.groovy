package org.lappsgrid.eager.rabbitmq.topic

import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Channel
import com.rabbitmq.client.DefaultConsumer
import com.rabbitmq.client.Envelope
import org.lappsgrid.eager.rabbitmq.RabbitMQ

/**
 *
 */
abstract class MailBox extends RabbitMQ {
    String exchange

    MailBox(String exchange, String address) {
        this(exchange, address, DEFAULT_HOST)
    }

    MailBox(String exchange, String address, String host) {
        super('', host)
        channel.exchangeDeclare(exchange, "direct");
        this.queueName = channel.queueDeclare().getQueue();
        this.channel.queueBind(queueName, exchange, address)
        this.channel.basicConsume(queueName, false, new Consumer(this))
    }

    public abstract void recv(String message);

    class Consumer extends DefaultConsumer {
        MailBox box

        Consumer(MailBox  box) {
            super(box.channel)
            this.box = box;
        }

        void handleDelivery(String consumerTag, Envelope envelope,
                            AMQP.BasicProperties properties, byte[] body)
                throws IOException {
            String message = new String(body, "UTF-8");
            println "Consumer handle delivery: $message"
            try {
                box.recv(message)
                this.channel.basicAck(envelope.deliveryTag, false)
            }
            catch (Exception e) {
                e.printStackTrace()
            }
        }

    }
}
