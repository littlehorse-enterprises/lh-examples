"use server";

import { getClient } from "@/lib/lhClient";
import { createHash } from "crypto";
import { writeFile } from "fs/promises";
import { join } from "path";

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

export async function createNewChat(initialMessage: string) {
  const lhClient = await getClient({ tenantId: "default" });
  
  // Generate a unique ID for the chat
  const chatId = createHash("sha256")
    .update(Date.now().toString())
    .digest("hex");

  // Start the chat workflow with initial message
  await lhClient.runWf({
    wfSpecName: "chat-with-llm",
    id: chatId,
    variables: {
      "initial-user-message": { str: initialMessage },
    },
  });

  return chatId;
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

export async function deleteChat(wfRunId: string) {
  const lhClient = await getClient({ tenantId: "default" });
  await lhClient.deleteWfRun({ id: { id: wfRunId } });
}

export async function uploadPdf(formData: FormData) {
  const file = formData.get("file") as File;
  if (!file || !file.name.endsWith(".pdf")) {
    throw new Error("Please upload a PDF file");
  }

  // Calculate SHA256 hash of the file
  const bytes = await file.arrayBuffer();
  const buffer = Buffer.from(bytes);
  const hash = createHash("sha256").update(buffer).digest("hex");

  // Save to temp directory
  const tempPath = join(process.cwd(), "../backend/temp", file.name);
  await writeFile(tempPath, buffer);

  // Start the RAG workflow
  const lhClient = await getClient({ tenantId: "default" });
  await lhClient.runWf({
    wfSpecName: "load-chunk-embed-pdf",
    id: hash,
    variables: {
      "s3-id": { str: file.name },
    },
  });

  return { success: true, wfRunId: hash };
}
