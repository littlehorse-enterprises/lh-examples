package io.littlehorse.document.processing.tasks;

import io.littlehorse.sdk.worker.LHTaskMethod;

public class NotifySubmitterTask {

	@LHTaskMethod("notify-processing-success")
	public String notifyProcessingSuccess(String submitterId, String documentId, String status) {
		return notifySubmitter(submitterId, documentId, status);
	}

	@LHTaskMethod("notify-processing-failure")
	public String notifyProcessingFailure(String submitterId, String documentId, String status) {
		return notifySubmitter(submitterId, documentId, status);
	}

	// Helper method to avoid code duplication
	private String notifySubmitter(String submitterId, String documentId, String status) {
		System.out.println("Notifying submitter " + submitterId + " about document " + documentId);
		System.out.println("Status: " + status);

		try {
			// Simulate notification sending
			Thread.sleep(500);

			// In a real scenario, this would send an email, SMS, or other notification
			String notificationId = "NOTIF-" + System.currentTimeMillis();

			System.out.println("Notification sent with ID: " + notificationId);
			return notificationId;

		} catch (Exception e) {
			System.err.println("Error sending notification: " + e.getMessage());
			// We'll still return a notification ID even if there's an error
			// In a real system, you might want to throw an exception to trigger retries
			return "ERROR-NOTIFICATION-" + System.currentTimeMillis();
		}
	}

	@LHTaskMethod("log-message")
	public String logMessage(String message) {
		System.out.println("LOG: " + message);
		return "Logged at " + System.currentTimeMillis();
	}
}