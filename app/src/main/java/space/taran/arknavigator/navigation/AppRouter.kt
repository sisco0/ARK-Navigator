package space.taran.arknavigator.navigation

import ru.terrakok.cicerone.Router
import ru.terrakok.cicerone.Screen

class AppRouter: Router() {
    fun navigateToFragmentUsingAdd(screen: Screen) {
        executeCommands(FragmentForwardAdd(screen))
    }
}