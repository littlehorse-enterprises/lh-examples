"use client";
import { Toast } from "@/components/Toast";
import { FC, useState } from "react";
import { useFormState } from "react-dom";
import { uploadPassport } from "./uploadPassport";

type FormProps = {
  wfRunId: string;
};

export const Form: FC<FormProps> = ({ wfRunId }) => {
  const [{ message }, formAction] = useFormState(uploadPassport, { wfRunId });
  const [checked, setChecked] = useState(true);

  if (message) return <Toast message={message} />;

  return (
    <form className="space-y-6" action={formAction}>
      <div>
        <label
          htmlFor="passport"
          className="block text-sm font-medium leading-6 text-gray-900"
        >
          Passport (Dummy file upload)
        </label>
        <div className="mt-2">
          <input
            id="passport"
            name="passport"
            type="file"
            required
            className="block w-full rounded-md border-0 p-1.5 text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 placeholder:text-gray-400 focus:ring-2 focus:ring-inset focus:ring-slate-600 sm:text-sm sm:leading-6"
          />
        </div>
      </div>

      <div>
        <label
          htmlFor="passport"
          className="block text-sm font-medium leading-6 text-gray-900"
        >
          Is Legit? (force manual verification if unchecked)
        </label>
        <div className="mt-2">
          <input
            id="legit"
            name="legit"
            type="checkbox"
            checked={checked}
            onChange={() => setChecked(!checked)}
          />
        </div>
      </div>

      <div>
        <button
          type="submit"
          className="flex w-full justify-center rounded-md bg-slate-600 px-3 py-1.5 text-sm font-semibold leading-6 text-white shadow-sm hover:bg-slate-500 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-slate-600"
        >
          Upload Passport
        </button>
      </div>
    </form>
  );
};
