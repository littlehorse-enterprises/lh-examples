import React, { FC } from 'react'

const VARIANTS = {
  success: "bg-white",
  error: "bg-red-500",
};

export type ToastProps = {
  message?: string
  variant?: keyof typeof VARIANTS
}

export const Toast: FC<ToastProps> = ({message, variant = "success"}) => {
  if (!message) return <></>

  return (
    <div
      id="toast-simple"
      className={`${VARIANTS[variant]} flex items-center w-full p-4 space-x-4 rtl:space-x-reverse text-gray-500 divide-x rtl:divide-x-reverse divide-gray-200 rounded-lg shadow space-x`}
      role="alert"
    >
      <svg
        className="w-5 h-5 text-slate-600 dark:text-slate-500 rotate-45"
        aria-hidden="true"
        xmlns="http://www.w3.org/2000/svg"
        fill="none"
        viewBox="0 0 18 20"
      >
        <path
          stroke="currentColor"
          strokeLinecap="round"
          strokeLinejoin="round"
          strokeWidth="2"
          d="m9 17 8 2L9 1 1 19l8-2Zm0 0V9"
        />
      </svg>
      <div className="ps-4 text-sm font-normal">{message}</div>
    </div>
  );
}
