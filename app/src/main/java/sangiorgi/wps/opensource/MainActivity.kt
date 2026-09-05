package sangiorgi.wps.opensource

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import sangiorgi.wps.opensource.domain.models.WifiNetwork
import sangiorgi.wps.opensource.ui.motion.ExpressiveMotion
import sangiorgi.wps.opensource.ui.navigation.NavRoute
import sangiorgi.wps.opensource.ui.scanner.WifiScannerViewModel
import sangiorgi.wps.opensource.ui.screens.*
import sangiorgi.wps.opensource.ui.theme.WIFIWPSWPATESTEROPENSOURCETheme
import sangiorgi.wps.opensource.ui.viewmodels.PermissionViewModel
import sangiorgi.wps.opensource.ui.viewmodels.WpsConnectionViewModel
import sangiorgi.wps.opensource.utils.RootChecker

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @javax.inject.Inject
    lateinit var rootChecker: RootChecker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request root access early on a background thread using lifecycleScope
        lifecycleScope.launch(Dispatchers.IO) {
            rootChecker.requestRootAccess()
        }

        setContent {
            WIFIWPSWPATESTEROPENSOURCETheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    MainScreen(rootChecker = rootChecker)
                }
            }
        }
    }
}

@Composable
fun MainScreen(rootChecker: RootChecker) {
    val context = LocalContext.current
    val activity = context as? Activity

    // Observe initialization state from Application
    val initState by WpsApplication.initializationState.collectAsStateWithLifecycle()

    // Permission ViewModel
    val permissionViewModel: PermissionViewModel = hiltViewModel()
    val permissionState by permissionViewModel.permissionState.collectAsStateWithLifecycle()

    val navController = rememberNavController()

    // State for selected network. Use rememberSaveable so the selection survives configuration
    // changes and process death; otherwise the ConnectionProgress screen renders blank on restore.
    var selectedNetwork by rememberSaveable { mutableStateOf<WifiNetwork?>(null) }
    var selectedMethod by rememberSaveable { mutableStateOf<ConnectionMethod?>(null) }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissions ->
        permissionViewModel.onPermissionsResult(permissions, activity)
    }

    // Handle permission events
    LaunchedEffect(permissionState.event) {
        when (permissionState.event) {
            is PermissionViewModel.PermissionEvent.PermissionsGranted -> {
                Toast.makeText(
                    context,
                    context.getString(R.string.toast_permissions_granted),
                    Toast.LENGTH_SHORT,
                ).show()
                permissionViewModel.consumeEvent()
            }
            is PermissionViewModel.PermissionEvent.LocationDisabled -> {
                Toast.makeText(
                    context,
                    context.getString(R.string.toast_enable_location_services),
                    Toast.LENGTH_LONG,
                ).show()
                context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                permissionViewModel.consumeEvent()
            }
            is PermissionViewModel.PermissionEvent.PermanentlyDenied -> {
                Toast.makeText(
                    context,
                    context.getString(R.string.toast_grant_permissions_from_settings),
                    Toast.LENGTH_LONG,
                ).show()
                permissionViewModel.consumeEvent()
            }
            is PermissionViewModel.PermissionEvent.SomeDenied -> {
                Toast.makeText(
                    context,
                    context.getString(R.string.toast_some_permissions_denied),
                    Toast.LENGTH_LONG,
                ).show()
                permissionViewModel.consumeEvent()
            }
            null -> { /* No event to handle */ }
        }
    }

    // Re-check permissions when returning from settings
    LaunchedEffect(Unit) {
        permissionViewModel.checkPermissions()
    }

    // Show appropriate screen based on initialization and permission status.
    // AnimatedContent gives initialization changes an expressive fade-through
    // (springy scale + fade) instead of an abrupt swap.
    AnimatedContent(
        targetState = initState,
        transitionSpec = {
            ExpressiveMotion.swapIn() togetherWith ExpressiveMotion.swapOut()
        },
        label = "initialization",
    ) { state ->
        when (state) {
            is WpsApplication.InitializationState.Loading -> {
                // Show loading screen while initializing
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            is WpsApplication.InitializationState.Failed -> {
                // Show error screen
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Failed to initialize: ${state.error}",
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }

            is WpsApplication.InitializationState.Ready -> {
                // App initialized, check permissions. The gate between the permission
                // screen and the nav graph slides directionally: forward when access
                // is granted, mirrored when it is revoked.
                AnimatedContent(
                    targetState = permissionState.isReady,
                    transitionSpec = {
                        if (targetState) {
                            ExpressiveMotion.enterPush() togetherWith ExpressiveMotion.exitPush()
                        } else {
                            ExpressiveMotion.popEnter() togetherWith ExpressiveMotion.popExit()
                        }
                    },
                    label = "permission-gate",
                ) { ready ->
                    if (ready) {
                        // Permissions granted, show navigation with type-safe routes
                        NavHost(
                            navController = navController,
                            startDestination = NavRoute.Scanner,
                            enterTransition = { ExpressiveMotion.enterPush() },
                            exitTransition = { ExpressiveMotion.exitPush() },
                            popEnterTransition = { ExpressiveMotion.popEnter() },
                            popExitTransition = { ExpressiveMotion.popExit() },
                        ) {
                            composable<NavRoute.Scanner> {
                                val viewModel: WifiScannerViewModel = hiltViewModel()

                                WifiScannerScreen(
                                    viewModel = viewModel,
                                    onNetworkSelected = { network ->
                                        selectedNetwork = network
                                        navController.navigate(NavRoute.NetworkDetail)
                                    },
                                )
                            }

                            composable<NavRoute.NetworkDetail>(
                                // The detail screen recedes in place when a task screen
                                // rises over it, and settles back when the task pops away.
                                exitTransition = { ExpressiveMotion.exitReceive() },
                                popEnterTransition = { ExpressiveMotion.popEnterReceive() },
                            ) {
                                selectedNetwork?.let { network ->
                                    NetworkDetailScreen(
                                        network = network,
                                        rootChecker = rootChecker,
                                        onBackClick = {
                                            navController.popBackStack()
                                        },
                                        onConnectionMethodSelected = { method ->
                                            selectedMethod = method
                                            navController.navigate(NavRoute.ConnectionProgress)
                                        },
                                    )
                                }
                            }

                            composable<NavRoute.ConnectionProgress>(
                                // Task screens rise like a sheet: springy vertical slide
                                // + scale settle, and they sink back down when dismissed.
                                enterTransition = { ExpressiveMotion.enterRise() },
                                popExitTransition = { ExpressiveMotion.popExitRise() },
                            ) {
                                val connectionViewModel: WpsConnectionViewModel = hiltViewModel()
                                val connectionState by connectionViewModel.connectionState
                                    .collectAsStateWithLifecycle()

                                selectedNetwork?.let { network ->
                                    selectedMethod?.let { method ->
                                        LaunchedEffect(Unit) {
                                            connectionViewModel.startConnection(network, method)
                                        }

                                        ConnectionProgressScreen(
                                            network = network,
                                            connectionMethod = method,
                                            connectionState = connectionState,
                                            onCancel = {
                                                // Stop the running attempt but stay on screen so the
                                                // user sees the cancelled result (Close/Retry).
                                                connectionViewModel.cancelConnection()
                                            },
                                            onClose = {
                                                navController.popBackStack()
                                            },
                                            onRetry = {
                                                connectionViewModel.startConnection(network, method)
                                            },
                                            onDone = {
                                                navController.popBackStack<NavRoute.Scanner>(inclusive = false)
                                            },
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // Permissions not granted, show permission screen
                        PermissionScreen(
                            onPermissionsGranted = {
                                permissionViewModel.onPermissionsGrantedManually()
                            },
                            onRequestPermissions = { permissions ->
                                permissionLauncher.launch(permissions.toTypedArray())
                            },
                            onOpenSettings = {
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.fromParts("package", context.packageName, null)
                                }
                                context.startActivity(intent)
                            },
                        )
                    }
                }
            }
        }
    }
}
