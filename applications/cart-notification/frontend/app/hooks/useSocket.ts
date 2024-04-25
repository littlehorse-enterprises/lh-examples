import { useCallback, useEffect, useState } from "react";
import io from "socket.io-client";
import { useCart } from "./useCart";

const NOTIFICATIONS_SERVICE =
  process.env.NOTIFICATIONS_SERVICE || "http://localhost:3001";
const socket = io(NOTIFICATIONS_SERVICE);

type Message = {
  type: "item" | "checkout" | "stale";
  message: string;
};

const useSocket = () => {
  const { cartId } = useCart();
  const [notification, setNotification] = useState<Message>();

  const messageCallback = useCallback(
    (type: Message["type"]) => (message: string) => {
      setNotification({ message, type });
    },
    []
  );

  useEffect(() => {
    socket.on(`item.${cartId}`, messageCallback("item"));
    socket.on(`checkout.${cartId}`, messageCallback("checkout"));
    socket.on(`stale.${cartId}`, messageCallback("stale"));

    return () => {
      socket.off(`item.${cartId}`, messageCallback("item"));
      socket.off(`checkout.${cartId}`, messageCallback("checkout"));
      socket.off(`stale.${cartId}`, messageCallback("stale"));
    };
  });

  return { notification };
};

export default useSocket;
