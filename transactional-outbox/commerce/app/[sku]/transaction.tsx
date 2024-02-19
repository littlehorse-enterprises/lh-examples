import { FC, useMemo } from "react";

type TransactionProps = {
  account: string;
  amount: number;
  index: number;
  kind?: "warehouse" | "payments" | "failures"
}

export const Transaction: FC<TransactionProps> = ({
  account,
  amount,
  kind = "payments",
  index
}) => {

  const { title, message, color } = getData(kind, amount, account);
  const opacity = useMemo(() => {
    return 100 - index * 20;
  }, [index])
  return (
    <div className={`bg-${color} opacity-${opacity} flex flex-wrap border px-4 py-2 rounded mb-2`}>
      <div className="w-full flex-none text-sm font-bold text-slate-700">{title}</div>
      <div className="w-full flex-none text-xs font-light text-slate-700">
        {message}
      </div>
    </div>
  );
};

const getData = (kind: TransactionProps["kind"], amount: number, account: string) => {
  if (kind === "payments") {
    if (amount > 0) {
      return {
        title: "Out of stock",
        message: `Refund issued to ${account} for $${amount}`,
        color: "yellow-50",
      };
    } else {
      return {
        title: "Payment",
        message: `Received $${amount * -1} from ${account}`,
        color: "green-50",
      };
    }
  } else if (kind === "warehouse") {
    return {
      title: "Shipping",
      message: `Shipped ${amount * -1} ${account} successfully`,
      color: "sky-50"
    }
  } else {
    return {
      title: "Failure",
      message: `Not enough balance in account ${account}`,
      color: "red-50"
    }
  }
}
