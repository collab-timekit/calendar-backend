package com.calendar.app.service;

import com.calendar.app.port.in.INotificationSendExecutor;
import com.calendar.app.port.in.NotificationStrategy;
import com.calendar.domain.ReminderMethod;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationSendExecutor implements INotificationSendExecutor {

    private final Map<String, NotificationStrategy> notificationStrategies;

    @Override
    public void sendNotification(ReminderMethod method, String userId, String message) {
        notificationStrategies.get(method.toString()).execute(userId, message);
    }
}
