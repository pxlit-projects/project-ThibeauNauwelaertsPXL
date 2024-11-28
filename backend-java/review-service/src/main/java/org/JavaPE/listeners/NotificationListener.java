package org.JavaPE.listeners;

import org.JavaPE.controller.dto.NotificationMessage;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class NotificationListener {

    @RabbitListener(queues = "notificationQueue")
    public void handleNotification(NotificationMessage message) {
        System.out.println("Received notification: " + message);

        if ("approved".equals(message.getStatus())) {
            System.out.println("Post " + message.getPostId() + " has been approved by " + message.getReviewer());
        } else if ("rejected".equals(message.getStatus())) {
            System.out.println("Post " + message.getPostId() + " has been rejected by " + message.getReviewer()
                    + " with remarks: " + message.getRemarks());
        }
    }
}
