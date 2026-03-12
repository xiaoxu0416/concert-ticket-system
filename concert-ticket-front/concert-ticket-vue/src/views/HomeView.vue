<template>
  <div class="home-container">
    <header class="top-nav">
      <div class="nav-inner">
        <div class="nav-left">
          <div class="nav-logo">
            <svg width="32" height="32" viewBox="0 0 48 48" fill="none"><rect width="48" height="48" rx="12" fill="#E85D3A"/><path d="M14 18h20v2H14zm0 5h20v2H14zm0 5h14v2H14z" fill="white" opacity="0.9"/><circle cx="36" cy="30" r="4" fill="white" opacity="0.9"/></svg>
          </div>
          <span class="nav-title">演唱会门票系统</span>
        </div>
        <div class="nav-right">
          <span class="user-greeting">
            <img v-if="userAvatar" :src="userAvatar" class="nav-avatar" />
            <i v-else class="el-icon-user-solid"></i>
            {{ userName }}
          </span>
          <button class="nav-btn" @click="$router.push('/profile')"><i class="el-icon-setting"></i><span>个人中心</span></button>
          <button class="nav-btn" @click="$router.push('/order/list')"><i class="el-icon-document"></i><span>我的订单</span></button>
          <button class="nav-btn nav-btn-logout" @click="logout"><i class="el-icon-switch-button"></i><span>退出</span></button>
        </div>
      </div>
    </header>

    <!-- 轮播Banner -->
    <div class="banner-section" v-if="banners.length > 0">
      <el-carousel height="320px" :interval="4000" arrow="hover" indicator-position="outside">
        <el-carousel-item v-for="b in banners" :key="b.id">
          <div class="banner-item" :style="{ backgroundImage: 'url(' + b.image + ')' }">
            <div class="banner-overlay">
              <h2 class="banner-title">{{ b.title }}</h2>
              <p class="banner-desc" v-if="b.content">{{ b.content }}</p>
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>
    </div>

    <!-- 公告栏 -->
    <div class="notice-bar" v-if="notices.length > 0">
      <div class="notice-inner">
        <i class="el-icon-bell" style="color:#E85D3A;font-size:16px;"></i>
        <div class="notice-scroll">
          <span v-for="n in notices" :key="n.id" class="notice-item">{{ n.title }}</span>
        </div>
      </div>
    </div>

    <!-- Hero -->
    <div class="hero-section" v-if="banners.length === 0">
      <div class="hero-inner">
        <div class="hero-badge">HOT CONCERTS</div>
        <h1 class="hero-title">发现精彩演出</h1>
        <p class="hero-subtitle">精选热门演唱会，一键抢票，不容错过</p>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <div class="search-inner">
        <el-input v-model="searchKeyword" placeholder="搜索演唱会名称、歌手、城市..." prefix-icon="el-icon-search" size="medium" clearable @clear="getConcertList" @keyup.enter.native="getConcertList" style="max-width:480px;"></el-input>
        <el-button type="primary" size="medium" @click="getConcertList" icon="el-icon-search">搜索</el-button>
        <el-button size="medium" :class="{ 'fav-active': showFavOnly }" @click="toggleFavFilter"><i :class="showFavOnly ? 'el-icon-star-on' : 'el-icon-star-off'"></i> {{ showFavOnly ? '全部' : '我的收藏' }}</el-button>
      </div>
    </div>

    <!-- 演唱会列表 -->
    <div class="content-wrapper">
      <el-row :gutter="24" class="concert-grid">
        <el-col :xs="24" :sm="12" :md="8" v-for="item in concertList" :key="item.id">
          <div class="concert-card" @click="showConcertDetail(item)">
            <div class="card-cover">
              <img :src="item.poster || getConcertCover(item.name)" alt="演唱会封面">
              <div class="card-status" :class="item.status === 1 ? 'status-on' : 'status-off'">{{ item.status === 1 ? '热售中' : '已售罄' }}</div>
              <div class="fav-btn" :class="{ 'fav-btn-active': favoriteIds.includes(item.id) }" @click.stop="toggleFavorite(item.id)"><i :class="favoriteIds.includes(item.id) ? 'el-icon-star-on' : 'el-icon-star-off'"></i></div>
            </div>
            <div class="card-body">
              <h3 class="concert-name">{{ item.name }}</h3>
              <p class="concert-singer">{{ item.singer }}</p>
              <div class="concert-meta">
                <span class="meta-item"><i class="el-icon-location-outline"></i> {{ item.city }} · {{ item.venue }}</span>
              </div>
              <div class="rating-row" v-if="item.avgRating > 0">
                <div class="stars">
                  <i v-for="s in 5" :key="s" class="el-icon-star-on" :style="{ color: s <= Math.round(item.avgRating) ? '#F59E0B' : '#E5E7EB' }"></i>
                </div>
                <span class="rating-text">{{ item.avgRating }}分 ({{ item.reviewCount }}条评价)</span>
              </div>
              <div class="card-footer">
                <div class="price-block">
                  <span class="price-label">起售价</span>
                  <span class="price-value">¥{{ item.minPrice || '--' }}</span>
                </div>
                <el-button type="primary" size="small" @click.stop="goToBuy(item.id)" v-if="item.status === 1" class="buy-btn">立即抢票</el-button>
                <el-button disabled size="small" v-else class="buy-btn-disabled">已售罄</el-button>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>

      <div v-if="concertList.length === 0" class="empty-state">
        <i class="el-icon-video-camera" style="font-size:48px;color:#D1D5DB;"></i>
        <p class="empty-text">暂无演唱会数据</p>
      </div>

      <div class="pagination-wrap" v-if="total > 0">
        <el-pagination @size-change="handleSizeChange" @current-change="handleCurrentChange" :current-page="currentPage" :page-sizes="[6, 12, 18]" :page-size="pageSize" layout="total, sizes, prev, pager, next, jumper" :total="total"></el-pagination>
      </div>
    </div>

    <!-- 演唱会详情弹窗（含评价） -->
    <el-dialog :title="detailConcert ? detailConcert.name : ''" :visible.sync="detailVisible" width="640px" :close-on-click-modal="true" top="5vh">
      <div v-if="detailConcert" class="detail-content">
        <div class="detail-top">
          <img :src="detailConcert.poster || getConcertCover(detailConcert.name)" class="detail-poster">
          <div class="detail-info">
            <p><strong>歌手/乐队：</strong>{{ detailConcert.singer }}</p>
            <p><strong>城市：</strong>{{ detailConcert.city }}</p>
            <p><strong>场馆：</strong>{{ detailConcert.venue }}</p>
            <p><strong>起售价：</strong><span class="amount">¥{{ detailConcert.minPrice || '--' }}</span></p>
            <p v-if="detailConcert.avgRating > 0"><strong>评分：</strong>
              <i v-for="s in 5" :key="s" class="el-icon-star-on" :style="{ color: s <= Math.round(detailConcert.avgRating) ? '#F59E0B' : '#E5E7EB' }"></i>
              {{ detailConcert.avgRating }}分
            </p>
            <el-button type="primary" size="small" @click="goToBuy(detailConcert.id)" v-if="detailConcert.status === 1" style="margin-top:8px;">立即抢票</el-button>
          </div>
        </div>

        <div class="review-section">
          <h4 class="review-title">用户评价 ({{ reviews.length }})</h4>
          <div class="review-form">
            <div class="review-stars">
              <span style="font-size:13px;color:#6B7280;margin-right:8px;">评分：</span>
              <i v-for="s in 5" :key="s" class="el-icon-star-on star-input" :class="{ active: s <= myRating }" @click="myRating = s"></i>
            </div>
            <div style="display:flex;gap:8px;margin-top:8px;">
              <el-input v-model="myReviewContent" placeholder="写下你的评价..." size="small" style="flex:1;"></el-input>
              <el-button type="primary" size="small" @click="submitReview" :loading="reviewSubmitting">发布</el-button>
            </div>
          </div>
          <div v-if="reviews.length === 0" class="empty-review">暂无评价，快来抢沙发吧~</div>
          <div v-for="r in reviews" :key="r.id" class="review-item">
            <div class="review-header">
              <span class="review-user"><i class="el-icon-user"></i> {{ r.nickname || r.username }}</span>
              <div class="review-rating">
                <i v-for="s in 5" :key="s" class="el-icon-star-on" :style="{ color: s <= r.rating ? '#F59E0B' : '#E5E7EB', fontSize: '14px' }"></i>
              </div>
              <span class="review-time">{{ formatTime(r.createTime) }}</span>
            </div>
            <p class="review-content">{{ r.content }}</p>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
