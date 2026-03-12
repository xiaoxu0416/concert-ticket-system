<template>
  <div class="profile-container">
    <header class="top-nav">
      <div class="nav-inner">
        <div class="nav-left">
          <button class="back-link" @click="$router.push('/home')"><i class="el-icon-arrow-left"></i> 返回首页</button>
          <span class="nav-divider">/</span>
          <span class="nav-current">个人中心</span>
        </div>
      </div>
    </header>

    <div class="profile-content">
      <div class="profile-card">
        <div class="profile-header">
          <div class="avatar-wrapper" @click="triggerAvatarUpload" title="点击更换头像">
            <div class="avatar" v-if="!userProfile.avatar"><i class="el-icon-user-solid"></i></div>
            <img v-else :src="userProfile.avatar" class="avatar-img" alt="avatar" />
            <div class="avatar-overlay"><i class="el-icon-camera"></i></div>
            <input type="file" ref="avatarInput" accept="image/*" style="display:none" @change="handleAvatarUpload" />
          </div>
          <div class="user-info">
            <h2 class="user-name">{{ userProfile.nickname || userProfile.username }}</h2>
            <p class="user-id" v-if="userProfile.type === 1">管理员</p>
          </div>
        </div>

        <div class="stat-row">
          <div class="stat-item">
            <span class="stat-num">{{ stats.totalOrders }}</span>
            <span class="stat-label">总订单</span>
          </div>
          <div class="stat-item">
            <span class="stat-num">{{ stats.paidOrders }}</span>
            <span class="stat-label">已支付</span>
          </div>
          <div class="stat-item">
            <span class="stat-num">¥{{ stats.totalSpent }}</span>
            <span class="stat-label">总消费</span>
          </div>
          <div class="stat-item">
            <span class="stat-num">{{ stats.concertCount }}</span>
            <span class="stat-label">参与演出</span>
          </div>
        </div>
      </div>

      <div class="section-card">
        <h3 class="section-title"><i class="el-icon-edit"></i> 修改个人信息</h3>
        <el-form :model="editForm" label-width="80px" size="small" style="max-width:400px;">
          <el-form-item label="用户名">
            <el-input :value="userProfile.username" disabled></el-input>
          </el-form-item>
          <el-form-item label="昵称">
            <el-input v-model="editForm.nickname" placeholder="输入昵称"></el-input>
          </el-form-item>
          <el-form-item label="手机号">
            <el-input v-model="editForm.phone" placeholder="输入手机号"></el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="saveProfile" :loading="profileSaving">保存修改</el-button>
          </el-form-item>
        </el-form>
      </div>

      <div class="section-card">
        <h3 class="section-title"><i class="el-icon-lock"></i> 修改密码</h3>
        <el-form :model="pwdForm" label-width="80px" size="small" style="max-width:400px;">
          <el-form-item label="旧密码">
            <el-input v-model="pwdForm.oldPassword" type="password" placeholder="输入旧密码" show-password></el-input>
          </el-form-item>
          <el-form-item label="新密码">
            <el-input v-model="pwdForm.newPassword" type="password" placeholder="输入新密码（至少6位）" show-password></el-input>
          </el-form-item>
          <el-form-item label="确认密码">
            <el-input v-model="pwdForm.confirmPassword" type="password" placeholder="再次输入新密码" show-password></el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="warning" @click="changePassword" :loading="pwdSaving">修改密码</el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ProfileView',
  data() {
    return {
      userProfile: {},
      editForm: { nickname: '', phone: '' },
      pwdForm: { oldPassword: '', newPassword: '', confirmPassword: '' },
      stats: { totalOrders: 0, paidOrders: 0, totalSpent: '0.00', concertCount: 0 },
      profileSaving: false,
      pwdSaving: false,
      avatarUploading: false
    }
  },
  mounted() {
    this.loadProfile()
    this.loadStats()
  },
  methods: {
    async loadProfile() {
      const userId = localStorage.getItem('userId')
      if (!userId) { this.$message.error('请先登录'); this.$router.push('/auth'); return }
      try {
        const r = await this.$axios.get('/user/profile', { params: { userId }, withCredentials: true })
        if (r.data.code === 200) {
          this.userProfile = r.data.data
          this.editForm.nickname = this.userProfile.nickname || ''
          this.editForm.phone = this.userProfile.phone || ''
        }
      } catch (e) { this.$message.error('获取用户信息失败') }
    },
    async loadStats() {
      const userId = localStorage.getItem('userId')
      try {
        const r = await this.$axios.get('/order/my', { params: { userId }, withCredentials: true })
        if (r.data.code === 200) {
          const orders = r.data.data || []
          this.stats.totalOrders = orders.length
          this.stats.paidOrders = orders.filter(o => o.status === 1).length
          this.stats.totalSpent = orders.filter(o => o.status === 1).reduce((s, o) => s + (parseFloat(o.totalAmount) || 0), 0).toFixed(2)
          const concertIds = new Set(orders.filter(o => o.status === 1).map(o => o.concertId || o.sessionId))
          this.stats.concertCount = concertIds.size
        }
      } catch (e) { /* ignore */ }
    },
    async saveProfile() {
      const userId = localStorage.getItem('userId')
      this.profileSaving = true
      try {
        const r = await this.$axios.put('/user/profile/update', this.editForm, { params: { userId }, withCredentials: true })
        if (r.data.code === 200) {
          this.$message.success('修改成功')
          if (this.editForm.nickname) localStorage.setItem('userName', this.editForm.nickname)
          this.loadProfile()
        } else { this.$message.error(r.data.msg || '修改失败') }
      } catch (e) { this.$message.error('修改失败') }
      finally { this.profileSaving = false }
    },
    triggerAvatarUpload() {
      this.$refs.avatarInput.click()
    },
    async handleAvatarUpload(e) {
      const file = e.target.files[0]
      if (!file) return
      if (!file.type.startsWith('image/')) { this.$message.warning('请选择图片文件'); return }
      if (file.size > 5 * 1024 * 1024) { this.$message.warning('图片不能超过5MB'); return }
      // 立即用FileReader显示本地预览
      const reader = new FileReader()
      reader.onload = (ev) => { this.$set(this.userProfile, 'avatar', ev.target.result) }
      reader.readAsDataURL(file)
      this.avatarUploading = true
      try {
        const formData = new FormData()
        formData.append('file', file)
        const uploadRes = await this.$axios.post('/file/upload', formData, { headers: { 'Content-Type': 'multipart/form-data' }, withCredentials: true })
        if (uploadRes.data.code === 200) {
          const avatarUrl = uploadRes.data.data
          const userId = localStorage.getItem('userId')
          const updateRes = await this.$axios.put('/user/profile/update', { avatar: avatarUrl }, { params: { userId }, withCredentials: true })
          if (updateRes.data.code === 200) {
            this.$message.success('头像更新成功')
          } else { this.$message.error(updateRes.data.msg || '头像保存失败') }
        } else { this.$message.error(uploadRes.data.msg || '上传失败') }
      } catch (err) { this.$message.error('头像上传失败') }
      finally { this.avatarUploading = false; this.$refs.avatarInput.value = '' }
    },
    async changePassword() {
      if (!this.pwdForm.oldPassword || !this.pwdForm.newPassword) { this.$message.warning('请输入密码'); return }
      if (this.pwdForm.newPassword.length < 6) { this.$message.warning('新密码至少6位'); return }
      if (this.pwdForm.newPassword !== this.pwdForm.confirmPassword) { this.$message.warning('两次密码不一致'); return }
      const userId = localStorage.getItem('userId')
      this.pwdSaving = true
      try {
        const r = await this.$axios.post('/user/password/change', { oldPassword: this.pwdForm.oldPassword, newPassword: this.pwdForm.newPassword }, { params: { userId }, withCredentials: true })
        if (r.data.code === 200) {
          this.$message.success('密码修改成功，请重新登录')
          localStorage.clear()
          this.$router.push('/auth')
        } else { this.$message.error(r.data.msg || '修改失败') }
      } catch (e) { this.$message.error('修改失败') }
      finally { this.pwdSaving = false }
    }
  }
}
</script>

