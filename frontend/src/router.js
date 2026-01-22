import { createRouter, createWebHistory } from 'vue-router';

import Home from '@/views/Home.vue';
import NewReleasesPage from '@/views/NewReleasesPage.vue';
import DramaPage from '@/views/DramaPage.vue';
import MoviePage from '@/views/MoviePage.vue';
import ActorPage from '@/views/ActorPage.vue';
import ImportDramaPage from '@/views/ImportDramaPage.vue';
import UpdateDramaPage from '@/views/UpdateDramaPage.vue';
import ImportMoviePage from '@/views/ImportMoviePage.vue';
import UpdateMoviePage from '@/views/UpdateMoviePage.vue';
import ImportActorPage from '@/views/ImportActorPage.vue';
import UpdateActorPage from '@/views/UpdateActorPage.vue';
import PlatformInfoPage from '@/views/PlatformInfoPage.vue';
import ContactUsPage from '@/views/ContactUsPage.vue';
import ExternalLinksPage from '@/views/ExternalLinksPage.vue';

const routes = [
  {
    // 首頁
    path: '/',
    name: 'Home',
    component: Home
  },
  {
    // 新上架
    path: '/new-releases',
    name: 'NewReleasesPage',
    component: NewReleasesPage
  },
  {
    // 熱門韓劇
    path: '/k-dramas',
    name: 'DramaPage',
    component: DramaPage
  },
  {
    // 精選韓影
    path: '/k-movies',
    name: 'MoviePage',
    component: MoviePage
  },
  {
    // 韓國演員一覽
    path: '/k-actors',
    name: 'ActorPage',
    component: ActorPage
  },
  {
    path: '/import-k-drama',
    name: 'ImportDramaPage',
    component: ImportDramaPage
  },
  {
    path: '/update-k-drama',
    name: 'UpdateDramaPage',
    component: UpdateDramaPage
  },
  {
    path: '/import-k-movie',
    name: 'ImportMoviePage',
    component: ImportMoviePage
  },
  {
    path: '/update-k-movie',
    name: 'UpdateMoviePage',
    component: UpdateMoviePage
  },
  {
    path: '/import-k-actor',
    name: 'ImportActorPage',
    component: ImportActorPage
  },
  {
    path: '/update-k-actor',
    name: 'UpdateActorPage',
    component: UpdateActorPage
  },
  {
    // 影音平台整理
    path: '/platforms',
    name: 'PlatformInfoPage',
    component: PlatformInfoPage
  },
  {
    // 聯絡我們
    path: '/contact-us',
    name: 'ContactUsPage',
    component: ContactUsPage
  },
  {
    // 外部連結
    path: '/external-links',
    name: 'ExternalLinksPage',
    component: ExternalLinksPage
  },
  
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

export default router;
