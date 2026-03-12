<template>
  <div class="ticket-buy-container">
    <div class="page-nav">
      <div class="page-nav-inner">
        <button class="back-link" @click="goBack">
          <i class="el-icon-arrow-left"></i>
          <span>返回列表</span>
        </button>
        <span class="nav-divider">/</span>
        <span class="nav-current">{{ concertInfo.name || '抢票页面' }}</span>
      </div>
    </div>

    <div class="buy-content">
      <div class="buy-card">
        <div class="card-header">
          <div class="header-icon">
            <svg width="40" height="40" viewBox="0 0 40 40" fill="none">
              <rect width="40" height="40" rx="10" fill="#FFF4F0"/>
              <path d="M12 15h16v2H12zm0 4h16v2H12zm0 4h12v2H12z" fill="#E85D3A" opacity="0.8"/>
              <circle cx="30" cy="25" r="3" fill="#E85D3A" opacity="0.8"/>
            </svg>
          </div>
          <div>
            <h2 class="card-title">确认购票信息</h2>
            <p class="card-subtitle">请选择场次、座位区域并确认购票数量</p>
          </div>
        </div>

        <el-form :model="buyForm" :rules="buyRules" ref="buyForm" label-width="100px" class="buy-form">
          <!-- 演唱会信息 -->
          <div class="info-block" v-if="concertInfo.name">
            <div class="info-label">演唱会信息</div>
            <div class="concert-brief">
              <img :src="concertInfo.poster || getConcertCover(concertInfo.name)" class="concert-thumb">
              <div class="concert-brief-info">
                <div class="concert-brief-name">{{ concertInfo.name }}</div>
                <div class="concert-brief-meta"><i class="el-icon-user"></i> {{ concertInfo.singer }}</div>
                <div class="concert-brief-meta"><i class="el-icon-location-outline"></i> {{ concertInfo.city }} · {{ concertInfo.venue }}</div>
              </div>
            </div>
          </div>

          <!-- 场次选择 -->
          <div class="info-block">
            <div class="info-label">选择场次</div>
            <div v-if="sessionList.length === 0" class="empty-tip">该演唱会暂无可用场次</div>
            <div class="session-select-list" v-else>
              <div v-for="s in sessionList" :key="s.id" class="session-select-item" :class="{ 'session-active': selectedSessionId === s.id, 'session-sold': s.status === 2 || s.surplusStock <= 0 }" @click="selectSession(s)">
                <div class="session-date">{{ formatDate(s.showTime) }}</div>
                <div class="session-time">{{ formatTimeOnly(s.showTime) }}</div>
                <div class="session-price">¥{{ s.price }}</div>
                <div class="session-stock-label" v-if="s.status === 2 || s.surplusStock <= 0">已售罄</div>
                <div class="session-stock-label session-stock-ok" v-else>余{{ s.surplusStock }}张</div>
              </div>
            </div>
          </div>

          <!-- 座位区域选择 -->
          <div class="info-block" v-if="seatAreas.length > 0">
            <div class="info-label">选择座位区域</div>
            <div class="seat-area-list">
              <div v-for="area in seatAreas" :key="area.id" class="seat-area-item" :class="{ 'seat-area-active': selectedAreaId === area.id, 'seat-area-sold': area.surplusStock <= 0 }" @click="selectArea(area)">
                <div class="area-name">{{ area.areaName }}</div>
                <div class="area-price">¥{{ area.price }}</div>
                <div class="area-stock">余{{ area.surplusStock }}张</div>
              </div>
            </div>
          </div>

          <!-- 购票数量 -->
          <div class="info-block" v-if="selectedSessionId">
            <div class="info-label">购票数量</div>
            <div class="quantity-row">
              <el-input-number v-model="buyForm.ticketNum" :min="1" :max="10" class="quantity-input"></el-input-number>
              <div class="stock-tag" :class="currentStock > 0 ? 'stock-ok' : 'stock-empty'">
                <i :class="currentStock > 0 ? 'el-icon-check' : 'el-icon-close'"></i>
                剩余库存：{{ currentStock }}
              </div>
            </div>
            <div class="total-price" v-if="currentPrice > 0">
              合计：<span class="total-amount">¥{{ (currentPrice * buyForm.ticketNum).toFixed(2) }}</span>
            </div>
          </div>

          <div class="action-row">
            <el-button type="primary" @click="submitBuy" :loading="isLoading" :disabled="!selectedSessionId" class="submit-btn">
              <i class="el-icon-shopping-cart-2" v-if="!isLoading"></i> 立即抢票
            </el-button>
            <el-button @click="goBack" class="cancel-btn">返回</el-button>
          </div>
        </el-form>
      </div>

      <div class="tips-card">
        <h4 class="tips-title">购票须知</h4>
        <ul class="tips-list">
          <li>每人每场最多购买 10 张</li>
          <li>下单后请在 30 分钟内完成支付</li>
          <li>超时未支付订单将自动取消</li>
          <li>购票成功后不支持退换</li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'TicketBuy',
  data() {
    return {
      concertId: this.$route.params.id,
      concertInfo: {},
      sessionList: [],
      selectedSessionId: null,
      seatAreas: [],
      selectedAreaId: null,
      currentStock: 0,
      currentPrice: 0,
      buyForm: { ticketNum: 1 },
      buyRules: { ticketNum: [{ required: true, message: '请选择购票数量', trigger: 'change' }] },
      isLoading: false
    }
  },
  async mounted() {
    await this.loadConcertInfo()
    await this.loadSessions()
  },
  methods: {
    formatTime(time) {
      if (!time) return '';
      return new Date(time).toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' });
    },
    formatDate(time) {
      if (!time) return '';
      const d = new Date(time);
      return d.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit', weekday: 'short' });
    },
    formatTimeOnly(time) {
      if (!time) return '';
      const d = new Date(time);
      return d.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' });
    },
    getConcertCover(name) { return `https://picsum.photos/seed/${name}/400/220.jpg` },
    goBack() { window.history.length > 1 ? this.$router.go(-1) : this.$router.push('/home'); },

    async loadConcertInfo() {
      try {
        const res = await this.$axios.get('/concert/all', { withCredentials: true });
        if (res.data.code === 200) {
          const list = res.data.data || [];
          this.concertInfo = list.find(c => c.id === Number(this.concertId)) || {};
        }
      } catch (e) { /* ignore */ }
    },
    async loadSessions() {
      try {
        const res = await this.$axios.get(`/session/list/${this.concertId}`);
        if (res.data.code === 200) { this.sessionList = res.data.data || []; }
      } catch (e) { this.$message.error('获取场次列表失败'); }
    },
    async selectSession(session) {
      if (session.status === 2 || session.surplusStock <= 0) { this.$message.warning('该场次已售罄'); return; }
      this.selectedSessionId = session.id;
      this.currentPrice = session.price;
      this.currentStock = session.surplusStock;
      this.selectedAreaId = null;
      this.buyForm.ticketNum = 1;
      // 加载该场次的座位区域
      try {
        const r = await this.$axios.get(`/seatArea/list/${session.id}`);
        if (r.data.code === 200) this.seatAreas = r.data.data || [];
        else this.seatAreas = [];
      } catch (e) { this.seatAreas = []; }
    },
    selectArea(area) {
      if (area.surplusStock <= 0) { this.$message.warning('该区域已售罄'); return; }
      this.selectedAreaId = area.id;
      this.currentPrice = area.price;
      this.currentStock = area.surplusStock;
      if (this.buyForm.ticketNum > area.surplusStock) this.buyForm.ticketNum = area.surplusStock;
    },
    async submitBuy() {
      if (this.isLoading) return;
      if (!this.selectedSessionId) { this.$message.warning('请先选择场次'); return; }
      this.$refs.buyForm.validate(async (valid) => {
        if (valid) {
          const userId = localStorage.getItem('userId');
          if (!userId) { this.$message.error('未检测到登录状态，请重新登录！'); localStorage.clear(); this.$router.push('/auth'); return; }
          if (this.currentStock <= 0) { this.$message.error('该场次票已售罄！'); return; }
          if (this.buyForm.ticketNum > this.currentStock) { this.$message.warning('购票数量不能超过剩余库存'); return; }
          this.isLoading = true;
          try {
            const buyData = { sessionId: this.selectedSessionId, ticketNum: this.buyForm.ticketNum, userId: Number(userId) };
            if (this.selectedAreaId) { buyData.seatAreaId = this.selectedAreaId; }
            const res = await this.$axios.post('/order/buy', buyData, { withCredentials: true, timeout: 10000 });
            if (res.data && res.data.code === 200) {
              this.$message.success('抢票请求已受理！请前往订单列表查看状态');
              this.$router.push('/order/list');
            } else { this.$message.error(`抢票失败：${res.data?.msg || '业务处理异常'}`); }
          } catch (error) {
            const status = error.response?.status;
            if (status === 401) { this.$message.error('登录已过期'); localStorage.clear(); this.$router.push('/auth'); }
            else { this.$message.error(`抢票失败：${error.response?.data?.msg || error.message}`); }
          } finally { this.isLoading = false; }
        }
      });
    }
  }
}
</script>

