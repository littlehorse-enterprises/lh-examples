"use client";
import { Toast } from "@/app/components/Toast";
import { FC } from "react";
import { useFormState } from "react-dom";
import { login } from "../actions/login";

export const Form: FC = () => {
  const [{ message, variant }, formAction] = useFormState(login, {});

  return (
    <form className="space-y-6" action={formAction}>
      <Toast message={message} variant={variant} />
      <div>
        <label
          htmlFor="email"
          className="block text-sm font-medium leading-6 text-gray-900"
        >
          Email address
        </label>
        <div className="mt-2">
          <input
            id="email"
            name="email"
            type="email"
            autoComplete="email"
            required
            className="block w-full rounded-md border-0 p-1.5 text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 placeholder:text-gray-400 focus:ring-2 focus:ring-inset focus:ring-slate-600 sm:text-sm sm:leading-6"
          />
        </div>
      </div>

      <div>
        <button
          type="submit"
          className="flex w-full justify-center rounded-md bg-slate-600 px-3 py-1.5 text-sm font-semibold leading-6 text-white shadow-sm hover:bg-slate-500 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-slate-600"
        >
          Login
        </button>
      </div>
    </form>
  );
};
