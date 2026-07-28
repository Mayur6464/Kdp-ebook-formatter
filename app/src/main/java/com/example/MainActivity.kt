package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.*
import com.example.ui.theme.KdpFormatterTheme
import com.example.ui.viewmodel.BookViewModel

object KdpRoutes {
    const val DASHBOARD = "dashboard"
    const val EDITOR = "editor"
    const val PROOF = "proof"
    const val CHECKLIST = "checklist"
    const val EXPORT = "export"
    const val COVER = "cover"
    const val MONETIZATION = "monetization"
    const val REFERRAL = "referral"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KdpFormatterTheme {
                val navController = rememberNavController()
                val viewModel: BookViewModel = viewModel()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = KdpRoutes.DASHBOARD,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(KdpRoutes.DASHBOARD) {
                            DashboardScreen(
                                viewModel = viewModel,
                                onNavigateToEditor = { navController.navigate(KdpRoutes.EDITOR) },
                                onNavigateToProof = { navController.navigate(KdpRoutes.PROOF) },
                                onNavigateToChecklist = { navController.navigate(KdpRoutes.CHECKLIST) },
                                onNavigateToExport = { navController.navigate(KdpRoutes.EXPORT) },
                                onNavigateToCover = { navController.navigate(KdpRoutes.COVER) },
                                onNavigateToMonetization = { navController.navigate(KdpRoutes.MONETIZATION) },
                                onNavigateToReferral = { navController.navigate(KdpRoutes.REFERRAL) }
                            )
                        }

                        composable(KdpRoutes.REFERRAL) {
                            ReferralDashboardScreen(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable(KdpRoutes.MONETIZATION) {
                            KdpMonetizationStudioScreen(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable(KdpRoutes.COVER) {
                            KdpCoverStudioScreen(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable(KdpRoutes.EDITOR) {
                            EditorScreen(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable(KdpRoutes.PROOF) {
                            ProofPreviewScreen(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable(KdpRoutes.CHECKLIST) {
                            ChecklistScreen(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable(KdpRoutes.EXPORT) {
                            ExportScreen(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
