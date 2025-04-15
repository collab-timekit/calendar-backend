package com.calendar.app.port.in;

import com.calendar.domain.ReminderMethod;

public interface NotificationStrategy {
    void execute(String message, String recipient);
    ReminderMethod getReminderMethod();
}
