import { createRouter, createWebHistory } from "vue-router";
// 引入vue页面
import Login from "@/views/Login.vue";
import Profile from "@/views/Profile.vue";
import User from "@/views/profile/User.vue";
import Admin from "@/views/profile/Admin.vue";
import Traffic from "@/views/data/Traffic.vue";
import Stations from "@/views/data/Stations.vue";
import PointDemand from "@/views/data/PointDemand.vue";
import ODDemand from "@/views/data/ODDemand.vue";
import PotentialSite from "@/views/data/PotentialSite.vue";
import PowerGrid from "@/views/data/PowerGrid.vue";
import Test from "@/views/Test.vue";
import TrafficFlow from "@/views/data/TrafficFlow.vue";
import PowerFlow from "@/views/data/PowerFlow.vue";
import ControlHome from "@/views/control/ControlHome.vue";
import EditTrafficFlow from "../views/Editor/EditTrafficFlow.vue";
import Settings from "@/views/RealSettings.vue";
import EditFlowSpeed from "@/views/Editor/EditFlowSpeed.vue";
import EditDefaultRoute from "@/views/Editor/EditDefaultRoute.vue";
import EditUserRoute from "@/views/Editor/EditUserRoute.vue";
import EditSetTrafficFlow from "@/views/Editor/EditSetTrafficFlow.vue";
import StartEnd from "@/views/data/StartEnd.vue";
import ControlFLP from "@/views/control/ControlFLP.vue";
import EditFlp from "@/views/Editor/EditFlp.vue";
import EditCoveredDemands from "@/views/Editor/EditCoveredDemands.vue";
import RealDemand from "@/views/data/RealDemand.vue";
import PassingCar from "@/views/data/PassingCar.vue";
import EditDistribution from "@/views/Editor/EditDistribution.vue";
import ControlRoute from "@/views/control/ControlRoute.vue";

const routes = [{
    path: "/test",
    name: "test",
    component: Test,
}, {
    path: "/",
    name: "login",
    component: Login,
}, {
    path: "/profile",
    name: "profile",
    component: Profile,
    children: [{
        path: "user",
        name: "user",
        component: User,
    }, {
        path: "admin",
        name: "admin",
        component: Admin,
    }]
}, {
    path: "/data",
    name: "data",
    redirect: "/data/index",
    children: [{
        path: "index",
        name: "index",
        component: Settings,
    }, {
        path: "traffic",
        name: "traffic",
        component: Traffic
    }, {
        path: "station",
        name: "station",
        component: Stations,
    }, {
        path: "pointDemand",
        name: "pointDemand",
        component: PointDemand,
    }, {
        path: "od-pairs",
        name: "od-pairs",
        component: ODDemand
    }, {
        path: "sites",
        name: "sites",
        component: PotentialSite
    }, {
        path: "power-grid",
        name: "power-grid",
        component: PowerGrid
    }, {
        path: "traffic-flow",
        name: "traffic-flow",
        component: TrafficFlow
    }, {
        path: "power-flow",
        name: "power-flow",
        component: PowerFlow
    }, {
        path: "real-demands",
        name: 'real-demands',
        component: RealDemand,
    }, {
        path: "passing",
        name: "passing",
        component: PassingCar
    }]
}, {
    path: "/control",
    name: "control",
    redirect: "/control/index",
    children: [{
        path: "index",
        name: "ctrl-index",
        component: ControlHome,
    }, {
        path: "/control/flp",
        name: "ctrl-flp",
        component: ControlFLP
    }, {
        path: "/control/route",
        name: "ctrl-route",
        component: ControlRoute,
    }]
}, {
    path: "/program",
    name: "program",
    redirect: "/program/traffic-flow",
    children: [{
        path: "flow-speed",
        name: "flow-speed",
        component: EditFlowSpeed
    }, {
        path: "traffic-flow",
        name: "program-traffic-flow",
        component: EditTrafficFlow
    }, {
        path: "code-route",
        name: "default-routes",
        component: EditDefaultRoute
    }, {
        path: "user-routes",
        name: "user-routes",
        component: EditUserRoute
    }, {
        path: "set-traffic-flow",
        name: "set-traffic-flow",
        component: EditSetTrafficFlow
    }, {
        path: "code-flp",
        name: "code-flp",
        component: EditFlp
    }, {
        path: "covered-demands",
        name: "covered-demands",
        component: EditCoveredDemands
    }, {
        path: "set-distribution",
        name: "set-distribution",
        component: EditDistribution
    }]
}]

const router = createRouter({
    history: createWebHistory(),
    routes: routes,
})

export default router;
