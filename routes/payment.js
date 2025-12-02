const express = require('express');
const router = express.Router();
const momoPayment = require('../services/momoPayment');

// Tạo thanh toán MoMo
router.post('/momo/create', async (req, res) => {
  try {
    const { amount, orderInfo } = req.body;
    const orderId = `DONATE_${Date.now()}`;
    
    const response = await momoPayment.createPayment(
      orderId,
      amount,
      orderInfo || 'Thanh toán quyên góp từ thiện'
    );

    if (response.data.resultCode === 0) {
      res.json({
        success: true,
        payUrl: response.data.payUrl
      });
    } else {
      res.status(400).json({
        success: false,
        message: response.data.message
      });
    }
  } catch (error) {
    res.status(500).json({
      success: false,
      message: error.message
    });
  }
});

// Callback sau khi thanh toán
router.get('/momo/callback', (req, res) => {
  const isValid = momoPayment.verifySignature(req.query);
  
  if (isValid && req.query.resultCode === '0') {
    res.redirect(`/payment/success?orderId=${req.query.orderId}`);
  } else {
    res.redirect(`/payment/failed?orderId=${req.query.orderId}`);
  }
});

// IPN (Instant Payment Notification)
router.post('/momo/ipn', (req, res) => {
  const isValid = momoPayment.verifySignature(req.body);
  
  if (isValid && req.body.resultCode === 0) {
    // Xử lý cập nhật database
    console.log('Payment successful:', req.body.orderId);
  }
  
  res.status(204).end();
});

module.exports = router;
