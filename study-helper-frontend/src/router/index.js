import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/Home.vue')
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/UnifiedRegister.vue')
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('../views/Profile.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/courses',
    name: 'Courses',
    redirect: '/student/courses'
  },
  {
    path: '/course/create',
    name: 'CourseCreate',
    component: () => import('../views/CourseForm.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/course/edit/:id',
    name: 'CourseEdit',
    component: () => import('../views/CourseForm.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/tasks',
    name: 'Tasks',
    redirect: '/student/courses'
  },
  {
    path: '/task/create',
    name: 'TaskCreate',
    component: () => import('../views/TaskForm.vue'),
    meta: { requiresAuth: true, requiresTeacher: true }
  },
  {
    path: '/task/edit/:id',
    name: 'TaskEdit',
    component: () => import('../views/TaskForm.vue'),
    meta: { requiresAuth: true, requiresTeacher: true }
  },
  {
    path: '/materials',
    name: 'Materials',
    redirect: '/student/materials'
  },
  {
    path: '/material/upload',
    name: 'MaterialUpload',
    component: () => import('../views/MaterialUpload.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/material/:id',
    name: 'MaterialDetail',
    component: () => import('../views/MaterialDetail.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/quizzes',
    name: 'Quizzes',
    redirect: '/student/courses'
  },
  {
    path: '/quiz/create',
    name: 'QuizCreate',
    component: () => import('../views/QuizCreate.vue'),
    meta: { requiresAuth: true, requiresTeacher: true }
  },
  {
    path: '/quiz/:id',
    name: 'QuizDetail',
    component: () => import('../views/QuizDetail.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/quiz/:id/take',
    name: 'QuizTake',
    component: () => import('../views/QuizTake.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/quiz/result/:recordId',
    name: 'QuizResult',
    component: () => import('../views/QuizResult.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/wrong-book',
    name: 'WrongBook',
    component: () => import('../views/WrongBook.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/statistics',
    name: 'Statistics',
    redirect: '/student/statistics'
  },
  {
    path: '/messages',
    name: 'Messages',
    component: () => import('../views/Messages.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/settings',
    name: 'Settings',
    component: () => import('../views/Settings.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/student',
    component: () => import('../views/StudentLayout.vue'),
    meta: { requiresAuth: true, requiresStudent: true },
    children: [
      {
        path: '',
        redirect: '/student/courses'
      },
      {
        path: 'courses',
        name: 'StudentCourses',
        component: () => import('../views/Courses.vue')
      },
      {
        path: 'course/:id',
        name: 'StudentCourseDetail',
        component: () => import('../views/StudentCourseDetail.vue')
      },
      {
        path: 'materials',
        name: 'StudentMaterials',
        component: () => import('../views/Materials.vue')
      },
      {
        path: 'statistics',
        name: 'StudentStatistics',
        component: () => import('../views/Statistics.vue')
      }
    ]
  },
  // 管理员路由
  {
    path: '/admin',
    component: () => import('../views/AdminLayout.vue'),
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      {
        path: '',
        redirect: '/admin/dashboard'
      },
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('../views/AdminDashboard.vue')
      },
      {
        path: 'users',
        name: 'AdminUsers',
        component: () => import('../views/AdminUsers.vue')
      },
      {
        path: 'content',
        name: 'AdminContent',
        component: () => import('../views/AdminContent.vue')
      },
      {
        path: 'invitations',
        name: 'AdminInvitations',
        component: () => import('../views/AdminInvitations.vue')
      }
    ]
  },
  // 教师路由
  {
    path: '/teacher',
    component: () => import('../views/TeacherLayout.vue'),
    meta: { requiresAuth: true, requiresTeacher: true },
    children: [
      {
        path: '',
        redirect: '/teacher/dashboard'
      },
      {
        path: 'dashboard',
        name: 'TeacherDashboard',
        component: () => import('../views/TeacherDashboard.vue')
      },
      {
        path: 'courses',
        name: 'TeacherCourses',
        component: () => import('../views/TeacherCourses.vue')
      },
      {
        path: 'tasks',
        name: 'TeacherTasks',
        component: () => import('../views/TeacherTasks.vue')
      },
      {
        path: 'materials',
        name: 'TeacherMaterials',
        component: () => import('../views/TeacherMaterials.vue')
      },
      {
        path: 'quizzes',
        name: 'TeacherQuizzes',
        component: () => import('../views/TeacherQuizzes.vue')
      },
      {
        path: 'course/create',
        name: 'TeacherCourseCreate',
        component: () => import('../views/CourseForm.vue')
      },
      {
        path: 'course/edit/:id',
        name: 'TeacherCourseEdit',
        component: () => import('../views/CourseForm.vue')
      },
      {
        path: 'course/:id/overview',
        name: 'TeacherCourseDetail',
        component: () => import('../views/TeacherCourseDetail.vue')
      },
      {
        path: 'task/create',
        name: 'TeacherTaskCreate',
        component: () => import('../views/TaskForm.vue')
      },
      {
        path: 'task/edit/:id',
        name: 'TeacherTaskEdit',
        component: () => import('../views/TaskForm.vue')
      },
      {
        path: 'quiz/create',
        name: 'TeacherQuizCreate',
        component: () => import('../views/QuizCreate.vue')
      },
      {
        path: 'material/upload',
        name: 'TeacherMaterialUpload',
        component: () => import('../views/MaterialUpload.vue')
      }
    ]
  },

  // 模拟教师登录（用于测试）
  {
    path: '/mock-teacher-login',
    name: 'MockTeacherLogin',
    component: () => import('../views/MockTeacherLogin.vue')
  },
  // 模拟管理员登录（用于测试）
  {
    path: '/mock-admin-login',
    name: 'MockAdminLogin',
    component: () => import('../views/MockAdminLogin.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  
  console.log('路由守卫检查:', {
    to: to.path,
    requiresAuth: to.meta.requiresAuth,
    requiresAdmin: to.meta.requiresAdmin,
    isLoggedIn: userStore.isLoggedIn,
    hasToken: !!userStore.token,
    hasUser: !!userStore.user,
    userRole: userStore.user?.role
  })
  
  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    console.log('未登录，重定向到登录页')
    next('/login')
  } else if (to.meta.requiresAdmin && userStore.user?.role !== 'ADMIN') {
    console.log('非管理员用户，重定向到首页')
    next('/')
  } else if (to.meta.requiresTeacher && userStore.user?.role !== 'TEACHER') {
    console.log('非教师用户，重定向到首页')
    next('/')
  } else if (to.meta.requiresStudent && userStore.user?.role !== 'STUDENT') {
    console.log('非学生用户，重定向到首页')
    next('/')
  } else if (to.path === '/login' && userStore.isLoggedIn) {
    console.log('已登录，根据角色重定向')
    // 根据用户角色跳转到相应页面
    if (userStore.isAdmin) {
      next('/admin')
    } else if (userStore.user?.role === 'TEACHER') {
      next('/teacher')
    } else {
      next('/')
    }
  } else {
    next()
  }
})

export default router
