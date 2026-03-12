<template>
  <div class="pay-container">
    <div class="page-nav">
      <div class="page-nav-inner">
        <button class="back-link" @click="$router.push('/order/list')">
          <i class="el-icon-arrow-left"></i>
          <span>返回订单</span>
        </button>
        <span class="nav-divider">/</span>
        <span class="nav-current">订单支付</span>
      </div>
    </div>

    <div class="pay-content">
      <div class="pay-card">
        <div class="card-header">
          <div class="header-icon">
            <svg width="40" height="40" viewBox="0 0 40 40" fill="none">
              <rect width="40" height="40" rx="10" fill="#FFF4F0"/>
              <path d="M13 16h14a2 2 0 012 2v8a2 2 0 01-2 2H13a2 2 0 01-2-2v-8a2 2 0 012-2z" stroke="#E85D3A" stroke-width="1.5" fill="none"/>
              <path d="M11 20h18" stroke="#E85D3A" stroke-width="1.5"/>
              <rect x="14" y="23" width="6" height="2" rx="1" fill="#E85D3A" opacity="0.5"/>
            </svg>
          </div>
          <div>
            <h2 class="card-title">订单支付</h2>
            <p class="card-subtitle">请确认订单信息并选择支付方式</p>
          </div>
        </div>

        <div class="order-info-section">
          <div class="info-grid">
            <div class="info-item">
              <span class="info-label">订单编号</span>
              <span class="info-value mono">{{ formatOrderNo(payForm.orderNo) }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">订单金额</span>
              <span class="info-value amount">¥{{ payForm.totalAmount }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">创建时间</span>
              <span class="info-value">{{ formatTime(orderInfo.createTime) }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">订单状态</span>
              <span class="status-pill" :class="'status-' + orderInfo.status">
                {{ orderInfo.status === 0 ? '待支付' : (orderInfo.status === 1 ? '已支付' : '已取消') }}
              </span>
            </div>
          </div>
        </div>

        <el-form :model="payForm" label-width="0" class="pay-form" ref="payForm" :rules="payRules">
          <div class="section-title">选择支付方式</div>
          <el-form-item prop="payType">
            <div class="pay-method-group">
              <label class="pay-method-card" :class="{ active: payForm.payType === 'WECHAT' }" @click="payForm.payType = 'WECHAT'">
                <div class="method-radio"><div class="radio-dot" v-if="payForm.payType === 'WECHAT'"></div></div>
                <div class="method-icon wechat-icon"><i class="el-icon-chat-dot-round"></i></div>
                <div class="method-info"><span class="method-name">微信支付</span><span class="method-desc">WeChat Pay</span></div>
              </label>
              <label class="pay-method-card" :class="{ active: payForm.payType === 'ALIPAY' }" @click="payForm.payType = 'ALIPAY'">
                <div class="method-radio"><div class="radio-dot" v-if="payForm.payType === 'ALIPAY'"></div></div>
                <div class="method-icon alipay-icon"><i class="el-icon-money"></i></div>
                <div class="method-info"><span class="method-name">支付宝支付</span><span class="method-desc">Alipay</span></div>
              </label>
            </div>
          </el-form-item>

          <div class="action-row">
            <el-button type="primary" @click="submitPay" :loading="isLoading" class="pay-btn" :disabled="isPayDisabled">
              <i class="el-icon-money" v-if="!isLoading"></i> 立即支付 ¥{{ payForm.totalAmount }}
            </el-button>
            <el-button @click="$router.push('/order/list')" class="back-btn-secondary">返回订单列表</el-button>
          </div>
        </el-form>
      </div>

      <div class="side-info">
        <div class="side-card">
          <h4 class="side-title">支付须知</h4>
          <ul class="side-list">
            <li>请在 30 分钟内完成支付</li>
            <li>超时未支付订单将自动取消</li>
            <li>支付完成后请勿重复支付</li>
          </ul>
        </div>
        <div class="side-card side-card-warm">
          <h4 class="side-title">遇到问题？</h4>
          <p class="side-text">如支付遇到问题，请刷新页面后重试，或联系客服获取帮助。</p>
        </div>
      </div>
    </div>

    <!-- 支付二维码弹窗 -->
    <el-dialog title="扫码支付" :visible.sync="showQrCode" width="420px" center :close-on-click-modal="false" :before-close="handleQrClose" custom-class="qr-dialog">
      <div class="qr-container">
        <img :src="qrCodeUrl" alt="支付二维码" class="qr-img" v-if="qrCodeUrl">
        <div v-else class="qr-loading"><i class="el-icon-loading"></i><p>生成支付二维码中...</p></div>
        <div class="qr-info">
          <p class="qr-method">{{ payForm.payType === 'WECHAT' ? '微信支付' : '支付宝支付' }}</p>
          <p class="qr-amount">¥{{ payForm.totalAmount }}</p>
          <p class="qr-tip">请使用{{ payForm.payType === 'WECHAT' ? '微信' : '支付宝' }}扫码完成支付</p>
          <div class="qr-countdown"><i class="el-icon-time"></i> 剩余时间：{{ countdown }}秒</div>
        </div>
      </div>
      <span slot="footer" class="qr-footer">
        <el-button type="primary" @click="mockPaySuccess" :loading="isMockPaying" class="mock-pay-btn">
          <i class="el-icon-circle-check" v-if="!isMockPaying"></i> 已支付
        </el-button>
        <el-button @click="cancelPay" class="qr-cancel-btn">取消支付</el-button>
      </span>
    </el-dialog>

    <!-- 支付成功弹窗 -->
    <el-dialog title="支付成功" :visible.sync="paySuccess" width="400px" center :close-on-click-modal="false" custom-class="success-dialog">
      <div class="success-content">
        <div class="success-icon-wrap"><i class="el-icon-circle-check"></i></div>
        <p class="success-title">支付成功</p>
        <p class="success-order">订单编号：{{ formatOrderNo(payForm.orderNo) }}</p>
      </div>
      <span slot="footer">
        <el-button type="primary" @click="paySuccess = false; $router.push('/order/list')" class="success-btn">查看订单</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import QRCode from 'qrcode'

export default {
  name: 'OrderPay',
  data() {
    return {
      payForm: { orderNo: this.$route.params.orderNo, totalAmount: 0, payType: '' },
      payRules: { payType: [{ required: true, message: '请选择支付方式', trigger: 'change' }] },
      orderInfo: { status: 0 },
      isLoading: false,
      paySuccess: false,
      showQrCode: false,
      qrCodeUrl: '',
      payPollTimer: null,
      countdown: 180,
      countdownTimer: null,
      isPaying: false,
      isMockPaying: false
    }
  },
  computed: {
    isPayDisabled() {
      return this.payForm.totalAmount <= 0 || this.orderInfo.status !== 0 || this.isPaying;
    }
  },
  async mounted() { await this.getOrderInfo(); },
  beforeDestroy() { this.stopPayPoll(); this.stopCountdown(); },
  methods: {
    formatOrderNo(orderNo) {
      if (!orderNo) return '';
      return 'ORDER_' + String(orderNo).slice(-6);
    },
    formatTime(time) {
      if (!time) return '';
      return new Date(time).toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit', second: '2-digit' });
    },
    async getOrderInfo() {
      let retryCount = 0;
      while (retryCount < 3) {
        try {
          const userId = localStorage.getItem('userId');
          if (!userId) { this.$message.error('未检测到登录状态'); localStorage.clear(); this.$router.push('/auth'); return; }
          const res = await this.$axios.get(`/order/info/${this.payForm.orderNo}`, { params: { userId }, timeout: 5000, withCredentials: true });
          if (res.data && res.data.code === 200) {
            this.orderInfo = res.data.data;
            this.payForm.totalAmount = res.data.data.totalAmount || 0;
            if (this.orderInfo.status === 2) { this.$message.warning('该订单已取消'); this.$router.push('/order/list'); }
            break;
          } else { this.$message.error('获取订单信息失败'); this.$router.push('/order/list'); break; }
        } catch (error) {
          retryCount++;
          if (error.response?.status === 401) { this.$message.error('登录已过期'); localStorage.clear(); this.$router.push('/auth'); break; }
          if (retryCount >= 3) { this.$message.error('网络繁忙，请稍后重试'); this.$router.push('/order/list'); }
        }
      }
    },
    async submitPay() {
      if (this.isPaying || this.orderInfo.status !== 0) return;
      this.$refs.payForm.validate(async (valid) => {
        if (!valid) return;
        const userId = localStorage.getItem('userId');
        if (!userId) { this.$message.error('未检测到登录状态'); localStorage.clear(); this.$router.push('/auth'); return; }
        this.isLoading = true; this.isPaying = true;
        try {
          const prePayRes = await this.$axios.post('/order/pay/pre', { orderNo: this.payForm.orderNo, payType: this.payForm.payType, userId: Number(userId), timestamp: Date.now() }, { timeout: 10000, withCredentials: true });
          if (prePayRes.data && prePayRes.data.code === 200) {
            const qrData = `orderNo=${prePayRes.data.data.orderNo}&amount=${prePayRes.data.data.totalAmount}&payType=${prePayRes.data.data.payType}`;
            this.qrCodeUrl = await QRCode.toDataURL(qrData, { width: 200, margin: 2 });
            this.showQrCode = true;
            this.startPayPoll();
            this.startCountdown();
          } else { this.$message.error(prePayRes.data?.msg || '生成支付二维码失败'); }
        } catch (error) {
          const status = error.response?.status;
          if (status === 401) { this.$message.error('登录已过期'); localStorage.clear(); this.$router.push('/auth'); }
          else if (status === 404) { this.$message.error('预支付接口不存在'); }
          else { this.$message.error(error.response?.data?.msg || '生成支付二维码失败'); }
        } finally { this.isLoading = false; this.isPaying = false; }
      });
    },
    async mockPaySuccess() {
      if (this.isMockPaying) return;
      const userId = localStorage.getItem('userId');
      if (!userId) { this.$message.error('未检测到登录状态'); localStorage.clear(); this.$router.push('/auth'); return; }
      this.isMockPaying = true;
      try {
        const res = await this.$axios.post('/order/pay', {
          orderNo: this.payForm.orderNo,
          payType: this.payForm.payType,
          userId: Number(userId)
        }, { timeout: 10000, withCredentials: true });
        if (res.data && res.data.code === 200) {
          this.stopPayPoll();
          this.stopCountdown();
          this.showQrCode = false;
          this.paySuccess = true;
          this.$message.success('支付成功！');
        } else {
          this.$message.error(res.data?.msg || '模拟支付失败');
        }
      } catch (error) {
        const status = error.response?.status;
        if (status === 401) { this.$message.error('登录已过期'); localStorage.clear(); this.$router.push('/auth'); }
        else { this.$message.error(error.response?.data?.msg || '模拟支付失败，请重试'); }
      } finally { this.isMockPaying = false; }
    },
    startPayPoll() {
      this.stopPayPoll();
      this.payPollTimer = setInterval(async () => {
        try {
          const userId = localStorage.getItem('userId');
          if (!userId) { this.stopPayPoll(); return; }
          const res = await this.$axios.get(`/order/info/${this.payForm.orderNo}`, { params: { userId }, timeout: 3000, withCredentials: true });
          if (res.data && res.data.code === 200) {
            this.orderInfo = res.data.data;
            if (this.orderInfo.status === 1) { this.stopPayPoll(); this.stopCountdown(); this.showQrCode = false; this.paySuccess = true; this.$message.success('支付成功！'); }
            else if (this.orderInfo.status === 2) { this.stopPayPoll(); this.stopCountdown(); this.showQrCode = false; this.$message.error('该订单已取消'); }
          }
        } catch (error) { console.error('查询支付状态失败：', error); }
      }, 2000);
    },
    stopPayPoll() { if (this.payPollTimer) { clearInterval(this.payPollTimer); this.payPollTimer = null; } },
    startCountdown() {
      this.stopCountdown(); this.countdown = 180;
      this.countdownTimer = setInterval(async () => {
        this.countdown--;
        if (this.countdown <= 0) {
          this.stopCountdown(); this.stopPayPoll(); this.showQrCode = false;
          try {
            const userId = localStorage.getItem('userId'); if (!userId) return;
            await this.$axios.post('/order/cancel', { orderNo: this.payForm.orderNo, reason: '支付超时', userId: Number(userId) }, { timeout: 5000, withCredentials: true });
            this.$message.warning('支付超时，订单已取消'); this.$router.push('/order/list');
          } catch (error) { this.$message.warning('支付超时，请手动取消订单'); }
        }
      }, 1000);
    },
    stopCountdown() { if (this.countdownTimer) { clearInterval(this.countdownTimer); this.countdownTimer = null; } },
    handleQrClose(done) { this.stopPayPoll(); this.stopCountdown(); done(); },
    async cancelPay() {
      try {
        const userId = localStorage.getItem('userId');
        if (!userId) { this.$message.error('未检测到登录状态'); localStorage.clear(); this.$router.push('/auth'); return; }
        await this.$axios.post('/order/cancel', { orderNo: this.payForm.orderNo, reason: '用户主动取消', userId: Number(userId) }, { timeout: 5000, withCredentials: true });
        this.stopPayPoll(); this.stopCountdown(); this.showQrCode = false;
        this.$message.info('已取消支付'); this.$router.push('/order/list');
      } catch (error) { this.$message.error(error.response?.data?.msg || '取消支付失败'); }
    }
  }
}
</script>

<style scoped>
.pay-container { min-height: 100vh; background: #F7F6F3; }

.page-nav { background: #fff; border-bottom: 1px solid #F3F4F6; }
.page-nav-inner { max-width: 1000px; margin: 0 auto; padding: 16px 24px; display: flex; align-items: center; gap: 12px; }
.back-link { display: flex; align-items: center; gap: 6px; background: none; border: none; color: #E85D3A; font-size: 14px; cursor: pointer; font-family: inherit; }
.back-link:hover { opacity: 0.7; }
.nav-divider { color: #9CA3AF; }
.nav-current { color: #6B7280; font-size: 14px; }

.pay-content { max-width: 1000px; margin: 0 auto; padding: 32px 24px; display: grid; grid-template-columns: 1fr 280px; gap: 24px; align-items: start; }

.pay-card { background: #fff; border-radius: 24px; padding: 36px; border: 1px solid #F3F4F6; box-shadow: 0 1px 2px rgba(0,0,0,0.04); }
.card-header { display: flex; align-items: center; gap: 16px; margin-bottom: 32px; padding-bottom: 24px; border-bottom: 1px solid #F3F4F6; }
.card-title { font-size: 22px; font-weight: 700; color: #1D1D1F; margin-bottom: 4px; }
.card-subtitle { font-size: 14px; color: #9CA3AF; }

.order-info-section { background: #F7F6F3; border-radius: 12px; padding: 20px 24px; margin-bottom: 28px; border: 1px solid #F3F4F6; }
.info-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.info-item { display: flex; flex-direction: column; gap: 4px; }
.info-label { font-size: 12px; font-weight: 600; color: #9CA3AF; text-transform: uppercase; letter-spacing: 0.5px; }
.info-value { font-size: 15px; color: #1D1D1F; font-weight: 500; }
.info-value.mono { font-family: 'DM Sans', monospace; letter-spacing: 0.5px; }
.info-value.amount { font-size: 22px; font-weight: 700; color: #E85D3A; }
.status-pill { display: inline-block; width: fit-content; padding: 3px 12px; border-radius: 9999px; font-size: 12px; font-weight: 600; }
.status-0 { background: #FFFBEB; color: #D97706; }
.status-1 { background: #ECFDF5; color: #059669; }
.status-2 { background: #F3F4F6; color: #9CA3AF; }

.section-title { font-size: 14px; font-weight: 600; color: #1D1D1F; margin-bottom: 16px; }
.pay-method-group { display: flex; gap: 12px; }
.pay-method-card { flex: 1; display: flex; align-items: center; gap: 14px; padding: 18px 20px; border-radius: 12px; border: 2px solid #E5E7EB; cursor: pointer; transition: all 0.3s ease; background: #fff; }
.pay-method-card:hover { border-color: #9CA3AF; }
.pay-method-card.active { border-color: #E85D3A; background: #FFF4F0; }
.method-radio { width: 20px; height: 20px; border-radius: 50%; border: 2px solid #E5E7EB; display: flex; align-items: center; justify-content: center; flex-shrink: 0; transition: all 0.3s ease; }
.pay-method-card.active .method-radio { border-color: #E85D3A; }
.radio-dot { width: 10px; height: 10px; border-radius: 50%; background: #E85D3A; }
.method-icon { width: 36px; height: 36px; border-radius: 8px; display: flex; align-items: center; justify-content: center; font-size: 18px; flex-shrink: 0; }
.wechat-icon { background: #ECFDF5; color: #059669; }
.alipay-icon { background: #EFF6FF; color: #2563EB; }
.method-info { display: flex; flex-direction: column; }
.method-name { font-size: 14px; font-weight: 600; color: #1D1D1F; }
.method-desc { font-size: 12px; color: #9CA3AF; }

.action-row { display: flex; gap: 12px; margin-top: 32px; padding-top: 24px; border-top: 1px solid #F3F4F6; }
.pay-btn { height: 50px !important; padding: 0 32px !important; border-radius: 12px !important; font-size: 16px !important; font-weight: 600 !important; letter-spacing: 1px; }
.back-btn-secondary { height: 50px !important; padding: 0 24px !important; border-radius: 12px !important; border: 1px solid #E5E7EB !important; background: transparent !important; color: #6B7280 !important; }
.back-btn-secondary:hover { border-color: #9CA3AF !important; color: #1D1D1F !important; }

.side-info { display: flex; flex-direction: column; gap: 16px; }
.side-card { background: #fff; border-radius: 16px; padding: 24px; border: 1px solid #F3F4F6; }
.side-card-warm { background: #FFFBF7; }
.side-title { font-size: 15px; font-weight: 600; color: #1D1D1F; margin-bottom: 14px; display: flex; align-items: center; gap: 8px; }
.side-title::before { content: ''; width: 4px; height: 16px; border-radius: 2px; background: #E85D3A; }
.side-list { list-style: none; padding: 0; margin: 0; display: flex; flex-direction: column; gap: 10px; }
.side-list li { font-size: 13px; color: #6B7280; padding-left: 16px; position: relative; line-height: 1.6; }
.side-list li::before { content: ''; position: absolute; left: 0; top: 7px; width: 6px; height: 6px; border-radius: 50%; background: #E5E7EB; }
.side-text { font-size: 13px; color: #6B7280; line-height: 1.7; }

/* 二维码弹窗 */
.qr-container { text-align: center; padding: 10px 0; }
.qr-img { width: 200px; height: 200px; margin: 0 auto 20px; display: block; border: 1px solid #E5E7EB; border-radius: 12px; padding: 8px; }
.qr-loading { padding: 40px 0; color: #9CA3AF; }
.qr-loading i { font-size: 32px; color: #E85D3A; display: block; margin-bottom: 12px; }
.qr-method { font-size: 16px; font-weight: 600; color: #1D1D1F; margin-bottom: 4px; }
.qr-amount { font-size: 28px; font-weight: 700; color: #E85D3A; margin-bottom: 8px; }
.qr-tip { font-size: 13px; color: #9CA3AF; margin-bottom: 12px; }
.qr-countdown { display: inline-flex; align-items: center; gap: 6px; font-size: 14px; font-weight: 600; color: #EF4444; background: #FEF2F2; padding: 6px 16px; border-radius: 9999px; }
.qr-footer { display: flex; flex-direction: row; gap: 12px; width: 100%; }
.mock-pay-btn { flex: 1; height: 44px !important; border-radius: 8px !important; font-weight: 600 !important; font-size: 15px !important; letter-spacing: 1px; }
.qr-cancel-btn { flex: 1; height: 44px !important; border-radius: 8px !important; border: 1px solid #E5E7EB !important; background: transparent !important; color: #6B7280 !important; }
.qr-cancel-btn:hover { color: #EF4444 !important; border-color: #EF4444 !important; }

/* 支付成功弹窗 */
.success-content { text-align: center; padding: 20px 0; }
.success-icon-wrap i { font-size: 64px; color: #10B981; }
.success-title { font-size: 22px; font-weight: 700; color: #1D1D1F; margin: 16px 0 8px; }
.success-order { font-size: 14px; color: #9CA3AF; }
.success-btn { width: 100%; height: 44px !important; border-radius: 8px !important; font-weight: 600 !important; }

@media (max-width: 768px) {
  .pay-content { grid-template-columns: 1fr; }
  .pay-card { padding: 24px; }
  .info-grid { grid-template-columns: 1fr; }
  .pay-method-group { flex-direction: column; }
  .action-row { flex-direction: column; }
  .pay-btn, .back-btn-secondary { width: 100% !important; }
}
</style>
