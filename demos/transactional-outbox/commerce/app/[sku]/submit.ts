"use server";

const PAYMENTS_API = process.env.PAYMENTS_API || "http://localhost:8082"

type CommerceData = {
  gifcard: string,
  sku: string,
  price: number,
  quantity: number,
}

export default async function submit(commerce: CommerceData) {
  fetch(`${PAYMENTS_API}/run-workflow`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify(commerce),
  });
}
