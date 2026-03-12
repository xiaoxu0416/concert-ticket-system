<template>
  <div class="order-list-container">
    <div class="page-nav">
      <div class="page-nav-inner">
        <button class="back-link" @click="$router.push('/home')">
          <i class="el-icon-arrow-left"></i>
          <span>返回首页</span>
        </button>
        <span class="nav-divider">/</span>
        <span class="nav-current">我的订单</span>
      </div>
    </div>

    <div class="order-content">
      <div class="order-card">
        <div class="card-header">
          <div class="header-left">
            <h2 class="card-title">我的订单</h2>
            <span class="order-count" v-if="orderList.length > 0">共 {{ orderList.length }} 个订单</span>
          </div>
          <button class="refresh-btn" @click="refreshOrderList">
            <i class="el-icon-refresh"></i>
            <span>刷新</span>
          </button>
        </div>

        <el-table
          :data="orderList"
          style="width: 100%"
          v-loading="isLoading"
          class="order-table"
          :header-cell-style="{ background: '#F9FAFB', color: '#6B7280', fontWeight: '600', fontSize: '13px' }"
        >
          <el-table-column label="订单编号" min-width="160">
            <template slot-scope="scope">
              <span class="order-no">{{ scope.row.orderNo }}</span>
            </template>
          </el-table-column>
          <el-table-column label="演唱会名称" min-width="150">
            <template slot-scope="scope">
              <span class="concert-name-cell">{{ scope.row.concertName || '--' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="场次时间" min-width="170">
            <template slot-scope="scope">
              <span class="time-cell" v-if="scope.row.sessionTime"><i class="el-icon-time"></i> {{ formatTime(scope.row.sessionTime) }}</span>
              <span class="time-cell" v-else>--</span>
            </template>
          </el-table-column>
          <el-table-column prop="ticketNum" label="数量" width="80" align="center">
            <template slot-scope="scope">
              <span class="num-badge">{{ scope.row.ticketNum }}</span>
            </template>
          </el-table-column>
          <el-table-column label="金额" width="110">
            <template slot-scope="scope">
              <span class="amount-cell">¥{{ scope.row.totalAmount }}</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="120" align="center">
            <template slot-scope="scope">
              <span class="status-pill" :class="'status-' + scope.row.status">
                {{ {0:'待支付',1:'已支付',2:'已取消',3:'创建中',4:'已退款',5:'退款审核中',6:'已核销'}[scope.row.status] || '未知' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="操作" min-width="220" align="center">
            <template slot-scope="scope">
              <div class="action-buttons">
                <button class="action-btn action-pay" @click="goToPay(scope.row.orderNo)" v-if="scope.row.status === 0">去支付</button>
                <button class="action-btn action-cancel" @click="cancelOrder(scope.row.orderNo)" v-if="scope.row.status === 0">取消</button>
                <button class="action-btn action-ticket" @click="showTicket(scope.row)" v-if="scope.row.status === 1 || scope.row.status === 6">电子票</button>
                <button class="action-btn action-refund" @click="applyRefund(scope.row.orderNo)" v-if="scope.row.status === 1">申请退款</button>
                <button class="action-btn action-del" @click="deleteMyOrder(scope.row)" v-if="scope.row.status === 1 || scope.row.status === 2 || scope.row.status === 4 || scope.row.status === 6">删除</button>
                <span class="action-verified" v-if="scope.row.status === 6"><i class="el-icon-circle-check"></i> 已核销</span>
                <span class="action-pending-refund" v-if="scope.row.status === 5"><i class="el-icon-time"></i> 审核中</span>
                <span class="action-refunded" v-if="scope.row.status === 4"><i class="el-icon-warning-outline"></i> 已退款</span>
                <span class="action-creating" v-if="scope.row.status === 3"><i class="el-icon-loading"></i> 创建中</span>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <div v-if="orderList.length === 0 && !isLoading" class="empty-state">
          <i class="el-icon-document" style="font-size:48px;color:#D1D5DB;"></i>
          <p class="empty-text">暂无订单记录</p>
          <button class="empty-action" @click="$router.push('/home')">去逛逛</button>
        </div>
      </div>
    </div>

    <!-- 电子票弹窗 -->
    <el-dialog title="电子票" :visible.sync="ticketVisible" width="420px" :close-on-click-modal="true" top="8vh">
      <div v-if="ticketData" class="ticket-card">
        <div class="ticket-header">
          <div class="ticket-logo">
            <svg width="32" height="32" viewBox="0 0 48 48" fill="none"><rect width="48" height="48" rx="12" fill="#E85D3A"/><path d="M14 18h20v2H14zm0 5h20v2H14zm0 5h14v2H14z" fill="white" opacity="0.9"/><circle cx="36" cy="30" r="4" fill="white" opacity="0.9"/></svg>
          </div>
          <div>
            <div class="ticket-badge">入场凭证</div>
            <div class="ticket-order-no">{{ ticketData.orderNo }}</div>
          </div>
        </div>
        <div class="ticket-divider"><span class="ticket-cut ticket-cut-l"></span><span class="ticket-cut ticket-cut-r"></span></div>
        <div class="ticket-info">
          <div class="ticket-row"><span class="ticket-label">座位区域</span><span class="ticket-val">{{ ticketData.seatAreaName || '普通座' }}</span></div>
          <div class="ticket-row"><span class="ticket-label">票数</span><span class="ticket-val">{{ ticketData.ticketNum }} 张</span></div>
          <div class="ticket-row"><span class="ticket-label">金额</span><span class="ticket-val ticket-amount">¥{{ ticketData.totalAmount }}</span></div>
          <div class="ticket-row"><span class="ticket-label">支付时间</span><span class="ticket-val">{{ formatTime(ticketData.payTime) }}</span></div>
        </div>
        <div class="ticket-qr">
          <canvas ref="qrCanvas"></canvas>
          <p class="qr-tip">扫码验票入场</p>
        </div>
        <div class="ticket-verify-code" v-if="ticketData.verifyCode">
          <span class="verify-label">核销码</span>
          <span class="verify-code">{{ ticketData.verifyCode }}</span>
          <p class="verify-tip" v-if="ticketData.status === 6" style="color:#059669;font-weight:600;margin-top:8px;"><i class="el-icon-circle-check"></i> 已核销</p>
          <p class="verify-tip" v-else>入场时出示核销码给工作人员</p>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import QRCode from 'qrcode'
export default {
  name: 'OrderList',
  data() {
    return { orderList: [], isLoading: false, pollTimer: null, ticketVisible: false, ticketData: null }
  },
  async mounted() { await this.getOrderList(); this.startPolling(); },
  beforeDestroy() { this.stopPolling(); },
  methods: {
    formatTime(time) {
      if (!time) return '';
      const d = new Date(time);
      if (isNaN(d.getTime())) return String(time);
      return d.toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' });
    },
    refreshOrderList() {
      this.stopPolling();
      this.getOrderList().then(() => { this.startPolling(); this.$message.success('订单列表已刷新'); });
    },
    stopPolling() { if (this.pollTimer) { clearInterval(this.pollTimer); this.pollTimer = null; } },
    startPolling() {
      this.stopPolling();
      this.pollTimer = setInterval(() => { this.getOrderList(); }, 3000);
      setTimeout(() => { this.stopPolling(); }, 30000);
    },
    async getOrderList() {
      const userId = localStorage.getItem('userId');
      if (!userId) { this.$message.error('请先登录！'); this.$router.push('/auth'); return; }
      this.isLoading = true;
      try {
        const res = await this.$axios.get('/order/my', { params: { userId: Number(userId) }, withCredentials: true });
        if (res.data.code === 200) { this.orderList = res.data.data || []; }
      } catch (error) {
        if (error.response?.status === 401) {
          this.$message.error('登录已过期'); localStorage.clear(); this.$router.push('/auth');
        } else { this.$message.error('获取订单列表失败'); }
      } finally { this.isLoading = false; }
    },
    goToPay(orderNo) { this.$router.push(`/pay/${orderNo}`); },
    async cancelOrder(orderNo) {
      const userId = localStorage.getItem('userId');
      if (!userId) { this.$message.error('请先登录！'); this.$router.push('/auth'); return; }
      this.$confirm('确定取消该订单吗？', '提示', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }).then(async () => {
        try {
          const res = await this.$axios.post('/order/cancel', { orderNo: String(orderNo), userId: String(userId), reason: '用户主动取消' }, { withCredentials: true });
          if (res.data.code === 200) { this.$message.success('取消成功'); this.getOrderList(); }
        } catch (error) { this.$message.error('取消订单失败：' + (error.response?.data?.msg || error.message)); }
      }).catch(() => { this.$message.info('已取消操作'); });
    },
    async applyRefund(orderNo) {
      const userId = localStorage.getItem('userId');
      if (!userId) { this.$message.error('请先登录！'); this.$router.push('/auth'); return; }
      this.$confirm('确定申请退款吗？提交后需等待管理员审核。', '申请退款', { confirmButtonText: '确认退款', cancelButtonText: '取消', type: 'warning' }).then(async () => {
        try {
          const res = await this.$axios.post('/order/refund/apply', { orderNo, userId }, { withCredentials: true });
          if (res.data.code === 200) { this.$message.success('退款申请已提交，请等待管理员审核'); this.getOrderList(); }
          else { this.$message.error(res.data.msg || '退款失败'); }
        } catch (error) { this.$message.error('退款失败：' + (error.response?.data?.msg || error.message)); }
      }).catch(() => {});
    },
    async showTicket(row) {
      const userId = localStorage.getItem('userId')
      if (!userId) { this.$message.error('请先登录！'); return }
      try {
        const r = await this.$axios.get(`/order/ticket/${row.orderNo}`, { params: { userId }, withCredentials: true })
        if (r.data.code === 200) {
          this.ticketData = r.data.data
          this.ticketVisible = true
          this.$nextTick(() => {
            const canvas = this.$refs.qrCanvas
            if (canvas && this.ticketData.qrContent) {
              QRCode.toCanvas(canvas, this.ticketData.qrContent, { width: 180, margin: 2, color: { dark: '#1D1D1F', light: '#FFFFFF' } })
            }
          })
        } else { this.$message.error(r.data.msg || '获取电子票失败') }
      } catch (e) { this.$message.error('获取电子票失败') }
    },
    async deleteMyOrder(row) {
      const userId = localStorage.getItem('userId');
      if (!userId) { this.$message.error('请先登录！'); this.$router.push('/auth'); return; }
      this.$confirm('确定删除该订单记录吗？删除后将无法恢复。', '删除订单', { confirmButtonText: '确认删除', cancelButtonText: '取消', type: 'warning' }).then(async () => {
        try {
          const res = await this.$axios.delete(`/order/my/delete/${row.id}`, { params: { userId }, withCredentials: true });
          if (res.data.code === 200) { this.$message.success('删除成功'); this.getOrderList(); }
          else { this.$message.error(res.data.msg || '删除失败'); }
        } catch (error) { this.$message.error('删除失败：' + (error.response?.data?.msg || error.message)); }
      }).catch(() => {});
    }
  }
}
</script>

<style scoped>
.order-list-container { min-height: 100vh; background: #F7F6F3; }

.page-nav { background: #fff; border-bottom: 1px solid #F3F4F6; }
.page-nav-inner { max-width: 1400px; margin: 0 auto; padding: 16px 24px; display: flex; align-items: center; gap: 12px; }
.back-link { display: flex; align-items: center; gap: 6px; background: none; border: none; color: #E85D3A; font-size: 14px; cursor: pointer; font-family: inherit; }
.back-link:hover { opacity: 0.7; }
.nav-divider { color: #9CA3AF; }
.nav-current { color: #6B7280; font-size: 14px; }

.order-content { max-width: 1400px; margin: 0 auto; padding: 32px 24px; }

.order-card { background: #fff; border-radius: 24px; padding: 32px; border: 1px solid #F3F4F6; box-shadow: 0 1px 2px rgba(0,0,0,0.04); }
.card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
.header-left { display: flex; align-items: baseline; gap: 12px; }
.card-title { font-size: 22px; font-weight: 700; color: #1D1D1F; }
.order-count { font-size: 13px; color: #9CA3AF; background: #F3F4F6; padding: 3px 10px; border-radius: 9999px; }
.refresh-btn {
  display: flex; align-items: center; gap: 6px; padding: 8px 16px;
  border-radius: 8px; border: 1px solid #E5E7EB; background: transparent;
  color: #6B7280; font-size: 13px; cursor: pointer; font-family: inherit; transition: all 0.3s ease;
}
.refresh-btn:hover { color: #E85D3A; border-color: #E85D3A; background: #FFF4F0; }

.order-table { border-radius: 12px; overflow: hidden; border: 1px solid #F3F4F6; }
.order-table >>> td { padding: 16px 0 !important; border-bottom: 1px solid #F3F4F6 !important; }
.order-table >>> .el-table__row:hover > td { background: #FFFBF7 !important; }
.order-table >>> .el-table::before { display: none; }

.order-no { font-family: 'DM Sans', monospace; font-weight: 600; color: #1D1D1F; font-size: 13px; }
.concert-name-cell { font-weight: 500; color: #1D1D1F; }
.time-cell { font-size: 13px; color: #6B7280; }
.time-cell i { color: #9CA3AF; margin-right: 4px; }
.num-badge { display: inline-flex; align-items: center; justify-content: center; width: 28px; height: 28px; border-radius: 8px; background: #F3F4F6; font-weight: 600; font-size: 13px; color: #1D1D1F; }
.amount-cell { font-weight: 700; color: #E85D3A; font-size: 15px; }

.status-pill { display: inline-block; padding: 4px 12px; border-radius: 9999px; font-size: 12px; font-weight: 600; white-space: nowrap; }
.status-0 { background: #FFFBEB; color: #D97706; }
.status-1 { background: #ECFDF5; color: #059669; }
.status-2 { background: #F3F4F6; color: #9CA3AF; }
.status-3 { background: #EFF6FF; color: #2563EB; }
.status-4 { background: #FEF2F2; color: #EF4444; }
.status-5 { background: #FFF7ED; color: #EA580C; }

.action-buttons { display: flex; gap: 8px; justify-content: center; white-space: nowrap; flex-wrap: nowrap; }
.action-btn { padding: 6px 16px; border-radius: 8px; font-size: 13px; font-weight: 500; cursor: pointer; transition: all 0.3s ease; border: none; font-family: inherit; }
.action-pay { background: #E85D3A; color: #fff; }
.action-pay:hover { background: #FF7F5C; transform: translateY(-1px); box-shadow: 0 4px 12px rgba(232,93,58,0.3); }
.action-cancel { background: transparent; color: #9CA3AF; border: 1px solid #E5E7EB; }
.action-cancel:hover { color: #EF4444; border-color: #EF4444; background: #FEF2F2; }
.action-done { font-size: 13px; font-weight: 600; color: #059669; display: flex; align-items: center; gap: 4px; }
.action-cancelled { font-size: 13px; font-weight: 500; color: #9CA3AF; display: flex; align-items: center; gap: 4px; }
.action-creating { font-size: 13px; font-weight: 500; color: #2563EB; display: flex; align-items: center; gap: 4px; }
.action-ticket { background: #EFF6FF; color: #2563EB; border: 1px solid #BFDBFE; }
.action-ticket:hover { background: #DBEAFE; }
.action-refund { background: #FFFBEB; color: #D97706; border: 1px solid #FDE68A; }
.action-refund:hover { background: #FEF3C7; }
.action-del { background: transparent; color: #9CA3AF; border: 1px solid #E5E7EB; }
.action-del:hover { color: #EF4444; border-color: #EF4444; background: #FEF2F2; }
.action-refunded { font-size: 13px; font-weight: 500; color: #EF4444; display: flex; align-items: center; gap: 4px; }
.action-pending-refund { font-size: 13px; font-weight: 500; color: #EA580C; display: flex; align-items: center; gap: 4px; }

.empty-state { text-align: center; padding: 60px 20px; }
.empty-text { font-size: 15px; color: #6B7280; margin: 16px 0; }
.empty-action { padding: 8px 24px; border-radius: 8px; background: #E85D3A; color: #fff; border: none; font-size: 14px; font-weight: 500; cursor: pointer; font-family: inherit; }
.empty-action:hover { background: #FF7F5C; }

/* 电子票 */
.ticket-card { background: #fff; border-radius: 16px; border: 2px dashed #E5E7EB; padding: 24px; text-align: center; }
.ticket-header { display: flex; align-items: center; gap: 12px; justify-content: center; margin-bottom: 16px; }
.ticket-badge { font-size: 12px; font-weight: 700; color: #E85D3A; letter-spacing: 2px; text-transform: uppercase; }
.ticket-order-no { font-family: 'DM Sans', monospace; font-size: 13px; color: #6B7280; }
.ticket-divider { position: relative; border-top: 2px dashed #E5E7EB; margin: 16px -24px; }
.ticket-cut { position: absolute; top: -10px; width: 20px; height: 20px; border-radius: 50%; background: #F7F6F3; }
.ticket-cut-l { left: -10px; }
.ticket-cut-r { right: -10px; }
.ticket-info { padding: 16px 0; }
.ticket-row { display: flex; justify-content: space-between; padding: 6px 0; }
.ticket-label { font-size: 13px; color: #9CA3AF; }
.ticket-val { font-size: 13px; font-weight: 600; color: #1D1D1F; }
.ticket-amount { color: #E85D3A; font-size: 16px; }
.ticket-qr { padding-top: 16px; border-top: 1px solid #F3F4F6; }
.ticket-qr canvas { margin: 0 auto; display: block; border-radius: 8px; }
.qr-tip { font-size: 12px; color: #9CA3AF; margin-top: 8px; }
.ticket-verify-code { margin-top: 16px; padding-top: 16px; border-top: 1px solid #F3F4F6; text-align: center; }
.verify-label { display: block; font-size: 12px; color: #9CA3AF; margin-bottom: 8px; letter-spacing: 2px; }
.verify-code { display: inline-block; font-family: 'DM Sans', monospace; font-size: 28px; font-weight: 700; color: #E85D3A; letter-spacing: 6px; background: #FFF4F0; padding: 10px 24px; border-radius: 12px; border: 2px dashed #FDCFBF; }
.verify-tip { font-size: 12px; color: #9CA3AF; margin-top: 8px; }
.status-6 { background: #F0FDF4; color: #059669; }
.action-verified { font-size: 13px; font-weight: 600; color: #059669; display: flex; align-items: center; gap: 4px; }

@media (max-width: 768px) {
  .order-card { padding: 20px 16px; border-radius: 16px; }
  .card-header { flex-direction: column; align-items: flex-start; gap: 12px; }
}
</style>
