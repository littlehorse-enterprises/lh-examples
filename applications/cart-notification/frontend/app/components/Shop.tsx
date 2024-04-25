"use client";
import Image from "next/image";
import { FC, useCallback, useEffect, useState } from "react";
import Hat from "../../public/Hat.jpeg";
import Mug from "../../public/Mug.jpeg";
import Tshirt from "../../public/T-shirt.jpeg";
import { logout } from "../actions/logout";
import { updateProducts } from "../actions/updateProducts";
import { useCart } from "../hooks/useCart";
const products = [
  { name: "T-shirt", price: 20, image: Tshirt },
  { name: "Mug", price: 5, image: Mug },
  { name: "Hat", price: 10, image: Hat },
];

export type Product = {
  name: string;
  price: number;
  quantity: number;
};

export const Shop: FC<{}> = () => {
  const { email, cartId } = useCart();
  const [cartItems, setCartItems] = useState<Product[]>([]);

  const addToCart = useCallback(
    (product: Omit<Product, "quantity">) => {
      const existingProduct = cartItems.find((p) => p.name === product.name);
      if (existingProduct) {
        console.log("existingProduct", existingProduct);
        existingProduct.quantity++;
      }
      setCartItems(
        existingProduct
          ? [...cartItems]
          : [...cartItems, { ...product, quantity: 1 }]
      );
    },
    [cartItems]
  );

  useEffect(() => {
    if (cartId && cartItems.length > 0) {
      updateProducts(cartId, cartItems);
    }
  });

  return (
    <div className="mt-8">
      <h1 className="text-3xl text-center font-bold">Shop</h1>
      <p className="text-gray-500 text-center">
        <span>Welcome, {email}! </span>
        <a
          className="text-blue-500 hover:underline cursor-pointer"
          onClick={() => {
            logout();
          }}
        >
          (logout)
        </a>
      </p>
      <div className="flow-root mt-4 bg-white rounded p-4">
        <ul role="list" className="-my-6 divide-y divide-gray-200">
          {products.map(({ name, price, image }) => (
            <li key={name} className="flex py-6">
              <div className="h-24 w-24 flex-shrink-0 overflow-hidden rounded-md border border-gray-200">
                <Image
                  src={image}
                  alt="Salmon orange fabric pouch with match zipper, gray zipper pull, and adjustable hip belt."
                  className="h-full w-full object-cover object-center"
                />
              </div>

              <div className="ml-4 flex flex-1 flex-col">
                <div>
                  <div className="flex justify-between text-base font-medium text-gray-900">
                    <h3>
                      <a href="#">{name}</a>
                    </h3>
                    <p className="ml-4">${price}</p>
                  </div>
                </div>
                <div className="flex flex-1 items-center justify-between text-sm">
                  <button
                    onClick={(e) => {
                      e.preventDefault();
                      addToCart({ name, price });
                    }}
                    className="text-white bg-blue-500 hover:bg-blue-600 px-4 py-2 w-full rounded-md"
                  >
                    Add to Cart
                  </button>
                </div>
              </div>
            </li>
          ))}
        </ul>
      </div>

      {cartItems.length > 0 && (
        <div className="mt-4">
          <div className="">
            <h2 className="text-center text-xl">In your cart</h2>
            <div className="mb-2">
              <ul>
                {cartItems.map((item) => (
                  <li key={item.name} className="flex justify-between">
                    <div>
                      {item.name} x{item.quantity}
                    </div>
                    <div className="">${item.price * item.quantity}</div>
                  </li>
                ))}
              </ul>
            </div>
          </div>
          <button
            onClick={() => {}}
            className="text-white bg-green-500 w-full hover:bg-blue-600 px-4 py-2 rounded-md"
          >
            Checkout
          </button>
        </div>
      )}
    </div>
  );
};
