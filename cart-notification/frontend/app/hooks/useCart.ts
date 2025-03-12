import { useContext } from "react"
import { CartContext } from "../contexts/CartContext"

export const useCart = () => {
  const { cartId, email } = useContext(CartContext)
  return {cartId, email}
}
