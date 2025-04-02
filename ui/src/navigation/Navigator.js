import router from "@/router.js";

export const Navigator = {
    navigateToHome() {
        return router.push("/")
    },

    navigateToConfiguration(name) {
        console.log("Navigator navigateToConfiguration", name);
        return router.push(`/configurations/${name}`)
    },
    navigateToModules(configuration) {
        if (!router.currentRoute.value.path.endsWith('modules')) {
            return router.push(`/configurations/${configuration}/modules`);
        }
    },
    navigateToDependencies(configuration) {
        if (!router.currentRoute.value.path.endsWith('dependencies')) {
            return router.push(`/configurations/${configuration}/dependencies`);
        }
    },
    navigateToConflicts(configuration) {
        if (!router.currentRoute.value.path.endsWith('conflicts')) {
            return router.push(`/configurations/${configuration}/conflicts`);
        }
    },

    // Dependencies
    navigateToDependency(configuration, dependency) {
        console.log("Navigator navigateToDependency", configuration, dependency);
        return router.push(`/configurations/${configuration}/dependencies/${dependency}`);
    },
    navigateToUsage(configuration, dependency) {
        if (!router.currentRoute.value.path.endsWith('usage')) {
            return router.push(`/configurations/${configuration}/dependencies/${dependency}/usage`);
        }
    },
    navigateToGraph(configuration, dependency) {
        if (!router.currentRoute.value.path.endsWith('graph')) {
            return router.push(`/configurations/${configuration}/dependencies/${dependency}/graph`);
        }
    },
    navigateToSubDependencies(configuration, dependency) {
        if (!router.currentRoute.value.path.endsWith('dependencies')) {
            return router.push(`/configurations/${configuration}/dependencies/${dependency}/dependencies`);
        }
    },

    back() {
        return router.back();
    }
}