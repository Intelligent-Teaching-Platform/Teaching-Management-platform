import { createRouter, createWebHashHistory } from 'vue-router'

const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    {
      path: '/',
      component: () => import('@/views/Manager.vue'),
      redirect: '/home',
      children: [
        { path: 'person', name: 'Person', component: () => import('@/views/manager/Person.vue')},
        { path: 'tPerson', name: 'TPerson', component: () => import('@/views/manager/TPerson.vue')},
        { path: 'sPerson', name: 'SPerson', component: () => import('@/views/manager/SPerson.vue')},
        { path: 'password', name: 'Password', component: () => import('@/views/manager/Password.vue')},
        { path: 'account', name: 'Account', component: () => import('@/views/manager/account.vue')},
        { path: 'resource', name: 'Resource', component: () => import('@/views/manager/resource.vue') },
        { path: 'home', name: 'Home', component: () => import('@/views/manager/Home.vue')},
        { path: 'dashboard', name: 'Dashboard', component: () => import('@/views/manager/Dashboard.vue'), meta: { title: '总览驾驶舱' } },
        { path: 'admin', name: 'Admin', component: () => import('@/views/manager/Admin.vue')},
        { path: 'clazz', name: 'Clazz', component: () => import('@/views/manager/Clazz.vue')},
        { path: 'teacher', name: 'Teacher', component: () => import('@/views/manager/Teacher.vue')},
        { path: 'student', name: 'Student', component: () => import('@/views/manager/Student.vue')},
        { path: 'notice', name: 'Notice', component: () => import('@/views/manager/Notice.vue')},
        { path: 'college', name: 'College', component: () => import('@/views/manager/College.vue')},
        { path: 'speciality', name: 'Speciality', component: () => import('@/views/manager/Speciality.vue')},
        { path: 'course', name: 'Course', component: () => import('@/views/manager/Course.vue')},
        { path: 'test', name: 'Test', component: () => import('@/views/manager/Test.vue')},
        { path: 'course/courseDetail',
          component: () => import('@/views/manager/CourseDetail.vue'),
          children: [
            { path: 'ai', component: () => import('@/views/courselist/ai.vue') },
            { path: 'chapter', component: () => import('@/views/courselist/chapter.vue') },
            { path: 'class', component: () => import('@/views/courselist/class.vue') },
            {
              path: 'homework',
              component: () => import('@/views/courselist/homework.vue'),
              redirect: '/course/courseDetail/homework/courseware',
              children: [
                { path: 'courseware', component: () => import('@/views/courselist/courseware.vue') },
                { path: 'work', component: () => import('@/views/courselist/Work.vue') },
              ],
            },
            { path: 'courseware', component: () => import('@/views/courselist/courseware.vue') },
            { path: 'material', component: () => import('@/views/courselist/material.vue') },
            { path: 'export', component: () => import('@/views/courselist/Export.vue') },
            { path: 'document', component: () => import('@/views/courselist/document.vue') },
            { path: 'test', component: () => import('@/views/courselist/test.vue') },
            { path: 'task', component: () => import('@/views/courselist/task.vue') },
            { path: 'taskAnalysis', component: () => import('@/views/courselist/TaskAnalysis.vue') },
            { path: 'teachPlan', component: () => import('@/views/courselist/teachplan.vue') },
            { path: 'work', component: () => import('@/views/courselist/Work.vue') },
            { path: 'questionType', component: () => import('@/views/courselist/QuestionType.vue') },
            { path: 'question', component: () => import('@/views/courselist/question.vue') },
            { path: 'testPaper', component: () => import('@/views/courselist/TestPaper.vue') },
            { path: 'exam', component: () => import('@/views/courselist/Exam.vue') },
            { path: 'score', component: () => import('@/views/courselist/Score.vue') },
            { path: 'scoreView', component: () => import('@/views/courselist/ScoreView.vue') },
            { path: 'paper', name: 'Paper', component: () => import('@/views/courselist/Paper.vue'), props: true },
            { path: 'testPaperView', name: 'testPaper', component: () => import('@/views/courselist/TestPaperView.vue'), props: true },
          ]},
        { path: 'choice', name: 'Choice', component: () => import('@/views/manager/Choice.vue')},


        { path: 'courseSidebar',component: () => import('@/views/manager/CourseSidebar.vue')},
        { path: 'defaultSidebar',component: () => import('@/views/manager/DefaultSidebar.vue')},
        { path: 'StuCourse', name: 'StuCourse', component:  () => import('@/views/manager/StuCourse.vue') },

        { path: 'myCourse', name: 'MyCourse', component:  () => import('@/views/manager/MyCourse.vue') },
        { path: 'myTeach', name: 'MyTeach', component:  () => import('@/views/manager/MyTeach.vue') },
          ]
    },
    {
      path:'/detail/lessonDetail',
      name:'lessonDetail',
      component: () => import('@/views/detail/lessonDetail.vue'),
    },
    {
      path:'/detail/document',
      name:'document',
      component: () => import('@/views/detail/document.vue'),
    },
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/Login.vue'),
      meta: {
        title: '登录'
      }
    },
    {
      path: '/register',
      name: 'Register',
      component: () => import('@/views/Register.vue'),
      meta: {
        title: '注册'
      }
    },
    {
      path: '/:pathMatch(.*)',
      name: 'NotFound',
      component: () => import('@/views/NotFound.vue'),
      meta: {
        title: '页面不存在'
      }
    }
  ]
})

// 路由导航守卫（可选）
router.beforeEach((to, from, next) => {
  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - 教学管理系统`
  }
  next()
})

export default router