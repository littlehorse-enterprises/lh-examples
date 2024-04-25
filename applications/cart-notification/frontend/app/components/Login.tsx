import { FC } from "react";
import { Form } from "./Form";

export const Login: FC<{}> = () => {
  return (
    <div className="flex font-sans bg-white shadow-md rounded">
      <div className="flex-auto w-96 p-6">
        <h1 className="text-lg font-bold mb-4">Login</h1>
        <Form />
      </div>
    </div>
  );
};
