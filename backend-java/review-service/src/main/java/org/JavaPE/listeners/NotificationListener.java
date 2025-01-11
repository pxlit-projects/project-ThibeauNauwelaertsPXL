package org.JavaPE.listeners;

import org.JavaPE.controller.dto.NotificationMessage;
import org.JavaPE.services.ReviewServiceImpl;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class NotificationListener {

    private final ReviewServiceImpl reviewService;

    // Inject your service
    public NotificationListener(ReviewServiceImpl reviewService) {
        this.reviewService = reviewService;
    }

    @RabbitListener(queues = "notificationQueue")
    public void handleNotification(NotificationMessage message) {
        System.out.println("Received notification: " + message);

        if ("approved".equals(message.getStatus())) {
            System.out.println("Post " + message.getPostId() + " approved by " + message.getReviewer());
            reviewService.publishToSseClients(message);

        } else if ("rejected".equals(message.getStatus())) {
            System.out.println("Post " + message.getPostId() + " rejected by " + message.getReviewer()
                    + " with remarks: " + message.getRemarks());
            reviewService.publishToSseClients(message);
        }
    }
}
