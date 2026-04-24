package com.example.camscanner.presentation.navigation

import android.graphics.Bitmap
import android.graphics.PointF
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.camscanner.presentation.camera.CameraScreen
import com.example.camscanner.presentation.detection.DetectionScreen
import com.example.camscanner.presentation.document.DocumentScreen
import com.example.camscanner.presentation.edit.EditScreen
import com.example.camscanner.presentation.export.ExportScreen
import com.example.camscanner.presentation.home.HomeScreen
import com.example.camscanner.presentation.settings.SettingsScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Camera : Screen("camera/{documentId}") {
        fun createRoute(documentId: String = "new") = "camera/$documentId"
    }
    object Detection : Screen("detection/{imageUri}/{documentId}") {
        fun createRoute(imageUri: String, documentId: String = "new") =
            "detection/${Uri.encode(imageUri)}/$documentId"
    }
    object Edit : Screen("edit/{documentId}/{pageId}") {
        fun createRoute(documentId: String = "new", pageId: String = "new") =
            "edit/$documentId/$pageId"
    }
    object Document : Screen("document/{documentId}") {
        fun createRoute(documentId: String) = "document/$documentId"
    }
    object Export : Screen("export/{documentId}") {
        fun createRoute(documentId: String) = "export/$documentId"
    }
    object Settings : Screen("settings")
}

/** In-memory state for passing bitmaps between screens (too large for nav args) */
object ScanSession {
    var capturedImageUri: String? = null
    var sourceBitmap: Bitmap? = null
    var corners: List<PointF> = emptyList()
    var pendingImportPaths: MutableList<String> = mutableListOf()  // Req 2.3: multi-import queue

    fun clear() {
        sourceBitmap?.recycle()
        sourceBitmap = null
        capturedImageUri = null
        corners = emptyList()
        // Don't clear pendingImportPaths — they're processed sequentially
    }
}

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Home.route) {

        // ── Home ──────────────────────────────────────────────────────────────
        composable(Screen.Home.route) {
            HomeScreen(
                onScanClick = {
                    ScanSession.clear()
                    navController.navigate(Screen.Camera.createRoute())
                },
                onImportImage = { path ->
                    ScanSession.clear()
                    navController.navigate(Screen.Detection.createRoute(path))
                },
                onDocumentClick = { docId ->
                    navController.navigate(Screen.Document.createRoute(docId))
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        // ── Camera ────────────────────────────────────────────────────────────
        composable(
            route = Screen.Camera.route,
            arguments = listOf(navArgument("documentId") { type = NavType.StringType })
        ) { backStack ->
            val documentId = backStack.arguments?.getString("documentId") ?: "new"
            CameraScreen(
                onImageCaptured = { uri ->
                    ScanSession.capturedImageUri = uri
                    navController.navigate(Screen.Detection.createRoute(uri, documentId))
                }
            )
        }

        // ── Detection ─────────────────────────────────────────────────────────
        composable(
            route = Screen.Detection.route,
            arguments = listOf(
                navArgument("imageUri") { type = NavType.StringType },
                navArgument("documentId") { type = NavType.StringType }
            )
        ) { backStack ->
            val imageUri = backStack.arguments?.getString("imageUri") ?: ""
            val documentId = backStack.arguments?.getString("documentId") ?: "new"

            DetectionScreen(
                imageUri = imageUri,
                onConfirm = { originalBitmap, corners ->
                    ScanSession.sourceBitmap = originalBitmap
                    ScanSession.corners = corners
                    navController.navigate(Screen.Edit.createRoute(documentId))
                },
                onRetake = {
                    navController.popBackStack()
                }
            )
        }

        // ── Edit ──────────────────────────────────────────────────────────────
        // pageId = "new" → new scan from ScanSession
        // pageId = <uuid> → editing existing saved page
        composable(
            route = Screen.Edit.route,
            arguments = listOf(
                navArgument("documentId") { type = NavType.StringType },
                navArgument("pageId") { type = NavType.StringType; defaultValue = "new" }
            )
        ) { backStack ->
            val documentId = backStack.arguments?.getString("documentId")?.takeIf { it != "new" }
            val pageId = backStack.arguments?.getString("pageId")?.takeIf { it != "new" }

            EditScreen(
                bitmap = if (pageId == null) ScanSession.sourceBitmap else null,
                corners = if (pageId == null) ScanSession.corners else emptyList(),
                documentId = documentId,
                existingPageId = pageId,
                onSaved = { savedDocId ->
                    ScanSession.clear()
                    // If there are more images to import, process next one (Req 2.3)
                    val nextPath = ScanSession.pendingImportPaths.removeFirstOrNull()
                    if (nextPath != null) {
                        navController.navigate(Screen.Detection.createRoute(nextPath, savedDocId)) {
                            popUpTo(Screen.Document.createRoute(savedDocId))
                        }
                    } else {
                        navController.navigate(Screen.Document.createRoute(savedDocId)) {
                            popUpTo(Screen.Home.route)
                        }
                    }
                },
                onRetake = {
                    ScanSession.clear()
                    ScanSession.pendingImportPaths.clear()
                    navController.popBackStack(Screen.Home.route, inclusive = false)
                }
            )
        }

        // ── Document ──────────────────────────────────────────────────────────
        composable(
            route = Screen.Document.route,
            arguments = listOf(navArgument("documentId") { type = NavType.StringType })
        ) { backStack ->
            val documentId = backStack.arguments?.getString("documentId") ?: return@composable
            DocumentScreen(
                documentId = documentId,
                onAddPage = { docId ->
                    navController.navigate(Screen.Camera.createRoute(docId))
                },
                onEditPage = { docId, pageId ->
                    navController.navigate(Screen.Edit.createRoute(docId, pageId))
                },
                onExport = { docId ->
                    navController.navigate(Screen.Export.createRoute(docId))
                },
                onBack = { navController.popBackStack() }
            )
        }

        // ── Export ────────────────────────────────────────────────────────────
        composable(
            route = Screen.Export.route,
            arguments = listOf(navArgument("documentId") { type = NavType.StringType })
        ) { backStack ->
            val documentId = backStack.arguments?.getString("documentId") ?: "temp"
            ExportScreen(
                documentId = documentId,
                fallbackBitmap = if (documentId == "temp") ScanSession.sourceBitmap else null,
                onDone = {
                    if (documentId == "temp") ScanSession.clear()
                    navController.popBackStack(Screen.Home.route, inclusive = false)
                }
            )
        }

        // ── Settings ──────────────────────────────────────────────────────────
        composable(Screen.Settings.route) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
