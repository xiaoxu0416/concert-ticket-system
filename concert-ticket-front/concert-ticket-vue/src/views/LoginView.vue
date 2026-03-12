<template>
  <div class="auth-container">
    <!-- 背景装饰元素 -->
    <div class="bg-orb bg-orb-1"></div>
    <div class="bg-orb bg-orb-2"></div>
    <div class="bg-orb bg-orb-3"></div>

    <!-- 左侧品牌区域 -->
    <div class="auth-brand">
      <div class="brand-content">
        <div class="brand-icon">
          <svg width="48" height="48" viewBox="0 0 48 48" fill="none">
            <rect width="48" height="48" rx="12" fill="#E85D3A"/>
            <path d="M14 18h20v2H14zm0 5h20v2H14zm0 5h14v2H14z" fill="white" opacity="0.9"/>
            <circle cx="36" cy="30" r="4" fill="white" opacity="0.9"/>
          </svg>
        </div>
        <h1 class="brand-title">演唱会门票系统</h1>
        <p class="brand-desc">Concert Ticket Platform</p>
        <div class="brand-features">
          <div class="feature-item"><div class="feature-dot"></div><span>一键抢票，极速出票</span></div>
          <div class="feature-item"><div class="feature-dot"></div><span>安全支付，无忧保障</span></div>
          <div class="feature-item"><div class="feature-dot"></div><span>热门演出，精彩不停</span></div>
        </div>
      </div>
    </div>

    <!-- 右侧登录/注册卡片 -->
    <div class="auth-card">
      <div class="auth-card-inner">
        <div class="auth-title">
          <h2 :class="{ active: isLogin }" @click="switchToLogin">用户登录</h2>
          <h2 :class="{ active: !isLogin }" @click="switchToRegister">账号注册</h2>
        </div>

        <!-- 登录表单 -->
        <el-form
          v-show="isLogin"
          ref="loginForm"
          :model="loginForm"
          :rules="loginRules"
          class="auth-form"
          label-position="left"
          label-width="0"
        >
          <el-form-item prop="username">
            <el-input
              v-model="loginForm.username"
              placeholder="请输入用户名"
              prefix-icon="el-icon-user"
              class="auth-input"
            ></el-input>
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              prefix-icon="el-icon-lock"
              class="auth-input"
            ></el-input>
          </el-form-item>
          <el-form-item class="auth-btn-group">
            <el-button type="primary" @click="submitLogin" class="auth-btn">登 录</el-button>
          </el-form-item>
        </el-form>

        <!-- 注册表单 -->
        <el-form
          v-show="!isLogin"
          ref="registerForm"
          :model="registerForm"
          :rules="registerRules"
          class="auth-form"
          label-position="left"
          label-width="0"
        >
          <el-form-item prop="username">
            <el-input
              v-model="registerForm.username"
              placeholder="请设置用户名"
              prefix-icon="el-icon-user"
              class="auth-input"
            ></el-input>
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="registerForm.password"
              type="password"
              placeholder="请设置密码"
              prefix-icon="el-icon-lock"
              class="auth-input"
            ></el-input>
          </el-form-item>
          <el-form-item prop="confirmPassword">
            <el-input
              v-model="registerForm.confirmPassword"
              type="password"
              placeholder="请确认密码"
              prefix-icon="el-icon-lock"
              class="auth-input"
            ></el-input>
          </el-form-item>
          <el-form-item class="auth-btn-group">
            <el-button type="primary" @click="submitRegister" class="auth-btn">注 册</el-button>
          </el-form-item>
        </el-form>

        <p class="auth-footer">安全登录 · 数据加密保护</p>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'LoginView',
  data() {
    const validateConfirmPassword = (rule, value, callback) => {
      if (value !== this.registerForm.password) {
        callback(new Error('两次输入的密码不一致'))
      } else {
        callback()
      }
    }
    return {
      isLogin: true,
      loginForm: {
        username: '',
        password: ''
      },
      loginRules: {
        username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
        password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
      },
      registerForm: {
        username: '',
        password: '',
        confirmPassword: ''
      },
      registerRules: {
        username: [{ required: true, message: '请设置用户名', trigger: 'blur' }],
        password: [{ required: true, message: '请设置密码', trigger: 'blur' }],
        confirmPassword: [
          { required: true, message: '请确认密码', trigger: 'blur' },
          { validator: validateConfirmPassword, trigger: 'blur' }
        ]
      }
    }
  },
  mounted() {
    const userId = localStorage.getItem('userId')
    if (userId) {
      const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
      if (userInfo.type === 1) {
        this.$message.info('您已登录，即将跳转到管理后台')
        this.$router.push('/admin')
      } else {
        this.$message.info('您已登录，即将跳转到首页')
        this.$router.push('/home')
      }
    }
  },
  methods: {
    switchToLogin() {
      this.isLogin = true
      this.$refs.registerForm?.resetFields()
    },
    switchToRegister() {
      this.isLogin = false
      this.$refs.loginForm?.resetFields()
    },
    submitLogin() {
      this.$refs.loginForm.validate(async (valid) => {
        if (valid) {
          try {
            const res = await this.$axios.post(
              '/user/login',
              this.loginForm,
              { withCredentials: true }
            )
            if (res.data.code === 200) {
              const userId = res.data.data.userId || res.data.data.id
              const userName = res.data.data.username || res.data.data.name
              localStorage.setItem('userId', userId)
              localStorage.setItem('userName', userName)
              localStorage.setItem('userInfo', JSON.stringify(res.data.data))
              this.$message.success('登录成功！')
              const userType = res.data.data.type
              if (userType === 1) {
                this.$router.push('/admin')
              } else {
                this.$router.push('/home')
              }
            } else {
              this.$message.error(res.data.msg || '登录失败：用户名或密码错误')
            }
          } catch (error) {
            const errorMsg = error.response?.data?.msg || '登录失败：网络异常，请稍后重试'
            this.$message.error(errorMsg)
          }
        }
      })
    },
    submitRegister() {
      this.$refs.registerForm.validate(async (valid) => {
        if (valid) {
          try {
            const res = await this.$axios.post(
              '/user/register',
              {
                username: this.registerForm.username,
                password: this.registerForm.password
              },
              { withCredentials: true }
            )
            if (res.data.code === 200) {
              this.$message.success('注册成功，请登录！')
              this.switchToLogin()
              this.loginForm.username = this.registerForm.username
              this.loginForm.password = ''
              this.registerForm = {
                username: '',
                password: '',
                confirmPassword: ''
              }
            } else {
              this.$message.error(res.data.msg || '注册失败：用户名已存在')
            }
          } catch (error) {
            const errorMsg = error.response?.data?.msg || '注册失败：网络异常，请稍后重试'
            this.$message.error(errorMsg)
          }
        }
      })
    }
  }
}
</script>

