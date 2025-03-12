package io.littlehorse.document.processing.tasks;

import java.util.Random;

/**
 * Base class for all API-calling tasks that simulates occasional failures
 */
public class BaseApiTask {
	protected final Random random = new Random();
	protected final double failureRate;

	public BaseApiTask() {
		// Default 33% failure rate
		this(0.33);
	}

	public BaseApiTask(double failureRate) {
		this.failureRate = failureRate;
	}

	/**
	 * Simulate potential API failure based on the configured failure rate
	 */
	protected void simulateApiCall(String apiName) throws Exception {
		if (random.nextDouble() < failureRate) {
			throw new Exception("API failure: " + apiName + " service is unavailable");
		}
	}
}