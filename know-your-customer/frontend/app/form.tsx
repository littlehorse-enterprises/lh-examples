"use client";
import { FC } from "react";
import { useFormState } from "react-dom";
import { registerUser } from "./registerUser";
import { Toast } from "@/components/Toast";

export const Form: FC = () => {
  const [{ message, variant }, formAction] = useFormState(registerUser, {});

  return (
    <form className="space-y-6" action={formAction}>
      <Toast message={message} variant={variant} />
      <div>
        <label
          htmlFor="firstname"
          className="block text-sm font-medium leading-6 text-gray-900"
        >
          Firstname
        </label>
        <div className="mt-2">
          <input
            id="firstname"
            name="firstname"
            type="text"
            autoComplete="firstname"
            required
            className="block w-full rounded-md border-0 p-1.5 text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 placeholder:text-gray-400 focus:ring-2 focus:ring-inset focus:ring-slate-600 sm:text-sm sm:leading-6"
          />
        </div>
      </div>

      <div>
        <label
          htmlFor="lastname"
          className="block text-sm font-medium leading-6 text-gray-900"
        >
          Lastname
        </label>
        <div className="mt-2">
          <input
            id="lastname"
            name="lastname"
            type="text"
            autoComplete="lastname"
            required
            className="block w-full rounded-md border-0 p-1.5 text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 placeholder:text-gray-400 focus:ring-2 focus:ring-inset focus:ring-slate-600 sm:text-sm sm:leading-6"
          />
        </div>
      </div>

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
          Create Account
        </button>
      </div>
    </form>
  );
};
