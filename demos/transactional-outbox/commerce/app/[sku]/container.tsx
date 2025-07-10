"use client";
import { FC, PropsWithChildren } from "react";
import { AccountContextProvider } from "./AccountContext";

export const Container: FC<PropsWithChildren> = ({ children }) => {
  return <AccountContextProvider>
    <div className="">
    {children}
    </div>
  </AccountContextProvider>
}