<style scoped>
.auth-container {
  width: 100%;
  min-height: 100vh;
  display: flex;
  position: relative;
  overflow: hidden;
  background: #F7F6F3;
}

/* 背景装饰球体 */
.bg-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  pointer-events: none;
  z-index: 0;
}
.bg-orb-1 {
  width: 500px; height: 500px;
  background: radial-gradient(circle, rgba(232,93,58,0.12), transparent 70%);
  top: -100px; right: -100px;
}
.bg-orb-2 {
  width: 400px; height: 400px;
  background: radial-gradient(circle, rgba(99,102,241,0.08), transparent 70%);
  bottom: -80px; left: -80px;
}
.bg-orb-3 {
  width: 300px; height: 300px;
  background: radial-gradient(circle, rgba(245,158,11,0.08), transparent 70%);
  top: 50%; left: 40%;
}

/* 左侧品牌 */
.auth-brand {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px;
  position: relative;
  z-index: 1;
  background: linear-gradient(135deg, #1D1D1F 0%, #2D2B3A 100%);
  color: #fff;
}
.brand-content {
  max-width: 380px;
}
.brand-icon {
  margin-bottom: 28px;
}
.brand-title {
  font-size: 36px;
  font-weight: 900;
  letter-spacing: 2px;
  margin-bottom: 8px;
  color: #fff;
}
.brand-desc {
  font-size: 14px;
  color: rgba(255,255,255,0.45);
  letter-spacing: 3px;
  text-transform: uppercase;
  margin-bottom: 48px;
  font-weight: 300;
}
.brand-features {
  display: flex;
  flex-direction: column;
  gap: 18px;
}
.feature-item {
  display: flex;
  align-items: center;
  gap: 14px;
  font-size: 15px;
  color: rgba(255,255,255,0.7);
}
.feature-dot {
  width: 8px; height: 8px;
  border-radius: 50%;
  background: #E85D3A;
  box-shadow: 0 0 12px rgba(232,93,58,0.5);
  flex-shrink: 0;
}

/* 右侧登录卡片 */
.auth-card {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px 40px;
  position: relative;
  z-index: 1;
  background: #fff;
}
.auth-card-inner {
  width: 100%;
  max-width: 400px;
}

/* Tab 切换 */
.auth-title {
  display: flex;
  gap: 0;
  margin-bottom: 36px;
  border-bottom: 2px solid #F3F4F6;
}
.auth-title h2 {
  font-size: 18px;
  font-weight: 500;
  color: #9CA3AF;
  padding: 0 20px 14px;
  cursor: pointer;
  position: relative;
  transition: all 0.3s ease;
  user-select: none;
}
.auth-title h2:hover {
  color: #6B7280;
}
.auth-title h2.active {
  color: #E85D3A;
  font-weight: 600;
}
.auth-title h2.active::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 20px;
  right: 20px;
  height: 2px;
  background: #E85D3A;
  border-radius: 2px;
}

