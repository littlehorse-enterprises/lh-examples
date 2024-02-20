"use client";
import React, { FC, FormEvent, useCallback, useMemo, useState } from "react";
import { useAccountContext } from "./AccountContext";
import { useParams } from "next/navigation";
import submit from "./submit";

type FormProps = {
  stock: number;
  price: number;
};
export const Form: FC<FormProps> = ({ stock, price }) => {
  const { sku } = useParams<{ sku: string }>();
  const { account, setAccount } = useAccountContext();
  const [quantity, setQuantity] = useState(1);
  const [isLoading, setIsLoading] = useState(false);

  const onSubmit = useCallback(
    async (e: FormEvent<HTMLFormElement>) => {
      e.preventDefault();
      setIsLoading(true);
      try {
        await submit({
          gifcard: account,
          quantity,
          price,
          sku,
        });
      } catch (error) {
        console.log(error);
      } finally {
        setIsLoading(false);
      }
    },
    [sku, account, quantity, price],
  );

  return (
    <div className="flex font-sans bg-white shadow-md rounded mb-3">
      <form className="flex-auto w-96 p-6" onSubmit={onSubmit}>
        <div className="flex flex-wrap">
          <h1 className="flex-auto text-lg capitalize font-semibold text-slate-900">
            {sku.toLocaleLowerCase()}
          </h1>
          <div
            className="text-lg font-semibold text-slate-500"
            suppressHydrationWarning
          >
            ${price.toFixed(2)}
          </div>
        </div>
        <div className="flex flex-wrap justify-between items-center w-full">
          <div className="text-sm font-medium text-slate-700 mt-2">
            {stock} In stock
          </div>
          <div className="flex w-48 items-center text-sm font-medium text-slate-700 mt-2">
            <label
              htmlFor="quantity"
              className="block mr-2 flex-1 text-right text-sm font-medium leading-6 text-gray-900"
            >
              Quantity
            </label>
            <input
              type="number"
              name="quantity"
              value={quantity}
              max={9999}
              maxLength={4}
              onChange={(e) => {
                const value = parseInt(e.target.value);
                if (value > 0 && value <= 9999) {
                  setQuantity(value);
                }
              }}
              autoComplete="off"
              className="bg-transparent w-16 text-right pl-2 py-1.5 text-gray-900 placeholder:text-gray-400 focus:ring-0 sm:text-sm sm:leading-6"
            />
          </div>
        </div>
        <div className="flex items-baseline mt-4 mb-6 pb-6 border-b border-slate-200"></div>
        <div className="flex space-x-4 mb-3 text-sm font-medium">
          <div className="flex-auto flex space-x-4">
            <input
              type="text"
              name="code"
              required={true}
              value={account}
              onChange={(e) => {
                setAccount(e.target.value);
              }}
              id="code"
              autoComplete="off"
              className="block rounded flex-1 border bg-transparent py-1.5 pl-1 text-gray-900 placeholder:text-gray-400 focus:ring-0 sm:text-sm sm:leading-6"
              placeholder="Gift Card Code"
            />
            <button
              className="h-10 px-6 font-semibold rounded-md bg-black text-white"
              type="submit"
              disabled={isLoading}
            >
              {isLoading ? "Submitting" : "Place Order"}
            </button>
          </div>
        </div>
      </form>
    </div>
  );
};
