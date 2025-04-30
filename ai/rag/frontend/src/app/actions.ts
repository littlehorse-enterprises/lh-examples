"use server";

import { getClient } from "@/lib/lhClient";
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

export async function uploadPdf(formData: FormData) {
  const file = formData.get("file") as File;
  if (!file || !file.name.endsWith(".pdf")) {
    throw new Error("Please upload a PDF file");
  }

  // Save to temp directory
  const bytes = await file.arrayBuffer();
  const buffer = Buffer.from(bytes);
  const tempPath = join(process.cwd(), "../backend/temp", file.name);
  await writeFile(tempPath, buffer);

  // Start the RAG workflow
  const lhClient = await getClient({ tenantId: "default" });
  await lhClient.runWf({
    wfSpecName: "load-chunk-embed-pdf",
    variables: {
      "s3-id": { str: file.name },
    },
  });

  return { success: true };
}
