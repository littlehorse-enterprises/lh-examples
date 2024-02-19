"use client";
import { FC, PropsWithChildren, createContext, useCallback, useContext, useState, } from "react";

const AccountContext = createContext({ account: "", setAccount: (name: string) => {} })

export const AccountContextProvider: FC<PropsWithChildren> = ({ children }) => {
  const [account, internalSetAccount] = useState("");

  const setAccount = useCallback((name: string) => {
    internalSetAccount(name);
  }, [internalSetAccount])

  return <AccountContext.Provider value={{account, setAccount}}>
    {children}
  </AccountContext.Provider>
};

export const useAccountContext = () => {
  const { account, setAccount } = useContext(AccountContext);
  return {account, setAccount};
}
