"use client";
import { FC } from "react";
import useSocket from "./useSocket";
import { useParams } from "next/navigation";
import { useAccountContext } from "./AccountContext";
import { Transaction } from "./transaction";

export const Notifications: FC = () => {
  const { account } = useAccountContext();
  const { sku } = useParams<{ sku: string }>();
  const { transactions } = useSocket(sku, account);

  if (transactions.length === 0) return <></>;

  return (
    <div className="flex font-sans bg-white shadow-md rounded">
      <div className="flex-auto w-96 p-6">
        <h2 className="flex-auto text-md capitalize font-semibold text-slate-900 mb-3">
          Transactions
        </h2>
        {transactions.map((tx, i) => {
          return <Transaction key={i} index={i} {...tx} />;
        })}
        {transactions.length === 5 && (
          <div className="text-center text-slate-300">...</div>
        )}
      </div>
    </div>
  );
};
