"use client"
import { FC, PropsWithChildren, createContext } from "react";

type CartContextType = {
  email?: string
  cartId?: string
}
export const CartContext = createContext<CartContextType>({})

export const CartProvider: FC<PropsWithChildren<CartContextType>> = ({children, ...props}) => {
  return (
    <CartContext.Provider value={props}>
      {children}
    </CartContext.Provider>
  )
}
