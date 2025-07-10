package io.littlehorse.incident.responder.tasks;

import java.util.Random;

import io.littlehorse.incident.responder.LHConstants;
import io.littlehorse.sdk.worker.LHTaskMethod;

public class NotifyStatusTask {
	private final Random random = new Random();

	@LHTaskMethod(LHConstants.TASK_NOTIFY_STATUS)
	public String notifyStatus(String alertId, String systemName, String status, String details) throws Exception {
		// Simulate API failure ~33% of the time
		if (random.nextInt(3) == 0) {
			throw new Exception("API failure: Notification service unavailable");
		}

		// If details is null, use a default message
		String detailsMsg = (details != null) ? " - Details: " + details : "";

		return "Successfully logged status update for alert " + alertId + " on system " + systemName +
				" with status: " + status + detailsMsg;
	}
}