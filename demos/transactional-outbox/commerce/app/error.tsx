"use client";

import { useEffect } from "react";

export default function Error({ error }: { error: Error & { digest?: string } }) {
  useEffect(() => {
    // Log the error to an error reporting service
    console.error(error);
  }, [error]);

  return (
      <div className="flex font-sans bg-white shadow-md rounded mb-3">
      <div className="flex-auto w-96 p-6">
      <h2 className="text-lg font-bold mb-3">Something went wrong!</h2>
      <p>
        If you see this you probably haven&apos;t setup the local environment
        correctly or you&apos;re trying to access a not yet created product sku.
      </p>
    </div>
    </div>
  );
}