/* eslint-disable vue/no-parsing-error */
export default {
  name: 'HomeView',
  data() {
    return {
      concertList: [], allConcertList: [], currentPage: 1, pageSize: 6, total: 0,
      userName: localStorage.getItem('userName') || '用户',
      userAvatar: '',
      banners: [], notices: [],
      searchKeyword: '', favoriteIds: [], showFavOnly: false,
      detailVisible: false, detailConcert: null,
      reviews: [], myRating: 5, myReviewContent: '', reviewSubmitting: false
    }
  },
  async mounted() {
    this.loadUserAvatar()
    this.loadBanners()
    this.loadNotices()
    this.loadFavorites()
    this.getConcertList()
  },
  methods: {
    async loadBanners() {
      try { const r = await this.$axios.get('/announcement/banners'); if (r.data.code === 200) this.banners = r.data.data || [] } catch (e) { /* ignore */ }
    },
    async loadNotices() {
      try { const r = await this.$axios.get('/announcement/list'); if (r.data.code === 200) this.notices = (r.data.data || []).filter(a => a.type === 0) } catch (e) { /* ignore */ }
    },
    async loadFavorites() {
      const userId = localStorage.getItem('userId')
      if (!userId) return
      try { const r = await this.$axios.get('/favorite/my', { params: { userId }, withCredentials: true }); if (r.data.code === 200) this.favoriteIds = r.data.data || [] } catch (e) { /* ignore */ }
    },
    async toggleFavorite(concertId) {
      const userId = localStorage.getItem('userId')
      if (!userId) { this.$message.error('请先登录'); return }
      try {
        const r = await this.$axios.post('/favorite/toggle', { concertId }, { params: { userId }, withCredentials: true })
        if (r.data.code === 200) { this.$message.success(r.data.data); this.loadFavorites() }
      } catch (e) { this.$message.error('操作失败') }
    },
    toggleFavFilter() {
      this.showFavOnly = !this.showFavOnly
      this.getConcertList()
    },
    async getConcertList() {
      try {
        const res = await this.$axios.get('/concert/list', { params: { current: this.currentPage, size: this.pageSize } })
        if (res.data.code === 200) {
          let list = res.data.data.records
          this.allConcertList = list
          if (this.searchKeyword) {
            const kw = this.searchKeyword.toLowerCase()
            list = list.filter(c => (c.name || '').toLowerCase().includes(kw) || (c.singer || '').toLowerCase().includes(kw) || (c.city || '').toLowerCase().includes(kw))
          }
          if (this.showFavOnly) { list = list.filter(c => this.favoriteIds.includes(c.id)) }
          this.concertList = list
          this.total = this.searchKeyword || this.showFavOnly ? list.length : res.data.data.total
          this.concertList.forEach(item => {
            this.getMinPrice(item.id, item)
            this.getAvgRating(item.id, item)
          })
        }
      } catch (error) { this.$message.error('获取演唱会列表失败') }
    },
    async getMinPrice(concertId, item) {
      try { const r = await this.$axios.get(`/session/minPrice/${concertId}`); if (r.data.code === 200) this.$set(item, 'minPrice', r.data.data ? Number(r.data.data) : 0) } catch (e) { this.$set(item, 'minPrice', 0) }
    },
    async getAvgRating(concertId, item) {
      try { const r = await this.$axios.get(`/review/avg/${concertId}`); if (r.data.code === 200) { this.$set(item, 'avgRating', r.data.data.avg || 0); this.$set(item, 'reviewCount', r.data.data.count || 0) } } catch (e) { this.$set(item, 'avgRating', 0); this.$set(item, 'reviewCount', 0) }
    },
    handleSizeChange(val) { this.pageSize = val; this.getConcertList() },
    handleCurrentChange(val) { this.currentPage = val; this.getConcertList() },
    goToBuy(concertId) {
      this.$router.push(`/buy/${concertId}`)
    },
    getConcertCover(name) { return `https://picsum.photos/seed/${name}/400/220.jpg` },
    formatTime(t) { if (!t) return ''; const d = new Date(t); return isNaN(d.getTime()) ? String(t) : d.toLocaleString('zh-CN', { year:'numeric', month:'2-digit', day:'2-digit', hour:'2-digit', minute:'2-digit' }) },

    async showConcertDetail(item) {
      this.detailConcert = item
      this.detailVisible = true
      this.myRating = 5
      this.myReviewContent = ''
      try { const r = await this.$axios.get(`/review/list/${item.id}`); if (r.data.code === 200) this.reviews = r.data.data || [] } catch (e) { this.reviews = [] }
    },
    async submitReview() {
      if (!this.myReviewContent.trim()) { this.$message.warning('请输入评价内容'); return }
      const userId = localStorage.getItem('userId')
      if (!userId) { this.$message.error('请先登录'); return }
      this.reviewSubmitting = true
      try {
        const r = await this.$axios.post('/review/add', { concertId: this.detailConcert.id, rating: this.myRating, content: this.myReviewContent }, { params: { userId }, withCredentials: true })
        if (r.data.code === 200) {
          this.$message.success('评价成功')
          this.myReviewContent = ''
          const r2 = await this.$axios.get(`/review/list/${this.detailConcert.id}`)
          if (r2.data.code === 200) this.reviews = r2.data.data || []
          this.getAvgRating(this.detailConcert.id, this.detailConcert)
        } else { this.$message.error(r.data.msg || '评价失败') }
      } catch (e) { this.$message.error('评价失败') }
      finally { this.reviewSubmitting = false }
    },

    async loadUserAvatar() {
      const userId = localStorage.getItem('userId')
      if (!userId) return
      try {
        const r = await this.$axios.get('/user/profile', { params: { userId }, withCredentials: true })
        if (r.data.code === 200 && r.data.data) {
          if (r.data.data.avatar) this.userAvatar = r.data.data.avatar
          if (r.data.data.nickname) { this.userName = r.data.data.nickname; localStorage.setItem('userName', r.data.data.nickname) }
        }
      } catch (e) { /* ignore */ }
    },
    logout() {
      this.$confirm('确定要退出登录吗？', '提示', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }).then(async () => {
        try { await this.$axios.post('/user/logout'); localStorage.removeItem('userId'); localStorage.removeItem('userName'); localStorage.removeItem('userInfo'); this.$message.success('退出成功'); this.$router.push('/auth') } catch (e) { localStorage.clear(); this.$router.push('/auth') }
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.home-container { min-height: 100vh; background: #F7F6F3; }

.top-nav { position: sticky; top: 0; z-index: 100; background: rgba(255,255,255,0.92); backdrop-filter: blur(20px) saturate(180%); border-bottom: 1px solid #F3F4F6; }
.nav-inner { max-width: 1200px; margin: 0 auto; display: flex; justify-content: space-between; align-items: center; height: 64px; padding: 0 24px; }
.nav-left { display: flex; align-items: center; gap: 12px; }
.nav-title { font-size: 18px; font-weight: 700; color: #1D1D1F; letter-spacing: 1px; }
.nav-right { display: flex; align-items: center; gap: 8px; }
.user-greeting { font-size: 14px; color: #6B7280; margin-right: 8px; display: flex; align-items: center; gap: 6px; }
.nav-avatar { width: 24px; height: 24px; border-radius: 50%; object-fit: cover; border: 1.5px solid #E85D3A; }
.user-greeting i { color: #E85D3A; }
.nav-btn { display: flex; align-items: center; gap: 6px; padding: 8px 16px; border-radius: 8px; border: 1px solid #E5E7EB; background: transparent; color: #6B7280; font-size: 13px; cursor: pointer; transition: all 0.3s; font-family: inherit; }
.nav-btn:hover { color: #E85D3A; border-color: #E85D3A; background: #FFF4F0; }
.nav-btn-logout { border-color: transparent; color: #9CA3AF; }
.nav-btn-logout:hover { color: #EF4444; border-color: transparent; background: #FEF2F2; }

/* Banner */
.banner-section { max-width: 1200px; margin: 24px auto 0; padding: 0 24px; }
.banner-section >>> .el-carousel__container { border-radius: 16px; overflow: hidden; }
.banner-item { width: 100%; height: 320px; background-size: cover; background-position: center; position: relative; }
.banner-overlay { position: absolute; bottom: 0; left: 0; right: 0; padding: 32px; background: linear-gradient(transparent, rgba(0,0,0,0.6)); }
.banner-title { color: #fff; font-size: 24px; font-weight: 700; margin-bottom: 6px; }
.banner-desc { color: rgba(255,255,255,0.85); font-size: 14px; }

/* 公告栏 */
.notice-bar { max-width: 1200px; margin: 16px auto 0; padding: 0 24px; }
.notice-inner { display: flex; align-items: center; gap: 12px; background: #FFFBEB; border: 1px solid #FDE68A; border-radius: 10px; padding: 10px 16px; overflow: hidden; }
.notice-scroll { display: flex; gap: 40px; overflow: hidden; white-space: nowrap; flex: 1; }
.notice-item { font-size: 13px; color: #92400E; font-weight: 500; }

/* Hero */
.hero-section { padding: 56px 24px 40px; text-align: center; background: linear-gradient(180deg, #fff 0%, #F7F6F3 100%); }
.hero-badge { display: inline-block; padding: 6px 16px; border-radius: 9999px; background: #FFF4F0; color: #E85D3A; font-size: 12px; font-weight: 700; letter-spacing: 2px; margin-bottom: 16px; }
.hero-title { font-size: 40px; font-weight: 900; color: #1D1D1F; margin-bottom: 12px; letter-spacing: 2px; }
.hero-subtitle { font-size: 16px; color: #9CA3AF; letter-spacing: 1px; }

/* 搜索栏 */
.search-bar { max-width: 1200px; margin: 20px auto 0; padding: 0 24px; }
.search-inner { display: flex; gap: 10px; align-items: center; background: #fff; padding: 16px 20px; border-radius: 12px; border: 1px solid #F3F4F6; }
.fav-active { color: #F59E0B !important; border-color: #F59E0B !important; background: #FFFBEB !important; }

.content-wrapper { max-width: 1200px; margin: 0 auto; padding: 24px 24px 60px; }

/* 卡片 */
.concert-card { background: #fff; border-radius: 16px; overflow: hidden; margin-bottom: 24px; border: 1px solid #F3F4F6; transition: all 0.3s; cursor: pointer; }
.concert-card:hover { transform: translateY(-6px); box-shadow: 0 10px 25px -3px rgba(0,0,0,0.06), 0 4px 6px -4px rgba(0,0,0,0.04); border-color: #E5E7EB; }
.card-cover { position: relative; width: 100%; height: 180px; overflow: hidden; }
.card-cover img { width: 100%; height: 100%; object-fit: cover; transition: transform 0.5s; }
.concert-card:hover .card-cover img { transform: scale(1.06); }
.card-status { position: absolute; top: 12px; right: 12px; padding: 4px 12px; border-radius: 9999px; font-size: 12px; font-weight: 600; }
.status-on { background: #ECFDF5; color: #059669; }
.status-off { background: #FEF2F2; color: #DC2626; }
.fav-btn { position: absolute; top: 12px; left: 12px; width: 32px; height: 32px; border-radius: 50%; background: rgba(255,255,255,0.85); display: flex; align-items: center; justify-content: center; cursor: pointer; font-size: 16px; color: #D1D5DB; transition: all 0.3s; z-index: 2; }
.fav-btn:hover { transform: scale(1.15); }
.fav-btn-active { color: #F59E0B; background: rgba(255,255,255,0.95); }
.card-body { padding: 20px; }
.concert-name { font-size: 17px; font-weight: 700; color: #1D1D1F; margin-bottom: 6px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.concert-singer { font-size: 14px; color: #9CA3AF; margin-bottom: 12px; }
.concert-meta { padding-bottom: 12px; border-bottom: 1px solid #F3F4F6; margin-bottom: 12px; }
.meta-item { font-size: 13px; color: #6B7280; display: flex; align-items: center; gap: 6px; }
.meta-item i { color: #E85D3A; font-size: 14px; }
.rating-row { display: flex; align-items: center; gap: 8px; margin-bottom: 12px; }
.stars { display: flex; gap: 2px; }
.stars i { font-size: 14px; }
.rating-text { font-size: 12px; color: #9CA3AF; }
.card-footer { display: flex; align-items: center; justify-content: space-between; }
.price-block { display: flex; flex-direction: column; }
.price-label { font-size: 11px; color: #9CA3AF; text-transform: uppercase; letter-spacing: 1px; }
.price-value { font-size: 22px; font-weight: 700; color: #E85D3A; }
.buy-btn { border-radius: 8px !important; font-weight: 600 !important; padding: 8px 20px !important; height: 36px !important; }
.buy-btn-disabled { border-radius: 8px !important; background: #F3F4F6 !important; border-color: #F3F4F6 !important; color: #9CA3AF !important; }
.amount { font-weight: 700; color: #E85D3A; }

/* 详情弹窗 */
.detail-content { max-height: 70vh; overflow-y: auto; }
.detail-top { display: flex; gap: 20px; margin-bottom: 24px; }
.detail-poster { width: 200px; height: 260px; object-fit: cover; border-radius: 12px; flex-shrink: 0; }
.detail-info { display: flex; flex-direction: column; gap: 8px; font-size: 14px; color: #374151; }
.detail-info strong { color: #6B7280; }

/* 评价区域 */
.review-section { border-top: 1px solid #F3F4F6; padding-top: 20px; }
.review-title { font-size: 16px; font-weight: 700; color: #1D1D1F; margin-bottom: 16px; }
.review-form { background: #F9FAFB; border-radius: 10px; padding: 14px; margin-bottom: 16px; }
.review-stars { display: flex; align-items: center; }
.star-input { font-size: 22px; cursor: pointer; color: #E5E7EB; transition: color 0.2s; }
.star-input.active { color: #F59E0B; }
.star-input:hover { color: #F59E0B; }
.empty-review { text-align: center; padding: 24px; color: #9CA3AF; font-size: 13px; }
.review-item { padding: 12px 0; border-bottom: 1px solid #F3F4F6; }
.review-header { display: flex; align-items: center; gap: 12px; margin-bottom: 6px; }
.review-user { font-size: 13px; font-weight: 600; color: #374151; display: flex; align-items: center; gap: 4px; }
.review-rating { display: flex; gap: 1px; }
.review-time { font-size: 12px; color: #9CA3AF; margin-left: auto; }
.review-content { font-size: 14px; color: #4B5563; line-height: 1.6; }

.empty-state { text-align: center; padding: 80px 20px; }
.empty-text { font-size: 16px; color: #6B7280; margin-top: 16px; }
.pagination-wrap { display: flex; justify-content: center; margin-top: 40px; }

@media (max-width: 768px) {
  .hero-title { font-size: 28px; }
  .hero-section { padding: 40px 20px 32px; }
  .nav-btn span { display: none; }
  .detail-top { flex-direction: column; align-items: center; }
  .detail-poster { width: 100%; height: 200px; }
}
</style>
