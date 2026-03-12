<template>
  <div class="admin-container">
    <div class="admin-nav">
      <div class="admin-nav-inner">
        <div class="nav-brand">
          <svg width="28" height="28" viewBox="0 0 48 48" fill="none"><rect width="48" height="48" rx="12" fill="#E85D3A"/><path d="M14 18h20v2H14zm0 5h20v2H14zm0 5h14v2H14z" fill="white" opacity="0.9"/></svg>
          <span class="nav-title">管理后台</span>
        </div>
        <div class="nav-actions">
          <span class="nav-user"><i class="el-icon-user-solid"></i> {{ userName }}</span>
          <button class="nav-logout" @click="logout"><i class="el-icon-switch-button"></i> 退出</button>
        </div>
      </div>
    </div>

    <div class="admin-body">
      <div class="admin-sidebar">
        <button class="sidebar-btn" :class="{ active: activeTab === 'dashboard' }" @click="switchTab('dashboard')"><i class="el-icon-data-analysis"></i> 数据概览</button>
        <button class="sidebar-btn" :class="{ active: activeTab === 'orders' }" @click="switchTab('orders')"><i class="el-icon-document"></i> 订单管理</button>
        <button class="sidebar-btn" :class="{ active: activeTab === 'concerts' }" @click="switchTab('concerts')"><i class="el-icon-film"></i> 演唱会管理</button>
        <button class="sidebar-btn" :class="{ active: activeTab === 'sessions' }" @click="switchTab('sessions')"><i class="el-icon-date"></i> 场次管理</button>
        <button class="sidebar-btn" :class="{ active: activeTab === 'users' }" @click="switchTab('users')"><i class="el-icon-user"></i> 用户管理</button>
        <button class="sidebar-btn" :class="{ active: activeTab === 'announcements' }" @click="switchTab('announcements')"><i class="el-icon-bell"></i> 公告管理</button>
        <button class="sidebar-btn" :class="{ active: activeTab === 'seatAreas' }" @click="switchTab('seatAreas')"><i class="el-icon-office-building"></i> 座位区域</button>
        <button class="sidebar-btn" :class="{ active: activeTab === 'reviews' }" @click="switchTab('reviews')"><i class="el-icon-chat-line-square"></i> 评价管理</button>
        <button class="sidebar-btn" :class="{ active: activeTab === 'verify' }" @click="switchTab('verify')"><i class="el-icon-circle-check"></i> 验票核销</button>
      </div>

      <div class="admin-main">
        <!-- ====== 数据概览 ====== -->
        <div v-if="activeTab === 'dashboard'" class="panel">
          <div class="panel-header"><h3 class="panel-title">数据概览</h3></div>
          <div class="stat-cards">
            <div class="stat-card"><div class="stat-icon" style="background:#FFF4F0;color:#E85D3A;"><i class="el-icon-document"></i></div><div class="stat-info"><span class="stat-value">{{ stats.totalOrders }}</span><span class="stat-label">总订单数</span></div></div>
            <div class="stat-card"><div class="stat-icon" style="background:#ECFDF5;color:#059669;"><i class="el-icon-circle-check"></i></div><div class="stat-info"><span class="stat-value">{{ stats.paidOrders }}</span><span class="stat-label">已支付</span></div></div>
            <div class="stat-card"><div class="stat-icon" style="background:#EFF6FF;color:#2563EB;"><i class="el-icon-film"></i></div><div class="stat-info"><span class="stat-value">{{ stats.totalConcerts }}</span><span class="stat-label">演唱会数</span></div></div>
            <div class="stat-card"><div class="stat-icon" style="background:#FFFBEB;color:#D97706;"><i class="el-icon-money"></i></div><div class="stat-info"><span class="stat-value">¥{{ stats.totalRevenue }}</span><span class="stat-label">总收入</span></div></div>
          </div>
          <div class="chart-row">
            <div class="chart-card"><h4 class="chart-title">订单状态分布</h4><div ref="pieChart" class="chart-box"></div></div>
            <div class="chart-card"><h4 class="chart-title">各演唱会销售额</h4><div ref="barChart" class="chart-box"></div></div>
          </div>
        </div>

        <!-- ====== 订单管理 ====== -->
        <div v-if="activeTab === 'orders'" class="panel">
          <div class="panel-header">
            <h3 class="panel-title">订单管理</h3>
            <div class="header-tools">
              <el-input v-model="orderSearch" placeholder="搜索订单号/用户/演唱会" prefix-icon="el-icon-search" size="small" clearable style="width:240px;"></el-input>
              <el-select v-model="orderStatusFilter" placeholder="状态筛选" size="small" clearable style="width:120px;">
                <el-option label="待支付" :value="0"></el-option>
                <el-option label="已支付" :value="1"></el-option>
                <el-option label="已取消" :value="2"></el-option>
                <el-option label="创建中" :value="3"></el-option>
                <el-option label="已退款" :value="4"></el-option>
              </el-select>
              <button class="refresh-btn" @click="loadOrders"><i class="el-icon-refresh"></i></button>
            </div>
          </div>
          <el-table :data="filteredOrders" v-loading="ordersLoading" style="width:100%" :header-cell-style="headerStyle" size="small">
            <el-table-column prop="orderNo" label="订单编号" min-width="240"><template slot-scope="scope"><span class="mono">{{ scope.row.orderNo }}</span></template></el-table-column>
            <el-table-column prop="username" label="用户" min-width="80"></el-table-column>
            <el-table-column prop="concertName" label="演唱会" min-width="200"></el-table-column>
            <el-table-column prop="ticketNum" label="数量" min-width="50" align="center"></el-table-column>
            <el-table-column label="金额" min-width="80"><template slot-scope="scope"><span class="amount">¥{{ scope.row.totalAmount }}</span></template></el-table-column>
            <el-table-column label="状态" min-width="80" align="center"><template slot-scope="scope"><span class="status-pill" :class="'status-' + scope.row.status">{{ getOrderStatusText(scope.row.status) }}</span></template></el-table-column>
            <el-table-column label="创建时间" min-width="150"><template slot-scope="scope">{{ formatTime(scope.row.createTime) }}</template></el-table-column>
            <el-table-column label="操作" min-width="150" align="center">
              <template slot-scope="scope">
                <div style="display:flex;gap:4px;justify-content:center;">
                  <button class="action-btn action-open" v-if="scope.row.status === 5" @click="approveRefund(scope.row)">通过</button>
                  <button class="action-btn action-delete" v-if="scope.row.status === 5" @click="rejectRefund(scope.row)">拒绝</button>
                  <button class="action-btn action-delete" @click="deleteOrder(scope.row)">删除</button>
                </div>
              </template>
            </el-table-column>
          </el-table>
          <div v-if="filteredOrders.length === 0 && !ordersLoading" class="empty">暂无订单数据</div>
        </div>

        <!-- ====== 演唱会管理 ====== -->
        <div v-if="activeTab === 'concerts'" class="panel">
          <div class="panel-header">
            <h3 class="panel-title">演唱会管理</h3>
            <div class="header-tools">
              <el-input v-model="concertSearch" placeholder="搜索名称/歌手/城市" prefix-icon="el-icon-search" size="small" clearable style="width:220px;"></el-input>
              <button class="add-btn" @click="openConcertDialog(null)"><i class="el-icon-plus"></i> 新增</button>
              <button class="refresh-btn" @click="loadConcerts"><i class="el-icon-refresh"></i></button>
            </div>
          </div>
          <el-table :data="filteredConcerts" v-loading="concertsLoading" style="width:100%" :header-cell-style="headerStyle" size="small">
            <el-table-column prop="id" label="ID" min-width="50" align="center"></el-table-column>
            <el-table-column prop="name" label="演唱会名称" min-width="200"></el-table-column>
            <el-table-column prop="singer" label="歌手/乐队" min-width="100"></el-table-column>
            <el-table-column prop="city" label="城市" min-width="70"></el-table-column>
            <el-table-column prop="venue" label="场馆" min-width="150"></el-table-column>
            <el-table-column label="状态" min-width="80" align="center"><template slot-scope="scope"><span class="status-pill" :class="scope.row.status===1?'status-1':'status-2'">{{ scope.row.status===1?'已上架':'未上架' }}</span></template></el-table-column>
            <el-table-column label="操作" min-width="120" align="center">
              <template slot-scope="scope"><div style="display:flex;gap:4px;justify-content:center;"><button class="action-btn action-edit" @click="openConcertDialog(scope.row)">编辑</button><button class="action-btn action-delete" @click="deleteConcert(scope.row)">删除</button></div></template>
            </el-table-column>
          </el-table>
          <div v-if="filteredConcerts.length === 0 && !concertsLoading" class="empty">暂无演唱会数据</div>
        </div>

        <!-- ====== 场次管理 ====== -->
        <div v-if="activeTab === 'sessions'" class="panel">
          <div class="panel-header">
            <h3 class="panel-title">场次管理</h3>
            <div class="header-tools">
              <el-input v-model="sessionSearch" placeholder="搜索演唱会名称" prefix-icon="el-icon-search" size="small" clearable style="width:180px;"></el-input>
              <el-select v-model="sessionStatusFilter" placeholder="状态" size="small" clearable style="width:100px;">
                <el-option label="未开票" :value="0"></el-option>
                <el-option label="售票中" :value="1"></el-option>
                <el-option label="已售罄" :value="2"></el-option>
                <el-option label="已结束" :value="3"></el-option>
              </el-select>
              <button class="add-btn" @click="openSessionDialog(null)"><i class="el-icon-plus"></i> 新增</button>
              <button class="refresh-btn" @click="loadSessions"><i class="el-icon-refresh"></i></button>
            </div>
          </div>
          <el-table :data="filteredSessions" v-loading="sessionsLoading" style="width:100%" :header-cell-style="headerStyle" size="small">
            <el-table-column prop="id" label="ID" min-width="50" align="center"></el-table-column>
            <el-table-column label="演唱会" min-width="160"><template slot-scope="scope">{{ getConcertName(scope.row.concertId) }}</template></el-table-column>
            <el-table-column label="演出时间" min-width="150"><template slot-scope="scope">{{ formatTime(scope.row.showTime) }}</template></el-table-column>
            <el-table-column label="票价" min-width="80"><template slot-scope="scope"><span class="amount">¥{{ scope.row.price }}</span></template></el-table-column>
            <el-table-column prop="totalStock" label="总库存" min-width="70" align="center"></el-table-column>
            <el-table-column prop="surplusStock" label="剩余" min-width="60" align="center"></el-table-column>
            <el-table-column label="状态" min-width="80" align="center"><template slot-scope="scope"><span class="status-pill" :class="'session-status-' + scope.row.status">{{ getSessionStatusText(scope.row.status) }}</span></template></el-table-column>
            <el-table-column label="操作" min-width="180" align="center">
              <template slot-scope="scope"><div style="display:flex;gap:4px;justify-content:center;">
                <button class="action-btn action-preload" v-if="scope.row.status===0" @click="preloadStock(scope.row.id)">预热</button>
                <button class="action-btn action-open" v-if="scope.row.status===0" @click="openSell(scope.row.id)">开票</button>
                <button class="action-btn action-edit" @click="openSessionDialog(scope.row)">编辑</button>
                <button class="action-btn action-delete" @click="deleteSession(scope.row)">删除</button>
              </div></template>
            </el-table-column>
          </el-table>
          <div v-if="filteredSessions.length === 0 && !sessionsLoading" class="empty">暂无场次数据</div>
        </div>

        <!-- ====== 用户管理 ====== -->
        <div v-if="activeTab === 'users'" class="panel">
          <div class="panel-header">
            <h3 class="panel-title">用户管理</h3>
            <div class="header-tools">
              <el-input v-model="userSearch" placeholder="搜索用户名/昵称/手机号" prefix-icon="el-icon-search" size="small" clearable style="width:240px;"></el-input>
              <button class="refresh-btn" @click="loadUsers"><i class="el-icon-refresh"></i></button>
            </div>
          </div>
          <el-table :data="filteredUsers" v-loading="usersLoading" style="width:100%" :header-cell-style="headerStyle" size="small">
            <el-table-column prop="id" label="ID" min-width="60" align="center"></el-table-column>
            <el-table-column prop="username" label="用户名" min-width="120"></el-table-column>
            <el-table-column prop="nickname" label="昵称" min-width="120"></el-table-column>
            <el-table-column prop="phone" label="手机号" min-width="130"></el-table-column>
            <el-table-column label="角色" min-width="80" align="center"><template slot-scope="scope"><span class="status-pill" :class="scope.row.type===1?'status-1':'status-0'">{{ scope.row.type===1?'管理员':'用户' }}</span></template></el-table-column>
            <el-table-column label="状态" min-width="80" align="center"><template slot-scope="scope"><span class="status-pill" :class="scope.row.status===0?'status-1':'status-2'">{{ scope.row.status===0?'正常':'禁用' }}</span></template></el-table-column>
            <el-table-column label="注册时间" min-width="150"><template slot-scope="scope">{{ formatTime(scope.row.createTime) }}</template></el-table-column>
            <el-table-column label="操作" min-width="120" align="center">
              <template slot-scope="scope"><div style="display:flex;gap:4px;justify-content:center;">
                <button class="action-btn action-open" @click="viewUserOrders(scope.row)">订单</button>
                <button class="action-btn action-edit" @click="openUserDialog(scope.row)">编辑</button>
                <button class="action-btn action-delete" v-if="scope.row.type!==1" @click="deleteUser(scope.row)">删除</button>
              </div></template>
            </el-table-column>
          </el-table>
          <div v-if="filteredUsers.length === 0 && !usersLoading" class="empty">暂无用户数据</div>
        </div>

        <!-- ====== 公告管理 ====== -->
        <div v-if="activeTab === 'announcements'" class="panel">
          <div class="panel-header">
            <h3 class="panel-title">公告管理</h3>
            <div class="header-tools">
              <button class="add-btn" @click="openAnnouncementDialog(null)"><i class="el-icon-plus"></i> 新增</button>
              <button class="refresh-btn" @click="loadAnnouncements"><i class="el-icon-refresh"></i></button>
            </div>
          </div>
          <el-table :data="announcementList" v-loading="announcementsLoading" style="width:100%" :header-cell-style="headerStyle" size="small">
            <el-table-column prop="id" label="ID" min-width="50" align="center"></el-table-column>
            <el-table-column prop="title" label="标题" min-width="200"></el-table-column>
            <el-table-column label="类型" min-width="80" align="center"><template slot-scope="scope"><span class="status-pill" :class="scope.row.type===1?'status-1':'status-0'">{{ scope.row.type===1?'轮播':'公告' }}</span></template></el-table-column>
            <el-table-column label="状态" min-width="80" align="center"><template slot-scope="scope"><span class="status-pill" :class="scope.row.status===1?'status-1':'status-2'">{{ scope.row.status===1?'显示':'隐藏' }}</span></template></el-table-column>
            <el-table-column prop="sortOrder" label="排序" min-width="60" align="center"></el-table-column>
            <el-table-column label="图片" min-width="80" align="center"><template slot-scope="scope"><img v-if="scope.row.image" :src="scope.row.image" style="width:50px;height:30px;object-fit:cover;border-radius:4px;"><span v-else style="color:#9CA3AF;">-</span></template></el-table-column>
            <el-table-column label="创建时间" min-width="150"><template slot-scope="scope">{{ formatTime(scope.row.createTime) }}</template></el-table-column>
            <el-table-column label="操作" min-width="120" align="center">
              <template slot-scope="scope"><div style="display:flex;gap:4px;justify-content:center;">
                <button class="action-btn action-edit" @click="openAnnouncementDialog(scope.row)">编辑</button>
                <button class="action-btn action-delete" @click="deleteAnnouncement(scope.row)">删除</button>
              </div></template>
            </el-table-column>
          </el-table>
          <div v-if="announcementList.length === 0 && !announcementsLoading" class="empty">暂无公告数据</div>
        </div>

        <!-- ====== 评价管理 ====== -->
        <div v-if="activeTab === 'reviews'" class="panel">
          <div class="panel-header">
            <h3 class="panel-title">评价管理</h3>
            <div class="header-tools">
              <el-input v-model="reviewSearch" placeholder="搜索用户/演唱会/内容" prefix-icon="el-icon-search" size="small" clearable style="width:220px;"></el-input>
              <el-select v-model="reviewRatingFilter" placeholder="评分筛选" size="small" clearable style="width:120px;">
                <el-option label="5星" :value="5"></el-option>
                <el-option label="4星" :value="4"></el-option>
                <el-option label="3星" :value="3"></el-option>
                <el-option label="2星" :value="2"></el-option>
                <el-option label="1星" :value="1"></el-option>
              </el-select>
              <button class="refresh-btn" @click="loadReviews"><i class="el-icon-refresh"></i></button>
            </div>
          </div>
          <div class="review-stats-row">
            <div class="review-stat-item"><span class="review-stat-val">{{ reviewList.length }}</span><span class="review-stat-label">总评价</span></div>
            <div class="review-stat-item"><span class="review-stat-val">{{ reviewAvg }}</span><span class="review-stat-label">平均评分</span></div>
            <div class="review-stat-item"><span class="review-stat-val">{{ reviewList.filter(r => r.rating >= 4).length }}</span><span class="review-stat-label">好评(4-5星)</span></div>
            <div class="review-stat-item"><span class="review-stat-val">{{ reviewList.filter(r => r.rating <= 2).length }}</span><span class="review-stat-label">差评(1-2星)</span></div>
          </div>
          <el-table :data="filteredReviews" v-loading="reviewsLoading" :header-cell-style="headerStyle" stripe size="small">
            <el-table-column label="用户" min-width="100">
              <template slot-scope="scope">{{ scope.row.nickname || scope.row.username || ('用户' + scope.row.user_id) }}</template>
            </el-table-column>
            <el-table-column label="演唱会" prop="concertName" min-width="140"></el-table-column>
            <el-table-column label="评分" width="140" align="center">
              <template slot-scope="scope">
                <div class="star-rating">
                  <i v-for="n in 5" :key="n" class="el-icon-star-on" :style="{ color: n <= scope.row.rating ? '#F59E0B' : '#E5E7EB', fontSize: '16px' }"></i>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="评价内容" prop="content" min-width="200" show-overflow-tooltip></el-table-column>
            <el-table-column label="时间" width="160">
              <template slot-scope="scope">{{ formatTime(scope.row.create_time) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="80" align="center">
              <template slot-scope="scope">
                <button class="action-btn action-delete" @click="deleteReview(scope.row)">删除</button>
              </template>
            </el-table-column>
          </el-table>
          <div v-if="filteredReviews.length === 0 && !reviewsLoading" class="empty">暂无评价数据</div>
        </div>

        <!-- ====== 验票核销 ====== -->
        <div v-if="activeTab === 'verify'" class="panel">
          <div class="panel-header"><h3 class="panel-title">验票核销</h3></div>
          <div class="verify-section">
            <div class="verify-input-area">
              <div class="verify-icon-wrap">
                <svg width="64" height="64" viewBox="0 0 64 64" fill="none"><rect width="64" height="64" rx="16" fill="#FFF4F0"/><path d="M20 24h24v2H20zm0 6h24v2H20zm0 6h16v2H20z" fill="#E85D3A" opacity="0.6"/><circle cx="44" cy="40" r="6" fill="#E85D3A" opacity="0.8"/><path d="M41.5 40l1.5 1.5 3-3" stroke="white" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/></svg>
              </div>
              <p class="verify-desc">请输入用户电子票上的核销码</p>
              <div class="verify-input-row">
                <el-input v-model="verifyCodeInput" placeholder="请输入核销码" size="large" clearable style="width:300px;font-size:18px;letter-spacing:4px;" @keyup.enter.native="doVerify" maxlength="8"></el-input>
                <el-button type="primary" size="large" @click="doVerify" :loading="verifyLoading" style="height:48px;padding:0 32px;font-size:16px;border-radius:10px;">
                  <i class="el-icon-circle-check" v-if="!verifyLoading"></i> 核销
                </el-button>
              </div>
            </div>
            <div class="verify-result" v-if="verifyResult">
              <div class="verify-success" v-if="verifyResult.success">
                <div class="verify-result-icon"><i class="el-icon-circle-check"></i></div>
                <h4>核销成功</h4>
                <div class="verify-result-info">
                  <div class="verify-result-row"><span>订单编号</span><span class="mono">{{ verifyResult.orderNo }}</span></div>
                  <div class="verify-result-row"><span>票数</span><span>{{ verifyResult.ticketNum }} 张</span></div>
                  <div class="verify-result-row"><span>金额</span><span class="amount">¥{{ verifyResult.totalAmount }}</span></div>
                  <div class="verify-result-row"><span>核销码</span><span class="mono">{{ verifyResult.verifyCode }}</span></div>
                </div>
              </div>
              <div class="verify-fail" v-else>
                <div class="verify-result-icon fail"><i class="el-icon-circle-close"></i></div>
                <h4>{{ verifyResult.msg }}</h4>
              </div>
            </div>
          </div>
        </div>

        <!-- ====== 座位区域管理 ====== -->
        <div v-if="activeTab === 'seatAreas'" class="panel">
          <div class="panel-header">
            <h3 class="panel-title">座位区域管理</h3>
            <div class="header-tools">
              <el-input v-model="seatAreaSearch" placeholder="搜索区域名称" prefix-icon="el-icon-search" size="small" clearable style="width:180px;"></el-input>
              <button class="add-btn" @click="openSeatAreaDialog(null)"><i class="el-icon-plus"></i> 新增</button>
              <button class="refresh-btn" @click="loadSeatAreas"><i class="el-icon-refresh"></i></button>
            </div>
          </div>
          <el-table :data="filteredSeatAreas" v-loading="seatAreasLoading" style="width:100%" :header-cell-style="headerStyle" size="small">
            <el-table-column prop="id" label="ID" min-width="50" align="center"></el-table-column>
            <el-table-column label="场次" min-width="200"><template slot-scope="scope">{{ getSeatAreaSessionLabel(scope.row.sessionId) }}</template></el-table-column>
            <el-table-column prop="areaName" label="区域名称" min-width="120"></el-table-column>
            <el-table-column label="票价" min-width="100" align="center"><template slot-scope="scope">¥{{ scope.row.price }}</template></el-table-column>
            <el-table-column prop="totalStock" label="总座位" min-width="80" align="center"></el-table-column>
            <el-table-column prop="surplusStock" label="剩余" min-width="80" align="center"></el-table-column>
            <el-table-column label="操作" min-width="120" align="center">
              <template slot-scope="scope"><div style="display:flex;gap:4px;justify-content:center;">
                <button class="action-btn action-edit" @click="openSeatAreaDialog(scope.row)">编辑</button>
                <button class="action-btn action-delete" @click="deleteSeatArea(scope.row)">删除</button>
              </div></template>
            </el-table-column>
          </el-table>
          <div v-if="filteredSeatAreas.length === 0 && !seatAreasLoading" class="empty">暂无座位区域数据</div>
        </div>
      </div>
    </div>

    <!-- ====== 演唱会弹窗 ====== -->
    <el-dialog :title="concertForm.id ? '编辑演唱会' : '新增演唱会'" :visible.sync="concertDialogVisible" width="520px" :close-on-click-modal="false">
      <el-form :model="concertForm" label-width="90px" size="small">
        <el-form-item label="名称"><el-input v-model="concertForm.name" placeholder="演唱会名称"></el-input></el-form-item>
        <el-form-item label="歌手/乐队"><el-input v-model="concertForm.singer" placeholder="歌手或乐队名称"></el-input></el-form-item>
        <el-form-item label="城市"><el-input v-model="concertForm.city" placeholder="举办城市"></el-input></el-form-item>
        <el-form-item label="场馆"><el-input v-model="concertForm.venue" placeholder="场馆名称"></el-input></el-form-item>
        <el-form-item label="海报">
          <el-upload class="poster-upload" action="/api/file/upload" name="file" :show-file-list="false" :on-success="handlePosterSuccess" :on-error="handlePosterError" :before-upload="beforePosterUpload" :with-credentials="true" accept="image/*">
            <img v-if="concertForm.poster" :src="concertForm.poster" class="poster-preview">
            <div v-else class="poster-placeholder"><i class="el-icon-plus"></i><span>点击上传海报</span></div>
          </el-upload>
        </el-form-item>
        <el-form-item label="状态"><el-select v-model="concertForm.status" placeholder="选择状态"><el-option label="未上架" :value="0"></el-option><el-option label="已上架" :value="1"></el-option></el-select></el-form-item>
      </el-form>
      <span slot="footer"><el-button @click="concertDialogVisible = false" size="small">取消</el-button><el-button type="primary" @click="saveConcert" size="small" :loading="concertSaving">保存</el-button></span>
    </el-dialog>

    <!-- ====== 场次弹窗 ====== -->
    <el-dialog :title="sessionForm.id ? '编辑场次' : '新增场次'" :visible.sync="sessionDialogVisible" width="520px" :close-on-click-modal="false">
      <el-form :model="sessionForm" label-width="90px" size="small">
        <el-form-item label="演唱会"><el-select v-model="sessionForm.concertId" placeholder="请选择演唱会" style="width:100%;"><el-option v-for="c in concertList" :key="c.id" :label="c.name" :value="c.id"></el-option></el-select></el-form-item>
        <el-form-item label="演出时间"><el-date-picker v-model="sessionForm.showTime" type="datetime" placeholder="选择演出时间" style="width:100%;"></el-date-picker></el-form-item>
        <el-form-item label="票价"><el-input-number v-model="sessionForm.price" :min="0" :precision="2"></el-input-number></el-form-item>
        <el-form-item label="总库存"><el-input-number v-model="sessionForm.totalStock" :min="1"></el-input-number></el-form-item>
        <el-form-item label="剩余库存"><el-input-number v-model="sessionForm.surplusStock" :min="0"></el-input-number></el-form-item>
        <el-form-item label="状态"><el-select v-model="sessionForm.status"><el-option label="未开票" :value="0"></el-option><el-option label="售票中" :value="1"></el-option><el-option label="已售罄" :value="2"></el-option><el-option label="已结束" :value="3"></el-option></el-select></el-form-item>
      </el-form>
      <span slot="footer"><el-button @click="sessionDialogVisible = false" size="small">取消</el-button><el-button type="primary" @click="saveSession" size="small" :loading="sessionSaving">保存</el-button></span>
    </el-dialog>

    <!-- ====== 座位区域弹窗 ====== -->
    <el-dialog :title="seatAreaForm.id ? '编辑座位区域' : '新增座位区域'" :visible.sync="seatAreaDialogVisible" width="480px" :close-on-click-modal="false">
      <el-form :model="seatAreaForm" label-width="80px" size="small">
        <el-form-item label="场次"><el-select v-model="seatAreaForm.sessionId" placeholder="请选择场次" style="width:100%;"><el-option v-for="s in sessionList" :key="s.id" :label="getSessionLabel(s)" :value="s.id"></el-option></el-select></el-form-item>
        <el-form-item label="区域名称"><el-input v-model="seatAreaForm.areaName" placeholder="如：VIP区、普通区、看台区"></el-input></el-form-item>
        <el-form-item label="票价"><el-input-number v-model="seatAreaForm.price" :min="0" :precision="2"></el-input-number></el-form-item>
        <el-form-item label="总座位数"><el-input-number v-model="seatAreaForm.totalStock" :min="1"></el-input-number></el-form-item>
        <el-form-item label="剩余座位"><el-input-number v-model="seatAreaForm.surplusStock" :min="0"></el-input-number></el-form-item>
      </el-form>
      <span slot="footer"><el-button @click="seatAreaDialogVisible = false" size="small">取消</el-button><el-button type="primary" @click="saveSeatArea" size="small" :loading="seatAreaSaving">保存</el-button></span>
    </el-dialog>

    <!-- ====== 公告弹窗 ====== -->
    <el-dialog :title="announcementForm.id ? '编辑公告' : '新增公告'" :visible.sync="announcementDialogVisible" width="520px" :close-on-click-modal="false">
      <el-form :model="announcementForm" label-width="80px" size="small">
        <el-form-item label="标题"><el-input v-model="announcementForm.title" placeholder="公告标题"></el-input></el-form-item>
        <el-form-item label="内容"><el-input v-model="announcementForm.content" type="textarea" :rows="3" placeholder="公告内容"></el-input></el-form-item>
        <el-form-item label="图片">
          <el-upload class="poster-upload" action="/api/file/upload" name="file" :show-file-list="false" :on-success="handleAnnouncementImageSuccess" :before-upload="beforePosterUpload" :with-credentials="true" accept="image/*">
            <img v-if="announcementForm.image" :src="announcementForm.image" class="poster-preview">
            <div v-else class="poster-placeholder"><i class="el-icon-plus"></i><span>上传图片</span></div>
          </el-upload>
        </el-form-item>
        <el-form-item label="类型"><el-select v-model="announcementForm.type"><el-option label="普通公告" :value="0"></el-option><el-option label="轮播Banner" :value="1"></el-option></el-select></el-form-item>
        <el-form-item label="状态"><el-select v-model="announcementForm.status"><el-option label="显示" :value="1"></el-option><el-option label="隐藏" :value="0"></el-option></el-select></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="announcementForm.sortOrder" :min="0"></el-input-number></el-form-item>
      </el-form>
      <span slot="footer"><el-button @click="announcementDialogVisible = false" size="small">取消</el-button><el-button type="primary" @click="saveAnnouncement" size="small" :loading="announcementSaving">保存</el-button></span>
    </el-dialog>

    <!-- ====== 用户订单弹窗 ====== -->
    <el-dialog :title="'用户订单 - ' + userOrderTarget" :visible.sync="userOrderDialogVisible" width="900px" :close-on-click-modal="true">
      <div v-loading="userOrdersLoading">
        <div style="margin-bottom:12px;display:flex;justify-content:space-between;align-items:center;">
          <span style="font-size:13px;color:#6B7280;">共 {{ userOrderList.length }} 条订单，已支付 {{ userOrderList.filter(o=>o.status===1).length }} 条，总消费 ¥{{ userOrderTotalSpent }}</span>
        </div>
        <el-table :data="userOrderList" style="width:100%" :header-cell-style="headerStyle" size="small" max-height="400">
          <el-table-column prop="orderNo" label="订单编号" min-width="200"><template slot-scope="scope"><span class="mono">{{ scope.row.orderNo }}</span></template></el-table-column>
          <el-table-column label="演唱会" min-width="180"><template slot-scope="scope">{{ getConcertName(scope.row.concertId) }}</template></el-table-column>
          <el-table-column label="座位区域" min-width="100"><template slot-scope="scope">{{ scope.row.seatAreaName || '-' }}</template></el-table-column>
          <el-table-column prop="ticketNum" label="数量" min-width="50" align="center"></el-table-column>
          <el-table-column label="金额" min-width="80"><template slot-scope="scope"><span class="amount">¥{{ scope.row.totalAmount }}</span></template></el-table-column>
          <el-table-column label="状态" min-width="80" align="center"><template slot-scope="scope"><span class="status-pill" :class="'status-' + scope.row.status">{{ getOrderStatusText(scope.row.status) }}</span></template></el-table-column>
          <el-table-column label="支付时间" min-width="140"><template slot-scope="scope">{{ formatTime(scope.row.payTime) || '-' }}</template></el-table-column>
          <el-table-column label="创建时间" min-width="140"><template slot-scope="scope">{{ formatTime(scope.row.createTime) }}</template></el-table-column>
        </el-table>
        <div v-if="userOrderList.length === 0 && !userOrdersLoading" class="empty">该用户暂无订单</div>
      </div>
      <span slot="footer"><el-button @click="userOrderDialogVisible = false" size="small">关闭</el-button></span>
    </el-dialog>

    <!-- ====== 用户编辑弹窗 ====== -->
    <el-dialog title="编辑用户" :visible.sync="userDialogVisible" width="480px" :close-on-click-modal="false">
      <el-form :model="userForm" label-width="80px" size="small">
        <el-form-item label="用户名"><el-input v-model="userForm.username" disabled></el-input></el-form-item>
        <el-form-item label="昵称"><el-input v-model="userForm.nickname" placeholder="用户昵称"></el-input></el-form-item>
        <el-form-item label="手机号"><el-input v-model="userForm.phone" placeholder="手机号"></el-input></el-form-item>
        <el-form-item label="角色"><el-select v-model="userForm.type"><el-option label="普通用户" :value="0"></el-option><el-option label="管理员" :value="1"></el-option></el-select></el-form-item>
        <el-form-item label="状态"><el-select v-model="userForm.status"><el-option label="正常" :value="0"></el-option><el-option label="禁用" :value="1"></el-option></el-select></el-form-item>
      </el-form>
      <span slot="footer"><el-button @click="userDialogVisible = false" size="small">取消</el-button><el-button type="primary" @click="saveUser" size="small" :loading="userSaving">保存</el-button></span>
    </el-dialog>
  </div>
</template>

<script>
import * as echarts from 'echarts'

export default {
  name: 'AdminView',
  data() {
    return {
      activeTab: 'dashboard',
      userName: localStorage.getItem('userName') || '管理员',
      headerStyle: { background: '#F9FAFB', color: '#6B7280', fontWeight: '600', fontSize: '13px' },
      orderList: [], concertList: [], sessionList: [], userList: [],
      ordersLoading: false, concertsLoading: false, sessionsLoading: false, usersLoading: false,
      stats: { totalOrders: 0, paidOrders: 0, totalConcerts: 0, totalRevenue: 0 },
      pieChartInstance: null, barChartInstance: null,
      // 搜索筛选
      orderSearch: '', orderStatusFilter: '',
      concertSearch: '',
      sessionSearch: '', sessionStatusFilter: '',
      userSearch: '',
      // 演唱会弹窗
      concertDialogVisible: false, concertSaving: false,
      concertForm: { id: null, name: '', singer: '', city: '', venue: '', poster: '', status: 1 },
      // 场次弹窗
      sessionDialogVisible: false, sessionSaving: false,
      sessionForm: { id: null, concertId: 1, showTime: '', price: 0, totalStock: 100, surplusStock: 100, status: 0 },
      // 用户弹窗
      userDialogVisible: false, userSaving: false,
      userForm: { id: null, username: '', nickname: '', phone: '', type: 0, status: 0 },
      // 用户订单弹窗
      userOrderDialogVisible: false, userOrdersLoading: false, userOrderList: [], userOrderTarget: '',
      // 公告
      announcementList: [], announcementsLoading: false,
      announcementDialogVisible: false, announcementSaving: false,
      announcementForm: { id: null, title: '', content: '', image: '', type: 0, status: 1, sortOrder: 0 },
      // 座位区域
      seatAreaList: [], seatAreasLoading: false, seatAreaSearch: '',
      seatAreaDialogVisible: false, seatAreaSaving: false,
      seatAreaForm: { id: null, sessionId: 1, areaName: '', price: 0, totalStock: 100, surplusStock: 100 },
      // 评价管理
      reviewList: [], reviewsLoading: false, reviewSearch: '', reviewRatingFilter: '',
      // 验票核销
      verifyCodeInput: '', verifyLoading: false, verifyResult: null
    }
  },
  computed: {
    userOrderTotalSpent() {
      return this.userOrderList.filter(o => o.status === 1).reduce((s, o) => s + (parseFloat(o.totalAmount) || 0), 0).toFixed(2)
    },
    filteredOrders() {
      let list = this.orderList
      if (this.orderStatusFilter !== '' && this.orderStatusFilter !== null) {
        list = list.filter(o => o.status === this.orderStatusFilter)
      }
      if (this.orderSearch) {
        const kw = this.orderSearch.toLowerCase()
        list = list.filter(o => (o.orderNo || '').toLowerCase().includes(kw) || (o.username || '').toLowerCase().includes(kw) || (o.concertName || '').toLowerCase().includes(kw))
      }
      return list
    },
    filteredConcerts() {
      if (!this.concertSearch) return this.concertList
      const kw = this.concertSearch.toLowerCase()
      return this.concertList.filter(c => (c.name || '').toLowerCase().includes(kw) || (c.singer || '').toLowerCase().includes(kw) || (c.city || '').toLowerCase().includes(kw))
    },
    filteredSessions() {
      let list = this.sessionList
      if (this.sessionStatusFilter !== '' && this.sessionStatusFilter !== null) {
        list = list.filter(s => s.status === this.sessionStatusFilter)
      }
      if (this.sessionSearch) {
        const kw = this.sessionSearch.toLowerCase()
        list = list.filter(s => { const name = this.getConcertName(s.concertId); return name.toLowerCase().includes(kw) })
      }
      return list
    },
    filteredUsers() {
      if (!this.userSearch) return this.userList
      const kw = this.userSearch.toLowerCase()
      return this.userList.filter(u => (u.username || '').toLowerCase().includes(kw) || (u.nickname || '').toLowerCase().includes(kw) || (u.phone || '').includes(kw))
    },
    filteredSeatAreas() {
      if (!this.seatAreaSearch) return this.seatAreaList
      const kw = this.seatAreaSearch.toLowerCase()
      return this.seatAreaList.filter(a => (a.areaName || '').toLowerCase().includes(kw) || this.getSeatAreaSessionLabel(a.sessionId).toLowerCase().includes(kw))
    },
    filteredReviews() {
      let list = this.reviewList
      if (this.reviewRatingFilter !== '' && this.reviewRatingFilter !== null) {
        list = list.filter(r => r.rating === this.reviewRatingFilter)
      }
      if (this.reviewSearch) {
        const kw = this.reviewSearch.toLowerCase()
        list = list.filter(r => (r.nickname || r.username || '').toLowerCase().includes(kw) || (r.concertName || '').toLowerCase().includes(kw) || (r.content || '').toLowerCase().includes(kw))
      }
      return list
    },
    reviewAvg() {
      if (this.reviewList.length === 0) return '0.0'
      const avg = this.reviewList.reduce((s, r) => s + (r.rating || 0), 0) / this.reviewList.length
      return avg.toFixed(1)
    }
  },
  watch: {
    activeTab(val) {
      if (val === 'dashboard') this.$nextTick(() => this.renderCharts())
    }
  },
  mounted() {
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    if (userInfo.type !== 1) { this.$message.error('无管理员权限'); this.$router.push('/home'); return }
    this.loadAllData()
    window.addEventListener('resize', this.handleResize)
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.handleResize)
    if (this.pieChartInstance) this.pieChartInstance.dispose()
    if (this.barChartInstance) this.barChartInstance.dispose()
  },
  methods: {
    uid() { return localStorage.getItem('userId') },
    handleResize() {
      if (this.pieChartInstance) this.pieChartInstance.resize()
      if (this.barChartInstance) this.barChartInstance.resize()
    },
    formatTime(t) { if (!t) return ''; const d = new Date(t); return isNaN(d.getTime()) ? String(t) : d.toLocaleString('zh-CN', { year:'numeric', month:'2-digit', day:'2-digit', hour:'2-digit', minute:'2-digit' }) },
    getOrderStatusText(s) { return { 0:'待支付', 1:'已支付', 2:'已取消', 3:'创建中', 4:'已退款', 5:'退款审核中', 6:'已核销' }[s] || '未知' },
    getSessionStatusText(s) { return { 0:'未开票', 1:'售票中', 2:'已售罄', 3:'已结束' }[s] || '未知' },
    getConcertName(concertId) { const c = this.concertList.find(c => c.id === concertId); return c ? c.name : ('未知演唱会') },
    getSeatAreaSessionLabel(sessionId) { const s = this.sessionList.find(s => s.id === sessionId); if (!s) return '未知场次'; return this.getConcertName(s.concertId) + ' - ' + this.formatTime(s.showTime) },
    switchTab(tab) { this.activeTab = tab; if (tab === 'users' && this.userList.length === 0) this.loadUsers(); if (tab === 'announcements' && this.announcementList.length === 0) this.loadAnnouncements(); if (tab === 'seatAreas' && this.seatAreaList.length === 0) this.loadSeatAreas(); if (tab === 'reviews' && this.reviewList.length === 0) this.loadReviews() },

    async loadAllData() {
      await Promise.all([this.loadOrders(), this.loadConcerts(), this.loadSessions(), this.loadUsers()])
      this.computeStats()
      this.$nextTick(() => this.renderCharts())
    },
    async loadOrders() {
      this.ordersLoading = true
      try { const res = await this.$axios.get('/order/all', { params: { userId: this.uid() }, withCredentials: true }); if (res.data.code === 200) this.orderList = res.data.data || [] } catch (e) { this.$message.error('获取订单失败') }
      finally { this.ordersLoading = false }
    },
    async loadConcerts() {
      this.concertsLoading = true
      try { const res = await this.$axios.get('/concert/all', { withCredentials: true }); if (res.data.code === 200) this.concertList = res.data.data || [] } catch (e) { this.$message.error('获取演唱会失败') }
      finally { this.concertsLoading = false }
    },
    async loadSessions() {
      this.sessionsLoading = true
      try { const res = await this.$axios.get('/session/all', { withCredentials: true }); if (res.data.code === 200) this.sessionList = res.data.data || [] } catch (e) { this.$message.error('获取场次失败') }
      finally { this.sessionsLoading = false }
    },
    async loadUsers() {
      this.usersLoading = true
      try { const res = await this.$axios.get('/user/all', { params: { userId: this.uid() }, withCredentials: true }); if (res.data.code === 200) this.userList = res.data.data || [] } catch (e) { this.$message.error('获取用户失败') }
      finally { this.usersLoading = false }
    },

    computeStats() {
      this.stats.totalOrders = this.orderList.length
      this.stats.paidOrders = this.orderList.filter(o => o.status === 1).length
      this.stats.totalConcerts = this.concertList.length
      this.stats.totalRevenue = this.orderList.filter(o => o.status === 1).reduce((s, o) => s + (parseFloat(o.totalAmount) || 0), 0).toFixed(2)
    },
    renderCharts() { this.renderPieChart(); this.renderBarChart() },
    renderPieChart() {
      if (!this.$refs.pieChart) return
      if (this.pieChartInstance) this.pieChartInstance.dispose()
      this.pieChartInstance = echarts.init(this.$refs.pieChart)
      const data = [
        { value: this.orderList.filter(o => o.status === 0).length, name: '待支付' },
        { value: this.orderList.filter(o => o.status === 1).length, name: '已支付' },
        { value: this.orderList.filter(o => o.status === 2).length, name: '已取消' },
        { value: this.orderList.filter(o => o.status === 3).length, name: '创建中' },
        { value: this.orderList.filter(o => o.status === 4).length, name: '已退款' },
        { value: this.orderList.filter(o => o.status === 5).length, name: '退款审核中' },
        { value: this.orderList.filter(o => o.status === 6).length, name: '已核销' }
      ].filter(d => d.value > 0)
      this.pieChartInstance.setOption({
        tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
        legend: { bottom: 0, textStyle: { fontSize: 12, color: '#6B7280' } },
        color: ['#D97706','#059669','#9CA3AF','#2563EB','#EF4444','#EA580C','#10B981'],
        series: [{ type:'pie', radius:['40%','65%'], center:['50%','45%'], label:{ show:true, formatter:'{b}\n{c}单', fontSize:12 }, data }]
      })
    },
    renderBarChart() {
      if (!this.$refs.barChart) return
      if (this.barChartInstance) this.barChartInstance.dispose()
      this.barChartInstance = echarts.init(this.$refs.barChart)
      const revenueMap = {}, nameMap = {}
      this.concertList.forEach(c => { nameMap[c.id] = c.singer || c.name })
      this.orderList.filter(o => o.status === 1 || o.status === 6).forEach(o => { if (!revenueMap[o.concertId]) revenueMap[o.concertId] = 0; revenueMap[o.concertId] += parseFloat(o.totalAmount) || 0 })
      const names = [], values = []
      Object.keys(revenueMap).forEach(k => { names.push(nameMap[k] || ('ID:'+k)); values.push(revenueMap[k]) })
      if (!names.length) { names.push('暂无数据'); values.push(0) }
      this.barChartInstance.setOption({
        tooltip: { trigger:'axis', formatter: (p) => { const c = this.concertList.find(c => (c.singer || c.name) === p[0].name); return (c ? c.name : p[0].name) + '<br/>销售额: ¥' + p[0].value } },
        grid: { left:60, right:20, top:20, bottom:60 },
        xAxis: { type:'category', data:names, axisLabel:{ fontSize:12, color:'#6B7280', interval:0 } },
        yAxis: { type:'value', axisLabel:{ fontSize:11, color:'#9CA3AF', formatter:'¥{value}' } },
        series: [{ type:'bar', data:values, barWidth:'40%', itemStyle:{ borderRadius:[6,6,0,0], color: new echarts.graphic.LinearGradient(0,0,0,1,[{offset:0,color:'#E85D3A'},{offset:1,color:'#FF9A76'}]) } }]
      })
    },

    // ====== 订单 ======
    deleteOrder(row) {
      this.$confirm(`确定删除订单 ${row.orderNo}？`, '确认', { type:'warning' }).then(async () => {
        try { const r = await this.$axios.delete(`/order/delete/${row.id}`, { params:{ userId:this.uid() }, withCredentials:true }); if (r.data.code===200) { this.$message.success('删除成功'); this.loadOrders(); this.computeStats(); this.$nextTick(()=>this.renderCharts()) } else this.$message.error(r.data.msg||'删除失败') } catch(e) { this.$message.error('删除失败') }
      }).catch(()=>{})
    },
    approveRefund(row) {
      this.$confirm(`确定通过订单 ${row.orderNo} 的退款申请？`, '审核退款', { type:'warning' }).then(async () => {
        try { const r = await this.$axios.post('/order/refund', { orderNo:row.orderNo, adminUserId:this.uid() }, { withCredentials:true }); if (r.data.code===200) { this.$message.success('退款审核通过'); this.loadOrders(); this.computeStats(); this.$nextTick(()=>this.renderCharts()) } else this.$message.error(r.data.msg||'操作失败') } catch(e) { this.$message.error('操作失败') }
      }).catch(()=>{})
    },
    rejectRefund(row) {
      this.$confirm(`确定拒绝订单 ${row.orderNo} 的退款申请？拒绝后订单将恢复为已支付状态。`, '拒绝退款', { type:'warning' }).then(async () => {
        try { const r = await this.$axios.post('/order/refund/reject', { orderNo:row.orderNo, adminUserId:this.uid() }, { withCredentials:true }); if (r.data.code===200) { this.$message.success('已拒绝退款申请'); this.loadOrders(); this.computeStats(); this.$nextTick(()=>this.renderCharts()) } else this.$message.error(r.data.msg||'操作失败') } catch(e) { this.$message.error('操作失败') }
      }).catch(()=>{})
    },

    // ====== 海报上传 ======
    beforePosterUpload(f) { if (!f.type.startsWith('image/')) { this.$message.error('只能上传图片'); return false } if (f.size/1024/1024>5) { this.$message.error('图片不能超过5MB'); return false } return true },
    handlePosterSuccess(r) { if (r.code===200) { this.concertForm.poster=r.data; this.$message.success('上传成功') } else this.$message.error(r.msg||'上传失败') },
    handlePosterError() { this.$message.error('上传失败') },

    // ====== 演唱会 ======
    openConcertDialog(row) { this.concertForm = row ? { ...row } : { id:null, name:'', singer:'', city:'', venue:'', poster:'', status:1 }; this.concertDialogVisible = true },
    async saveConcert() {
      if (!this.concertForm.name) { this.$message.warning('请输入名称'); return }
      this.concertSaving = true
      try { let r; if (this.concertForm.id) { r = await this.$axios.put('/concert/update', this.concertForm, { params:{userId:this.uid()}, withCredentials:true }) } else { r = await this.$axios.post('/concert/add', this.concertForm, { params:{userId:this.uid()}, withCredentials:true }) } if (r.data.code===200) { this.$message.success('保存成功'); this.concertDialogVisible=false; this.loadConcerts() } else this.$message.error(r.data.msg||'操作失败') } catch(e) { this.$message.error('操作失败') }
      finally { this.concertSaving = false }
    },
    deleteConcert(row) {
      this.$confirm(`确定删除「${row.name}」？`, '确认', { type:'warning' }).then(async () => {
        try { const r = await this.$axios.delete(`/concert/delete/${row.id}`, { params:{userId:this.uid()}, withCredentials:true }); if (r.data.code===200) { this.$message.success('删除成功'); this.loadConcerts() } else this.$message.error(r.data.msg||'删除失败') } catch(e) { this.$message.error('删除失败') }
      }).catch(()=>{})
    },

    // ====== 场次 ======
    openSessionDialog(row) { if (this.concertList.length === 0) this.loadConcerts(); this.sessionForm = row ? { ...row } : { id:null, concertId:'', showTime:'', price:0, totalStock:100, surplusStock:100, status:0 }; this.sessionDialogVisible = true },
    async saveSession() {
      if (!this.sessionForm.concertId) { this.$message.warning('请选择演唱会'); return }
      this.sessionSaving = true
      try { let r; if (this.sessionForm.id) { r = await this.$axios.put('/session/update', this.sessionForm, { params:{userId:this.uid()}, withCredentials:true }) } else { r = await this.$axios.post('/session/add', this.sessionForm, { params:{userId:this.uid()}, withCredentials:true }) } if (r.data.code===200) { this.$message.success('保存成功'); this.sessionDialogVisible=false; this.loadSessions() } else this.$message.error(r.data.msg||'操作失败') } catch(e) { this.$message.error('操作失败') }
      finally { this.sessionSaving = false }
    },
    deleteSession(row) {
      this.$confirm(`确定删除场次 ID=${row.id}？`, '确认', { type:'warning' }).then(async () => {
        try { const r = await this.$axios.delete(`/session/delete/${row.id}`, { params:{userId:this.uid()}, withCredentials:true }); if (r.data.code===200) { this.$message.success('删除成功'); this.loadSessions() } else this.$message.error(r.data.msg||'删除失败') } catch(e) { this.$message.error('删除失败') }
      }).catch(()=>{})
    },
    async preloadStock(id) { try { const r = await this.$axios.post(`/session/preload/${id}`, {}, { withCredentials:true }); if (r.data.code===200) { this.$message.success('预热成功'); this.loadSessions() } else this.$message.error(r.data.msg||'预热失败') } catch(e) { this.$message.error('预热失败') } },
    async openSell(id) { try { const r = await this.$axios.post(`/session/open/${id}`, {}, { withCredentials:true }); if (r.data.code===200) { this.$message.success('开票成功'); this.loadSessions() } else this.$message.error(r.data.msg||'开票失败') } catch(e) { this.$message.error('开票失败') } },

    // ====== 用户 ======
    openUserDialog(row) { this.userForm = { ...row }; this.userDialogVisible = true },
    async saveUser() {
      this.userSaving = true
      try { const r = await this.$axios.put('/user/update', this.userForm, { params:{adminUserId:this.uid()}, withCredentials:true }); if (r.data.code===200) { this.$message.success('修改成功'); this.userDialogVisible=false; this.loadUsers() } else this.$message.error(r.data.msg||'修改失败') } catch(e) { this.$message.error('修改失败') }
      finally { this.userSaving = false }
    },
    deleteUser(row) {
      this.$confirm(`确定删除用户「${row.username}」？`, '确认', { type:'warning' }).then(async () => {
        try { const r = await this.$axios.delete(`/user/delete/${row.id}`, { params:{userId:this.uid()}, withCredentials:true }); if (r.data.code===200) { this.$message.success('删除成功'); this.loadUsers() } else this.$message.error(r.data.msg||'删除失败') } catch(e) { this.$message.error('删除失败') }
      }).catch(()=>{})
    },

    // ====== 公告 ======
    async loadAnnouncements() {
      this.announcementsLoading = true
      try { const r = await this.$axios.get('/announcement/all', { params:{ userId:this.uid() }, withCredentials:true }); if (r.data.code===200) this.announcementList = r.data.data || [] } catch(e) { this.$message.error('获取公告失败') }
      finally { this.announcementsLoading = false }
    },
    openAnnouncementDialog(row) { this.announcementForm = row ? { ...row } : { id:null, title:'', content:'', image:'', type:0, status:1, sortOrder:0 }; this.announcementDialogVisible = true },
    handleAnnouncementImageSuccess(r) { if (r.code===200) { this.announcementForm.image=r.data; this.$message.success('上传成功') } else this.$message.error(r.msg||'上传失败') },
    async saveAnnouncement() {
      if (!this.announcementForm.title) { this.$message.warning('请输入标题'); return }
      this.announcementSaving = true
      try { let r; if (this.announcementForm.id) { r = await this.$axios.put('/announcement/update', this.announcementForm, { params:{userId:this.uid()}, withCredentials:true }) } else { r = await this.$axios.post('/announcement/add', this.announcementForm, { params:{userId:this.uid()}, withCredentials:true }) } if (r.data.code===200) { this.$message.success('保存成功'); this.announcementDialogVisible=false; this.loadAnnouncements() } else this.$message.error(r.data.msg||'操作失败') } catch(e) { this.$message.error('操作失败') }
      finally { this.announcementSaving = false }
    },
    deleteAnnouncement(row) {
      this.$confirm(`确定删除公告「${row.title}」？`, '确认', { type:'warning' }).then(async () => {
        try { const r = await this.$axios.delete(`/announcement/delete/${row.id}`, { params:{userId:this.uid()}, withCredentials:true }); if (r.data.code===200) { this.$message.success('删除成功'); this.loadAnnouncements() } else this.$message.error(r.data.msg||'删除失败') } catch(e) { this.$message.error('删除失败') }
      }).catch(()=>{})
    },

    // ====== 座位区域 ======
    async loadSeatAreas() {
      this.seatAreasLoading = true
      try { const r = await this.$axios.get('/seatArea/all', { withCredentials:true }); if (r.data.code===200) this.seatAreaList = r.data.data || [] } catch(e) { this.$message.error('获取座位区域失败') }
      finally { this.seatAreasLoading = false }
    },
    getSessionLabel(s) { const concert = this.concertList.find(c => c.id === s.concertId); const name = concert ? concert.name : ('演唱会ID:' + s.concertId); return '场次' + s.id + ' - ' + name + ' - ' + this.formatTime(s.showTime) },
    openSeatAreaDialog(row) { if (this.sessionList.length === 0) this.loadSessions(); if (this.concertList.length === 0) this.loadConcerts(); this.seatAreaForm = row ? { ...row } : { id:null, sessionId:'', areaName:'', price:0, totalStock:100, surplusStock:100 }; this.seatAreaDialogVisible = true },
    async saveSeatArea() {
      if (!this.seatAreaForm.areaName) { this.$message.warning('请输入区域名称'); return }
      this.seatAreaSaving = true
      try { let r; if (this.seatAreaForm.id) { r = await this.$axios.put('/seatArea/update', this.seatAreaForm, { params:{userId:this.uid()}, withCredentials:true }) } else { r = await this.$axios.post('/seatArea/add', this.seatAreaForm, { params:{userId:this.uid()}, withCredentials:true }) } if (r.data.code===200) { this.$message.success('保存成功'); this.seatAreaDialogVisible=false; this.loadSeatAreas() } else this.$message.error(r.data.msg||'操作失败') } catch(e) { this.$message.error('操作失败') }
      finally { this.seatAreaSaving = false }
    },
    deleteSeatArea(row) {
      this.$confirm(`确定删除区域「${row.areaName}」？`, '确认', { type:'warning' }).then(async () => {
        try { const r = await this.$axios.delete(`/seatArea/delete/${row.id}`, { params:{userId:this.uid()}, withCredentials:true }); if (r.data.code===200) { this.$message.success('删除成功'); this.loadSeatAreas() } else this.$message.error(r.data.msg||'删除失败') } catch(e) { this.$message.error('删除失败') }
      }).catch(()=>{})
    },

    async viewUserOrders(user) {
      this.userOrderTarget = user.nickname || user.username
      this.userOrderDialogVisible = true
      this.userOrdersLoading = true
      this.userOrderList = []
      try {
        const r = await this.$axios.get('/order/all', { params: { userId: this.uid() }, withCredentials: true })
        if (r.data.code === 200) {
          this.userOrderList = (r.data.data || []).filter(o => o.userId === user.id)
        }
      } catch (e) { this.$message.error('获取订单失败') }
      finally { this.userOrdersLoading = false }
    },
    // ====== 评价管理 ======
    async loadReviews() {
      this.reviewsLoading = true
      try {
        const r = await this.$axios.get('/review/all', { params: { userId: this.uid() }, withCredentials: true })
        if (r.data.code === 200) this.reviewList = r.data.data || []
        else this.$message.error(r.data.msg || '获取评价失败')
      } catch (e) { this.$message.error('获取评价失败') }
      finally { this.reviewsLoading = false }
    },
    deleteReview(row) {
      this.$confirm(`确定删除该条评价？`, '确认', { type: 'warning' }).then(async () => {
        try {
          const r = await this.$axios.delete(`/review/delete/${row.id}`, { params: { userId: this.uid() }, withCredentials: true })
          if (r.data.code === 200) { this.$message.success('删除成功'); this.loadReviews() }
          else this.$message.error(r.data.msg || '删除失败')
        } catch (e) { this.$message.error('删除失败') }
      }).catch(() => {})
    },

    // ====== 验票核销 ======
    async doVerify() {
      if (!this.verifyCodeInput || !this.verifyCodeInput.trim()) { this.$message.warning('请输入核销码'); return }
      this.verifyLoading = true
      this.verifyResult = null
      try {
        const r = await this.$axios.post('/order/verify', { verifyCode: this.verifyCodeInput.trim(), adminUserId: this.uid() }, { withCredentials: true })
        if (r.data.code === 200) {
          this.verifyResult = { success: true, ...r.data.data }
          this.$message.success('核销成功！')
          this.verifyCodeInput = ''
          this.loadOrders()
          this.computeStats()
          this.$nextTick(() => this.renderCharts())
        } else {
          this.verifyResult = { success: false, msg: r.data.msg || '核销失败' }
          this.$message.error(r.data.msg || '核销失败')
        }
      } catch (e) { this.verifyResult = { success: false, msg: '核销失败，请重试' }; this.$message.error('核销失败') }
      finally { this.verifyLoading = false }
    },
    logout() { localStorage.clear(); this.$axios.post('/user/logout', {}, { withCredentials:true }).catch(()=>{}); this.$message.success('已退出'); this.$router.push('/auth') }
  }
}
</script>

<style scoped>
.admin-container { min-height: 100vh; background: #F7F6F3; }
.admin-nav { background: #fff; border-bottom: 1px solid #F3F4F6; position: sticky; top: 0; z-index: 10; }
.admin-nav-inner { margin: 0 auto; padding: 12px 24px; display: flex; justify-content: space-between; align-items: center; }
.nav-brand { display: flex; align-items: center; gap: 10px; }
.nav-title { font-size: 18px; font-weight: 700; color: #1D1D1F; }
.nav-actions { display: flex; align-items: center; gap: 16px; }
.nav-user { font-size: 14px; color: #6B7280; display: flex; align-items: center; gap: 6px; }
.nav-logout { background: none; border: 1px solid #E5E7EB; border-radius: 8px; padding: 6px 14px; font-size: 13px; color: #6B7280; cursor: pointer; font-family: inherit; display: flex; align-items: center; gap: 4px; transition: all 0.3s; }
.nav-logout:hover { color: #EF4444; border-color: #EF4444; }

.admin-body { margin: 0 auto; padding: 24px; display: flex; gap: 20px; }
.admin-sidebar { width: 170px; flex-shrink: 0; display: flex; flex-direction: column; gap: 4px; position: sticky; top: 80px; align-self: flex-start; }
.sidebar-btn { display: flex; align-items: center; gap: 10px; width: 100%; padding: 12px 16px; border: none; border-radius: 10px; background: transparent; font-size: 14px; font-weight: 500; color: #6B7280; cursor: pointer; font-family: inherit; transition: all 0.2s; text-align: left; }
.sidebar-btn:hover { background: #fff; color: #1D1D1F; }
.sidebar-btn.active { background: #fff; color: #E85D3A; font-weight: 600; box-shadow: 0 1px 3px rgba(0,0,0,0.06); }
.sidebar-btn i { font-size: 16px; }
.admin-main { flex: 1; min-width: 0; }

.panel { background: #fff; border-radius: 16px; padding: 24px; border: 1px solid #F3F4F6; }
.panel-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; flex-wrap: wrap; gap: 10px; }
.panel-title { font-size: 18px; font-weight: 700; color: #1D1D1F; }
.header-tools { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.refresh-btn { display: flex; align-items: center; justify-content: center; width: 32px; height: 32px; border-radius: 8px; border: 1px solid #E5E7EB; background: transparent; color: #6B7280; font-size: 14px; cursor: pointer; transition: all 0.3s; }
.refresh-btn:hover { color: #E85D3A; border-color: #E85D3A; }
.add-btn { display: flex; align-items: center; gap: 6px; padding: 6px 14px; border-radius: 8px; border: none; background: #E85D3A; color: #fff; font-size: 13px; font-weight: 600; cursor: pointer; font-family: inherit; transition: all 0.3s; }
.add-btn:hover { background: #FF7F5C; }

.stat-cards { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 24px; }
.stat-card { display: flex; align-items: center; gap: 14px; padding: 20px; border-radius: 12px; border: 1px solid #F3F4F6; }
.stat-icon { width: 44px; height: 44px; border-radius: 10px; display: flex; align-items: center; justify-content: center; font-size: 20px; flex-shrink: 0; }
.stat-info { display: flex; flex-direction: column; }
.stat-value { font-size: 22px; font-weight: 700; color: #1D1D1F; }
.stat-label { font-size: 12px; color: #9CA3AF; margin-top: 2px; }
.chart-row { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.chart-card { border: 1px solid #F3F4F6; border-radius: 12px; padding: 20px; }
.chart-title { font-size: 15px; font-weight: 600; color: #1D1D1F; margin-bottom: 12px; }
.chart-box { width: 100%; height: 300px; }

.mono { font-family: 'DM Sans', monospace; font-size: 12px; font-weight: 600; color: #1D1D1F; }
.amount { font-weight: 700; color: #E85D3A; }
.status-pill { display: inline-block; padding: 2px 8px; border-radius: 9999px; font-size: 11px; font-weight: 600; }
.status-0 { background: #FFFBEB; color: #D97706; }
.status-1 { background: #ECFDF5; color: #059669; }
.status-2 { background: #F3F4F6; color: #9CA3AF; }
.status-3 { background: #EFF6FF; color: #2563EB; }
.status-4 { background: #FEF2F2; color: #EF4444; }
.status-5 { background: #FFF7ED; color: #EA580C; }
.session-status-0 { background: #F3F4F6; color: #6B7280; }
.session-status-1 { background: #ECFDF5; color: #059669; }
.session-status-2 { background: #FEF2F2; color: #EF4444; }
.session-status-3 { background: #F3F4F6; color: #9CA3AF; }

.action-btn { padding: 4px 10px; border-radius: 4px; font-size: 12px; font-weight: 500; cursor: pointer; border: none; font-family: inherit; transition: all 0.2s; }
.action-edit { background: #EFF6FF; color: #2563EB; }
.action-edit:hover { background: #DBEAFE; }
.action-delete { background: #FEF2F2; color: #EF4444; }
.action-delete:hover { background: #FEE2E2; }
.action-preload { background: #F0FDF4; color: #059669; }
.action-preload:hover { background: #DCFCE7; }
.action-open { background: #E85D3A; color: #fff; }
.action-open:hover { background: #FF7F5C; }
.action-refund { background: #FFFBEB; color: #D97706; }
.action-refund:hover { background: #FEF3C7; }

.poster-upload >>> .el-upload { width: 140px; height: 180px; border: 1px dashed #D1D5DB; border-radius: 8px; cursor: pointer; overflow: hidden; transition: border-color 0.3s; display: flex; align-items: center; justify-content: center; }
.poster-upload >>> .el-upload:hover { border-color: #E85D3A; }
.poster-preview { width: 100%; height: 100%; object-fit: cover; }
.poster-placeholder { display: flex; flex-direction: column; align-items: center; gap: 8px; color: #9CA3AF; font-size: 13px; }
.poster-placeholder i { font-size: 24px; color: #D1D5DB; }

.empty { text-align: center; padding: 40px; color: #9CA3AF; font-size: 14px; }

.verify-section { display: flex; flex-direction: column; align-items: center; padding: 40px 20px; }
.verify-input-area { text-align: center; margin-bottom: 32px; }
.verify-icon-wrap { margin-bottom: 16px; }
.verify-desc { font-size: 15px; color: #6B7280; margin-bottom: 24px; }
.verify-input-row { display: flex; gap: 12px; align-items: center; justify-content: center; }
.verify-input-row >>> .el-input__inner { font-size: 20px !important; letter-spacing: 6px; text-align: center; height: 48px; border-radius: 10px; text-transform: uppercase; }
.verify-result { margin-top: 16px; width: 100%; max-width: 400px; }
.verify-success { text-align: center; background: #F0FDF4; border: 1px solid #BBF7D0; border-radius: 16px; padding: 32px; }
.verify-success h4 { font-size: 18px; color: #059669; margin: 12px 0 20px; }
.verify-result-icon { font-size: 48px; color: #059669; }
.verify-result-icon.fail { color: #EF4444; }
.verify-fail { text-align: center; background: #FEF2F2; border: 1px solid #FECACA; border-radius: 16px; padding: 32px; }
.verify-fail h4 { font-size: 16px; color: #EF4444; margin-top: 12px; }
.verify-result-info { text-align: left; }
.verify-result-row { display: flex; justify-content: space-between; padding: 8px 0; border-bottom: 1px solid #D1FAE5; font-size: 14px; }
.verify-result-row span:first-child { color: #6B7280; }
.verify-result-row span:last-child { font-weight: 600; color: #1D1D1F; }
.status-6 { background: #F0FDF4; color: #059669; }

.review-stats-row { display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px; margin-bottom: 20px; }
.review-stat-item { text-align: center; padding: 16px; background: #F9FAFB; border-radius: 10px; border: 1px solid #F3F4F6; }
.review-stat-val { display: block; font-size: 24px; font-weight: 700; color: #1D1D1F; }
.review-stat-label { display: block; font-size: 12px; color: #9CA3AF; margin-top: 4px; }
.star-rating { display: flex; gap: 2px; justify-content: center; }

.panel >>> .el-table .cell { white-space: nowrap !important; }

@media (max-width: 768px) {
  .admin-body { flex-direction: column; }
  .admin-sidebar { width: 100%; flex-direction: row; overflow-x: auto; position: static; }
  .stat-cards { grid-template-columns: 1fr 1fr; }
  .chart-row { grid-template-columns: 1fr; }
}
</style>
