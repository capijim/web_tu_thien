const crypto = require('crypto');
const axios = require('axios');

class MoMoPayment {
  constructor() {
    this.partnerCode = process.env.MOMO_PARTNER_CODE;
    this.accessKey = process.env.MOMO_ACCESS_KEY;
    this.secretKey = process.env.MOMO_SECRET_KEY;
    this.endpoint = process.env.MOMO_ENDPOINT;
    this.redirectUrl = process.env.MOMO_REDIRECT_URL;
    this.ipnUrl = process.env.MOMO_IPN_URL;
  }

  createPayment(orderId, amount, orderInfo, extraData = '') {
    const requestId = orderId;
    const requestType = 'captureWallet';
    const rawSignature = `accessKey=${this.accessKey}&amount=${amount}&extraData=${extraData}&ipnUrl=${this.ipnUrl}&orderId=${orderId}&orderInfo=${orderInfo}&partnerCode=${this.partnerCode}&redirectUrl=${this.redirectUrl}&requestId=${requestId}&requestType=${requestType}`;
    
    const signature = crypto
      .createHmac('sha256', this.secretKey)
      .update(rawSignature)
      .digest('hex');

    const requestBody = {
      partnerCode: this.partnerCode,
      accessKey: this.accessKey,
      requestId,
      amount,
      orderId,
      orderInfo,
      redirectUrl: this.redirectUrl,
      ipnUrl: this.ipnUrl,
      extraData,
      requestType,
      signature,
      lang: 'vi'
    };

    return axios.post(this.endpoint, requestBody, {
      headers: { 'Content-Type': 'application/json' }
    });
  }

  verifySignature(data) {
    const {
      partnerCode,
      orderId,
      requestId,
      amount,
      orderInfo,
      orderType,
      transId,
      resultCode,
      message,
      payType,
      responseTime,
      extraData,
      signature
    } = data;

    const rawSignature = `accessKey=${this.accessKey}&amount=${amount}&extraData=${extraData}&message=${message}&orderId=${orderId}&orderInfo=${orderInfo}&orderType=${orderType}&partnerCode=${partnerCode}&payType=${payType}&requestId=${requestId}&responseTime=${responseTime}&resultCode=${resultCode}&transId=${transId}`;
    
    const calculatedSignature = crypto
      .createHmac('sha256', this.secretKey)
      .update(rawSignature)
      .digest('hex');

    return calculatedSignature === signature;
  }
}

module.exports = new MoMoPayment();
