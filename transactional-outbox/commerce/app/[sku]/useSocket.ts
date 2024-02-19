import { useCallback, useEffect, useState } from 'react';
import io from 'socket.io-client';
import revalidate from './revalidate';

const NOTIFICATIONS_SERVICE = process.env.NOTIFICATIONS_SERVICE || "http://localhost:3001";
const socket = io(NOTIFICATIONS_SERVICE);
const sockets = ["warehouse", "payments", "failures"]

type Kind = "warehouse" | "payments" | "failures"
type Transaction = {
  account: string
  amount: number
  kind: Kind
}
const useSocket = (sku: string, account: String) => {
  const [transactions, setTransactions] = useState<Transaction[]>([]);

  const transactionsCallback = useCallback((kind: Kind) => {
    return (transaction: Transaction) => {
      setTransactions([{...transaction, kind}, ...transactions.slice(0,4), ] );
      revalidate(sku);
    }
  },
    [sku, transactions],
  );


  useEffect(() => {
    socket.on(`warehouse.${sku}`, transactionsCallback("warehouse"));
    socket.on(`payments.${account}`, transactionsCallback("payments"));
    socket.on(`payments.failures.${account}`, transactionsCallback("failures"));

    return () => {
      socket.off(`warehouse.${sku}`, transactionsCallback("warehouse"));
      socket.off(`payments.${account}`, transactionsCallback("payments"));
      socket.off(`payments.failures.${account}`, transactionsCallback("failures"));
    };
  });

  return { transactions };
}

export default useSocket;
