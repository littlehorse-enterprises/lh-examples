import React, { useState } from 'react';
import './App.css';

function App() {
  const [cartStatus, setCartStatus] = useState('Empty');

  const addItemToCart = async () => {
    try {
      const response = await fetch('/addItem', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ itemName: 'Example Item' })
      });
      if (response.ok) {
        setCartStatus('Not Empty');
      } else {
        console.error('Failed to add item to cart');
      }
    } catch (error) {
      console.error('Failed to add item to cart:', error);
    }
  };

  return (
    <div className="App">
      <h1>Cart Reminder Notifier</h1>
      <div id="cartStatus">Cart Status: {cartStatus}</div>
      <button id="addItemButton" onClick={addItemToCart}>Add Item to Cart</button>
    </div>
  );
}

export default App;
