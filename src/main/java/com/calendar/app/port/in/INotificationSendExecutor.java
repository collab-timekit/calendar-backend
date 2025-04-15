package com.calendar.app.port.in;

import com.calendar.domain.ReminderMethod;

public interface INotificationSendExecutor {
    void sendNotification(ReminderMethod method, String userId, String message);
}