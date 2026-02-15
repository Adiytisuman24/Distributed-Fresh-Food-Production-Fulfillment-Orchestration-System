const axios = require('axios');

const ORDER_SERVICE_URL = 'http://localhost:8081/api/orders';

async function placeOrder(customerId, storeId) {
    const idempotencyKey = `KEY-${Date.now()}-${Math.random().toString(36).substring(7)}`;
    const orderData = {
        customerId,
        storeId,
        items: [
            { sku: 'PREM_BURGER', quantity: 1, price: 12.50 },
            { sku: 'FRIES_L', quantity: 1, price: 3.50 }
        ]
    };

    try {
        console.log(`Placing order with key: ${idempotencyKey}`);
        const response = await axios.post(ORDER_SERVICE_URL, orderData, {
            headers: { 'X-Idempotency-Key': idempotencyKey }
        });
        console.log(`Order Accepted: ${response.data.orderId}`);
    } catch (error) {
        console.error(`Order Placement Failed: ${error.message}`);
    }
}

// Start simulation: 1 order every 5 seconds
setInterval(() => {
    const stores = ['STORE-01', 'STORE-02', 'STORE-03', 'STORE_OVERLOADED'];
    const randomStore = stores[Math.floor(Math.random() * stores.length)];
    placeOrder('CUST-' + Math.floor(Math.random()*1000), randomStore);
}, 5000);

