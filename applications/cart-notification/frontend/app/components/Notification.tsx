"use client";
import { FC, useEffect, useState } from "react";
import useSocket from "../hooks/useSocket";
import { CheckCircleIcon, PlusCircleIcon, ShoppingCartIcon } from "@heroicons/react/20/solid";

export const Notification: FC = () => {
  const { notification } = useSocket();
  const [open, setOpen] = useState(true);

  useEffect(() => { 
    setOpen(true);
  }, [notification]);

  useEffect(() => { 
    if (notification && notification.type !== "stale") {
      const timer = setTimeout(() => {
        setOpen(false);
      }, 5000);
      return () => clearTimeout(timer);
    }
  }, [notification]);

  if (notification && open) {
    return (
      <div
        className="flex fixed top-3 right-3 items-center w-full max-w-xs p-4 mb-4 text-gray-500 bg-white rounded-lg shadow dark:text-gray-400 dark:bg-gray-800"
        role="alert"
      >
        <div className="inline-flex items-center justify-center flex-shrink-0 w-8 h-8 text-green-500 bg-green-100 rounded-lg dark:bg-green-800 dark:text-green-200">
          {notification.type === "stale" && (<ShoppingCartIcon className="w-5 h-5" />)}
          {notification.type === "checkout" && (<CheckCircleIcon className="w-5 h-5" />)}
          {notification.type === "item" && (<PlusCircleIcon className="w-5 h-5" />)}
        </div>
        <div className="ms-3 text-sm font-normal">{notification.message}</div>
        <button
          onClick={() => setOpen(false)}
          type="button"
          className="ms-auto -mx-1.5 -my-1.5 bg-white text-gray-400 hover:text-gray-900 rounded-lg focus:ring-2 focus:ring-gray-300 p-1.5 hover:bg-gray-100 inline-flex items-center justify-center h-8 w-8 dark:text-gray-500 dark:hover:text-white dark:bg-gray-800 dark:hover:bg-gray-700"
          data-dismiss-target="#toast-success"
          aria-label="Close"
        >
          <span className="sr-only">Close</span>
          <svg
            className="w-3 h-3"
            aria-hidden="true"
            xmlns="http://www.w3.org/2000/svg"
            fill="none"
            viewBox="0 0 14 14"
          >
            <path
              stroke="currentColor"
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="m1 1 6 6m0 0 6 6M7 7l6-6M7 7l-6 6"
            />
          </svg>
        </button>
      </div>
      // <div
      //   className={`fixed top-5 right-5 ${
      //     notification.type === "success" ? "bg-green-100" : "bg-red-100"
      //   } p-4 border border-gray-200 rounded`}
      // >
      //   {notification.message}
      // </div>
    );
  }
  return <></>;
};
