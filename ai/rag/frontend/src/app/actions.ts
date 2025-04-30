"use server";

import { getClient } from "@/lib/lhClient";

export async function getChatHistories() {
  const lhClient = await getClient({ tenantId: "default" });

  return await Promise.all(
    (
      await lhClient.searchWfRun({
        limit: 999,
        wfSpecName: "chat-with-llm",
      })
    ).results.map(async ({ id }) => {
      const chatHistory = await lhClient.getVariable({
        wfRunId: { id },
        name: "chat-history",
      });
      return {
        wfRunId: id,
        history: JSON.parse(chatHistory.value?.jsonArr ?? "[]"),
      };
    })
  );
}

export async function postChatMessage(message: string, wfRunId: string) {
  const lhClient = await getClient({ tenantId: "default" });

  await lhClient.putExternalEvent({
    externalEventDefId: { name: "user-message" },
    wfRunId: { id: wfRunId },
    content: {
      str: message,
    },
  });
}