<style scoped>
.profile-container { min-height: 100vh; background: #F7F6F3; }
.top-nav { background: #fff; border-bottom: 1px solid #F3F4F6; }
.nav-inner { max-width: 800px; margin: 0 auto; padding: 16px 24px; display: flex; align-items: center; }
.nav-left { display: flex; align-items: center; gap: 12px; }
.back-link { display: flex; align-items: center; gap: 6px; background: none; border: none; color: #E85D3A; font-size: 14px; cursor: pointer; font-family: inherit; }
.back-link:hover { opacity: 0.7; }
.nav-divider { color: #9CA3AF; }
.nav-current { color: #6B7280; font-size: 14px; }

.profile-content { max-width: 800px; margin: 0 auto; padding: 32px 24px; }

.profile-card { background: #fff; border-radius: 16px; padding: 32px; border: 1px solid #F3F4F6; margin-bottom: 20px; }
.profile-header { display: flex; align-items: center; gap: 20px; margin-bottom: 24px; }
.avatar-wrapper { position: relative; width: 64px; height: 64px; border-radius: 50%; cursor: pointer; flex-shrink: 0; overflow: hidden; }
.avatar-wrapper:hover .avatar-overlay { opacity: 1; }
.avatar { width: 64px; height: 64px; border-radius: 50%; background: linear-gradient(135deg, #E85D3A, #FF9A76); display: flex; align-items: center; justify-content: center; color: #fff; font-size: 28px; }
.avatar-img { width: 64px; height: 64px; border-radius: 50%; object-fit: cover; }
.avatar-overlay { position: absolute; top: 0; left: 0; width: 100%; height: 100%; border-radius: 50%; background: rgba(0,0,0,0.4); display: flex; align-items: center; justify-content: center; color: #fff; font-size: 20px; opacity: 0; transition: opacity 0.2s; }
.user-name { font-size: 22px; font-weight: 700; color: #1D1D1F; }
.user-id { font-size: 13px; color: #9CA3AF; margin-top: 4px; }

.stat-row { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; }
.stat-item { text-align: center; padding: 16px; background: #F9FAFB; border-radius: 12px; }
.stat-num { display: block; font-size: 20px; font-weight: 700; color: #E85D3A; }
.stat-label { display: block; font-size: 12px; color: #9CA3AF; margin-top: 4px; }

.section-card { background: #fff; border-radius: 16px; padding: 24px 32px; border: 1px solid #F3F4F6; margin-bottom: 20px; }
.section-title { font-size: 16px; font-weight: 700; color: #1D1D1F; margin-bottom: 20px; display: flex; align-items: center; gap: 8px; }
.section-title i { color: #E85D3A; }

@media (max-width: 768px) {
  .stat-row { grid-template-columns: repeat(2, 1fr); }
  .profile-header { flex-direction: column; text-align: center; }
}
</style>
