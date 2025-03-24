import {createMemoryHistory, createRouter, createWebHashHistory, createWebHistory} from 'vue-router'
import ConfigurationScreen from "@/screens/ConfigurationScreen.vue";
import HomeScreen from "@/screens/HomeScreen.vue";
import DependencyScreen from "@/screens/DependencyScreen.vue";
import ConflictsScreen from "@/screens/configuration/ConflictsScreen.vue";
import DependenciesScreen from "@/screens/configuration/DependenciesScreen.vue";
import NotFoundScreen from "@/screens/NotFoundScreen.vue";
import DependencyUsageScreen from "@/screens/dependency/DependencyUsageScreen.vue";
import DependencyGraphScreen from "@/screens/dependency/DependencyGraphScreen.vue";
import DependencyDependenciesScreen from "@/screens/dependency/DependencyDependenciesScreen.vue";


const routes = [
    {path: '', component: HomeScreen},
    {path: '/configurations', redirect: '/'},
    {
        path: '/configurations/:configuration',
        component: ConfigurationScreen,
        redirect: (to) => `/configurations/${to.params.configuration}/conflicts`,
        children: [
            {
                path: 'conflicts',
                component: ConflictsScreen,
            },
            {
                path: 'dependencies',
                component: DependenciesScreen,
            }
        ]
    },
    {
        path: '/configurations/:configuration/dependencies/:dependency',
        component: DependencyScreen,
        redirect: (to) => {
            return `/configurations/${to.params.configuration}/dependencies/${to.params.dependency}/usage`
        },
        children: [
            {
                path: 'usage',
                component: DependencyUsageScreen,
            },
            {
                path: 'graph',
                component: DependencyGraphScreen,
            }, {
                path: 'dependencies',
                component: DependencyDependenciesScreen,
            }
        ]
    },
    {path: '/:pathMatch(.*)*', component: NotFoundScreen}
]

const router = createRouter({
    history: createWebHashHistory(),
    routes,
})

export default router