/* 表单 */
.auth-form {
  width: 100%;
}
.auth-form >>> .el-form-item {
  margin-bottom: 22px;
}
.auth-input >>> .el-input__inner {
  height: 48px;
  line-height: 48px;
  background: #F7F6F3;
  border: 1.5px solid #E5E7EB;
  border-radius: 10px;
  font-size: 15px;
  color: #1D1D1F;
  transition: all 0.3s ease;
}
.auth-input >>> .el-input__inner:focus {
  border-color: #E85D3A;
  background: #fff;
  box-shadow: 0 0 0 4px rgba(232,93,58,0.08);
}
.auth-input >>> .el-input__prefix {
  color: #9CA3AF;
  font-size: 16px;
}

/* 提交按钮 */
.auth-btn-group {
  margin-top: 8px;
}
.auth-btn {
  width: 100%;
  height: 50px;
  border-radius: 12px !important;
  background: #E85D3A !important;
  border: none !important;
  font-size: 16px;
  font-weight: 600;
  color: #fff !important;
  letter-spacing: 4px;
  transition: all 0.3s ease;
  cursor: pointer;
}
.auth-btn:hover {
  background: #FF7F5C !important;
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(232,93,58,0.35);
}

/* 底部文字 */
.auth-footer {
  text-align: center;
  margin-top: 32px;
  font-size: 13px;
  color: #9CA3AF;
  letter-spacing: 1px;
}

@media (max-width: 900px) {
  .auth-container { flex-direction: column; }
  .auth-brand { padding: 40px 30px; }
  .brand-title { font-size: 28px; }
  .brand-features { display: none; }
  .auth-card { padding: 40px 24px; }
}
@media (max-width: 480px) {
  .auth-brand { padding: 30px 20px; }
  .brand-desc { margin-bottom: 0; }
}
</style>