<style scoped>
.ticket-buy-container { min-height: 100vh; background: #F7F6F3; }

.page-nav { background: #fff; border-bottom: 1px solid #F3F4F6; }
.page-nav-inner { max-width: 1000px; margin: 0 auto; padding: 16px 24px; display: flex; align-items: center; gap: 12px; }
.back-link { display: flex; align-items: center; gap: 6px; background: none; border: none; color: #E85D3A; font-size: 14px; cursor: pointer; font-family: inherit; }
.back-link:hover { opacity: 0.7; }
.nav-divider { color: #9CA3AF; }
.nav-current { color: #6B7280; font-size: 14px; }

.buy-content { max-width: 1000px; margin: 0 auto; padding: 32px 24px; display: grid; grid-template-columns: 1fr 280px; gap: 24px; align-items: start; }

.buy-card { background: #fff; border-radius: 24px; padding: 36px; border: 1px solid #F3F4F6; box-shadow: 0 1px 2px rgba(0,0,0,0.04); }
.card-header { display: flex; align-items: center; gap: 16px; margin-bottom: 36px; padding-bottom: 24px; border-bottom: 1px solid #F3F4F6; }
.card-title { font-size: 22px; font-weight: 700; color: #1D1D1F; margin-bottom: 4px; }
.card-subtitle { font-size: 14px; color: #9CA3AF; }

.info-block { margin-bottom: 28px; }
.info-label { font-size: 13px; font-weight: 600; color: #6B7280; text-transform: uppercase; letter-spacing: 1px; margin-bottom: 12px; }
.empty-tip { color: #9CA3AF; font-size: 14px; padding: 20px; text-align: center; background: #F9FAFB; border-radius: 10px; }

/* 演唱会信息卡片 */
.concert-brief { display: flex; gap: 16px; background: #F7F6F3; padding: 16px; border-radius: 12px; border: 1px solid #F3F4F6; }
.concert-thumb { width: 80px; height: 100px; object-fit: cover; border-radius: 8px; flex-shrink: 0; }
.concert-brief-info { display: flex; flex-direction: column; justify-content: center; gap: 6px; }
.concert-brief-name { font-size: 16px; font-weight: 700; color: #1D1D1F; }
.concert-brief-meta { font-size: 13px; color: #6B7280; display: flex; align-items: center; gap: 6px; }
.concert-brief-meta i { color: #E85D3A; font-size: 14px; }

/* 场次选择 */
.session-select-list { display: grid; grid-template-columns: repeat(auto-fill, minmax(150px, 1fr)); gap: 10px; }
.session-select-item { padding: 14px; border-radius: 12px; border: 2px solid #F3F4F6; cursor: pointer; text-align: center; transition: all 0.3s; background: #fff; }
.session-select-item:hover { border-color: #E85D3A; box-shadow: 0 2px 8px rgba(232,93,58,0.1); }
.session-active { border-color: #E85D3A !important; background: #FFF4F0 !important; }
.session-sold { opacity: 0.5; cursor: not-allowed; }
.session-sold:hover { border-color: #F3F4F6; box-shadow: none; }
.session-date { font-size: 14px; font-weight: 600; color: #1D1D1F; margin-bottom: 2px; }
.session-time { font-size: 13px; color: #6B7280; margin-bottom: 6px; }
.session-price { font-size: 18px; font-weight: 700; color: #E85D3A; margin-bottom: 4px; }
.session-stock-label { font-size: 11px; color: #DC2626; font-weight: 500; }
.session-stock-ok { color: #059669; }

/* 座位区域 */
.seat-area-list { display: grid; grid-template-columns: repeat(auto-fill, minmax(140px, 1fr)); gap: 10px; }
.seat-area-item { padding: 14px; border-radius: 10px; border: 2px solid #F3F4F6; cursor: pointer; text-align: center; transition: all 0.3s; background: #fff; }
.seat-area-item:hover { border-color: #E85D3A; }
.seat-area-active { border-color: #E85D3A !important; background: #FFF4F0 !important; }
.seat-area-sold { opacity: 0.5; cursor: not-allowed; }
.area-name { font-size: 14px; font-weight: 600; color: #1D1D1F; margin-bottom: 4px; }
.area-price { font-size: 18px; font-weight: 700; color: #E85D3A; margin-bottom: 2px; }
.area-stock { font-size: 12px; color: #9CA3AF; }

/* 购票数量 */
.quantity-row { display: flex; align-items: center; gap: 16px; }
.quantity-input { width: 180px; }
.stock-tag { display: inline-flex; align-items: center; gap: 6px; padding: 6px 14px; border-radius: 9999px; font-size: 13px; font-weight: 500; }
.stock-ok { background: #ECFDF5; color: #059669; }
.stock-empty { background: #FEF2F2; color: #DC2626; }
.total-price { margin-top: 16px; font-size: 15px; color: #6B7280; }
.total-amount { font-size: 24px; font-weight: 700; color: #E85D3A; }

.action-row { display: flex; gap: 12px; margin-top: 36px; padding-top: 24px; border-top: 1px solid #F3F4F6; }
.submit-btn { height: 48px !important; padding: 0 36px !important; border-radius: 12px !important; font-size: 16px !important; font-weight: 600 !important; letter-spacing: 2px; }
.cancel-btn { height: 48px !important; padding: 0 24px !important; border-radius: 12px !important; border: 1px solid #E5E7EB !important; background: transparent !important; color: #6B7280 !important; }
.cancel-btn:hover { border-color: #9CA3AF !important; color: #1D1D1F !important; }

.tips-card { background: #FFFBF7; border-radius: 16px; padding: 24px; border: 1px solid #F3F4F6; }
.tips-title { font-size: 15px; font-weight: 600; color: #1D1D1F; margin-bottom: 16px; display: flex; align-items: center; gap: 8px; }
.tips-title::before { content: ''; width: 4px; height: 16px; border-radius: 2px; background: #E85D3A; }
.tips-list { list-style: none; padding: 0; margin: 0; display: flex; flex-direction: column; gap: 12px; }
.tips-list li { font-size: 13px; color: #6B7280; padding-left: 18px; position: relative; line-height: 1.6; }
.tips-list li::before { content: ''; position: absolute; left: 0; top: 7px; width: 6px; height: 6px; border-radius: 50%; background: #E5E7EB; }

@media (max-width: 768px) {
  .buy-content { grid-template-columns: 1fr; }
  .buy-card { padding: 24px; }
  .session-select-list { grid-template-columns: repeat(2, 1fr); }
  .quantity-row { flex-direction: column; align-items: flex-start; }
  .action-row { flex-direction: column; }
  .submit-btn, .cancel-btn { width: 100% !important; }
  .concert-brief { flex-direction: column; align-items: center; text-align: center; }
}
</style>
