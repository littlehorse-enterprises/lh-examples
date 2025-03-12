package io.littlehorse.workflow;

import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.sdk.worker.WorkerContext;

public class Task {
  protected NotificationsService notificationsService;

  public Task(NotificationsService notificationsServices) {
    this.notificationsService = notificationsServices;
  }

  @LHTaskMethod("notify-cart")
  public String notifyCart(String account, WorkerContext context) throws Exception {
    Message message = new Message("You have items in your cart, please checkout", context.getWfRunId().getId());
    this.notificationsService.publishMessage("stale", message);
    return "Notification sent";
  }
  
  @LHTaskMethod("notify-add-to-cart")
  public String addToCart(String products, WorkerContext context) throws Exception {
    Message message = new Message("Product added to cart", context.getWfRunId().getId());
    this.notificationsService.publishMessage("item", message);
    return "Product added to cart";
  }

  @LHTaskMethod("notify-checkout")
  public String checkout(String account, WorkerContext context) throws Exception {
    Message message = new Message("Checkout completed", context.getWfRunId().getId());
    this.notificationsService.publishMessage("checkout", message);
    return "Checkout completed";
  }
}
